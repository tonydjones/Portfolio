using UnityEngine;
using System.Collections;

public class PlayerLaser : MonoBehaviour
{
    public ParticleSystem hit;
    
    void Start()
    {

    }
    
    void Update()
    {
        
        if (transform.position.x > 2.0f)
        {
            Destroy(gameObject);
        }
        else
        {
            transform.Translate(PlayerShooter.speed * Time.deltaTime, 0, 0);
        }
    }

    void OnTriggerEnter(Collider other)
    {
        hit.transform.position = new Vector3(transform.position.x + 0.1f, transform.position.y, transform.position.z);
        hit.Play();
        Destroy(gameObject);
        other.transform.parent.gameObject.GetComponent<EnemyShip>().Damage();
    }
}
