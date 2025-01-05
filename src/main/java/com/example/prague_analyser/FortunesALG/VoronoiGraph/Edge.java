package com.example.prague_analyser.FortunesALG.VoronoiGraph;

public class Edge {

    Point start;
    Point end;
    Point left;
    Point right;
    Point direction;

    Edge neighbour; // same vector, but opposite direction

    double slope;
    double y;

    public Edge(Point first, Point left, Point right){
        start = first;
        this.left = left;
        this.right = right;

        direction = new Point(right.y - left.y, left.x - right.x);

        end = null;

        slope = (right.x - left.x)/(left.y - right.y);

        Point middle = new Point((right.x + left.x)/2, (right.y + left.y)/2);
        y = middle.y - slope* middle.x;
    }

}
