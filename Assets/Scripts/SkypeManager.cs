using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using System.Net;
using System.IO;
using UnityEngine.UI;
using System.Text;
using System.Threading;

public class SkypeManager : MonoBehaviour
{
    private NetworkView networkView;

    public static SkypeManager Instance = null;

    public VideoReceiver videoReceiver;

    public VideoSender videoSender;

    public bool isCaller = true;

    public GameObject buttonCanvas;

    public GameObject mainCanvas;

    public List<string> filestoSend = new List<string>();

    public GameObject receivedFilesObject;
    public Text receivedFileNameText;
    private string receivedFileName;
    private string receivedFileFullName;
    public Text downloadedNotificationText;

    public string receivedfilesDirPath = "";

    void Awake()
    {
        if (Instance == null)
            Instance = this;
    }

    void Start()
    {
        Application.OpenURL("file://" + WWW.EscapeURL(Application.persistentDataPath) + "/Document.pdf");
        print(Application.persistentDataPath);

        if(File.Exists("/mnt/sdcard/ReceivedFiles/Document.pdf"))
        {
            print("File exists da");

            Application.OpenURL("file://mnt/sdcard/ReceivedFiles/Document.pdf");
        }
        else
        {
            print("Not found");
        }

        mainCanvas.SetActive(false);
        buttonCanvas.SetActive(true);

        networkView = GetComponent<NetworkView>();
        Network.Connect("13.126.154.86", 7777);

        StopCoroutine("FileUploader");
        StartCoroutine("FileUploader");

        if (Application.platform == RuntimePlatform.WindowsEditor || Application.platform == RuntimePlatform.WindowsPlayer)
        {
            receivedfilesDirPath = Application.dataPath + "/ReceivedFiles";
        }
        else if (Application.platform == RuntimePlatform.Android)
        {
            receivedfilesDirPath = "/mnt/sdcard/ReceivedFiles";
        }

        if (!Directory.Exists(receivedfilesDirPath))
            Directory.CreateDirectory(receivedfilesDirPath);
    }

    public void DownloadReceivedFile()
    {
        Thread fileDownloadThread = new Thread(FileDownloadThreadFunction);
        fileDownloadThread.Start();
    }

    void HideDownloadedNotificationText()
    {
        downloadedNotificationText.gameObject.SetActive(false);
    }

    void FileUploadThreadFunction()
    {
        string myFilePath = filestoSend[0];
        string[] splitNames = myFilePath.Split(new char[] { '\\' });
        string serverPath = "ftp://123.176.34.172/" + splitNames[splitNames.Length - 1];

        filestoSend.RemoveAt(0);

        FtpWebRequest request = (FtpWebRequest)WebRequest.Create(serverPath);
        request.Method = WebRequestMethods.Ftp.UploadFile;

        request.Credentials = new NetworkCredential("maxi", "asdfghjk");
        StreamReader sourceStream = new StreamReader(myFilePath);
        // print(Application.persistentDataPath + "/" + myFilePath + " ftp location file");

        BinaryReader binaryReader = new BinaryReader(sourceStream.BaseStream);
        int length = (int)sourceStream.BaseStream.Length;

        byte[] datas = binaryReader.ReadBytes(length);
        print(datas.Length);

        sourceStream.Close();

        Stream requestStream = request.GetRequestStream();
        requestStream.Write(datas, 0, datas.Length);
        requestStream.Close();

        SendFile(serverPath);
    }

    void FileDownloadThreadFunction()
    {
        receivedFilesObject.SetActive(false);

        WebClient client = new WebClient();
        client.Credentials = new NetworkCredential("maxi", "asdfghjk");

        client.DownloadFile(receivedFileFullName, receivedfilesDirPath + "/" + receivedFileName);

        downloadedNotificationText.gameObject.SetActive(true);
        downloadedNotificationText.text = "Downloaded File " + receivedFileName;

        CancelInvoke("HideDownloadedNotificationText");
        Invoke("HideDownloadedNotificationText", 5f);
    }

    IEnumerator FileUploader()
    {
        while (true)
        {
            if (filestoSend.Count > 0)
            {
                Thread fileUploadThread = new Thread(FileUploadThreadFunction);
                fileUploadThread.Start();
            }

            yield return new WaitForSeconds(0.1f);
        }
    }

    [RPC]
    void FinishedSending(string guid, string fullFileName)
    {
        print("GOT FILE " + fullFileName);

        receivedFilesObject.SetActive(true);

        receivedFileFullName = fullFileName;
        string[] splitNames = fullFileName.Split(new char[] { '/' });
        receivedFileName = splitNames[splitNames.Length - 1];
        receivedFileNameText.text = receivedFileName;
    }

    void OnDestroy()
    {
        Instance = null;
    }

    public void CallerPressed(int value)
    {
        isCaller = (value == 1);

        buttonCanvas.SetActive(false);
        mainCanvas.SetActive(true);

        videoSender.StartVideoSender();
        videoReceiver.StartVideoReceiver();
    }

    public void SendFile(string fullFileName)
    {
        networkView.RPC("FinishedSending", RPCMode.Others, Network.player.guid, fullFileName);
    }

    public void UploadFile(string fullFileName)
    {
        if (!File.Exists(fullFileName))
            return;

        filestoSend.Add(fullFileName);
        print(filestoSend[0]);
    }

    void Update()
    {
        // if (Network.connections.Length > 0)
        // {
        //     print("YES");
        //     // NetworkPlayer networkPlayer = Network.connections[0];

        //     // print(networkPlayer.guid);
        // }
    }
}
