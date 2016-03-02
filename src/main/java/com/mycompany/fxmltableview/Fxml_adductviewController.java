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
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

/**
 * FXML Controller class
 *
 * @author stefankoch
 */
public class Fxml_adductviewController implements Initializable {
    
    
     
    @FXML
    GridPane gridPane;
    
    
    TreeTableView<Entry> metTable;
    ChartGenerator chartGenerator;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      chartGenerator = new ChartGenerator();
    }    
    
    
    public void print() {
        
       Entry entry = metTable.getSelectionModel().getSelectedItem().getValue();
       gridPane.getChildren().clear();
       for (int i = 0; i<entry.getListofAdducts().size(); i++) {
           
           Label label = new Label(Double.toString(entry.getListofAdducts().get(i).getMZ()));
           label.setRotate(270);
           gridPane.addRow(i,label);
           LineChart<Number,Number> linechart1 = chartGenerator.generateEIC(entry.getListofAdducts().get(i));
           gridPane.addColumn(1,linechart1);
           LineChart<Number,Number> linechart2 = chartGenerator.generateNormalizedEIC(entry.getListofAdducts().get(i));
           gridPane.addColumn(2, linechart2);
           ScatterChart<Number,Number> scatterchart = chartGenerator.generateMassChart(entry.getListofAdducts().get(i));
           gridPane.addColumn(3, scatterchart);
           System.out.println("new" + linechart1.getData().get(0).getNode().getId());
           System.out.println(scatterchart.getData().get(0).getData().get(0).getNode().getClass());
           System.out.println(linechart1.getData().get(0).getNode().getClass());
           System.out.println(((Path)linechart1.getData().get(0).getNode()).getStroke());
           System.out.println(((Rectangle)scatterchart.getData().get(0).getData().get(0).getNode()).getFill());
           System.out.println(((Path)linechart1.getData().get(0).getNode()).getStrokeWidth());
        
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
