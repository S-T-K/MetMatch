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
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

/**
 * FXML Controller class
 *
 * @author stefankoch
 */
public class Fxml_adductviewController implements Initializable {
    
    @FXML
    Label label;
     @FXML
    NumberAxis RTAxis;
      @FXML
    NumberAxis intensityAxis;
    
    @FXML
    LineChart chart;
    
    @FXML
    GridPane gridPane;
    
    
    public Entry entry;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      
    }    
    
    public void print() {
        
         System.out.println(entry.getListofAdducts().get(0).getRT());
         label.setText(Double.toString(entry.getListofAdducts().get(0).getRT()));
          XYChart.Series series = new XYChart.Series();
        series.setName("My portfolio");
        //populating the series with data
        Slice slice = entry.getListofAdducts().get(0).getListofSlices().get(0);
        
        
        for (int i = 0; i<slice.getIntensityList().size(); i++) {
            series.getData().add(new XYChart.Data(slice.getRetentionTimeList().get(i), slice.getIntensityList().get(i)));
            
            
            
        }
        
        
        chart.getData().add(series);
       RTAxis.setAutoRanging(false);
       RTAxis.setLowerBound(slice.getRetentionTimeList().get(0));
       RTAxis.setUpperBound(slice.getRetentionTimeList().get(slice.getRetentionTimeList().size()-1));
       
       for (int i = 0; i<entry.getListofAdducts().size(); i++) {
           Label test = new Label(Double.toString(entry.getListofAdducts().get(i).getRT()));
           gridPane.addRow(i+1,test);
       }
       
     
        
    }
}
