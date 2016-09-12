package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Batch;
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import com.mycompany.fxmltableview.logic.Session;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import static javafx.scene.control.SelectionMode.MULTIPLE;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

//this is the Controller for the Main GUI
public class BatchController implements Initializable {

    //link fxml information to controller
    @FXML
    MenuBar batchMenu;

    @FXML
    private TableView<RawDataFile> batchFileView;

    @FXML
    TableColumn<RawDataFile, String> fileColumn;

    @FXML
    TableColumn widthColumn;

    @FXML
    TableColumn<RawDataFile, Boolean> activeColumn;

    @FXML
    TableColumn<RawDataFile, Color> colorColumn;

    @FXML
    TextField batdefwidth, batsetwidth, paneName;

    @FXML
    ColorPicker batdefcol, batsetcol;

    @FXML
    MenuItem deleteFile, delete;

    @FXML
    TitledPane BatchPane;

    @FXML
    private CheckBox batact;

    //current session, storing all information
    Session session;
    Dataset batch;
    ProgressBar progressbar;
    TitledPane pane;
    FXMLTableViewController TVcontroller;

    //List with data for table, Ogroups (adducts within the Ogroups)
    ObservableList<Entry> data;
    
    private boolean positive = false;
    private boolean cancel = false;
    private boolean windowopen = false;

    //constructor, has reference to the session
    public BatchController(Session session, Dataset batch, ProgressBar bar, ObservableList<Entry> data, TitledPane tps, FXMLTableViewController tvController) {
        this.session = session;
        this.batch = batch;
        this.progressbar = bar;
        this.data = data;
        this.pane = tps;
        this.TVcontroller = tvController;

    }

    //initialize the table, and various elements
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (batch.equals(session.getReference())) {
            delete.setVisible(false);
        }

        Label label = new Label("Click to add Files");
        label.setFont(Font.font("Verdana", 14));
        label.setAlignment(Pos.CENTER);
        label.setMinHeight(200);
        label.setMinWidth(200);
        label.setOnMouseClicked((MouseEvent event) -> {
            try {
                openBatchmzxmlChooser();
                ProgressIndicator prog = new ProgressIndicator();
                prog.setMaxHeight(50);

                batchFileView.setPlaceholder(prog);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        batchFileView.setPlaceholder(label);
        batchFileView.setRowFactory(new Callback<TableView<RawDataFile>, TableRow<RawDataFile>>() {
            @Override
            public TableRow<RawDataFile> call(TableView<RawDataFile> tableView2) {
                final TableRow<RawDataFile> row = new TableRow<>();
                row.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton().equals(MouseButton.PRIMARY)) {
                            final int index = row.getIndex();
                            if (index >= 0 && index < batchFileView.getItems().size() && batchFileView.getSelectionModel().isSelected(index)) {
                                batchFileView.getSelectionModel().clearSelection(index);
                                event.consume();
                            }
                        }
                    }
                });
                return row;
            }
        });

//        BatchPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//          if (!newValue) {
//              batchFileView.getSelectionModel().clearSelection();
//          }
//            }
//            
//            
//
//        }); 
        //set Factories for the tables
        fileColumn.setCellValueFactory(new PropertyValueFactory<RawDataFile, String>("name"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<RawDataFile, Color>("color"));
        colorColumn.setCellFactory(ColorTableCell::new);

        batact.setSelected(true);

        activeColumn.setCellValueFactory(new PropertyValueFactory("active"));

        activeColumn.setCellFactory(new Callback<TableColumn<RawDataFile, Boolean>, TableCell<RawDataFile, Boolean>>() {

            public TableCell<RawDataFile, Boolean> call(TableColumn<RawDataFile, Boolean> p) {

                return new CheckBoxTableCell<RawDataFile, Boolean>();

            }

        });

        //enables edit functionality for width cell
        widthColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        widthColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<RawDataFile, Number>>() {
            @Override
            public void handle(CellEditEvent<RawDataFile, Number> t) {
                ((RawDataFile) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setWidth((t.getNewValue().floatValue()));
            }
        }
        );

        widthColumn.setCellFactory(TextFieldTableCell.<RawDataFile, Number>forTableColumn(new NumberStringConverter()));

        //bind default values
        batdefwidth.textProperty().bindBidirectional(batch.getWidthProperty(), new NumberStringConverter());
        batdefcol.valueProperty().bindBidirectional(batch.getColorProperty());

        //add functionality to set the color for all files
        batsetcol.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i < batch.getListofFiles().size(); i++) {
                    batch.getListofFiles().get(i).setColor(batsetcol.getValue());

                }

            }
        });

        //add functionality to set the width for all files
        batsetwidth.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i < batch.getListofFiles().size(); i++) {
                    batch.getListofFiles().get(i).setWidth((Float.parseFloat(batsetwidth.getText())));
                    getBatchFileView().refresh();

                }

            }
        });

        paneName.textProperty().bindBidirectional(batch.getNameProperty());
        BatchPane.textProperty().bind(batch.getNameProperty());

        getBatchFileView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // File Chooser for mzXML files
    public void openBatchmzxmlChooser() throws FileNotFoundException {

//Property to link with progressbar
        FloatProperty progress = new SimpleFloatProperty(0.0f);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progress.set((float) -0.000001);
            }
        });
        progressbar.progressProperty().bind(progress);

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("mzXML files (*.mzxml)", "*.mzxml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        List<File> filelist = fileChooser.showOpenMultipleDialog(null);

        // create a new task
        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
                float test = 1 / (float) filelist.size();
                if (filelist != null) {
                    TVcontroller.loading.getAndIncrement();
                    TVcontroller.maskerpane.setVisible(true);
                    TVcontroller.indicatorbar.setEffect(TVcontroller.adjust);
                    progressbar.setVisible(true);

                    //test polarity
                    String pol = testpolarity(filelist);
                    positive = false;
                    cancel = false;
                    switch (pol) {
                        case "both":
                            windowopen=true;
        Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setTitle("Positive and negative polarity detected");
                            alert.setHeaderText("Positive and negative polarity data has been detected.");
                            alert.setContentText("The chosen files contain positive as well as negaitve polarity data. Files can only be processed in one polarity, please choose one. If you wish to process the files in both polarities, choose one now, load the same files again and choose the other polarity.");

                            ButtonType buttonTypeOne = new ButtonType("Positive");
                            ButtonType buttonTypeTwo = new ButtonType("Negative");
                            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
                       
                            
                            Optional<ButtonType> result = alert.showAndWait();
                        
                            if (result.get() == buttonTypeOne) {
                                try {
                                    openFiles(filelist,true,progress);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else if (result.get() == buttonTypeTwo) {
                                try {
                                    openFiles(filelist,false,progress);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(BatchController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                            }
}});
        break;
                        case "pos":
                            openFiles(filelist,true,progress);
                            break;
                        case "neg":
                            openFiles(filelist,false,progress);
                            break;
                    }
                }
                return null;
                }
        };


        //new thread that executes task
        new Thread(task).start();

    }

    public void printInfo() {

    }

    public void newwindowcalculate() throws IOException, InterruptedException {
        TVcontroller.newwindowcalculate();
    }

    public void deleteFile() {
        ArrayList<RawDataFile> list = new ArrayList();
        for (int i = 0; i < getBatchFileView().getSelectionModel().getSelectedItems().size(); i++) {
            list.add(getBatchFileView().getSelectionModel().getSelectedItems().get(i));
        }

        for (int i = 0; i < list.size(); i++) {
            list.get(i).deleteFile();

        }
        getBatchFileView().getSelectionModel().clearSelection();
    }

    public void checkforFile() {
        ObservableList<RawDataFile> list = getBatchFileView().getSelectionModel().getSelectedItems();
        if (list.size() < 1) {
            deleteFile.setDisable(true);
            deleteFile.setVisible(false);
        } else {
            deleteFile.setDisable(false);
            deleteFile.setVisible(true);
        }

    }

    public void changedFile() {
        Task task = new Task<Void>() {
            @Override

            //sets Score to the max over all selected Files
            public Void call() throws InterruptedException {
                List<RawDataFile> completeList = session.getSelectedFiles();
                for (int i = 0; i < TVcontroller.getMasterListofOGroups().size(); i++) {

//                   float maxScore = 0;
//                   float maxScorepeakclose = 0;
//                   float maxScorepeakfound = 0;
//                   for (int f = 0; f<completeList.size(); f++) {
//                       if(completeList.get(f).isselected()) {
//                       RawDataFile file = completeList.get(f);
//                       if (TVcontroller.getMasterListofOGroups().get(i).getScore(file)>maxScore) {
//                           maxScore = TVcontroller.getMasterListofOGroups().get(i).getScore(file);
//                       }
////                       if (TVcontroller.getMasterListofOGroups().get(i).getListofSlices().get(file).getScorepeakfound()>maxScorepeakfound) {
////                           maxScorepeakfound=TVcontroller.getMasterListofOGroups().get(i).getListofSlices().get(file).getScorepeakfound();
////                       }
////                       
////                       if (TVcontroller.getMasterListofOGroups().get(i).getListofSlices().get(file).getScorepeakclose()>maxScorepeakclose) {
////                       maxScorepeakclose=TVcontroller.getMasterListofOGroups().get(i).getListofSlices().get(file).getScorepeakclose();
////                       }
//                       }
//                   }
//                   TVcontroller.getMasterListofOGroups().get(i).setScore(new SimpleFloatProperty(maxScore));
//                   TVcontroller.getMasterListofOGroups().get(i).setScorepeakclose(new SimpleFloatProperty(maxScorepeakclose));
//                   TVcontroller.getMasterListofOGroups().get(i).setScorepeakfound(new SimpleFloatProperty(maxScorepeakfound));
//                   float omaxScore = 0;
                    float ominScorepeakclose = Float.POSITIVE_INFINITY;
                    float omaxScorepeakfound = Float.NEGATIVE_INFINITY;
                    float omaxScorefitabove = Float.NEGATIVE_INFINITY;
                    float omaxrange = Float.NEGATIVE_INFINITY;

                    for (int j = 0; j < TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().size(); j++) {
//                       float maxScore = 0;
                        float minScorepeakclose = Float.POSITIVE_INFINITY;
                        float maxScorepeakfound = Float.NEGATIVE_INFINITY;
                        float maxScorefitabove = Float.NEGATIVE_INFINITY;
                        for (int f = 0; f < completeList.size(); f++) {
                            RawDataFile file = completeList.get(f);
//                       if (TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getScore(file)>maxScore) {
//                           maxScore = TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getScore(file);
//                       }
                            if (TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().containsKey(file)) {
                                if (TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().get(file).getScorepeakfound() > maxScorepeakfound) {
                                    maxScorepeakfound = TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().get(file).getScorepeakfound();
                                }
                                if (TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().get(file).getFitabove() > maxScorefitabove) {
                                    maxScorefitabove = TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().get(file).getFitabove();
                                }

                                if (TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().get(file).getScorepeakclose() < minScorepeakclose) {
                                    minScorepeakclose = TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getListofSlices().get(file).getScorepeakclose();
                                }
                            }

                        }
//                   TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).setScore(new SimpleFloatProperty(maxScore));
                        TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).setScorepeakclose(new SimpleFloatProperty(minScorepeakclose));
                        TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).setScorepeakfound(new SimpleFloatProperty(maxScorepeakfound));
                        TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).setScorefitabove(new SimpleFloatProperty(maxScorefitabove));

//                   if (maxScore>omaxScore) {
//                       omaxScore = maxScore;
//                   }
                        if (minScorepeakclose < ominScorepeakclose) {
                            ominScorepeakclose = minScorepeakclose;
                        }
                        if (maxScorepeakfound > omaxScorepeakfound) {
                            omaxScorepeakfound = maxScorepeakfound;
                        }
                        if (maxScorefitabove > omaxScorefitabove) {
                            omaxScorefitabove = maxScorefitabove;
                        }

//                   TVcontroller.getMasterListofOGroups().get(i).setScore(new SimpleFloatProperty(omaxScore));
                        TVcontroller.getMasterListofOGroups().get(i).setScorepeakclose(new SimpleFloatProperty(ominScorepeakclose));
                        TVcontroller.getMasterListofOGroups().get(i).setScorepeakfound(new SimpleFloatProperty(omaxScorepeakfound));
                        TVcontroller.getMasterListofOGroups().get(i).setScorefitabove(new SimpleFloatProperty(omaxScorefitabove));

                    }

                    for (int f = 0; f < completeList.size(); f++) {
                        float omincertainty = Float.NaN;

                        if (TVcontroller.getMasterListofOGroups().get(i).getCertainties().containsKey(completeList.get(f))) {
                            omincertainty = Float.MAX_VALUE;
                            if (TVcontroller.getMasterListofOGroups().get(i).getCertainties().get(completeList.get(f)) < omincertainty) {
                                omincertainty = TVcontroller.getMasterListofOGroups().get(i).getCertainties().get(completeList.get(f));
                            }
                        }
                        if (TVcontroller.getMasterListofOGroups().get(i).getRanges().containsKey(completeList.get(f))) {
                            HashMap<RawDataFile, Float> Ranges = TVcontroller.getMasterListofOGroups().get(i).getRanges();
                            if (TVcontroller.getMasterListofOGroups().get(i).getRanges().get(completeList.get(f)) > omaxrange) {
                                omaxrange = TVcontroller.getMasterListofOGroups().get(i).getRanges().get(completeList.get(f));
                            }

                        }
                        TVcontroller.getMasterListofOGroups().get(i).setScorecertainty(new SimpleFloatProperty(omincertainty));

                    }

                    TVcontroller.getMasterListofOGroups().get(i).setScorepeakrange(new SimpleFloatProperty(omaxrange));

                }

                return null;
            }

        };

        new Thread(task).start();
        TVcontroller.getMetTable().refresh();
    }

    public void deleteBatch() {
        for (int i = 0; i < batch.getListofFiles().size(); i++) {
            batch.getListofFiles().get(i).deleteFile();

        }
        session.getListofDatasets().remove(batch);
        getBatchFileView().getItems().clear();
        TVcontroller.getAccordion().getPanes().remove(BatchPane);
        System.out.println("Deleted Batch");
    }

    /**
     * @return the batchFileView
     */
    public TableView<RawDataFile> getBatchFileView() {
        return batchFileView;
    }

    /**
     * @param batchFileView the batchFileView to set
     */
    public void setBatchFileView(TableView<RawDataFile> batchFileView) {
        this.batchFileView = batchFileView;
    }

    /**
     * @return the batact
     */
    public CheckBox getBatact() {
        return batact;
    }

    /**
     * @param batact the batact to set
     */
    public void setBatact(CheckBox batact) {
        this.batact = batact;
    }

    public void batactclick() {
        boolean val = batact.selectedProperty().get();
        for (int i = 0; i < batch.getListofFiles().size(); i++) {
            batch.getListofFiles().get(i).setActive(val);

        }

    }

    public String testpolarity(List<File> filelist) throws FileNotFoundException, IOException {
        boolean pos = false;
        boolean neg = false;

        for (File file : filelist) {
            BufferedReader BR = new BufferedReader(new FileReader(file));
            String line;
            for (int i = 0; i < 100; i++) {
                line = BR.readLine();
                if (!pos && line.contains("polarity=\"+\"")) {
                    pos = true;
                }
                if (!neg && line.contains("polarity=\"-\"")) {
                    neg = true;
                }

            }

        }

        if (pos && neg) {
            return "both";
        } else if (pos) {
            return "pos";
        } else if (neg) {
            return "neg";
        } else {
            return "non";
        }

    }
    
    public void showpolarityalert() {
        Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            
                            
                            
                        }});
        
    }

    /**
     * @return the positive
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     * @param positive the positive to set
     */
    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    /**
     * @return the cancel
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * @param cancel the cancel to set
     */
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * @return the windowopen
     */
    public boolean isWindowopen() {
        return windowopen;
    }

    /**
     * @param windowopen the windowopen to set
     */
    public void setWindowopen(boolean windowopen) {
        this.windowopen = windowopen;
    }
    
    public void openFiles(List<File> filelist, boolean positive, FloatProperty progress) throws InterruptedException, IOException {
        float test = 1 / (float) filelist.size();
                            for (File file : filelist) {
                        double start = System.currentTimeMillis();
                        batch.addFile(true, file, session, positive);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progress.set(progress.get() + test);
                            }
                        });
                        getBatchFileView().refresh();
                        System.out.println(progress.get());
                        double end = System.currentTimeMillis();
                        System.out.println(end - start);
                        //refresh files
                        getBatchFileView().setItems(batch.getListofFiles());
//session.testdeletearray();
                        if (batch.equals(session.getReference())) {
                            TVcontroller.setstep(4);
                        } else {
                            TVcontroller.setstep(5);
                        }
                    }
                            
                                                progressbar.setVisible(false);
                
                TVcontroller.loading.getAndDecrement();
                if (TVcontroller.loading.get() == 0) {
                    TVcontroller.maskerpane.setVisible(false);
                    TVcontroller.indicatorbar.setEffect(TVcontroller.shadow);
                }
        
    }
}
