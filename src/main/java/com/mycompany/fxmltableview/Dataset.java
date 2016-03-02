/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author stefankoch
 * Store information about a Dataset
 * 
 * 
 */
public class Dataset {
    
    private ObservableList<RawDataFile> listofFiles;
    private String name;

    
    
    public Dataset() {
        
        this.listofFiles = FXCollections.observableArrayList();
        
        
    }
    
    
    public void addFile(RawDataFile file) {
        this.listofFiles.add(file); 
    }
    
    /**
     * @return the listofFiles
     */
    public ObservableList<RawDataFile> getListofFiles() {
        return listofFiles;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
    
    
}
