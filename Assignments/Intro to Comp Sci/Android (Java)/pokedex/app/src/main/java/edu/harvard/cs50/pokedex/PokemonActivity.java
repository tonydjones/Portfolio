package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import static java.sql.DriverManager.println;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView caught;
    private String url;
    private RequestQueue requestQueue;
    private Pokemon pkmn;
    public List<String> caught_pkmn = new ArrayList<>();
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    public ImageView image;
    public String imageurl;
    public TextView description;
    public String descurl;
    public String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        caught = findViewById(R.id.pokemon_caught);
        pkmn = (Pokemon) getIntent().getSerializableExtra("pkmn");
        image = findViewById(R.id.sprite);
        pref = getApplicationContext().getSharedPreferences("pref", 0); // 0 - for private mode
        editor = pref.edit();
        description = findViewById(R.id.pokemon_desc);


        load();
    }
    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");
        if (pref.getBoolean(pkmn.getName(), false)) {
            caught.setText("Release");
        } else {
            caught.setText("Catch");
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    JSONObject imagedict = response.getJSONObject("sprites");
                    imageurl = imagedict.getString("front_default");
                    new DownloadSpriteTask().execute(imageurl);
                    number = String.format("%d", response.getInt("id"));
                    descurl = "https://pokeapi.co/api/v2/pokemon-species/" + number + "/";

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        } else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }

                    JsonObjectRequest desc = new JsonObjectRequest(Request.Method.GET, descurl, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject message) {
                            try {
                                JSONArray flavor = message.getJSONArray("flavor_text_entries");
                                for (int i = 0; i < flavor.length(); i++) {
                                    JSONObject info = flavor.getJSONObject(i);
                                    JSONObject lang = info.getJSONObject("language");
                                    String language = lang.getString("name");
                                    String text = info.getString("flavor_text");
                                    if (language.equals("en")) {
                                        description.setText(text);
                                        break;
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("cs50", "Pokemon description error ", error);
                        }
                    });
                    requestQueue.add(desc);

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });





        requestQueue.add(request);


    }

    public void load2() {
        JsonObjectRequest desc = new JsonObjectRequest(Request.Method.GET, descurl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject message) {
                try {
                    JSONArray flavor = message.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < flavor.length(); i++) {
                        JSONObject info = flavor.getJSONObject(i);
                        JSONObject lang = info.getJSONObject("language");
                        String language = lang.getString("name");
                        String text = info.getString("flavor_text");
                        if (language.equals("en")) {
                            description.setText(text);
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon description error ", error);
            }
        });


        requestQueue.add(desc);
    }



    public void toggleCatch(View view) {
        Button status = (Button)findViewById(R.id.pokemon_caught);
        if (status.getText().equals("Catch")) {
            status.setText("Release");
            editor.putBoolean(pkmn.getName(), true);
            editor.commit();
        }
        else{
            status.setText("Catch");
            editor.remove(pkmn.getName());
            editor.commit();
        }
        }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            image.setImageBitmap(bitmap);
        }
    }
}
