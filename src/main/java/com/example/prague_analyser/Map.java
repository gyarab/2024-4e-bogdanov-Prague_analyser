package com.example.prague_analyser;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

public class Map {

    public static final int TILE_SIZE = 256;

    public int WIDTH = 0;
    public int HEIGHT = 0;

    public Pane makeMap(int zoom) throws Exception {
        Pane mapPane = new Pane();


        //Severovýchodní roh
        double latMax = 50.1404d;
        double lonMax = 14.7275d;

        CalculateTile ctMax = new CalculateTile();
        ctMax.getIntTile(latMax, lonMax, zoom);


        //Jihozápadní roh
        double latMin = 49.9419d;
        double lonMin = 14.2601d;

        CalculateTile ctMin = new CalculateTile();
        ctMin.getIntTile(latMin, lonMin, zoom);

        WIDTH = ctMax.xtile - ctMin.xtile + 1;
        HEIGHT = - ctMax.ytile + ctMin.ytile + 1;

        for (int x = ctMin.xtile; x <= ctMax.xtile; x++) {
            for (int y = ctMin.ytile; y >= ctMax.ytile; y--) {
                String tileURL = "https://tile.openstreetmap.org/10/"+x+"/"+y +".png";

                Image tileImage = loadImageWithUserAgent(tileURL, TILE_SIZE, TILE_SIZE);

                ImageView tileView = new ImageView(tileImage);

                tileView.setX((x - ctMin.xtile * TILE_SIZE));
                tileView.setY((y - ctMin.ytile) * TILE_SIZE);

                mapPane.getChildren().addAll(tileView);

                //TimeUnit.MILLISECONDS.sleep(500);
            }

        }
        return mapPane;
    }
    private Image loadImageWithUserAgent(String tileUrl, double width, double height) throws Exception {
        URL url = new URL(tileUrl);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "JavaFX OSM Map Viewer");
        return new Image(connection.getInputStream(), width, height, false, false);
    }
}
