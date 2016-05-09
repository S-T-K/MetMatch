/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import com.mycompany.fxmltableview.logic.DomParser;
import com.mycompany.fxmltableview.logic.Session;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
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
    private MappedByteBuffer MMFile;
    private Dataset dataset;
    private List<Scan> listofScans;
    private float[] RTArray;
    private Slice[] listofSlices;
    private StringProperty name;
    private Session session;
    private float scanspersecond;
    private float factor;
    
    private Property<Boolean> active;
    private final Property<Color> color;
    private FloatProperty Width;
   
    //for M/Z cleaning
    private int[] mzbins;
    private FloatProperty mzshift;
    
    private FloatProperty pfound;
    private FloatProperty avgcertainty;
    
    private int maxPeakLengthInt;
    private int peakRTTolerance;

    //Constructor for new Raw Data file
    public RawDataFile(Dataset dataset, File file, Session session) {
        this.file=file;
        this.dataset=dataset;
        this.name = new SimpleStringProperty(file.getName());
        this.color= new SimpleObjectProperty(dataset.getColor());
        this.Width = new SimpleFloatProperty(dataset.getWidth());
        this.session = session;
        mzbins = new int[session.getResolution()];
        mzshift = new SimpleFloatProperty();
        pfound = new SimpleFloatProperty();
        avgcertainty = new SimpleFloatProperty();
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
        
//        Width.addListener(new ChangeListener<FloatProperty>() {
//            @Override
//            public void changed(ObservableValue<? extends FloatProperty> observable, Float oldValue, Float newValue) {
//              for (int i = 0; i< session.getSelectedFiles().size(); i++) {
//                  session.getSelectedFiles().get(i).setWidth(new SimpleFloatProperty(newValue));
//              }
//            }
//
//        }); 
    }

    // parse Scans
    public void parseFile() {
        DomParser dpe = new DomParser(file.toString());
        this.listofScans = dpe.ParseFile();
        setRTArray(new float[listofScans.size()]);
     for (int i = 0; i<listofScans.size(); i++) {
            getRTArray()[i] = listofScans.get(i).getRetentionTime();
     }
//        int RT = 0;
//        int points = 0;
//        for (int i = 0; i< listofScans.size(); i++) {
//        RT++;
//        points+=listofScans.get(i).getMassovercharge().length;
//            
//    }
        
//        System.out.println("Number of scans: " + RT);
//        System.out.println("Number of points: " + points);
        
        //calculate scans/second for area calculation
        scanspersecond = (listofScans.get(listofScans.size()-1).getRetentionTime()-listofScans.get(0).getRetentionTime())/listofScans.size()*60;
        
        
        dpe=null;
        
        maxPeakLengthInt = (int) (session.getMaxPeakLength().floatValue()*60/scanspersecond);
        peakRTTolerance = (int) (session.getPeakRTTolerance().floatValue()*60/scanspersecond);
        //factor is the value whith whitch a int from the datapoints has to be multiplied in order to get the corresponding int of the resolution
        setFactor(1.0f/(session.getRTTolerance()*2.0f*60.0f/(float)session.getResolution()*scanspersecond));
    }

    //extract Slices, according to tolerances
    public void extractSlices(boolean isreference, List<Entry> data, float RTTolerance, float MZTolerance) throws InterruptedException, IOException {
        double start = System.currentTimeMillis();
        listofSlices = new Slice[session.getNumberofadducts()];
        int number = 0;
        int slices = 0;
       
        for (int i = 0; i < data.size(); i++) {
            //System.out.println("started with OGroup " + i);
            for (int j = 0; j < data.get(i).getListofAdducts().size(); j++) {
                //System.out.println("started with Adduct " + j);
                Slice newSlice = new Slice(this, data.get(i).getListofAdducts().get(j)); 
                newSlice.newbinaryExtractSlicefromScans(listofScans, RTArray);
                
                
                if(!newSlice.isEmpty()) {
                listofSlices[slices]=newSlice;
                slices++;
                //System.out.println("finished with Adduct " + j);
                } else {
                    number++;
                }
                
            }
           
            
        }
        System.out.println("empty slices: " + number);
        System.out.println("nonempty slices: " + slices);
        System.out.println("                               Time:  " + (System.currentTimeMillis()-start));
 
        double start2 = System.currentTimeMillis();
        //calculate MZshift
        float ppm= 0.0f;
        int count = 0;
        for (int i = 0; i< listofSlices.length; i++) {
            if (listofSlices[i]!=null){
            float[] mzs = new float[listofSlices[i].getMZArray().length];
            System.arraycopy(listofSlices[i].getMZArray(), 0, mzs, 0, listofSlices[i].getMZArray().length);
            Arrays.sort(mzs);
            //calculate ppm for middle value
            if (mzs[mzs.length/2]!=0) {
            ppm += (mzs[mzs.length/2]-listofSlices[i].getAdduct().getMZ())/(listofSlices[i].getAdduct().getMZ()/1000000); 
            count++;
            }
        }
        }
        ppm/=count;
        System.out.println("PPM: " + ppm);
        System.out.println("                               Timeppm:  " + (System.currentTimeMillis()-start2));
        
        double start3 = System.currentTimeMillis();
        int numberofgoodslices = 0;
        //correct MZvalues
        for (int i = 0; i< listofSlices.length; i++) {
            if (listofSlices[i]!=null){
                if (!listofSlices[i].narrowMZ(ppm)) {
                    numberofgoodslices++;
                }
            }
        }
        
        
    Slice[] newlist = new Slice[numberofgoodslices];
    count = 0;
    //get number of bytes stored in all slices
    int bytecount = 0;
    for(int i = 0; i<listofSlices.length; i++) {
        if (listofSlices[i]!=null&&!listofSlices[i].isEmpty()) {
            newlist[count] = listofSlices[i];
            count++;
            bytecount+=listofSlices[i].getMZArray().length;
            
        }
    }
    //multiply to get the corrent number (2 because 2 arrays, 4 because 1 float=4bytes)
    bytecount*=8;
 System.out.println("                               Timenarrow:  " + (System.currentTimeMillis()-start3));
        
        
this.listofScans=null; //get rid of Scans, they are not needed any more
double end = System.currentTimeMillis();
System.out.println("Complete Extraction: " + (end-start));

initializeFile(bytecount);

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
    public float getWidth() {
        return Width.get();
    }

    /**
     * @param width the width to set
     */
    public void setWidth(float width) {
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
    public float getMzshift() {
        return mzshift.get();
    }

    /**
     * @param mzshift the mzshift to set
     */
    public void setMzshift(FloatProperty mzshift) {
        this.mzshift = mzshift;
    }
    
    /**
     * @return the mzshift
     */
    public float getPfound() {
        return pfound.get();
    }

    /**
     * @param mzshift the mzshift to set
     */
    public void setPfound(FloatProperty pfound) {
        this.pfound = pfound;
    }
    
    /**
     * @return the mzshift
     */
    public float getAvgcertainty() {
        return avgcertainty.get();
    }

    /**
     * @param mzshift the mzshift to set
     */
    public void setAvgCertainty(FloatProperty avg) {
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
                list.get(i).getListofAdducts().get(j).getCertainties().remove(this);
                list.get(i).getListofAdducts().get(j).getPenArray().remove(this);
                //list.get(i).getListofAdducts().get(j).getAdductPropArray().remove(this);
                
            }
        }
        
        for ( int i =0; i<listofSlices.size(); i++) {
            listofSlices.get(i).deleteSlice();
            
        }
       System.out.println("Deleted File");
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
    
    public FloatProperty getWidthProperty() {
        return Width;
    }

    /**
     * @return the scanspersecond
     */
    public float getScanspersecond() {
        return scanspersecond;
    }

    /**
     * @param scanspersecond the scanspersecond to set
     */
    public void setScanspersecond(float scanspersecond) {
        this.scanspersecond = scanspersecond;
    }
    
    public void calculateScore() {
        
        List<Float> certainties = new ArrayList<>();
        int found = 0;
        for (int i = 0; i< session.getListofOGroups().size(); i++) {
            certainties.add(session.getListofOGroups().get(i).getCertainties().get(this));
            found += session.getListofOGroups().get(i).getPeakfound(this);
        }
        float sum = 0.0f;
    for (float cert : certainties) {
        sum += cert;
    }
    avgcertainty=new SimpleFloatProperty(sum/certainties.size());
    pfound=new SimpleFloatProperty((float)found/(float)session.getListofOGroups().size()*100);
  
    getDataset().getController().getBatchFileView().refresh();
        
        
    }
    
    public void initializeFile(int bytecount) throws FileNotFoundException, IOException, InterruptedException {
        RandomAccessFile memoryMappedFile = new RandomAccessFile("C:\\Users\\stefankoch\\Documents\\tmp2\\" + this.toString() + ".out", "rw");
        MMFile = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, bytecount);
        
    }
    
    public void readData(Slice slice) throws InterruptedException {
        int number = listofSlices.indexOf(slice);
        int pos = 500*number;
        MMFile.position(pos);
        int[] intensity = new int[session.getResolution()];
        for (int i = 0; i <= 99; i++) {
            intensity[i]=MMFile.getInt();
        }
        slice.setIntensityArray(intensity);
        
        byte[] MZ = new byte[session.getResolution()];
        for (int i = 0; i<100; i++) {
            MZ[i]=MMFile.get();
        }
        slice.setByteMZArray(MZ);
        slice.setStored(false);
        
    }
    
    public void writeData(Slice slice) throws InterruptedException {
        int number = listofSlices.indexOf(slice);
        //System.out.println("number: " + number);
        int pos = 500*number;
        //System.out.println("Pos: " + pos);
        
        int[] intensity = slice.getIntensityArray();
        try {MMFile.position(pos);}
        catch (NullPointerException e) {
            System.out.println("Error during File writing");
            System.out.println("number: " + number);
            System.out.println("Pos: " + pos);
            System.out.println("MMFile: " + MMFile);
            writeData(slice);
        }
        for (int i = 0; i <= 99; i++) {
            MMFile.putInt(intensity[i]);
        }
        byte[] MZ = slice.getByteMZArray();
        for (int i = 0; i<100; i++) {
            MMFile.put(MZ[i]);
        }

        slice.setStored(true);
        slice.setWritten(true);
        slice.setIntensityArray(null);
        slice.setByteMZArray(null);
        
        
    }

    /**
     * @return the RTArray
     */
    public float[] getRTArray() {
        return RTArray;
    }

    /**
     * @param RTArray the RTArray to set
     */
    public void setRTArray(float[] RTArray) {
        this.RTArray = RTArray;
    }

    /**
     * @return the maxPeakLengthInt
     */
    public int getMaxPeakLengthInt() {
        return maxPeakLengthInt;
    }

    /**
     * @param maxPeakLengthInt the maxPeakLengthInt to set
     */
    public void setMaxPeakLengthInt(int maxPeakLengthInt) {
        this.maxPeakLengthInt = maxPeakLengthInt;
    }

    /**
     * @return the peakRTTolerance
     */
    public int getPeakRTTolerance() {
        return peakRTTolerance;
    }

    /**
     * @param peakRTTolerance the peakRTTolerance to set
     */
    public void setPeakRTTolerance(int peakRTTolerance) {
        this.peakRTTolerance = peakRTTolerance;
    }

    /**
     * @return the factor
     */
    public float getFactor() {
        return factor;
    }

    /**
     * @param factor the factor to set
     */
    public void setFactor(float factor) {
        this.factor = factor;
    }
}
