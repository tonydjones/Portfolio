package com.example.cbtaid;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Emotion implements Serializable {
    public String emotion;
    public int rating;
    public String belief;
    public int beliefrating;
    public Set<String> errors;
    public int rethink;
    public Map<String, Integer> responses;

    Emotion(String emotion, int rating){
        this.emotion = emotion;
        this.rating = rating;
        this.belief = "";
        this.beliefrating = 0;
        this.errors = new HashSet<>();
        this.rethink = 0;
        this.responses = new HashMap<>();
    }
}
