using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ToggleHandler : MonoBehaviour
{
	public ButtonReplacer[] otherButtons;

	public void TurnOffRest()
	{
		foreach(ButtonReplacer button in otherButtons)
		{
			button.ButtonSet(false);
		}
	}
}
