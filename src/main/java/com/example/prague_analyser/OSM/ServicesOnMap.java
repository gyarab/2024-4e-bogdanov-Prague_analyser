package com.example.prague_analyser.OSM;

import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.prague_analyser.OSM.CalculateTile.getConvertedNodesCoord;


public class ServicesOnMap {
    int TYPE = 0;
    String query;
    ArrayList<JsonNode> allInfo = new ArrayList<>();

    public void serviceType(String serviceType){

        if(serviceType.equals("Metro")){
            TYPE = 1;
            query = """
                    node["railway"="station"]["station"="subway"](area.searchArea);
                    """;
        }
        //ujistit se, ze zastavka tam je jen jednou
        //Praha na Slovensku = problem
        if(serviceType.equals("Bus")){
            TYPE = 1;
            query = """
                    (
                      node["highway"="bus_stop"](area.searchArea);
                      node["public_transport"="platform"]["bus"="yes"](area.searchArea);
                    );
                    """;
        }
        if(serviceType.equals("Lekarna")){
            TYPE = 0;
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
                area["ISO3166-1"="CZ"]->.cz;
                area["name"="Praha"]["admin_level"="8"](area.cz)->.searchArea; 
                """
                + query +
                """
                out body;
                """; // musi byt nastaveno v Čr, protože Praha je i na Slovensku

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
        setJSONInfo(elements);
        //staci jedna zastavka
        if(TYPE == 1) {
            HashMap<String, double[]> uniqueElement = new HashMap();
            for (JsonNode element : elements) {
                uniqueElement.put(
                        element.get("tags").path("name").asText(),
                        new double[]{element.path("lat").asDouble(), element.path("lon").asDouble()}
                );
            }

            for (String key : uniqueElement.keySet()) {
                System.out.println(key +" "+uniqueElement.get(key)[0]+ " " + uniqueElement.get(key)[1]);
                Point point = getConvertedNodesCoord(
                        uniqueElement.get(key)[0],
                        uniqueElement.get(key)[1],
                        50.1764594, 14.2377536,
                        stat.min.zoom
                );
                listCord.add(point);
            }
        } else {
            for (JsonNode element : elements) {
                Point point = getConvertedNodesCoord(
                        element.path("lat").asDouble(),
                        element.path("lon").asDouble(),
                        50.1764594, 14.2377536,
                        stat.min.zoom
                );

                listCord.add(point);
            }
        }
        return listCord;
    }
    private void setJSONInfo(JsonNode e){
        for(JsonNode element: e){
         allInfo.add(element);
        }
    }
    public String getNodeInfoName(int i){
        return allInfo.get(i).get("tags").path("name").asText();
    }
}
