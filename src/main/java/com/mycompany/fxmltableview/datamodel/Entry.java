/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;



import com.mycompany.fxmltableview.logic.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.XYChart;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author stefankoch
 * Represents either a Metabolite or an adduct/fragment
 * Metabolites have adducts/fragments on their own
 * 
 */
public class Entry {
    
    private SimpleDoubleProperty RT;
    private SimpleDoubleProperty Score;
    private SimpleDoubleProperty Scorepeakfound;
    private SimpleDoubleProperty Scorepeakclose;
    private IntegerProperty Num;
    private DoubleProperty MZ;
    private IntegerProperty Xn;
    private IntegerProperty OGroup;
    private StringProperty Ion;
    private DoubleProperty M;
    private Entry OGroupObject;
    private List<Entry> listofAdducts;
    private HashMap<RawDataFile, Slice> listofSlices;   //stores all slices
    private Session session;
    private HashMap<RawDataFile, Double> Scores;
    

    
    
    //for peak probability
    private HashMap<RawDataFile, Integer> OGroupfittedShift;
    private HashMap<RawDataFile, Integer> AdductfittedShift;
    
    //for penalties and bonuses in certain regions
    private double[] PenArray;
    
    //maxIntensity of all Slices
    private float maxIntensity;

    //Interpolated Arrays
    private double[] RTArray;
    private double[] IntensityArray;
    
    public Entry() {
    }

    //constructor for Adduct
    public Entry(int Num, double MZ, double RT, int Xn, int OGroup, String Ion, double M, Session session, Entry ogroup) {
        this.Num = new SimpleIntegerProperty(Num);
        this.MZ = new SimpleDoubleProperty(MZ);
        this.RT = new SimpleDoubleProperty(RT);
        this.Xn = new SimpleIntegerProperty(Xn);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Ion = new SimpleStringProperty(Ion);
        this.M = new SimpleDoubleProperty(M);
        this.Score = new SimpleDoubleProperty(0);
        this.Scorepeakclose = new SimpleDoubleProperty(0);
        this.Scorepeakfound = new SimpleDoubleProperty(0);
        this.listofSlices = new HashMap<RawDataFile, Slice>();

        this.Scores = new HashMap<RawDataFile, Double>();
        this.session=session;
        this.OGroupObject=ogroup;
        this.maxIntensity = 0;
        AdductfittedShift = new HashMap<>();
    }
    
   
    //constructor for OGroup/Metabolite
    public Entry(int OGroup, Session session) {
        this.listofAdducts= new ArrayList<>();
        this.RT = new SimpleDoubleProperty(0);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Score = new SimpleDoubleProperty(0);
        this.Scorepeakclose = new SimpleDoubleProperty(0);
        this.Scorepeakfound = new SimpleDoubleProperty(0);
        this.session = session;
        this.OGroupObject=null;

        OGroupfittedShift = new HashMap<>();
        this.Scores = new HashMap<RawDataFile, Double>();
        
        PenArray = new double[session.getResolution()];

    }
    
    //add Slice to Adduct
    public void addSlice(Slice slice) {
        listofSlices.put(slice.getFile(), slice);
        if (slice.getMaxIntensity()>maxIntensity) {
            maxIntensity = slice.getMaxIntensity();
        }
        
    }
    
    //add adduct to an OGroup
    public void addAdduct(Entry adduct) {
        this.getListofAdducts().add(adduct);
        this.setRT(new SimpleDoubleProperty(((this.getRT() * (getListofAdducts().size() - 1)) + adduct.getRT()) / getListofAdducts().size()));
    }
    
    public void generateRTArray() {
        
        int resolution = getSession().getResolution();
        double startRT = this.getMinRT();
        double endRT = this.getMaxRT();
        setRTArray(new double[resolution]);
        
     
      
      //fill Arrays
      for (int i = 0; i< resolution; i++) {
            getRTArray()[i] = startRT+(((endRT-startRT))/(resolution-1))*i;
      }
        
        
    }
    
    //generates PropArray of a dataset for an Adduct 
    public void peakpickAdduct(RawDataFile file) {
//        double [] propArray = new double[getSession().getResolution()];
//
        Slice currentSlice = listofSlices.get(file);
//        
        if (session.getPeackPick().equals("Naïve")) {
            currentSlice.NaivePeakPicking();
            
        } else if (session.getPeackPick().equals("MassSpecWavelet")) {
            currentSlice.WaveletPeakPicking();
            
        } else {
            System.out.println("Error");
        }
//           
//        double [] sliceArray = currentSlice.getPropArray();
//                
//            for (int j = 0; j < getSession().getResolution(); j++) {
//                if (sliceArray[j]+propArray[j]>1){
//                    if (sliceArray[j]>propArray[j]) {
//                        propArray[j]=propArray[j]*0.1+sliceArray[j];
//                    } else {
//                        propArray[j]=propArray[j]+sliceArray[j]*0.1;
//                    }
//                } else {
//                propArray[j] += sliceArray[j];}
//            }
//            getAdductPropArray().put(file, propArray);
//        
//        
//        
    }
    
    //generates average PropArray over all Adducts for a dataset
    //TODO: Avg?
    public void peakpickOGroup(RawDataFile file) {
        
        //double [] propArray = new double[getSession().getResolution()];
        if (session.isPeakPickchanged()) {
        for (int i = 0; i<listofAdducts.size(); i++) {
            listofAdducts.get(i).peakpickAdduct(file);
//            for (int j = 0; j<session.getResolution(); j++) {
//                if(listofAdducts.get(i).getAdductPropArray(file)[j]+propArray[j]>1){
//                    if (listofAdducts.get(i).getAdductPropArray(file)[j]>propArray[j]) {
//                        propArray[j]=propArray[j]*0.1+listofAdducts.get(i).getAdductPropArray(file)[j];
//                    } else {
//                        propArray[j]=propArray[j]+listofAdducts.get(i).getAdductPropArray(file)[j]*0.1;
//                    }
//                } else {
//                propArray[j]+=listofAdducts.get(i).getAdductPropArray(file)[j];}
//            
//            
//        }
        
        //normalize
//        List asList = Arrays.asList(ArrayUtils.toObject(PropArray));
//        double max = (double) Collections.max(asList);
//        
//        for (int i = 0; i<PropArray.length; i++) {
//            PropArray[i] = PropArray[i]/max;
//            
//        }
        
        //log normalize
//        for (int i = 0; i<PropArray.length; i++) {
//            if (PropArray[i]>0) {
//            PropArray[i] = Math.log10(PropArray[i]);}
//            
//        }
        
        }
        //getOGroupPropArray().put(file, propArray);
        }
    }

    /**
     * @return the listofAdducts
     */
    public List<Entry> getListofAdducts() {
        return listofAdducts;
    }

    
    /**
     * @return the RT
     */
    public double getRT() {
        return RT.get();
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(SimpleDoubleProperty RT) {
        this.RT = RT;
    }

    /**
     * @return the Score
     */
    public double getScore() {
        return Score.get();
    }

    public SimpleDoubleProperty ScoreProperty() {
        return Score;
    }
    /**
     * @param Score the Score to set
     */
    public void setScore(SimpleDoubleProperty score) {
        this.Score = score;
    }
    /**
     * @return the Score
     */
    public double getScorepeakfound() {
        return Scorepeakfound.get();
    }

    public SimpleDoubleProperty ScorepeakfoundProperty() {
        return Scorepeakfound;
    }
    /**
     * @param Score the Score to set
     */
    public void setScorepeakfound(SimpleDoubleProperty score) {
        this.Scorepeakfound = score;
    }
    /**
     * @return the Score
     */
    public double getScorepeakclose() {
        return Scorepeakclose.get();
    }

    public SimpleDoubleProperty ScorepeakcloseProperty() {
        return Scorepeakclose;
    }
    /**
     * @param Score the Score to set
     */
    public void setScorepeakclose(SimpleDoubleProperty score) {
        this.Scorepeakclose = score;
    }
        /**
     * @return the Num
     */
    public int getNum() {
        if (Num == null) {
            return 0;
        } else {
        return Num.get();}
    }

    /**
     * @param Num the Num to set
     */
    public void setNum(SimpleIntegerProperty Num) {
        this.Num = Num;
    }

    /**
     * @return the MZ
     */
    public double getMZ() {
        if (this.MZ==null) {
            return 0;
        }
        return MZ.get();
    }

    /**
     * @param MZ the MZ to set
     */
    public void setMZ(SimpleDoubleProperty MZ) {
        this.MZ = MZ;
    }


    /**
     * @return the Xn
     */
    public int getXn() {
        return Xn.get();
    }

    /**
     * @param Xn the Xn to set
     */
    public void setXn(SimpleIntegerProperty Xn) {
        this.Xn = Xn;
    }

    /**
     * @return the OGroup
     */
    public int getOGroup() {
        return OGroup.get();
    }

    /**
     * @param OGroup the OGroup to set
     */
    public void setOGroup(SimpleIntegerProperty OGroup) {
        this.OGroup = OGroup;
    }

    /**
     * @return the Ion
     */
    public String getIon() {
        return Ion.get();
    }

    /**
     * @param Ion the Ion to set
     */
    public void setIon(SimpleStringProperty Ion) {
        this.Ion = Ion;
    }

    /**
     * @return the M
     */
    public double getM() {
        return M.get();
    }

    /**
     * @param M the M to set
     */
    public void setM(SimpleDoubleProperty M) {
        this.M = M;
    }

    /**
     * @return the OGroupObject
     */
    public Entry getOGroupObject() {
        return OGroupObject;
    }

    /**
     * @param OGroupObject the OGroupObject to set
     */
    public void setOGroupObject(Entry OGroupObject) {
        this.OGroupObject = OGroupObject;
    }

    /**
     * @return the listofSlices
     */
    public HashMap<RawDataFile, Slice> getListofSlices() {
        return listofSlices;
    }

    
    
    
    //generates an array with gaussian peak probability through correlation with such a peak over all slices
   
    
   

    /**
     * @return the RTArray
     */
    public double[] getRTArray() {
       if(OGroupObject!=null){
            return (OGroupObject.getRTArray());
        } else {
            return (RTArray);
        }
    }

    /**
     * @param RTArray the RTArray to set
     */
    public void setRTArray(double[] RTArray) {
        this.RTArray = RTArray;
    }

    /**
     * @return the IntensityArray
     */
    public double[] getIntensityArray() {
        return IntensityArray;
    }

    /**
     * @param IntensityArray the IntensityArray to set
     */
    public void setIntensityArray(double[] IntensityArray) {
        this.IntensityArray = IntensityArray;
    }

    
    public double median(double[] m) {
    int middle = m.length/2;
    if (m.length%2 == 1) {
        return m[middle];
    } else {
        return (m[middle-1] + m[middle]) / 2.0;
    }
}
    
    public double summ(double[] m) {
        double sum =0;
        for (int i =0; i<m.length; i++) {
            sum+=m[i];
            
        }
        return sum;
    }
    
    public double getOGroupRT() {
        
        return this.OGroupObject.getRT();
    }

  

   
    

    
    public double getMinRT() {
        if(OGroupObject!=null){
            return (OGroupObject.getRT()-getSession().getRTTolerance());
        } else {
            return (getRT()-getSession().getRTTolerance());
        }
    }
     public double getMaxRT() {
        if(OGroupObject!=null){
            return (OGroupObject.getRT()+getSession().getRTTolerance());
        } else {
            return (getRT()+getSession().getRTTolerance());
        }
    }
     public double getMinMZ() {
        return (getMZ()*(1-(getSession().getMZTolerance()/1000000)));
    }
      public double getMaxMZ() {
        return (getMZ()*(1+(getSession().getMZTolerance()/1000000)));
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

    /**
     * @return the OGroupfittedShift
     */
    public int getOGroupFittedShift(RawDataFile file) {
        if (Entry.this.getOGroupFittedShift().containsKey(file)){
        return Entry.this.getOGroupFittedShift().get(file);}
        else {
            return 0;
        }
    }
    
    /**
     * @return the OGroupfittedShift
     */
    public int getAdductFittedShift(RawDataFile file) {
        if (Entry.this.getAdductfittedShift().containsKey(file)){
        return Entry.this.getAdductfittedShift().get(file);}
        else {
            return 0;
        }
    }

    /**
     * @param fittedShift the OGroupfittedShift to set
     */
    public void setFittedShift(RawDataFile file, int shift) {
        this.getOGroupFittedShift().put(file, shift);
        
        for (int i = 0; i<listofAdducts.size(); i++) {
        listofAdducts.get(i).getAdductfittedShift().put(file,listofAdducts.get(i).getListofSlices().get(file).setFittedPeak(shift));
    }
    }

    /**
     * @return the AdductPropArray
     */
    public double[] getAdductPropArray(RawDataFile file) {
        double[] PropArray = new double[session.getResolution()];
        List<Integer> list = new ArrayList<>();
        list.addAll(listofSlices.get(file).getPeakIndex());
        
        for (int i = 0; i< list.size(); i++) {
            PropArray[list.get(i)] = 1;
        }
        
        return PropArray;
    }

   
    /**
     * @return the OGroupPropArray
     * with only 1 and 0
     */
    public double[] getOGroupPropArray(RawDataFile file) {
        double[] PropArray = new double[session.getResolution()];
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i<listofAdducts.size(); i++) {
        list.addAll(listofAdducts.get(i).listofSlices.get(file).getPeakIndex());
        }
        
        for (int i = 0; i< list.size(); i++) {
            PropArray[list.get(i)] = 1;
        }
        
        return PropArray;
    }
    
    //returns a "smooth" PropArray
    public double[] getOGroupPropArraySmooth(RawDataFile file) {
        double[] PropArray = new double[session.getResolution()];
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i<listofAdducts.size(); i++) {
        list.addAll(listofAdducts.get(i).listofSlices.get(file).getPeakIndex());
        }
        
        for (int i = 0; i< list.size(); i++) {
            PropArray[list.get(i)] = 1;
            //change values within tolerance
            for (int j = 1; j<=session.getIntPeakRTTol(); j++) {
                //calculate the value
                double value = 1*((double)session.getIntPeakRTTol()-j)/(double)session.getIntPeakRTTol();
                
                //check for borders and insert new value of old value is smaller
                if((list.get(i)-j)>0&&PropArray[list.get(i)-j]<value) {
                    PropArray[list.get(i)-j]=value;
                }
                if ((list.get(i)+j)<PropArray.length&&PropArray[list.get(i)+j]<value) {
                    PropArray[list.get(i)+j]=value;
                }
            }
        }
        
        return PropArray;
    }

    

    /**
     * @return the Scores
     */
    public HashMap<RawDataFile, Double> getScores() {
        return Scores;
    }

    /**
     * @param Scores the Scores to set
     */
    public void setScores(HashMap<RawDataFile, Double> Scores) {
        this.Scores = Scores;
    }

   

    /**
     * @return the OGroupfittedShift
     */
    public HashMap<RawDataFile, Integer> getOGroupFittedShift() {
        return OGroupfittedShift;
    }

    /**
     * @param fittedShift the OGroupfittedShift to set
     */
    public void setOGroupFittedShift(HashMap<RawDataFile, Integer> fittedShift) {
        this.OGroupfittedShift = fittedShift;             
    }

    /**
     * @return the AdductfittedShift
     */
    public HashMap<RawDataFile, Integer> getAdductfittedShift() {
        return AdductfittedShift;
    }

    /**
     * @param AdductfittedShift the AdductfittedShift to set
     */
    public void setAdductfittedShift(HashMap<RawDataFile, Integer> AdductfittedShift) {
        this.AdductfittedShift = AdductfittedShift;
    }

    
    
    //Comparator to sort List of Entries
    public static class orderbyRT implements Comparator<Entry> {

        @Override
        public int compare(Entry o1, Entry o2) {
            return Double.valueOf(o1.getRT()).compareTo(Double.valueOf(o2.getRT()));
        }
    }

public void addScore(RawDataFile file, double score) {
    this.getScores().put(file, score);
}

public void setScore(RawDataFile file) {
    if (getScores().containsKey(file)) {
        setScore(new SimpleDoubleProperty(getScores().get(file)));
    }
    
}

public double getScore(RawDataFile file) {
    return getScores().get(file);
}
    
//for Shiftview, returns max over all Adducts in OGroup
public double getmaxScorepeakfound(RawDataFile file) {
    double max = 0;
    for (int i  = 0; i<listofAdducts.size(); i++) {
        if (listofAdducts.get(i).getListofSlices().get(file).getScorepeakfound()>max) {
            max = listofAdducts.get(i).getListofSlices().get(file).getScorepeakfound();
        }
    }
    return max;
}

//for Shiftview, returns min over all Adducts in OGroup
public double getminScorepeakclose(RawDataFile file) {
    double min = 1;
    for (int i  = 0; i<listofAdducts.size(); i++) {
        if (listofAdducts.get(i).getListofSlices().get(file).getScorepeakclose()<min) {
            min = listofAdducts.get(i).getListofSlices().get(file).getScorepeakclose();
        }
    }
    return min;
}

public double getmaxScoredistance(RawDataFile file) {
       double max = 0; 
       for (int i  = 0; i<listofAdducts.size(); i++) {
        if (listofAdducts.get(i).getListofSlices().get(file).getScoredistance()>max) {
            max = listofAdducts.get(i).getListofSlices().get(file).getScoredistance();
        }
    }
    return max;  
}
      
    
}
