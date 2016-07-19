/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import com.mycompany.fxmltableview.logic.B64Coder;
import com.mycompany.fxmltableview.logic.DomParser;
import com.mycompany.fxmltableview.logic.Session;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

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
    private int pointer;
    private Dataset dataset;
    private List<Scan> listofScans;
    private float[] RTArray;
    private Slice[] listofSlices;
    private StringProperty name;
    private Session session;
    private float scanspersecond;
    private float factor;
    private float NoiseFactor;
    
    private Property<Boolean> active;
    private final Property<Color> color;
    private FloatProperty Width;
   
    //for M/Z cleaning
    private FloatProperty mzshift;
    
    private FloatProperty pfound;
    
    private int maxPeakLengthInt;
    private int peakRTTolerance;
    
    private int column;
    
    private PolynomialSplineFunction rtshiftfunction;

    //Constructor for new Raw Data file
    public RawDataFile(Dataset dataset, File file, Session session) {
        this.file=file;
        this.dataset=dataset;
        this.name = new SimpleStringProperty(file.getName());
        this.color= new SimpleObjectProperty(dataset.getColor());
        this.Width = new SimpleFloatProperty(dataset.getWidth());
        this.session = session;
       
        mzshift = new SimpleFloatProperty();
        pfound = new SimpleFloatProperty(Float.NaN);
        active = new SimpleBooleanProperty(true);
        pointer = 0;
        
        
        color.addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
              for (RawDataFile file: session.getSelectedFiles()) {
                  if (file.getDataset().equals(dataset)) {
                  file.setColor(newValue);}
              }
            }

        }); 
        
        active.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
              for (RawDataFile file: session.getSelectedFiles()) {
                  if (file.getDataset().equals(dataset)) {
                  file.setActive(newValue);
                  }
              }
              
              
              boolean active = false;
              for (RawDataFile file: dataset.getListofFiles()) {
                  if (file.getActive().booleanValue()) {
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
        int[] mzbins = new int[(int)session.getMZTolerance()*100+1];
        float[] ppmvalues = new float[mzbins.length];
        float valuestep = session.getMZTolerance()*2/mzbins.length;
        for (int i = 0; i< mzbins.length; i++) {
            ppmvalues[i]=session.getMZTolerance()*(-1)+i*valuestep;
        }
        float ppm;
        for (int i = 0; i< listofSlices.length; i++) {
            if (listofSlices[i]!=null){
                double count = 0;
                double sum = 0;
                for (int j = 0; j<listofSlices[i].getMZArray().length; j++) {
                    if (listofSlices[i].getMZArray()[j]!=0) {
                        sum+=listofSlices[i].getMZArray()[j]*listofSlices[i].getIntArray()[j];
                        count+=listofSlices[i].getIntArray()[j];
                    }}
                    double weightedsum = sum/count;
//                    weightedsum-=listofSlices[i].getAdduct().getMZ();
//                    weightedsum/=listofSlices[i].getAdduct().getMZ()/1000000;
                    weightedsum = (weightedsum-listofSlices[i].getAdduct().getMZ())/(weightedsum/1000000);
                    int bin = Arrays.binarySearch(ppmvalues, (float)weightedsum)-1;
       //System.out.println(bin);
       if (bin<0) {
            bin =((bin + 3) * (-1));
       }
      if (bin>=0){
       mzbins[bin]++;
       //System.out.println(bin);
        }}
        }
        int max = 0;
        int maxbin = 0;
        for (int i  = 0; i<mzbins.length; i++) {
            if (mzbins[i]>max) {
                max = mzbins[i];
                maxbin = i;
            }
            
        }
        //don't just take the max bin
        double accuratebin = 0;
        int sum = 0;
        for (int i  = maxbin-4; i<maxbin+5; i++) {
            if (i>0&&i<mzbins.length) {
            accuratebin+=(i+1)*mzbins[i];
            sum+=mzbins[i];
            }
        }
        accuratebin=accuratebin/sum-1;
        ppm = (float) (session.getMZTolerance()*(-1)+accuratebin*valuestep);
        System.out.println("PPM: " + ppm);
        System.out.println("                               Timeppm:  " + (System.currentTimeMillis()-start2));
        
        mzshift.set(ppm);
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
    int count = 0;
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
    listofSlices=newlist;
 System.out.println("                               Timenarrow:  " + (System.currentTimeMillis()-start3));
 NoiseFactor = session.getSncalculator().calculateNoiseFactor(listofSlices);       
        
this.listofScans=null; //get rid of Scans, they are not needed any more
double end = System.currentTimeMillis();
System.out.println("Complete Extraction: " + (end-start));

initializeFile(bytecount);
session.getIothread().writefile(this);


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
    public Slice[] getListofSlices() {
        return listofSlices;
    }

    /**
     * @param listofSlices the listofSlices to set
     */
    public void setListofSlices(Slice[] listofSlices) {
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
            list.get(i).getOgroupShift().remove(this);
            for (int j =0; j<list.get(i).getListofAdducts().size(); j++) {
                list.get(i).getListofAdducts().get(j).getListofSlices().remove(this);
                list.get(i).getListofAdducts().get(j).getScores().remove(this);
                list.get(i).getListofAdducts().get(j).getCertainties().remove(this);
                list.get(i).getListofAdducts().get(j).getPenArray().remove(this);
                //list.get(i).getListofAdducts().get(j).getAdductPropArray().remove(this);
                
            }
        }
        
        for ( int i =0; i<listofSlices.length; i++) {
            listofSlices[i].deleteSlice();
            
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
    pfound=new SimpleFloatProperty((float)found/(float)session.getListofOGroups().size()*100);
  
    getDataset().getController().getBatchFileView().refresh();
        
        
    }
    
    public void calculateScorenew() {
        
        
        int found = 0;
        for (int i = 0; i< session.getListofOGroups().size(); i++) {
            found += session.getListofOGroups().get(i).getPeakfound(this);
        }
        float sum = 0.0f;
    
    pfound=new SimpleFloatProperty((float)found/(float)session.getListofOGroups().size()*100);
  
    getDataset().getController().getBatchFileView().refresh();
        
        
    }
    
    public void initializeFile(int bytecount) throws FileNotFoundException, IOException, InterruptedException {
        RandomAccessFile memoryMappedFile = new RandomAccessFile("tmp\\" + this.toString() + ".out", "rw");
        MMFile = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, bytecount);
        
    }
    
    public void readData(Slice slice) throws InterruptedException {
        MMFile.position(slice.getPosition());
        float[] intensity = new float[slice.getSize()];
        for (int i = 0; i <slice.getSize(); i++) {
            intensity[i]=MMFile.getFloat();
        }
        slice.setIntArray(intensity);
        
        float[] MZ = new float[slice.getSize()];
        for (int i = 0; i<slice.getSize(); i++) {
            MZ[i]=MMFile.getFloat();
        }
        slice.setMZArray(MZ);
        slice.setStored(false);
        if (slice.getMZArray()==null) {
            System.out.println("Error while reading");
        }
        
    }
    
    public void writeData(Slice slice) throws InterruptedException {
        //System.out.println("number: " + number);
        //int pos = 500*number;
        //System.out.println("Pos: " + pos);
        
        
        float[] intensity = slice.getIntArray();
        try {MMFile.position(pointer);
        slice.setPosition(pointer);
        for (int i = 0; i <intensity.length; i++) {
            MMFile.putFloat(intensity[i]);
           
        }
        float[] MZ = slice.getMZArray();
        for (int i = 0; i<MZ.length; i++) {
            MMFile.putFloat(MZ[i]); 
        }

        slice.setStored(true);
        slice.setWritten(true);
        pointer = MMFile.position();
        }
        catch (NullPointerException e) {
            System.out.println("Error during File writing");
            System.out.println("MMFile: " + MMFile);
            writeData(slice);
        } 
        
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

    /**
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * @param column the column to set
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * @return the NoiseFactor
     */
    public float getNoiseFactor() {
        return NoiseFactor;
    }

    /**
     * @param NoiseFactor the NoiseFactor to set
     */
    public void setNoiseFactor(float NoiseFactor) {
        this.NoiseFactor = NoiseFactor;
    }
    
    public String getPfoundString() {
        if (!pfound.getValue().isNaN()) {
            return pfound.getValue().toString();
        }
        return "";
    }

    /**
     * @return the rtshiftfunction
     */
    public PolynomialSplineFunction getRtshiftfunction() {
        return rtshiftfunction;
    }

    /**
     * @param rtshiftfunction the rtshiftfunction to set
     */
    public void setRtshiftfunction(ObservableList<Entry> list, float[] centroids) {
        //uncomment to write changed file
        
//        LinearInterpolator inter = new LinearInterpolator();
//        ArrayList<Double> RTlist = new ArrayList<>();
//        ArrayList<Double> centlist = new ArrayList<>();
//        RTlist.add(0d);
//        centlist.add(0d);
//        float step = session.getRTTolerance()*2*60/session.getResolution();
//        float middle = session.getResolution()/2-1;
//        int i = 1;
//       
//        for (Entry o:list) {
//            if (RTlist.get(RTlist.size()-1)<o.getRT()) {
//            RTlist.add((double)o.getRT());
//            centlist.add((double)(centroids[i-1]-middle)*step);
//            }
//            i++;
//        }
//        
//        RTlist.add(Double.MAX_VALUE);
//        centlist.add(0d);
//       
//        double[] RT = new double[RTlist.size()];
//        double[] cent = new double[RTlist.size()];
//        
//        i = 0;
//        for(Double d:RTlist) {
//            RT[i]=d*60;
//            i++;
//        }
//        i=0;
//        for (Double d:centlist) {
//            cent[i]=d;
//            i++;
//        }
//        
//        try {
//        this.rtshiftfunction = inter.interpolate(RT, cent);
//
//        
//        
//        String content = readFile(file, StandardCharsets.UTF_8);
//        StringBuilder file = new StringBuilder(content);
//        
//        
//        //look for RT
//        String RTstring = "retentionTime=\"PT";
//        String Peakstartstring = "<peaks";
//        String Peakendstring = "</peaks>";
//        String RTend = "S\"";
//        B64Coder decoder = new B64Coder();
//        float mzshift = this.mzshift.get();
//        int start = file.indexOf(RTstring)+17;
//        int end = file.indexOf(RTend,start);
//        int peakstart = file.indexOf(Peakstartstring);
//        peakstart = file.indexOf(">", peakstart)+1;
//        int peakend = file.indexOf(Peakendstring,peakstart);
//        i = 0;
//while (start >= 17) {
//    String retentiontime = (file.substring(start,end));
//    String codedpeak = file.substring(peakstart,peakend);
//    float[] decoded = decoder.extractArray(codedpeak);
//    
//    //change m/z values
//    for (int m = 0; m<decoded.length; m=m+2) {
//        decoded[m] = decoded[m]-decoded[m]/1000000*mzshift;
//    }
//    
//    String newcodedpeak = decoder.encodeArray(decoded);
//    for (int s = 0; s <newcodedpeak.length(); s++) {
//        file.setCharAt(peakstart+s, newcodedpeak.charAt(s));
//    }
//    
//    peakstart = file.indexOf(Peakstartstring,peakend);
//    peakstart = file.indexOf(">", peakstart)+1;
//    peakend = file.indexOf(Peakendstring,peakstart);
//    
//    //rt1 = old RT value
//    double rt1 = Double.parseDouble(retentiontime);
//    
//    //rt2 = new RT value
//    double rt2 = rt1 - rtshiftfunction.value(rt1);
//
//    
//    String newrettime = String.valueOf(rt2)+"00000000";
//    for (int s = 0; s <retentiontime.length(); s++) {
//        file.setCharAt(start+s, newrettime.charAt(s));
//    }
//    start = content.indexOf(RTstring, start + 10)+17;
//    end = content.indexOf(RTend, start);
//
//}
//String newfile = file.toString();
//
//
////print new file
//PrintWriter out = new PrintWriter("changed_" + this.file.getName());
//out.println(newfile);
//out.close();
//        
//        }
//        catch (Exception e ) {
//            System.out.println(e.getMessage());
//        }
    }
    
    static String readFile(File file, Charset encoding) 
  throws IOException 
{
  byte[] encoded = Files.readAllBytes(file.toPath());
  return new String(encoded, encoding);
}
}
