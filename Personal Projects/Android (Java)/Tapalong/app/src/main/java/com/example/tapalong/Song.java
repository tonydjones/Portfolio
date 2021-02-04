package com.example.tapalong;

import java.io.Serializable;

public class Song implements Serializable, Comparable<Song> {
    public String location;
    public String title;

    Song(String location, String title) {
        this.location = location;
        this.title = title;
    }

    @Override
    public int compareTo(Song song) {

        int comp = this.title.toUpperCase().compareTo(song.title.toUpperCase());
        return comp;
    }
}
