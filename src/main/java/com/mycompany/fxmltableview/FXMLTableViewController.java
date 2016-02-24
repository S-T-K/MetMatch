package com.mycompany.fxmltableview;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class FXMLTableViewController implements Initializable {

    //link fxml information to controller
    @FXML
    TableView<Met> metTable;

    @FXML
    TableColumn nameColumn;

    @FXML
    TableColumn scoreColumn;
    
    @FXML
    TableColumn rtColumn;

    //List with data for table
    ObservableList<Met> data;

    
    //add new Metabolite
    public void addMet(String name, double score) {
        Met met = new Met();
        met.setName(name);
        met.setScore(score);
        met.setProp1(100);
        data.add(met);

    }

    //initialize the table
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<Met, String>("name"));  //String in brackets has to be the same as PropertyValueFactory property= "..." in fxml
        scoreColumn.setCellValueFactory(new PropertyValueFactory<Met, Double>("score"));
        rtColumn.setCellValueFactory(new PropertyValueFactory<Met, Double>("prop1"));

        data = FXCollections.observableArrayList(
                new Met("A", 1.0),
                new Met("B", 2.0),
                new Met("C", 3.0)
        );
        metTable.setItems(data);
    }
}
