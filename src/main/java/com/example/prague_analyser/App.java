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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.stage.Window;
import javafx.util.Duration;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.*;

import static com.example.prague_analyser.Calculations.calculatePolygonArea;
import static com.example.prague_analyser.Calculations.cleanAndSortPolygon;


public class App extends Application {

    public int zoom = 13;

    @Override
    public void start(Stage stage) throws Exception {
        Stage welcomeStage = new Stage();

        TextField category = new TextField();
        category.getStyleClass().add("text-field");

        Button help = new Button();
        help.getStyleClass().add("button-help");
        HBox hb = new HBox(help);

        Label welcomeText = new Label("Welcome to Prague Analyser, choose service you want to analyse");
        welcomeText.getStyleClass().add("title");

        Button btn = new Button("Analyse");
        btn.getStyleClass().add("button-analysis");

        //Nastavení pevné šířky
        double fixedWidth = 150;
        category.setPrefWidth(fixedWidth);
        category.setMaxWidth(300);
        hb.setPrefWidth(fixedWidth);
        btn.setPrefWidth(fixedWidth);

        //nastaveni help loga
        help.setText(" ? ");
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
        Scene welcomeScene = new Scene(welcomeRoot, 600, 600);
        welcomeScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

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
            Hyperlink turbo = new Hyperlink("here");
            turbo.setOnAction(event -> openLink("https://overpass-turbo.eu/"));

            Text helpTextStart = new Text("The predefined services are pharmacy, hospital, school, " +
                    "supermarket, metro station, tram,and bus stop.\n For these services, simply enter one of " +
                    "the following options: lékárna, nemocnice, škola, supermarket, metro, tramvaj, bus.\n" +
                    " If you want to specify your own, use the 'key;value' format or write overpass query just for node line" +
                    "and familiar lines as relation or way or nwr or node, in the end of the query add space (' ').\n" +
                    " For example, for a school, the correct format is \"amenity;school\".\n" +
                    "\n" +
                    "To test it, you can use this page: ");
            Text helpTextEnd = new Text(" or find more details in the OSM documentation.");

            TextFlow helpText = new TextFlow(helpTextStart, turbo, helpTextEnd);
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
                "Please make sure it is in the format key;value or query is not right. If the problem persists and you are sure it is correct" +
                ", please contact support.");

        alert.showAndWait();
    }

    private void loadingScreen(Stage stage,String category ){
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingLabel = new Label("Loading...");

        VBox loadingLayout = new VBox(20, progressIndicator, loadingLabel);
        loadingLayout.setAlignment(Pos.CENTER);
        loadingLayout.setPadding(new Insets(50));

        Scene loadingScene = new Scene(loadingLayout, 300, 200);
        loadingScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            try {
                showMainScene(stage, category);
            } catch (Exception e) {
                stage.close();
                e.printStackTrace();
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
        stage.setTitle("Loading...");
        stage.show();
    }


    int currPolygon = 1;
    ArrayList<Double> polyArea = new ArrayList();
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
                double absX = node.localToScreen(e.getX(), e.getY()).getX();
                double absY = node.localToScreen(e.getX(), e.getY()).getY();
                popup.show(node, absX + 10, absY + 10);

                node.setOnMouseExited(ev->{
                    popup.hide();
                });
            });


            node.setStroke(Color.BLACK);
            node.setFill(Color.CYAN);
            nodes.getChildren().add(node);

            id++;
        }

        Hyperlink osmLicense = new Hyperlink("© OpenStreetMap contributors");
        osmLicense.setOnAction(event -> openLink("https://www.openstreetmap.org/copyright"));

        osmLicense.setStyle("-fx-text-fill: gray; -fx-underline: true;-fx-background-color: white; -fx-border-color: black;");
        HBox link = new HBox(osmLicense);

        link.setStyle("-fx-alignment: bottom-right;");
        nodes.getChildren().add(link);


        // Wrap everything in a ScrollPane for panning
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(nodes);
        scrollPane.setPannable(true);
        scrollPane.setHmax(mapVal.WIDTH*256);
        scrollPane.setVmax(mapVal.HEIGHT*256);
        scrollPane.setMinSize(200,400);

        EventHandler diagram = new EventHandler(listNodes, mapVal.WIDTH*256 , mapVal.HEIGHT*256);
        for(Edge e: diagram.edges){
            Line edge = new Line(e.start.x, e.start.y, e.end.x, e.end.y);
            edge.getStrokeDashArray().addAll((double) (mapVal.WIDTH*256), (double) (mapVal.HEIGHT*256));
            nodes.getChildren().add(edge);
        }


        Button btnExit = new Button("Exit");
        Button btnPrevPoly = new Button("Previous Polygon");
        Button btnNextPoly = new Button("Next Polygon");
        Button btnClearPoly = new Button("Delete");
        Button btnReset = new Button("Reset");
        Button btnRemovePoly = new Button("Remove");
        Button btnCreatePoly = new Button("Create Polygon");

        btnExit.setPrefWidth(150);
        btnPrevPoly.setPrefWidth(150);
        btnNextPoly.setPrefWidth(150);
        btnClearPoly.setPrefWidth(150);
        btnReset.setPrefWidth(150);
        btnRemovePoly.setPrefWidth(150);
        btnCreatePoly.setPrefWidth(150);

        btnExit.setMinWidth(125);
        btnPrevPoly.setMinWidth(125);
        btnNextPoly.setMinWidth(125);
        btnClearPoly.setMinWidth(125);
        btnReset.setMinWidth(125);
        btnRemovePoly.setMinWidth(125);
        btnCreatePoly.setMinWidth(125);

        btnExit.getStyleClass().add("button-transparent");
        btnPrevPoly.getStyleClass().add("button-transparent");
        btnNextPoly.getStyleClass().add("button-transparent");
        btnClearPoly.getStyleClass().add("button-transparent");
        btnReset.getStyleClass().add("button-transparent");
        btnRemovePoly.getStyleClass().add("button-transparent");
        btnCreatePoly.getStyleClass().add("button-transparent");



        VBox buttons = new VBox();
        buttons.getChildren().addAll(btnNextPoly, btnPrevPoly, btnCreatePoly, btnClearPoly, btnRemovePoly, btnReset, btnExit);
        buttons.getStyleClass().add("button-container");
        buttons.setPrefWidth(200);

        HBox hb = new HBox(scrollPane, buttons);
        StackPane root = new StackPane(hb);
        root.setPadding(new Insets(20, 20, 20, 20));
        Scene scene = new Scene(root, 800, 600); // Set initial scene size
        scene.getStylesheets().add(getClass().getResource("/mainSceneStyle.css").toExternalForm());


        ArrayList<Polygon> polygons = new ArrayList<>();
        polygons.add(new Polygon());
        nodes.getChildren().add(1, polygons.get(0));

        Set<KeyCode> pressedKeys = new HashSet<>();




        scene.setOnKeyPressed(ev->{
            if (ev.getCode() == KeyCode.ESCAPE) {
                exitStage(stage);
            }
        });

        btnExit.setOnAction(ev ->{
           exitStage(stage);
        });

        btnPrevPoly.setOnAction(ev ->{
            prevPolygon(polygons);
        });

        btnNextPoly.setOnAction(ev ->{
            nextPolygon();
        });

        btnClearPoly.setOnAction(ev ->{
            clearPolygon(polygons);
        });


        btnCreatePoly.setOnAction(ev ->{
            createPolygon(nodes, polygons);
        });

        btnRemovePoly.setOnAction(ev ->{
            removePolygon(nodes, polygons);
        });


        btnReset.setOnAction(ev ->{
            resetPolygons(nodes,polygons);
        });

        Label info = new Label();

        scrollPane.setOnMouseClicked(ev->{
            double clickX = scrollPane.getContent().sceneToLocal(ev.getSceneX(), ev.getSceneY()).getX();
            double clickY = scrollPane.getContent().sceneToLocal(ev.getSceneX(), ev.getSceneY()).getY();

            if (ev.getButton() == MouseButton.SECONDARY) {
                if(polygons.get(polygons.size() - currPolygon).getPoints().isEmpty()) {
                    double colorR = ((polygons.size() * 123) % 255) / 255.0;
                    double colorG = ((polygons.size() * 321) % 255) / 255.0;
                    double colorB = ((polygons.size() * 213) % 255) / 255.0;

                    polygons.get(polygons.size() - currPolygon).setStroke(Color.RED);
                    polygons.get(polygons.size() - currPolygon).setFill(new Color(colorR, colorG, colorB, 0.3));

                }

                polyArea.remove(calculatePolygonArea(polygons.get(polygons.size()-currPolygon)));

                polygons.get(polygons.size()-currPolygon).getPoints().addAll(clickX,clickY);
                cleanAndSortPolygon(polygons.get(polygons.size()-currPolygon));

                double area = calculatePolygonArea(polygons.get(polygons.size()-currPolygon));
                if(polyArea.isEmpty()) polyArea.add(area);
                else {
                    boolean added = false;
                    for (int i = 0; i < polyArea.size(); i++) {
                        if (area >= polyArea.get(i)) {
                            polyArea.add(i, area);
                            added = true;
                            break;
                        }
                    }
                    if(!added){
                        polyArea.add(area);
                    }
                }

                polygons.get(polygons.size() - currPolygon).setOnMouseEntered(e->{
                    if(polygons.size() - currPolygon == 2) {
                        info.setText("Information:\n" +
                                "     area: " + new BigDecimal(area).setScale(4, RoundingMode.HALF_UP) + "km\n"
                                + "     position: " + (polyArea.indexOf(area) + 1) + ".");
                    }else {
                        info.setText("Information:\n" +
                                "     area: " +  new BigDecimal(area).setScale(4, RoundingMode.HALF_UP) + "km^2\n"
                                + "     position: " + (polyArea.indexOf(area) + 1) + ".");
                    }
                    info.getStyleClass().add("info-text");
                    buttons.getChildren().add(info);

                    polygons.get(polygons.size() - currPolygon).setOnMouseExited(event->{
                         buttons.getChildren().remove(info);
                    });
                });
            }
        });

        root.setOnKeyPressed(ev->{
            pressedKeys.add(ev.getCode());

            if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.N)) {
                createPolygon(nodes, polygons);
            }
            if (ev.getCode() == KeyCode.DELETE) {
                clearPolygon(polygons);
            }
            if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.A) && pressedKeys.contains(KeyCode.DELETE)) {
                removePolygon(nodes, polygons);
            }
            if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.R)) {
                resetPolygons(nodes,polygons);
            }
            if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.UP)) {
                nextPolygon();
            }
            if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.DOWN)){
                prevPolygon(polygons);
            }
        });

        root.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });

        stage.setTitle("Prague Analyser-" + category);
        stage.setScene(scene);
        stage.show();
    }

    private void exitStage(Stage stage){
        Stage exitStage = new Stage();
        exitStage.initModality(Modality.APPLICATION_MODAL); // Blokuje hlavní okno
        exitStage.initStyle(StageStyle.UTILITY); // Menší styl okna


        Button btnExit = new Button("Exit");
        btnExit.setPrefWidth(150);
        btnExit.setOnAction(event -> System.exit(0)); // Zavře aplikaci
        btnExit.getStyleClass().add("button-exit-menu");

        Button btnMainMenu = new Button("Back to main menu");
        btnMainMenu.getStyleClass().add("button-main-menu");
        btnMainMenu.setPrefWidth(150);
        btnMainMenu.setOnAction(event -> {
            try {
                start(exitStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            exitStage.close();
            stage.close();
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("button-cancel");
        btnCancel.setPrefWidth(150);
        btnCancel.setOnAction(event -> exitStage.close()); // Zavře okno

        VBox layout = new VBox(10, btnExit, btnMainMenu, btnCancel);
        layout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Scene scene = new Scene(layout, 250, 150);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        exitStage.setScene(scene);
        exitStage.setTitle("Exit menu");
        exitStage.showAndWait();
    }


    private void resetPolygons(Group nodes, ArrayList<Polygon> polygons){
        polyArea.remove(calculatePolygonArea(polygons.get(polygons.size() - currPolygon)));
        nodes.getChildren().removeAll(polygons);
        polygons.removeAll(polygons);
        //reset
        polygons.add(new Polygon());
        nodes.getChildren().add(1, polygons.get(0));
        currPolygon = 1;
    }

    private void createPolygon(Group nodes, ArrayList<Polygon> polygons){
        polygons.add(new Polygon());
        nodes.getChildren().add(2, polygons.get(polygons.size()-1));
    }

    private void removePolygon(Group nodes, ArrayList<Polygon> polygons){
        if(polygons.size() > 1) {
            polyArea.remove(calculatePolygonArea(polygons.get(polygons.size() - currPolygon)));
            polygons.remove(polygons.size() - currPolygon);
            nodes.getChildren().remove(2);
            currPolygon = 1;
        }else{
            clearPolygon(polygons);
        }
    }

    private void clearPolygon(ArrayList<Polygon> polygons){
        if(!polygons.get(polygons.size()-currPolygon).getPoints().isEmpty())
            polyArea.remove(calculatePolygonArea(polygons.get(polygons.size() - currPolygon)));
            polygons.get(polygons.size()-currPolygon).getPoints().clear();
    }

    private void nextPolygon(){
        if(currPolygon > 1)
            currPolygon--;
    }

    private void prevPolygon(ArrayList<Polygon> polygons){
        if(currPolygon < polygons.size())
            currPolygon++;
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
