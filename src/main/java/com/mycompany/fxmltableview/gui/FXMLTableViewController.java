package com.mycompany.fxmltableview.gui;

import com.mycompany.fxmltableview.datamodel.Batch;
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.Entry.orderbyRT;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.logic.Session;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
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
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
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
    private TreeTableView<Entry> metTable;

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
    private Accordion accordion;

    @FXML
    Button addBatchButton;

    @FXML
    MenuBar referenceMenu;

    @FXML
    TableView<RawDataFile> referenceFileView;

    @FXML
    TableColumn<RawDataFile, String> fileColumn;

    @FXML
    TableColumn widthColumn, shiftColumn;

    @FXML
    TableColumn<RawDataFile, Color> colorColumn;

    @FXML
    TextField refdefwidth, refsetwidth, paneName, refsetpen;

    @FXML
    ColorPicker refdefcol, refsetcol;

    @FXML
    ProgressBar progressbar;
    
    @FXML
    MenuItem deleteFile;
    
    @FXML
    ContextMenu fileContextMenu;

    //List with MasterListofOGroups for table, Ogroups (adducts within the Ogroups)
    private ObservableList<Entry> MasterListofOGroups;

    //current session, storing all information
    Session session;
    FXGraphics2D test;
    HashMap<TitledPane,Dataset> panelink;
   

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
        getAccordion().setExpandedPane(ReferencePane);

        //disable not needed elements
        addBatchButton.setDisable(true);
        referenceMenu.setDisable(true);
        referenceMenu.setVisible(false);
        referenceFileView.setDisable(true);
        referenceFileView.setVisible(false);
        
        
        //enable FileView functionality
        referenceFileView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
       


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
        session.setResolution(100);
        session.setRTTolerance(1.5f);
        session.setMZTolerance(10); //ppm
        
        panelink = new HashMap<>();
        panelink.put(ReferencePane, session.getReference());

        //set batchcount to 0,
        batchcount = 0;

        //bind default values
        refdefwidth.textProperty().bindBidirectional(session.getReference().getWidthProperty(), new NumberStringConverter());
        refdefcol.valueProperty().bindBidirectional(session.getReference().getColorProperty());
        refsetpen.textProperty().bindBidirectional(session.getReference().getPenaltyProperty(), new NumberStringConverter());

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
        
        //add functionality to change the active dataset
        getAccordion().expandedPaneProperty().addListener(new 
            ChangeListener<TitledPane>() {
                public void changed(ObservableValue<? extends TitledPane> ov,
                    TitledPane old_val, TitledPane new_val) {
                        Dataset dataset;
                        if(new_val!=null){
                        dataset = panelink.get(new_val);
                        session.setCurrentdataset(dataset);}
                    
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
        setMasterListofOGroups(session.parseReferenceTsv());
        

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

                        //add MasterListofOGroups to new controller
                        controller.metTable = getMetTable();

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
                        Task task = new Task<Void>() {
            @Override
            public Void call() {
                        double start = System.currentTimeMillis();


                        session.getReference().addFile(true, file, session);                    
                        progress.set(progress.get() + test);
                        referenceFileView.refresh();
                        System.out.println(progress.get());
                        double end = System.currentTimeMillis();
                        System.out.println(end - start);
                        //refresh files
                        referenceFileView.setItems(session.getReference().getListofFiles());
                        return null;
            }

        };
new Thread(task).start();
                    }

                    referenceButton.setVisible(false);
                    addBatchButton.setDisable(false);

                }
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
            getAccordion().getPanes().add(tps);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Batch.fxml"));
            Batch batch = new Batch(batchcount);
            getAccordion().setExpandedPane(tps);
            batch.setName("Batch Nr. " + (batchcount + 1));
            session.addBatch(batch);
            batchcount++;
            loader.setController(new BatchController(session, batch, progressbar, getMasterListofOGroups(), tps, this));
            loader.setRoot(test);
            test = (AnchorPane) loader.load();
            panelink.put(tps, batch);
        } catch (IOException ex) {
            Logger.getLogger(FXMLTableViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

 //calculates Shift and opens a new window
   public void newwindowcalculate() throws IOException, InterruptedException {
       CountDownLatch latch = new CountDownLatch(1);
        Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
                calculate(latch);
                
                
                return null;
            }
        };
       new Thread(task).start();
       latch.await();
        
        //open new window
              Stage stage = new Stage();
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxml_shiftview.fxml"));
              Pane myPane = (Pane) loader.load();
              Scene myScene = new Scene(myPane);
              stage.setScene(myScene);
              Fxml_shiftviewController controller = loader.<Fxml_shiftviewController>getController();
              controller.setSupercontroller(this);

              //print graphs
              controller.print(getMasterListofOGroups());
              System.out.println("PRINT");
              stage.show();
      
       
   }
    
    
    //does the Shift calculation
  public void calculate(CountDownLatch latch) throws IOException, InterruptedException {
      
      
      
      Task task = new Task<Void>() {
            @Override
            public Void call() throws IOException {
                
                
                    for (int f = 0; f<session.getCurrentdataset().getListofFiles().size(); f++) {
                        RawDataFile currentfile = session.getCurrentdataset().getListofFiles().get(f);
                    
                Collections.sort(getMasterListofOGroups(), new orderbyRT());
                double[][] matrix = new double[getMasterListofOGroups().size()][session.getResolution()];
                
                DoubleProperty progress = new SimpleDoubleProperty(0.0);
                progressbar.progressProperty().bind(progress);
        
                for (int i = 0; i<getMasterListofOGroups().size(); i++) {
                   

                        getMasterListofOGroups().get(i).generateOGroupPropArray(currentfile);
                    double[] PropArray = getMasterListofOGroups().get(i).getOGroupPropArray(currentfile);
                    for (int j =0; j<session.getResolution(); j++) {
                        matrix[i][j] = PropArray[j];
                        
                    }
                    
                    progress = new SimpleDoubleProperty((double)i/getMasterListofOGroups().size());
                    System.out.println(progress.get()*100 + "%");
                }
                
                
                //Test artificial shift
//                double[][] matrix2 = new double[MasterListofOGroups.size()][session.getResolution()];
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
                double[][] weights = new double[getMasterListofOGroups().size()][session.getResolution()];
                //fill first row
                for (int j =0; j<session.getResolution(); j++) {
                    weights[0][j] = matrix[0][j];
                    
                }
                //TODO: Penalty for change in j
                //fill rest of weights matrix
                double penalty = session.getCurrentdataset().getPenalty();
                for (int i = 1; i<getMasterListofOGroups().size(); i++) {
                    for (int j =0; j<session.getResolution(); j++) {
                        double max = 0;
                        if(weights[i-1][j]>max){
                            max=weights[i-1][j]+matrix[i][j];}
                        if((j-1)>0 && weights[i-1][j-1]+matrix[i][j]-penalty>max){
                            max = weights[i-1][j-1]+matrix[i][j]-penalty;}
                        if ((j+1)<session.getResolution() && weights[i-1][j+1]+matrix[i][j]-penalty>max){
                            max = weights[i-1][j+1]+matrix[i][j]-penalty;
                        }
                        weights[i][j] = max;
                        
                        
                        
                    }
  
                }
                //get max in last row
                double max = 0;
                int maxint = 0;
                for (int j =0; j<session.getResolution(); j++) {
                    if (weights[getMasterListofOGroups().size()-1][j]> max) {
                        maxint = j;
                        max = weights[getMasterListofOGroups().size()-1][j];
                    }
                }
                System.out.println(maxint);
                    getMasterListofOGroups().get(getMasterListofOGroups().size()-1).setFittedShift(currentfile,maxint);
                
                
             
                
                //TODO: calculate range as function of time
                for (int i = getMasterListofOGroups().size()-2; i>-1; i--){
                    max = 0;
                    int range = 0;
                    int j = maxint;
                    if((j-1)>0 && getmaxofrange(weights, i, j-1, range)> max) {
                        max = getmaxofrange(weights, i, j-1, range);
                        maxint = j-1;
                    }
                    if(getmaxofrange(weights, i, j, range)> max) {
                        max = getmaxofrange(weights, i, j, range);
                        maxint = j;
                    }
                      if ((j + 1) < session.getResolution() && getmaxofrange(weights, i, j+1, range) > max) {
                          max = getmaxofrange(weights, i, j+1, range);
                          maxint = j + 1;
                      }
                      System.out.println(maxint);
                        getMasterListofOGroups().get(i).setFittedShift(currentfile, maxint);
                      
                      //set score for OPGroup
                        getMasterListofOGroups().get(i).addScore(currentfile,(getMasterListofOGroups().get(i).getOGroupPropArray(currentfile)[getMasterListofOGroups().get(i).getFittedShift(currentfile)]));
                      
                      //set score for every addact
                      for (int a = 0; a<getMasterListofOGroups().get(i).getListofAdducts().size(); a++) {
                            getMasterListofOGroups().get(i).getListofAdducts().get(a).addScore(currentfile,(getMasterListofOGroups().get(i).getListofAdducts().get(a).getAdductPropArray(currentfile)[getMasterListofOGroups().get(i).getFittedShift(currentfile)]));
                      }
                      
                        getMetTable().refresh();
                      

                  }

              }

              
              latch.countDown();
              return null;
          }
            

        };

        //new thread that executes task
        new Thread(task).start();
        
  }
    
    public double getmaxofrange(double[][] weights, int row, int col, int range) {
        double max = 0;
        
        for ( int i =(col-range); i<=(col+range); i++ ) {
            if (i<session.getResolution() && i>= 0 && weights[row][i]>max) {
                max += weights[row][i];
            }
            
        }
        
        
        
        return max;
    }
    
    
    public void deleteFile() {
        ObservableList<RawDataFile> list = referenceFileView.getSelectionModel().getSelectedItems();
        for (int i = 0; i< list.size(); i++) {
            list.get(i).deleteFile();
            
        }
        referenceFileView.getSelectionModel().clearSelection();
    }
    
    public void checkforFile() {
        ObservableList<RawDataFile> list = referenceFileView.getSelectionModel().getSelectedItems();
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
            //sets Score to the max over all selected Files
            public Void call() throws InterruptedException {
               session.setSelectedFiles(referenceFileView.getSelectionModel().getSelectedItems());
               for (int i =0; i<getMasterListofOGroups().size(); i++) {
                   double maxScore = 0;
                   for (int f = 0; f<session.getSelectedFiles().size(); f++) {
                       RawDataFile file = session.getSelectedFiles().get(f);
                       if (getMasterListofOGroups().get(i).getScore(file)>maxScore) {
                           maxScore = getMasterListofOGroups().get(i).getScore(file);
                       }
                        getMasterListofOGroups().get(i).setScore(new SimpleDoubleProperty(maxScore));
                   }
                   for (int j = 0; j<getMasterListofOGroups().get(i).getListofAdducts().size(); j++) {
                       maxScore = 0;
                   for (int f = 0; f<session.getSelectedFiles().size(); f++) {
                       RawDataFile file = session.getSelectedFiles().get(f);
                       if ( getMasterListofOGroups().get(i).getListofAdducts().get(j).getScore(file)>maxScore) {
                           maxScore = getMasterListofOGroups().get(i).getListofAdducts().get(j).getScore(file);
                       }
                            getMasterListofOGroups().get(i).getListofAdducts().get(j).setScore(new SimpleDoubleProperty(maxScore));
                   }
                   }
                   
                   
               }
              
                return null;
            }

        };   
         
         new Thread(task).start();
         getMetTable().refresh();
         }

    /**
     * @return the metTable
     */
    public TreeTableView<Entry> getMetTable() {
        return metTable;
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
    
}
