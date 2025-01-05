package com.example.prague_analyser.OSM;

public class CalculateTile {

    int xtile;
    int ytile;

    public CalculateTile() {
        this.xtile = 0;
        this.ytile = 0;
    }

    //vypocet pro zjisteni souradnic
    //https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    public void getIntTile(double lat, double lon, int zoom ) {
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
}
