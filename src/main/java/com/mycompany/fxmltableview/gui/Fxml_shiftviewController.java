package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.logic.Session;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
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
    
    @FXML
    ChoiceBox shiftOpacity;
    
    //Keep references to Properties and Listeners to be able to delete them
    private HashMap<ChangeListener, Property> listeners;
    private HashMap<ListChangeListener, ObservableList> listlisteners;
    private Session session;
    ChartGenerator chartGenerator;
    private FXMLTableViewController supercontroller;
    ObservableList<Entry> olist;
    private HashMap<RawDataFile, List<XYChart.Series>> filetoseries;
    private HashMap<XYChart.Series, RawDataFile> seriestofile;
    private HashMap<Ellipse,TreeItem<Entry>> nodetoogroup;
private DropShadow hover = new DropShadow();
private String OpacityMode;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //add ChartGenerator
      chartGenerator = new ChartGenerator(null,this);
      hover.setColor(Color.LIME);
        hover.setSpread(1);
        hover.setRadius(2);
        listeners = new HashMap<ChangeListener, Property>();
        listlisteners = new HashMap<ListChangeListener, ObservableList>();
      shiftOpacity.setItems(FXCollections.observableArrayList("Peak found", "Peak close", "distance"));
      shiftOpacity.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue ov, Number value, Number newVal) {
                setOpacityMode(shiftOpacity.getItems().get(newVal.intValue()).toString());
                //Fire Event to change Opacity immediately
                if (filetoseries!=null){
                    for (RawDataFile file : filetoseries.keySet()) {
                        Node node = ((XYChart.Data)filetoseries.get(file).get(0).getData().get(0)).getNode();
                        Event.fireEvent((EventTarget) node, new MouseEvent(MouseEvent.MOUSE_EXITED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
                    } 
             }}
            
            
            
        });
        shiftOpacity.getSelectionModel().select(0);
      
    }    
    
    //method that generates the graphs
    public void print(ObservableList<Entry> list) {
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        setNodetoogroup((HashMap<Ellipse,TreeItem<Entry>>) new HashMap());
        
    olist = list;   
       //get selected Entry
       
          
    
           ScatterChart<Number,Number> scatterchart = chartGenerator.generateScatterShiftChart(olist);
           box.getChildren().add(scatterchart);
           
       //add listener to every color property, to show changes instantly
        for (int i = 0; i < filetoseries.size(); i++) {
            Set<RawDataFile> files = filetoseries.keySet();
            for (RawDataFile file : files) {
                ChangeListener<Color> listener = new ChangeListener<Color>() {
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
                };
                
                file.getColorProperty().addListener(listener);
                listeners.put(listener, file.getColorProperty());
                
               ChangeListener listener2 = new ChangeListener() {
                   @Override
                   public void changed(ObservableValue o, Object oldVal, Object newVal) {
                       System.out.println("Change");
                            List<XYChart.Series> list = filetoseries.get(file);
                            
                            for (int i = 0; i<list.size(); i++) {

                                Node node = list.get(i).getNode();
                                ((Path) node).setStrokeWidth(file.getWidth());
                              
                            
                            }
                    
                    }
                };
                
                 file.getWidthProperty().addListener(listener2);
                 listeners.put(listener2, file.getWidthProperty());

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
        
      
        
        
        
        ScatterChart<Number,Number> scatterchart = chartGenerator.generateScatterShiftChart(olist);
        box.getChildren().remove(box.getChildren().size()-1);
        box.getChildren().add(scatterchart);
        
        Set<XYChart.Series> set = seriestofile.keySet();
        for(XYChart.Series series:set) {
            applyMouseEvents(series);
        }
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
        refsetpen.textProperty().bindBidirectional(supercontroller.session.getListofDatasets().get(0).getPenaltyProperty(), new NumberStringConverter());
     
                //Colors selected files in Shiftview, reacts to selection
                
        ListChangeListener<RawDataFile> listener = new ListChangeListener<RawDataFile>() {

    @Override
    public void onChanged(ListChangeListener.Change<? extends RawDataFile> change) {
         Platform.runLater(new Runnable() {
            @Override
            public void run() {
          
   List<RawDataFile> completeList = supercontroller.session.getAllFiles();
       

                for (int i = 0; i < completeList.size(); i++) {
                    if (completeList.get(i).isselected()) {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        for (int j = 0; j < list.size(); j++) {
                            for(int k =0; k<list.get(j).getData().size(); k++) {

                                Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                //node.setEffect(hover);
                                
                                ((Ellipse) node).setFill(Color.RED);
                                node.toFront();
                            }
                        }
                    } else {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        for (int j = 0; j < list.size(); j++) {
for(int k =0; k<list.get(j).getData().size(); k++) {

                                Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                //node.setEffect(hover);
                                
                                ((Ellipse) node).setFill(completeList.get(i).getColor());
                            }
                            
                        }

                    }
                }
                
                  
            }
        });   
                
    }

};
        
        for (int i = 0; i<supercontroller.session.getListofDatasets().size(); i++) {
            BatchController controller = supercontroller.getDatasettocontroller().get(supercontroller.session.getListofDatasets().get(i));
            controller.getBatchFileView().getSelectionModel().getSelectedItems().addListener(listener);
            listlisteners.put(listener, controller.getBatchFileView().getSelectionModel().getSelectedItems());
        }

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

        for (int i = 0; i< series.getData().size(); i++) {
        
        Node node = ((XYChart.Data) series.getData().get(i)).getNode();

        node.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                RawDataFile file = getSeriestofile().get(series);
                List<XYChart.Series> list = getFiletoseries().get(file);
                
                for (int i = 0; i<list.size(); i++) {
                    //if series is masschart
                    if (list.get(i).getNode() == null) {
                        for (int j = 0; j<list.get(i).getData().size(); j++) {
                        Node node = (( XYChart.Data)list.get(i).getData().get(j)).getNode();
                        ((Ellipse)node).setFill(Color.LIME);
                        node.setOpacity(1);
                        node.toFront();
                        
                        //((Rectangle)node).setFill(Color.RED);
                        
                    } }else {
                    
                    Node node = list.get(i).getNode();
                    node.setEffect(hover);
                    node.toFront();
                node.setCursor(Cursor.HAND);
                //((Path) node).setStroke(Color.RED);
                }}
            }
        });

        node.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                RawDataFile file = getSeriestofile().get(series);
                List<XYChart.Series> list = getFiletoseries().get(file);
                
                for (int i = 0; i<list.size(); i++) {
                    //if series is masschart
                    if (list.get(i).getNode() == null) {
                        for (int j = 0; j<list.get(i).getData().size(); j++) {
                        Node node = (( XYChart.Data)list.get(i).getData().get(j)).getNode();
                        if (OpacityMode.equals("Peak found")) {
                        node.setOpacity(nodetoogroup.get(node).getValue().getmaxScorepeakfound(file)+0.02);
                        } else if (OpacityMode.equals("Peak close")) {
                        node.setOpacity(nodetoogroup.get(node).getValue().getminScorepeakclose(file)+0.02);
                        } else if (OpacityMode.equals("distance")) {
                            node.setOpacity(nodetoogroup.get(node).getValue().getmaxScoredistance(file)+0.02);
                        }
                        if (file.isselected()){
                        ((Ellipse)node).setFill(Color.RED);
                        } else {
                            ((Ellipse)node).setFill(file.getColor());
                        }
                        
                        //((Rectangle)node).setFill(Color.RED);
                        
                    } }else {
                    
                    Node node = list.get(i).getNode();
                    node.setEffect(null);
                node.setCursor(Cursor.DEFAULT);
                //((Path) node).setStroke(Color.RED);
                }}
            }
        });

        node.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    
                    if(mouseEvent.getClickCount() == 2){
                        supercontroller.getMetTable().getSelectionModel().select(nodetoogroup.get(node));
                        //scroll to selected Ogroup
                        supercontroller.getMetTable().scrollTo(supercontroller.getMetTable().getRow(nodetoogroup.get(node))-5);
                        
                        try {

//only select file of interest
RawDataFile file = getSeriestofile().get(series);
getSupercontroller().getDatasettocontroller().get(file.getDataset()).getBatchFileView().getSelectionModel().clearSelection();
getSupercontroller().getDatasettocontroller().get(file.getDataset()).getBatchFileView().getSelectionModel().select(file);
getSupercontroller().getDatasettocontroller().get(file.getDataset()).changedFile();
                            
                        //create new window
                        Stage stage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_adductview.fxml"));
                        Pane myPane = (Pane) loader.load();
                        Scene myScene = new Scene(myPane);
                        stage.setScene(myScene);
                        Fxml_adductviewController controller = loader.<Fxml_adductviewController>getController();
                        controller.setSession(supercontroller.session);
                        controller.setMainController(supercontroller);

                        //add MasterListofOGroups to new controller
                        controller.metTable = supercontroller.getMetTable();
                        
                        
                        

                        //print graphs
                        controller.print();
                        stage.show();

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        
                    } else {
                    
                     Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RawDataFile file = getSeriestofile().get(series);
                List<XYChart.Series> list = getFiletoseries().get(file);
                 BatchController controller = supercontroller.getDatasettocontroller().get(file.getDataset());
                  if (supercontroller.session.getSelectedFiles().contains(file)) {
                                    
                                    ObservableList<RawDataFile> selist = controller.getBatchFileView().getSelectionModel().getSelectedItems();

                                    List<RawDataFile> newlist = new ArrayList<RawDataFile>();
                                    for (RawDataFile sel : selist) {
                                        newlist.add(sel);
                                    }
                                    controller.getBatchFileView().getSelectionModel().clearSelection();
                                    newlist.remove(file);
                                    for (RawDataFile sel : newlist) {
                                        controller.getBatchFileView().getSelectionModel().select(sel);
                                    }

                                } else {
                                    controller.getBatchFileView().getSelectionModel().select(file);
                                }
                                controller.changedFile();

            }
        });
                   
                }
                }
            }
        });
    }
    }

    /**
     * @return the nodetoogroup
     */
    public HashMap<Ellipse,TreeItem<Entry>> getNodetoogroup() {
        return nodetoogroup;
    }

    /**
     * @param nodetoogroup the nodetoogroup to set
     */
    public void setNodetoogroup(HashMap<Ellipse,TreeItem<Entry>> nodetoogroup) {
        this.nodetoogroup = nodetoogroup;
    }
    
    
    public void close() {
        //delete all nodes
        for(Ellipse el:nodetoogroup.keySet()) {
            el = null;
        }
        
        //delete all listeners
        for(Map.Entry<ChangeListener,Property> lis : listeners.entrySet()){
            lis.getValue().removeListener(lis.getKey());
        }
        for(Map.Entry<ListChangeListener,ObservableList> lis : listlisteners.entrySet()){
            lis.getValue().removeListener(lis.getKey());
        }
        System.gc();
    }
    
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
        this.chartGenerator.setSession(session);
    }

    /**
     * @return the OpacityMode
     */
    public String getOpacityMode() {
        return OpacityMode;
    }

    /**
     * @param OpacityMode the OpacityMode to set
     */
    public void setOpacityMode(String OpacityMode) {
        this.OpacityMode = OpacityMode;
    }
}
