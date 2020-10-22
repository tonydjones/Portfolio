using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class Player : MonoBehaviour
{

    public float speed;
    public Rigidbody rb;
    private float vertical, horizontal;
    public ParticleSystem explosion;
    public AudioSource explosionSound;
    public GameObject maincanvas;
    public GameObject shield;
    public AudioSource playmusic;
    public AudioSource losemusic;
    public AudioSource damage;
    public Text lose;
    public Text losestart;
    public bool invincible = false;
    public GameObject ship;
    public bool shielded = true;
    public bool dead = false;
    public GameObject powerup;
    public float x;
    public float y;

    
    void Start()
    {
       x = transform.position.x;
       y = transform.position.y;
    }
    
    void Update()
    {
        if (maincanvas.GetComponent<CanvasOperator>().state != "title" && maincanvas.GetComponent<CanvasOperator>().state != "lose")
        {
            if (Input.GetAxisRaw("Vertical") != 0)
            {

                vertical = Input.GetAxis("Vertical") * speed;

            }
            else
            {
                vertical = 0f;
            }
            
            if (Input.GetAxisRaw("Horizontal") != 0)
            {

                horizontal = Input.GetAxis("Horizontal") * speed;

            }
            else
            {
                horizontal = 0f;
            }

            transform.position = new Vector3(transform.position.x + horizontal, transform.position.y + vertical, transform.position.z);

            if (transform.position.y < -4.9f)
            {
                transform.position = new Vector3(transform.position.x, -4.9f, transform.position.z);
            }
            else if (transform.position.y > -4.11f)
            {
                transform.position = new Vector3(transform.position.x, -4.11f, transform.position.z);
            }

            if (transform.position.x < 0.16f)
            {
                transform.position = new Vector3(0.16f, transform.position.y, transform.position.z);
            }
            else if (transform.position.x > 2.05f)
            {
                ship.transform.position = new Vector3(2.05f, ship.transform.position.y, ship.transform.position.z);
            }
        }
        
    }

    public void Reset()
    {
        
        if (dead)
        {
            dead = false;
            ship.transform.position = new Vector3(ship.transform.position.x + 5, ship.transform.position.y, ship.transform.position.z);
            shielded = true;
            shield.transform.position = new Vector3(shield.transform.position.x + 5, shield.transform.position.y, shield.transform.position.z);
            transform.position = new Vector3(x, y, transform.position.z);
        }
        else if (!shielded)
        {
            shielded = true;
            shield.transform.position = new Vector3(shield.transform.position.x + 5, shield.transform.position.y, shield.transform.position.z);

        }
        invincible = false;
        StopCoroutine(Invincible());
        ship.GetComponent<MeshRenderer>().enabled = true;
    }

    public void Damage()
    {
        if (!invincible)
        {
            StartCoroutine(Invincible());
            damage.Play();
            if (shielded)
            {
                shielded = false;
                StartCoroutine(Regen());
                shield.transform.position = new Vector3(shield.transform.position.x - 5, shield.transform.position.y, shield.transform.position.z);
            }
            else
            {
                dead = true;
                StopCoroutine(Regen());
                explosion.transform.position = ship.transform.position;
                explosion.Play();
                playmusic.Stop();
                losemusic.Play();

                lose.color = new Color(255, 0, 0, 255);
                losestart.color = new Color(255, 0, 0, 255);
                maincanvas.GetComponent<CanvasOperator>().state = "lose";
                ship.transform.position = new Vector3(ship.transform.position.x - 5, ship.transform.position.y, ship.transform.position.z);


            }
        }
        
    }

    IEnumerator Regen()
    {
        yield return new WaitForSeconds(Random.Range(15.0f, 30.0f));
        while (!shielded && !dead)
        {
            if (!powerup.GetComponent<Powerup>().going){
                powerup.GetComponent<Powerup>().going = true;
            }
            yield return new WaitForSeconds(Random.Range(15.0f, 30.0f));
        }
        
    }
    

    IEnumerator Invincible()
    {
        invincible = true;
        for (int i = 0; i < 50; i++)
        {
            if (ship.GetComponent<MeshRenderer>().enabled)
            {
                ship.GetComponent<MeshRenderer>().enabled = false;
            }
            else
            {
                ship.GetComponent<MeshRenderer>().enabled = true;
            }
            yield return new WaitForSeconds(0.1f);
        }
        ship.GetComponent<MeshRenderer>().enabled = true;
        invincible = false;
    }
    
}
