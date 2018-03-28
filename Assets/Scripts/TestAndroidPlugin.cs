using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Threading;
using System.IO;
using System;

public class TestAndroidPlugin : MonoBehaviour
{
    Thread audioPlayThread;

    byte[] dataRead;
    int totalDataRead = 0;

    AndroidJavaClass androidJavaClass;
    AndroidJavaObject innerInstance;

    private bool dataPresent = false;
    private bool dataStopped = false;

	private bool stopAudio = true;

    private int counter = 0;

    private byte[] buffer;	
	private	int bufferSize = 5000;

    void Start()
    {
        audioPlayThread = new Thread(new ThreadStart(AudioPlayUpdate));

		buffer = new byte[bufferSize];

        print(Application.streamingAssetsPath);

        if (Application.platform == RuntimePlatform.Android)
        {
            androidJavaClass = new AndroidJavaClass("audiotest.takeleap.com.playsound.PlaySoundExternal");
            innerInstance = androidJavaClass.CallStatic<AndroidJavaObject>("instance");

            innerInstance.Call("InitSound");

            StartCoroutine(TestFile());
        }
    }

    IEnumerator TestFile()
    {
        string filePath = "jar:file://" + Application.dataPath + "!/assets/" + "out.wav";

        WWW www = new WWW(filePath);
        yield return www;

        if (!string.IsNullOrEmpty(www.error))
        {
            print("Can't read");
        }
        else
        {
            totalDataRead = www.bytes.Length;
            dataRead = new byte[totalDataRead];

            print("Exists File " + totalDataRead + " " + www.bytesDownloaded);

            Buffer.BlockCopy(www.bytes, 0, dataRead, 0, totalDataRead);

			stopAudio = false;

            audioPlayThread.Start();
        }
    }

    void AudioPlayUpdate()
    {
        while (true)
        {
            if (dataPresent)
            {
                Thread.Sleep(10);
                continue;
            }

            Buffer.BlockCopy(dataRead, counter, buffer, 0, bufferSize);

            dataPresent = true;
			counter += bufferSize;

			if(counter > totalDataRead)
			{
				dataStopped = true;
				break;
			}
        }
    }

    void Update()
    {
		if(stopAudio)
			return;

        if (dataPresent)
        {
            innerInstance.Call("SendData", new object[] { buffer, bufferSize });
			dataPresent = false;
        }

		if(dataStopped)
		{
			innerInstance.Call("StopSound");

			stopAudio = true;
		}
    }
}
