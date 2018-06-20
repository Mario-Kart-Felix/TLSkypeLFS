using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SyncState : MonoBehaviour 
{
	public	GameObject[]	toSync;
	void OnEnable()
	{
		foreach(GameObject go in toSync)
		{
			go.SetActive(true);
		}
	}

	void OnDisable()
	{
		foreach(GameObject go in toSync)
		{
			go.SetActive(false);
		}
	}
}
