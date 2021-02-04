using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;
using System.Collections;

public class CanvasOperator : MonoBehaviour
{

    public string state = "title";
    public Text title;
    public Text titlestart;
    public Text victory;
    public Text victorystart;
    public Text lose;
    public Text losestart;
    public AudioSource titlemusic;
    public AudioSource playmusic;
    public AudioSource victorymusic;
    public AudioSource losemusic;
    public GameObject player;
    public GameObject enemy;
    public GameObject[] prefabs;

    
    void Start()
    {
        titlemusic.Play();
    }
    
    void Update()
    {
        if (Input.GetButtonDown("Jump"))
        {
            if (state == "title")
            {
                title.color = new Color(255, 0, 0, 0);
                titlestart.color = new Color(255, 0, 0, 0);
                titlemusic.Stop();
                enemy.transform.position = new Vector3(0.8695403f, -3.47f, enemy.transform.position.z);
                playmusic.Play();
                state = "play";
                enemy.GetComponent<EnemyShip>().Shoot();
            }
            else if (state == "victory")
            {
                victory.color = new Color(255, 0, 0, 0);
                victorystart.color = new Color(255, 0, 0, 0);
                victorymusic.Stop();
                enemy.GetComponent<EnemyShip>().health = 850;
                enemy.transform.position = new Vector3(0.8695403f, -3.47f, enemy.transform.position.z);
                player.GetComponent<Player>().Reset();
                playmusic.Play();
                state = "play";
                enemy.GetComponent<EnemyShip>().Shoot();
            }
            else if (state == "lose")
            {
                lose.color = new Color(255, 0, 0, 0);
                losestart.color = new Color(255, 0, 0, 0);
                losemusic.Stop();
                enemy.GetComponent<EnemyShip>().health = 850;
                player.GetComponent<Player>().Reset();
                playmusic.Play();
                state = "play";
                enemy.GetComponent<EnemyShip>().Shoot();
            }
        }
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            if (UnityEditor.EditorApplication.isPlaying)
            {
                UnityEditor.EditorApplication.isPlaying = false;
            }
            else
            {
                Application.Quit();
            }
        }
    }
}
