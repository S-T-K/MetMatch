/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

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
    
    private Property<Color> color;
    private DoubleProperty Width;

    
    
    public Dataset() {
        
        this.listofFiles = FXCollections.observableArrayList();
        this.Width = new SimpleDoubleProperty(2.0);
        this.color = new SimpleObjectProperty(Color.BLACK);
        
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
    
    public final Color getColor() {
	return color.getValue();
    }

    public final void setColor(Color color) {
	this.color.setValue(color);
    }
    
    public Property<Color> colorProperty() {
	return color;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return Width.get();
    }

    /**
     * @param width the width to set
     */
    public void setWidth(DoubleProperty width) {
        this.Width = width;
    }
    
    
}
