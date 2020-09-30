package edu.harvard.cs50.pokedex;

import java.io.Serializable;

public class Pokemon implements Serializable {
    private String name;
    private String url;
    public Boolean caught;

    Pokemon(String name, String url) {
        this.name = name;
        this.url = url;
        this.caught = false;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getCaught() {
        return caught;
    }

    public void changeCaught() {
        if (this.caught == false) {
            this.caught = true;
        }
        else{
            this.caught = false;
        }
    }
}
