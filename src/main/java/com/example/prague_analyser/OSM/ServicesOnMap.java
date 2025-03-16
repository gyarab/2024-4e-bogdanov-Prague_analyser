package com.example.prague_analyser.OSM;

import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.prague_analyser.OSM.CalculateTile.getConvertedNodesCoord;


public class ServicesOnMap {
    int TYPE = 0;
    String key, value;
    ArrayList<JsonNode> allInfo = new ArrayList<>();
    Map<String, String[]> categoryMap = new HashMap();


    public void serviceType(String serviceType){

        categoryMap.put("lékárna", new String[]{"amenity", "pharmacy"});
        categoryMap.put("nemocnice", new String[]{"amenity", "hospital"});
        categoryMap.put("supermarket", new String[]{"shop", "supermarket"});
        categoryMap.put("škola", new String[]{"amenity", "school"});


        categoryMap.put("metro", new String[]{"railway", "subway_entrance"});
        categoryMap.put("bus", new String[]{"highway", "bus_stop"});
        categoryMap.put("tramvaj", new String[]{"railway", "tram_stop"});

        if(categoryMap.containsKey(serviceType)){
            if(serviceType.equals("metro") || serviceType.equals("bus")|| serviceType.equals("tramvaj")) TYPE = 1;

            String[] selectedCategory = categoryMap.get(serviceType);
            key = selectedCategory[0];
            value = selectedCategory[1];
        } else {
          String parts[] = serviceType.split(";");
          if (parts.length != 2)return;
          key = parts[0];
          value = parts[1];
        }
    }


    public ArrayList<Point>  serviceCoords(Maps stat) {
        ArrayList<Point> listCord = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        String overpassUrl = "https://overpass-api.de/api/interpreter";

        String fullQuery = createOverpassQuery(key, value);

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
            try {
                Thread.sleep(retryDelay); // Wait before the next retry
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }

        return listCord;
    }

    private static String createOverpassQuery(String key, String value) {
        return String.format("""
                [out:json];
                area["ISO3166-1"="CZ"]->.cz;
                area["name"="Praha"]["admin_level"="8"](area.cz)->.searchArea;
                (
                  node["%s"="%s"](area.searchArea);
                  way["%s"="%s"](area.searchArea);
                  relation["%s"="%s"](area.searchArea);
                );
                out geom;
                """, key, value, key, value, key, value);
    }

    private ArrayList<Point> extractCoordinates(String jsonResponse, Maps stat) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode elements = rootNode.path("elements");

        ArrayList<Point> listCord = new ArrayList<>();

        if(TYPE == 1) {
            for (JsonNode element : elements) {
                boolean checkDuplicity = false;
                for (int i = 0; i < allInfo.size(); i++) {
                    if(allInfo.get(i).get("tags").path("name").asText().equals(element.get("tags").path("name").asText())){
                        checkDuplicity = true;
                    }
                }
                if(!checkDuplicity){
                    setJSONInfo(element);
                }
            }

            for (JsonNode a : allInfo) {
                Point point = getConvertedNodesCoord(
                        a.path("lat").asDouble(),
                        a.path("lon").asDouble(),
                        50.1764594, 14.2377536,
                        stat.min.zoom
                );
                listCord.add(point);
            }
        } else {
            for (JsonNode element : elements) {
                setJSONInfo(element);
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
        allInfo.add(e);
    }

    public String getNodeInfoName(int i){
        return allInfo.get(i).get("tags").path("name").asText();
    }
}
