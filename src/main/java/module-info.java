module com.example.prague_analyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.prague_analyser to javafx.fxml;
    exports com.example.prague_analyser;
    exports com.example.prague_analyser.OSM;
    opens com.example.prague_analyser.OSM to javafx.fxml;
}