package com.example.prague_analyser.FortunesALG.VoronoiGraph;

public class Point implements  Comparable<Point>{

    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public int compareTo(Point p){
        if(y == p.y){
            if (x == p.x) return 0;
            else if(x > p.x) return 1;
            else return -1;
        }
        else if (y > p.y) return 1;
        else return -1;
    }

}
