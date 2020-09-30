using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class GrabPickups : MonoBehaviour {
    public Text victory;

    void OnControllerColliderHit(ControllerColliderHit hit) {
		if (hit.gameObject.tag.Equals("Pickup")) {
            victory.color = new Color(0, 0, 0, 1);
		}
	}
}
