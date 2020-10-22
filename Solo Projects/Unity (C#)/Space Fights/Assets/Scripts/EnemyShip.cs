using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class EnemyShip : MonoBehaviour
{

    public float speed;
    public Rigidbody rb;
    private float vertical;
    public ParticleSystem explosion;
    public AudioSource explosionSound;
    public int health = 850;
    public bool movingup = true;
    public AudioSource titlemusic;
    public AudioSource playmusic;
    public AudioSource victorymusic;
    public AudioSource losemusic;
    public Text victory;
    public Text victorystart;
    public GameObject maincanvas;
    public GameObject twinlaser1;
    public GameObject twinlaser2;
    public GameObject megalaser;
    public GameObject[] prefabs;
    public bool running = false;
    public AudioSource pew;

    
    void Start()
    {
    }

    public void Shoot()
    {
        if (!running)
        {
            StartCoroutine(ShootLasers());
        }
        
    }
    
    void Update()
    {
        if (health > 0)
        {
            if (movingup)
            {
                vertical = speed;
            }
            else
            {
                vertical = -1 * speed;
            }

            if (transform.position.y < -3.84f)
            {
                movingup = true;
            }
            if (transform.position.y > -3.13f)
            {
                movingup = false;
            }

            transform.position = new Vector3(transform.position.x, transform.position.y + vertical, transform.position.z);
        }
        else if (maincanvas.GetComponent<CanvasOperator>().state == "play")
        {
            Explode();
        }
        
    }

    public void Damage()
    {
        health -= 1;
    }

    IEnumerator ShootLasers()
    {
        running = true;
        yield return new WaitForSeconds(Random.Range(1.0f, 3.0f));
        
        while (maincanvas.GetComponent<CanvasOperator>().state == "play")
        {
            int pattern = Random.Range(1, 4);
            if (pattern == 1)
            {
                Instantiate(prefabs[0], new Vector3(twinlaser1.transform.position.x, twinlaser1.transform.position.y, twinlaser1.transform.position.z),
                Quaternion.identity).GetComponent<EnemyLaser>().parent = twinlaser1;
            }
            else if (pattern == 2)
            {
                Instantiate(prefabs[0], new Vector3(twinlaser2.transform.position.x, twinlaser2.transform.position.y, twinlaser2.transform.position.z),
                Quaternion.identity).GetComponent<EnemyLaser>().parent = twinlaser2;
            }
            else
            {
                Instantiate(prefabs[0], new Vector3(twinlaser1.transform.position.x, twinlaser1.transform.position.y, twinlaser1.transform.position.z),
                Quaternion.identity).GetComponent<EnemyLaser>().parent = twinlaser1;
                Instantiate(prefabs[0], new Vector3(twinlaser2.transform.position.x, twinlaser2.transform.position.y, twinlaser2.transform.position.z),
                Quaternion.identity).GetComponent<EnemyLaser>().parent = twinlaser2;
            }
            pew.Play();
            yield return new WaitForSeconds(Random.Range(2.0f, 6.0f));
            
        }
        running = false;
    }

    public void Explode()
    {
        explosionSound.Play();
        explosion.transform.position = megalaser.transform.position;
        explosion.Play();

        playmusic.Stop();
        victorymusic.Play();

        victory.color = new Color(255, 0, 0, 255);
        victorystart.color = new Color(255, 0, 0, 255);
        maincanvas.GetComponent<CanvasOperator>().state = "victory";

        transform.position = new Vector3(2.0f, transform.position.y + vertical, transform.position.z);
    }

    
}
