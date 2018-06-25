using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System;
using UnityEngine.Events;

public class ButtonReplacer : MonoBehaviour
{
	public Sprite	onSprite;
	public Sprite	offSprite;
	public Color	onTextColor;
	public bool		isToggle;
	public bool		toggled;
	public UnityEvent	buttonClickEvent;
	public UnityEvent	buttonToggleEvent;

	private GameManager gmInstance;
	private	Image	buttonImage;
	private Text	buttonText;
	private Color	offTextColor;
	private bool	toClick = true;

	private string buttonString
	{
		set
		{
			buttonText.text = value;
		}
	}

	private Color textColor
	{
		set
		{
			buttonText.color = value;
		}
	}

	private Sprite	buttonSprite
	{
		set
		{
			buttonImage.sprite = value;
		}
	}

	void Awake ()
	{
		buttonImage = GetComponent<Image>();
		buttonText = GetComponentInChildren<Text>();
		if(buttonText != null)
			offTextColor = buttonText.color;
	}

	void Start ()
	{
		gmInstance = GameManager.gmInstance;
	}

	public void OnButtonPressed ()
	{		
		if(!isToggle)
		{
			buttonSprite = onSprite;
			textColor = onTextColor;
		}
		else
		{
			buttonSprite = !toggled ? onSprite : offSprite;
		}

		toClick = true;
	}

	public void OnButtonReleased ()
	{
		if(!toClick)
		{
			return;
		}

		if(!isToggle)
		{
			buttonSprite = offSprite;
			textColor = offTextColor;

			buttonClickEvent.Invoke();
		}
		else
		{
			toggled = !toggled;

			buttonToggleEvent.Invoke();

			GetComponent<ToggleHandler>().TurnOffRest();
		}

	}

	public void PasswordNumber ()
	{
		gmInstance.PasswordEnter(Int32.Parse(buttonText.text));
	}

	public void CallLogScreen ()
	{
		if(toggled)
			gmInstance.allScreens.TurnScreen(3, true, true);
	}

	public void SearchScreen ()
	{
		if(toggled)
			gmInstance.allScreens.TurnScreen(2, true, true);
	}

	public void CallExpertScreen()
	{
		if(toggled)
			gmInstance.allScreens.TurnScreen(4, true, true);
	}

	public void ButtonSet (bool setState)
	{
		buttonSprite = setState ? onSprite : offSprite;
		toClick = false;
		toggled = false;
	}

	public void OnButtonCancelled ()
	{
		if(!isToggle)
		{
			toClick = false;

			buttonSprite = offSprite;
			textColor = offTextColor;
		}
		else
		{
			toClick = false;
			buttonSprite = toggled ? onSprite : offSprite;
		}
	}
}
