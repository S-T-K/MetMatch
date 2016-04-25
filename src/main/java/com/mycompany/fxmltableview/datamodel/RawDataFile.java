/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import com.mycompany.fxmltableview.logic.DomParser;
import com.mycompany.fxmltableview.logic.Session;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private Dataset dataset;
    private List<Scan> listofScans;
    private List<Slice> listofSlices;
    private StringProperty name;
    private Session session;
    private double scanspersecond;
    
    private Property<Boolean> active;
    private final Property<Color> color;
    private DoubleProperty Width;
   
    //for M/Z cleaning
    private int[] mzbins;
    private DoubleProperty mzshift;
    
    private DoubleProperty pfound;
    private DoubleProperty avgcertainty;

    //Constructor for new Raw Data file
    public RawDataFile(Dataset dataset, File file, Session session) {
        this.file=file;
        this.dataset=dataset;
        this.name = new SimpleStringProperty(file.getName());
        this.color= new SimpleObjectProperty(dataset.getColor());
        this.Width = new SimpleDoubleProperty(dataset.getWidth());
        this.session = session;
        mzbins = new int[session.getResolution()];
        mzshift = new SimpleDoubleProperty();
        pfound = new SimpleDoubleProperty();
        avgcertainty = new SimpleDoubleProperty();
        active = new SimpleBooleanProperty(true);
        
        color.addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
              for (int i = 0; i< session.getSelectedFiles().size(); i++) {
                  session.getSelectedFiles().get(i).setColor(newValue);
              }
            }

        }); 
        
        active.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
              for (int i = 0; i< session.getSelectedFiles().size(); i++) {
                  session.getSelectedFiles().get(i).setActive(newValue);
              }
              
              
              boolean active = false;
              for (int i = 0; i< dataset.getListofFiles().size(); i++) {
                  if (dataset.getListofFiles().get(i).getActive().booleanValue()) {
                      active = true;
                  }
              }
              dataset.getController().getBatact().setSelected(active);
            }
            
            

        }); 
        
//        Width.addListener(new ChangeListener<DoubleProperty>() {
//            @Override
//            public void changed(ObservableValue<? extends DoubleProperty> observable, Double oldValue, Double newValue) {
//              for (int i = 0; i< session.getSelectedFiles().size(); i++) {
//                  session.getSelectedFiles().get(i).setWidth(new SimpleDoubleProperty(newValue));
//              }
//            }
//
//        }); 
    }

    // parse Scans
    public void parseFile() {
        DomParser dpe = new DomParser(file.toString());
        this.listofScans = dpe.ParseFile();
        //calculate scans/second for area calculation
        scanspersecond = 1.0/(listofScans.get(1).getRetentionTime()-listofScans.get(0).getRetentionTime());
        
        
        dpe=null;
    }

    //extract Slices, according to tolerances
    public void extractSlices(boolean isreference, List<Entry> data, float RTTolerance, float MZTolerance) {
        double start = System.currentTimeMillis();
        this.setListofSlices(new ArrayList<>());


        for (int i = 0; i < data.size(); i++) {
            //System.out.println("started with OGroup " + i);
            for (int j = 0; j < data.get(i).getListofAdducts().size(); j++) {
                //System.out.println("started with Adduct " + j);
                int Num = data.get(i).getListofAdducts().get(j).getNum();
                float MZ = (float) data.get(i).getListofAdducts().get(j).getMZ();
                float RT = (float) data.get(i).getListofAdducts().get(j).getOGroupRT();   //RT in Minutes
                Slice newSlice = new Slice(this, data.get(i).getListofAdducts().get(j)); 
                newSlice.binaryExtractSlicefromScans(listofScans);
                data.get(i).getListofAdducts().get(j).addSlice(newSlice);
                getListofSlices().add(newSlice);
                //System.out.println("finished with Adduct " + j);
                
            }
           
            
        }
 //get max bin
 int maxint = 0;
 int max = 0;
 for (int i =0; i<mzbins.length; i++) {
     if (mzbins[i]>max){
         max = mzbins[i];
         maxint = i;   
     }
 }
 
 //calculate "median" shift
double step = session.getMZTolerance()/(mzbins.length)*2;
mzshift = new SimpleDoubleProperty(session.getMZTolerance()-maxint*step);

//clean slices according to shift and tolerance
for (int i =0; i< listofSlices.size(); i++) {
    listofSlices.get(i).clean();
    listofSlices.get(i).generateInterpolatedEIC();
}
        
        
this.listofScans=null; //get rid of Scans, they are not needed any more
double end = System.currentTimeMillis();
System.out.println("Complete Extraction: " + (end-start));
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
     * @return the width
     */
    public double getWidth() {
        return Width.get();
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.Width.set(width);
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

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
    
    public void addtoBin(int bin){
        mzbins[bin]++;
        
    }

    /**
     * @return the mzbins
     */
    public int[] getMzbins() {
        return mzbins;
    }

    /**
     * @param mzbins the mzbins to set
     */
    public void setMzbins(int[] mzbins) {
        this.mzbins = mzbins;
    }

    /**
     * @return the mzshift
     */
    public double getMzshift() {
        return mzshift.get();
    }

    /**
     * @param mzshift the mzshift to set
     */
    public void setMzshift(DoubleProperty mzshift) {
        this.mzshift = mzshift;
    }
    
    /**
     * @return the mzshift
     */
    public double getPfound() {
        return pfound.get();
    }

    /**
     * @param mzshift the mzshift to set
     */
    public void setPfound(DoubleProperty pfound) {
        this.pfound = pfound;
    }
    
    /**
     * @return the mzshift
     */
    public double getAvgcertainty() {
        return avgcertainty.get();
    }

    /**
     * @param mzshift the mzshift to set
     */
    public void setAvgCertainty(DoubleProperty avg) {
        this.avgcertainty = avg;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }
    
    public void deleteFile() {
        
        System.out.println(this.getActive().booleanValue());
        dataset.getListofFiles().remove(this);
        List<Entry> list = session.getListofOGroups();
        for (int i = 0; i<list.size(); i++) {
            list.get(i).getScores().remove(this);
            //list.get(i).getOGroupPropArray().remove(this);
            list.get(i).getOGroupFittedShift().remove(this);
            for (int j =0; j<list.get(i).getListofAdducts().size(); j++) {
                list.get(i).getListofAdducts().get(j).getListofSlices().remove(this);
                list.get(i).getListofAdducts().get(j).getScores().remove(this);
                //list.get(i).getListofAdducts().get(j).getAdductPropArray().remove(this);
                
            }
        }
        
        for ( int i =0; i<listofSlices.size(); i++) {
            listofSlices.get(i).deleteSlice();
            
        }
       System.out.println("Deleted File");
        System.gc();
    }

    public boolean isselected() {
       
        if (dataset.getController().getBatchFileView().getSelectionModel().getSelectedItems().contains(this)) {
            return true;
        }
        return false;
    }
    
    public Property getColorProperty() {
        return color;
    }
    
    public DoubleProperty getWidthProperty() {
        return Width;
    }

    /**
     * @return the scanspersecond
     */
    public double getScanspersecond() {
        return scanspersecond;
    }

    /**
     * @param scanspersecond the scanspersecond to set
     */
    public void setScanspersecond(double scanspersecond) {
        this.scanspersecond = scanspersecond;
    }
    
    public void calculateScore() {
        
        List<Double> certainties = new ArrayList<>();
        int found = 0;
        for (int i = 0; i< session.getListofOGroups().size(); i++) {
            certainties.add(session.getListofOGroups().get(i).getCertainties().get(this));
            found += session.getListofOGroups().get(i).getPeakfound(this);
        }
        double sum = 0.0;
    for (double cert : certainties) {
        sum += cert;
    }
    avgcertainty=new SimpleDoubleProperty(sum/certainties.size());
    pfound=new SimpleDoubleProperty((double)found/(double)session.getListofOGroups().size()*100);
  
    getDataset().getController().getBatchFileView().refresh();
        
        
    }
}
