package com.example.prague_analyser.OSM;

import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;

public class CalculateTile {

    int xtile;
    int ytile;
    int zoom;

    //predpoklad, ze jedenn ctverecek na mape je takto veliky
    private static final int  TILE_SIZE = 256;

    public CalculateTile() {
        this.xtile = 0;
        this.ytile = 0;
    }


    //vypocet pro zjisteni souradnic
    //https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    public void getIntTile(double lat, double lon, int zoom ) {
        this.zoom = zoom;

        xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));

        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
    }

    public static Point getConvertedNodesCoord(
            double lat, double lon, double latStart, double lonStart,
            int zoom) {
        if(lat == 0 && lon == 0){
            return new Point(0,0);
        }

        final int TILE_SIZE = 256; // Každá OSM dlaždice má 256x256 pixelů

        double tilesPerRow = Math.pow(2, zoom);

        double xStart = ((lonStart + 180) / 360) * (TILE_SIZE * tilesPerRow);
        double yStart = (0.5 - (Math.log(Math.tan((Math.PI / 4) + (latStart * Math.PI / 360))) / (2 * Math.PI))) * (TILE_SIZE * tilesPerRow);

        double x = ((lon + 180) / 360) * (TILE_SIZE * tilesPerRow);
        double y = (0.5 - (Math.log(Math.tan((Math.PI / 4) + (lat * Math.PI / 360))) / (2 * Math.PI))) * (TILE_SIZE * tilesPerRow);



        double finalX = x - xStart;
        double finalY = y - yStart;

        System.out.println("finalX:" + x +" finalY:" + y );

        return new Point((int) finalX, (int) finalY); // Převod na int, protože souřadnice jsou v pixelech
    }

}
