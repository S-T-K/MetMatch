package com.mycompany.fxmltableview;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

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
    Button referencemzxmlButton, addBatchButton;
    
    @FXML
    Label mzxmlLabel, mzxmlPathLabel;
    
    
    

    //List with data for table, Ogroups (adducts within the Ogroups)
    ObservableList<Entry> data;
    

    
    Session session;
    
    //add new Metabolite
    public void addMet(String name, double score) {
        Met met = new Met();
        met.setName(name);
        met.setScore(score);
        met.setProp1(100);
        //data.add(met);

    }

    //initialize the table
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, String>("OGroup"));  //String in brackets has to be the same as PropertyValueFactory property= "..." in fxml
        scoreColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Double>("Score"));
        rtColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Double>("RT"));
        mzColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Entry, Double>("MZ"));

        //make referencePane expanded
        accordion.setExpandedPane(ReferencePane);
        referencemzxmlButton.setDisable(true);
        addBatchButton.setDisable(true);
        
        
        //highlight the Button
        Platform.runLater(new Runnable() {
        @Override
        public void run() {
            referenceButton.requestFocus();
        }
    });
        //metTable.setItems(data);
        
        session = new Session();
       
    }
    
    public void openReferenceDataMatrixChooser () throws FileNotFoundException {
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
        for (int i = 0; i<data.size(); i++) {
        TreeItem<Entry> root = new TreeItem<>(data.get(i));
        root.setExpanded(false);  
        superroot.getChildren().add(root);
        
            for (int j=0; j<data.get(i).getListofAdducts().size(); j++) {
                TreeItem<Entry> childNode1 = new TreeItem<>(data.get(i).getListofAdducts().get(j));
                root.getChildren().add(childNode1);
                
            }
           
        }
        metTable.setRoot(superroot);
        metTable.setShowRoot(false);
        referenceButton.setDisable(true);
        referenceButton.setVisible(false);
        referencemzxmlButton.setDisable(false);
        referencemzxmlButton.requestFocus();
        DataMatrixLabel.setText("Data Matrix:");
        DataMatrixPathLabel.setText(file.toString());
        
        
        
    }
    
     public void openReferencemzxmlChooser () throws FileNotFoundException {
         FileChooser fileChooser = new FileChooser();
 
              //Set extension filter
              FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("mzXML files (*.mzxml)", "*.mzxml");
              fileChooser.getExtensionFilters().add(extFilter);
             
              //Show open file dialog
              File file = fileChooser.showOpenDialog(null);
              mzxmlLabel.setText("mzXML file:");
              mzxmlPathLabel.setText(file.toString());
              referencemzxmlButton.setDisable(false);
              referencemzxmlButton.setVisible(false);
              referenceButton.setVisible(false);
              addBatchButton.setDisable(false);
              
              double start = System.currentTimeMillis();
              RawDataFile newfile = new RawDataFile(file);
              newfile.parseFile();
              newfile.extractSlices(data, 1, 0.1f);
              System.out.println("Done!");
              double end = System.currentTimeMillis();
              System.out.println(end-start);
              //metTable.getTreeItem(0).getValue().setRT(new SimpleDoubleProperty(999));
              //metTable.getTreeItem(0).getChildren().get(0).getValue().setScore(new SimpleDoubleProperty(-100));
              
              
         
     }
     
     public void addBatch() {
         AnchorPane test = new AnchorPane();
         TitledPane tps = new TitledPane("tset",test);
         tps.setExpanded(true);
         accordion.getPanes().add(tps);
         
         
     }
     
     public void printInfo() {
         
         
     }
     
     
}
