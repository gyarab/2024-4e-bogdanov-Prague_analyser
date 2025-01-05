package com.example.prague_analyser;

import com.example.prague_analyser.OSM.Maps;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class App extends Application {

    private double scaleFactor = 1.0;

    private int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {

        Maps mapVal = new Maps();
        Pane mapPane = new Pane(mapVal.makeMap(zoom, scaleFactor));

        Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
        mapPane.getTransforms().add(scale);

        ScrollPane scrollPane = new ScrollPane(mapPane);
        scrollPane.setPannable(true);

        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            try {
                mapVal.makeMap(zoom, scaleFactor); // Dynamically update grid on resize
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mapPane.setOnScroll((ScrollEvent e) ->{
            if(e.getDeltaY() > 0){
                scaleFactor *= 1.1;
            }else if(e.getDeltaY() < 1){
                scaleFactor /= 1.1;
            }

            scaleFactor = Math.max(0.5, Math.min(scaleFactor,5.0));

            try {
                mapVal.makeMap(zoom, scaleFactor);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            scale.setX(scaleFactor);
            scale.setY(scaleFactor);

            scale.setPivotX(e.getX());
            scale.setPivotY(e.getY());

            double mouseX = e.getX();
            double mouseY = e.getY();

            scale.setPivotX(mouseX);
            scale.setPivotY(mouseY);
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