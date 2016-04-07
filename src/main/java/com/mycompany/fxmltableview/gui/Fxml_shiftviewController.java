package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    private HashMap<RawDataFile, List<XYChart.Series>> filetoseries;
    private HashMap<XYChart.Series, RawDataFile> seriestofile;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //add ChartGenerator
      chartGenerator = new ChartGenerator(null,this);
      
      
    }    
    
    //method that generates the graphs
    public void print(ObservableList<Entry> list) {
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        
    olist = list;   
       //get selected Entry
       
          
    
           LineChart<Number,Number> linechart1 = chartGenerator.generateShiftChart(olist);
           box.getChildren().add(linechart1);
           
       //add listener to every color property, to show changes instantly
        for (int i = 0; i < filetoseries.size(); i++) {
            Set<RawDataFile> files = filetoseries.keySet();
            for (RawDataFile file : files) {
                file.getColorProperty().addListener(new ChangeListener<Color>() {
                    @Override
                    public void changed(ObservableValue<? extends Color> ov,
                            Color old_val, Color new_val) {
                      if (!file.isselected()) {
                            List<XYChart.Series> list = filetoseries.get(file);
                            
                            for (int i = 0; i<list.size(); i++) {              
                                Node node = list.get(i).getNode();
                                ((Path) node).setStroke(new_val);
                            
                            }
                    }
                    }
                });
                
                
                 file.getWidthProperty().addListener(new ChangeListener() {
                   @Override
                   public void changed(ObservableValue o, Object oldVal, Object newVal) {
                       System.out.println("Change");
                            List<XYChart.Series> list = filetoseries.get(file);
                            
                            for (int i = 0; i<list.size(); i++) {

                                Node node = list.get(i).getNode();
                                ((Path) node).setStrokeWidth(file.getWidth());
                              
                            
                            }
                    
                    }
                });

            }

        }
         Set<XYChart.Series> set = seriestofile.keySet();
        for(XYChart.Series series:set) {
            applyMouseEvents(series);
        }
        
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
        
                //Colors selected files in Shiftview, reacts to selection
        supercontroller.referenceFileView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<RawDataFile>() {

    @Override
    public void onChanged(ListChangeListener.Change<? extends RawDataFile> change) {
   List<RawDataFile> completeList = supercontroller.referenceFileView.getItems();
                List<RawDataFile> selectedList = supercontroller.referenceFileView.getSelectionModel().getSelectedItems();

                for (int i = 0; i < completeList.size(); i++) {
                    if (selectedList.contains(completeList.get(i))) {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        for (int j = 0; j < list.size(); j++) {
                         

                                Node node = list.get(j).getNode();
                                //node.setEffect(hover);
                                node.setCursor(Cursor.HAND);
                                ((Path) node).setStroke(Color.RED);
                            
                        }
                    } else {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        for (int j = 0; j < list.size(); j++) {

                                Node node = list.get(j).getNode();
                                //node.setEffect(hover);
                                node.setCursor(Cursor.HAND);
                                ((Path) node).setStroke(completeList.get(i).getColor());
                            
                        }

                    }
                }
    }

});
                

    }
       
      
         /**
     * @return the filetoseries
     */
    public HashMap<RawDataFile, List<XYChart.Series>> getFiletoseries() {
        return filetoseries;
    }

    /**
     * @param filetoseries the filetoseries to set
     */
    public void setFiletoseries(HashMap<RawDataFile, List<XYChart.Series>> filetoseries) {
        this.filetoseries = filetoseries;
    }

    /**
     * @return the seriestofile
     */
    public HashMap<XYChart.Series, RawDataFile> getSeriestofile() {
        return seriestofile;
    }

    /**
     * @param seriestofile the seriestofile to set
     */
    public void setSeriestofile(HashMap<XYChart.Series, RawDataFile> seriestofile) {
        this.seriestofile = seriestofile;
    }
        
    private void applyMouseEvents(final XYChart.Series series) {
if (series.getNode()!=null) {
        Node node = series.getNode();

//        node.setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//            @Override
//            public void handle(MouseEvent arg0) {
//                RawDataFile file = adductcontroller.getSeriestofile().get(series);
//                List<XYChart.Series> list = adductcontroller.getFiletoseries().get(file);
//                
//                for (int i = 0; i<list.size(); i++) {
//                    //if series is masschart
//                    if (list.get(i).getNode() == null) {
//                        for (int j = 0; j<list.get(i).getData().size(); j++) {
//                        Node node = (( XYChart.Data)list.get(i).getData().get(j)).getNode();
//                        //node.setEffect(hover);
//                        ((Rectangle)node).setFill(Color.RED);
//                        
//                    } }else {
//                    
//                    Node node = list.get(i).getNode();
//                    //node.setEffect(hover);
//                node.setCursor(Cursor.HAND);
//                ((Path) node).setStroke(Color.RED);
//                }}
//            }
//        });

//        node.setOnMouseExited(new EventHandler<MouseEvent>() {
//
//            @Override
//            public void handle(MouseEvent arg0) {
//                node.setEffect(null);
//                node.setCursor(Cursor.DEFAULT);
//            }
//        });

        node.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                     Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RawDataFile file = getSeriestofile().get(series);
                List<XYChart.Series> list = getFiletoseries().get(file);
                
                
                if (getSupercontroller().referenceFileView.getSelectionModel().getSelectedItems().contains(file)) {
                    ObservableList<RawDataFile> selist = getSupercontroller().referenceFileView.getSelectionModel().getSelectedItems();
                   
                    List<RawDataFile> newlist = new ArrayList<RawDataFile>();
                    for(RawDataFile sel : selist) {
                        newlist.add(sel);
                    }
                    getSupercontroller().referenceFileView.getSelectionModel().clearSelection();
                    newlist.remove(file);
                    for(RawDataFile sel : newlist) {
                        getSupercontroller().referenceFileView.getSelectionModel().select(sel);
                    }
                                       
                    
                } else {
                 getSupercontroller().referenceFileView.getSelectionModel().select(file);}
                

            }
        });
                   
                }
            }
        });
    }
    }
    
    
}
