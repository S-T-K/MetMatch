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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    Button  addBatchButton;
    
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
    TextField refdefwidth, refsetwidth;
    
    @FXML
    ColorPicker refdefcol, refsetcol;
    

    //List with data for table, Ogroups (adducts within the Ogroups)
    ObservableList<Entry> data;
  
    //current session, storing all information
    Session session;
    FXGraphics2D test;


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
                t.getTablePosition().getRow())
                ).setWidth(new SimpleDoubleProperty(t.getNewValue().doubleValue()));
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
        
        //bind default values
        refdefwidth.textProperty().bindBidirectional(session.getReference().getWidthProperty(), new NumberStringConverter());
        refdefcol.valueProperty().bindBidirectional(session.getReference().getColorProperty());
        
        
        //add functionality to set the color for all files
        refsetcol.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i<session.getReference().getListofFiles().size(); i++) {
                    session.getReference().getListofFiles().get(i).setColor(refsetcol.getValue());
                    
                }
                         
            }
        });
        
        //add functionality to set the width for all files
        refsetwidth.setOnAction(new EventHandler() {
            public void handle(Event t) {
                for (int i = 0; i<session.getReference().getListofFiles().size(); i++) {
                    session.getReference().getListofFiles().get(i).setWidth(new SimpleDoubleProperty(Double.parseDouble(refsetwidth.getText())));
                    referenceFileView.refresh();
                    
                }
                         
            }
        });

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
        data = session.parseReferenceTsv();
       

        //Convert List into TreeTable Entries
        TreeItem<Entry> superroot = new TreeItem<>();

        //for all OGroups
        for (int i = 0; i < data.size(); i++) {
            TreeItem<Entry> root = new TreeItem<>(data.get(i));
            root.setExpanded(false);
            superroot.getChildren().add(root);

            for (int j = 0; j < data.get(i).getListofAdducts().size(); j++) {
                TreeItem<Entry> childNode1 = new TreeItem<>(data.get(i).getListofAdducts().get(j));
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
                        
                        //add data to new controller
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
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("mzXML files (*.mzxml)", "*.mzxml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        List<File> filelist  = fileChooser.showOpenMultipleDialog(null);
        
        if (filelist != null) {
                        for (File file : filelist) {
                            double start = System.currentTimeMillis();
                            
                            session.getReference().addFile(file, data);
                            
                            System.out.println("Done!");
        double end = System.currentTimeMillis();
        System.out.println(end - start);
       
                            
                        }
                        
                        
        referenceButton.setVisible(false);
        addBatchButton.setDisable(false);
        referenceFileView.setItems(session.getReference().getListofFiles());

      

    }
    }
    
    //add a new batch
    public void addBatch() {
        AnchorPane test = new AnchorPane();
        TitledPane tps = new TitledPane("tset", test);
        tps.setExpanded(true);
        accordion.getPanes().add(tps);

    }

    public void printInfo() {

    }

}
