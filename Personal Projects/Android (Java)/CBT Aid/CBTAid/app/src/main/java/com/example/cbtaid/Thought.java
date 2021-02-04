package com.example.cbtaid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Thought implements Serializable, Comparable<Thought> {
    public Date date;
    public String situation;
    public List<Emotion> emotions;

    Thought(){
        this.situation = "";
        this.emotions = new ArrayList<>();
        this.date = Calendar.getInstance().getTime();
    }

    @Override
    public int compareTo(Thought o) {
        int comp = this.date.compareTo(o.date);
        return comp;
    }
}
