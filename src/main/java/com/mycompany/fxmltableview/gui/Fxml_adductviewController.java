package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.sun.webkit.ContextMenuItem;
import java.awt.Checkbox;

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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
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
    
 @FXML 
   ScrollPane scrollPane;
    
    
    TreeTableView<Entry> metTable;
    ChartGenerator chartGenerator;
    boolean showProp;

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
       int adductnumber = 0;
       Entry entry;
       TreeItem<Entry> OGroupItem;
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
         entry = metTable.getSelectionModel().getSelectedItem().getValue().getOGroupObject();
         TreeItem<Entry> AdductItem = metTable.getSelectionModel().getSelectedItem();
         OGroupItem = metTable.getSelectionModel().getSelectedItem().getParent();
         adductnumber = OGroupItem.getChildren().indexOf(AdductItem);
         System.out.println(adductnumber);
        } else {
       entry = metTable.getSelectionModel().getSelectedItem().getValue();
       OGroupItem = metTable.getSelectionModel().getSelectedItem();}
       
       //delete previous graphs
       gridPane.getChildren().clear();
       scrollPane.setVvalue(((double)adductnumber)/(entry.getListofAdducts().size()-1));
       
       //for every Adduct/Fragment
       for (int i = 0; i<entry.getListofAdducts().size(); i++) {
           
           Entry adduct = OGroupItem.getChildren().get(i).getValue();
           
           
           //Label showing the MZ
           Label label = new Label(Double.toString(adduct.getMZ()));
           label.setRotate(270);
           
           //generate graphs
          
           
           gridPane.addRow(i,label);
           LineChart<Number,Number> linechart1 = chartGenerator.generateEIC(adduct);
           gridPane.addColumn(1,linechart1);
           if (showProp) {
           LineChart<Number,Number> linechart2 = chartGenerator.generateNormalizedEICwithProp(adduct);
           gridPane.addColumn(2, linechart2);
           } else {
           LineChart<Number,Number> linechart2 = chartGenerator.generateNormalizedEIC(adduct);
           gridPane.addColumn(2, linechart2);
           }
           
           ScatterChart<Number,Number> scatterchart = chartGenerator.generateMassChart(adduct);
           gridPane.addColumn(3, scatterchart);
           
         
        
       }
       
      
    System.out.println(scrollPane.getVmax());
     System.out.println(scrollPane.getVmin());
        
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
    
    public void setShowProp() {
        showProp=!showProp;
        print();
        
    }
}
