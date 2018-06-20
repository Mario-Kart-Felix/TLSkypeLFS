using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace UIManager
{
	[System.Serializable]
	public class AllScreens
	{
		public List<ScreenDetails> screens;
		private GameObject	previousScreen;

		public void AddScreen(GameObject screen)
		{
			if(!screens.Exists(e => e.screenName == screen.name))
			{
				screens.Add(new ScreenDetails(screen));
			}
			else
			{
				Debug.LogError("Selected screen Already Exists");
			}
		}

		public void RemoveScreen (GameObject screen)
		{
			int screenToRemove = screens.FindIndex(e => e.screenName == screen.name);
			if(screens.Count < 1)
			{
				Debug.LogError("Screen List is Empty");
				return;
			}
			else if(!screens.Exists(e => e.screenName == screen.name))
			{
				Debug.LogError("No Such object exists");
				return;
			}
			screens.RemoveAt(screenToRemove);
		}

		public void TurnScreen (string screenName, bool setToState)
		{
			GameObject currentScreen = screens.Find(e => e.screenName == screenName).screens;
			currentScreen.SetActive(setToState);

			previousScreen = currentScreen;
		}

		public void TurnScreen(int screenIndex, bool setToState)
		{
			GameObject currentScreen = screens[screenIndex].screens;
			currentScreen.SetActive(setToState);
			
			previousScreen = currentScreen;
		}

		public void TurnScreen (string screenName, bool setToState, bool TurnOffPrevious)
		{			
			GameObject currentScreen = screens.Find(e => e.screenName == screenName).screens;
			currentScreen.SetActive(setToState);

			if(TurnOffPrevious && previousScreen != null)
				previousScreen.SetActive(!TurnOffPrevious);

			previousScreen = currentScreen;
		}

		public void TurnScreen(int screenIndex, bool setToState, bool TurnOffPrevious)
		{
			if(TurnOffPrevious && previousScreen != null)
				previousScreen.SetActive(!TurnOffPrevious);

			GameObject currentScreen = screens[screenIndex].screens;
			currentScreen.SetActive(setToState);
			
			previousScreen = currentScreen;
		}

		public void TurnOffAllBut (int indexOfScreenToBeLeftOn)
		{
			GameObject currentScreen = screens[indexOfScreenToBeLeftOn].screens;

			for(int i = 0; i < screens.Count; i++)
			{
				if(i == indexOfScreenToBeLeftOn)
					screens[i].screens.SetActive(true);
				else
					screens[i].screens.SetActive(false);
			}

			previousScreen = screens[indexOfScreenToBeLeftOn].screens;
		}
	}

	[System.Serializable]
	public class ScreenDetails
	{
		public	GameObject	screens;
		[HideInInspector]	public	string		screenName;

		public ScreenDetails (GameObject screen)
		{
			screens = screen;
			screenName = screen.name;
		}
	}
}
