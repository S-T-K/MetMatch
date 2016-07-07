package com.mycompany.fxmltableview.gui;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.scene.layout.Pane;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;

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
       
        //on close
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
     
                            
                            public void handle(WindowEvent we) {
                               Alert dlg = new Alert(AlertType.CONFIRMATION, "");
                               dlg.setTitle("Close MetMatch");
                               dlg.setHeaderText("Are you sure you want to close MetMatch?");
                               dlg.setContentText("Unsaved data will be lost.");
                               Optional<ButtonType> result = dlg.showAndWait();
                               we.consume();
if (result.get() == ButtonType.OK){
     primaryStage.close();
     controller.close();
} else {
   
}
                                
                               
      }
  }); 
        
        
        
       

    }

    public static void main(String[] args) {
        launch(args);

    }

}
