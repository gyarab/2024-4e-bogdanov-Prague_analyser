package com.example.prague_analyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class App extends Application {

    private double scaleFactor = 1.0;

    @Override
    public void start(Stage stage) throws Exception {

        Map mapVal = new Map();
        Pane mapPane = new Pane(mapVal.makeMap(13));

        Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
        mapPane.getTransforms().add(scale);

        ScrollPane scrollPane = new ScrollPane(mapPane);
        scrollPane.setPannable(true);

        mapPane.setOnScroll((ScrollEvent e) ->{
            if(e.getDeltaY() > 0){
                scaleFactor *= 1.1;
            }else {
                scaleFactor /= 1.1;
            }

            scale.setX(scaleFactor);
            scale.setY(scaleFactor);

            scale.setPivotX(e.getX());
            scale.setPivotY(e.getY());
        });



        Scene scene = new Scene(scrollPane, mapVal.TILE_SIZE * (mapVal.WIDTH), mapVal.TILE_SIZE * (mapVal.HEIGHT));
        stage.setTitle("OSM Map Viewer - Praha");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}