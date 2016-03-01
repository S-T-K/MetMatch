/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.util.Collections;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author stefankoch
 */
public class ChartGenerator {

    public ChartGenerator() {
    }

    public LineChart generateEIC(Entry adduct) {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [seconds]");
        yAxis.setLabel("Intensity");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);
       

        // for all slices (= for all files)
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);

            XYChart.Series newSeries = new XYChart.Series();

            for (int j = 0; j < currentSlice.getIntensityList().size(); j++) {
                newSeries.getData().add(new XYChart.Data(currentSlice.getRetentionTimeList().get(j), currentSlice.getIntensityList().get(j)));

            }
            linechart.getData().add(newSeries);
            linechart.setCreateSymbols(false);
            linechart.setMaxSize(300, 200);
            
            //set Range
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(currentSlice.getRetentionTimeList().get(0));
            xAxis.setUpperBound(currentSlice.getRetentionTimeList().get(currentSlice.getRetentionTimeList().size() - 1));

        }
linechart.setLegendVisible(false);
        return linechart;
    }

    
    public LineChart generateNormalizedEIC(Entry adduct) {
        
        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [seconds]");
        yAxis.setLabel("Intensity (normalized)");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);
        
        
        // for all slices (= for all files)
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);

            XYChart.Series newSeries = new XYChart.Series();

            
            float maxIntensity = Collections.max(currentSlice.getIntensityList());
            
            for (int j = 0; j < currentSlice.getIntensityList().size(); j++) {
                newSeries.getData().add(new XYChart.Data(currentSlice.getRetentionTimeList().get(j), currentSlice.getIntensityList().get(j)/maxIntensity));

            }
            linechart.getData().add(newSeries);
            linechart.setCreateSymbols(false);
            linechart.setMaxSize(300, 200);
            
            //set Range
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(currentSlice.getRetentionTimeList().get(0));
            xAxis.setUpperBound(currentSlice.getRetentionTimeList().get(currentSlice.getRetentionTimeList().size() - 1));
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(1);


        }

        linechart.setLegendVisible(false);
        return linechart;
    }
    
    public ScatterChart generateMassChart(Entry adduct) {
        
        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [seconds]");
        yAxis.setLabel("Mass");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);
        

        // for all slices (= for all files)
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);

            XYChart.Series newSeries = new XYChart.Series();
            float maxIntensity = Collections.max(currentSlice.getIntensityList());

            for (int j = 0; j < currentSlice.getMassList().size(); j++) {
                if (currentSlice.getMassList().get(j) != null) {
                    XYChart.Data data = new XYChart.Data(currentSlice.getRetentionTimeList().get(j), currentSlice.getMassList().get(j));
                    Rectangle rect1 = new Rectangle(5, 5);
                    rect1.setFill(Color.RED);
                    data.setNode(rect1);
                    data.getNode().setOpacity(currentSlice.getIntensityList().get(j)/maxIntensity);
                    Tooltip tooltip = new Tooltip();
            tooltip.setText(currentSlice.getIntensityList().get(j).toString());
            Tooltip.install(data.getNode(), tooltip);
                    
                    newSeries.getData().add(data);
                }
                

            }
            scatterchart.getData().add(newSeries);
            
            scatterchart.setMaxSize(300, 200);
            
            //set Range
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(currentSlice.getRetentionTimeList().get(0));
            xAxis.setUpperBound(currentSlice.getRetentionTimeList().get(currentSlice.getRetentionTimeList().size() - 1));
            
            yAxis.setAutoRanging(false);
            yAxis.setTickUnit((adduct.getListofSlices().get(0).getMaxMZ()-adduct.getListofSlices().get(0).getMinMZ())/5);
            yAxis.setLowerBound(adduct.getListofSlices().get(0).getMinMZ());
            yAxis.setUpperBound(adduct.getListofSlices().get(0).getMaxMZ());
            
            
           

        }
scatterchart.setLegendVisible(false);
        return scatterchart;
 
    }
}