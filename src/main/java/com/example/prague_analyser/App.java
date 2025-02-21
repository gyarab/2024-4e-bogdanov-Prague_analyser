package com.example.prague_analyser;

import com.example.prague_analyser.FortunesALG.VoronoiGraph.Edge;
import com.example.prague_analyser.FortunesALG.VoronoiGraph.EventHandler;
import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;

import com.example.prague_analyser.OSM.Maps;
import com.example.prague_analyser.OSM.ServicesOnMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Vector;

public class App extends Application {

    public int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {
        Stage welcomeStage = new Stage();
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Lekarna", "Metro", "Bus");
        choiceBox.setValue("Metro");

        Label l = new Label("Welcome to Prague Analyser, choose service you want to analyse");
        Button btn = new Button("Analyse");
        VBox vb= new VBox(l,choiceBox, btn);


        StackPane welcomeRoot = new StackPane(vb);
        Scene welcomeScene = new Scene(welcomeRoot, 400, 200);
        welcomeStage.setTitle("Welcome - Prague Analyser");
        welcomeStage.setScene(welcomeScene);
        welcomeStage.show();

        btn.setOnAction(ev->{
            welcomeStage.close();
            Maps mapVal = new Maps();
            Pane mapPane = null;
            try {
                mapPane = new Pane(mapVal.makeMap(zoom));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ServicesOnMap service = new ServicesOnMap();
            service.serviceType(choiceBox.getValue());

            Group nodes = new Group(mapPane);
            ArrayList<Point> listNodes = service.serviceCoords(mapVal);

            for (Point point : listNodes) {
                Circle node = new Circle();
                //System.out.println("point.add(new Point(" + point.x+", "+point.y+"));");

                node.setCenterX(point.x);
                node.setCenterY(point.y);
                node.setRadius(5.0);

                node.setStroke(Color.BLACK);
                node.setFill(Color.CYAN);
                nodes.getChildren().add(node);
            }

            EventHandler diagram = new EventHandler(listNodes, mapVal.WIDTH*256 , mapVal.HEIGHT*256);
            for(Edge e: diagram.edges){
                Line edge = new Line(e.start.x, e.start.y, e.end.x, e.end.y);
                nodes.getChildren().add(edge);
            }


            // Wrap everything in a ScrollPane for panning
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(nodes);
            scrollPane.setPannable(true);



            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 800, 600); // Set initial scene size

            stage.setTitle("Prague Analyser");
            stage.setScene(scene);
            stage.show();
        });

    }

    public static void main(String[] args) {
        launch();
    }
}
