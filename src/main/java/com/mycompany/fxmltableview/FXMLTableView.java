package com.mycompany.fxmltableview;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.FileReader;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.scene.layout.Pane;
import static javafx.application.Application.launch;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class FXMLTableView extends Application {

    //JavaFX doesn't use main, but start
    @Override
    public void start(Stage primaryStage) throws Exception {
        //new loader
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_tableview.fxml"));
        //add title
        primaryStage.setTitle("FXML TableView Example");
        Pane myPane = (Pane) loader.load();
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        //show window
        primaryStage.show();
        //get reference to controller
        FXMLTableViewController controller = loader.<FXMLTableViewController>getController();
       
        
        
        
        
       

    }

    public static void main(String[] args) {
        launch(args);

    }

}
