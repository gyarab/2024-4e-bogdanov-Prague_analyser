package com.example.prague_analyser.FortunesALG.VoronoiGraph;

public class Event implements Comparable<Event> {

    public static int LOCAL_EVENT = 0;

    public static int CIRCLE_EVENT = 1;

    Point p;
    int type;
    Parabola arc;

    public Event(Point p, int type) {
        this.p = p;
        this.type = type;
        this.arc = null;
    }

    public int compareTo(Event e){
        return this.p.compareTo(e.p);
    }
}
