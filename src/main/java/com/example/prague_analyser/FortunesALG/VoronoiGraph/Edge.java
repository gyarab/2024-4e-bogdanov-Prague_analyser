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

    public Edge(Point first, Point left, Point right, double width, double height){
        if(first.x < 0)first.x = 0;
        if(first.x > width)first.x = width;
        if(first.y < 0)first.y = 0;
        if(first.y > height)first.y = height;

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
        double x1 = start.x, y1 = start.y;
        double x2 = end.x, y2 = end.y;

        // Pokud je bod už uvnitř oblasti, použijeme ho přímo
        if ((x2 >= 0 && x2 <= width) && (y2 >= 0 && y2 <= height)) {
            this.end = end;
            return;
        }

        // Vypočítáme směrnici přímky (pozor na dělení nulou)
        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0) { // Vertikální čára
            y2 = (y2 < 0) ? 0 : height;
        } else if (dy == 0) { // Horizontální čára
            x2 = (x2 < 0) ? 0 : width;
        } else {
            double a = dy / dx;
            double b = y1 - a * x1;

            double newX = x2, newY = y2;

            // Průsečík s horní hranou (y = 0)
            if (y2 < 0) {
                newX = (-b) / a;
                newY = 0;
            }
            // Průsečík s dolní hranou (y = height)
            else if (y2 > height) {
                newX = (height - b) / a;
                newY = height;
            }

            // Pokud je průsečík mimo levý nebo pravý okraj, opravíme jej
            if (newX < 0) {
                newY = b;
                newX = 0;
            } else if (newX > width) {
                newY = a * width + b;
                newX = width;
            }

            x2 = newX;
            y2 = newY;
        }

        // **Dvojitá kontrola, zda je bod v rámci hranic**
        x2 = Math.max(0, Math.min(x2, width));
        y2 = Math.max(0, Math.min(y2, height));

        // Nastavíme opravený konec čáry
        this.end = new Point(x2, y2);
    }

}
