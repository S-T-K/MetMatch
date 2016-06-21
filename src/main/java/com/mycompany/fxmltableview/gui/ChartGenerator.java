/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Peak;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import com.mycompany.fxmltableview.logic.Session;
import java.io.File;
import java.util.Collections;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import org.apache.commons.lang3.ArrayUtils;
import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.shape.Ellipse;

/**
 *
 * @author stefankoch TODO: Just read every slice once and generate all the
 different graphs TODO: Performance:
 http://stackoverflow.com/questions/28850211/performance-issue-with-javafx-areachart-with-65000-data-points
 *
 */
public class ChartGenerator {
    
    private Fxml_adductviewController adductcontroller;
    private Fxml_pathshiftviewController shiftcontroller;
    private Fxml_gravityshiftviewController newshiftcontroller;
    

    private Session session;
    

    public ChartGenerator(Fxml_adductviewController controller, Fxml_pathshiftviewController shiftcontroller, Fxml_gravityshiftviewController newshiftcontroller) {
        this.session = session;
        this.adductcontroller = controller;
        this.shiftcontroller = shiftcontroller;
        this.newshiftcontroller=newshiftcontroller;

       
        
       
    }

    public LineChart generateEIC(Entry adduct) throws InterruptedException {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);
        
        
        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
            adductcontroller.getAdducttochart().get(adduct).add(linechart);
        } else {
            ArrayList list = new ArrayList();
            list.add(linechart);
            adductcontroller.getAdducttochart().put(adduct, list);
        }
        
        

        //float startouter = System.currentTimeMillis();
        // for all slices (= for all files)
        for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                float[] RTArray=currentfile.getRTArray();
                if(adduct.getListofSlices().containsKey(currentfile)) {
                Slice currentSlice = adduct.getListofSlices().get(currentfile);
                if (currentSlice.isStored()) {
                     System.out.println("Adding Read Slice from ChartGenerator");
                    session.getIothread().readslice(currentSlice);
                    while (currentSlice.isStored()) {
                        Thread.sleep(10);
                    }
                }
                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, linechart);
                adductcontroller.getAlignableseries().put(newSeries,adduct);
                
                //add Series to HashMaps
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList list = new ArrayList();
                list.add(newSeries);
                adductcontroller.getFiletoseries().put(currentfile, list);
                        }

                //while the next RT is the same as the one before, add Intensities
                //float startinner = System.currentTimeMillis();
                float[]intArr = currentSlice.getIntArray();
                int startRT = currentSlice.getRTstart();
                List<XYChart.Data> points = new ArrayList<>(intArr.length);
                for (int j = 0; j < intArr.length; j++) {
                    float intensity = intArr[j];
                    float currentRT = RTArray[startRT+j];
                    XYChart.Data data = new XYChart.Data(currentRT, intensity);

                    points.add(data);

                }
                newSeries.getData().addAll(points);
                File file = new File("C:\\Users\\stefankoch\\Documents\\NetBeansProjects\\JavaFXTable\\src\\main\\java\\com\\mycompany\\fxmltableview\\gui\\stylesheet2.css");
                linechart.getStylesheets().clear();
                linechart.getStylesheets().add("file:///" + file.getAbsolutePath().replace("\\", "/"));

                // add new Series
                linechart.getData().add(newSeries);
                //apply Css to create nodes
                linechart.applyCss();
                //cast to path to be able to set stroke
                if (currentfile.isselected()) {
                    paintselectedLine(newSeries.getNode());
                }else {
                ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor()); 
                }
                
                ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());


            }}
        }}}

        //don't draw symbols
//        linechart.setCreateSymbols(false);
        //set size of chart
        linechart.setMaxSize(450, 300);

        float lower = adduct.getMinRT();
        float upper = adduct.getMaxRT();
        //set Range
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        linechart.setAnimated(false);
//        areachart.setCache(true);
//        areachart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }

      public AreaChart generateAreaTest(Entry adduct) throws InterruptedException {
//Info:
//newSeries.getNode() is Group, consisting of 2 Paths, 0 is a simple line, as in areachart, 1 is the line plus the area, color is a pale version of line
//1 can be set invisible, then it works like a normal line chart
          
          
        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity");
        AreaChart<Number, Number> linechart = new AreaChart(xAxis, yAxis);
        
        
        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
            adductcontroller.getAdducttochart().get(adduct).add(linechart);
        } else {
            ArrayList list = new ArrayList();
            list.add(linechart);
            adductcontroller.getAdducttochart().put(adduct, list);
        }
        
        

        //float startouter = System.currentTimeMillis();
        // for all slices (= for all files)
        for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                float[] RTArray=currentfile.getRTArray();
                if(adduct.getListofSlices().containsKey(currentfile)) {
                Slice currentSlice = adduct.getListofSlices().get(currentfile);
                if (currentSlice.isStored()) {
                     System.out.println("Adding Read Slice from ChartGenerator");
                    session.getIothread().readslice(currentSlice);
                    while (currentSlice.isStored()) {
                        Thread.sleep(10);
                    }
                }
                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, linechart);
                
                //add Series to HashMaps
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList list = new ArrayList();
                list.add(newSeries);
                adductcontroller.getFiletoseries().put(currentfile, list);
                        }

                //while the next RT is the same as the one before, add Intensities
                //float startinner = System.currentTimeMillis();
                float[]intArr = currentSlice.getIntArray();
                int startRT = currentSlice.getRTstart();
                
                for (int j = 0; j < intArr.length; j++) {
                    float intensity = intArr[j];
                    float currentRT = RTArray[startRT+j];
                    XYChart.Data data = new XYChart.Data(currentRT, intensity);

                    newSeries.getData().add(data);

                }

                // add new Series
                linechart.getData().add(newSeries);
                //apply Css to create nodes
                linechart.applyCss();
                //cast to path to be able to set stroke
                if (currentfile.isselected()) {
                    paintselectedLine(((Group) newSeries.getNode()).getChildren().get(0));
                    ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(Color.color(1,0.5,0.5));
                    
                }else {
                    
                ObservableList<Node> list = ((Group) newSeries.getNode()).getChildren();
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStroke(currentSlice.getFile().getColor());
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setStroke(currentSlice.getFile().getColor());
                double[] color = new double[] {currentSlice.getFile().getColor().getRed(),currentSlice.getFile().getColor().getGreen(),currentSlice.getFile().getColor().getBlue()};
                color = brightencolor(color);
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(Color.color(color[0],color[1],color[2]));
                }
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setVisible(false);
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStrokeWidth(currentSlice.getFile().getWidth());
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setStrokeWidth(currentSlice.getFile().getWidth());


            }}
        }}}

        //don't draw symbols
        linechart.setCreateSymbols(false);
        //set size of chart
        linechart.setMaxSize(450, 300);

        float lower = adduct.getMinRT();
        float upper = adduct.getMaxRT();
        //set Range
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        linechart.setAnimated(false);
//        areachart.setCache(true);
//        areachart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }
    public LineChart generateNormalizedEIC(Entry adduct) throws InterruptedException {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity (normalized)");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);
        
        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
            adductcontroller.getAdducttochart().get(adduct).add(linechart);
        } else {
            ArrayList list = new ArrayList();
            list.add(linechart);
            adductcontroller.getAdducttochart().put(adduct, list);
        }

        // for all slices (= for all files)
        //float startouter = System.currentTimeMillis();
         for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                float[] RTArray=currentfile.getRTArray();
                if(adduct.getListofSlices().containsKey(currentfile)) {
                Slice currentSlice = adduct.getListofSlices().get(currentfile);

                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, linechart);
 
                //add Series to HashMaps
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList list = new ArrayList();
                list.add(newSeries);
                adductcontroller.getFiletoseries().put(currentfile, list);
                        }
                float[]intArr = currentSlice.getIntArray();
                int startRT = currentSlice.getRTstart();
                float maxIntensity = currentSlice.getMaxIntensity();
                //float startinner = System.currentTimeMillis();
                for (int j = 0; j < intArr.length; j++) {
                    float intensity = intArr[j];
                    float currentRT = RTArray[j+startRT];
                    newSeries.getData().add(new XYChart.Data(currentRT, intensity / maxIntensity));

                }
                //float endinner = System.currentTimeMillis();
                //System.out.println("Inner loop norm: " + (endinner-startinner));
                linechart.getData().add(newSeries);
                linechart.applyCss();
                if (currentfile.isselected()) {
                    paintselectedLine(newSeries.getNode());
                }else {
                ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor()); 
                }
                ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());
                linechart.setCreateSymbols(false);
                linechart.setMaxSize(450, 300);

                //float endouter = System.currentTimeMillis();
                //System.out.println("Outer loop norm: " + (endouter-startouter));
//set Range
             generateShiftmarker(adduct,currentfile,linechart);
             generatePeakmarker(adduct, currentfile, linechart);
            }}
            
        }}}
        float lower = adduct.getMinRT();
        float upper = adduct.getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        linechart.setAnimated(false);
//        areachart.setCache(true);
//        areachart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }
    
     public AreaChart generateNormalizedEICArea(Entry adduct) throws InterruptedException {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity (normalized)");
        AreaChart<Number, Number> areachart = new AreaChart(xAxis, yAxis);
        
        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
            adductcontroller.getAdducttochart().get(adduct).add(areachart);
        } else {
            ArrayList list = new ArrayList();
            list.add(areachart);
            adductcontroller.getAdducttochart().put(adduct, list);
        }

        // for all slices (= for all files)
        //float startouter = System.currentTimeMillis();
         for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                float[] RTArray=currentfile.getRTArray();
                if(adduct.getListofSlices().containsKey(currentfile)) {
                Slice currentSlice = adduct.getListofSlices().get(currentfile);

                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, areachart);
                adductcontroller.getAlignableseries().put(newSeries,adduct);
 
                //add Series to HashMaps
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList list = new ArrayList();
                list.add(newSeries);
                adductcontroller.getFiletoseries().put(currentfile, list);
                        }
                float[]intArr = currentSlice.getIntArray();
                int startRT = currentSlice.getRTstart();
                float maxIntensity = currentSlice.getMaxIntensity();
                //float startinner = System.currentTimeMillis();
                List<XYChart.Data> points = new ArrayList<>(intArr.length);
                for (int j = 0; j < intArr.length; j++) {
                    float intensity = intArr[j];
                    float currentRT = RTArray[j+startRT];
                    points.add(new XYChart.Data(currentRT, intensity / maxIntensity));

                }
                newSeries.getData().addAll(points);
                //float endinner = System.currentTimeMillis();
                //System.out.println("Inner loop norm: " + (endinner-startinner));
                areachart.getData().add(newSeries);
                File file = new File("C:\\Users\\stefankoch\\Documents\\NetBeansProjects\\JavaFXTable\\src\\main\\java\\com\\mycompany\\fxmltableview\\gui\\stylesheet2.css");
        areachart.getStylesheets().clear();
        areachart.getStylesheets().add("file:///" + file.getAbsolutePath().replace("\\", "/"));
                
                areachart.applyCss();
                if (currentfile.isselected()) {
                    paintselectedLine(((Group) newSeries.getNode()).getChildren().get(1));
                    
                }else {
                    
                ObservableList<Node> list = ((Group) newSeries.getNode()).getChildren();
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStroke(currentSlice.getFile().getColor());
                double[] color = new double[] {currentSlice.getFile().getColor().getRed(),currentSlice.getFile().getColor().getGreen(),currentSlice.getFile().getColor().getBlue()};
                color = brightencolor(color);
                
                }
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setVisible(false);
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStrokeWidth(currentSlice.getFile().getWidth());

//                areachart.setCreateSymbols(false);
                areachart.setMaxSize(450, 300);

                //float endouter = System.currentTimeMillis();
                //System.out.println("Outer loop norm: " + (endouter-startouter));
//set Range
             generateShiftmarkerArea(adduct,currentfile,areachart);
             generatePeakmarkerArea(adduct, currentfile, areachart);
            }}
            
        }}}
        float lower = adduct.getMinRT();
        float upper = adduct.getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 6);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        areachart.setAnimated(false);
//        areachart.setCache(true);
//        areachart.setCacheHint(CacheHint.SPEED);
        areachart.setLegendVisible(false);
        return areachart;
    }
//    public LineChart generateNormalizedEICwithPeak(Entry adduct) throws InterruptedException {
//
//        //Basic Chart attributes
//        NumberAxis xAxis = new NumberAxis();
//        NumberAxis yAxis = new NumberAxis();
//        xAxis.setLabel("RT [minutes]");
//        yAxis.setLabel("Intensity (normalized)");
//        LineChart<Number, Number> areachart = new LineChart(xAxis, yAxis);
//
//        // for all slices (= for all files)
//        //float startouter = System.currentTimeMillis();
//         for (int d = 0; d<session.getListofDatasets().size(); d++) {
//                    if (session.getListofDatasets().get(d).getActive()) {
//        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
//            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
//            if (currentfile.getActive().booleanValue()) {
//                if(adduct.getListofSlices().containsKey(currentfile)) {
//                Slice currentSlice = adduct.getListofSlices().get(currentfile);
//
//                XYChart.Series newSeries = new XYChart.Series();
// 
//                //add Series to HashMaps
//                adductcontroller.getSeriestofile().put(newSeries, currentfile);
//                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
//                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
//                } else {
//                ArrayList list = new ArrayList();
//                list.add(newSeries);
//                adductcontroller.getFiletoseries().put(currentfile, list);
//                        }
//                
//                float maxIntensity = Arrays.stream(currentSlice.getIntensityArray()).max().getAsInt();
//                //float startinner = System.currentTimeMillis();
//                for (int j = 0; j < currentSlice.getIntensityArray().length; j++) {
//                    float intensity = currentSlice.getIntensityArray()[j];
//                    float currentRT = currentSlice.getRTArray()[j];
//                    newSeries.getData().add(new XYChart.Data(currentRT, intensity / maxIntensity));
//
//                }
//                //float endinner = System.currentTimeMillis();
//                //System.out.println("Inner loop norm: " + (endinner-startinner));
//                areachart.getData().add(newSeries);
//                areachart.applyCss();
//                if (currentfile.isselected()) {
//                    paintselectedLine(newSeries.getNode());
//                }else {
//                ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor()); 
//                }
//                ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());
//                areachart.setCreateSymbols(false);
//                areachart.setMaxSize(300, 200);
//
//                //float endouter = System.currentTimeMillis();
//                //System.out.println("Outer loop norm: " + (endouter-startouter));
////set Range
//                if (adduct.getAdductFittedShift(currentfile) > 0) {
//                    XYChart.Series newSeries2 = new XYChart.Series();
//                    float[] RTArray = adduct.getRTArray();
//                    newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getAdductFittedShift(currentfile)], 0));
//                    newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getAdductFittedShift(currentfile)], 1));
//                    areachart.getData().add(newSeries2);
//                    areachart.applyCss();
//                    if (currentfile.isselected()) {
//                    paintselectedLine(newSeries2.getNode());
//                }else {
//                ((Path) newSeries2.getNode()).setStroke(currentSlice.getFile().getColor()); 
//                }
//                    ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth());
//                    ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(4d, 4d, 4d, 4d, 4d);
//                    adductcontroller.getSeriestofile().put(newSeries2, currentfile);
//                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries2);
//                    
//                    XYChart.Series newSeries3 = new XYChart.Series();
//                    float[] RTArray3 = adduct.getRTArray();
//                    newSeries3.getData().add(new XYChart.Data(RTArray[adduct.getListofSlices().get(currentfile).getfittedPeakStart()], 1));
//                    newSeries3.getData().add(new XYChart.Data(RTArray[adduct.getListofSlices().get(currentfile).getfittedPeakStart()], 0));
//                     newSeries3.getData().add(new XYChart.Data(RTArray[adduct.getListofSlices().get(currentfile).getfittedPeakEnd()], 0));
//                    newSeries3.getData().add(new XYChart.Data(RTArray[adduct.getListofSlices().get(currentfile).getfittedPeakEnd()], 1));
//                    areachart.getData().add(newSeries3);
//                    areachart.applyCss();
//                    if (currentfile.isselected()) {
//                    paintselectedLine(newSeries3.getNode());
//                }else {
//                ((Path) newSeries3.getNode()).setStroke(currentSlice.getFile().getColor()); 
//                }
//                    ((Path) newSeries3.getNode()).setStrokeWidth(currentfile.getWidth());
//                    ((Path) newSeries3.getNode()).getStrokeDashArray().setAll(0.5d, 6d, 0.5d, 6d);
//                    adductcontroller.getSeriestofile().put(newSeries3, currentfile);
//                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries3);
//
//                } else {
//                    XYChart.Series newSeries2 = new XYChart.Series();
//                    float[] RTArray = adduct.getRTArray();
//                    newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getOGroupFittedShift(currentfile)], 0));
//                    newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getOGroupFittedShift(currentfile)], 1));
//                    areachart.getData().add(newSeries2);
//                    areachart.applyCss();
//                    if (currentfile.isselected()) {
//                    paintselectedLine(newSeries2.getNode());
//                }else {
//                ((Path) newSeries2.getNode()).setStroke(currentSlice.getFile().getColor()); 
//                }
//                    ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth());
//                    ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(1d,15d,1d,15d);
//                    adductcontroller.getSeriestofile().put(newSeries2, currentfile);
//                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries2);
//
//                    
//                }
//            }}
//        }}}
//        float lower = adduct.getMinRT();
//        float upper = adduct.getMaxRT();
//        xAxis.setAutoRanging(false);
//        xAxis.setTickUnit((upper - lower) / 6);
//        xAxis.setLowerBound(lower);
//        xAxis.setUpperBound(upper);
//
//        yAxis.setAutoRanging(false);
//        yAxis.setLowerBound(0);
//        yAxis.setUpperBound(1);
//        areachart.setAnimated(false);
////        areachart.setCache(true);
////        areachart.setCacheHint(CacheHint.SPEED);
//        areachart.setLegendVisible(false);
//        return areachart;
//    }


//    public LineChart generateNormalizedEICwithProp(Entry adduct) throws InterruptedException {
//
//        //Basic Chart attributes
//        NumberAxis xAxis = new NumberAxis();
//        NumberAxis yAxis = new NumberAxis();
//        xAxis.setLabel("RT [minutes]");
//        yAxis.setLabel("Intensity (normalized)");
//        LineChart<Number, Number> areachart = new LineChart(xAxis, yAxis);
//
//        // for all slices (= for all files)
//        //float startouter = System.currentTimeMillis();
//         for (int d = 0; d<session.getListofDatasets().size(); d++) {
//                    if (session.getListofDatasets().get(d).getActive()) {
//        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
//            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
//            if (currentfile.getActive().booleanValue()) {
//                if(adduct.getListofSlices().containsKey(currentfile)) {
//                Slice currentSlice = adduct.getListofSlices().get(currentfile);
//
//                XYChart.Series newSeries = new XYChart.Series();
//                
//                 //add Series to HashMaps
//                adductcontroller.getSeriestofile().put(newSeries, currentfile);
//                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
//                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
//                } else {
//                ArrayList list = new ArrayList();
//                list.add(newSeries);
//                adductcontroller.getFiletoseries().put(currentfile, list);
//                        }
//
//                float maxIntensity = Arrays.stream(currentSlice.getIntensityArray()).max().getAsInt();
//                //float startinner = System.currentTimeMillis();
//                for (int j = 0; j < currentSlice.getIntensityArray().length; j++) {
//                    float intensity = currentSlice.getIntensityArray()[j];
//                    float currentRT = currentSlice.getRTArray()[j];
//                    newSeries.getData().add(new XYChart.Data(currentRT, intensity / maxIntensity));
//
//                }
//                //float endinner = System.currentTimeMillis();
//                //System.out.println("Inner loop norm: " + (endinner-startinner));
//                areachart.getData().add(newSeries);
//                areachart.applyCss();
//                if (currentfile.isselected()) {
//                    paintselectedLine(newSeries.getNode());
//                }else {
//                ((Path) newSeries.getNode()).setStroke(currentSlice.getFile().getColor()); 
//                }
//                ((Path) newSeries.getNode()).setStrokeWidth(currentSlice.getFile().getWidth());
//                areachart.setCreateSymbols(false);
//                areachart.setMaxSize(300, 200);
//
//            }}
//        }}}
//
//        //float endouter = System.currentTimeMillis();
//        //System.out.println("Outer loop norm: " + (endouter-startouter));
////set Range
//        float lower = adduct.getMinRT();
//        float upper = adduct.getMaxRT();
//        xAxis.setAutoRanging(false);
//        xAxis.setTickUnit((upper - lower) / 6);
//        xAxis.setLowerBound(lower);
//        xAxis.setUpperBound(upper);
//
//        areachart.setAnimated(false);
////        areachart.setCache(true);
////        areachart.setCacheHint(CacheHint.SPEED);
//        areachart.setLegendVisible(false);
//        yAxis.setAutoRanging(false);
//        yAxis.setLowerBound(0);
//        yAxis.setUpperBound(PropArray(adduct, areachart));
//        return areachart;
//    }

    public ScatterChart generateMassChart(Entry adduct) throws InterruptedException {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("ppm M/Z deviation");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);
        
        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
            adductcontroller.getAdducttochart().get(adduct).add(scatterchart);
        } else {
            ArrayList list = new ArrayList();
            list.add(scatterchart);
            adductcontroller.getAdducttochart().put(adduct, list);
        }

        // for all slices (= for all files)
        //float startouter = System.currentTimeMillis();
         for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                float[] RTArray = currentfile.getRTArray();
                if(adduct.getListofSlices().containsKey(currentfile)) {
                Slice currentSlice = adduct.getListofSlices().get(currentfile);

                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, scatterchart);
                adductcontroller.getAlignableseries().put(newSeries,adduct);
                
                
                 //add Series to HashMaps
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList list = new ArrayList();
                list.add(newSeries);
                adductcontroller.getFiletoseries().put(currentfile, list);
                        }
                
                float maxIntensity = currentSlice.getMaxIntensity();
                float[] intArr = currentSlice.getIntArray();
                float[] mzArr = currentSlice.getMZArray();
                int RTstart = currentSlice.getRTstart();
                float width = currentSlice.getFile().getWidth() + 1.5f;
//float startinner = System.currentTimeMillis();
List<XYChart.Data> points = new ArrayList<>(intArr.length);
                for (int j = 0; j < intArr.length; j++) {
                    XYChart.Data data = new XYChart.Data(RTArray[j+RTstart], getppm(adduct.getMZ(), mzArr[j]));

                    //rect is the node of the plot
                    Rectangle rect1 = new Rectangle(width, width);
                    
                    
                    if (currentfile.isselected()) {
                    paintselectedScatter(rect1);
                }else {
                rect1.setFill(currentSlice.getFile().getColor());
                }
                    

                    //set rect as node
                    data.setNode(rect1);

                    //set opacity
                    data.getNode().setOpacity(intArr[j] / maxIntensity);

                    //set Tooltip
                    //Tooltip tooltip = new Tooltip();
                    //tooltip.setText(currentSlice.getIntensityList().get(j).toString());
                    //Tooltip.install(data.getNode(), tooltip);
                    points.add(data);
                }
                newSeries.getData().addAll(points);

                //float endinner = System.currentTimeMillis();
//System.out.println("Inner loop mass: " + (endinner-startinner));
                scatterchart.getData().add(newSeries);
                scatterchart.setMaxSize(450, 300);

                //set Range
            }}
        }}}
        //float endouter = System.currentTimeMillis();
        //System.out.println("Outer loop mass: " + (endouter-startouter));
        float lower = adduct.getMinRT();
        float upper = adduct.getMaxRT();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit((upper - lower) / 7);
        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        yAxis.setAutoRanging(false);
        yAxis.setTickUnit((adduct.getSession().getMZTolerance() * 2 / 5));
        yAxis.setLowerBound(-1 * adduct.getSession().getMZTolerance());
        yAxis.setUpperBound(adduct.getSession().getMZTolerance());
        scatterchart.setAnimated(false);
//        scatterchart.setCache(true);
//        scatterchart.setCacheHint(CacheHint.SPEED);
        scatterchart.setLegendVisible(false);
        scatterchart.setHorizontalZeroLineVisible(false);

        return scatterchart;

    }

//    public float PropArray(Entry adduct, LineChart<Number, Number> areachart) throws InterruptedException {
//
//        float maxProp = 1;
//         for (int d = 0; d<session.getListofDatasets().size(); d++) {
//                    if (session.getListofDatasets().get(d).getActive()) {
//        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
//            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
//            if (currentfile.getActive().booleanValue()) {
//
//                if (adduct.getOGroupObject().getOGroupPropArraySmooth(currentfile) == null) {
//                    adduct.getOGroupObject().peakpickOGroup(currentfile);
//                }
//
//                float[] PropArray = adduct.getOGroupObject().getOGroupPropArraySmooth(currentfile);
////        
//                List asList = Arrays.asList(ArrayUtils.toObject(PropArray));
//                float max = (float) Collections.max(asList);
//                float[] RTArray = adduct.getRTArray();
//                XYChart.Series newSeries = new XYChart.Series();
//
//                for (int i = 0; i < PropArray.length; i++) {
//                    newSeries.getData().add(new XYChart.Data(RTArray[i], PropArray[i]));
//                    if (maxProp < PropArray[i]) {
//                        maxProp = PropArray[i];
//                    }
//
//                }
//                areachart.getData().add(newSeries);
//                areachart.applyCss();
//                ((Path) newSeries.getNode()).setStroke(Color.ORANGE);
//                ((Path) newSeries.getNode()).setStrokeWidth(1.5);
//
//                float[] PropArray2 = adduct.getAdductPropArray(currentfile);
//
//                List asList2 = Arrays.asList(ArrayUtils.toObject(PropArray2));
//                float max2 = (float) Collections.max(asList2);
//                float[] RTArray2 = adduct.getRTArray();
//                XYChart.Series newSeries3 = new XYChart.Series();
//
//                for (int i = 0; i < PropArray2.length; i++) {
//                    newSeries3.getData().add(new XYChart.Data(RTArray2[i], PropArray2[i]));
//
//                }
//                areachart.getData().add(newSeries3);
//                areachart.applyCss();
//                ((Path) newSeries3.getNode()).setStroke(Color.RED);
//                ((Path) newSeries3.getNode()).setStrokeWidth(1.5);
//
//                if (adduct.getOGroupObject().getOGroupFittedShift(currentfile) > 0) {
//                    XYChart.Series newSeries2 = new XYChart.Series();
//                    newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getOGroupFittedShift(currentfile)], 0));
//                    newSeries2.getData().add(new XYChart.Data(RTArray[adduct.getOGroupObject().getOGroupFittedShift(currentfile)], maxProp));
//                    areachart.getData().add(newSeries2);
//                    areachart.applyCss();
//                    ((Path) newSeries2.getNode()).setStroke(currentfile.getColor());
//                    ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth());
//                    ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(4d, 4d, 4d, 4d, 4d);
//                }
//
//            }
//        }}}
//        return maxProp;
//    }

//    public LineChart generateShiftChart(ObservableList<Entry> list) {
//
//        NumberAxis xAxis = new NumberAxis();
//        NumberAxis yAxis = new NumberAxis();
//        xAxis.setLabel("RT [minutes]");
//        yAxis.setLabel("Shift [seconds]");
//        LineChart<Number, Number> areachart = new LineChart(xAxis, yAxis);
//
//        float upper = 0;
//        float lower = 0;
// for (int d = 0; d<session.getListofDatasets().size(); d++) {
//                    if (session.getListofDatasets().get(d).getActive()) {
//        for (int f = 0; f < list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
//            RawDataFile currentfile = list.get(0).getSession().getListofDatasets().get(d).getListofFiles().get(f);
//            if (currentfile.getActive().booleanValue()) {
//                XYChart.Series newSeries = new XYChart.Series();
//                
//                shiftcontroller.getSeriestofile().put(newSeries, currentfile);
//                if (shiftcontroller.getFiletoseries().containsKey(currentfile)){
//                    shiftcontroller.getFiletoseries().get(currentfile).add(newSeries);
//                } else {
//                ArrayList array = new ArrayList();
//                array.add(newSeries);
//                shiftcontroller.getFiletoseries().put(currentfile, array);
//                        }
//                
//                float shiftiter = (list.get(0).getSession().getRTTolerance() * 2) / list.get(0).getSession().getResolution();
//                int middleint = (list.get(0).getSession().getResolution() / 2) - 1;
//
//                for (int i = 0; i < list.size(); i++) {
//                    float shift = (list.get(i).getOGroupFittedShift(currentfile) - middleint) * shiftiter * 60;
//                    XYChart.Data data = new XYChart.Data(list.get(i).getRT(), shift);
//                    newSeries.getData().add(data);
//                    if (shift > upper) {
//                        upper = shift;
//                    } else if (shift < lower) {
//                        lower = shift;
//                    }
//                }
//                areachart.getData().add(newSeries);
//                areachart.applyCss();
//                if (currentfile.isselected()) {
//                    paintselectedLine(newSeries.getNode());
//                }else{
//                ((Path) newSeries.getNode()).setStroke(currentfile.getColor());
//                }
//                
//                ((Path) newSeries.getNode()).setStrokeWidth(currentfile.getWidth());
//
////TODO: calulate number of active files
//                System.out.println("Charts " + (f + 1) + "of " + list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size() + " drawn");
//            }
//        }}}
//        areachart.setCreateSymbols(false);
//        areachart.setMaxSize(2000, 500);
//        areachart.setLegendVisible(false);
//        yAxis.setAutoRanging(false);
//        yAxis.setLowerBound(lower - 20);
//        yAxis.setUpperBound(upper + 20);
//        return areachart;
//    }

    public float getppm(float massref, float mass) {
        float dif = mass-massref;

        return dif / (massref / 1000000);
    }
    
    

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    public void paintselectedLine(Node node) {
        node.setCursor(Cursor.HAND);
        ((Path) node).setStroke(Color.RED);
        
    }
    
    
    public void paintselectedScatter(Rectangle rect) {
        rect.setFill(Color.RED);
        
    }
    
    public ScatterChart generateScatterShiftChart(ObservableList<Entry> list) {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Shift [seconds]");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);

        float upper = 0;
        float lower = 0;
        float shiftiter = (list.get(0).getSession().getRTTolerance() * 2) / list.get(0).getSession().getResolution();
        int middleint = (list.get(0).getSession().getResolution() / 2) - 1;
        
        
        //draw penalty Area
        XYChart.Series backSeries = new XYChart.Series();
        
        
        List<RawDataFile> sellist =  session.getSelectedFiles();
        List<XYChart.Data> points = new ArrayList<XYChart.Data>();
        //draw background
        for (int i = 0;i<list.size(); i=i+3) {
            for (int j = 0; j< session.getResolution(); j++) {
                boolean penalty = false;
               for (int s = 0; s<sellist.size(); s++) {
                   if (sellist.get(s).getActive()) {
                       if (list.get(i).getPenArray()!=null){
                           if (list.get(i).getPenArray().containsKey(sellist.get(s))) {
                       if (list.get(i).getPenArray().get(sellist.get(s))[j]<0) {
                           penalty = true;
                           break;
                           
                       }
                       }
                       }
                   }
               }
                if (penalty) {
                    XYChart.Data data = new XYChart.Data(list.get(i).getRT(), (j-middleint)*60*shiftiter);
                    Rectangle rect = new Rectangle(5,5);
                    rect.setFill(Color.PINK);
                    data.setNode(rect);
                    points.add(data);
                }
               
            }
        }
        backSeries.getData().addAll(points);
        scatterchart.getData().add(backSeries);
        shiftcontroller.getSeriestofile().put(backSeries, null);
            
            
 
        
 for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = list.get(0).getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                shiftiter=1.0f/currentfile.getScanspersecond();
               
                
                XYChart.Series newSeries = new XYChart.Series();
                
                shiftcontroller.getSeriestofile().put(newSeries, currentfile);
                if (shiftcontroller.getFiletoseries().containsKey(currentfile)){
                    shiftcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList array = new ArrayList();
                array.add(newSeries);
                shiftcontroller.getFiletoseries().put(currentfile, array);
                        }
                
                
                
                

                
                
                for (int i = 1; i < list.size()-1; i++) {
                    
                    float shift = (list.get(i).getOgroupShift().get(currentfile))*60;
                    XYChart.Data data = new XYChart.Data(list.get(i).getRT(), shift);
                    
                    Ellipse cir = new Ellipse(1.5,4);
                    
                    TreeItem<Entry> item = null;
                    for (int e = 0; e<shiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().size(); e++) {
                        if (shiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().get(e).getValue().equals(list.get(i)))  {
                            item = shiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().get(e);
                            break;
                        }
                    }
                    shiftcontroller.getNodetoogroup().put(cir, item);
                    if (currentfile.isselected()) {
                    cir.setFill(Color.RED);
                }else {
                cir.setFill(currentfile.getColor());
                }
                    data.setNode(cir);
                    if (shiftcontroller.getOpacityMode().equals("Peak found")) {
                        data.getNode().setOpacity(list.get(i).getmaxScorepeakfound(currentfile)+0.02); 
                    } else if (shiftcontroller.getOpacityMode().equals("Peak close")) {
                        data.getNode().setOpacity(list.get(i).getminScorepeakclose(currentfile)+0.02); 
                    }
                    
                    
                    
                    
                    newSeries.getData().add(data);
                    if (shift > upper) {
                        upper = shift;
                    } else if (shift < lower) {
                        lower = shift;
                    }
                }
                scatterchart.getData().add(newSeries);
              
                

//TODO: number
                System.out.println("Charts " + (f + 1) + "of " + list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size() + " drawn");
            }
        }}}

        scatterchart.setMaxSize(2000, 2000);
        scatterchart.setLegendVisible(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lower - 30);
        yAxis.setUpperBound(upper + 30);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(session.getStart().floatValue()-5.0f);
        xAxis.setUpperBound(session.getEnd().floatValue()+5.0f);
        return scatterchart;
    }
    
    public ScatterChart generateScatterShiftChartnew(ObservableList<Entry> list) {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Shift [seconds]");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);

        float upper = 0;
        float lower = 0;
  
        
            
 
        
 for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = list.get(0).getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {

                XYChart.Series newSeries = new XYChart.Series();
                
                
                if (newshiftcontroller.getFiletoseries().containsKey(currentfile)){
                    newshiftcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList array = new ArrayList();
                array.add(newSeries);
                newshiftcontroller.getFiletoseries().put(currentfile, array);
                        }
                
                
                
                

                
                List<XYChart.Data> points2 = new ArrayList<>();
                for (int i = 1; i < list.size()-1; i++) {
                    
                    float shift = (list.get(i).getOgroupShift().get(currentfile))*60;
                    XYChart.Data data = new XYChart.Data(list.get(i).getRT(), shift);
                    
                    Ellipse cir = new Ellipse(1.5,2);
                    
                    TreeItem<Entry> item = null;
                    for (int e = 0; e<newshiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().size(); e++) {
                        if (newshiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().get(e).getValue().equals(list.get(i)))  {
                            item = newshiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().get(e);
                            break;
                        }
                    }
                    newshiftcontroller.getNodetoogroup().put(cir, item);
                    if (currentfile.isselected()) {
                    cir.setFill(Color.RED);
                }else {
                cir.setFill(currentfile.getColor());
                }
                    data.setNode(cir);
//                    if (newshiftcontroller.getOpacityMode().equals("Peak found")) {
//                        data.getNode().setOpacity(list.get(i).getmaxScorepeakfound(currentfile)+0.02); 
//                    } else if (newshiftcontroller.getOpacityMode().equals("Peak close")) {
//                        data.getNode().setOpacity(list.get(i).getminScorepeakclose(currentfile)+0.02); 
//                    }
                    
                    
                    
                    
                    points2.add(data);
                    if (shift > upper) {
                        upper = shift;
                    } else if (shift < lower) {
                        lower = shift;
                    }
                }
                newshiftcontroller.getSeriestofile().put(newSeries, currentfile);
                newSeries.getData().addAll(points2);
                scatterchart.getData().add(newSeries);
              
                

//TODO: number
                System.out.println("Charts " + (f + 1) + "of " + list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size() + " drawn");
            }
        }}}

        scatterchart.setMaxSize(2000, 2000);
        scatterchart.setLegendVisible(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lower - 30);
        yAxis.setUpperBound(upper + 30);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(session.getStart().floatValue()-5.0f);
        xAxis.setUpperBound(session.getEnd().floatValue()+5.0f);
        return scatterchart;
    }
    
    //Peak Chart for peak view (context menu in Shiftview)
    public ScatterChart generateScatterPeakChart(ObservableList<Entry> list) {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Shift [seconds]");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);

     
        //float shiftiter = (list.get(0).getSession().getRTTolerance() * 2) / list.get(0).getSession().getResolution();
        //int middleint = (list.get(0).getSession().getResolution() / 2) - 1;
          List<XYChart.Data> points = new ArrayList<XYChart.Data>();
          List<RawDataFile> sellist =  session.getSelectedFiles();
        
        
            
   
        double opacity = 1.0/((double)sellist.size()+1.0);
        
        for (int i = 0; i< sellist.size(); i++) {
            for (int k = 1; k < list.size()-1; k++) {
            float shift = (list.get(k).getOgroupShift().get(sellist.get(i)))*60;
                   XYChart.Data data = new XYChart.Data(list.get(k).getRT(), shift);
                    
                    Ellipse cir = new Ellipse(1.5,4);
                    cir.setFill(Color.ANTIQUEWHITE);
                    data.setNode(cir);
                    points.add(data);
            }
        }
        
        for (int i = 0; i< sellist.size(); i++) {
           
            Slice[] slices = sellist.get(i).getListofSlices();
            for (int j = 0; j< slices.length; j++) {
                if (slices[j]!=null&&slices[j].getListofPeaks()!=null) {
                    int middle = (slices[j].getRTend()-slices[j].getRTstart())/2;
                for (int p = 0; p<slices[j].getListofPeaks().size(); p++) {
                    XYChart.Data data = new XYChart.Data(slices[j].getRT(), slices[j].getListofPeaks().get(p).getIndexshift()*60);
                    Rectangle rect = new Rectangle(1.5,1.5);
                    rect.setFill(Color.BLACK);
                    rect.setOpacity(opacity);
                    data.setNode(rect);
                    points.add(data);
                    
                }
                
                }
                    
            }
            

        }
        XYChart.Series peakSeries = new XYChart.Series();
        peakSeries.getData().addAll(points);
        scatterchart.getData().add(peakSeries);
        
        

        scatterchart.setMaxSize(2000, 2000);
        scatterchart.setLegendVisible(false);
//        yAxis.setAutoRanging(false);
//        yAxis.setLowerBound(lower - 90);
//        yAxis.setUpperBound(upper + 90);
        return scatterchart;
    }
    
    void generateShiftmarker(Entry adduct, RawDataFile currentfile, LineChart linechart) {
        if (adduct.getOGroupObject().getOgroupShift().containsKey(currentfile)) {
        
          if (adduct.getListofSlices().get(currentfile).getFittedpeak()!=null) {
                    XYChart.Series newSeries2 = new XYChart.Series();
                    adductcontroller.getSeriestochart().put(newSeries2, linechart);
                    float[] RTArray = currentfile.getRTArray();
                    float RT = RTArray[adduct.getListofSlices().get(currentfile).getListofPeaks().get(adduct.getListofSlices().get(currentfile).getFittedpeak()).getIndex()+adduct.getListofSlices().get(currentfile).getRTstart()];
                    newSeries2.getData().add(new XYChart.Data(RT, 0));
                    newSeries2.getData().add(new XYChart.Data(RT, 1));
                    linechart.getData().add(newSeries2);
                    linechart.applyCss();
                    if (currentfile.isselected()) {
                    paintselectedLine(newSeries2.getNode());
                }else {
                ((Path) newSeries2.getNode()).setStroke(currentfile.getColor()); 
                }
                    ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth());
                    ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(4d, 4d, 4d, 4d, 4d);
                    adductcontroller.getSeriestofile().put(newSeries2, currentfile);
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries2);

                } else {
                    XYChart.Series newSeries2 = new XYChart.Series();
                    adductcontroller.getSeriestochart().put(newSeries2, linechart);
                    //float[] RTArray = currentfile.getRTArray();
                    float RT = adduct.getOGroupObject().getOgroupShift().get(currentfile)+adduct.getOGroupObject().getRT();
                    newSeries2.getData().add(new XYChart.Data(RT, 0));
                    newSeries2.getData().add(new XYChart.Data(RT, 1));
                    linechart.getData().add(newSeries2);
                    linechart.applyCss();
                    if (currentfile.isselected()) {
                    paintselectedLine(newSeries2.getNode());
                }else {
                ((Path) newSeries2.getNode()).setStroke(currentfile.getColor()); 
                }
                    ((Path) newSeries2.getNode()).setStrokeWidth(currentfile.getWidth());
                    ((Path) newSeries2.getNode()).getStrokeDashArray().setAll(1d,15d,1d,15d);
                    adductcontroller.getSeriestofile().put(newSeries2, currentfile);
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries2);

                    
                }
          
        }
        
    }
    void generateShiftmarkerArea(Entry adduct, RawDataFile currentfile, AreaChart areachart) {
        
        float width = session.getRTTolerance()*(1.0f/30.0f);
        
        
        if (adduct.getOGroupObject().getOgroupShift().containsKey(currentfile)) {
        
          if (adduct.getListofSlices().get(currentfile).getFittedpeak()!=null) {
                    XYChart.Series newSeries2 = new XYChart.Series();
                    adductcontroller.getSeriestochart().put(newSeries2, areachart);
                    adductcontroller.getAlignableseries().put(newSeries2, adduct);
                    float[] RTArray = currentfile.getRTArray();
                    float RT = RTArray[adduct.getListofSlices().get(currentfile).getListofPeaks().get(adduct.getListofSlices().get(currentfile).getFittedpeak()).getIndex()+adduct.getListofSlices().get(currentfile).getRTstart()];
                    newSeries2.getData().add(new XYChart.Data(RT-width, 0));
                    newSeries2.getData().add(new XYChart.Data(RT, -0.05));
                    newSeries2.getData().add(new XYChart.Data(RT, 1));
                    newSeries2.getData().add(new XYChart.Data(RT, -0.05));
                    newSeries2.getData().add(new XYChart.Data(RT+width, 0));
                    areachart.getData().add(newSeries2);
                    areachart.applyCss();
                    if (currentfile.isselected()) {
                     paintselectedLine(((Group) newSeries2.getNode()).getChildren().get(1));
                     ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).setFill(Color.RED);
                }else {
                ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).setStroke(currentfile.getColor()); 
                }
                    ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).setStrokeWidth(currentfile.getWidth());
                    ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).setFill(currentfile.getColor());
                    adductcontroller.getSeriestofile().put(newSeries2, currentfile);
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries2);

                } else {
                    XYChart.Series newSeries2 = new XYChart.Series();
                    adductcontroller.getSeriestochart().put(newSeries2, areachart);
                    adductcontroller.getAlignableseries().put(newSeries2, adduct);
                    //float[] RTArray = currentfile.getRTArray();
                    float RT = adduct.getOGroupObject().getOgroupShift().get(currentfile)+adduct.getOGroupObject().getRT();
                    newSeries2.getData().add(new XYChart.Data(RT, -0.05));
                    newSeries2.getData().add(new XYChart.Data(RT, 1));
                    areachart.getData().add(newSeries2);
                    areachart.applyCss();
                    if (currentfile.isselected()) {
                    paintselectedLine(((Group) newSeries2.getNode()).getChildren().get(1));
                }else {
                ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).setStroke(currentfile.getColor()); 
                }
                    ((Group) newSeries2.getNode()).getChildren().get(0).setVisible(false);
                    ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).setStrokeWidth(currentfile.getWidth());
                    ((Path) ((Group) newSeries2.getNode()).getChildren().get(1)).getStrokeDashArray().setAll(1d,15d,1d,15d);
                    adductcontroller.getSeriestofile().put(newSeries2, currentfile);
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries2);

                    
                }
          
        }
        
    }
    
    void generatePeakmarker(Entry adduct, RawDataFile currentfile, LineChart linechart) {
        
        List<Peak> list = adduct.getListofSlices().get(currentfile).getListofPeaks();
        if (list!= null) {
            for (int i = 0; i< list.size(); i++) {
                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, linechart);
                float[] RTArray = currentfile.getRTArray();
                int start = list.get(i).getStart()+list.get(i).getSlice().getRTstart();
                int index = list.get(i).getIndex()+list.get(i).getSlice().getRTstart();
                int end = list.get(i).getEnd()+list.get(i).getSlice().getRTstart();
                newSeries.getData().add(new XYChart.Data(RTArray[start], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[start], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[start], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[index], 1.05));
                newSeries.getData().add(new XYChart.Data(RTArray[index], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[index], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[index], 1.05));
                newSeries.getData().add(new XYChart.Data(RTArray[end], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[end], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[end], 1.2));
              
                linechart.getData().add(newSeries);
                adductcontroller.getSeriestopeak().put(newSeries, list.get(i));
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                linechart.applyCss();
                if (currentfile.isselected()) {
                    paintselectedLine(newSeries.getNode());
                }else {
                ((Path) newSeries.getNode()).setStroke(currentfile.getColor()); 
                }
                ((Path) newSeries.getNode()).setStrokeWidth(currentfile.getWidth());
                //((Path) newSeries.getNode()).getStrokeDashArray().setAll(0.5d, 4d);
            } 
             
                   
                    
                    
            
        }
       
        
        
        
    }
    
    //generates peak marker
        void generatePeakmarkerArea(Entry adduct, RawDataFile currentfile, AreaChart areachart) {
        Slice currentSlice = adduct.getListofSlices().get(currentfile);
        List<Peak> list = currentSlice.getListofPeaks();
        float[] RTArray = currentfile.getRTArray();
        float[] IntArray = currentSlice.getIntArray();
        int offset = currentSlice.getRTstart();
        if (list!= null) {
           //for all peaks
            for (int i = 0; i< list.size(); i++) {
                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, areachart);
                adductcontroller.getAlignableseries().put(newSeries, adduct);
                Peak peak = list.get(i);
                 //get Data Points
                for ( int j = peak.getStart(); j<=peak.getIndex(); j++) {
                    newSeries.getData().add(new XYChart.Data(RTArray[j+offset], IntArray[j]/currentSlice.getMaxIntensity()));
                }
                newSeries.getData().add(new XYChart.Data(RTArray[peak.getIndex()+offset], 0));
                for ( int j = peak.getIndex(); j<=peak.getEnd(); j++) {
                    newSeries.getData().add(new XYChart.Data(RTArray[j+offset], IntArray[j]/currentSlice.getMaxIntensity()));
                }
              
                areachart.getData().add(newSeries);
                adductcontroller.getSeriestopeak().put(newSeries, list.get(i));
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                areachart.applyCss();
                
                if (currentfile.isselected()) {
                     paintselectedLine(((Group) newSeries.getNode()).getChildren().get(0));
                    ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(Color.RED.deriveColor(1, 1, 1, 0.15));
                }else {
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStroke(currentSlice.getFile().getColor());
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setStroke(currentSlice.getFile().getColor());
                //double[] color = new double[] {currentSlice.getFile().getColor().getRed(),currentSlice.getFile().getColor().getGreen(),currentSlice.getFile().getColor().getBlue()};
                //color = brightencolor(color);
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(currentSlice.getFile().getColor().deriveColor(1, 1, 1, 0.15));
                }
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setVisible(false);
                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStrokeWidth(currentSlice.getFile().getWidth());
                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setStrokeWidth(currentSlice.getFile().getWidth());
                
                
               // System.out.println("Num: " + list.get(i).getSlice().getAdduct().getNum() + " SNR: " + peak.getSNR());
                //System.out.println("Num: " + list.get(i).getSlice().getAdduct().getNum() + " NU: " + peak.getNoiseUnits());
                
            } 

        }
 
    }
    
    
     public LineChart generateShiftMap(Entry adduct) throws InterruptedException {

        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Shift [seconds]");
        LineChart<Number, Number> linechart = new LineChart(xAxis, yAxis);
        float upper = 0;
        float lower = 0;
        
        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
            adductcontroller.getAdducttochart().get(adduct).add(linechart);
        } else {
            ArrayList list = new ArrayList();
            list.add(linechart);
            adductcontroller.getAdducttochart().put(adduct, list);
        }
        
        

        //float startouter = System.currentTimeMillis();
        // for all slices (= for all files)
        for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                
                XYChart.Series newSeries = new XYChart.Series();
                adductcontroller.getSeriestochart().put(newSeries, linechart);
                
                //add Series to HashMaps
                adductcontroller.getSeriestofile().put(newSeries, currentfile);
                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
                } else {
                ArrayList list = new ArrayList();
                list.add(newSeries);
                adductcontroller.getFiletoseries().put(currentfile, list);
                        }

                //while the next RT is the same as the one before, add Intensities
                //float startinner = System.currentTimeMillis();
                float shiftiter = (session.getRTTolerance() * 2) / session.getResolution();
                int middleint = (session.getResolution() / 2) - 1;
                
                List<XYChart.Data> points = new ArrayList<>();
                for (int j = 0; j < session.getListofOGroups().size(); j++) {
                  float shift = (session.getListofOGroups().get(j).getOgroupShift().get(currentfile))*60;
                    XYChart.Data data = new XYChart.Data(session.getListofOGroups().get(j).getRT(), shift);
                    points.add(data);
                    if (shift > upper) {
                        upper = shift;
                    } else if (shift < lower) {
                        lower = shift;
                    }
                   

                }
                newSeries.getData().addAll(points);
                
                 XYChart.Series shiftSeries = new XYChart.Series();
                 XYChart.Data data = new XYChart.Data(adduct.getRT(), -100);
                 shiftSeries.getData().add(data);
                 XYChart.Data data2 = new XYChart.Data(adduct.getRT(), 100);
                shiftSeries.getData().add(data2);
                
                // add new Series
                linechart.getData().add(newSeries);
                linechart.getData().add(shiftSeries);
                //apply Css to create nodes
                linechart.applyCss();
                //cast to path to be able to set stroke
                if (currentfile.isselected()) {
                    paintselectedLine(newSeries.getNode());
                }else {
                ((Path) newSeries.getNode()).setStroke(currentfile.getColor()); 
                }
                
                ((Path) newSeries.getNode()).setStrokeWidth(currentfile.getWidth());
                ((Path) shiftSeries.getNode()).setStrokeWidth(1.0);
                ((Path) shiftSeries.getNode()).setStroke(Color.BLACK); 
                ((Path) shiftSeries.getNode()).getStrokeDashArray().setAll(0.5d, 6d, 0.5d, 6d);


            }
        }}}
        
        File file = new File("C:\\Users\\stefankoch\\Documents\\NetBeansProjects\\JavaFXTable\\src\\main\\java\\com\\mycompany\\fxmltableview\\gui\\stylesheet2.css");
        linechart.getStylesheets().clear();
        linechart.getStylesheets().add("file:///" + file.getAbsolutePath().replace("\\", "/"));

        //set size of chart
        linechart.setMaxSize(450, 300);

        
        //set Range
       
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lower - 20);
        yAxis.setUpperBound(upper + 20);

        linechart.setAnimated(false);
//        areachart.setCache(true);
//        areachart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        return linechart;
    }
    
     public double[] brightencolor(double[] color) {
         boolean black = true;
         for (int i = 0; i<3; i++) {
             if (color[i]>0){
             color[i] = 1-((1-color[i])/2);
             black = false;
             }
         }
         if (black) {
             color[0]= 0.5;
             color[1]=0.5;
             color[2] = 0.5;
         }
         return color;
     }
     
     
     public AreaChart generateNewShift(ObservableList<Entry> list) throws InterruptedException {
//Info:
//newSeries.getNode() is Group, consisting of 2 Paths, 0 is a simple line, as in linechart, 1 is the line plus the area, color is a pale version of line
//1 can be set invisible, then it works like a normal line chart
          
          
        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("RT-Shift [sec]");
        AreaChart<Number, Number> linechart = new AreaChart(xAxis, yAxis);
        XYChart.Series topSeries = new XYChart.Series();
        linechart.getData().add(topSeries);
        XYChart.Series midSeries = new XYChart.Series();
        linechart.getData().add(midSeries);
        XYChart.Series botSeries = new XYChart.Series();
        linechart.getData().add(botSeries);
        List<XYChart.Data> list1 = new ArrayList<>();
        List<XYChart.Data> list2 = new ArrayList<>();
        List<XYChart.Data> list3 = new ArrayList<>();
        
        for (int i = 0; i<list.size(); i++) {
            XYChart.Data data1 = new XYChart.Data(list.get(i).getRT(), 100);
            list1.add(data1);
            newshiftcontroller.setTopSeries(topSeries);
            XYChart.Data data3 = new XYChart.Data(list.get(i).getRT(), 0);
           list3.add(data3);
            newshiftcontroller.setBotSeries(botSeries);
            XYChart.Data data2 = new XYChart.Data(list.get(i).getRT(), 0.0f);
            list2.add(data2);
            newshiftcontroller.setMidSeries(midSeries);
        }
        topSeries.getData().addAll(list1);
        botSeries.getData().addAll(list3);
        midSeries.getData().addAll(list2);
         newshiftcontroller.getSeriestofile().put(topSeries, null);
         newshiftcontroller.getSeriestofile().put(midSeries, null);
         newshiftcontroller.getSeriestofile().put(botSeries, null);
         
        linechart.applyCss();
        //Color Top Series
        ((Group) topSeries.getNode()).getChildren().get(1).setVisible(false);
        ((Group) topSeries.getNode()).getChildren().get(0).setVisible(true);
        ((Path)((Group) topSeries.getNode()).getChildren().get(0)).setStroke(Color.TRANSPARENT);
        ((Path)((Group) topSeries.getNode()).getChildren().get(0)).setFill(Color.LIGHTSALMON);
        
        //Color Middle Series
        ((Group) midSeries.getNode()).getChildren().get(0).setVisible(false);
        ((Path)((Group) midSeries.getNode()).getChildren().get(1)).setStroke(Color.RED);
        
        //Color Bottom Series
        ((Group) botSeries.getNode()).getChildren().get(1).setVisible(false);
         ((Group) botSeries.getNode()).getChildren().get(0).setVisible(true);
        ((Path)((Group) botSeries.getNode()).getChildren().get(0)).setStroke(Color.TRANSPARENT);
        ((Path)((Group) botSeries.getNode()).getChildren().get(0)).setFill(Color.color(0.95, 0.95, 0.95));
        
//                    ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(Color.color(1,0.5,0.5));
        
        
        
//        if (adductcontroller.getAdducttochart().containsKey(adduct)) {
//            adductcontroller.getAdducttochart().get(adduct).add(linechart);
//        } else {
//            ArrayList list = new ArrayList();
//            list.add(linechart);
//            adductcontroller.getAdducttochart().put(adduct, list);
//        }
//        
        

        //float startouter = System.currentTimeMillis();
        // for all slices (= for all files)
//        for (int d = 0; d<session.getListofDatasets().size(); d++) {
//                    if (session.getListofDatasets().get(d).getActive()) {
//        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
//            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
//            if (currentfile.getActive().booleanValue()) {
//                float[] RTArray=currentfile.getRTArray();
//                if(adduct.getListofSlices().containsKey(currentfile)) {
//                Slice currentSlice = adduct.getListofSlices().get(currentfile);
//                if (currentSlice.isStored()) {
//                     System.out.println("Adding Read Slice from ChartGenerator");
//                    session.getIothread().readslice(currentSlice);
//                    while (currentSlice.isStored()) {
//                        Thread.sleep(10);
//                    }
//                }
//                XYChart.Series newSeries = new XYChart.Series();
//                adductcontroller.getSeriestochart().put(newSeries, linechart);
//                
//                //add Series to HashMaps
//                adductcontroller.getSeriestofile().put(newSeries, currentfile);
//                if (adductcontroller.getFiletoseries().containsKey(currentfile)){
//                    adductcontroller.getFiletoseries().get(currentfile).add(newSeries);
//                } else {
//                ArrayList list = new ArrayList();
//                list.add(newSeries);
//                adductcontroller.getFiletoseries().put(currentfile, list);
//                        }
//
//                //while the next RT is the same as the one before, add Intensities
//                //float startinner = System.currentTimeMillis();
//                float[]intArr = currentSlice.getIntArray();
//                int startRT = currentSlice.getRTstart();
//                
//                for (int j = 0; j < intArr.length; j++) {
//                    float intensity = intArr[j];
//                    float currentRT = RTArray[startRT+j];
//                    XYChart.Data data = new XYChart.Data(currentRT, intensity);
//
//                    newSeries.getData().add(data);
//
//                }
//
//                // add new Series
//                linechart.getData().add(newSeries);
//                //apply Css to create nodes
//                linechart.applyCss();
//                //cast to path to be able to set stroke
//                if (currentfile.isselected()) {
//                    paintselectedLine(((Group) newSeries.getNode()).getChildren().get(0));
//                    ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(Color.color(1,0.5,0.5));
//                    
//                }else {
//                    
//                ObservableList<Node> list = ((Group) newSeries.getNode()).getChildren();
//                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStroke(currentSlice.getFile().getColor());
//                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setStroke(currentSlice.getFile().getColor());
//                double[] color = new double[] {currentSlice.getFile().getColor().getRed(),currentSlice.getFile().getColor().getGreen(),currentSlice.getFile().getColor().getBlue()};
//                color = brightencolor(color);
//                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setFill(Color.color(color[0],color[1],color[2]));
//                }
//                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setVisible(false);
//                ((Path)((Group) newSeries.getNode()).getChildren().get(1)).setStrokeWidth(currentSlice.getFile().getWidth());
//                ((Path)((Group) newSeries.getNode()).getChildren().get(0)).setStrokeWidth(currentSlice.getFile().getWidth());
//
//
//            }}
//        }}}

        //don't draw symbols
        //linechart.setCreateSymbols(false);
        File file = new File("C:\\Users\\stefankoch\\Documents\\NetBeansProjects\\JavaFXTable\\src\\main\\java\\com\\mycompany\\fxmltableview\\gui\\stylesheet2.css");
        linechart.getStylesheets().clear();
        linechart.getStylesheets().add("file:///" + file.getAbsolutePath().replace("\\", "/"));
        //set size of chart
        linechart.setMaxSize(2000, 2000);

//        float lower = adduct.getMinRT();
//        float upper = adduct.getMaxRT();
//        //set Range
//        xAxis.setAutoRanging(false);
//        xAxis.setTickUnit((upper - lower) / 6);
//        xAxis.setLowerBound(lower);
//        xAxis.setUpperBound(upper);

        linechart.setAnimated(false);
        linechart.setHorizontalGridLinesVisible(false);
        linechart.setVerticalGridLinesVisible(false);
//        areachart.setCache(true);
//        areachart.setCacheHint(CacheHint.SPEED);
        linechart.setLegendVisible(false);
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(40);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(session.getResolution());
        yAxis.setOpacity(0);
        xAxis.setOpacity(0);
        return linechart;
    } 
     
     public ScatterChart generateNewPeak(ObservableList<Entry> list) {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("RT-Shift [sec]");
        ScatterChart<Number, Number> scatterchart = new ScatterChart(xAxis, yAxis);
        XYChart.Series backSeries = new XYChart.Series();
        RawDataFile file = session.getSelectedFiles().get(0);
        List<XYChart.Data> points = new ArrayList<>();
        for (int i = 0; i<list.size(); i++) {
            for (int j = 0; j<list.get(i).getListofAdducts().size(); j++) {
                Entry adduct = list.get(i).getListofAdducts().get(j);
                if (adduct.getListofSlices().containsKey(file)) {
                    Slice slice = adduct.getListofSlices().get(file);
                    if (slice.getListofPeaks()!=null){
                    for (int p = 0; p<slice.getListofPeaks().size(); p++) {
                         XYChart.Data data = new XYChart.Data(list.get(i).getRT(),slice.getListofPeaks().get(p).getIndexshift()*60);
                         float weight = slice.getListofPeaks().get(p).getWeight();
                         //get size, max 5
                         
                         
                          Rectangle rect;
                          
                          if (weight>1) {
                              rect = new Rectangle(5,5);
                              rect.setFill(Color.GREEN);
                          }else if (weight<0) {
                              rect = new Rectangle(5,5);
                              rect.setFill(Color.RED);
                          } else {
                              rect = new Rectangle(1.5,1.5);
                              rect.setFill(Color.BLACK);
                              rect.setOpacity(0.5);
                          }
                    
                    data.setNode(rect);
                         points.add(data);
                    }
                    }
                }
            }
        }
        backSeries.getData().addAll(points);
        scatterchart.getData().add(backSeries);
        newshiftcontroller.getSeriestofile().put(backSeries, null);
//        float upper = 0;
//        float lower = 0;
//        float shiftiter = (list.get(0).getSession().getRTTolerance() * 2) / list.get(0).getSession().getResolution();
//        int middleint = (list.get(0).getSession().getResolution() / 2) - 1;
//        
//        
//        //draw penalty Area
//        XYChart.Series backSeries = new XYChart.Series();
//        
//        
//        List<RawDataFile> sellist =  session.getSelectedFiles();
//        List<XYChart.Data> points = new ArrayList<XYChart.Data>();
//        //draw background
//        for (int i = 0;i<list.size(); i=i+3) {
//            for (int j = 0; j< session.getResolution(); j++) {
//                boolean penalty = false;
//               for (int s = 0; s<sellist.size(); s++) {
//                   if (sellist.get(s).getActive()) {
//                       if (list.get(i).getPenArray()!=null){
//                           if (list.get(i).getPenArray().containsKey(sellist.get(s))) {
//                       if (list.get(i).getPenArray().get(sellist.get(s))[j]<0) {
//                           penalty = true;
//                           break;
//                           
//                       }
//                       }
//                       }
//                   }
//               }
//                if (penalty) {
//                    XYChart.Data data = new XYChart.Data(list.get(i).getRT(), (j-middleint)*60*shiftiter);
//                    Rectangle rect = new Rectangle(5,5);
//                    rect.setFill(Color.PINK);
//                    data.setNode(rect);
//                    points.add(data);
//                }
//               
//            }
//        }
//        backSeries.getData().addAll(points);
//        scatterchart.getData().add(backSeries);
//            
//            
//            
// 
//        
// for (int d = 0; d<session.getListofDatasets().size(); d++) {
//                    if (session.getListofDatasets().get(d).getActive()) {
//        for (int f = 0; f < list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
//            RawDataFile currentfile = list.get(0).getSession().getListofDatasets().get(d).getListofFiles().get(f);
//            if (currentfile.getActive().booleanValue()) {
//                shiftiter=1.0f/currentfile.getScanspersecond();
//               
//                
//                XYChart.Series newSeries = new XYChart.Series();
//                
//                shiftcontroller.getSeriestofile().put(newSeries, currentfile);
//                if (shiftcontroller.getFiletoseries().containsKey(currentfile)){
//                    shiftcontroller.getFiletoseries().get(currentfile).add(newSeries);
//                } else {
//                ArrayList array = new ArrayList();
//                array.add(newSeries);
//                shiftcontroller.getFiletoseries().put(currentfile, array);
//                        }
//                
//                
//                
//                
//
//                
//                
//                for (int i = 1; i < list.size()-1; i++) {
//                    
//                    float shift = (list.get(i).getOgroupShift().get(currentfile))*60;
//                    XYChart.Data data = new XYChart.Data(list.get(i).getRT(), shift);
//                    
//                    Ellipse cir = new Ellipse(1.5,4);
//                    
//                    TreeItem<Entry> item = null;
//                    for (int e = 0; e<shiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().size(); e++) {
//                        if (shiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().get(e).getValue().equals(list.get(i)))  {
//                            item = shiftcontroller.getSupercontroller().getMetTable().getRoot().getChildren().get(e);
//                            break;
//                        }
//                    }
//                    shiftcontroller.getNodetoogroup().put(cir, item);
//                    if (currentfile.isselected()) {
//                    cir.setFill(Color.RED);
//                }else {
//                cir.setFill(currentfile.getColor());
//                }
//                    data.setNode(cir);
//                    if (shiftcontroller.getOpacityMode().equals("Peak found")) {
//                        data.getNode().setOpacity(list.get(i).getmaxScorepeakfound(currentfile)+0.02); 
//                    } else if (shiftcontroller.getOpacityMode().equals("Peak close")) {
//                        data.getNode().setOpacity(list.get(i).getminScorepeakclose(currentfile)+0.02); 
//                    }
//                    
//                    
//                    
//                    
//                    newSeries.getData().add(data);
//                    if (shift > upper) {
//                        upper = shift;
//                    } else if (shift < lower) {
//                        lower = shift;
//                    }
//                }
//                scatterchart.getData().add(newSeries);
//              
//                
//
////TODO: number
//                System.out.println("Charts " + (f + 1) + "of " + list.get(0).getSession().getListofDatasets().get(d).getListofFiles().size() + " drawn");
//            }
//        }}}
        //scatterchart.applyCss();
        scatterchart.setMaxSize(2000, 2000);
        scatterchart.setLegendVisible(false);
        scatterchart.setHorizontalGridLinesVisible(false);
        scatterchart.setVerticalGridLinesVisible(false);
        scatterchart.setAlternativeRowFillVisible(false);
        scatterchart.setAlternativeColumnFillVisible(false);
        
   
        File f = new File("C:\\Users\\stefankoch\\Documents\\NetBeansProjects\\JavaFXTable\\src\\main\\java\\com\\mycompany\\fxmltableview\\gui\\stylesheet.css");
        scatterchart.getStylesheets().clear();
        scatterchart.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
        //scatterchart.applyCss();
//        Background back = new Background(new BackgroundFill(Color.GREEN, null, null));
//       scatterchart.setBackground(back);
//        yAxis.setVisible(false);
//        xAxis.setVisible(false);
        yAxis.setAutoRanging(false);
//        yAxis.setLowerBound(lower - 30);
//        yAxis.setUpperBound(upper + 30);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(40);
        yAxis.setLowerBound(session.getRTTolerance()*-55);
        yAxis.setUpperBound(session.getRTTolerance()*55);
       
//        xAxis.setLowerBound(session.getStart().floatValue()-5.0f);
//        xAxis.setUpperBound(session.getEnd().floatValue()+5.0f);
        return scatterchart;
    }
    
}
