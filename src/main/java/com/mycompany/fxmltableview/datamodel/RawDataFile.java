/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import com.mycompany.fxmltableview.logic.DomParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 *
 * @author stefankoch
 * 
 * A File
 * Basically just a list of Slices, all other information is only needed when constructing a new file
 * 
 * TODO:
 * Implement Labels for Files (sick and healthy...)
 */
public class RawDataFile {

    private File file;
    private List<Scan> listofScans;
    private List<Slice> listofSlices;
    private StringProperty name;
    
    
    private final Property<Color> color;
    private DoubleProperty Width;
   
    

    //Constructor for new Raw Data file
    public RawDataFile(File file, Property color,  DoubleProperty width) {
        this.file=file;
        this.name = new SimpleStringProperty(file.getName());
        this.color= new SimpleObjectProperty(color.getValue());
        this.Width = new SimpleDoubleProperty(width.get());
    }

    // parse Scans
    public void parseFile() {
        DomParser dpe = new DomParser(file.toString());
        this.listofScans = dpe.ParseFile();
        dpe=null;
    }

    //extract Slices, according to tolerances
    public void extractSlices(boolean isreference, List<Entry> data, float RTTolerance, float MZTolerance) {
        this.setListofSlices(new ArrayList<>());


        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).getListofAdducts().size(); j++) {
                int Num = data.get(i).getListofAdducts().get(j).getNum();
                float MZ = (float) data.get(i).getListofAdducts().get(j).getMZ();
                float RT = (float) data.get(i).getListofAdducts().get(j).getOGroupRT();   //RT in Minutes
                Slice newSlice = new Slice(this,Num, MZ, MZTolerance, RT, RTTolerance); 
                newSlice.extractSlicefromScans(listofScans);
                data.get(i).getListofAdducts().get(j).addSlice(newSlice);
                getListofSlices().add(newSlice);
                
                //add to reference Slices if reference
                if (isreference) {
                    data.get(i).getListofAdducts().get(j).addRefSlice(newSlice);
                    
                }
                
            }
     
        }
       
        
this.listofScans=null; //get rid of Scans, they are not needed any more

    }

    /**
     * @return the name
     */
    public String getName() {
        return name.get();
       
    }

    /**
     * @param name the name to set
     */
    public void setName(StringProperty name) {
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

    /**
     * @return the listofSlices
     */
    public List<Slice> getListofSlices() {
        return listofSlices;
    }

    /**
     * @param listofSlices the listofSlices to set
     */
    public void setListofSlices(List<Slice> listofSlices) {
        this.listofSlices = listofSlices;
    }
}
