package com.example.prague_analyser;

import com.example.prague_analyser.OSM.Maps;
import com.example.prague_analyser.OSM.ServicesOnMap;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.util.ArrayList;

public class App extends Application {

    private double scaleFactor = 1.0;

    private int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {

        Maps mapVal = new Maps();
        Pane mapPane = new Pane(mapVal.makeMap(zoom, scaleFactor));

        ServicesOnMap service = new ServicesOnMap();
        service.serviceType("Metro");


        Group nodes = new Group();
        ArrayList<double[]> listNodes = service.serviceCoords();

        for(int i = 0; i < listNodes.size(); i++){
            Circle node = new Circle();
            node.setCenterX(listNodes.get(i)[0]);
            node.setCenterY(listNodes.get(i)[1]);
            node.setRadius(10.0);

            nodes.getChildren().add(node);
        }

        ScrollPane scrollPane = new ScrollPane(mapPane);
        scrollPane.setPannable(true);


        Scene scene = new Scene(nodes, mapVal.TILE_SIZE * (mapVal.WIDTH), mapVal.TILE_SIZE * (mapVal.HEIGHT));
        stage.setTitle("OSM Map Viewer - Praha");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}