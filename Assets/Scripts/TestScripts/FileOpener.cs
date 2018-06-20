using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.IO;

public class FileOpener : MonoBehaviour
{
	private string path;
	void Start ()
	{
		if(!Application.isEditor)
			{
			path = Application.persistentDataPath + "/RecievedFiles";
			if(!Directory.Exists(path))
			{
				Directory.CreateDirectory(path);
			}
		}
	}
	
	public void Open ()
	{
		print(path);
		print(File.Exists(path + "/1.jpg"));
		Application.OpenURL("content://" + path + "/1.jpg");
	}
}
