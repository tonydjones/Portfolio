using UnityEngine;
using System.Collections;

public class EnemyLaser : MonoBehaviour
{
    public GameObject parent;
    
    void Start()
    {

    }
    
    void Update()
    {
        if (transform.position.x < -3.0f)
        {
            Destroy(gameObject);
        }
        else
        {
            if (transform.position.x > -1.0f)
            {
                transform.position = new Vector3(transform.position.x, parent.transform.position.y, transform.position.z);
            }
                
            transform.Translate(-PlayerShooter.speed * 1.5f * Time.deltaTime, 0, 0);
            
        }
    }

    void OnTriggerEnter(Collider other)
    {
        other.transform.parent.gameObject.GetComponent<Player>().Damage();
    }
}
