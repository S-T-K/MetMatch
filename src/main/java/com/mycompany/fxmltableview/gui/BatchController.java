package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Batch;
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import com.mycompany.fxmltableview.logic.Session;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.jfree.fx.FXGraphics2D;


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
    TextField batdefwidth, batsetwidth, paneName, batsetpen;
    
    @FXML
    ColorPicker batdefcol, batsetcol;
    
 @FXML
    MenuItem deleteFile;
 
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

    
    //constructor, has reference to the session
    public BatchController(Session session, Dataset batch, ProgressBar bar, ObservableList<Entry> data, TitledPane tps, FXMLTableViewController tvController) {
        this.session = session;
        this.batch= batch;
        this.progressbar= bar;
        this.data = data;
        this.pane=tps;
      this.TVcontroller=tvController;
        
    }

    //initialize the table, and various elements
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        //set Factories for the tables

        
        fileColumn.setCellValueFactory(new PropertyValueFactory<RawDataFile, String>("name"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<RawDataFile, Color>("color"));
        colorColumn.setCellFactory(ColorTableCell::new);
        
        
        
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
                t.getTablePosition().getRow())
                ).setWidth((t.getNewValue().doubleValue()));
        }
    }
);

        widthColumn.setCellFactory(TextFieldTableCell.<RawDataFile, Number>forTableColumn(new NumberStringConverter()));

        
        //bind default values
        batdefwidth.textProperty().bindBidirectional(batch.getWidthProperty(), new NumberStringConverter());
        batdefcol.valueProperty().bindBidirectional(batch.getColorProperty());
        batsetpen.textProperty().bindBidirectional(batch.getPenaltyProperty(), new NumberStringConverter());
        
        //add functionality to set the color for all files
        batsetcol.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i<batch.getListofFiles().size(); i++) {
                    batch.getListofFiles().get(i).setColor(batsetcol.getValue());
                    
                }
                         
            }
        });
        
        //add functionality to set the width for all files
        batsetwidth.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i<batch.getListofFiles().size(); i++) {
                    batch.getListofFiles().get(i).setWidth((Double.parseDouble(batsetwidth.getText())));
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


                        batch.addFile(true, file, session);                    
                        progress.set(progress.get() + test);
                        getBatchFileView().refresh();
                        System.out.println(progress.get());
                        double end = System.currentTimeMillis();
                        System.out.println(end - start);
                        //refresh files
                        getBatchFileView().setItems(batch.getListofFiles());

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
       for (int i = 0; i< getBatchFileView().getSelectionModel().getSelectedItems().size(); i++) {
            list.add(getBatchFileView().getSelectionModel().getSelectedItems().get(i));
        }
        
        for (int i = 0; i< list.size(); i++) {
            list.get(i).deleteFile();
            
        }
        getBatchFileView().getSelectionModel().clearSelection();
    }
    
    public void checkforFile() {
        ObservableList<RawDataFile> list = getBatchFileView().getSelectionModel().getSelectedItems();
        if (list.size()<1) {
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
            //TODO
            //sets Score to the max over all selected Files
            public Void call() throws InterruptedException {
               List<RawDataFile> completeList = session.getAllFiles();
               for (int i =0; i<TVcontroller.getMasterListofOGroups().size(); i++) {
                   double maxScore = 0;
                   for (int f = 0; f<completeList.size(); f++) {
                       if(completeList.get(f).isselected()) {
                       RawDataFile file = completeList.get(f);
                       if (TVcontroller.getMasterListofOGroups().get(i).getScore(file)>maxScore) {
                           maxScore = TVcontroller.getMasterListofOGroups().get(i).getScore(file);
                       }}
                   TVcontroller.getMasterListofOGroups().get(i).setScore(new SimpleDoubleProperty(maxScore));
                   
                   
                   }
                   for (int j = 0; j<TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().size(); j++) {
                       maxScore = 0;
                   for (int f = 0; f<completeList.size(); f++) {
                       if(completeList.get(f).isselected()) {
                       RawDataFile file = completeList.get(f);
                       if (TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getScore(file)>maxScore) {
                           maxScore = TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).getScore(file);
                       }}
                   TVcontroller.getMasterListofOGroups().get(i).getListofAdducts().get(j).setScore(new SimpleDoubleProperty(maxScore));
                   }
                   }
                   
                   
               }
              
                return null;
            }

        };   
         
         new Thread(task).start();
         TVcontroller.getMetTable().refresh();
         }
    
    public void deleteBatch() {
        for (int i = 0; i< batch.getListofFiles().size(); i++) {
            batch.getListofFiles().get(i).deleteFile();
           
        }
        session.getListofDatasets().remove(batch);
        getBatchFileView().getItems().clear();
        TVcontroller.getAccordion().getPanes().remove(pane);
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
}
