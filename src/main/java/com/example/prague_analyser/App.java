package com.example.prague_analyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class App extends Application {



    @Override
    public void start(Stage stage) throws Exception {

        Map mapVal = new Map();
        //Pane mapPane = mapVal.makeMap(10);

        // Nastavení scény a zobrazení
        Scene scene = new Scene(mapVal.makeMap(10), mapVal.TILE_SIZE * (mapVal.WIDTH), mapVal.TILE_SIZE * (mapVal.HEIGHT));
        stage.setTitle("OSM Map Viewer - Praha");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}