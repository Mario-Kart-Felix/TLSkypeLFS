using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEditor;

public class Test : MonoBehaviour
{

	static Image reference;
	public static bool statusVisibility
	{
		get
		{
			return PlayerSettings.statusBarHidden;
		}

		set
		{
			PlayerSettings.statusBarHidden = value;
		}
	}

	[MenuItem("UIManager/Status Bar Set")]
	static void DoSomething()
	{
		statusVisibility = !statusVisibility;
	}

	[MenuItem("UIManager/Enable Reference _`")]
	static void EnableReference()
	{
		if(reference == null)
			reference = GameObject.FindGameObjectWithTag("Respawn").GetComponent<Image>();
		
		reference.enabled = !reference.enabled;
	}

	[MenuItem("UIManager/AddSelectedScreen")]
	static void AddSelectScreen()
	{
		GameManager gm = GameObject.FindGameObjectWithTag("Finish").GetComponent<GameManager>();

		gm.allScreens.AddScreen(Selection.activeGameObject);
	}

	[MenuItem("UIManager/RemoveSelectedItem")]
	static void RemoveSelectedItem()
	{
		GameManager gm = GameObject.FindGameObjectWithTag("Finish").GetComponent<GameManager>();
		
		gm.allScreens.RemoveScreen(Selection.activeGameObject);
	}
}
