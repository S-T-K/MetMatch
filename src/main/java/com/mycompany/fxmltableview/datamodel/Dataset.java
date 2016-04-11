/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import com.mycompany.fxmltableview.logic.Session;
import java.io.File;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    
    //holds files
    private ObservableList<RawDataFile> listofFiles;
    
    //name of Dataset
    private StringProperty name;
    
    //Default values for new Files
    private Property<Color> color;
    private DoubleProperty Width;
    
    //penalty for path calculation
    private SimpleDoubleProperty penalty;
    
    private Property<Boolean> active;
    

    
    
    //Constructor
    public Dataset() {
        
        this.listofFiles = FXCollections.observableArrayList();
        //default values for new Dataset
        this.Width = new SimpleDoubleProperty(1.5);
        this.color = new SimpleObjectProperty(Color.BLACK);
        this.penalty = new SimpleDoubleProperty(0.5);
        active = new SimpleBooleanProperty(true);
        
    }
    
    
    //add new File and parse it
    public void addFile(boolean isreference, File file, Session session) {
        RawDataFile newfile = new RawDataFile(this, file, session);
        this.listofFiles.add(newfile); 
        newfile.parseFile();
        System.out.println("parsed");
        newfile.extractSlices(isreference, session.getListofOGroups(), session.getRTTolerance(), session.getMZTolerance());
        
        
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
        return name.get();
    }
    
    public StringProperty getNameProperty() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = new SimpleStringProperty(name);
    }
    
    public final Color getColor() {
	return color.getValue();
    }

    public final void setColor(Color color) {
	this.color.setValue(color);
    }
    
    public Property<Color> getColorProperty() {
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
    
    public DoubleProperty getWidthProperty() {
        return Width;
    }

    public double getPenalty() {
        return this.penalty.get();
    }
    
    /**
     * @return the penalty
     */
    public SimpleDoubleProperty getPenaltyProperty() {
        return penalty;
    }

    /**
     * @param penalty the penalty to set
     */
    public void setPenalty(double penalty) {
        this.penalty = new SimpleDoubleProperty(penalty);
    }
    
    public final Boolean getActive() {
	return active.getValue();
    }

    public final void setActive(Boolean bool) {
	this.active.setValue(bool);
    }
    
    public Property<Boolean> activeProperty() {
	return active;
    }
}
