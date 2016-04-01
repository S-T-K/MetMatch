package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry;
import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.converter.NumberStringConverter;

/**
 * FXML Controller class
 *
 * Controller for the Adduct GUI
 * @author stefankoch
 */
public class Fxml_shiftviewController implements Initializable {
    
    
    //Gridpane holding all the graphs
    @FXML
    VBox box;
    
    @FXML
    TextField refsetpen;
    
    @FXML 
    Button button;
    
    ChartGenerator chartGenerator;
    private FXMLTableViewController supercontroller;
    ObservableList<Entry> olist;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //add ChartGenerator
      chartGenerator = new ChartGenerator();
      
      
    }    
    
    //method that generates the graphs
    public void print(ObservableList<Entry> list) {
    olist = list;   
       //get selected Entry
       
          
    
           LineChart<Number,Number> linechart1 = chartGenerator.generateShiftChart(olist);
           box.getChildren().add(linechart1);
           
           
         
        
       }
    
    public void recalculate() throws IOException, InterruptedException {
       CountDownLatch latch = new CountDownLatch(1);
        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
                supercontroller.calculate(latch);
                
                
                return null;
            }
        };
       new Thread(task).start();
       latch.await();
        
      
        
        
        
        LineChart<Number,Number> linechart1 = chartGenerator.generateShiftChart(olist);
        box.getChildren().remove(box.getChildren().size()-1);
        box.getChildren().add(linechart1);
    }

    /**
     * @return the supercontroller
     */
    public FXMLTableViewController getSupercontroller() {
        return supercontroller;
    }

    /**
     * @param supercontroller the supercontroller to set
     */
    public void setSupercontroller(FXMLTableViewController supercontroller) {
        this.supercontroller = supercontroller;
        refsetpen.textProperty().bindBidirectional(supercontroller.session.getCurrentdataset().getPenaltyProperty(), new NumberStringConverter());
    }
       
      
     
        
    
    
    
}
