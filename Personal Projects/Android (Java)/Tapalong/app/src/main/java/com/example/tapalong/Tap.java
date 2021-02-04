package com.example.tapalong;

import java.io.Serializable;

public class Tap implements Serializable, Comparable<Tap> {
    public String title;
    public String data;
    public String location;

    Tap(String title, String location, String data){
        this.title = title;
        this.data = data;
        this.location = location;
    }

    @Override
    public int compareTo(Tap tap) {

        int comp = this.title.toUpperCase().compareTo(tap.title.toUpperCase());
        return comp;
    }
}
