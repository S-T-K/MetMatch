package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.commons.io.FileUtils;

public class FXMLTableView extends Application {

    //JavaFX doesn't use main, but start
    @Override
    public void start(Stage primaryStage) throws Exception {
//        System.out.println("But me too!");
        //new loader
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_tableview.fxml"));
        //add title
        primaryStage.setTitle("MetMatch [Alpha] Main Window");
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
//        System.out.println("I do start!");
//        String Property = System.getProperty("java.library.path");
//        Property = "C:\\Users\\stefankoch\\MetMatch\\R\\R-3.2.3\\bin\\x64;C:\\Users\\stefankoch\\MetMatch\\R\\R-3.2.3\\library\\rJava\\jri;" + Property;
//        
//        System.setProperty("java.library.path",Property);
//        System.out.println(System.getProperty("java.library.path"));

        //Handle your exception here
        System.out.println("Starting MetMatch [Alpha]...");
        launch(args);

    }

}
