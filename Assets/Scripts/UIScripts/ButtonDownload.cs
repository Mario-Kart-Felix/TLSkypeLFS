using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System.Net;
using System.ComponentModel;
using System;

public class ButtonDownload : MonoBehaviour
{

	public DownloadAssist downloadAssist;
	public Button	buttonObject;

	private GameManager gm;
	WebClient client;

	void Start ()
	{
		gm = GameManager.gmInstance;

		client = new WebClient();
		client.DownloadFileCompleted += new AsyncCompletedEventHandler(DownloadFileCompleted);
	}
	
	public void DownloadFile(GameObject buttonObject, string recievedFullName)
	{
		print("Downloading");
        client.Credentials = new NetworkCredential("maxi", "asdfghjk");

		string[] directorySplit = downloadAssist.fileAddresses[buttonObject.transform.parent.GetSiblingIndex()].Split('/');
		print(buttonObject.transform.parent.GetSiblingIndex());
		print(downloadAssist.fileAddresses[buttonObject.transform.parent.GetSiblingIndex()]);

		Uri downloadUri = new Uri(recievedFullName);
        client.DownloadFileAsync(downloadUri, downloadAssist.fileAddresses[buttonObject.transform.parent.GetSiblingIndex()]);
	}
	
	void DownloadFileCompleted (object sender, AsyncCompletedEventArgs e)
	{
		buttonObject.GetComponent<Image>().color = Color.green;
		buttonObject.onClick.RemoveAllListeners();
		buttonObject.onClick.AddListener(delegate{OpenFile(downloadAssist.fileAddresses[buttonObject.transform.parent.GetSiblingIndex()]);});
	}

	public void OpenFile (string fileAddress)
	{
		print(fileAddress);
		gm.OpenFile(fileAddress);
	}
}
