package com.mycompany.fxmltableview.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.logic.Session;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author stefankoch
 */
public class Fxml_peakviewController implements Initializable {

    
    @FXML
    VBox box;
    
    @FXML
    Label info;
    
    ChartGenerator chartGenerator;
    private ObservableList<Entry> olist;
    private Session session;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chartGenerator = new ChartGenerator(null,null,null);
        box.setMaxHeight(2000);
        
       
    }    

    /**
     * @param olist the olist to set
     */
    public void setOlist(ObservableList<Entry> olist) {
        this.olist = olist;
    }
    
    public void print() {
        box.getChildren().add(0,chartGenerator.generateScatterPeakChart(olist));
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
        chartGenerator.setSession(session);
        List<RawDataFile> list = session.getSelectedFiles();
        if (list.size()>0) {
        String string = list.get(0).getName();
        for (int i = 1; i< list.size(); i++) {
            string = string.concat(", " + list.get(i).getName());
        }
        info.setText(string);
        info.setMinHeight(20*list.size());
        }
    }
    
}
