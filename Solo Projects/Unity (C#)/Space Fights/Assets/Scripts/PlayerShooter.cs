using UnityEngine;
using System.Collections;

public class PlayerShooter : MonoBehaviour
{

    public GameObject[] prefabs;
    public static float speed = 0.75f;
    public GameObject maincanvas;
    public ParticleSystem hit;
    public AudioSource pew;
    
    void Start()
    {
        
    }
    
    void Update()
    {
        if (maincanvas.GetComponent<CanvasOperator>().state != "title")
        {
            if (Input.GetButtonDown("Jump"))
            {
                StartCoroutine(ShootLasers());
            }
        }
            
            
    }

    IEnumerator ShootLasers()
    {
        while (Input.GetButton("Jump"))
        {
            Instantiate(prefabs[0], new Vector3(gameObject.transform.position.x, gameObject.transform.position.y,gameObject.transform.position.z),
                Quaternion.identity).GetComponent<PlayerLaser>().hit = hit;
            pew.Play();
       
            yield return new WaitForSeconds(0.15f);
        }
    }
}
