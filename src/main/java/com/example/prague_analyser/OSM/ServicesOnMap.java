package com.example.prague_analyser.OSM;

import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.prague_analyser.OSM.CalculateTile.getConvertedNodesCoord;


public class ServicesOnMap {

    String query;

    public void serviceType(String serviceType){

        if(serviceType.equals("Metro")){
            query = """
                    node["railway"="station"]["station"="subway"](area.searchArea);
                    """;
        }
        //ujistit se, ze zastavka tam je jen jednou
        //Praha na Slovensku = problem
        if(serviceType.equals("Bus")){
            query = """
                    (
                      node["highway"="bus_stop"](area.searchArea);
                      node["public_transport"="platform"]["bus"="yes"](area.searchArea);
                    );
                    """;
        }
        if(serviceType.equals("Lekarna")){
            query = """
                    node["amenity"="pharmacy"](area.searchArea);
                    """;

        }

    }


    public ArrayList<Point>  serviceCoords(Maps stat) {
        ArrayList<Point> listCord = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String fullQuery = """
                [out:json];
                area[name="Praha"]->.searchArea;
                """
                + query +
                """
                out body;
                """;

        String encodedQuery = URLEncoder.encode(fullQuery, StandardCharsets.UTF_8);
        Request request = new Request.Builder()
                .url(overpassUrl + "?data=" + encodedQuery)
                .get()
                .build();

        int maxRetries = 3; // Maximum number of retries
        int retryCount = 0; // Current retry count
        long retryDelay = 20; // Delay between retries in milliseconds

        while (retryCount < maxRetries) {
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String jsonResponse = response.body().string();
                    listCord = extractCoordinates(jsonResponse, stat);
                    return listCord; // Return the result if successful
                } else {
                    System.out.println("Response body is null.");
                }
            } catch (SocketException e) {
                System.out.println("SocketException occurred: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException occurred: " + e.getMessage());
            }

            // Increment the retry count and wait before retrying
            retryCount++;
            System.out.println("Retrying... (" + retryCount + "/" + maxRetries + ")");
            try {
                Thread.sleep(retryDelay); // Wait before the next retry
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }

        return listCord;
    }

    private ArrayList<Point> extractCoordinates(String jsonResponse, Maps stat) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode elements = rootNode.path("elements");

        ArrayList<Point> listCord = new ArrayList<>();

        for (JsonNode element : elements) {
            Point point = getConvertedNodesCoord(
                    element.path("lat").asDouble(),
                    element.path("lon").asDouble(),
                    50.1764594,14.2377536,
                    stat.min.zoom
                    );
            System.out.println(point.x+"  " + point.y);

            listCord.add(point);
        }
        return listCord;
    }
}
