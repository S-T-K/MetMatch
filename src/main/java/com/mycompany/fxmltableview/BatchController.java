package com.mycompany.fxmltableview;

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
public class BatchController implements Initializable {

    //link fxml information to controller
 
    @FXML
    MenuBar batchMenu;
    
    @FXML
    TableView<RawDataFile> batchFileView;
    
    @FXML
    TableColumn<RawDataFile, String> fileColumn;
    
    @FXML
    TableColumn widthColumn;
    
    @FXML
    TableColumn<RawDataFile, Color> colorColumn;
    
    @FXML
    TextField batdefwidth, batsetwidth, paneName;
    
    @FXML
    ColorPicker batdefcol, batsetcol;
    
 
  
  
    //current session, storing all information
    Session session;
    Batch batch;
    ProgressBar progressbar;
    TitledPane pane;
    
    //List with data for table, Ogroups (adducts within the Ogroups)
    ObservableList<Entry> data;

    
    //constructor, has reference to the session
    public BatchController(Session session, Batch batch, ProgressBar bar, ObservableList<Entry> data, TitledPane tps) {
        this.session = session;
        this.batch= batch;
        this.progressbar= bar;
        this.data = data;
        this.pane=tps;
        
    }

    //initialize the table, and various elements
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        //set Factories for the tables

        
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
                t.getTablePosition().getRow())
                ).setWidth(new SimpleDoubleProperty(t.getNewValue().doubleValue()));
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
                for (int i = 0; i<batch.getListofFiles().size(); i++) {
                    batch.getListofFiles().get(i).setColor(batsetcol.getValue());
                    
                }
                         
            }
        });
        
        //add functionality to set the width for all files
        batsetwidth.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i<batch.getListofFiles().size(); i++) {
                    batch.getListofFiles().get(i).setWidth(new SimpleDoubleProperty(Double.parseDouble(batsetwidth.getText())));
                    batchFileView.refresh();
                    
                }
                         
            }
        });

        paneName.textProperty().bindBidirectional(batch.getNameProperty());
        pane.textProperty().bind(batch.getNameProperty());
        
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
        List<File> filelist  = fileChooser.showOpenMultipleDialog(null);
        
        
        // create a new task
        Task task = new Task<Void>() {
    @Override public Void call() {
        double test = 1/(double)filelist.size();
        
        if (filelist != null) {
                        for (File file : filelist) {
                            double start = System.currentTimeMillis();
                            
                            batch.addFile(false, file, data, session.getRTTolerance(), session.getMZTolerance());
                            progress.set(progress.get()+test);
                            System.out.println(progress.get());
        double end = System.currentTimeMillis();
        System.out.println(end - start);
        //refresh files
       batchFileView.setItems(batch.getListofFiles());
                           
                        }
                        
                        RawDataFile bat = batch.getListofFiles().get(0);
                        
                        
                     
                        int currentGroup = 0;
                        int currentAdduct = 0;
                        int currentGroupMax = session.getListofOGroups().get(0).getListofAdducts().size()-1;
                        
                        
                        for (int i =0; i< bat.getListofSlices().size(); i++) {
                            Slice batch = bat.getListofSlices().get(i);
                            batch.generateInterpolatedEIC();

                            
  
                           
                            EICComparer comp = new EICComparer();
                            double cor= comp.compare(data.get(currentGroup).getListofAdducts().get(currentAdduct), batch);
                            System.out.println("Correlation: " + cor);
                           
                            session.getListofOGroups().get(currentGroup).getListofAdducts().get(currentAdduct).setScore(new SimpleDoubleProperty(comp.getEICQuality(batch)));
                            
                            
                            
                            currentAdduct++;
                            if (currentAdduct > currentGroupMax) {
                                currentGroup++;
                                currentAdduct = 0;
                                currentGroupMax = session.getListofOGroups().get(currentGroup).getListofAdducts().size()-1;
                                
                            }
                        }
                        System.out.println("Done");
                        


                      
    }
        return null;
    }
    
    };
        
        //new thread that executes task
        new Thread(task).start();
        
    }
    

    public void printInfo() {

    }

}
