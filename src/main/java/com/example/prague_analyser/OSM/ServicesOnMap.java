package com.example.prague_analyser.OSM;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
                    (
                      node["highway"="bus_stop"](area.searchArea);
                      node["public_transport"="platform"]["bus"="yes"](area.searchArea);
                    );
                    """;

        }
        if(serviceType.equals("Nemocnice")){
            query = """
                    (
                      node["amenity"="hospital"](area.searchArea);
                      way["amenity"="hospital"](area.searchArea);
                      relation["amenity"="hospital"](area.searchArea);
                    );
                    """;
        }

    }


    public ArrayList<double[]>  serviceCoords() {
        ArrayList<double[]> listCord = new ArrayList<>();

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

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                listCord = extractCoordinates(jsonResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listCord;
    }

    private ArrayList<double[]> extractCoordinates(String jsonResponse) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode elements = rootNode.path("elements");

        ArrayList<double[]> listCord = new ArrayList<>();

        for (JsonNode element : elements) {
            double[] arrCord = {
                    element.path("lat").asDouble(),
                    element.path("lon").asDouble()
            };

            listCord.add(arrCord);
        }
        return listCord;
    }
}
