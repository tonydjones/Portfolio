using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityStandardAssets.Characters.FirstPerson;
using UnityEngine.SceneManagement;

public class Destroyer : MonoBehaviour
{

    // Use this for initialization
    void Start()
    {

    }

    void Update()
    {
        if (SceneManager.GetActiveScene().name.ToString().Equals("End"))
        {
            Destroy(gameObject);
        }
    }
}
