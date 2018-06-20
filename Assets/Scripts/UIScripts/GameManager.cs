using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UIManager;
using System;

public class GameManager : MonoBehaviour
{
	public static GameManager gmInstance = null;
	public AllScreens allScreens;
	private bool init;

	void Awake ()
	{
		if(gmInstance == null)
			gmInstance = this;
		else
			Destroy(gameObject);
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
