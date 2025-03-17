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
import java.net.URI;
import java.util.*;
import java.util.List;


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
            Hyperlink turbo = new Hyperlink("here");
            turbo.setOnAction(event -> openLink("https://overpass-turbo.eu/"));

            Text helpTextStart = new Text("The predefined services are pharmacy, hospital, school, " +
                    "supermarket, metro station, tram,and bus stop.\n For these services, simply enter one of " +
                    "the following options: lékárna, nemocnice, škola, supermarket, metro, tramvaj, bus.\n" +
                    " If you want to specify your own, use the 'key;value' format or write overpass query just for node line" +
                    "and familiar lines as relation or way, in the end of the query add space (' ').\n" +
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
        stage.setTitle("Loading...");
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

        Polygon polygon = new Polygon();
        nodes.getChildren().add(polygon);

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

        EventHandler diagram = new EventHandler(listNodes, mapVal.WIDTH*256 , mapVal.HEIGHT*256);



        for(Edge e: diagram.edges){
            Line edge = new Line(e.start.x, e.start.y, e.end.x, e.end.y);
            edge.getStrokeDashArray().addAll((double) (mapVal.WIDTH*256), (double) (mapVal.HEIGHT*256));
            nodes.getChildren().add(edge);
        }

        StackPane root = new StackPane(scrollPane);
        root.setPadding(new Insets(20, 20, 20, 20));
        Scene scene = new Scene(root, 800, 600); // Set initial scene size

        root.setOnKeyPressed(ev->{
            if (ev.getCode() == KeyCode.ESCAPE) {
                exitStage(stage);
            }
        });



        scrollPane.setOnMouseClicked(ev->{
            double clickX = scrollPane.getContent().sceneToLocal(ev.getSceneX(), ev.getSceneY()).getX();
            double clickY = scrollPane.getContent().sceneToLocal(ev.getSceneX(), ev.getSceneY()).getY();

            if (ev.getButton() == MouseButton.SECONDARY) {
                polygon.getPoints().addAll(clickX,clickY);
                cleanAndSortPolygon(polygon);
                polygon.setStroke(Color.RED);
                polygon.setFill(new Color(0, 0, 1, 0.3));
            }
        });

        root.setOnKeyPressed(ev->{
            if (ev.getCode() == KeyCode.DELETE) {
                polygon.getPoints().clear();
            }
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

        Button btnMainMenu = new Button("Back to main menu");
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
        btnCancel.setPrefWidth(150);
        btnCancel.setOnAction(event -> exitStage.close()); // Zavře okno

        VBox layout = new VBox(10, btnExit, btnMainMenu, btnCancel);
        layout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Scene scene = new Scene(layout, 250, 150);
        exitStage.setScene(scene);
        exitStage.setTitle("Exit menu");
        exitStage.showAndWait();
    }

    private void cleanAndSortPolygon(Polygon polygon) {
        // Step 1: Extract points from Polygon
        List<Double> rawPoints = polygon.getPoints();
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < rawPoints.size(); i += 2) {
            points.add(new Point(rawPoints.get(i), rawPoints.get(i + 1)));
        }

        // Step 2: Compute Convex Hull (removes inside points)
        List<Point> boundaryPoints = computeConvexHull(points);

        // Step 3: Compute centroid
        double centroidX = boundaryPoints.stream().mapToDouble(p -> p.x).average().orElse(0);
        double centroidY = boundaryPoints.stream().mapToDouble(p -> p.y).average().orElse(0);

        // Step 4: Sort by angle relative to centroid
        boundaryPoints.sort(Comparator.comparingDouble(p -> Math.atan2(p.y - centroidY, p.x - centroidX)));

        // Step 5: Update polygon with cleaned and sorted points
        polygon.getPoints().clear();
        for (Point p : boundaryPoints) {
            polygon.getPoints().addAll(p.x, p.y);
        }
    }


    //generovano ChatGPT
    // Compute Convex Hull using Graham’s scan algorithm (removes inside points)
    private List<Point> computeConvexHull(List<Point> points) {
        if (points.size() < 3) return new ArrayList<>(points); // No need to filter

        // Step 1: Find the lowest Y point (leftmost if tie)
        Point minYPoint = Collections.min(points, Comparator.comparingDouble((Point p) -> p.y)
                .thenComparingDouble(p -> p.x));

        // Step 2: Sort points by polar angle with minYPoint
        points.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.y - minYPoint.y, p1.x - minYPoint.x);
            double angle2 = Math.atan2(p2.y - minYPoint.y, p2.x - minYPoint.x);
            return Double.compare(angle1, angle2);
        });

        // Step 3: Use stack to build convex hull
        List<Point> hull = new ArrayList<>();
        for (Point p : points) {
            while (hull.size() >= 2 && crossProduct(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        return hull;
    }

    // Helper function to compute cross product
    private double crossProduct(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
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
