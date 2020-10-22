using UnityEngine;
using System.Collections;

public class Powerup : MonoBehaviour
{
    public bool going = false;
    public AudioSource sound;
    
    void Start()
    {
        transform.position = new Vector3(1.34f, Random.Range(0.66f, 1.34f), transform.position.z);
    }
    
    void Update()
    {
        if (going)
        {
            if (transform.position.x > -1.25)
            {
                transform.Translate(-PlayerShooter.speed * Time.deltaTime, 0, 0);
            }
            else
            {
                going = false;
                transform.position = new Vector3(1.34f, Random.Range(0.66f, 1.34f), transform.position.z);
            }
        }
        
    }

    void OnTriggerEnter(Collider other)
    {
        other.transform.parent.gameObject.GetComponent<Player>().Reset();
        sound.Play();
        transform.position = new Vector3(1.34f, Random.Range(0.66f, 1.34f), transform.position.z);
        going = false;
    }
}
