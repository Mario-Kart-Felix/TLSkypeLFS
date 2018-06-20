using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class StatManager : MonoBehaviour
{
	public Sprite	onSprite;

	private Sprite	offSprite;
	private Image	statImage;

	void Awake ()
	{
		statImage = GetComponent<Image>();
		offSprite = statImage.sprite;
	}
	
	public void SetStat (bool setState)
	{
		statImage.sprite = setState ? onSprite : offSprite;
	}
}
