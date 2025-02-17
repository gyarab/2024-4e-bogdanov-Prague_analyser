module com.example.prague_analyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;


    opens com.example.prague_analyser to javafx.fxml;
    exports com.example.prague_analyser;
    exports com.example.prague_analyser.OSM;
    opens com.example.prague_analyser.OSM to javafx.fxml;
}