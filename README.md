# 2024-4e-bogdanov-Prague_analyser

## About project
This is school project for the year 2024/2025.  

This project divide city of Prague into regions based on public services. Each area contains a point. Any other point within this region is closest to that given point (service).
That is called a Voronoi diagram. To create Voronoi diagram I use Fortune's algorithm. For now the predefined public services are: Pharmacy, Subway station, Bus stop, Tram station, School, Supermarket and Hospital, also users can put their own question for OSM database. 

## Some results
Here will be images of results

## Run app
To try this program on your owm, you have to:

1. clone repository
```b
git clone https://github.com/gyarab/2024-4e-bogdanov-Prague_analyser.git
```
2. run these commands
```b
cd 2024-4e-bogdanov-Prague_analyser

./mvnw install
```
(make sure you are using java version 20 or more)

3. to run project  
```b
./mvnw clean javafx:run
```
 


