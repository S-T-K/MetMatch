package com.mycompany.fxmltableview;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.scene.layout.Pane;

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
        System.out.println(controller);
        System.out.println(controller.metTable.getItems().get(0).getName());
        //add Metabolite to Table
        controller.addMet("Test", 999);
        //remove the first two Metabolites
        controller.metTable.getItems().remove(0, 2);

    }

    public static void main(String[] args) {
        launch(args);

    }

}
