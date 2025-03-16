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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;


public class App extends Application {

    public int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {
        Stage welcomeStage = new Stage();

        TextField category = new TextField();
        Button help = new Button();
        HBox hb = new HBox(help);

        Label welcomeText = new Label("Welcome to Prague Analyser, choose service you want to analyse");
        Button btn = new Button("Analyse");

        //Nastavení pevné šířky
        double fixedWidth = 150;
        category.setPrefWidth(fixedWidth);
        category.setMaxWidth(300);
        hb.setPrefWidth(fixedWidth);
        btn.setPrefWidth(fixedWidth);

        //nastaveni help loga
        help.setText(" ? ");
        help.setStyle(
                "-fx-font-size: 18px; -fx-text-fill: black;"+
                "-fx-border-color: black; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-radius: 15px;"
        );

        hb.setAlignment(Pos.TOP_RIGHT);
        hb.setStyle("-fx-padding: 20px;");

        //Layout a umístění
        VBox vb = new VBox(15, welcomeText, category, btn); // Zvýšený spacing mezi prvky
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(Region.USE_COMPUTED_SIZE);

        //Přidání paddingu dolů pro tlačítko
        vb.setTranslateY(20); // Posunutí prvků dolů

        BorderPane welcomeRoot = new BorderPane();
        welcomeRoot.setTop(hb);
        welcomeRoot.setCenter(vb);
        Scene welcomeScene = new Scene(welcomeRoot, 400, 250);

        welcomeStage.setTitle("Welcome - Prague Analyser");
        welcomeStage.setScene(welcomeScene);

        //Vycentrování na střed obrazovky
        centerStage(welcomeStage, 400, 220);

        //Při změně velikosti okna zůstane na středu
        welcomeStage.widthProperty().addListener((obs, oldVal, newVal) -> centerStage(welcomeStage, newVal.doubleValue(), welcomeStage.getHeight()));
        welcomeStage.heightProperty().addListener((obs, oldVal, newVal) -> centerStage(welcomeStage, welcomeStage.getWidth(), newVal.doubleValue()));

        welcomeStage.show();

        btn.setOnAction(ev->{
            if(!category.getText().trim().isEmpty()) {
                welcomeStage.close();
                loadingScreen(stage, category.getText());
            }else{
                welcomeText.setText("You have to write something to access a map");
            }
        });

        category.setOnKeyPressed(ev->{
            if (ev.getCode() == KeyCode.ENTER) {
                if(!category.getText().trim().isEmpty()) {
                    welcomeStage.close();
                    loadingScreen(stage, category.getText());
                }else{
                    welcomeText.setText("You have to write something to access a map");
                }
            }

        });

        help.setOnMouseClicked(ev->{
            Label helpText = new Label("The predefined services are pharmacy, hospital, school, supermarket, metro station, tram, " +
                    "and bus stop.\n For these services, simply enter one of the following options: lékárna, nemocnice, škola, supermarket, metro, tramvaj, bus.\n" +
                    " If you want to specify your own, use the 'key;value' format. For example, for a school, the correct format is \"amenity;school\".\n" +
                    "\n" +
                    "To test it, you can use this page: https://overpass-turbo.eu/ and find more details in the OSM documentation.");
            helpText.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 10px;");

            Popup popup = new Popup();
            popup.getContent().add(helpText);

            // Získání hlavního okna (Stage)
            Window window = help.getScene().getWindow();

            // Výpočet středu okna
            double centerX = window.getX() + window.getWidth() / 2 - helpText.getWidth() / 2;
            double centerY = window.getY() + window.getHeight() / 2 - helpText.getHeight() / 2;

            popup.show(window, centerX, centerY);

            // Skrytí popupu po kliknutí kamkoliv jinam
            help.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, e -> popup.hide());
        });

    }
    private void centerStage(Stage stage, double width, double height) {
        Screen screen = Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();

        stage.setX((screenWidth - width) / 2);
        stage.setY((screenHeight - height) / 2);
    }

    private void showErrorDialog(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception-PragueAnalyser");
        alert.setHeaderText("An error during process");
        alert.setContentText("An error has likely occurred somewhere. You may have entered the key and value incorrectly.\n" +
                "Please make sure it is in the format key;value. If the problem persists, please contact support.");

        alert.showAndWait();
    }

    private void loadingScreen(Stage stage,String category ){
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingLabel = new Label("Načítání...");

        VBox loadingLayout = new VBox(20, progressIndicator, loadingLabel);
        loadingLayout.setAlignment(Pos.CENTER);
        loadingLayout.setPadding(new Insets(50));

        Scene loadingScene = new Scene(loadingLayout, 300, 200);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            try {
                showMainScene(stage, category);
            } catch (Exception e) {
                stage.close();

                // Zobrazení chybového dialogu mimo animaci
                Platform.runLater(() -> showErrorDialog());

                // Návrat na úvodní obrazovku
                Platform.runLater(() -> {
                    try {
                        start(stage);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        });
        delay.play();

        stage.setScene(loadingScene);
        stage.setTitle("Načítání...");
        stage.show();
    }


    private void showMainScene(Stage stage, String category){
        Maps mapVal = new Maps();
        Pane mapPane = null;
        try {
            mapPane = new Pane(mapVal.makeMap(zoom));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ServicesOnMap service = new ServicesOnMap();
        service.serviceType(category);

        Group nodes = new Group(mapPane);
        ArrayList<Point> listNodes = service.serviceCoords(mapVal);

        int id = 0;
        for (Point point : listNodes) {
            Circle node = new Circle();
            node.setCenterX(point.x);
            node.setCenterY(point.y);
            node.setRadius(5.0);

            int finalId = id;
            node.setOnMouseEntered(e->{
                Label name = new Label(service.getNodeInfoName(finalId));
                name.setBackground(Background.fill(Color.WHITE));
                Popup popup = new Popup();
                popup.getContent().add(name);
                popup.show(node,e.getSceneX() , e.getSceneY());

                node.setOnMouseExited(ev->{
                    popup.hide();
                });
            });


            node.setStroke(Color.BLACK);
            node.setFill(Color.CYAN);
            nodes.getChildren().add(node);

            id++;
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

        stage.setTitle("Prague Analyser-" + category);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
