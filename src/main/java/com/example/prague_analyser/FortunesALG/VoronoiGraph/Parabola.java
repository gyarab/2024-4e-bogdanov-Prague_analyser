package com.example.prague_analyser.FortunesALG.VoronoiGraph;

public class Parabola {

    public static int CENTER = 0;
    public static int VERTEX = 1;


    int type;
    Point point; //if is center of parabola
    Edge edge;
    Event event;

    Parabola parent;
    Parabola leftChild;
    Parabola rightChild;

    public Parabola(){
        type = VERTEX;
    }

    public Parabola(Point p){
        point = p;
        type = CENTER;
    }

    public void setLeftChild(Parabola leftChild) {
        this.leftChild = leftChild;
        leftChild.parent = this;
    }

    public void setRightChild(Parabola rightChild) {
        this.rightChild = rightChild;
        rightChild.parent = this;
    }


    public static Parabola getLeft(Parabola p){
        return getLeftChild(getLeftParent(p));
    }

    public static Parabola getRight(Parabola p){
        return getRightChild(getRightParent(p));
    }

    public static Parabola getLeftParent(Parabola p){
        Parabola parent = p.parent;
        if (parent == null) return null;
        Parabola last = p;
        while (parent.leftChild == last) {
            if(parent.parent == null) return null;
            last = parent;
            parent = parent.parent;
        }
        return parent;
    }

    public static Parabola getRightParent(Parabola p) {
        Parabola parent = p.parent;
        if (parent == null) return null;
        Parabola last = p;
        while (parent.rightChild == last) {
            if(parent.parent == null) return null;
            last = parent;
            parent = parent.parent;
        }
        return parent;
    }

    public static Parabola getLeftChild(Parabola p) {
        if (p == null) return null;
        Parabola child = p.leftChild;
        while(child.type == VERTEX) child = child.rightChild;
        return child;
    }

    public static Parabola getRightChild(Parabola p) {
        if (p == null) return null;
        Parabola child = p.rightChild;
        while (child.type == VERTEX) child = child.leftChild;
        return child;
    }

}
