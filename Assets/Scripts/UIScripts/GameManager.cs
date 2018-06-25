using System;
using System.IO;
using UIManager;
using UnityEngine;
using System.Text;
using UnityEngine.UI;
using System.Collections;
using System.Collections.Generic;

public class GameManager : MonoBehaviour
{
	public static GameManager gmInstance = null;
	public AllScreens allScreens;

	private bool init;
	private static string downloadDirectory;

	[SerializeField]	private	RectTransform	callerRect;
	[SerializeField]	private	RectTransform	senderRect;
	public bool swapped;

	#region | JNIVariables
    AndroidJavaClass unityClass;
    AndroidJavaClass pluginClass;
    AndroidJavaObject pluginObject;

	IntPtr instancePtr;
	IntPtr currentActivityPtr;
	#endregion

	void Awake ()
	{
		if(gmInstance == null)
			gmInstance = this;
		else
			Destroy(gameObject);
		
		if(Application.platform == RuntimePlatform.Android)
			InitPlugin();
	}

	void Start ()
	{
		Initialise();
	}
	
	void Update ()
	{
		if(Input.GetKeyUp(KeyCode.Mouse0) && !init)
		{
			init = true;
			allScreens.TurnScreen(1, true, true);
		}
	}

	void Initialise ()
	{
		allScreens.TurnOffAllBut(0);
	}

	public void SwapCams ()
	{
		print("Swapping");
		Vector3 callerV3Temp = callerRect.position;
		Vector3 senderV3Temp = senderRect.position;

		Vector2 callerV2Temp = callerRect.anchoredPosition;
		Vector2 senderV2Temp = senderRect.anchoredPosition;

		Vector3 callerv3Temp = callerRect.localScale;
		Vector3 senderv3Temp = senderRect.localScale;

		Vector2 callerV2Offset = callerRect.offsetMax;
		Vector2 senderV2Offset = senderRect.offsetMax;
		
		Vector2 callerV2OffsetMin = callerRect.offsetMin;
		Vector2 senderV2OffsetMin = senderRect.offsetMin;

		int callerPos = callerRect.transform.GetSiblingIndex();
		int senderPos = senderRect.transform.GetSiblingIndex();

		callerRect.position = senderV3Temp;
		senderRect.position = callerV3Temp;

		callerRect.anchoredPosition = senderV2Temp;
		senderRect.anchoredPosition = callerV2Temp;

		callerRect.localScale = senderv3Temp;
		senderRect.localScale = callerv3Temp;

		callerRect.offsetMax = senderV2Offset;
		senderRect.offsetMax = callerV2Offset;

		callerRect.offsetMin = senderV2OffsetMin;
		senderRect.offsetMin = callerV2OffsetMin;

		callerRect.transform.SetSiblingIndex(senderPos);
		senderRect.transform.SetSiblingIndex(callerPos);

	}

	#region | Plugin Functions
	public void InitPlugin()
	{
        unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		pluginClass = new AndroidJavaClass("com.TL.openfile.FileOpener");

		instancePtr = AndroidJNI.CallStaticObjectMethod(pluginClass.GetRawClass(), AndroidJNI.GetStaticMethodID(pluginClass.GetRawClass(), "instance",
													"()Lcom/TL/openfile/FileOpener;"), new jvalue[] { });

        // currentActivityPtr = AndroidJNI.GetStaticObjectField
		// (
		// 	unityClass.GetRawClass(), AndroidJNI.GetStaticFieldID
		// 	(
		// 		unityClass.GetRawClass(), "currentActivity", "Landroid/app/Activity;"
		// 	)
		// );

		downloadDirectory = AndroidJNI.CallStaticStringMethod
		(
			instancePtr, AndroidJNI.GetMethodID
			(
				pluginClass.GetRawClass(), "GetDownloadDirectory", "()Ljava/lang/String;"
			),
			new jvalue[]
			{

			}
		);
		print("DownloadDirectory " + downloadDirectory);
		downloadDirectory = "/storage/emulated/0/";
		print("DownloadDirectory " + downloadDirectory);
	}

	public void CallPlugin ()
	{
		// instance sig ()Lcom/TL/openfile/FileOpener;
		// function sig (Ljava/lang/String;Landroid/content/Context;)V
		// fileOpen sig (Ljava/lang/String;Landroid/content/Context;)V
		// get download dir sig ()Ljava/lang/String;

		// print(currentActivityPtr);
	}

	public void OpenFile (string fileName)
	{
		// if(!File.Exists(fileName))
		// {
		// 	print("No Such file");
		// 	return;
		// }
		instancePtr = AndroidJNI.CallStaticObjectMethod(pluginClass.GetRawClass(), AndroidJNI.GetStaticMethodID(pluginClass.GetRawClass(), "instance",
											"()Lcom/TL/openfile/FileOpener;"), new jvalue[] { });

        currentActivityPtr = AndroidJNI.GetStaticObjectField
		(
			unityClass.GetRawClass(), AndroidJNI.GetStaticFieldID
			(
				unityClass.GetRawClass(), "currentActivity", "Landroid/app/Activity;"
			)
		);

		IntPtr strID = AndroidJNI.NewStringUTF(fileName);
		AndroidJNI.CallVoidMethod
		(
			instancePtr, AndroidJNI.GetMethodID
			(
				pluginClass.GetRawClass(), "OpenFile", "(Ljava/lang/String;Landroid/content/Context;)V"
			),
			new jvalue[]
			{ 
				new jvalue() 
				{ 
					l = strID
				}, 
				new jvalue ()
				{
					l = currentActivityPtr
				} 
			}
		);
	}
	#endregion
	#region | Password Screen

	[Header("PasswordScreen")]

	public StatManager[] passwordStats;
	public string	correctPassword;

	private string password = "0000";
	private int passwordIndex = 0;

	public void PasswordEnter (int number)
	{
		password = password.Insert(passwordIndex, number.ToString());
		password = password.Remove(password.Length - 1);

		passwordStats[passwordIndex].SetStat(true);

		passwordIndex++;

		if(passwordIndex > 3)
		{
			passwordIndex = 0;
			if(correctPassword != password)
			{
				ResetPasswordStat();
			}
			else
			{
				allScreens.TurnScreen(2, true, true);
			}
		}
	}

	void ResetPasswordStat()
	{
		foreach(StatManager Stat in passwordStats)
		{
			Stat.SetStat(false);
		}
	}
#endregion
}
