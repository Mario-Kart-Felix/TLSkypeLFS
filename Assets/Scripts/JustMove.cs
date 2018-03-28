using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using DG.Tweening;

public class JustMove : MonoBehaviour {

	void Start () {
		transform.DOKill();
		transform.DOMoveX(-10, 1f).SetLoops(-1, LoopType.Yoyo);
	}	
}
