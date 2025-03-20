package com.example.prague_analyser;

import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Calculations {
    //scale is 1:70 000
    private static final double SCALE_METERS_PER_PIXEL = 19.109;//in meters https://wiki.openstreetmap.org/wiki/Zoom_levels


    //generovano ChatGPT
    // Compute Convex Hull using Graham’s scan algorithm (removes inside points)
    public static List<Point> computeConvexHull(List<Point> points) {
        if (points.size() < 3) return new ArrayList<>(points); // No need to filter

        // Step 1: Find the lowest Y point (leftmost if tie)
        Point minYPoint = Collections.min(points, Comparator.comparingDouble((Point p) -> p.y)
                .thenComparingDouble(p -> p.x));

        // Step 2: Sort points by polar angle with minYPoint
        points.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.y - minYPoint.y, p1.x - minYPoint.x);
            double angle2 = Math.atan2(p2.y - minYPoint.y, p2.x - minYPoint.x);
            return Double.compare(angle1, angle2);
        });

        // Step 3: Use stack to build convex hull
        List<Point> hull = new ArrayList<>();
        for (Point p : points) {
            while (hull.size() >= 2 && crossProduct(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        return hull;
    }

    // Helper function to compute cross product
    public static double crossProduct(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    public static void cleanAndSortPolygon(Polygon polygon) {
        // Step 1: Extract points from Polygon
        List<Double> rawPoints = polygon.getPoints();
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < rawPoints.size(); i += 2) {
            points.add(new Point(rawPoints.get(i), rawPoints.get(i + 1)));
        }

        // Step 2: Compute Convex Hull (removes inside points)
        List<Point> boundaryPoints = computeConvexHull(points);

        // Step 3: Compute centroid
        double centroidX = boundaryPoints.stream().mapToDouble(p -> p.x).average().orElse(0);
        double centroidY = boundaryPoints.stream().mapToDouble(p -> p.y).average().orElse(0);

        // Step 4: Sort by angle relative to centroid
        boundaryPoints.sort(Comparator.comparingDouble(p -> Math.atan2(p.y - centroidY, p.x - centroidX)));

        // Step 5: Update polygon with cleaned and sorted points
        polygon.getPoints().clear();
        for (Point p : boundaryPoints) {
            polygon.getPoints().addAll(p.x, p.y);
        }
    }

    public static double calculatePolygonArea(Polygon polygon){
        List<Double> points = polygon.getPoints();
        int n = points.size() / 2; // pocet vrcholu polygon je x a dalsi index y

        if(n < 3){
            if (n == 2){
                double x1 = points.get(0);
                double y1 = points.get(1);
                double x2 = points.get(2);
                double y2 = points.get(3);
                return Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1, 2));
            }else {
                return 0;
            }
        }

        double sum1 = 0;
        double sum2 = 0;

        for (int i = 0; i < n; i++) {
            double x1 = points.get(2 * i);
            double y1 = points.get(2 * i + 1);
            double x2 = points.get((2 * (i + 1)) % points.size());// zajistuje cyklus, takze se vratime na prvni bod
            double y2 = points.get((2 * (i + 1) + 1) % points.size());

            sum1 += x1 * y2;
            sum2 += y1 * x2;
        }

        double areaPixels = Math.abs(sum1 - sum2) / 2.0;
        return areaPixels * Math.pow(SCALE_METERS_PER_PIXEL / 1000, 2);
    }

}
