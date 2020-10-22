using UnityEngine;
using System.Collections;

public class BigShip : MonoBehaviour
{
    
    void Start()
    {

    }
    void Update()
    {
        
    }

    void OnTriggerEnter(Collider other)
    {
        if (other.transform.parent)
        {
           other.transform.parent.gameObject.GetComponent<Player>().Damage();
        }
    }
}
