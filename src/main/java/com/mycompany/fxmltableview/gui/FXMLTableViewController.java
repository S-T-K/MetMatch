package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Batch;
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry.orderbyRT;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Reference;
import com.mycompany.fxmltableview.logic.CertaintyCalculator;
import com.mycompany.fxmltableview.logic.IOThread;
import com.mycompany.fxmltableview.logic.Session;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.io.FileUtils;
import org.jfree.fx.FXGraphics2D;

//this is the Controller for the Main GUI
public class FXMLTableViewController implements Initializable {

    //link fxml information to controller
    @FXML
    private TreeTableView<Entry> metTable;

    @FXML
    TreeTableColumn nameColumn;

    @FXML
    TreeTableColumn numColumn, scoreColumn, scorepeakfoundColumn, scorepeakcloseColumn, scorecertaintyColumn;

    @FXML
    TreeTableColumn rtColumn;

    @FXML
    TreeTableColumn mzColumn;

 

    @FXML
    private Accordion accordion;

    @FXML
    Button addBatchButton, paramButton;

    @FXML
    ProgressBar progressbar;

    @FXML
    TextField RTTol, MZTol, SliceMZTol, Res, Base, RTTolShift, AdName1, AdName2, AdName3, AdName4, AdName5, AdName6, AdName7, AdMass1, AdMass2, AdMass3, AdMass4, AdMass5, AdMass6, AdMass7;

    @FXML
    Label label1, label2, label3, label4, label5, label6, label7, label8, label9, label10, label11;

    @FXML
    Rectangle box1, box2, box3, box4;

    @FXML
    ChoiceBox PeakPick;
    
    @FXML
    CheckBox toggleadductgeneration;

    @FXML
    MenuItem paramMenu, shift, output;

    @FXML
    TabPane TabPane;

    //Check for changed parameters
    String oldPick;
    String oldBase;
    String oldRT;

    //List with MasterListofOGroups for table, Ogroups (adducts within the Ogroups)
    private ObservableList<Entry> MasterListofOGroups;

    //current session, storing all information
    Session session;
    FXGraphics2D test;
    HashMap<TitledPane, Dataset> panelink;
    private HashMap<Dataset, BatchController> datasettocontroller;

    //number of current batches, as an index
    int batchcount;

    //max number of adducts in Input Matrix
    int maxnumber;

    //initialize the table, and various elements
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //set Factories for the tables
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, String>("OGroup"));  //String in brackets has to be the same as PropertyValueFactory property= "..." in fxml
        scoreColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Float>("Score"));
        scorepeakfoundColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Float>("Scorepeakfound"));
        scorepeakcloseColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Float>("Scorepeakclose"));
        scorecertaintyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Float>("Scorecertainty"));
        numColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, String>("Num"));
        rtColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Float>("RT"));
        mzColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Float>("MZ"));


        //create new Session
        session = new Session();
        session.getReference().setName("Reference");

//        try {
//            FileUtils.deleteDirectory(new File("C:\\Users\\stefankoch\\Documents\\tmp2"));
//        } catch (IOException ex) {
//            Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        new File("C:\\Users\\stefankoch\\Documents\\tmp2").mkdirs();

        //Parameters
        AdName1.textProperty().bindBidirectional(session.getListofadductnameproperties().get(0));
        AdName2.textProperty().bindBidirectional(session.getListofadductnameproperties().get(1));
        AdName3.textProperty().bindBidirectional(session.getListofadductnameproperties().get(2));
        AdName4.textProperty().bindBidirectional(session.getListofadductnameproperties().get(3));
        AdName5.textProperty().bindBidirectional(session.getListofadductnameproperties().get(4));
        AdName6.textProperty().bindBidirectional(session.getListofadductnameproperties().get(5));
        AdName7.textProperty().bindBidirectional(session.getListofadductnameproperties().get(6));
        AdMass1.textProperty().bindBidirectional(session.getListofadductmassproperties().get(0), new NumberStringConverter());
        AdMass2.textProperty().bindBidirectional(session.getListofadductmassproperties().get(1), new NumberStringConverter());
        AdMass3.textProperty().bindBidirectional(session.getListofadductmassproperties().get(2), new NumberStringConverter());
        AdMass4.textProperty().bindBidirectional(session.getListofadductmassproperties().get(3), new NumberStringConverter());
        AdMass5.textProperty().bindBidirectional(session.getListofadductmassproperties().get(4), new NumberStringConverter());
        AdMass6.textProperty().bindBidirectional(session.getListofadductmassproperties().get(5), new NumberStringConverter());
        AdMass7.textProperty().bindBidirectional(session.getListofadductmassproperties().get(6), new NumberStringConverter());
        RTTol.textProperty().bindBidirectional(session.getRTTolProp(), new NumberStringConverter());
        RTTolShift.textProperty().bindBidirectional(session.getPeakRTTolerance(), new NumberStringConverter());
        MZTol.textProperty().bindBidirectional(session.getMZTolProp(), new NumberStringConverter());
        SliceMZTol.textProperty().bindBidirectional(session.getSliceMZTolProp(), new NumberStringConverter());
        Res.textProperty().bindBidirectional(session.getResProp(), new NumberStringConverter());
        Base.textProperty().bindBidirectional(session.getBaseProp(), new NumberStringConverter());
        PeakPick.setItems(FXCollections.observableArrayList(
                "Naïve", "MassSpecWavelet")
        );

        PeakPick.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue ov, Number value, Number newVal) {
                session.setPeackPick(PeakPick.getItems().get(newVal.intValue()).toString());
            }

        });
        PeakPick.getSelectionModel().select(0);
        panelink = new HashMap<>();
        setDatasettocontroller(new HashMap<>());

        //set batchcount to 0,
        batchcount = 0;
        
        Label label = new Label("1.)    <------------  Set Parameters \n\n\n  2.)  Click here to choose Data Matrix");
        label.setAlignment(Pos.CENTER);
        label.setMinHeight(500);
        label.setMinWidth(500);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(Font.font ("Verdana", 14));
        label.setOnMouseClicked((MouseEvent event) -> {
            try {
               
                ProgressIndicator prog = new ProgressIndicator();
                prog.setMaxHeight(50);
               
                metTable.setPlaceholder(prog);
                prog.setOnMouseClicked((MouseEvent event2) -> {
            try {
                openReferenceDataMatrixChooser();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
                openReferenceDataMatrixChooser();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        metTable.setPlaceholder(label);

    }

    //Open File Chooser for Data Matrix
    public void openReferenceDataMatrixChooser() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MetExtract output files (*.tsv)", "*.tsv");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);
        session.setReferenceTsv(file);
        System.out.println(session.getReferenceTsv().toString());
        setMasterListofOGroups(session.parseReferenceTsv());

        //generate additional adducts
        session.finalizeAdducts();
        generateAdducts();
        //Convert List into TreeTable Entries
        TreeItem<Entry> superroot = new TreeItem<>();

        //for all OGroups
        for (int i = 0; i < getMasterListofOGroups().size(); i++) {
            TreeItem<Entry> root = new TreeItem<>(getMasterListofOGroups().get(i));
            root.setExpanded(false);
            superroot.getChildren().add(root);

            for (int j = 0; j < getMasterListofOGroups().get(i).getListofAdducts().size(); j++) {
                TreeItem<Entry> childNode1 = new TreeItem<>(getMasterListofOGroups().get(i).getListofAdducts().get(j));
                root.getChildren().add(childNode1);

            }

        }

        getMetTable().setRoot(superroot);
        getMetTable().setShowRoot(false);
        
        addBatchButton.setDisable(false);
        addBatchButton.setVisible(true);
        accordion.setVisible(true);
        setParameterPane(false);
        
        TabPane.setVisible(false);
        RTTol.setDisable(true);
        MZTol.setDisable(true);
        SliceMZTol.setDisable(true);
        Res.setDisable(true);
        paramMenu.setDisable(false);
        AdName1.setDisable(true);
        AdName2.setDisable(true);
        AdName3.setDisable(true);
        AdName4.setDisable(true);
        AdName5.setDisable(true);
        AdName6.setDisable(true);
        AdName7.setDisable(true);
        AdMass1.setDisable(true);
        AdMass2.setDisable(true);
        AdMass3.setDisable(true);
        AdMass4.setDisable(true);
        AdMass5.setDisable(true);
        AdMass6.setDisable(true);
        AdMass7.setDisable(true);
        toggleadductgeneration.setDisable(true);

        session.prepare();
        

        getMetTable().getSortOrder().clear();
        getMetTable().getSortOrder().add(mzColumn);
        getMetTable().getSortOrder().add(rtColumn);

        try {
            TitledPane tps = new TitledPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Batch.fxml"));
            //loader.setRoot(tps);
            Reference reference = new Reference();
            reference.setName("Reference");
            session.addDataset(reference);
            batchcount++;
            panelink.put(tps, reference);
            loader.setController(new BatchController(session, reference, progressbar, getMasterListofOGroups(), tps, this));
            getDatasettocontroller().put(reference, loader.getController());
            reference.setController(loader.getController());
            tps = loader.load();
            tps.setExpanded(true);
            getAccordion().getPanes().add(tps);
            getAccordion().setExpandedPane(tps);

        } catch (IOException ex) {
            Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //add float click functionality to the TreeTable
        getMetTable().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {

                    try {

                        //create new window
                        Stage stage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_adductview.fxml"));
                        Pane myPane = (Pane) loader.load();
                        Scene myScene = new Scene(myPane);
                        stage.setScene(myScene);
                        Fxml_adductviewController controller = loader.<Fxml_adductviewController>getController();
                        controller.setSession(session);
                        controller.setMainController(getController());

                        //add MasterListofOGroups to new controller
                        controller.metTable = getMetTable();

                        //print graphs
                        controller.print();
                        stage.show();
                        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

                            public void handle(WindowEvent we) {
                                controller.close();
                            }
                        });

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });

    }

    //add a new batch
    public void addBatch() {

        try {
            TitledPane tps = new TitledPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Batch.fxml"));
            //loader.setRoot(tps);
            Batch batch = new Batch(batchcount);
            batch.setName("Batch Nr. " + (batchcount + 1));
            session.addDataset(batch);
            batchcount++;
            panelink.put(tps, batch);
            loader.setController(new BatchController(session, batch, progressbar, getMasterListofOGroups(), tps, this));
            getDatasettocontroller().put(batch, loader.getController());
            batch.setController(loader.getController());
            tps = loader.load();
            tps.setExpanded(true);
            getAccordion().getPanes().add(tps);
            getAccordion().setExpandedPane(tps);

        } catch (IOException ex) {
            Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //calculates Shift and opens a new window
    public void newwindowcalculate() throws IOException, InterruptedException {

        output.setDisable(false);
        //open new window
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_shiftview.fxml"));
        Pane myPane = (Pane) loader.load();
        Scene myScene = new Scene(myPane);
        stage.setScene(myScene);
        Fxml_shiftviewController controller = loader.<Fxml_shiftviewController>getController();
        controller.setSupercontroller(this);
        controller.setSession(session);

        //print graphs
        controller.print(getMasterListofOGroups());
        System.out.println("PRINTNEW");
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                controller.close();
            }
        });

    }

    //does the Shift calculation
    public void calculate(CountDownLatch latch, ProgressBar prog) throws IOException, InterruptedException {

        FloatProperty progress = new SimpleFloatProperty(0.0f);
        prog.progressProperty().bind(progress);

        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {

                for (int d = 0; d < session.getListofDatasets().size(); d++) {
                    if (session.getListofDatasets().get(d).getActive()) {
                        for (int f = 0; f < session.getListofDatasets().get(d).getListofFiles().size(); f++) {
                            RawDataFile currentfile = session.getListofDatasets().get(d).getListofFiles().get(f);
                            if (currentfile.getActive().booleanValue()) {

                                Collections.sort(getMasterListofOGroups(), new orderbyRT());
                                float[][] matrix = new float[getMasterListofOGroups().size()][session.getResolution()];

                                CountDownLatch latchpeak = new CountDownLatch(1);
                                Task task = new Task<Void>() {
                                    @Override
                                    public Void call() throws IOException, InterruptedException {

                                        LinkedList<Integer> queue = new LinkedList<Integer>();
                                        //go trough and check if all are ready
                                        for (int i = 0; i < getMasterListofOGroups().size(); i++) {
                                            //if not ready, add to queue
                                            if (getMasterListofOGroups().get(i).isStored(currentfile)) {
                                                queue.add(i);
                                                session.getIothread().addOGroup(getMasterListofOGroups().get(i), currentfile);
                                                //if ready calculate
                                            } else {
                                                getMasterListofOGroups().get(i).peakpickOGroup(currentfile);
                                                float[] PropArray = getMasterListofOGroups().get(i).getOGroupPropArraySmooth(currentfile);
                                                for (int j = 0; j < session.getResolution(); j++) {
                                                    matrix[i][j] = PropArray[j];
                                                }
                                            }
                                        }
                                        System.out.println("Size of Queue: " + queue.size());
                                        //go through queue until it is empty
                                        while (queue.size() > 0) {
                                            Integer current = queue.pop();
                                            if (getMasterListofOGroups().get(current).isStored(currentfile)) {
                                                queue.add(current);
                                            } else {
                                                getMasterListofOGroups().get(current).peakpickOGroup(currentfile);
                                                float[] PropArray = getMasterListofOGroups().get(current).getOGroupPropArraySmooth(currentfile);
                                                for (int j = 0; j < session.getResolution(); j++) {
                                                    matrix[current][j] = PropArray[j];
                                                }
                                            }

                                        }

                                        latchpeak.countDown();
                                        return null;
                                    }

                                };

                                //new thread that executes task
                                new Thread(task).start();
                                latchpeak.await();

                                //Test artificial shift
//                float[][] matrix2 = new float[MasterListofOGroups.size()][session.getResolution()];
//                for (int i = 0; i< MasterListofOGroups.size(); i++) {
//                    int currentshift = (int) (Math.floor(10+(Math.sin(MasterListofOGroups.get(i).getRT())*10)));
//                    for (int j = 0; j<currentshift; j++) {
//                        matrix2[i][j] = 0; 
//                    }
//                    for (int j = currentshift; j<session.getResolution(); j++) {
//                        matrix2[i][j] = matrix[i][j-currentshift];
//                        
//                    }
//                }
//               
//                matrix = matrix2;
                                //calculate weight matrix
                                float[][] weights = new float[getMasterListofOGroups().size()][session.getResolution()];
                                //fill first row
                                for (int j = 0; j < session.getResolution(); j++) {
                                    weights[0][j] = matrix[0][j];

                                }
                                //TODO: Penalty for change in j
                                //fill rest of weights matrix
                                float penalty = session.getListofDatasets().get(0).getPenalty();
                                for (int i = 1; i < getMasterListofOGroups().size(); i++) {
                                    for (int j = 0; j < session.getResolution(); j++) {
                                        float max = 0;
                                        if (weights[i - 1][j] > max) {
                                            max = weights[i - 1][j] + matrix[i][j];
                                        }
                                        if ((j - 1) > 0 && weights[i - 1][j - 1] + matrix[i][j] - penalty > max) {
                                            max = weights[i - 1][j - 1] + matrix[i][j] - penalty;
                                        }
                                        if ((j + 1) < session.getResolution() && weights[i - 1][j + 1] + matrix[i][j] - penalty > max) {
                                            max = weights[i - 1][j + 1] + matrix[i][j] - penalty;
                                        }
                                        weights[i][j] = max;

                                    }

                                }
                                //get max in last row
                                float max = 0;
                                int maxint = 0;
                                for (int j = 0; j < session.getResolution(); j++) {
                                    if (weights[getMasterListofOGroups().size() - 1][j] > max) {
                                        maxint = j;
                                        max = weights[getMasterListofOGroups().size() - 1][j];
                                    }
                                }

                                getMasterListofOGroups().get(getMasterListofOGroups().size() - 1).setFittedShift(currentfile, (short) maxint);

                                //TODO: calculate range as function of time
                                for (int i = getMasterListofOGroups().size() - 1; i > -1; i--) {
                                    max = 0;

                                    int j = maxint;
                                    if ((j - 1) > 0 && weights[i][j - 1] > max) {
                                        max = weights[i][j - 1];
                                        maxint = j - 1;
                                    }
                                    if (weights[i][j] > max) {
                                        max = weights[i][j];
                                        maxint = j;
                                    }
                                    if ((j + 1) < session.getResolution() && weights[i][j + 1] > max) {
                                        //max = weights[i][j+1];
                                        maxint = j + 1;
                                    }

                                    getMasterListofOGroups().get(i).setFittedShift(currentfile, (short) maxint);

                                    //set score for OPGroup
                                    getMasterListofOGroups().get(i).addScore(currentfile, (getMasterListofOGroups().get(i).getOGroupPropArraySmooth(currentfile)[getMasterListofOGroups().get(i).getOGroupFittedShift(currentfile)]));

                                    //set score for every addact
                                    for (int a = 0; a < getMasterListofOGroups().get(i).getListofAdducts().size(); a++) {
                                        getMasterListofOGroups().get(i).getListofAdducts().get(a).addScore(currentfile, (getMasterListofOGroups().get(i).getListofAdducts().get(a).getAdductPropArray(currentfile)[getMasterListofOGroups().get(i).getOGroupFittedShift(currentfile)]));
                                    }

                                    getMetTable().refresh();

                                }
                                //TODO number of active files

                                progress.set(progress.get() + 1.0f / (session.getListofDatasets().get(d).getListofFiles().size()));
                                System.out.println("Calculation: " + progress.get() + "%");
                            }
                        }
                    }
                }

                //don't recalculate unless something changes
                session.setPeakPickchanged(false);
                CertaintyCalculator calc = new CertaintyCalculator(session);
                calc.calculate();
                latch.countDown();
                return null;
            }

        };

        //new thread that executes task
        new Thread(task).start();

    }

    public float getmaxofrange(float[][] weights, int row, int col, int range) {
        float max = 0;

        for (int i = (col - range); i <= (col + range); i++) {
            if (i < session.getResolution() && i >= 0 && weights[row][i] > max) {
                max += weights[row][i];
            }

        }

        return max;
    }

    /**
     * @return the metTable
     */
    public TreeTableView<Entry> getMetTable() {
        return metTable;
    }

    public FXMLTableViewController getController() {
        return this;
    }

    /**
     * @param metTable the metTable to set
     */
    public void setMetTable(TreeTableView<Entry> metTable) {
        this.metTable = metTable;
    }

    /**
     * @return the MasterListofOGroups
     */
    public ObservableList<Entry> getMasterListofOGroups() {
        return MasterListofOGroups;
    }

    /**
     * @param MasterListofOGroups the MasterListofOGroups to set
     */
    public void setMasterListofOGroups(ObservableList<Entry> MasterListofOGroups) {
        this.MasterListofOGroups = MasterListofOGroups;
    }

    /**
     * @return the accordion
     */
    public Accordion getAccordion() {
        return accordion;
    }

    /**
     * @param accordion the accordion to set
     */
    public void setAccordion(Accordion accordion) {
        this.accordion = accordion;
    }

    /**
     * @return the datasettocontroller
     */
    public HashMap<Dataset, BatchController> getDatasettocontroller() {
        return datasettocontroller;
    }

    /**
     * @param datasettocontroller the datasettocontroller to set
     */
    public void setDatasettocontroller(HashMap<Dataset, BatchController> datasettocontroller) {
        this.datasettocontroller = datasettocontroller;
    }

    public void generateOutput() throws FileNotFoundException, UnsupportedEncodingException, IOException {

        //sort by OGroup and Num, to get order of Input
        getMetTable().getSortOrder().clear();
        getMetTable().getSortOrder().add(numColumn);
        getMetTable().getSortOrder().add(nameColumn);
        getMetTable().sort();

        //generate sorted List
        List<Entry> list = new ArrayList<Entry>();
        TreeItem<Entry> root = getMetTable().getRoot();

        for (int i = 0; i < root.getChildren().size(); i++) {
            list.add(root.getChildren().get(i).getValue());
        }

//        for (int i = 0; i < session.getListofDatasets().size(); i++) {
//            for (int j = 0; j < session.getListofDatasets().get(i).getListofFiles().size(); j++) {
//                RawDataFile file = session.getListofDatasets().get(i).getListofFiles().get(j);
//                System.out.println("File: " + file.getName());
//                for (int o = 0; o < list.size(); o++) {
//                    for (int s = 0; s < list.get(o).getListofAdducts().size(); s++) {
//                        System.out.println("OGroup: " + list.get(o).getOGroup() + "  Number: " + list.get(o).getListofAdducts().get(s).getNum() + "   Area: " + list.get(o).getListofAdducts().get(s).getListofSlices().get(file).getfittedArea());
//                    }
//                }
//
//            }
//
//        }
        //parse Input Matrix again
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");

        TsvParser parser = new TsvParser(settings);
        FileReader reader = new FileReader(session.getReferenceTsv());
        List<String[]> allRows = parser.parseAll(reader);

        //get Headers
        List<String> headers = Arrays.asList(allRows.get(0));
        headers = new ArrayList<>(headers);

        //List for convenience
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < allRows.size(); i++) {
            rows.add(new ArrayList<>(Arrays.asList(allRows.get(i))));
        }

        PrintWriter printwriter = new PrintWriter("C:\\Users\\stefankoch\\Documents\\Output\\output.txt", "UTF-8");
        TsvWriter writer = new TsvWriter(printwriter, new TsvWriterSettings());

        //add newly generated adducts to rows
        int currentline = 1;
        for (int o = 0; o < list.size(); o++) {
            for (int s = 0; s < list.get(o).getListofAdducts().size(); s++) {
                Entry adduct = list.get(o).getListofAdducts().get(s);
                //if old
                if (list.get(o).getListofAdducts().get(s).getNum() <= maxnumber) {
                    currentline++;
                    //if new
                } else {
                    //check if empty
                    boolean empty = true;
                    for (int i = 0; i < session.getListofDatasets().size(); i++) {
                        for (int j = 0; j < session.getListofDatasets().get(i).getListofFiles().size(); j++) {
                            RawDataFile file = session.getListofDatasets().get(i).getListofFiles().get(j);
                            if (adduct.getListofSlices().containsKey(file) && adduct.getListofSlices().get(file).getfittedArea() != null) {
                                empty = false;
                                break;
                            }
                        }
                    }

                    adduct.setEmpty(empty);

                    if (!empty) {
                        List<String> newline = new ArrayList<String>();
                        newline.add(String.valueOf(adduct.getNum()));
                        newline.add(String.valueOf(adduct.getMZ()));
                        newline.add("");
                        newline.add("");
                        newline.add(String.valueOf(adduct.getRT()));
                        newline.add(String.valueOf(adduct.getXn()));
                        newline.add(String.valueOf(adduct.getOriginalAdduct().getCharge()));
                        newline.add(adduct.getOriginalAdduct().getScanEvent());
                        newline.add(adduct.getOriginalAdduct().getIonisation());
                        newline.add("");
                        newline.add(String.valueOf(adduct.getOGroup()));
                        newline.add(adduct.getIon());
                        newline.add("");
                        if (adduct.getM() > 0) {
                            newline.add(String.valueOf(adduct.getM()));
                        } else {
                            newline.add("");
                        }
                        rows.add(currentline, newline);
                        currentline++;
                    }
                }
            }
        }

        //write data from top to bottom, add data of currentfile
        for (int i = 0; i < session.getListofDatasets().size(); i++) {
            for (int j = 0; j < session.getListofDatasets().get(i).getListofFiles().size(); j++) {
                RawDataFile file = session.getListofDatasets().get(i).getListofFiles().get(j);
                headers.add(14, file.getName().substring(0, file.getName().length() - 6) + "_Test_Area");
                currentline = 1;
                for (int o = 0; o < list.size(); o++) {
                    for (int s = 0; s < list.get(o).getListofAdducts().size(); s++) {
                        if (!list.get(o).getListofAdducts().get(s).isEmpty()) {
                            if (list.get(o).getListofAdducts().get(s).getListofSlices().get(file) == null || list.get(o).getListofAdducts().get(s).getListofSlices().get(file).getfittedArea() == null) {
                                rows.get(currentline).add(14, "");
                                currentline++;
                            } else {
                                rows.get(currentline).add(14, Float.toString(list.get(o).getListofAdducts().get(s).getListofSlices().get(file).getfittedArea()));
                                currentline++;
                            }
                        }
                    }
                }

            }

        }

        //back to arrays...
        allRows = new ArrayList<String[]>();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = new String[rows.get(i).size()];
            row = rows.get(i).toArray(row);
            allRows.add(row);
        }

        writer.writeHeaders(headers);
        for (int i = 1; i < rows.size(); i++) {
            writer.writeRow(rows.get(i).toArray());
        }

        writer.close();
        Runtime.getRuntime().exec("explorer.exe /select," + "C:\\Users\\stefankoch\\Documents\\Output\\output.txt");
        getMetTable().getSortOrder().clear();
        getMetTable().getSortOrder().add(mzColumn);
        getMetTable().getSortOrder().add(rtColumn);

    }

    public void generateAdducts() {
        if (toggleadductgeneration.selectedProperty().get()){

        //get highest num of Adduct
        int max = 0;
        for (int i = 0; i < MasterListofOGroups.size(); i++) {
            for (int j = 0; j < MasterListofOGroups.get(i).getListofAdducts().size(); j++) {
                if (MasterListofOGroups.get(i).getListofAdducts().get(j).getNum() > max) {
                    max = MasterListofOGroups.get(i).getListofAdducts().get(j).getNum();
                }
            }
        }
        this.maxnumber = max;
        max++;

        for (int o = 0; o < MasterListofOGroups.size(); o++) {
            int size = MasterListofOGroups.get(o).getListofAdducts().size();
            for (int a = 0; a < size; a++) {
                Entry adduct = MasterListofOGroups.get(o).getListofAdducts().get(a);
                //for every adduct, check if ion specified
                if (adduct.getIon() == null) {
                    //if not
                    for (int j = 0; j < session.getListofadductnames().size(); j++) {
                        //subtract every possible adduct
                        for (int k = 0; k < session.getListofadductnames().size(); k++) {
                            //and add every possible adduct
                            if (j != k) {
                                //don't add the same value
                                Float mass = adduct.getMZ() + session.getListofadductmasses().get(j) - session.getListofadductmasses().get(k);
                                Float ppm = mass / 1000000 * session.getMZTolerance();
                                boolean duplicate = false;
                                for (int c = 0; c < MasterListofOGroups.get(o).getListofAdducts().size(); c++) {
                                    if (Math.abs(mass - MasterListofOGroups.get(o).getListofAdducts().get(c).getMZ()) < ppm) {
                                        duplicate = true;
                                        System.out.println("Duplicate generated");
                                        break;
                                    }
                                }
                                if (!duplicate) {
                                    String Ion = "[(" + adduct.getNum() + "-" + session.getListofadductnames().get(k) + ")+" + session.getListofadductnames().get(j) + "]+";
                                    MasterListofOGroups.get(o).addAdduct(new Entry(max,mass, adduct.getRT(), adduct.getXn(), adduct.getOGroup(), Ion, adduct.getM(), session, MasterListofOGroups.get(o), adduct));
                                    max++;
                                }
                            }
                        }

                    }

                    //if ion specified
                } else //if multiple Ions specified
                if (adduct.getIon().indexOf(',') > 0) {
                    //do something
                } else {
                    String Ion = adduct.getIon().substring(adduct.getIon().indexOf('+') + 1, adduct.getIon().indexOf(']', 3));
                    int k = session.getListofadductnames().indexOf(Ion);
                    //if specified Ion in List
                    if (k > 0) {
                        for (int j = 0; j < session.getListofadductnames().size(); j++) {
                            //and add every possible adduct
                            if (j != k) {
                                //don't add the same value
                                Float mass = adduct.getMZ() + session.getListofadductmasses().get(j) - session.getListofadductmasses().get(k);
                                Float ppm = mass / 1000000 * session.getMZTolerance();
                                boolean duplicate = false;
                                for (int c = 0; c < MasterListofOGroups.get(o).getListofAdducts().size(); c++) {
                                    if (Math.abs(mass - MasterListofOGroups.get(o).getListofAdducts().get(c).getMZ()) < ppm) {
                                        duplicate = true;
                                        System.out.println("Duplicate generated");
                                        break;
                                    }
                                }
                                if (!duplicate) {
                                    Ion = "[M+" + session.getListofadductnameproperties().get(j).get() + "]+";
                                    MasterListofOGroups.get(o).addAdduct(new Entry(max, adduct.getMZ() + mass, adduct.getRT(), adduct.getXn(), adduct.getOGroup(), Ion, adduct.getM(), session, MasterListofOGroups.get(o), adduct));
                                    max++;
                                }
                            }
                        }

                    }
                }

            }
        }

    }
    }

    public void showParameters() {
        setParameterPane(true);
        TabPane.setVisible(true);
        accordion.setVisible(false);
        addBatchButton.setVisible(false);
        oldPick = PeakPick.getSelectionModel().getSelectedItem().toString();
        oldBase = Base.getText();
        oldRT = RTTolShift.getText();

    }

    public void hideParameters() {
        setParameterPane(false);
        TabPane.setVisible(false);
        accordion.setVisible(true);
        addBatchButton.setVisible(true);
        session.prepare();

        //indicate change
        if (!oldPick.equals(PeakPick.getSelectionModel().getSelectedItem().toString())) {
            session.setPeakPickchanged(true);
        }
        if (!oldBase.equals(Base.getText())) {
            session.setPeakPickchanged(true);
        }

        if (!oldRT.equals(RTTolShift.getText())) {
            session.setPeakPickchanged(true);
        }

    }

    public void setParameterPane(boolean bool) {
//        label1.setVisible(bool);
//        label2.setVisible(bool);
//        label3.setVisible(bool);
//        label4.setVisible(bool);
//        label5.setVisible(bool);
//        label6.setVisible(bool);
//        label7.setVisible(bool);
//        label8.setVisible(bool);
//        label9.setVisible(bool);
//        label10.setVisible(bool);
//        label11.setVisible(bool);
//        box1.setVisible(bool);
//        box2.setVisible(bool);
//        box3.setVisible(bool);
//        box4.setVisible(bool);
//        RTTol.setVisible(bool);
//        RTTolShift.setVisible(bool);
//        MZTol.setVisible(bool);
//        SliceMZTol.setVisible(bool);
//        Res.setVisible(bool);
//        Base.setVisible(bool);
//        PeakPick.setVisible(bool);
        paramButton.setVisible(bool);

    }

    //when closing the window, end all running processes, such as Rengine
    public void close() {
        session.getEngine().end();
        session.getIothread().terminate();

    }

    public void toggleAdductGeneration() {
        boolean toggle = !toggleadductgeneration.selectedProperty().get();
        AdName1.setDisable(toggle);
        AdName2.setDisable(toggle);
        AdName3.setDisable(toggle);
        AdName4.setDisable(toggle);
        AdName5.setDisable(toggle);
        AdName6.setDisable(toggle);
        AdName7.setDisable(toggle);
        AdMass1.setDisable(toggle);
        AdMass2.setDisable(toggle);
        AdMass3.setDisable(toggle);
        AdMass4.setDisable(toggle);
        AdMass5.setDisable(toggle);
        AdMass6.setDisable(toggle);
        AdMass7.setDisable(toggle);
    }
}
