package com.example.prague_analyser.FortunesALG.VoronoiGraph;

import java.util.*;

public class EventHandler {

    List<Point> sites;
    public List <Edge> edges;
    PriorityQueue<Event> events;
    Parabola root;

    double CORNER_EDGE_WIDTH;
    double CORNER_EDGE_HEIGHT;

    double width = 1;
    double height = 1;

    double currentY;

    public EventHandler(List<Point> sites, double CORNER_EDGE_WIDTH, double CORNER_EDGE_HEIGHT){
        this.CORNER_EDGE_HEIGHT = CORNER_EDGE_HEIGHT;
        this.CORNER_EDGE_WIDTH = CORNER_EDGE_WIDTH;
        this.sites = sites;
        edges = new ArrayList<Edge>();
        generateVoronoi();
    }

    // Main method for generating the Voronoi diagram using Fortune's algorithm
    private void  generateVoronoi(){

        // Remove duplicate points
        removeDuplicatePoints();

        // Perturb points with the same y-coordinate to ensure uniqueness
        perturbPoints();

        // Sort sites by y-coordinate (descending), and by x-coordinate if y-coordinates are equal
        Collections.sort(sites, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                if (p1.y == p2.y) {
                    return Double.compare(p1.x, p2.x); // Sort by x-coordinate if y-coordinates are equal
                }
                return Double.compare(p2.y, p1.y); // Sort in descending order of y-coordinate
            }
        });

        // Initialize the event queue with site events
        events = new PriorityQueue<Event>();
        for (Point p : sites) {
            events.add(new Event(p, Event.LOCAL_EVENT));
        }

        // process events (sweep line)
        while (!events.isEmpty()) {
            Event e = events.remove();
            // Update the sweep line position
            currentY = e.p.y;
            if (e.type == Event.LOCAL_EVENT) {
                handleSite(e.p);
            } else {
                handleCircle(e);
            }
        }

        // Complete remaining edges after processing all events
        currentY = CORNER_EDGE_HEIGHT + CORNER_EDGE_WIDTH;

        // close off any dangling edges
        endEdges(root);

        // get rid of those crazy infinite lines
        for (Edge e : edges) {
            if (e.neighbour != null) {
                e.start = e.neighbour.end;
                e.neighbour = null;
            }
        }
    }

    // Remove duplicate points
    private void removeDuplicatePoints() {
        Set<Point> uniquePoints = new HashSet<>();
        List<Point> uniqueSites = new ArrayList<>();

        for (Point p : sites) {
            if (!uniquePoints.contains(p)) {
                uniquePoints.add(p);
                uniqueSites.add(p);
            }
        }
        sites = uniqueSites;
    }

    // Perturb points with the same y-coordinate to ensure uniqueness
    private void perturbPoints() {
        double epsilon = 1e-10; // Small perturbation value
        Map<Double, Integer> yCount = new HashMap<>();

        for (Point p : sites) {
            if (yCount.containsKey(p.y)) {
                yCount.put(p.y, yCount.get(p.y) + 1);
                p.y += yCount.get(p.y) * epsilon; // Perturb y-coordinate
            } else {
                yCount.put(p.y, 0);
            }
        }
    }


    // Recursively close off edges that extend to infinity
    private void endEdges(Parabola p) {
        if (p.type == Parabola.CENTER) {
            p = null;
            return;
        }

        double x = getXofEdge(p);
        p.edge.setEnd(new Point(x, p.edge.slope * x + p.edge.y), CORNER_EDGE_WIDTH, CORNER_EDGE_HEIGHT);
        edges.add(p.edge);

        endEdges(p.leftChild);
        endEdges(p.rightChild);

        p=null;
    }



    // Handles a site event (new point added to the beach line)
    private void handleSite(Point p) {
        // base case
        if (root == null) {
            root = new Parabola(p);
            return;
        }

        // find parabola on beach line right above p
        Parabola par = getParabolaByX(p.x);
        if (par.event != null) {
            events.remove(par.event);
            par.event = null;
        }

        // Create new edges and split the parabola
        Point start = new Point(p.x, getY(par.point, p.x));
        Edge el = new Edge(start, par.point, p, CORNER_EDGE_WIDTH, CORNER_EDGE_HEIGHT);
        Edge er = new Edge(start, p, par.point,  CORNER_EDGE_WIDTH, CORNER_EDGE_HEIGHT);
        el.neighbour = er;
        er.neighbour = el;
        par.edge = el;
        par.type = Parabola.VERTEX;

        // replace original parabola par with p0, p1, p2
        Parabola p0 = new Parabola (par.point);
        Parabola p1 = new Parabola (p);
        Parabola p2 = new Parabola (par.point);

        par.setLeftChild(p0);
        par.setRightChild(new Parabola());
        par.rightChild.edge = er;
        par.rightChild.setLeftChild(p1);
        par.rightChild.setRightChild(p2);

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void handleCircle(Event e) {
        if (e.arc == null) return;

        // find p0, p1, p2 that generate this event from left to right
        Parabola p1 = e.arc;
        Parabola xl = Parabola.getLeftParent(p1);
        Parabola xr = Parabola.getRightParent(p1);
        Parabola p0 = Parabola.getLeftChild(xl);
        Parabola p2 = Parabola.getRightChild(xr);

        if (p0.event != null) {
            events.remove(p0.event);
            p0.event = null;
        }
        if (p2.event != null) {
            events.remove(p2.event);
            p2.event = null;
        }
        // Remove associated circle events for p0 and p2
        if (p0.event != null) {
            events.remove(p0.event);
            p0.event = null;
        }
        if (p2.event != null) {
            events.remove(p2.event);
            p2.event = null;
        }

        // Calculate the new vertex where edges intersect
        Point p = new Point(e.p.x, getY(p1.point, e.p.x));

        // end edges!
        xl.edge.setEnd(p, CORNER_EDGE_WIDTH,CORNER_EDGE_HEIGHT);
        xr.edge.setEnd(p, CORNER_EDGE_WIDTH,CORNER_EDGE_HEIGHT);
        edges.add(xl.edge);
        edges.add(xr.edge);

        // start new bisector (edge) from this vertex on which ever original edge is higher in tree
        Parabola higher = new Parabola();
        Parabola par = p1;
        while (par != root) {
            par = par.parent;
            if (par == xl) higher = xl;
            if (par == xr) higher = xr;
        }
        higher.edge = new Edge(p, p0.point, p2.point, CORNER_EDGE_WIDTH, CORNER_EDGE_HEIGHT);

        // delete p1 and parent (boundary edge) from beach line
        Parabola gparent = p1.parent.parent;
        if (p1.parent.leftChild == p1) {
            if(gparent.leftChild  == p1.parent) gparent.setLeftChild(p1.parent.rightChild);
            if(gparent.rightChild == p1.parent) gparent.setRightChild(p1.parent.rightChild);
        }
        else {
            if(gparent.leftChild  == p1.parent) gparent.setLeftChild(p1.parent.leftChild);
            if(gparent.rightChild == p1.parent) gparent.setRightChild(p1.parent.leftChild);
        }

        Point op = p1.point;
        p1.parent = null;
        p1 = null;

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void checkCircleEvent(Parabola b) {

        Parabola leftP = Parabola.getLeftParent(b);
        Parabola rightP = Parabola.getRightParent(b);

        if (leftP == null || rightP == null) return;

        Parabola a = Parabola.getLeftChild(leftP);
        Parabola c = Parabola.getRightChild(rightP);

        if (a == null || c == null || a.point == c.point) return;

        if (ccw(a.point, b.point, c.point) != 1) return;

        // edges will intersect to form a vertex for a circle event
        Point start = getEdgeIntersection(leftP.edge, rightP.edge);
        if (start == null ) return;

        // compute radius
        double dx = b.point.x - start.x;
        double dy = b.point.y - start.y;
        double d = Math.sqrt((dx*dx) + (dy*dy));
        if (start.y + d < currentY) return;

        Point ep = new Point(start.x, start.y + d);

        // add circle event
        Event e = new Event (ep, Event.CIRCLE_EVENT);
        e.arc = b;
        b.event = e;
        events.add(e);
    }

    // Checks the orientation of three points (counterclockwise)
    public int ccw(Point a, Point b, Point c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1;
        else return  0;
    }

    // returns intersection of the lines of with vectors a and b
    private Point getEdgeIntersection(Edge a, Edge b) {
        if (b.slope == a.slope && b.y != a.y) return null;

        double x = (b.y - a.y) / (a.slope - b.slope);
        double y = a.slope * x + a.y;

        return new Point(x, y);
    }


    // returns current x-coordinate of an unfinished edge
    private double getXofEdge (Parabola par) {
        if (par == null) return 0;

        Parabola left = Parabola.getLeftChild(par);
        Parabola right = Parabola.getRightChild(par);

        //if (left == null || right == null) return 0;

        Point p = left.point;
        Point r = right.point;

        double dp = 2*(p.y - currentY);
        double a1 = 1/dp;
        double b1 = -2*p.x/dp;
        double c1 = (p.x*p.x + p.y*p.y - currentY * currentY)/dp;

        double dp2 = 2*(r.y - currentY);
        double a2 = 1/dp2;
        double b2 = -2*r.x/dp2;
        double c2 = (r.x*r.x + r.y*r.y - currentY * currentY)/dp2;

        double a = a1-a2;
        double b = b1-b2;
        double c = c1-c2;

        double disc = b*b - 4*a*c;
        double x1 = (-b + Math.sqrt(disc))/(2*a);
        double x2 = (-b - Math.sqrt(disc))/(2*a);

        double ry;
        if (p.y > r.y) ry = Math.max(x1, x2);
        else ry = Math.min(x1, x2);

        return ry;
    }

    // returns parabola above this x coordinate in the beach line
    private Parabola getParabolaByX (double xx) {
        Parabola par = root;
        double x = 0;
        while (par != null && par.type == Parabola.VERTEX) {
            x = getXofEdge(par);
            if (x > xx) par = par.leftChild;
            else par = par.rightChild;
        }
        return par;
    }

    // find corresponding y-coordinate to x on parabola with focus p
    private double getY(Point p, double x) {
        // determine equation for parabola around focus p
        double dp = 2*(p.y - currentY);
        double a1 = 1/dp;
        double b1 = -2*p.x/dp;
        double c1 = (p.x*p.x + p.y*p.y - currentY*currentY)/dp;
        return (a1*x*x + b1*x + c1);
    }


}
