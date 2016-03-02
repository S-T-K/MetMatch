/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author stefankoch
 * 
 * TODO:
 * Implement Labels for Files (sick and healthy...)
 */
public class RawDataFile {

    private File file;
    private List<Scan> listofScans;
    private List<Slice> listofSlices;
    private StringProperty name;
   
    

    //Constructor for new Raw Data file
    public RawDataFile(File file) {
        this.file=file;
        this.name = new SimpleStringProperty(file.getName());

    }

    // parse Scans
    public void parseFile() {
        DomParser dpe = new DomParser(file.toString());
        this.listofScans = dpe.ParseFile();
    }

    public void extractSlices(ObservableList<Entry> data, float RTTolerance, float MZTolerance) {
        this.listofSlices = new ArrayList<>();


        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).getListofAdducts().size(); j++) {
                int Num = data.get(i).getListofAdducts().get(j).getNum();
                float MZ = (float) data.get(i).getListofAdducts().get(j).getMZ();
                float RT = (float) data.get(i).getListofAdducts().get(j).getRT();   //RT in Minutes
                Slice newSlice = new Slice(file.toString(),Num, MZ, MZTolerance, RT, RTTolerance); 
                newSlice.extractSlice(listofScans);
                data.get(i).getListofAdducts().get(j).addSlice(newSlice);
                listofSlices.add(newSlice);
                
            }
     
        }
       
        
this.listofScans=null; //get rid of Scans
    }

    /**
     * @return the name
     */
    public String getName() {
         System.out.println("tried to get name");
        return name.get();
       
    }

    /**
     * @param name the name to set
     */
    public void setName(StringProperty name) {
        this.name = name;
    }

    
    
}
