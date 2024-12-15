package com.example.prague_analyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {


    @Override
    public void start(Stage stage) {

        Map mapVal = new Map();
        Pane mapPane = new Pane();//mapVal.makeMap(10);


        Image tileImage = new Image("https://a.tile.openstreetmap.org/10/553/346.png",256,256, false, false);
        ImageView tileView = new ImageView(tileImage);

        mapPane.getChildren().add(tileView);


        // Nastavení scény a zobrazení
        Scene scene = new Scene(mapPane, 512, 512);//mapVal.TILE_SIZE * (mapVal.WIDTH), mapVal.TILE_SIZE * (mapVal.HEIGHT));
        stage.setTitle("OSM Map Viewer - Praha");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}