package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.logic.Session;
import com.sun.webkit.ContextMenuItem;
import java.awt.Checkbox;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

/**
 * FXML Controller class
 *
 * Controller for the Adduct GUI
 *
 * @author stefankoch
 */
public class Fxml_adductviewController implements Initializable {

    //Gridpane holding all the graphs
    @FXML
    GridPane gridPane;

    @FXML
    ScrollPane scrollPane;
    
    @FXML
    ProgressBar progress;
    
    @FXML
    CheckMenuItem EICToggle, NEICToggle, MZToggle;
    
    @FXML
    ToggleButton EICMode, PeakMode;

    TreeTableView<Entry> metTable;
    private FXMLTableViewController mainController;
    ChartGenerator chartGenerator;
    boolean showProp;
    private Session session;
    private DropShadow hover = new DropShadow();
    private HashMap<RawDataFile, List<XYChart.Series>> filetoseries;
    private HashMap<XYChart.Series, RawDataFile> seriestofile;
    private HashMap<ChangeListener, Property> listeners;
    private HashMap<ListChangeListener, ObservableList> listlisteners;
    private HashMap<XYChart.Series, XYChart<Number,Number>> seriestochart;
    private List<XYChart.Series> peakseries;
    private HashMap<Entry,List<XYChart<Number,Number>>> adducttochart;
    
     XYChart.Series line;
    
    private double scroll;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //add ChartGenerator
        chartGenerator = new ChartGenerator(this, null);
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        seriestochart = new HashMap<XYChart.Series, XYChart<Number,Number>>();
        peakseries = new ArrayList<XYChart.Series>();
        adducttochart= new HashMap<Entry,List<XYChart<Number,Number>>>();
        hover.setColor(Color.LIME);
        hover.setSpread(1);
        hover.setRadius(1.8);
        listeners = new HashMap<ChangeListener, Property>();
        listlisteners = new HashMap<ListChangeListener, ObservableList>();
        progress.setOpacity(0.15);
        EICToggle.selectedProperty().setValue(true);
        NEICToggle.selectedProperty().setValue(true);
        MZToggle.selectedProperty().setValue(true);
        EICMode.selectedProperty().set(true);
        
        
    }

    //method that generates the graphs
    public void print() {
        double start = System.currentTimeMillis();
        //new Maps, old Series are gone
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        seriestochart = new HashMap<>();
        peakseries = new ArrayList<>();
        adducttochart= new HashMap<>();
        
        

        
                
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
            OGroupItem = metTable.getSelectionModel().getSelectedItem();
        }

        //delete previous graphs
        gridPane.getChildren().clear();
        
        progress.setVisible(true);
        setScroll(((double) adductnumber) / (entry.getListofAdducts().size() - 1));
        
        
                
        
        Task task;
        task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
                //for every Adduct/Fragment
                
                for (int i = 0; i < entry.getListofAdducts().size(); i++) {
                   
                    Entry adduct = OGroupItem.getChildren().get(i).getValue();
                    
                    //Label showing the MZ
                    VBox box = new VBox();
                    String MZ = Double.toString(adduct.getMZ());
                    MZ = MZ.substring(0,MZ.indexOf(".")+5);
                    Label label = new Label("MZ: " + MZ);
                    box.getChildren().add(label);
                    Label label2 = new Label("Ion: " + adduct.getIon());
                    box.getChildren().add(label2);
                    Label label3 = new Label("Xn: " + Double.toString(adduct.getXn()));
                    box.getChildren().add(label3);
                    
                    //generate graphs
                    
                            addRow(i, box);
                            if(EICToggle.selectedProperty().get()) {
                            LineChart<Number, Number> linechart1 = chartGenerator.generateEIC(adduct);
                            addColumn(1, linechart1);
                            }
                            
                            if(NEICToggle.selectedProperty().get()) {
                            if (showProp) {
                                LineChart<Number, Number> linechart2 = chartGenerator.generateNormalizedEICwithProp(adduct);
                                addColumn(2, linechart2);
                            } else {
                                LineChart<Number, Number> linechart2 = chartGenerator.generateNormalizedEIC(adduct);
                                addColumn(2, linechart2);
                                addChartMouseEvents(linechart2);
                            }}
                            
                            
                            if (MZToggle.selectedProperty().get()) {
                            ScatterChart<Number, Number> scatterchart = chartGenerator.generateMassChart(adduct);
                            addColumn(3, scatterchart);
                            }
                            
                            System.out.println("generated charts " + (i + 1) + " of " + entry.getListofAdducts().size());
                           updateProgress(i+1,entry.getListofAdducts().size());
                        }
                   
                 
                
                
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
                                        if (list.get(i).getNode() == null) {
                                            for (int k = 0; k < list.get(i).getData().size(); k++) {
                                                Node node = ((XYChart.Data) list.get(i).getData().get(k)).getNode();
                                                
                                                ((Rectangle) node).setFill(new_val);
                                                
                                            }
                                        } else {
                                            
                                            Node node = list.get(i).getNode();
                                            ((Path) node).setStroke(new_val);
                                        }
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
                                
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getNode() == null) {
                                        for (int k = 0; k < list.get(i).getData().size(); k++) {
                                            Node node = ((XYChart.Data) list.get(i).getData().get(k)).getNode();
                                            
                                            ((Rectangle) node).setHeight(file.getWidth() + 1.5);
                                            ((Rectangle) node).setWidth(file.getWidth() + 1.5);
                                            
                                        }
                                    } else {
                                        
                                        Node node = list.get(i).getNode();
                                        ((Path) node).setStrokeWidth(file.getWidth());
                                        
                                    }
                                }
                                
                            }
                        };
                        
                        file.getWidthProperty().addListener(listener2);
                        listeners.put(listener2, file.getWidthProperty());
                        
                    }
               
                }
                
                Set<XYChart.Series> set = seriestofile.keySet();
                for (XYChart.Series series : set) {
                    applyMouseEvents(series);
                }
                
                for (XYChart.Series series:peakseries) {
                    applyPeakMouseEvents(series);
                }
                
                if (EICMode.selectedProperty().get()) {
                    EICModeactivated();
                } else {
                    peakModeactivated();
                }
                
                progress.setVisible(false);
               Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scroll();
                        }});
                return null;
            }
        };
        progress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
        

         double end = System.currentTimeMillis();
         
        System.out.println("Drawing time: " + (end-start) );
    }

    //select next metabolite, changes Selection in Main GUI
    public void next() {
        nextprev();
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
            metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().getParent());
        }
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().nextSibling());
        print();

    }

    //select previous metablite, changes Selection in Main GUI
    public void previous() {
        nextprev();
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
            metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().getParent());
        }
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().previousSibling());
        print();

    }

    public void setShowProp() {
        nextprev();
        showProp = !showProp;
        print();

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
        this.chartGenerator.setSession(session);
    }

    /**
     * @return the mainController
     */
    public FXMLTableViewController getMainController() {
        return mainController;
    }

    /**
     * @param mainController the mainController to set
     */
    public void setMainController(FXMLTableViewController mainController) {
        this.mainController = mainController;

        //Colors selected files in Adductview, reacts to selection
        ListChangeListener listener = new ListChangeListener<RawDataFile>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends RawDataFile> change) {
                //to ensure it doesn't run before selectedFiles has been updated
                 Platform.runLater(new Runnable() {
            @Override
            public void run() {
           
                List<RawDataFile> completeList = mainController.session.getAllFiles();

                for (int i = 0; i < completeList.size(); i++) {
                    if (completeList.get(i).isselected()) {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).getNode() == null) {
                                for (int k = 0; k < list.get(j).getData().size(); k++) {
                                    Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                    //node.setEffect(hover);
                                    ((Rectangle) node).setFill(Color.RED);
                                    node.toFront();

                                }
                                System.out.println("Scatter colored Red");
                            } else {

                                Node node = list.get(j).getNode();
                                //node.setEffect(hover);
                                node.toFront();
                               
                                ((Path) node).setStroke(Color.RED);
                                System.out.println("Line colored Red");
                            }
                        }
                    } else {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).getNode() == null) {
                                for (int k = 0; k < list.get(j).getData().size(); k++) {
                                    Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                    //node.setEffect(hover);
                                    ((Rectangle) node).setFill(completeList.get(i).getColor());

                                }
                                System.out.println("Scatter colored normal");
                            } else {
                                Node node = list.get(j).getNode();
                                //node.setEffect(hover);
                                
                                ((Path) node).setStroke(completeList.get(i).getColor());
                                System.out.println("Line colored normal");
                            }
                        }

                    }
                }
                
                     
            }
        });
            }

        };
        for (int i = 0; i<mainController.session.getListofDatasets().size(); i++) {
            BatchController controller = mainController.getDatasettocontroller().get(mainController.session.getListofDatasets().get(i));
            controller.getBatchFileView().getSelectionModel().getSelectedItems().addListener(listener);
            listlisteners.put(listener, controller.getBatchFileView().getSelectionModel().getSelectedItems());
        }
        

    }

    private void applyMouseEvents(final XYChart.Series series) {
        if (series.getNode() != null) {
            Node node = series.getNode();


            //hover effect
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
                        node.setEffect(hover);
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

        //hover effect
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
                        node.setEffect(null);
                        //((Rectangle)node).setFill(Color.RED);
                        
                    } }else {
                    
                    Node node = list.get(i).getNode();
                    node.setEffect(null);
                node.setCursor(Cursor.DEFAULT);
                //((Path) node).setStroke(Color.RED);
                }}
            }
        });

        //select file when clicked
            node.setOnMouseReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                RawDataFile file = getSeriestofile().get(series);
                                List<XYChart.Series> list = getFiletoseries().get(file);
                                BatchController controller = getMainController().getDatasettocontroller().get(file.getDataset());
                                if (getMainController().session.getSelectedFiles().contains(file)) {
                                    
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
            });
        }
    }
    
        private void applyPeakMouseEvents(final XYChart.Series series) {
        if (series.getNode() != null) {
            Node node = series.getNode();


            //hover effect
        node.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                ((XYChart.Data)series.getData().get(0)).setYValue(0);
                ((XYChart.Data)series.getData().get(3)).setYValue(0);
                ((XYChart.Data)series.getData().get(6)).setYValue(0);
                
                RawDataFile file = getSeriestofile().get(series);
                List<XYChart.Series> list = getFiletoseries().get(file);
                
                for (int i = 0; i<list.size(); i++) {
                    //if series is masschart
                    if (list.get(i).getNode() == null) {
                        for (int j = 0; j<list.get(i).getData().size(); j++) {
                        Node node = (( XYChart.Data)list.get(i).getData().get(j)).getNode();
                        node.setEffect(hover);
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

        //hover effect
        node.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                
                ((XYChart.Data)series.getData().get(0)).setYValue(1.13);
                ((XYChart.Data)series.getData().get(3)).setYValue(1.05);
                ((XYChart.Data)series.getData().get(6)).setYValue(1.13);
                
                
                RawDataFile file = getSeriestofile().get(series);
                List<XYChart.Series> list = getFiletoseries().get(file);
                
                for (int i = 0; i<list.size(); i++) {
                    //if series is masschart
                    if (list.get(i).getNode() == null) {
                        for (int j = 0; j<list.get(i).getData().size(); j++) {
                        Node node = (( XYChart.Data)list.get(i).getData().get(j)).getNode();
                        node.setEffect(null);
                        //((Rectangle)node).setFill(Color.RED);
                        
                    } }else {
                    
                    Node node = list.get(i).getNode();
                    node.setEffect(null);
                node.setCursor(Cursor.DEFAULT);
                //((Path) node).setStroke(Color.RED);
                }}
            }
        });

        //select file when clicked
            node.setOnMouseReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                RawDataFile file = getSeriestofile().get(series);
                                List<XYChart.Series> list = getFiletoseries().get(file);
                                BatchController controller = getMainController().getDatasettocontroller().get(file.getDataset());
                                if (getMainController().session.getSelectedFiles().contains(file)) {
                                    
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
            });
        }
    }
        
    private void unapplyMouseEvents(final XYChart.Series series) {
    if (series.getNode() != null) {
            Node node = series.getNode();


            //hover effect
        node.setOnMouseEntered(null);

        //hover effect
        node.setOnMouseExited(null);

        //select file when clicked
            node.setOnMouseReleased(null);
        }
    }
    
    public void close() {
        //delete all nodes
        for(XYChart.Series ser : seriestofile.keySet()) {
            ser = null;
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
    
     public void nextprev() {
        //delete all nodes
        for(XYChart.Series ser : seriestofile.keySet()) {
            ser = null;
        }
        
        //delete all listeners
        for(Map.Entry<ChangeListener,Property> lis : listeners.entrySet()){
            lis.getValue().removeListener(lis.getKey());
        }
        System.gc();
    }

     
     public void addRow (int i, VBox box ) {
         Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            gridPane.addRow(i, box);
                        }});
         
     }
     
     public void addColumn (int i, XYChart<Number,Number> chart ) {
          Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            gridPane.addColumn(i, chart);
                        }});
         
     }
     
     public void setScroll(double scroll) {
         this.scroll=scroll; 
     }
     
     public void scroll() {
         Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scrollPane.setVvalue(scroll);
                            System.out.println("Set to " + scroll);
                        }});
                            
                       
         
     }
     
     //gets called when PeakMode is clicked
     public void toggleEIC() {
         if (PeakMode.selectedProperty().get()) {
             EICMode.selectedProperty().set(false);
             peakModeactivated();
         } else {
             EICMode.selectedProperty().set(true);
             EICModeactivated();
         }
         
         
     }
     
     //gets called when EICMode is clicked
     public void togglePeak() {
         if (EICMode.selectedProperty().get()) {
             PeakMode.selectedProperty().set(false);
             EICModeactivated();
         } else {
             PeakMode.selectedProperty().set(true);
             peakModeactivated();
         }
         
     }
     
     
     public void peakModeactivated() {
         for (XYChart.Series series:peakseries) {
             
             ((NumberAxis)seriestochart.get(series).getYAxis()).setUpperBound(1.2);
         }
         
         
         System.out.println("Peak Mode acitvated");
         
     }
     
     public void EICModeactivated() {
         for (XYChart.Series series:peakseries) {
           
             ((NumberAxis)seriestochart.get(series).getYAxis()).setUpperBound(1.0);
         }
         System.out.println("EIC Mode acitvated");
     }

    /**
     * @return the seriestochart
     */
    public HashMap<XYChart.Series, XYChart<Number,Number>> getSeriestochart() {
        return seriestochart;
    }

    /**
     * @param seriestochart the seriestochart to set
     */
    public void setSeriestochart(HashMap<XYChart.Series, XYChart<Number,Number>> seriestochart) {
        this.seriestochart = seriestochart;
    }

    /**
     * @return the peakseries
     */
    public List<XYChart.Series> getPeakseries() {
        return peakseries;
    }

    /**
     * @param peakseries the peakseries to set
     */
    public void setPeakseries(List<XYChart.Series> peakseries) {
        this.peakseries = peakseries;
    }

    /**
     * @return the adducttochart
     */
    public HashMap<Entry,List<XYChart<Number,Number>>> getAdducttochart() {
        return adducttochart;
    }

    /**
     * @param adducttochart the adducttochart to set
     */
    public void setAdducttochart(HashMap<Entry,List<XYChart<Number,Number>>> adducttochart) {
        this.adducttochart = adducttochart;
    }
     
    
    public boolean isPeakMode() {
        if (PeakMode.selectedProperty().get()) {
            return true;
        }
        return false;
    }
     
    public void addChartMouseEvents(XYChart chart) {
        chart.setOnMousePressed((MouseEvent event) -> {

    Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
    double x = chart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
    double y = chart.getYAxis().sceneToLocal(mouseSceneCoords).getY();

    System.out.println("" +
        chart.getXAxis().getValueForDisplay(x) + ",  " +
        chart.getYAxis().getValueForDisplay(y)
    );
   
    line = new XYChart.Series();
    line.getData().add(new XYChart.Data(chart.getXAxis().getValueForDisplay(x),chart.getYAxis().getValueForDisplay(y)));
    line.getData().add(new XYChart.Data(chart.getXAxis().getValueForDisplay(x),chart.getYAxis().getValueForDisplay(y)));
    chart.getData().add(line);
    chart.applyCss();
    
    ((Path) line.getNode()).setStroke(Color.GREEN);
    ((Path) line.getNode()).setStrokeWidth(2.0);
    ((Path) line.getNode()).setOpacity(1.0);
    
});
        
chart.setOnMouseDragged((MouseEvent event) -> {

    Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
    double x = chart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
    double y = chart.getYAxis().sceneToLocal(mouseSceneCoords).getY();

    System.out.println("" +
        chart.getXAxis().getValueForDisplay(x) + ",  " +
        chart.getYAxis().getValueForDisplay(y)
    );
   
    
    ((XYChart.Data)line.getData().get(1)).setXValue(chart.getXAxis().getValueForDisplay(x));
    ((XYChart.Data)line.getData().get(1)).setYValue(chart.getYAxis().getValueForDisplay(y));
    
    
});
       
chart.setOnMouseReleased((MouseEvent event) -> {

    
    
    double x1 = (double) ((XYChart.Data)line.getData().get(0)).getXValue();
    double y1 = (double) ((XYChart.Data)line.getData().get(0)).getYValue();
    double x2 = (double) ((XYChart.Data)line.getData().get(1)).getXValue();
    double y2 = (double) ((XYChart.Data)line.getData().get(1)).getYValue();
    
    XYChart.Series newSeries = new XYChart.Series();
    newSeries.getData().add(new XYChart.Data(x1,1.13));
    newSeries.getData().add(new XYChart.Data(x1,1.19));
    newSeries.getData().add(new XYChart.Data((x1+x2)/2,1.19));
    newSeries.getData().add(new XYChart.Data((x1+x2)/2,1.05));
    newSeries.getData().add(new XYChart.Data((x1+x2)/2,1.19));
    newSeries.getData().add(new XYChart.Data(x2,1.19));
    newSeries.getData().add(new XYChart.Data(x2,1.13));
    
    
    chart.getData().add(newSeries);
    
    chart.applyCss();
    
    ((Path) newSeries.getNode()).setStroke(Color.GREEN);
    ((Path) newSeries.getNode()).setStrokeWidth(2.0);
    ((Path) newSeries.getNode()).setOpacity(1.0);
    
    ((NumberAxis)chart.getYAxis()).setUpperBound(1.2);
    chart.getData().remove(line);
    line = null;
    
});
        
        
    }
    
}
