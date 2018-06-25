using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System.Net;
using System;
using System.IO;
using System.ComponentModel;

public class DownloadAssist : MonoBehaviour
{

	public GameObject	downloadButton;
	public string		recievedFileName;
	public List<string>	fileAddresses;
	public GameObject	downloadSpawnPoint;
	private GameObject currentButton;
	private string path = "/mnt/sdcard/TL_RecievedFiles";

	void Start ()
	{
		// if(!Directory.Exists(path))
		// {
		// 	Directory.CreateDirectory(path);
		// }
	}

	public void ToggleButton ()
	{
		downloadButton.SetActive(!downloadButton.activeInHierarchy);
	}

	public void SpawnDownload (string fileName, string fullFileName)
	{
		GameObject spawned = Instantiate(Resources.Load("Item") as GameObject);
		spawned.transform.parent = downloadSpawnPoint.transform;
		spawned.transform.localScale = Vector3.one;

		RectTransform rectTransform = spawned.GetComponent<RectTransform>();
		Vector3 position = rectTransform.localPosition;
		position.z = 0;
		rectTransform.localPosition = position;

		Text downloadName = spawned.GetComponentInChildren<Text>();
		downloadName.text = fileName;
		if(Application.isEditor)
			fileAddresses.Add(Application.dataPath + "/" + fileName);
		else if (Application.platform == RuntimePlatform.Android)
			fileAddresses.Add(Application.persistentDataPath + "/" + fileName);

		Button downloadButton = spawned.GetComponentInChildren<Button>();
		ButtonDownload download = spawned.GetComponentInChildren<ButtonDownload>();
		download.downloadAssist = this;
		download.buttonObject = spawned.GetComponentInChildren<Button>();

		downloadButton.onClick.AddListener(delegate{download.DownloadFile(downloadButton.gameObject, fullFileName);});
	}
}
