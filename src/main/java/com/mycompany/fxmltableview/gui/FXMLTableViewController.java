package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Batch;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.logic.Session;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import org.jfree.fx.FXGraphics2D;

//this is the Controller for the Main GUI
public class FXMLTableViewController implements Initializable {

    //link fxml information to controller
    @FXML
    TreeTableView<Entry> metTable;

    @FXML
    TreeTableColumn nameColumn;

    @FXML
    TreeTableColumn scoreColumn;

    @FXML
    TreeTableColumn rtColumn;

    @FXML
    TreeTableColumn mzColumn;

    @FXML
    Button referenceButton;

    @FXML
    Label DataMatrixLabel;

    @FXML
    Label DataMatrixPathLabel;

    @FXML
    TitledPane ReferencePane;

    @FXML
    Accordion accordion;

    @FXML
    Button addBatchButton;

    @FXML
    MenuBar referenceMenu;

    @FXML
    TableView<RawDataFile> referenceFileView;

    @FXML
    TableColumn<RawDataFile, String> fileColumn;

    @FXML
    TableColumn widthColumn;

    @FXML
    TableColumn<RawDataFile, Color> colorColumn;

    @FXML
    TextField refdefwidth, refsetwidth, paneName;

    @FXML
    ColorPicker refdefcol, refsetcol;

    @FXML
    ProgressBar progressbar;

    //List with MasterListofOGroups for table, Ogroups (adducts within the Ogroups)
    ObservableList<Entry> MasterListofOGroups;

    //current session, storing all information
    Session session;
    FXGraphics2D test;

    //number of current batches, as an index
    int batchcount;

    //initialize the table, and various elements
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //set Factories for the tables
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, String>("OGroup"));  //String in brackets has to be the same as PropertyValueFactory property= "..." in fxml
        scoreColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Double>("Score"));
        rtColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Double>("RT"));
        mzColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Double>("MZ"));

        fileColumn.setCellValueFactory(new PropertyValueFactory<RawDataFile, String>("name"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<RawDataFile, Color>("color"));
        colorColumn.setCellFactory(ColorTableCell::new);

        //enables edit functionality for width cell
        widthColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        widthColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<RawDataFile, Number>>() {
            @Override
            public void handle(CellEditEvent<RawDataFile, Number> t) {
                ((RawDataFile) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setWidth(new SimpleDoubleProperty(t.getNewValue().doubleValue()));
            }
        }
        );

        widthColumn.setCellFactory(TextFieldTableCell.<RawDataFile, Number>forTableColumn(new NumberStringConverter()));

        //make referencePane expanded
        accordion.setExpandedPane(ReferencePane);

        //disable not needed elements
        addBatchButton.setDisable(true);
        referenceMenu.setDisable(true);
        referenceMenu.setVisible(false);
        referenceFileView.setDisable(true);
        referenceFileView.setVisible(false);

        //highlight the Button, can't be done the normal way
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                referenceButton.requestFocus();
            }
        });

        //create new Session
        session = new Session();
        session.getReference().setName("Reference");

        //set batchcount to 0,
        batchcount = 0;

        //bind default values
        refdefwidth.textProperty().bindBidirectional(session.getReference().getWidthProperty(), new NumberStringConverter());
        refdefcol.valueProperty().bindBidirectional(session.getReference().getColorProperty());

        //add functionality to set the color for all files
        refsetcol.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i < session.getReference().getListofFiles().size(); i++) {
                    session.getReference().getListofFiles().get(i).setColor(refsetcol.getValue());

                }

            }
        });

        //add functionality to set the width for all files
        refsetwidth.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i < session.getReference().getListofFiles().size(); i++) {
                    session.getReference().getListofFiles().get(i).setWidth(new SimpleDoubleProperty(Double.parseDouble(refsetwidth.getText())));
                    referenceFileView.refresh();

                }

            }
        });

        paneName.textProperty().bindBidirectional(session.getReference().getNameProperty());
        ReferencePane.textProperty().bind(session.getReference().getNameProperty());

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
        MasterListofOGroups = session.parseReferenceTsv();
        session.setRTTolerance(1.5f);
        session.setMZTolerance(0.002f);

        //Convert List into TreeTable Entries
        TreeItem<Entry> superroot = new TreeItem<>();

        //for all OGroups
        for (int i = 0; i < MasterListofOGroups.size(); i++) {
            TreeItem<Entry> root = new TreeItem<>(MasterListofOGroups.get(i));
            root.setExpanded(false);
            superroot.getChildren().add(root);

            for (int j = 0; j < MasterListofOGroups.get(i).getListofAdducts().size(); j++) {
                TreeItem<Entry> childNode1 = new TreeItem<>(MasterListofOGroups.get(i).getListofAdducts().get(j));
                root.getChildren().add(childNode1);

            }

        }
        metTable.setRoot(superroot);
        metTable.setShowRoot(false);
        referenceButton.setDisable(true);
        referenceButton.setVisible(false);
        DataMatrixLabel.setText("Data Matrix:");
        DataMatrixPathLabel.setText(file.getName());
        referenceMenu.setDisable(false);
        referenceMenu.setVisible(true);
        referenceFileView.setDisable(false);
        referenceFileView.setVisible(true);
        addBatchButton.setDisable(false);

        //add double click functionality to the TreeTable
        metTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {

                    try {
                        //get selected item
                        TreeItem<Entry> item = metTable.getSelectionModel().getSelectedItem();

                        //create new window
                        Stage stage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_adductview.fxml"));
                        Pane myPane = (Pane) loader.load();
                        Scene myScene = new Scene(myPane);
                        stage.setScene(myScene);
                        Fxml_adductviewController controller = loader.<Fxml_adductviewController>getController();

                        //add MasterListofOGroups to new controller
                        controller.metTable = metTable;

                        //print graphs
                        controller.print();
                        stage.show();

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });

    }

    // File Chooser for mzXML files
    public void openReferencemzxmlChooser() throws FileNotFoundException {

        //Property to link with progressbar
        DoubleProperty progress = new SimpleDoubleProperty(0.0);
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
            public Void call() {
                double test = 1 / (double) filelist.size();
               
                if (filelist != null) {
                    for (File file : filelist) {
                        double start = System.currentTimeMillis();


                        session.getReference().addFile(true, file, session);                    
                        progress.set(progress.get() + test);
                        System.out.println(progress.get());
                        double end = System.currentTimeMillis();
                        System.out.println(end - start);
                        //refresh files
                        referenceFileView.setItems(session.getReference().getListofFiles());

                    }

                    referenceButton.setVisible(false);
                    addBatchButton.setDisable(false);

                }
                finalizeReference();
                return null;
            }

        };

        //new thread that executes task
        new Thread(task).start();

        
    }

    //add a new batch
    public void addBatch() {

        try {
            AnchorPane test = new AnchorPane();
            TitledPane tps = new TitledPane("", test);
            tps.setExpanded(true);
            accordion.getPanes().add(tps);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Batch.fxml"));

            Batch batch = new Batch(batchcount);
            batch.setName("Batch Nr. " + (batchcount + 1));
            session.addBatch(batch);
            batchcount++;
            loader.setController(new BatchController(session, batch, progressbar, MasterListofOGroups, tps));

            loader.setRoot(test);
            test = (AnchorPane) loader.load();
        } catch (IOException ex) {
            Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void printInfo() {

    }

    
    //all Adducts generate AvgEIC over all Slices
    public void finalizeReference() {
        for (int i = 0; i<MasterListofOGroups.size(); i++) {
            for (int j = 0; j<MasterListofOGroups.get(i).getListofAdducts().size(); j++) {
                Entry currentAdduct = MasterListofOGroups.get(i).getListofAdducts().get(j);
               
                
            }
            
        }
        
    }
}