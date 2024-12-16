package com.example.prague_analyser;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Maps {

    public static final int TILE_SIZE = 256;

    public int WIDTH = 0;
    public int HEIGHT = 0;

    private final Map<String, Image> rememberTile = new HashMap<>();

    public Pane makeMap(int zoom, double scaleFactor) throws Exception {
        GridPane mapPane = new GridPane();


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


        mapPane.getChildren().clear();


        for (int x = ctMin.xtile; x <= ctMax.xtile; x++) {
            for (int y = ctMin.ytile; y >= ctMax.ytile; y--) {
                String tileURL = "https://tile.openstreetmap.org/"+zoom+"/"+x+"/"+y+".png";

                Image tileImage = rememberTile.get(tileURL);

                if (tileImage == null) {
                    tileImage = loadImageWithUserAgent(tileURL, TILE_SIZE, TILE_SIZE);
                    if (tileImage != null) {
                        rememberTile.put(tileURL, tileImage);
                    } else {
                        continue;
                    }
                }

                ImageView tileView = new ImageView(tileImage);

                tileView.setFitWidth(TILE_SIZE * scaleFactor);
                tileView.setFitHeight(TILE_SIZE * scaleFactor);

                mapPane.add(tileView, x - ctMin.xtile, y-ctMax.ytile);

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
