package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Peak;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import com.mycompany.fxmltableview.logic.CertaintyCalculator;
import com.mycompany.fxmltableview.logic.GravityCalculator;
import com.mycompany.fxmltableview.logic.Session;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
 *
 * @author stefankoch
 */
public class Fxml_gravityshiftviewController implements Initializable {

    //Gridpane holding all the graphs
    @FXML
    StackPane stackpane;

    @FXML
    ToggleButton paramToggle;

    @FXML
    AnchorPane anchorPane;
    
    @FXML
    ProgressBar progress;

    @FXML
    Pane paramPane;

    @FXML
    TextField x1, x2, x3, x4, x5, x6, x7, x8, x9, y1, y2, y3, y4, y5, y6, y7, y8, y9;

    @FXML
    Slider speed;
            
    @FXML 
    Menu menu;
    
    @FXML
    MenuItem item1, item2;
    

//    @FXML
//    ProgressBar progress;
//    @FXML
//    ProgressIndicator calculating;



    private AreaChart<Number, Number> areachart;
    private ScatterChart<Number, Number> scatterchart;
   

    //Keep references to Properties and Listeners to be able to delete them
    private HashMap<ChangeListener, Property> listeners;
    private HashMap<ListChangeListener, ObservableList> listlisteners;
    private Session session;
    ChartGenerator chartGenerator;
    private FXMLTableViewController supercontroller;
    private ObservableList<Entry> olist;
    private HashMap<RawDataFile, List<XYChart.Series>> filetoseries;
    private HashMap<XYChart.Series, RawDataFile> seriestofile;
    private HashMap<Ellipse, TreeItem<Entry>> nodetoogroup;

    private String OpacityMode;

    //newShift
    private XYChart.Series topSeries;
    private XYChart.Series midSeries;
    private XYChart.Series botSeries;
    private GravityCalculator grav;
    private float[][] samplematrix;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        paramToggle.setSelected(true);
        paramToggle.setText("Click to Start");
        paramPane.setVisible(true);
        stackpane.setVisible(false);
        menu.setDisable(true);
        progress.setVisible(false);

        

    

        
        //add ChartGenerator
        chartGenerator = new ChartGenerator(null, null, this);
       
        listeners = new HashMap<ChangeListener, Property>();
        listlisteners = new HashMap<ListChangeListener, ObservableList>();
       
            

    }

    //method that generates the graphs
    public void print(ObservableList<Entry> list) throws InterruptedException, IOException {
        //progress.setVisible(true);
        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {

                //CountDownLatch latch = new CountDownLatch(1);
                //supercontroller.animate(latch, progress);
                //latch.await();
                setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
                setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
                setNodetoogroup((HashMap<Ellipse, TreeItem<Entry>>) new HashMap());

                Collections.sort(list, new Entry.orderbyRT());
                setOlist(list);
                //get selected Entry

                areachart = chartGenerator.generateNewShift(list);
                areachart.setAnimated(true);
                //scatterchart = chartGenerator.generateNewPeak(null);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stackpane.getChildren().add(areachart);
                        //stackpane.getChildren().add(scatterchart);

                    }
                });

                RawDataFile file = session.getAllFiles().get(0);
                //Peak picking
                CountDownLatch latchpeak = new CountDownLatch(1);
                Task task = new Task<Void>() {
                    @Override
                    public Void call() throws IOException, InterruptedException {
                        this.updateProgress(0, list.size());
                        int done = 0;
                        session.getIothread().lockFile(file, true);
                        double start = System.currentTimeMillis();
                        LinkedList<Integer> queue = new LinkedList<Integer>();
                        //go trough and check if all are ready
                        for (int i = 0; i < list.size(); i++) {
                            //if not ready, add to queue
                            if (list.get(i).isStored(file)) {
                                queue.add(i);
                                session.getIothread().readOGroup(list.get(i), file);
                                //if ready animate
                            } else {
                                list.get(i).peakpickOGroup(file);
                                System.out.println(i + " of " + list.size() + " OGroups calculated");
                                done++;
                                this.updateProgress(done, list.size());
                            }
                        }
                        System.out.println("Size of Queue: " + queue.size());
                        //go through queue until it is empty
                        double picktime = 0;
                        while (queue.size() > 0) {
                            int size = queue.size();
                            for (int j = 0; j < size; j++) {
                                Integer current = queue.pop();
                                if (list.get(current).isStored(file)) {
                                    queue.add(current);
                                } else {
                                    double pick = System.currentTimeMillis();
                                    list.get(current).peakpickOGroup(file);
                                    picktime += System.currentTimeMillis() - pick;
                                    done++;
                                    this.updateProgress(done, list.size());

                                }
                            }
                            System.out.println("Size of Queue: " + queue.size());
                        }

                        latchpeak.countDown();
                        System.out.println("Total peak picking time: " + picktime);
                        System.out.println("Total time: " + (System.currentTimeMillis() - start));
                        session.getIothread().lockFile(file, false);
                        return null;
                    }

                };

                //new thread that executes maintask
                new Thread(task).start();
                progress.progressProperty().bind(task.progressProperty());
                latchpeak.await();

                int iterations = 10;

                //calculate initial centroids
                //int[][] windows = calculateWindows(list);
                //float[] centroids = new float[list.size()];
                //TODO: peak weights
                //calculateCentroids(file, windows, list, centroids, true);
                //calculateAreas(file,list);
                //iterate
                for (int i = 0; i < iterations; i++) {

                }

//                for (int j = 0; j<midSeries.getData().size(); j++) {
//                    ((XYChart.Data)midSeries.getData().get(j)).YValueProperty().setValue(centroids[j]);
//                    
//                    //((XYChart.Data)topSeries.getData().get(j)).YValueProperty().setValue(i+30);
//                    //((XYChart.Data)botSeries.getData().get(j)).YValueProperty().setValue(centroids[j]);
//                }
                Thread.sleep(300);

                //add listener to every color property, to show changes instantly
//        for (int i = 0; i < filetoseries.size(); i++) {
//            Set<RawDataFile> files = filetoseries.keySet();
//            for (RawDataFile file : files) {
//                ChangeListener<Color> listener = new ChangeListener<Color>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Color> ov,
//                            Color old_val, Color new_val) {
//                        if (!file.isselected()) {
//                            List<XYChart.Series> list = filetoseries.get(file);
//
//                            for (int i = 0; i < list.size(); i++) {
//                                Node node = list.get(i).getNode();
//                                ((Path) node).setStroke(new_val);
//
//                            }
//                        }
//                    }
//                };
//
//                file.getColorProperty().addListener(listener);
//                listeners.put(listener, file.getColorProperty());
//
//            }
//
//        }
//
//        
//        Set<XYChart.Series> set = seriestofile.keySet();
//        for (XYChart.Series series : set) {
//            applyMouseEvents(series);
//        
//        }
//        progress.setVisible(false);
//        calculating.setVisible(false);
                return null;
            }

        };

        //new thread that executes maintask
        new Thread(task).start();
    }

//    public void recalculate() throws IOException, InterruptedException {
//        progress.setVisible(true);
//        calculating.setVisible(true);
//        calculating.toFront();
//        Task maintask = new Task<Void>() {
//            @Override
//            public Void call() throws IOException, InterruptedException {
//        if (penSelection) {
//            togglePenaltySelectionButton.fire();
//        }
//        
//        CountDownLatch latch = new CountDownLatch(1);
//
//                supercontroller.animate(latch, progress);
//
//        latch.await();
//        
//        
//        
//       scatterchart = chartGenerator.generateScatterShiftChart(olist); 
//       
//        
//        
//         Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                stackpane.getChildren().clear();
//                stackpane.getChildren().add(scatterchart);
//                System.out.println("new chart added");
//            }
//        });
//
//        Set<XYChart.Series> set = seriestofile.keySet();
//        for (XYChart.Series series : set) {
//            applyMouseEvents(series);
//        }
//        
//        if (filetoseries != null) {
//                    for (RawDataFile file : filetoseries.keySet()) {
//                        Node node = ((XYChart.Data) filetoseries.get(file).get(0).getData().get(0)).getNode();
//                        Event.fireEvent((EventTarget) node, new MouseEvent(MouseEvent.MOUSE_EXITED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
//                    }
//                }
//        progress.setVisible(false);
//        calculating.setVisible(false);
//        calculating.toBack();
//         return null;
//            }
//
//        };
//
//        //new thread that executes maintask
//        new Thread(maintask).start();
//    }
    public void animate(ObservableList<Entry> list) throws IOException, InterruptedException {
        session.setPeakPickchanged(true);
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        setNodetoogroup((HashMap<Ellipse, TreeItem<Entry>>) new HashMap());
        progress.setVisible(true);
        olist = list;
        //get selected Entry

        if (areachart == null) {
            areachart = chartGenerator.generateNewShift(list);
            areachart.setAnimated(true);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    stackpane.getChildren().add(areachart);

                }
            });
        }
        ((Group) topSeries.getNode()).getChildren().get(0).setVisible(true);
        ((Group) botSeries.getNode()).getChildren().get(0).setVisible(true);

        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
        
        float[] centroids = new float[list.size()];
                for (int d = 0; d < session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
                        for (int f = 0; f < session.getListofDatasets().get(d).getListofFiles().size(); f++) {
                            RawDataFile currentfile = session.getListofDatasets().get(d).getListofFiles().get(f);
                            if (currentfile.getActive().booleanValue()) {
                                if (currentfile.isselected()) {

                                CountDownLatch latchpeak = new CountDownLatch(1);
                                Task task = new Task<Void>() {
                                    @Override
                                    public Void call() throws IOException, InterruptedException {
                                        
                                        int done = 0;
                                        samplematrix = new float[list.size()][session.getResolution()];
                                        session.getIothread().lockFile(currentfile, true);
                                        double start = System.currentTimeMillis();
                                        LinkedList<Integer> queue = new LinkedList<Integer>();
                                        //go trough and check if all are ready
                                        for (int i = 0; i < list.size(); i++) {
                                            //if not ready, add to queue
                                            if (list.get(i).isStored(currentfile)) {
                                                queue.add(i);
                                                session.getIothread().readOGroup(list.get(i), currentfile);
                                                //if ready animate
                                            } else {
                                                list.get(i).peakpickOGroup(currentfile);
                                                list.get(i).getOGroupPropArraySmooth(currentfile, samplematrix, i);
                                                System.out.println(i + " of " + list.size() + " OGroups calculated");
                                                done++;
                                                updateProgress(done, list.size());
                                            }
                                        }
                                        System.out.println("Size of Queue: " + queue.size());
                                        //go through queue until it is empty
                                        double picktime = 0;
                                        while (queue.size() > 0) {
                                            int size = queue.size();
                                            for (int j = 0; j < size; j++) {
                                                Integer current = queue.pop();
                                                if (list.get(current).isStored(currentfile)) {
                                                    queue.add(current);
                                                } else {
                                                    double pick = System.currentTimeMillis();
                                                    list.get(current).peakpickOGroup(currentfile);
                                                    picktime += System.currentTimeMillis() - pick;
                                                    list.get(current).getOGroupPropArraySmooth(currentfile, samplematrix, current);
                                                    done++;
                                                    updateProgress(done, list.size());
                                                   
                                                }
                                            }
                                            System.out.println("Size of Queue: " + queue.size());
                                        }

                                        latchpeak.countDown();
                                        
                                        System.out.println("Total peak picking time: " + picktime);
                                        System.out.println("Total time: " + (System.currentTimeMillis() - start));
                                        session.getIothread().lockFile(currentfile, false);

                                        return null;
                                    }

                                };

                                //new thread that executes maintask
                                if (scatterchart == null) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.progressProperty().bind(task.progressProperty());
                                        }
                                    });
                                    
                                    new Thread(task).start();
                                    latchpeak.await();
                                     Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                           progress.progressProperty().unbind();
                                           progress.setProgress(-1d);

                                        }
                                    });

                                    scatterchart = chartGenerator.generateNewPeak(list);

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            stackpane.getChildren().add(scatterchart);

                                        }
                                    });
                                }
                                progress.setVisible(false);
                                //calculation
                                //calculateAreas(matrix, 0, list.size()-1, 49, 7, 49);
                               

                                if (!getParameters()) {
                                    System.out.println("Error while parsing Parameters!!!!");
                                    System.out.println("Please check Parameters, all have to be Integers.");
                                    return null;
                                }
                                int count = 0;
                                while (count<9&&grav.getxRanges()[count] != 0) {
                                    //draw new borders
                                    int yrange = grav.getyRanges()[count];
                                  
                                            for (int i = 0; i < centroids.length; i++) {
                                                ((XYChart.Data) topSeries.getData().get(i)).YValueProperty().setValue(centroids[i] + yrange);
                                                ((XYChart.Data) botSeries.getData().get(i)).YValueProperty().setValue(centroids[i] - yrange);
                                            }
                                        

                                    Thread.sleep((long) speed.getValue());

                                    centroids = grav.gravity(count, samplematrix, centroids);

                                   
                                            for (int i = 0; i < centroids.length; i++) {
                                                ((XYChart.Data) midSeries.getData().get(i)).YValueProperty().setValue(centroids[i]);
                                            }

                                        

                                    Thread.sleep((long) speed.getValue());

                                    count++;

                                }
                                progress.setVisible(false);
                             ((Group) topSeries.getNode()).getChildren().get(0).setVisible(false);
                             ((Group) botSeries.getNode()).getChildren().get(0).setVisible(false);

                                
//set fitted peak
//                for (int i = 0; i<olist.size(); i++) {
//                olist.get(i).setFittedShift(currentfile, (short) centroids[i]);
//                }
                paramToggle.setDisable(false);
menu.setDisable(false);
                return null;
                            }
                            }
                        }
                    }
                }

//                don't recalculate unless something changes

                
                
                System.out.println("Error: No file in Dataset selected");
             return null;   
            }

        };

        //new thread that executes maintask
        new Thread(task).start();

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
                                if (list != null) {
                                    for (int j = 0; j < list.size(); j++) {
                                        for (int k = 0; k < list.get(j).getData().size(); k++) {

                                            Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                            //node.setEffect(hover);

                                            ((Ellipse) node).setFill(Color.RED);
                                            node.toFront();
                                        }
                                    }
                                }
                            } else {
                                List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                                if (list != null) {
                                    for (int j = 0; j < list.size(); j++) {
                                        for (int k = 0; k < list.get(j).getData().size(); k++) {

                                            Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                            //node.setEffect(hover);

                                            ((Ellipse) node).setFill(completeList.get(i).getColor());
                                        }

                                    }

                                }
                            }
                        }

                    }
                });

            }

        };

        for (int i = 0; i < supercontroller.session.getListofDatasets().size(); i++) {
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

        for (int i = 0; i < series.getData().size(); i++) {

            Node node = ((XYChart.Data) series.getData().get(i)).getNode();

            node.setOnMouseEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent arg0) {
                    RawDataFile file = getSeriestofile().get(series);
                    List<XYChart.Series> list = getFiletoseries().get(file);

                    for (int i = 0; i < list.size(); i++) {
                        //if series is masschart
                        if (list.get(i).getNode() == null) {
                            for (int j = 0; j < list.get(i).getData().size(); j++) {
                                Node node = ((XYChart.Data) list.get(i).getData().get(j)).getNode();
                                ((Ellipse) node).setFill(Color.LIME);
                                node.setOpacity(1);
                                node.toFront();

                                //((Rectangle)node).setFill(Color.RED);
                            }
                        } else {

                            Node node = list.get(i).getNode();
                            node.toFront();
                            node.setCursor(Cursor.HAND);
                            //((Path) node).setStroke(Color.RED);
                        }
                    }
                }
            });

            node.setOnMouseExited(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent arg0) {
                    RawDataFile file = getSeriestofile().get(series);
                    List<XYChart.Series> list = getFiletoseries().get(file);

                    for (int i = 0; i < list.size(); i++) {
                        //if series is masschart
                        if (list.get(i).getNode() == null) {
                            for (int j = 0; j < list.get(i).getData().size(); j++) {
                                Node node = ((XYChart.Data) list.get(i).getData().get(j)).getNode();
//                                if (OpacityMode.equals("Peak found")) {
//                                    node.setOpacity(nodetoogroup.get(node).getValue().getmaxScorepeakfound(file) + 0.02);
//                                } else if (OpacityMode.equals("Peak close")) {
//                                    node.setOpacity(nodetoogroup.get(node).getValue().getminScorepeakclose(file) + 0.02);
//                                } else if (OpacityMode.equals("distance")) {
//                                    node.setOpacity(nodetoogroup.get(node).getValue().getmaxScoredistance(file) + 0.02);
//                                } else if (OpacityMode.equals("certainty")) {
//                                    node.setOpacity(nodetoogroup.get(node).getValue().getCertainties().get(file));
//                                }
                                if (file.isselected()) {
                                    ((Ellipse) node).setFill(Color.RED);
                                } else {
                                    ((Ellipse) node).setFill(file.getColor());
                                }

                                //((Rectangle)node).setFill(Color.RED);
                            }
                        } else {

                            Node node = list.get(i).getNode();
                            node.setEffect(null);
                            node.setCursor(Cursor.DEFAULT);
                            //((Path) node).setStroke(Color.RED);
                        }
                    }
                }
            });

            node.setOnMouseReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {

                        if (mouseEvent.getClickCount() == 2) {
                            supercontroller.getMetTable().getSelectionModel().select(nodetoogroup.get(node));
                            //scroll to selected Ogroup
                            supercontroller.getMetTable().scrollTo(supercontroller.getMetTable().getRow(nodetoogroup.get(node)) - 5);

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
    
    //method that generates the graphs
    public void showShift(ObservableList<Entry> list) throws InterruptedException, IOException {
        
      // progress.setVisible(true);
        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {

        
  
        
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        setNodetoogroup((HashMap<Ellipse, TreeItem<Entry>>) new HashMap());

        olist = list;
        //get selected Entry

      
             scatterchart = chartGenerator.generateScatterShiftChartnew(olist); 
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stackpane.getChildren().clear();
                stackpane.getChildren().add(scatterchart);
            }
        });
        
        

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

                            for (int i = 0; i < list.size(); i++) {
                                Node node = list.get(i).getNode();
                                ((Path) node).setStroke(new_val);

                            }
                        }
                    }
                };

                file.getColorProperty().addListener(listener);
                listeners.put(listener, file.getColorProperty());

            }

        }

        
        Set<XYChart.Series> set = seriestofile.keySet();
        for (XYChart.Series series : set) {
            applyMouseEvents(series);
        
        }
        progress.setVisible(false);
        //calculating.setVisible(false);
        return null;
            }

        };

        //new thread that executes maintask
        new Thread(task).start();
    }

    /**
     * @return the nodetoogroup
     */
    public HashMap<Ellipse, TreeItem<Entry>> getNodetoogroup() {
        return nodetoogroup;
    }

    /**
     * @param nodetoogroup the nodetoogroup to set
     */
    public void setNodetoogroup(HashMap<Ellipse, TreeItem<Entry>> nodetoogroup) {
        this.nodetoogroup = nodetoogroup;
    }

    public void close() {
        //delete all nodes
//        for (Ellipse el : nodetoogroup.keySet()) {
//            el = null;
//        }

        //delete all listeners
//        for (Map.Entry<ChangeListener, Property> lis : listeners.entrySet()) {
//            lis.getValue().removeListener(lis.getKey());
//        }
//        for (Map.Entry<ListChangeListener, ObservableList> lis : listlisteners.entrySet()) {
//            lis.getValue().removeListener(lis.getKey());
//        }
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
        this.grav = session.getGravitycalculator();
        initializeparams();
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

//    public void togglePenaltySelection() {
//        penSelection = !penSelection;
//
//        if (penSelection) {
//            Node chartBackground = stackpane.getParent();
//
//            //Handlers for Selection Box 
//            chartBackground.setOnMousePressed(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    anchor.set(new Point2D(event.getX(), event.getY()));
//                    select.setVisible(true);
//                    select.setWidth(0);
//                    select.setHeight(0);
//                }
//            });
//
//            chartBackground.setOnMouseDragged(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    double x = event.getX();
//                    double y = event.getY();
//                    select.setX(Math.min(x, anchor.get().getX()));
//                    select.setY(Math.min(y, anchor.get().getY()));
//                    select.setWidth(Math.abs(x - anchor.get().getX()));
//                    select.setHeight(Math.abs(y - anchor.get().getY()));
//                }
//            });
//            
//            chartBackground.setOnMouseReleased(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    select.setVisible(false);
//                            
//                }
//            });
//
//            //handlers to get correct peaks
//            chartBackground = scatterchart.lookup(".chart-plot-background");
//
//            chartBackground.setOnMousePressed(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//startX = scatterchart.getXAxis().getValueForDisplay(event.getX()).floatValue();
//startY = scatterchart.getYAxis().getValueForDisplay(event.getY()).floatValue();
//
//                    System.out.println(startX + "   " + startY);
//                }
//            });
//
//            chartBackground.setOnMouseDragged(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    
//                }
//            });
//            
//            chartBackground.setOnMouseReleased(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                endX = scatterchart.getXAxis().getValueForDisplay(event.getX()).floatValue(); 
//                endY = scatterchart.getYAxis().getValueForDisplay(event.getY()).floatValue();
//                System.out.println(endX + "   " + endY);
//                session.addPenalty(startX, startY, endX, endY);
//                }
//            });
//
//            //disable Handlers
//        } else {
//            Node chartBackground = stackpane.getParent();
//            chartBackground.setOnMousePressed(null);
//            chartBackground.setOnMouseDragged(null);
//            chartBackground.setOnMouseReleased(null);
//            chartBackground = scatterchart.lookup(".chart-plot-background");
//            chartBackground.setOnMousePressed(null);
//            chartBackground.setOnMouseDragged(null);
//            chartBackground.setOnMouseReleased(null);
//        }
//
//    }
    public void showPeakView() throws IOException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_peakview.fxml"));
        Pane myPane = (Pane) loader.load();
        Scene myScene = new Scene(myPane);
        stage.setScene(myScene);
        Fxml_peakviewController controller = loader.<Fxml_peakviewController>getController();
        controller.setOlist(olist);
        controller.setSession(session);
        controller.print();
        stage.show();

    }

    /**
     * @return the topSeries
     */
    public XYChart.Series getTopSeries() {
        return topSeries;
    }

    /**
     * @param topSeries the topSeries to set
     */
    public void setTopSeries(XYChart.Series topSeries) {
        this.topSeries = topSeries;
    }

    /**
     * @return the midSeries
     */
    public XYChart.Series getMidSeries() {
        return midSeries;
    }

    /**
     * @param midSeries the midSeries to set
     */
    public void setMidSeries(XYChart.Series midSeries) {
        this.midSeries = midSeries;
        midSeries.getNode().toFront();
    }

    /**
     * @return the botSeries
     */
    public XYChart.Series getBotSeries() {
        return botSeries;
    }

    /**
     * @param botSeries the botSeries to set
     */
    public void setBotSeries(XYChart.Series botSeries) {
        this.botSeries = botSeries;
    }

    public int[][] calculateWindows(ObservableList<Entry> list) {
        float range = 3f;
        //windows[0] holds the start OGroup of each window, windows[1] holds the end
        int[][] windows = new int[list.size()][2];

        //for all Ogroups
        for (int i = 0; i < list.size(); i++) {
            //get start
            int start = i;
            while (start > 0 && list.get(i).getRT() - list.get(start - 1).getRT() <= range) {
                start--;
            }
            int end = i;
            while (end < list.size() - 1 && list.get(end + 1).getRT() - list.get(i).getRT() <= range) {
                end++;
            }

            windows[i][0] = start;
            windows[i][1] = end;

        }

        return windows;
    }

    //windows holds start and end, centroids holds the centroid for each OGroup, intitialize is true if it is the first iteration
    public void calculateCentroids(RawDataFile file, int[][] windows, ObservableList<Entry> list, float[] centroids, boolean initialize) throws InterruptedException {
//        for (int i = 0; i<list.size(); i++) {
//            float weights = 0;
//            float peaks = 0;
//            for (int w = windows[i][0]; w<=windows[i][1]; w++) {
//                for (int j = 0; j<list.get(w).getListofAdducts().size(); j++) {
//                    if (list.get(w).getListofAdducts().get(j).getListofSlices().containsKey(file)) {
//                        Slice slice = list.get(w).getListofAdducts().get(j).getListofSlices().get(file);
//                        if (slice.getListofPeaks()!=null){
//                        for (int p = 0; p<slice.getListofPeaks().size(); p++) {
//                            peaks+=slice.getListofPeaks().get(p).getIndexshift()*60.0f;
//                            weights+=1;
//                        }
//                    }
//                    }
//
//                }
//        }
//            if (weights>0) {
//            centroids[i] = peaks/weights;
//            //System.out.println(centroids[i]);
//            } else {
//                centroids[i] = Float.NaN;
//            }
//            
//        }

//          for (int i = 0; i<list.size(); i++) {
//            float weights = 0;
//            float peaks = 0;
//             int[] bins = new int[12];
//            for (int w = windows[i][0]; w<=windows[i][1]; w++) {
//               
//                for (int j = 0; j<list.get(w).getListofAdducts().size(); j++) {
//                    if (list.get(w).getListofAdducts().get(j).getListofSlices().containsKey(file)) {
//                        Slice slice = list.get(w).getListofAdducts().get(j).getListofSlices().get(file);
//                        if (slice.getListofPeaks()!=null){
//                        for (int p = 0; p<slice.getListofPeaks().size(); p++) {
//                            peaks=slice.getListofPeaks().get(p).getIndexshift()*60.0f;
//                            if (peaks < -50) {
//                                bins[0]++;
//                            } else if (peaks<-40) {
//                                bins[1]++;
//                            } else if (peaks<-30) {
//                                bins[2]++;
//                            } else if (peaks<-20) {
//                                bins[3]++;
//                            } else if (peaks<-10) {
//                                bins[4]++;
//                            } else if (peaks<0) {
//                                bins[5]++;
//                            } else if (peaks<10) {
//                                bins[6]++;
//                            } else if (peaks<20) {
//                                bins[7]++;
//                            } else if (peaks<30) {
//                                bins[8]++;
//                            } else if (peaks<40) {
//                                bins[9]++;
//                            } else if (peaks<50) {
//                                bins[10]++;
//                            } else {
//                                bins[11]++;
//                            } 
//                            
//                        }
//                    }
//                    }
//
//                }
//                         
//        }
//             int max = 0;
//                            int maxint = 0;
//                            for (int b = 0; b<bins.length; b++) {
//                                if (bins[b]>max) {
//                                    max = bins[b];
//                                    maxint = b;
//                                }
//                            }
//                           centroids[i]=-60+10*maxint;
//                            System.out.println(-60+10*maxint);
//            
//        }
        float[] weights = new float[centroids.length];

        ArrayList<Peak>[] allpeaks = (ArrayList<Peak>[]) new ArrayList[list.size()];
        for (int i = 0; i < list.size(); i++) {

            ArrayList<Peak> peaks = new ArrayList<Peak>();
            for (int w = windows[i][0]; w <= windows[i][1]; w++) {
                for (int j = 0; j < list.get(w).getListofAdducts().size(); j++) {
                    if (list.get(w).getListofAdducts().get(j).getListofSlices().containsKey(file)) {
                        Slice slice = list.get(w).getListofAdducts().get(j).getListofSlices().get(file);
                        if (slice.getListofPeaks() != null) {
                            peaks.addAll(slice.getListofPeaks());
                        }
                    }

                }
            }
            Collections.sort(peaks, new Peak.orderbyIndexShift());
            allpeaks[i] = peaks;
        }
        System.out.println("Peak lists created");

        //initial calculations
        float dist = 3;
        for (int i = 0; i < list.size(); i++) {
            if (allpeaks[i].size() > 0) {

                float max = 0;
                int maxint = 0;

                for (int b = 0; b < allpeaks[i].size(); b++) {
                    float max2 = 1;
                    int start = b - 1;
                    while (start > 0 && (allpeaks[i].get(b).getIndexshift() * 60 - allpeaks[i].get(start - 1).getIndexshift() * 60) <= dist) {
                        max2 += 1;
                        start--;
                    }
                    int end = b + 1;
                    while (end < allpeaks[i].size() - 2 && (allpeaks[i].get(end + 1).getIndexshift() * 60 - allpeaks[i].get(b).getIndexshift() * 60) <= dist) {
                        max2 += 1;
                        end++;
                    }

                    if (max2 > max) {
                        max = max2;
                        maxint = b;
                    }

                }
                centroids[i] = allpeaks[i].get(maxint).getIndexshift() * 60;
                weights[i] = max;
                ((XYChart.Data) midSeries.getData().get(i)).YValueProperty().setValue(centroids[i]);
            }
            System.out.println(i);
        }

        //iterate
        for (int t = 0; t < 50; t++) {
            dist = dist - 0.05f;

            float[] ncentroids = new float[centroids.length];
            float[] nweights = new float[centroids.length];

            for (int i = 0; i < list.size(); i++) {
                if (allpeaks[i].size() > 0) {

                    float max = 0;
                    int maxint = 0;

                    for (int b = 0; b < allpeaks[i].size(); b++) {
                        float max2 = 1;
                        int start = b - 1;
                        while (start > 0 && (allpeaks[i].get(b).getIndexshift() * 60 - allpeaks[i].get(start - 1).getIndexshift() * 60) <= dist) {
                            max2 += 1;
                            start--;
                        }
                        int end = b + 1;
                        while (end < allpeaks[i].size() - 2 && (allpeaks[i].get(end + 1).getIndexshift() * 60 - allpeaks[i].get(b).getIndexshift() * 60) <= dist) {
                            max2 += 1;
                            end++;
                        }

                        if (i > 0 && Math.abs(allpeaks[i].get(b).getIndexshift() * 60 - centroids[i - 1]) <= dist) {
                            max2 += weights[i - 1];
                        }

                        if (i < list.size() - 2 && Math.abs(allpeaks[i].get(b).getIndexshift() * 60 - centroids[i + 1]) <= dist) {
                            max2 += weights[i + 1];
                        }

                        if (max2 > max) {
                            max = max2;
                            maxint = b;
                        }

                    }
                    ncentroids[i] = allpeaks[i].get(maxint).getIndexshift() * 60;
                    nweights[i] = max;
                    ((XYChart.Data) midSeries.getData().get(i)).YValueProperty().setValue(centroids[i]);
                }
                System.out.println(i);
            }

            centroids = ncentroids;
            weights = nweights;

        }

    }

    //calculates the area with max peak weigts, iteratively for thinner and narrower windows
    //start is RTstart, end is RTend, range is Shiftrange, windowsize is size in one direction
    public void calculateAreas(float[][] matrix, int start, int end, int range, int windowsize, int centroid) {

        float[] window = new float[windowsize * 2 + 1];
        //if even number
        if (window.length % 2 == 0) {
            float dif = 1 / window.length;
            float val = 1 - dif;
            for (int i = 0; i < window.length / 2; i++) {
                window[window.length / 2 + i] = val;
                window[window.length / 2 - (i + 1)] = val;
                val = val - 2 * dif;
            }
        } else {
            float dif = 1.0f / ((float) window.length / 2.0f);
            float val = 1 - dif;
            int middle = (window.length - 1) / 2;
            window[middle] = 1;
            for (int i = 1; i <= middle; i++) {
                window[middle - i] = val;
                window[middle + i] = val;
                val -= dif;
            }
        }

        //get max window
        int upperanchor = centroid + range;
        int maxint = centroid;
        float max = 0;

        int i = 0;
        while (upperanchor - i - window.length + 1 >= centroid - range) {
            float nmax = 0;
            //calculate tops and bottoms
            int top = 0;
            if (upperanchor - i >= session.getResolution()) {
                top = upperanchor - i - session.getResolution() + 1;
            }
            int bottom = window.length - 1;
            if (upperanchor - i - window.length + 1 < 0) {
                bottom = window.length - 1 + (upperanchor - i - window.length);
            }

            for (int j = start; j <= end; j++) {

                for (int k = top; k <= bottom; k++) {

                    nmax += window[k] * matrix[j][upperanchor - i - k];

                }
            }

            if (nmax > max) {
                max = nmax;
                maxint = upperanchor - i - windowsize;
            }

            i++;

        }

//        System.out.println ("finished");
//        System.out.println ("start: " +  start + ", end: " +end);
//        System.out.println ("maxint: " + maxint);
        if (start - end == 0) {
            System.out.println(start + ":      maxint: " + maxint);
            ((XYChart.Data) midSeries.getData().get(start)).YValueProperty().setValue((maxint - 49) * 1.8);
        } else {

            //divide
            int middle = (start + end) / 2;
            int size = (int) (windowsize * 0.6);
            if (size < 0) {
                size = 0;
            }
            int r = (int) (range * 0.5);
//        if (r<3) {
//            r = 3;
//        }
            calculateAreas(matrix, start, middle, r, size, maxint);
            calculateAreas(matrix, middle + 1, end, r, size, maxint);
        }
    }

   

    public void showhideparam() throws IOException, InterruptedException {
        paramPane.setVisible(paramToggle.selectedProperty().get());
        stackpane.setVisible(!paramToggle.selectedProperty().get());
        if (!paramToggle.selectedProperty().get()) {
            paramToggle.setText("Change Parameters");
            animate(olist);
            paramToggle.setDisable(true);
        } else {
            paramToggle.setText("Click to Start");
            menu.setDisable(true);
        }
    }

    public boolean getParameters() {
        int count = 0;
        grav.setxRanges(new int[9]);
        grav.setyRanges(new int[9]);
        if (!x1.getText().isEmpty() && !y1.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x1.getText());
                grav.getyRanges()[count] = Integer.parseInt(y1.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x2.getText().isEmpty() && !y2.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x2.getText());
                grav.getyRanges()[count] = Integer.parseInt(y2.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x3.getText().isEmpty() && !y3.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x3.getText());
                grav.getyRanges()[count] = Integer.parseInt(y3.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x4.getText().isEmpty() && !y4.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x4.getText());
                grav.getyRanges()[count] = Integer.parseInt(y4.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x5.getText().isEmpty() && !y5.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x5.getText());
                grav.getyRanges()[count] = Integer.parseInt(y5.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x6.getText().isEmpty() && !y6.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x6.getText());
                grav.getyRanges()[count] = Integer.parseInt(y6.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x7.getText().isEmpty() && !y7.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x7.getText());
                grav.getyRanges()[count] = Integer.parseInt(y7.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x8.getText().isEmpty() && !y8.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x8.getText());
                grav.getyRanges()[count] = Integer.parseInt(y8.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!x9.getText().isEmpty() && !y9.getText().isEmpty()) {
            try {
                grav.getxRanges()[count] = Integer.parseInt(x9.getText());
                grav.getyRanges()[count] = Integer.parseInt(y9.getText());
                count++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the olist
     */
    public ObservableList<Entry> getOlist() {
        return olist;
    }

    /**
     * @param olist the olist to set
     */
    public void setOlist(ObservableList<Entry> olist) {
        this.olist = olist;
        Collections.sort(olist, new Entry.orderbyRT());
    }

    public void initializeparams() {

        if (grav.getxRanges()[0] > 0) {
            x1.setText(String.valueOf(grav.getxRanges()[0]));
            y1.setText(String.valueOf(grav.getyRanges()[0]));
        }

        if (grav.getxRanges()[1] > 0) {
            x2.setText(String.valueOf(grav.getxRanges()[1]));
            y2.setText(String.valueOf(grav.getyRanges()[1]));
        }
        if (grav.getxRanges()[2] > 0) {
            x3.setText(String.valueOf(grav.getxRanges()[2]));
            y3.setText(String.valueOf(grav.getyRanges()[2]));
        }
        if (grav.getxRanges()[3] > 0) {
            x4.setText(String.valueOf(grav.getxRanges()[3]));
            y4.setText(String.valueOf(grav.getyRanges()[3]));
        }
        if (grav.getxRanges()[4] > 0) {
            x5.setText(String.valueOf(grav.getxRanges()[4]));
            y5.setText(String.valueOf(grav.getyRanges()[4]));
        }
        if (grav.getxRanges()[5] > 0) {
            x6.setText(String.valueOf(grav.getxRanges()[5]));
            y6.setText(String.valueOf(grav.getyRanges()[5]));
        }
        if (grav.getxRanges()[6] > 0) {
            x7.setText(String.valueOf(grav.getxRanges()[6]));
            y7.setText(String.valueOf(grav.getyRanges()[6]));
        }
        if (grav.getxRanges()[7] > 0) {
            x8.setText(String.valueOf(grav.getxRanges()[7]));
            y8.setText(String.valueOf(grav.getyRanges()[7]));
        }
        if (grav.getxRanges()[8] > 0) {
            x9.setText(String.valueOf(grav.getxRanges()[8]));
            y9.setText(String.valueOf(grav.getyRanges()[8]));
        }

    }
    
   public void calculateFiles(List<RawDataFile> filelist) throws InterruptedException, IOException {
       
  
       int files = filelist.size();
       
       progress.setVisible(true);
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        setNodetoogroup((HashMap<Ellipse, TreeItem<Entry>>) new HashMap());
        CountDownLatch latch = new CountDownLatch(1);
        Task maintask = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {

                
                System.out.println("Files: " + files);
                int filesdone= 0;
                
                
                for (int d = 0; d < filelist.size(); d++) {
                    RawDataFile currentfile=filelist.get(d);
                    
                    

                                
                               
                                        float[][] matrix = new float[olist.size()][session.getResolution()];
                                        session.getIothread().lockFile(currentfile, true);
                                        double start = System.currentTimeMillis();
                                        int done =0;
                                        LinkedList<Integer> queue = new LinkedList<Integer>();
                                        //go trough and check if all are ready
                                        for (int i = 0; i < olist.size(); i++) {
                                            //if not ready, add to queue
                                            if (olist.get(i).isStored(currentfile)) {
                                                queue.add(i);
                                                session.getIothread().readOGroup(olist.get(i), currentfile);
                                                //if ready animate
                                            } else {
                                                olist.get(i).peakpickOGroup(currentfile);
                                                olist.get(i).getOGroupPropArraySmooth(currentfile, matrix, i);
                                                System.out.println(i + " of " + olist.size() + " OGroups calculated");
       done++;
       this.updateProgress(done+filesdone*olist.size(), olist.size()*files);
                                            }
                                        }
                                        System.out.println("Size of Queue: " + queue.size());
                                        //go through queue until it is empty
                                        double picktime = 0;
                                        while (queue.size() > 0) {
                                            int size = queue.size();
                                            for (int j = 0; j < size; j++) {
                                                Integer current = queue.pop();
                                                if (olist.get(current).isStored(currentfile)) {
                                                    
                                                    queue.add(current);
                                                } else {
                                                    double pick = System.currentTimeMillis();
                                                    olist.get(current).peakpickOGroup(currentfile);
                                                    picktime += System.currentTimeMillis() - pick;
                                                    olist.get(current).getOGroupPropArraySmooth(currentfile, matrix, current);
done++;
       this.updateProgress(done+filesdone*olist.size(), olist.size()*files);
                                                }
                                            }
                                            System.out.println("Size of Queue: " + queue.size());
                                        }

                                       
                                        System.out.println("Count down");
                                        System.out.println("Total peak picking time: " + picktime);
                                        System.out.println("Total time: " + (System.currentTimeMillis() - start));
                                        session.getIothread().lockFile(currentfile, false);

                                        
                                         //calculation
                                //calculateAreas(matrix, 0, list.size()-1, 49, 7, 49);
                                float[] centroids = new float[olist.size()];
                                
                                int count = 0;
                                while (count<9&&grav.getxRanges()[count] != 0) {
                                    centroids = grav.gravity(count, matrix, centroids);


                                    count++;

                                }
                          
//set fitted peak
                for (int i = 0; i<olist.size(); i++) {
                olist.get(i).setFittedShift(currentfile, centroids[i]);
                }
                                        
                                      
                                     filesdone++;  
                            
                      
                }

//                don't recalculate unless something changes
//                session.setPeakPickchanged(false);
                

            Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                           progress.progressProperty().unbind();
                                           progress.setProgress(-1d);

                                        }
                                    });
            
          
            showShift(olist);
            
            session.setPeakPickchanged(false);
             return null;   
            }

        };

        //new thread that executes maintask
        progress.progressProperty().bind(maintask.progressProperty());
        new Thread(maintask).start();
  
        
        
       
   }
   
   public void calculateBatch() throws InterruptedException, IOException {
       //disable all other Files
       Dataset current = session.getSelectedFiles().get(0).getDataset();
       List<RawDataFile> filelist = current.getListofFiles();
       
      
       //inactivate all other files (don't draw them)
       for (int i = 0; i<session.getAllFiles().size(); i++) {
           if (!filelist.contains(session.getAllFiles().get(i))) {
               session.getAllFiles().get(i).setActive(false);
           }
       }

       //reactivate selected file (gets deactivated because of selection)
       session.getSelectedFiles().get(0).setActive(true);
       
       calculateFiles(filelist);
       
   }
   
   public void calculateAllFiles() throws InterruptedException, IOException {
        calculateFiles(session.getAllFiles());
   }
   
   
}
