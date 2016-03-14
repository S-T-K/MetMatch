/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import static java.lang.Math.abs;
import java.util.Collections;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;

/**
 *
 * @author stefankoch TODO: Just read every slice once and generate all the
 * different graphs TODO: Performance:
 * http://stackoverflow.com/questions/28850211/performance-issue-with-javafx-linechart-with-65000-data-points
 *
 */
public class ChartGenerator {

    public ChartGenerator() {
    }

    public LineChart generateEIC(Entry adduct) {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);

        //double startouter = System.currentTimeMillis();
        // for all slices (= for all files)
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);
            XYChart.Series newSeries = new XYChart.Series();

            //while the next RT is the same as the one before, add Intensities
            //double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getIntensityList().size(); j++) {
                float intensity = currentSlice.getIntensityList().get(j);
                float currentRT = currentSlice.getRetentionTimeList().get(j);
                while (j < currentSlice.getIntensityList().size() - 1 && abs(currentRT - currentSlice.getRetentionTimeList().get(j + 1)) < 0.0001) {
                    j++;
                    intensity = intensity + currentSlice.getIntensityList().get(j);

                }

                XYChart.Data data = new XYChart.Data(currentRT, intensity);

                newSeries.getData().add(data);

            }

            // add new Series
            linechart.getData().add(newSeries);
            //apply Css to create nodes
            linechart.applyCss();
            //cast to path to be able to set stroke
            ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor());
            ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());

        }

        //don't draw symbols
        linechart.setCreateSymbols(false);
        //set size of chart
        linechart.setMaxSize(300, 200);

        float lower = adduct.getListofSlices().get(0).getMinRT();
        float upper = adduct.getListofSlices().get(0).getMaxRT();
        //set Range
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 7);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        linechart.setAnimated(false);
        linechart.setCache(true);
        linechart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }

    public LineChart generateNormalizedEIC(Entry adduct) {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity (normalized)");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);

        // for all slices (= for all files)
        //double startouter = System.currentTimeMillis();
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);

            XYChart.Series newSeries = new XYChart.Series();

            float maxIntensity = Collections.max(currentSlice.getIntensityList());
            //double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getIntensityList().size(); j++) {
                float intensity = currentSlice.getIntensityList().get(j);
                float currentRT = currentSlice.getRetentionTimeList().get(j);
                while (j < currentSlice.getIntensityList().size() - 1 && abs(currentRT - currentSlice.getRetentionTimeList().get(j + 1)) < 0.0001) {
                    j++;
                    intensity = intensity + currentSlice.getIntensityList().get(j);

                }

                newSeries.getData().add(new XYChart.Data(currentRT, intensity / maxIntensity));

            }
            //double endinner = System.currentTimeMillis();
            //System.out.println("Inner loop norm: " + (endinner-startinner));
            linechart.getData().add(newSeries);
            linechart.applyCss();
            ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor());
            ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());
            linechart.setCreateSymbols(false);
            linechart.setMaxSize(300, 200);

        }
        
        //double endouter = System.currentTimeMillis();
        //System.out.println("Outer loop norm: " + (endouter-startouter));
//set Range
float lower = adduct.getListofSlices().get(0).getMinRT();
        float upper = adduct.getListofSlices().get(0).getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 7);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        linechart.setAnimated(false);
        linechart.setCache(true);
        linechart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }

    public ScatterChart generateMassChart(Entry adduct) {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Mass");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);

        // for all slices (= for all files)
        //double startouter = System.currentTimeMillis();
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);

            XYChart.Series newSeries = new XYChart.Series();
            float maxIntensity = Collections.max(currentSlice.getIntensityList());
            double width = currentSlice.getFile().getWidth() + 1.5;
//double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getMassList().size(); j++) {
                if (currentSlice.getMassList().get(j) != null) {
                    XYChart.Data data = new XYChart.Data(currentSlice.getRetentionTimeList().get(j), currentSlice.getMassList().get(j));

                    //rect is the node of the plot
                    Rectangle rect1 = new Rectangle(width, width);
                    rect1.setFill(currentSlice.getFile().getColor());

                    //set rect as node
                    data.setNode(rect1);

                    //set opacity
                    data.getNode().setOpacity(currentSlice.getIntensityList().get(j) / maxIntensity);

                    //set Tooltip
                    //Tooltip tooltip = new Tooltip();
                    //tooltip.setText(currentSlice.getIntensityList().get(j).toString());
                    //Tooltip.install(data.getNode(), tooltip);
                    newSeries.getData().add(data);
                }

            }
            //double endinner = System.currentTimeMillis();
//System.out.println("Inner loop mass: " + (endinner-startinner));
            scatterchart.getData().add(newSeries);
            scatterchart.setMaxSize(300, 200);

            //set Range
        }
        //double endouter = System.currentTimeMillis();
        //System.out.println("Outer loop mass: " + (endouter-startouter));
        float lower = adduct.getListofSlices().get(0).getMinRT();
        float upper = adduct.getListofSlices().get(0).getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 7);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setTickUnit((adduct.getListofSlices().get(0).getMaxMZ() - adduct.getListofSlices().get(0).getMinMZ()) / 5);
        yAxis.setLowerBound(adduct.getListofSlices().get(0).getMinMZ());
        yAxis.setUpperBound(adduct.getListofSlices().get(0).getMaxMZ());
        scatterchart.setAnimated(false);
        scatterchart.setCache(true);
        scatterchart.setCacheHint(CacheHint.SPEED);
        scatterchart.setLegendVisible(false);
        return scatterchart;

    }
    
     public LineChart generateNormalizedEICAVG(Entry adduct) {

        
         
        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity (normalized)");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);

        // for all slices (= for all files)
        //double startouter = System.currentTimeMillis();
        for (int i = 0; i < adduct.getListofSlices().size(); i++) {
            Slice currentSlice = adduct.getListofSlices().get(i);
          

            XYChart.Series newSeries = new XYChart.Series();

            float maxIntensity = Collections.max(currentSlice.getIntensityList());
            //double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getIntensityList().size(); j++) {
                float intensity = currentSlice.getIntensityList().get(j);
                float currentRT = currentSlice.getRetentionTimeList().get(j);
                while (j < currentSlice.getIntensityList().size() - 1 && abs(currentRT - currentSlice.getRetentionTimeList().get(j + 1)) < 0.0001) {
                    j++;
                    intensity = intensity + currentSlice.getIntensityList().get(j);

                }

                newSeries.getData().add(new XYChart.Data(currentRT, intensity / maxIntensity));

            }
            //double endinner = System.currentTimeMillis();
            //System.out.println("Inner loop norm: " + (endinner-startinner));
            linechart.getData().add(newSeries);
            linechart.applyCss();
            ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor());
            ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());
            linechart.setCreateSymbols(false);
            linechart.setMaxSize(300, 200);

        }
        
        XYChart.Series newSeries = new XYChart.Series();
        Peak peak = adduct.getPeak();
        peak.normalize();
        for (int j = 0; j < peak.getRTArray().length; j++){
            newSeries.getData().add(new XYChart.Data(peak.getRTArray()[j], peak.getIntensityArray()[j]));
            
        }
        
        linechart.getData().add(newSeries);
        linechart.applyCss();
        ((Path) newSeries.getNode()).setStroke(Color.RED);
        ((Path) newSeries.getNode()).setStrokeWidth(2.0);
        
        //double endouter = System.currentTimeMillis();
        //System.out.println("Outer loop norm: " + (endouter-startouter));
//set Range
float lower = adduct.getListofSlices().get(0).getMinRT();
        float upper = adduct.getListofSlices().get(0).getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 7);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        linechart.setAnimated(false);
        linechart.setCache(true);
        linechart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }
}
