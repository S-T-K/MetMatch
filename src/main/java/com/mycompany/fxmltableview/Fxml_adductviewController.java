package com.mycompany.fxmltableview;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.Entry;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

/**
 * FXML Controller class
 *
 * @author stefankoch
 */
public class Fxml_adductviewController implements Initializable {
    
    
     
    @FXML
    GridPane gridPane;
    
    
    TreeTableView<Entry> metTable;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      
    }    
    
    
    public void print() {
        ChartGenerator chartGenerator = new ChartGenerator();
       Entry entry = metTable.getSelectionModel().getSelectedItem().getValue();
       gridPane.getChildren().clear();
       for (int i = 0; i<entry.getListofAdducts().size(); i++) {
           
           Label label = new Label(Double.toString(entry.getListofAdducts().get(i).getMZ()));
           label.setRotate(270);
           gridPane.addRow(i,label);
           LineChart<Number,Number> linechart = chartGenerator.generateEIC(entry.getListofAdducts().get(i));
           gridPane.addColumn(1,linechart);
           linechart = chartGenerator.generateNormalizedEIC(entry.getListofAdducts().get(i));
           gridPane.addColumn(2, linechart);
           ScatterChart<Number,Number> scatterchart = chartGenerator.generateMassChart(entry.getListofAdducts().get(i));
           gridPane.addColumn(3, scatterchart);
       }
       
     
        
    }
    
    public void next() {
        metTable.getSelectionModel().getSelectedItem().setExpanded(false);
        metTable.getSelectionModel().selectNext();
        print();
        
    }
    
    public void previous() {
        metTable.getSelectionModel().getSelectedItem().setExpanded(false);
        metTable.getSelectionModel().selectPrevious();
        print();
        
    }
}
