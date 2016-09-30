package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Peak;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import com.mycompany.fxmltableview.logic.Session;
import com.sun.webkit.ContextMenuItem;
import java.awt.Checkbox;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

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
    CheckMenuItem EICToggle, NEICToggle, MZToggle, ShiftToggle;
    
    @FXML
    ToggleButton EICMode, PeakMode, addPeak, weighttoggle, AlignToggle;
    
    @FXML
    ContextMenu contextMenu;
    
    @FXML
    Label nodatalabel;
    
    @FXML
    Slider weightslider;
    
    @FXML
    TextField weightfield;
    
    @FXML
    AnchorPane anchor;
    
    @FXML
    Button up, down;
           

    TreeTableView<Entry> metTable;
    private FXMLTableViewController mainController;
    ChartGenerator chartGenerator;
    Thread t;
    
    private Session session;
    private DropShadow hover = new DropShadow();
    private HashMap<RawDataFile, List<XYChart.Series>> filetoseries;
    private HashMap<XYChart.Series, RawDataFile> seriestofile;
    private HashMap<ChangeListener, Property> listeners;
    private HashMap<ListChangeListener, ObservableList> listlisteners;
    private HashMap<XYChart.Series, XYChart<Number,Number>> seriestochart;
    private HashMap<XYChart.Series, Peak> seriestopeak;
    private HashMap<Entry,List<XYChart<Number,Number>>> adducttochart;
    private HashMap<XYChart.Series, Entry> alignableseries;
    private Entry entry;
    private List<XYChart> charts;
    
    private boolean locked = false;
    private XYChart.Series selectedPeak;
    private Slice selectedSlice;
    private RawDataFile selectedFile;
    
     XYChart.Series line;
     double lineanchor;
    
    private float scroll;
    
    public int maxnumberofdrawnadducts;
    public int currentadduct;
    public int nextadduct;
    public int previousadduct;
    
    public TreeItem<Entry> OGroupItem;
    public List<Entry> listofadducts;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
        //add ChartGenerator
        chartGenerator = new ChartGenerator(this, null, null);
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        seriestochart = new HashMap<XYChart.Series, XYChart<Number,Number>>();
        setSeriestopeak(new HashMap<XYChart.Series, Peak>());
        charts = new ArrayList<XYChart>();
        alignableseries = new HashMap<>();
        adducttochart= new HashMap<Entry,List<XYChart<Number,Number>>>();
        hover.setColor(Color.LIME);
        hover.setSpread(1);
        hover.setRadius(1.8);
        listeners = new HashMap<ChangeListener, Property>();
        listlisteners = new HashMap<ListChangeListener, ObservableList>();
        progress.setOpacity(0.25);
        EICToggle.selectedProperty().setValue(true);
        NEICToggle.selectedProperty().setValue(true);
        MZToggle.selectedProperty().setValue(true);
        EICMode.selectedProperty().set(true);
        scrollPane.setContextMenu(null);
       
        
        weightfield.textProperty().bindBidirectional(weightslider.valueProperty(), new NumberStringConverter());
        
                                ChangeListener<Boolean> listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean prevselected, Boolean newselected) {
                if (!prevselected) {
                    weightslider.setVisible(true);
                } else {
                   if (!weightslider.focusedProperty().get()) {
                       weightslider.setVisible(false);
                   }
                }
            }
                        };
        
        
        weightfield.focusedProperty().addListener(listener);
        
    }

    //method that generates the graphs
    public void print(int first) {
        currentadduct=first;
        nextprev();
        anchor.setDisable(true);
        float start = System.currentTimeMillis();
        //new Maps, old Series are gone
        setFiletoseries((HashMap<RawDataFile, List<XYChart.Series>>) new HashMap());
        setSeriestofile((HashMap<XYChart.Series, RawDataFile>) new HashMap());
        seriestochart = new HashMap<>();
        setSeriestopeak(new HashMap<XYChart.Series, Peak>());
        adducttochart= new HashMap<>();
        
  
                
                //get selected Entry
        int adductnumber = first;
        if (first==-2) {
        adductnumber=0;
        
        
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
            entry = metTable.getSelectionModel().getSelectedItem().getValue().getOGroupObject();
            TreeItem<Entry> AdductItem = metTable.getSelectionModel().getSelectedItem();
            OGroupItem = metTable.getSelectionModel().getSelectedItem().getParent();
            adductnumber = OGroupItem.getChildren().indexOf(AdductItem);
            //System.out.println(adductnumber);
        } else {
            entry = metTable.getSelectionModel().getSelectedItem().getValue();
            OGroupItem = metTable.getSelectionModel().getSelectedItem();
        }
        
        if (listofadducts==null) {
            listofadducts=new ArrayList<>();
            for (int i = 0; i<OGroupItem.getChildren().size(); i++) {
                listofadducts.add(OGroupItem.getChildren().get(i).getValue());
            }
        }
        
        } else if (first==0) {
            if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
            entry = metTable.getSelectionModel().getSelectedItem().getValue().getOGroupObject();
            TreeItem<Entry> AdductItem = metTable.getSelectionModel().getSelectedItem();
            OGroupItem = metTable.getSelectionModel().getSelectedItem().getParent();
   
        } else {
            entry = metTable.getSelectionModel().getSelectedItem().getValue();
            OGroupItem = metTable.getSelectionModel().getSelectedItem();
        }
        
        if (listofadducts==null) {
            listofadducts=new ArrayList<>();
            for (int i = 0; i<OGroupItem.getChildren().size(); i++) {
                listofadducts.add(OGroupItem.getChildren().get(i).getValue());
            }
        }
            
        }
        
        //if (OGroupItem.getChildren().get(adductnumber).getValue().equals(listofadducts.get(adductnumber))) System.out.println("Success!");

        
        
        //delete previous graphs
        gridPane.getChildren().clear();
        
        progress.setVisible(true);
//        setScroll(((float) adductnumber) / (entry.getListofAdducts().size() - 1));
        
        currentadduct=adductnumber;
                
        
        Task task;
        task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
                nodatalabel.setVisible(false);
                //for every Adduct/Fragment
                int row=0;
                
                //read
                System.out.println("Adding Read OGroup from Adduct Print");
                session.getIothread().lockOGroup(entry, true);
                session.getIothread().readOGroup(entry);
                session.getIothread().clearnext();
                
                 TreeItem<Entry> next = metTable.getSelectionModel().getSelectedItem().nextSibling(OGroupItem);
                 TreeItem<Entry> prev = metTable.getSelectionModel().getSelectedItem().previousSibling(OGroupItem);
                for (int i = 0; i<4; i++) {
                     
                     if(next!=null) {
                         session.getIothread().nextogroup(next.getValue());
                         next = metTable.getSelectionModel().getSelectedItem().nextSibling(next);
                     }
                     if (prev!=null) {
                          session.getIothread().nextogroup(prev.getValue());
                         prev = metTable.getSelectionModel().getSelectedItem().previousSibling(prev);   
                     }
                     
                   
                   
                }
                
                boolean nodata = true;
                
                int numberofadducts = 0;
                int numberofdrawnadducts=0;
                for (int i = currentadduct; i < entry.getListofAdducts().size()&&numberofdrawnadducts<maxnumberofdrawnadducts; i++) {
                    numberofadducts++;
                   boolean empty = true;
                    Entry adduct = listofadducts.get(i);
                    
                     for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                if(adduct.getListofSlices().containsKey(currentfile)) {
                    empty = false;
                }
            }
        }
                    }
                     }
                     
                     if (!empty) {
                         numberofdrawnadducts++;
                         nodata=false;
                    System.out.println("Adding Read Adduct from Print");
                    session.getIothread().readAdduct(adduct);
                         
                    //gridPane.getColumnConstraints().add(new ColumnConstraints(350));
                    //Label showing the MZ
                    VBox box = new VBox();
                    box.setStyle("-fx-background-color: #E1E1E1");
                    box.setAlignment(Pos.CENTER_LEFT);
                    String MZ = Float.toString(adduct.getMZ()).concat("00000000000");
                    MZ = MZ.substring(0,MZ.indexOf(".")+5);
                    Label label = new Label(" MZ: ");
                    label.setFont(Font.font("Verdana",FontWeight.BOLD , 12));
                    box.getChildren().add(label);
                    Label vlabel = new Label(MZ);
                    formatLabel(vlabel);
                    box.getChildren().add(vlabel);
                    Label label2 = new Label(" Ion: ");
                    label2.setFont(Font.font("Verdana",FontWeight.BOLD , 12));
                    box.getChildren().add(label2);
                    Label vlabel2 = new Label(adduct.getIon());
                    formatLabel(vlabel2);
                    box.getChildren().add(vlabel2);
                    Label label3 = new Label(" Xn: ");
                    label3.setFont(Font.font("Verdana",FontWeight.BOLD , 12));
                    box.getChildren().add(label3);
                    Label vlabel3 = new Label(Integer.toString(adduct.getXn()));
                    formatLabel(vlabel3);
                    box.getChildren().add(vlabel3);
                    Label label4 = new Label(" Num:   ");
                    label4.setFont(Font.font("Verdana",FontWeight.BOLD , 12));
                    box.getChildren().add(label4);
                    Label vlabel4 = new Label(Integer.toString(adduct.getNum()));
                    formatLabel(vlabel4);
                    box.getChildren().add(vlabel4);
                    
                    //generate graphs
                    
                            addRow(row, box);
                            if(EICToggle.selectedProperty().get()) {
                            LineChartnoSymbol<Number, Number> linechart1 = chartGenerator.generateEIC(adduct);
                            addColumn(1, linechart1);
                            }
                            
                            if(NEICToggle.selectedProperty().get()) {
                                AreaChartnoSymbol<Number, Number> linechart2 = chartGenerator.generateNormalizedEICArea(adduct);
                                addColumn(2, linechart2);
                                charts.add(linechart2);
                                if (addPeak.selectedProperty().get()) {
                                addChartMouseEvents(linechart2);}
                            }
                            
                            
                            if (MZToggle.selectedProperty().get()) {
                            ScatterChart<Number, Number> scatterchart = chartGenerator.generateMassChart(adduct);
                            addColumn(3, scatterchart);
                            }
                            
                            if (ShiftToggle.selectedProperty().get()) {
                            LineChartnoSymbol<Number, Number> shiftchart = chartGenerator.generateShiftMap(adduct);
                            addColumn(4, shiftchart);
                                
                            }
                            
                            System.out.println("generated charts " + (i + 1) + " of " + entry.getListofAdducts().size());
                           updateProgress(i+1,entry.getListofAdducts().size());
                           row++;
                           
                           
                        } else {
                         
                     }
                             
                     
                }
                
//                for (int p = 0; p<numberofadducts; p++) {
//                    metTable.getSelectionModel().selectNext();
//                }
                
                if (currentadduct+numberofadducts+1<entry.getListofAdducts().size()) {
                    boolean empty = true;
                    for (int i = currentadduct+numberofadducts+1; i < entry.getListofAdducts().size(); i++) {
                   
                    Entry adduct = listofadducts.get(i);
                    
                     for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                if(adduct.getListofSlices().containsKey(currentfile)) {
                    empty = false;
                    break;
                }
            }
        }
        if (!empty) break; 
                    }
                     }
                     if (!empty) break; 
                    }
                    if (!empty) {
                        nextadduct = currentadduct+numberofadducts; 
                    down.setVisible(true);
                } else {
                    nextadduct = -1;
                    down.setVisible(false);
                }
                } else {
                    nextadduct = -1;
                    down.setVisible(false);
                }
                    
                    
                if (currentadduct>0) {
                    int count = 0;
                    int countd=0;
                    boolean empty = true;
                    for (int i = currentadduct-1; i > 0 && countd<maxnumberofdrawnadducts; i--) {
                        count++;
                   
                    Entry adduct = listofadducts.get(i);
                    
                     for (int d = 0; d<session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
        for (int f = 0; f < adduct.getSession().getListofDatasets().get(d).getListofFiles().size(); f++) {
            RawDataFile currentfile = adduct.getSession().getListofDatasets().get(d).getListofFiles().get(f);
            if (currentfile.getActive().booleanValue()) {
                if(adduct.getListofSlices().containsKey(currentfile)) {
                    empty = false;
                    countd++;
                    break;
                }
            }
        }
        if (countd==maxnumberofdrawnadducts) break;
        
                    }
                     }
                     if (countd==maxnumberofdrawnadducts) break;
                    }
                    if (countd>0) {
                    previousadduct = currentadduct-count; 
                    up.setVisible(true);
                } else {
                    previousadduct = -1;
                    up.setVisible(false);
                }
                } else {
                    previousadduct = -1;
                    up.setVisible(false);
                }
                
                nodatalabel.setVisible(nodata);
                
//                metTable.getSelectionModel().select(entry.getListofAdducts().get(currentadduct).treeitem);
                
                
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
                                        } else if (list.get(i).getNode().getClass().equals(Group.class)){
//                                Color c = new_val;
//                                double[] color = new double[] {c.getRed(), c.getGreen(), c.getBlue()};
//                                color = chartGenerator.brightencolor(color);
                                ((Path)((Group) list.get(i).getNode()).getChildren().get(0)).setStroke(file.getColor());
                                ((Path)((Group) list.get(i).getNode()).getChildren().get(0)).setFill(file.getColor().deriveColor(1, 1, 1, 0.15));
                                ((Path)((Group) list.get(i).getNode()).getChildren().get(1)).setStroke(file.getColor());
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
                                    } else if (list.get(i).getNode().getClass().equals(Group.class)){
                                
                                ((Path)((Group) list.get(i).getNode()).getChildren().get(0)).setStrokeWidth(file.getWidth());
                                ((Path)((Group) list.get(i).getNode()).getChildren().get(1)).setStrokeWidth(file.getWidth());
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
                
                for (XYChart.Series series:getSeriestopeak().keySet()) {
                    applyPeakMouseEvents(series);
                }
                
                if (EICMode.selectedProperty().get()) {
                    EICModeactivated();
                } else {
                    peakModeactivated();
                }
                if (weighttoggle.selectedProperty().get()) {
                    peakWeightMode();
                }
                
                if (addPeak.selectedProperty().get()) {
                peakPickMode();
                }
                         if (AlignToggle.selectedProperty().get()) {
             Align();
         }
                
                progress.setVisible(false);
                anchor.setDisable(false);
               Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scroll();
                        }});
                session.getIothread().lockOGroup(entry, false);
                return null;
            }
        };
        progress.progressProperty().bind(task.progressProperty());
        t = new Thread(task);
        t.start();
         float end = System.currentTimeMillis();
         
         
         

         
        System.out.println("Drawing time: " + (end-start) );
    }

    //select next metabolite, changes Selection in Main GUI
    public void next() {
        nextprev();
        listofadducts=null;
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
            metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().getParent());
        }
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().nextSibling());
        Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            metTable.getSelectionModel().getSelectedItem().nextSibling().getChildren().sort(metTable.getComparator());
                        }});
        up.setVisible(true);
        down.setVisible(true);
        print(0);

    }

    //select previous metablite, changes Selection in Main GUI
    public void previous() {
        nextprev();
        listofadducts=null;
        if (metTable.getSelectionModel().getSelectedItem().isLeaf()) {
            metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().getParent());
        }
        metTable.getSelectionModel().select(metTable.getSelectionModel().getSelectedItem().previousSibling());
        Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            metTable.getSelectionModel().getSelectedItem().previousSibling().getChildren().sort(metTable.getComparator());
                        }});
        up.setVisible(true);
        down.setVisible(true);
        print(0);

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
        maxnumberofdrawnadducts = session.maxnumberofdrawnfileadducts/session.getAllFiles().size();
        if (maxnumberofdrawnadducts<1) {
            maxnumberofdrawnadducts=1;
        }
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
                        if (list!=null){
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).getNode() == null) {
                                for (int k = 0; k < list.get(j).getData().size(); k++) {
                                    Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                    //node.setEffect(hover);
                                    ((Rectangle) node).setFill(Color.RED);
                                    node.toFront();

                                }
                                //System.out.println("Scatter colored Red");
                            } else if (list.get(j).getNode().getClass().equals(Group.class)){
                                ((Path)((Group) list.get(j).getNode()).getChildren().get(0)).setStroke(Color.RED);
                                ((Path)((Group) list.get(j).getNode()).getChildren().get(0)).setFill(Color.RED.deriveColor(1, 1, 1, 0.15));
                                ((Path)((Group) list.get(j).getNode()).getChildren().get(1)).setStroke(Color.RED);
                            } else {

                                Node node = list.get(j).getNode();
                                //node.setEffect(hover);
                                node.toFront();
                               
                                ((Path) node).setStroke(Color.RED);
                                //System.out.println("Line colored Red");
                            }
                        }}
                    } else {
                        List<XYChart.Series> list = filetoseries.get(completeList.get(i));
                        if (list!=null){
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).getNode() == null) {
                                for (int k = 0; k < list.get(j).getData().size(); k++) {
                                    Node node = ((XYChart.Data) list.get(j).getData().get(k)).getNode();
                                    //node.setEffect(hover);
                                    ((Rectangle) node).setFill(completeList.get(i).getColor());

                                }
                                //System.out.println("Scatter colored normal");
                            } else if (list.get(j).getNode().getClass().equals(Group.class)){
//                                Color c = completeList.get(i).getColor();
//                                double[] color = new double[] {c.getRed(), c.getGreen(), c.getBlue()};
//                                color = chartGenerator.brightencolor(color);
                                ((Path)((Group) list.get(j).getNode()).getChildren().get(0)).setStroke(completeList.get(i).getColor());
                                ((Path)((Group) list.get(j).getNode()).getChildren().get(0)).setFill(completeList.get(i).getColor().deriveColor(1, 1, 1, 0.15));
                                ((Path)((Group) list.get(j).getNode()).getChildren().get(1)).setStroke(completeList.get(i).getColor());
                            } else {
                                Node node = list.get(j).getNode();
                                //node.setEffect(hover);
                                
                                ((Path) node).setStroke(completeList.get(i).getColor());
                                //System.out.println("Line colored normal");
                            }}
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
                if (!locked) {
                ((Path)((Group) series.getNode()).getChildren().get(0)).setFill(Color.GREENYELLOW);
                
                selectedPeak = series;
                selectedSlice = charttoadduct(seriestochart.get(series)).getListofSlices().get(seriestofile.get(series));
                selectedFile = seriestofile.get(series);
                toggleContextMenu(true);
                
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
            }}
        });

        //hover effect
        node.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                if (!locked) {
               
                
          
                toggleContextMenu(false);
                
                RawDataFile file = getSeriestofile().get(series);
                
                if (file.isselected()) {
                    
                ((Path)((Group) series.getNode()).getChildren().get(0)).setFill(Color.RED.deriveColor(1, 1, 1, 0.15));
                } else {
//                double[] color = new double[] {file.getColor().getRed(),file.getColor().getGreen(),file.getColor().getBlue()};
//                color = chartGenerator.brightencolor(color);
                ((Path)((Group) series.getNode()).getChildren().get(0)).setFill(file.getColor().deriveColor(1, 1, 1, 0.15));
                }
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
            }}
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
        gridPane.getChildren().clear();
        t.interrupt();
        
        
        //delete all listeners
        for(Map.Entry<ChangeListener,Property> lis : listeners.entrySet()){
            lis.getValue().removeListener(lis.getKey());
        }
        for(Map.Entry<ListChangeListener,ObservableList> lis : listlisteners.entrySet()){
            lis.getValue().removeListener(lis.getKey());
        }
        nextprev();
    }
    
     public void nextprev() {
         
         
         
        //delete all nodes
        if (t!=null) {
t.interrupt();}
for (XYChart.Series series : seriestofile.keySet()) {
            series.getData().clear();
        }
for (XYChart chart : seriestochart.values()) {
            chart.getData().clear();
        }
        filetoseries.clear();
                
        
       
        seriestofile.clear();
        seriestochart.clear();
        getSeriestopeak().clear();
        adducttochart.clear();
        alignableseries.clear();
                
                
        charts.clear();
        gridPane.getChildren().clear();
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
     
     public void setScroll(float scroll) {
         this.scroll=scroll; 
     }
     
     public void scroll() {
         Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scrollPane.setVvalue(scroll);
                            //System.out.println("Set to " + scroll);
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
         for (XYChart chart:charts) {
           
             
             ((NumberAxis)chart.getYAxis()).setLowerBound(-0.05);
         }
         for (Series series:seriestopeak.keySet()) {

             ((Path) ((Group) series.getNode()).getChildren().get(0)).setVisible(true);
         }
         
         
         addPeak.setDisable(false);
         System.out.println("Peak Mode acitvated");
         
     }
     
     public void EICModeactivated() {
         for (XYChart chart:charts) {
           clearChartMouseEvents(chart);
             ((NumberAxis)chart.getYAxis()).setLowerBound(0);
         }
         
         for (Series series:seriestopeak.keySet()) {

             ((Path) ((Group) series.getNode()).getChildren().get(0)).setVisible(false);
         }
         addPeak.selectedProperty().set(false);
         peakPickMode();
         addPeak.setDisable(true);
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
if (event.isPrimaryButtonDown()) {
    Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
    double x = chart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
    double y = chart.getYAxis().sceneToLocal(mouseSceneCoords).getY();
    lineanchor = x;
    System.out.println("" +
        chart.getXAxis().getValueForDisplay(x) + ",  " +
        chart.getYAxis().getValueForDisplay(y)
    );
   
    line = new XYChart.Series();
    line.getData().add(new XYChart.Data(chart.getXAxis().getValueForDisplay(x),1.0f));
    line.getData().add(new XYChart.Data(chart.getXAxis().getValueForDisplay(x),1.0f));
    chart.getData().add(line);
    chart.applyCss();
    
    Group group = (Group) line.getNode();
    Path area = (Path) group.getChildren().get(0);
    Path invisible = (Path) group.getChildren().get(1);
    area.setStroke(Color.GREEN);
    area.setFill(Color.LIGHTGREEN.deriveColor(1, 1, 1, 0.3));
    area.setStrokeWidth(1.0);
    invisible.setVisible(false);
    
}});
        
chart.setOnMouseDragged((MouseEvent event) -> {
    if (event.isPrimaryButtonDown()) {

    Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
    double x = chart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
    double y = chart.getYAxis().sceneToLocal(mouseSceneCoords).getY();

    System.out.println("" +
        chart.getXAxis().getValueForDisplay(x) + ",  " +
        chart.getYAxis().getValueForDisplay(y)
    );
   
    
  if (x>lineanchor)  { 
  //System.out.println(((XYChart.Data)line.getData().get(1)).getXValue().getClass());
  
    ((XYChart.Data)line.getData().get(1)).setXValue(chart.getXAxis().getValueForDisplay(x));
    ((XYChart.Data)line.getData().get(0)).setXValue(chart.getXAxis().getValueForDisplay(lineanchor));
   // ((XYChart.Data)line.getData().get(1)).setYValue(chart.getYAxis().getValueForDisplay(y));
    } else {
      ((XYChart.Data)line.getData().get(0)).setXValue(chart.getXAxis().getValueForDisplay(x));
      ((XYChart.Data)line.getData().get(1)).setXValue(chart.getXAxis().getValueForDisplay(lineanchor));
  }
    
}});
       
chart.setOnMouseReleased((MouseEvent event) -> {

    
    
    double x1 = (double) ((XYChart.Data)line.getData().get(0)).getXValue();
    double x2 = (double) ((XYChart.Data)line.getData().get(1)).getXValue();
    
    if (x1!=x2) {
   
    Entry adduct = charttoadduct(chart);
    List<RawDataFile> list = session.getSelectedFiles();
    for (int i = 0; i<list.size(); i++) {
        try {
            XYChart.Series peakSeries = adduct.manualPeak(list.get(i), (float)x1, (float)x2);
            if (peakSeries!=null){
                chart.getData().add(peakSeries);
                chart.applyCss();
                if (list.get(i).isselected()) {
                                     chartGenerator.paintselectedLine(((Group) peakSeries.getNode()).getChildren().get(0));
                    ((Path)((Group) peakSeries.getNode()).getChildren().get(0)).setFill(Color.RED.deriveColor(1, 1, 1, 0.15));
                }else {
                ((Path)((Group) peakSeries.getNode()).getChildren().get(1)).setStroke(list.get(i).getColor());
                ((Path)((Group) peakSeries.getNode()).getChildren().get(0)).setStroke(list.get(i).getColor());
//                double[] color = new double[] {list.get(i).getColor().getRed(),list.get(i).getColor().getGreen(),list.get(i).getColor().getBlue()};
//                color = chartGenerator.brightencolor(color);
                ((Path)((Group) peakSeries.getNode()).getChildren().get(0)).setFill(list.get(i).getColor().deriveColor(1, 1, 1, 0.15));
                }
                ((Path)((Group) peakSeries.getNode()).getChildren().get(1)).setVisible(false);
                ((Path)((Group) peakSeries.getNode()).getChildren().get(1)).setStrokeWidth(list.get(i).getWidth());
                ((Path)((Group) peakSeries.getNode()).getChildren().get(0)).setStrokeWidth(list.get(i).getWidth());
                getSeriestopeak().put(peakSeries,adduct.getListofSlices().get(list.get(i)).getListofPeaks().get(adduct.getListofSlices().get(list.get(i)).getListofPeaks().size()-1));
                getSeriestofile().put(peakSeries, list.get(i));
                getSeriestochart().put(peakSeries, chart);
                getFiletoseries().get(list.get(i)).add(peakSeries);
                applyPeakMouseEvents(peakSeries);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Fxml_adductviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
   
    
 
    chart.getData().remove(line);
    line = null;
    
}
    else  {
        chart.getData().remove(line);
    line = null;
        
    }});
        
        
    }
    
    public void addChartWeightEvents(XYChart chart) {
        chart.setOnMousePressed((MouseEvent event) -> {
if (event.isPrimaryButtonDown()) {
    Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
    double x = chart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
    double y = chart.getYAxis().sceneToLocal(mouseSceneCoords).getY();
    lineanchor = x;
    System.out.println("" +
        chart.getXAxis().getValueForDisplay(x) + ",  " +
        chart.getYAxis().getValueForDisplay(y)
    );
   
    line = new XYChart.Series();
    line.getData().add(new XYChart.Data(chart.getXAxis().getValueForDisplay(x),1.0f));
    line.getData().add(new XYChart.Data(chart.getXAxis().getValueForDisplay(x),1.0f));
    chart.getData().add(line);
    chart.applyCss();
    
    Group group = (Group) line.getNode();
    Path area = (Path) group.getChildren().get(0);
    Path invisible = (Path) group.getChildren().get(1);
    area.setStroke(Color.GREEN);
    area.setFill(Color.LIGHTGREEN.deriveColor(1, 1, 1, 0.3));
    area.setStrokeWidth(1.0);
    invisible.setVisible(false);
    
}});
        
chart.setOnMouseDragged((MouseEvent event) -> {
    if (event.isPrimaryButtonDown()) {

    Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
    double x = chart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
    double y = chart.getYAxis().sceneToLocal(mouseSceneCoords).getY();

    System.out.println("" +
        chart.getXAxis().getValueForDisplay(x) + ",  " +
        chart.getYAxis().getValueForDisplay(y)
    );
   
    
    
    if (x>lineanchor)  { 
  //System.out.println(((XYChart.Data)line.getData().get(1)).getXValue().getClass());
  
    ((XYChart.Data)line.getData().get(1)).setXValue(chart.getXAxis().getValueForDisplay(x));
    ((XYChart.Data)line.getData().get(0)).setXValue(chart.getXAxis().getValueForDisplay(lineanchor));
   // ((XYChart.Data)line.getData().get(1)).setYValue(chart.getYAxis().getValueForDisplay(y));
    } else {
      ((XYChart.Data)line.getData().get(0)).setXValue(chart.getXAxis().getValueForDisplay(x));
      ((XYChart.Data)line.getData().get(1)).setXValue(chart.getXAxis().getValueForDisplay(lineanchor));
  }
    
    
}});
       
chart.setOnMouseReleased((MouseEvent event) -> {

    
    
    double x1 = (double) ((XYChart.Data)line.getData().get(0)).getXValue();
    double x2 = (double) ((XYChart.Data)line.getData().get(1)).getXValue();
    
    if (x1!=x2) {
   
    Entry adduct = charttoadduct(chart);
    List<RawDataFile> list = session.getSelectedFiles();
    for (int i = 0; i<list.size(); i++) {
        adduct.setPeakWeight(list.get(i),weightslider.getValue(), (float)x1, (float)x2);
    }
   
    
 
    chart.getData().remove(line);
    line = null;
    
}
    else  {
        chart.getData().remove(line);
    line = null;
        
    }});
        
        
    }
    
    public void clearChartMouseEvents(XYChart chart) {
        chart.setOnMousePressed(null);
        
chart.setOnMouseDragged(null);
       
chart.setOnMouseReleased(null);
        
        
    }
    
    public Entry charttoadduct(XYChart chart) {
        for (Map.Entry<Entry,List<XYChart<Number,Number>>> set :adducttochart.entrySet()){
            for (int i = 0; i<set.getValue().size(); i++) {
                if (set.getValue().get(i).equals(chart)) {
                    return set.getKey();
                }
            }
        }
        System.out.println("Error: entry not found");
        return null;
    }
    
    public void peakPickMode() {
        if (addPeak.selectedProperty().get()) {
            weighttoggle.selectedProperty().set(false);
        for (XYChart chart:charts) {
           
             addChartMouseEvents(chart);
            
         }
        } else {
            for (XYChart chart:charts) {
           
             clearChartMouseEvents(chart);
            
         }
        }
        
    }
    
    public void peakWeightMode() {
        if (weighttoggle.selectedProperty().get()) {
            addPeak.selectedProperty().set(false);
        for (XYChart chart:charts) {
           
             addChartWeightEvents(chart);
            
         }
        } else {
            for (XYChart chart:charts) {
           
             clearChartMouseEvents(chart);
            
         }
        }
        
    }

    /**
     * @return the seriestopeak
     */
    public HashMap<XYChart.Series, Peak> getSeriestopeak() {
        return seriestopeak;
    }

    /**
     * @param seriestopeak the seriestopeak to set
     */
    public void setSeriestopeak(HashMap<XYChart.Series, Peak> seriestopeak) {
        this.seriestopeak = seriestopeak;
    }
    
    public void toggleContextMenu(boolean toggle) {
        if (!toggle) {
        scrollPane.setContextMenu(null);
        } else {
       scrollPane.setContextMenu(contextMenu);
        }    
    }
    
    public void deletePeak() {
        if (selectedPeak!=null) {
        Peak peak = seriestopeak.get(selectedPeak);
        if (selectedSlice.getFittedPeak()!=null&&selectedSlice.getListofPeaks().indexOf(peak)==selectedSlice.getFittedpeak()) {
            selectedSlice.setFittedpeak(null);
        }
        selectedSlice.getListofPeaks().remove(peak);
        List<XYChart.Series> list = filetoseries.get(selectedFile);
        list.remove(selectedPeak);
        seriestofile.remove(selectedPeak);
        seriestochart.get(selectedPeak).getData().remove(selectedPeak);
        seriestochart.remove(selectedPeak);
        seriestopeak.remove(selectedPeak);     
    }}
    
    public void lock() {
        locked = true;
    }
    
    public void unlock() {
        locked = false;
    }
    
    public void showSlider() {
        weightslider.setVisible(true);
    }
    
    public void hideSlider() {
        weighttoggle.setSelected(true);
        weightslider.setVisible(false);
        weightslider.setValue(Math.round(weightslider.getValue()));
        peakWeightMode();
    }
    
    public void Align() {
        //if acitve
        if (AlignToggle.selectedProperty().get()) {
            for (Map.Entry<XYChart.Series,Entry> entry :alignableseries.entrySet()) {
                XYChart.Series series = entry.getKey();
                Entry adduct = entry.getValue();
                RawDataFile file = seriestofile.get(series);
                float shift=0;
                float mzshift = file.getMzshift();
                //if peak found
                if (adduct.getListofSlices().containsKey(file)&&adduct.getListofSlices().get(file).getFittedPeak()!=null) {
                    shift = adduct.getListofSlices().get(file).getFittedPeak().getIndexshift();
                    //if no peak found
                } else if (adduct.getOGroupObject().getOgroupShift().get(file)!=null) {
                    shift = adduct.getOGroupObject().getOgroupShift().get(file);
                }
                ObservableList<XYChart.Data> list = series.getData();
                for (XYChart.Data data : list) {
                    data.XValueProperty().setValue(((float)data.XValueProperty().getValue())-shift);
                
                    
                }
                
                XYChart chart = seriestochart.get(entry.getKey());
                if (chart instanceof ScatterChart) {
                    for (XYChart.Data data : list) {
                    data.YValueProperty().setValue(((float)data.YValueProperty().getValue())-mzshift);
                
                    
                }
                    
                }
                
                
                
            }
//            for (XYChart chart:charts) {
//                if (chart.getClass().equals(new ScatterChart<>(null,null))) {
//                    List<XYChart.Series<Number,Number>> series = chart.getData();
//                    for (XYChart.Series<Number,Number> s:series) {
//                        RawDataFile file = seriestofile.get(s);
//                        
//                        List<XYChart.Data<Number,Number>> points = s.getData();
//                        for (XYChart.Data<Number,Number> point:points) {
//                            point.
//                        }
//                    }
//                    
//                    List<XYChart.Data> points = ((XYChart.Series)chart.getData().get(0)).getData();
//                    for (i)
//                }
//                
//                
//                
//                
//                
//            }
//            
            
            //if not acitve
        }else {
            for (Map.Entry<XYChart.Series,Entry> entry :alignableseries.entrySet()) {
                XYChart.Series series = entry.getKey();
                Entry adduct = entry.getValue();
                RawDataFile file = seriestofile.get(series);
                float shift=0;
                float mzshift = file.getMzshift();
                //if peak found
                if (adduct.getListofSlices().containsKey(file)&&adduct.getListofSlices().get(file).getFittedPeak()!=null) {
                    shift = adduct.getListofSlices().get(file).getFittedPeak().getIndexshift();
                    //if no peak found
                 } else if (adduct.getOGroupObject().getOgroupShift().get(file)!=null) {
                    shift = adduct.getOGroupObject().getOgroupShift().get(file);
                }
                
                
                
                ObservableList<XYChart.Data> list = series.getData();
                for (XYChart.Data data : list) {
                    data.XValueProperty().setValue(((float)data.XValueProperty().getValue())+shift);
                
                    
                }
                
                  XYChart chart = seriestochart.get(entry.getKey());
                if (chart instanceof ScatterChart) {
                    for (XYChart.Data data : list) {
                    data.YValueProperty().setValue(((float)data.YValueProperty().getValue())+mzshift);
                
                    
                }
                    
                }
                
            }
            
        }
        
    }

    /**
     * @return the alignableseries
     */
    public HashMap<XYChart.Series, Entry> getAlignableseries() {
        return alignableseries;
    }

    /**
     * @param alignableseries the alignableseries to set
     */
    public void setAlignableseries(HashMap<XYChart.Series, Entry> alignableseries) {
        this.alignableseries = alignableseries;
    }

    /**
     * @return the alignablescatterseries
     */
    
public void formatLabel(Label label) {
    if (label.getText()==null) {
                        label.setText("?");
                    } else if (label.getText().length()>25) {
                        label.setTooltip(new Tooltip(label.getText()));
                        String newtext = label.getText().substring(0, 21) + "...";
                        label.setText(newtext);
                    }
    label.setText("  " + label.getText());
    
                    label.setMaxWidth(180);
                    label.setPrefWidth(180);
                    label.setMinWidth(180);
                    label.setMaxHeight(25);
                    label.setPrefHeight(25);
                    label.setMinHeight(25);
                    label.setAlignment(Pos.TOP_LEFT);
    
}

public void up() {
    
//    if (!metTable.getSelectionModel().getSelectedItem().isLeaf()) {
//        metTable.getSelectionModel().getSelectedItem().expandedProperty().set(true);
//    }
//    
//    
//    for (int i = 0; i<currentadduct-previousadduct; i++) {
//        metTable.getSelectionModel().selectPrevious();
//    }
//   metTable.getSelectionModel().select(previousadduct);
    up.setVisible(true);
        down.setVisible(true);
        print(previousadduct);
}

public void down() {
    
//    if (!metTable.getSelectionModel().getSelectedItem().isLeaf()) {
//        metTable.getSelectionModel().getSelectedItem().expandedProperty().set(true);
//    }
//    
//    for (int i = 0; i<nextadduct-currentadduct; i++) {
//        metTable.getSelectionModel().selectNext();
//    }
//    metTable.getSelectionModel().select(nextadduct);
    up.setVisible(true);
        down.setVisible(true); 
        print(nextadduct);
}
    
    public void defaultprint() {
        
        print(0);
    }
    
    public void showupdown() {
        up.setOpacity(0.8);
        down.setOpacity(0.8);
    }
    
    public void hideupdown() {
        up.setOpacity(0.3);
        down.setOpacity(0.3);
    }
    
    
   public void newpeaktest(XYChart.Series series) throws InterruptedException {
        XYChart chart = seriestochart.get(series);
        RawDataFile file = seriestofile.get(series);
        Entry adduct=null;
        for (Map.Entry<Entry,List<XYChart<Number,Number>>> entry:adducttochart.entrySet()) {
            if (entry.getValue().contains(chart)) {
                adduct = entry.getKey();
                break;
            }
            
        }
        
        Slice slice = adduct.getListofSlices().get(file);
        
        long start = System.nanoTime();
         float[] IntArray = slice.getIntArray().clone();
         float max = slice.getMaxIntensity();
         
//         //smoothing
//         float[] sint = new float[IntArray.length];
//         
//         for (int i = 0; i<3; i++) {
//             sint[0]=(IntArray[0]+IntArray[1])/2;
//             for (int j = 1; j<IntArray.length-1; j++) {
//                 sint[j]=(IntArray[j-1]+IntArray[j]+IntArray[j+1])/3;  
//             }
//             sint[IntArray.length-1]=(IntArray[IntArray.length-1]+IntArray[IntArray.length-2])/2;
//    IntArray = sint;
//         }
         
         
         
         
         
         
         
         float []smoothed = new float[IntArray.length];
         
     
         
        
        int number = 10;
        for (int i  = number; i<IntArray.length-number; i++) {
            smoothed[i]=IntArray[i]-(IntArray[i-number]+IntArray[i+number])/2;
        }
        
        boolean[] sign = new boolean[IntArray.length];
        for (int i  = 1; i<IntArray.length-1; i++) {
            sign[i]=true;
            if (smoothed[i]/IntArray[i]<0.2) {
                sign[i]=false;
                sign[i-1]=false;
                sign[i+1]=false;
                i++;
            }
        }
        
        
        
//  byte[] sign = new byte[IntArray.length];
//  number = 3;
//  boolean empty = true;
//  for (int i = number; i<IntArray.length-number; i++) {
//              boolean pos = false;
//              boolean same=true;
//              if (smoothed[i-number]>0) pos = true;
//              for (int j = i-number+1; j<i+number; j++) {
//                  if (pos&&smoothed[j]<0) {
//                      same = false;
//                      break;}
//                  else if (!pos&&smoothed[j]>0) {
//                      same = false;
//                      break;
//                  }
//              }
//              if (same) {
//                  empty=false;
//                  if (pos) {
//                      sign[i]=1;
//                  } else {
//                      sign[i]=-1;
//                  }
//              }
//  }
  
//      int a = -1;
//      int b = -1;
//      int c = -1;
//      boolean afound = false;
//      boolean bfound = false;
//  for (int i = 0; i<sign.length; i++) {
//     if (!afound) {
//         if (sign[i]<0) {
//             a=i;
//             afound = true;
//         } if (sign[i]==0) {
//             
//         } else {
//             a=0;
//             b=i;
//             afound = true;
//             bfound=true;
//         }
//     } else if (!bfound) {
//         if (sign[i]<0) {
//             a=i;
//         } else if (sign[i]==0) {
//             
//         } else {
//             b=i;
//         }
//         
//     } else {
//         
//     }
//     
//     
//  }
//  if (!empty) {
      if (slice.getListofPeaks()==null) {
         slice.setListofPeaks(new ArrayList<Peak>());}
        int a = -1;
        int b = -1;
        int c = -1;
        float sminv = Float.MAX_VALUE;
        float eminv = Float.MAX_VALUE;
        float maxv = Float.MIN_VALUE;
        boolean afound = false;
        boolean bfound = false;
        boolean cfound = false;
        
        int i = 0;
        while (i<smoothed.length) {
        while (a<0) {
            while (i<smoothed.length) {
            if (!sign[i]) {
                if (smoothed[i]<sminv) {
                    sminv=smoothed[i];
                    a=i;
                }
            } else {
                i++;
                break;
            }
            i++;
            }
        }
        b=i-1;
        maxv=smoothed[i-1];

        while (i<smoothed.length&&sign[i]) {
            if (smoothed[i]>maxv) {
                maxv=smoothed[i];
                b=i;
            }
            i++;
        }
        eminv=smoothed[i-1];
        c=i-1;
        while (i<smoothed.length&&!sign[i]) {
            if(smoothed[i]<eminv) {
                eminv=smoothed[i];
                c=i;
            }
            i++;
        }
        //new peak
     slice.addPeak(new Peak((short)b,(short)a,(short)c,slice));
     
        a=c;
        b=-1;
        c=-1;
        sminv = Float.MAX_VALUE;
        eminv = Float.MAX_VALUE;
        maxv = Float.MIN_VALUE;
        }


//  }
        
        //IntArray=slice.getIntArray();
         
         
         
         
         
//  float[] fd = new float[IntArray.length];
//        
//      
//        for (int i =0; i<IntArray.length; i++)  {
//            fd[i]=IntArray[i];
//            
//        }
//        
//        
//        float[] fds = new float[fd.length];
//        
//        
//    for (int s = 0; s<3; s++) {    
//        int number = 3;
//        float sum =0;
//        for (int i = 0; i<=1+number*2; i++) {
//            sum+=fd[i];
//        }
//        int n = number*2+1;
//
//        for (int i = number; i<fd.length-number-1; i++) {
//            fds[i]=sum/n;
//            
//            sum-=fd[i-number];
//            sum+=fd[i+number];
//        }
//        fds[fd.length-number]=sum/n;
//        fd=fds;
//    }   
//    
//    
//    byte[] trend = new byte[fd.length];
//    float k = fds[1]-fds[0];
//    float maxk = fds[1]+k*1.5f;
//    float mink = fds[1]+k*0.5f;
//    if (maxk<mink) {
//        float temp = maxk;
//        maxk = mink;
//        mink=temp;
//    }
//    for (int i = 1; i<fd.length; i++) {
//        if (fds[i]<mink) {
//            trend[i]=(byte) (trend[i-1]-1);
//            k = fds[i]-fds[i-1];
//            maxk = fds[i]+k*1.5f;
//            mink = fds[i]+k*0.5f;
//            if (maxk<mink) {
//        float temp = maxk;
//        maxk = mink;
//        mink=temp;
//    }
// 
//        } else if (fds[i]>maxk) {
//            trend[i]=(byte) (trend[i-1]+1);
//            k = fds[i]-fds[i-1];
//            maxk = fds[i]+k*1.5f;
//            mink = fds[i]+k*0.5f;
//            if (maxk<mink) {
//        float temp = maxk;
//        maxk = mink;
//        mink=temp;
//    }
//        } else {
//            trend[i]=trend[i-1];
//            maxk+=k;
//            mink+=k;
//        }
//        
//    }
       
   
         
         
         
         
         
//         //line
//         //------------------------------------------------------------------
//        
//         
//      
//
//        float[] minmax = new float[IntArray.length];
//        int number = 25;
//        
//        for (int i = 0; i<minmax.length-number; i++) {
////        float k = (IntArray[i+number]-IntArray[i])/number;
////        float linev=IntArray[i];
////        int ma= -1;
////        float mav=1000;
////        int mi = -1;
////        float miv=-1000;
////            for (int j = i+1; j<i+number; j++) {
////                linev+=k;
////                float val = IntArray[j]-linev;
////                if (val > 0) {
////                    if (val > linev * 0.1) {
////                        if (val > mav) {
////                            mav = val;
////                            ma = j;
////                        }
////                    }
////                } else if (val < linev * -0.1) {
////                    if (val < miv) {
////                        miv = val;
////                        mi = j;
////                    }
////                } 
////             
////            }
////            if (ma>=0) minmax[ma]++;
////            if (mi>=0) minmax[mi]--;
//
//float val = (IntArray[i+number]+IntArray[i])/2;
//float dif = IntArray[i+number/2]-val;
//minmax[i+number/2]=dif;
//
//        }
//        
////        IntArray = slice.getIntArray();
////        for (int i = 1; i<minmax.length-1; i++) {
////            if (minmax[i]>0) {
////                if (IntArray[i]<1000||IntArray[i-1]==0||IntArray[i+1]==0) {
////                    minmax[i]=0;
////                }
////            }
////            
////        }
        
        
        
        
        //end line
        //------------------------------------------------------------
        
        
        //higher-lower//
        //-----------------------------------------------------------
        
//        float[] IntArray = slice.getIntArray();
//        float max = slice.getMaxIntensity();
//        
//        float[] fd = new float[IntArray.length];
//        
//        float fdmax=0;
//        for (int i =0; i<IntArray.length-1; i++)  {
//            fd[i]=IntArray[i+1]-IntArray[i];
//            if (fd[i]>fdmax) fdmax=fd[i];
//        }
//        
//        
//        float[] fds = new float[fd.length];
//        
//        
//    for (int s = 0; s<3; s++) {    
//        int number = 3;
//        float sum =0;
//        for (int i = 0; i<=1+number*2; i++) {
//            sum+=fd[i];
//        }
//        int n = number*2+1;
//
//        for (int i = number; i<fd.length-number-1; i++) {
//            fds[i]=sum/n;
//            
//            sum-=fd[i-number];
//            sum+=fd[i+number];
//        }
//        fds[fd.length-number]=sum/n;
//        fd=fds;
//    }   
//        
//        int[] sign = new int[fd.length];
//        
//        for (int i = 0; i<fd.length; i++) {
//            if (fds[i]>0) {
//                sign[i]=1;
//            } else if (fds[i]<0) {
//                sign[i]=-1;
//            }
//        }
//        
//        int[] hl = new int[fd.length];
//        for (int i = 1; i< fd.length; i++) {
//            if (fds[i]>=fds[i-1]) {
//                hl[i]=1;
//                
//            } else {
//                hl[i]=-1;
//            }
//        }

        //end higher-lower//
        //-----------------------------------------------------------
        
        System.out.println(System.nanoTime()-start);
        
        
//        byte[] slope = new byte[IntArray.length];
//        
//        for (int i = 1; i<slope.length-2; i++) {
//           
//            if (IntArray[i-1]>IntArray[i]&&IntArray[i+1]<IntArray[i]) {
//                slope[i]=-1;
//            } else if (IntArray[i-1]<IntArray[i]&&IntArray[i+1]>IntArray[i]) {
//                slope[i]=1;
//            }
//        }
        
        
        
        
        Stage stage = new Stage();
        stage.setTitle("Peak Test");
        //Basic Chart attributes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("RT [minutes]");
        yAxis.setLabel("Intensity");
        
        //linechart.getData().clear();
        
        AreaChartnoSymbol<Number, Number> linechart = new AreaChartnoSymbol(xAxis, yAxis);

                XYChart.Series newSeries = new XYChart.Series();

                List<XYChart.Data> list = new ArrayList<>();
                //just fill the chart with data points
                for (int j = 0; j < IntArray.length; j++) {
                    float intensity = IntArray[j]/max;
                    float currentRT = j;

                    list.add(new XYChart.Data(currentRT, intensity));
                }
                newSeries.getData().addAll(list);

                // add new Series
                linechart.getData().add(newSeries);
                
                XYChart.Series newSeries2 = new XYChart.Series();
                IntArray=slice.getIntArray();

                List<XYChart.Data> list2 = new ArrayList<>();
                //just fill the chart with data points
                for (int j = 0; j < IntArray.length; j++) {
                    float intensity = 0;
                    if(sign[j]) {
                        intensity = 1;
                    }
                    float currentRT = j;

                    list2.add(new XYChart.Data(currentRT, intensity));
                }
                newSeries2.getData().addAll(list2);

                // add new Series
                linechart.getData().add(newSeries2);
                

        Scene scene  = new Scene(linechart,800,600);       
        System.out.println("est02");

        stage.setScene(scene);
//        linechart.applyCss();
//        for (Object data: newSeries.getData()) {
//            ((XYChart.Data)data).getNode().setDisable(true);
//            ((XYChart.Data)data).getNode().setVisible(false);
//        }
        stage.show();
        
        
        
        
        
        
    }
   
   
    
   
}
