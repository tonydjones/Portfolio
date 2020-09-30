using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityStandardAssets.Characters.FirstPerson;

public class Drop : MonoBehaviour
{

    // Use this for initialization
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {
        if (GameObject.Find("FPSController").transform.position.y < -10)
        {
            FirstPersonController.maze = 0;
            SceneManager.LoadScene("End");
        }
    }
}
