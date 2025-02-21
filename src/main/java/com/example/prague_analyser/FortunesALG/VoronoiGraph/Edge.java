package com.example.prague_analyser.FortunesALG.VoronoiGraph;

public class Edge {

   public Point start;
   public Point end;
   public Point left;
   public Point right;
   public Point direction;

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

   public void setEnd(Point end, double width, double height) {
       //zkontrolujeme prusecik s hranici
       if(((end.y >= 0) && (end.y <= height)) && ((end.x >= 0) && (end.x < width))){this.end = end;}
       //rekurzivne zkontrolujeme, kdyby po vypoctu nesedela podminka
       //y = a*x + b
       else if(end.y < 0){
           double a = (end.y - start.y) / (end.x - start.x);
           double b = start.y - (a * start.x);
           double v = (0 - b) / a;
           setEnd(new Point(v, 0), width, height);
       }else if(end.y > height){
           double a = (end.y - start.y) / (end.x - start.x);
           double b = start.y - (a * start.x);
           double v = (0 - b) / a;
           setEnd(new Point(v, height), width, height);
       }
       if(end.x < 0){
           double a = (end.x - start.x)/(end.y - start.y);
           double b = start.x - (a * start.y);
           double v = (0 - b) / a;
           setEnd(new Point(width, v), width, height);
       }else if(end.x > width){
           double a = (end.x - start.x)/(end.y - start.y);
           double b = start.x - (a * start.y);
           double v = (width - b) / a;
           setEnd(new Point(width, v), width, height);
       }
   }
}
