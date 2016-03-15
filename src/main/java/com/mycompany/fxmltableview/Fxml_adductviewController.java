package com.mycompany.fxmltableview;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.Entry;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

/**
 * FXML Controller class
 *
 * Controller for the Adduct GUI
 * @author stefankoch
 */
public class Fxml_adductviewController implements Initializable {
    
    
    //Gridpane holding all the graphs
    @FXML
    GridPane gridPane;
    
    
    TreeTableView<Entry> metTable;
    ChartGenerator chartGenerator;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //add ChartGenerator
      chartGenerator = new ChartGenerator();
    }    
    
    //method that generates the graphs
    public void print() {
        
       //get selected Entry
       Entry entry = metTable.getSelectionModel().getSelectedItem().getValue();
       float upper = (float) (entry.getRT()+1.5);
       float lower = (float) (entry.getRT()-1.5);
       
       //delete previous graphs
       gridPane.getChildren().clear();
       
       //for every Adduct/Fragment
       for (int i = 0; i<entry.getListofAdducts().size(); i++) {
           
           //Label showing the MZ
           Label label = new Label(Double.toString(entry.getListofAdducts().get(i).getMZ()));
           label.setRotate(270);
           
           //generate graphs
          
           gridPane.addRow(i,label);
           LineChart<Number,Number> linechart1 = chartGenerator.generateEIC(entry.getListofAdducts().get(i));
           gridPane.addColumn(1,linechart1);
           LineChart<Number,Number> linechart2 = chartGenerator.generateNormalizedBestPeakEIC(entry.getListofAdducts().get(i));
           gridPane.addColumn(2, linechart2);
           ScatterChart<Number,Number> scatterchart = chartGenerator.generateMassChart(entry.getListofAdducts().get(i));
           gridPane.addColumn(3, scatterchart);
           
         
        
       }
       
      
     
        
    }
    
    //select next metabolite, changes Selection in Main GUI
    public void next() {
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().getParent());
        }
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().nextSibling());
        print();
        
    }
    
    //select previous metablite, changes Selection in Main GUI
    public void previous() {
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().getParent());
        }
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().previousSibling());
        print();
        
    }
}
