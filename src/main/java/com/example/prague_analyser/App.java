package com.example.prague_analyser;

import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;
import com.example.prague_analyser.OSM.Maps;
import com.example.prague_analyser.OSM.ServicesOnMap;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;

public class App extends Application {

    private int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {
        Maps mapVal = new Maps();
        Pane mapPane = new Pane(mapVal.makeMap(zoom));

        ServicesOnMap service = new ServicesOnMap();
        service.serviceType("Metro");

        Group nodes = new Group(mapPane);
        ArrayList<Point> listNodes = service.serviceCoords(mapVal);

        for (Point point : listNodes) {
            Circle node = new Circle();

            node.setCenterX(point.x);
            node.setCenterY(point.y);
            node.setRadius(5.0);

            node.setStroke(Color.BLACK);
            node.setFill(Color.CYAN);
            nodes.getChildren().add(node);
        }

        // Wrap everything in a ScrollPane for panning
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(nodes);
        scrollPane.setPannable(true);

        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 800, 600); // Set initial scene size

        stage.setTitle("OSM Map Viewer - Praha");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
