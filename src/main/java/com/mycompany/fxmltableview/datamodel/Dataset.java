/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import com.mycompany.fxmltableview.gui.BatchController;
import com.mycompany.fxmltableview.logic.Session;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
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
    private FloatProperty Width;
    
    //penalty for path calculation
    private SimpleFloatProperty penalty;
    
    private Property<Boolean> active;
    private BatchController controller;
    

    
    
    //Constructor
    public Dataset() {
        
        this.listofFiles = FXCollections.observableArrayList();
        //default values for new Dataset
        this.Width = new SimpleFloatProperty(1.5f);
        this.color = new SimpleObjectProperty(Color.BLACK);
        this.penalty = new SimpleFloatProperty(0.5f);
        active = new SimpleBooleanProperty(true);
        
    }
    
    
    //add new File and parse it
    public void addFile(boolean isreference, File file, Session session, boolean positive) throws InterruptedException, IOException {
        RawDataFile newfile = new RawDataFile(this, file, session, positive);
        this.listofFiles.add(newfile); 
        newfile.parseFile(positive);
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
    public float getWidth() {
        return Width.get();
    }

    /**
     * @param width the width to set
     */
    public void setWidth(FloatProperty width) {
        this.Width = width;
    }
    
    public FloatProperty getWidthProperty() {
        return Width;
    }

    public float getPenalty() {
        return this.penalty.get();
    }
    
    /**
     * @return the penalty
     */
    public SimpleFloatProperty getPenaltyProperty() {
        return penalty;
    }

    /**
     * @param penalty the penalty to set
     */
    public void setPenalty(float penalty) {
        this.penalty = new SimpleFloatProperty(penalty);
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

    /**
     * @return the controller
     */
    public BatchController getController() {
        return controller;
    }

    /**
     * @param controller the controller to set
     */
    public void setController(BatchController controller) {
        this.controller = controller;
    }
}
