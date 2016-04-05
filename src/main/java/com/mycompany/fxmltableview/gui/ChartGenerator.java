/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import flanagan.analysis.CurveSmooth;
import static java.lang.Double.NaN;
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
import java.util.Arrays;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import static java.lang.Math.abs;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.ArrayUtils;

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
        for (int f = 0; f <adduct.getSession().getCurrentdataset().getListofFiles().size(); f++ ) {
            RawDataFile currentfile = adduct.getSession().getCurrentdataset().getListofFiles().get(f);
            Slice currentSlice = adduct.getListofSlices().get(currentfile);
            XYChart.Series newSeries = new XYChart.Series();

            //while the next RT is the same as the one before, add Intensities
            //double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getIntensityArray().length; j++) {
                double intensity = currentSlice.getIntensityArray()[j];
                double currentRT = currentSlice.getRTArray()[j];
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

        double lower = adduct.getMinRT();
        double upper = adduct.getMaxRT();
        //set Range
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        linechart.setAnimated(false);
//        linechart.setCache(true);
//        linechart.setCacheHint(CacheHint.SPEED);
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
        for (int f = 0; f <adduct.getSession().getCurrentdataset().getListofFiles().size(); f++ ) {
            RawDataFile currentfile = adduct.getSession().getCurrentdataset().getListofFiles().get(f);
            Slice currentSlice = adduct.getListofSlices().get(currentfile);

            XYChart.Series newSeries = new XYChart.Series();

            double maxIntensity = Arrays.stream(currentSlice.getIntensityArray()).max().getAsDouble();
            //double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getIntensityArray().length; j++) {
                double intensity = currentSlice.getIntensityArray()[j];
                double currentRT = currentSlice.getRTArray()[j];
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

        
        
        
        //double endouter = System.currentTimeMillis();
        //System.out.println("Outer loop norm: " + (endouter-startouter));
//set Range

        
          if (adduct.getOGroupObject().getFittedShift(currentfile)>0) {
        XYChart.Series newSeries2 = new XYChart.Series();
        double[] RTArray = adduct.getRTArray();
        newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getFittedShift(currentfile)], 0));
        newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getFittedShift(currentfile)], 1));
        linechart.getData().add(newSeries2);
        linechart.applyCss();
        ((Path) newSeries2.getNode()).setStroke(currentfile.getColor());
        ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth());
        ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(4d, 4d, 4d, 4d, 4d);
          }
        }
        double lower = adduct.getMinRT();
        double upper = adduct.getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        linechart.setAnimated(false);
//        linechart.setCache(true);
//        linechart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }
    
    public LineChart generateNormalizedEICwithProp(Entry adduct) {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity (normalized)");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);

        // for all slices (= for all files)
        //double startouter = System.currentTimeMillis();
        for (int f = 0; f <adduct.getSession().getCurrentdataset().getListofFiles().size(); f++ ) {
            RawDataFile currentfile = adduct.getSession().getCurrentdataset().getListofFiles().get(f);
            Slice currentSlice = adduct.getListofSlices().get(currentfile);

            XYChart.Series newSeries = new XYChart.Series();

            double maxIntensity = Arrays.stream(currentSlice.getIntensityArray()).max().getAsDouble();
            //double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getIntensityArray().length; j++) {
                double intensity = currentSlice.getIntensityArray()[j];
                double currentRT = currentSlice.getRTArray()[j];
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
double lower = adduct.getMinRT();
        double upper = adduct.getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        
        linechart.setAnimated(false);
//        linechart.setCache(true);
//        linechart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(PropArray(adduct, linechart));
        return linechart;
    }

    public ScatterChart generateMassChart(Entry adduct) {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("ppm M/Z deviation");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);

        // for all slices (= for all files)
        //double startouter = System.currentTimeMillis();
        for (int f = 0; f <adduct.getSession().getCurrentdataset().getListofFiles().size(); f++ ) {
            RawDataFile currentfile = adduct.getSession().getCurrentdataset().getListofFiles().get(f);
            Slice currentSlice = adduct.getListofSlices().get(currentfile);

            XYChart.Series newSeries = new XYChart.Series();
            double maxIntensity = Arrays.stream(currentSlice.getIntensityArray()).max().getAsDouble();
            double width = currentSlice.getFile().getWidth() + 1.5;
//double startinner = System.currentTimeMillis();
            for (int j = 0; j < currentSlice.getMZArray().length; j++) {
                    XYChart.Data data = new XYChart.Data(currentSlice.getRTArray()[j], getppm(adduct.getMZ(),currentSlice.getMZArray()[j]));

                    //rect is the node of the plot
                    Rectangle rect1 = new Rectangle(width, width);
                    rect1.setFill(currentSlice.getFile().getColor());

                    //set rect as node
                    data.setNode(rect1);

                    //set opacity
                    data.getNode().setOpacity(currentSlice.getIntensityArray()[j] / maxIntensity);

                    //set Tooltip
                    //Tooltip tooltip = new Tooltip();
                    //tooltip.setText(currentSlice.getIntensityList().get(j).toString());
                    //Tooltip.install(data.getNode(), tooltip);
                    newSeries.getData().add(data);
                }

            
            //double endinner = System.currentTimeMillis();
//System.out.println("Inner loop mass: " + (endinner-startinner));
            scatterchart.getData().add(newSeries);
            scatterchart.setMaxSize(300, 200);

            //set Range
        }
        //double endouter = System.currentTimeMillis();
        //System.out.println("Outer loop mass: " + (endouter-startouter));
        double lower = adduct.getMinRT();
        double upper = adduct.getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 7);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setTickUnit((adduct.getSession().getMZTolerance()*2 / 5));
        yAxis.setLowerBound(-1*adduct.getSession().getMZTolerance());
        yAxis.setUpperBound(adduct.getSession().getMZTolerance());
        scatterchart.setAnimated(false);
//        scatterchart.setCache(true);
//        scatterchart.setCacheHint(CacheHint.SPEED);
        scatterchart.setLegendVisible(false);
       scatterchart.setHorizontalZeroLineVisible(false);
        
        return scatterchart;

    }
    
    
    
    public double PropArray(Entry adduct, LineChart<Number, Number> linechart) {
        
    
        double maxProp = 1;
        for (int f = 0; f <adduct.getSession().getCurrentdataset().getListofFiles().size(); f++ ) {
            RawDataFile currentfile = adduct.getSession().getCurrentdataset().getListofFiles().get(f);
        
        
        if (adduct.getOGroupObject().getOGroupPropArray(currentfile)== null) {
            adduct.getOGroupObject().generateOGroupPropArray(currentfile);
        }
        
        
        double[] PropArray = adduct.getOGroupObject().getOGroupPropArray(currentfile);
//        
        List asList = Arrays.asList(ArrayUtils.toObject(PropArray));
        double max = (double) Collections.max(asList);
        double[] RTArray = adduct.getRTArray();
        XYChart.Series newSeries = new XYChart.Series();
        
        for (int i =0; i<PropArray.length; i++) {
            newSeries.getData().add(new XYChart.Data(RTArray[i], PropArray[i]));
            if (maxProp<PropArray[i]) {
                maxProp =PropArray[i];}
            
        }
        linechart.getData().add(newSeries);
        linechart.applyCss();
        ((Path) newSeries.getNode()).setStroke(Color.ORANGE);
        ((Path) newSeries.getNode()).setStrokeWidth(1.5); 
        
        double[] PropArray2 = adduct.getAdductPropArray(currentfile);
        
        List asList2 = Arrays.asList(ArrayUtils.toObject(PropArray2));
        double max2 = (double) Collections.max(asList2);
        double[] RTArray2 = adduct.getRTArray();
        XYChart.Series newSeries3 = new XYChart.Series();
        
        for (int i =0; i<PropArray2.length; i++) {
            newSeries3.getData().add(new XYChart.Data(RTArray2[i], PropArray2[i]));
            
        }
        linechart.getData().add(newSeries3);
        linechart.applyCss();
        ((Path) newSeries3.getNode()).setStroke(Color.RED);
        ((Path) newSeries3.getNode()).setStrokeWidth(1.5); 
        
        if (adduct.getOGroupObject().getFittedShift(currentfile)>0) {
        XYChart.Series newSeries2 = new XYChart.Series();
        newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getFittedShift(currentfile)], 0));
        newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getFittedShift(currentfile)], maxProp));
        linechart.getData().add(newSeries2);
        linechart.applyCss();
        ((Path) newSeries2.getNode()).setStroke(currentfile.getColor());
        ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth()); 
        ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(4d, 4d, 4d, 4d, 4d);
        }
        
        
        }
        return maxProp;
    }
    
    
    public LineChart generateShiftChart(ObservableList<Entry> list) {
        
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Shift [seconds]");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);
       
        double upper = 0;
        double lower = 0;
        
        for (int f = 0; f < list.get(0).getSession().getCurrentdataset().getListofFiles().size(); f++) {
            RawDataFile currentfile = list.get(0).getSession().getCurrentdataset().getListofFiles().get(f);
         XYChart.Series newSeries = new XYChart.Series();
        
        
        double shiftiter = (list.get(0).getSession().getRTTolerance()*2)/list.get(0).getSession().getResolution();
        int middleint = (list.get(0).getSession().getResolution()/2)-1;
        
        
        
        for (int i = 0; i< list.size(); i++) {
            double shift = (list.get(i).getFittedShift(currentfile)-middleint)*shiftiter*60;
            XYChart.Data data = new XYChart.Data(list.get(i).getRT(), shift);
            newSeries.getData().add(data);
            if(shift>upper) {
                upper = shift;
            } else if (shift<lower) {
                lower = shift;
            }
        }
        linechart.getData().add(newSeries);
            linechart.applyCss();
            ((Path) newSeries.getNode()).setStroke(currentfile.getColor());
            ((Path) newSeries.getNode()).setStrokeWidth(currentfile.getWidth());
            
            
                    
        
        System.out.println("Charts " + (f+1) + "of " + list.get(0).getSession().getCurrentdataset().getListofFiles().size() + " drawn");
        }
        linechart.setCreateSymbols(false);
            linechart.setMaxSize(2000, 500);
            linechart.setLegendVisible(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lower - 20);
        yAxis.setUpperBound(upper + 20);
        return linechart;
    }
    
    public double getppm(double massref, double mass) {
        double dif = massref-mass;

        return dif/(massref/1000000);
    }
    
    
}
