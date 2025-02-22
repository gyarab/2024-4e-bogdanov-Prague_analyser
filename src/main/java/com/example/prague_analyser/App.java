package com.example.prague_analyser;

import com.example.prague_analyser.FortunesALG.VoronoiGraph.Edge;
import com.example.prague_analyser.FortunesALG.VoronoiGraph.EventHandler;
import com.example.prague_analyser.FortunesALG.VoronoiGraph.Point;

import com.example.prague_analyser.OSM.Maps;
import com.example.prague_analyser.OSM.ServicesOnMap;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;


public class App extends Application {

    public int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {
        Stage welcomeStage = new Stage();

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Lekarna", "Metro", "Bus");
        choiceBox.setValue("Metro");

        Label l = new Label("Welcome to Prague Analyser, choose service you want to analyse");
        Button btn = new Button("Analyse");

        // **Nastavení pevné šířky**
        double fixedWidth = 150;
        choiceBox.setPrefWidth(fixedWidth);
        btn.setPrefWidth(fixedWidth);

        // **Layout a umístění**
        VBox vb = new VBox(15, l, choiceBox, btn); // Zvýšený spacing mezi prvky
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // **Přidání paddingu dolů pro tlačítko**
        vb.setTranslateY(20); // Posunutí prvků dolů

        StackPane welcomeRoot = new StackPane(vb);
        Scene welcomeScene = new Scene(welcomeRoot, 400, 220);

        welcomeStage.setTitle("Welcome - Prague Analyser");
        welcomeStage.setScene(welcomeScene);

        // **Vycentrování na střed obrazovky**
        centerStage(welcomeStage, 400, 220);

        // **Při změně velikosti okna zůstane na středu**
        welcomeStage.widthProperty().addListener((obs, oldVal, newVal) -> centerStage(welcomeStage, newVal.doubleValue(), welcomeStage.getHeight()));
        welcomeStage.heightProperty().addListener((obs, oldVal, newVal) -> centerStage(welcomeStage, welcomeStage.getWidth(), newVal.doubleValue()));

        welcomeStage.show();

        btn.setOnAction(ev->{
            welcomeStage.close();
            loadingScreen(stage,choiceBox);
        });

    }
    private void centerStage(Stage stage, double width, double height) {
        Screen screen = Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();

        stage.setX((screenWidth - width) / 2);
        stage.setY((screenHeight - height) / 2);
    }


    private void loadingScreen(Stage stage, ChoiceBox<String> choiceBox){
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingLabel = new Label("Načítání...");

        VBox loadingLayout = new VBox(20, progressIndicator, loadingLabel);
        loadingLayout.setStyle("-fx-alignment: center; -fx-padding: 50px;");

        Scene loadingScene = new Scene(loadingLayout, 300, 200);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> showMainScene(stage, choiceBox));
        delay.play();

        stage.setScene(loadingScene);
        stage.setTitle("Načítání...");
        stage.show();
    }


    private void showMainScene(Stage stage, ChoiceBox<String> choiceBox){
        Maps mapVal = new Maps();
        Pane mapPane = null;
        try {
            mapPane = new Pane(mapVal.makeMap(zoom));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ServicesOnMap service = new ServicesOnMap();
        service.serviceType(choiceBox.getValue());

        Group nodes = new Group(mapPane);
        ArrayList<Point> listNodes = service.serviceCoords(mapVal);

        for (Point point : listNodes) {
            Circle node = new Circle();


            node.setCenterX(point.x);
            node.setCenterY(point.y);
            node.setRadius(5.0);

            node.setStroke(Color.BLACK);
            node.setFill(Color.CYAN);
            nodes.getChildren().add(node);
        }

        // Wrap everything in a ScrollPane for panning
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(nodes);
        scrollPane.setPannable(true);
        scrollPane.setHmax(mapVal.WIDTH*256);
        scrollPane.setVmax(mapVal.HEIGHT*256);

        EventHandler diagram = new EventHandler(listNodes, mapVal.WIDTH*256 , mapVal.HEIGHT*256);
        for(Edge e: diagram.edges){
            Line edge = new Line(e.start.x, e.start.y, e.end.x, e.end.y);
            edge.getStrokeDashArray().addAll((double) (mapVal.WIDTH*256), (double) (mapVal.HEIGHT*256));
            nodes.getChildren().add(edge);
        }

        StackPane root = new StackPane(scrollPane);
        root.setPadding(new Insets(20, 20, 20, 20));
        Scene scene = new Scene(root, 800, 600); // Set initial scene size

        stage.setTitle("Prague Analyser-" + choiceBox.getValue());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
