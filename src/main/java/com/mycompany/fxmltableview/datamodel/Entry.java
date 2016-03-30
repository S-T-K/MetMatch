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
    

    
    
    //for peak probability
    private HashMap<Dataset, double[]> AdductPropArray;
    private HashMap<Dataset, double[]> OGroupPropArray;
    private HashMap<Dataset, Integer> fittedShift;
    
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
        this.listofSlices = new HashMap<RawDataFile, Slice>();
        this.AdductPropArray = new HashMap<Dataset, double[]>();
        this.session=session;
        this.OGroupObject=ogroup;
        this.maxIntensity = 0;
        
    }
    
   
    //constructor for OGroup/Metabolite
    public Entry(int OGroup, Session session) {
        this.listofAdducts= new ArrayList<>();
        this.RT = new SimpleDoubleProperty(0);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Score = new SimpleDoubleProperty(0);
        this.session = session;
        this.OGroupObject=null;
        this.OGroupPropArray = new HashMap<Dataset, double[]>();
        fittedShift = new HashMap<>();

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
        double startRT = this.getMinRT()+0.05;
        double endRT = this.getMaxRT()-0.05;
        setRTArray(new double[resolution]);
        
     
      
      //fill Arrays
      for (int i = 0; i< resolution; i++) {
            getRTArray()[i] = startRT+(((endRT-startRT))/(resolution-1))*i;
      }
        
        
    }
    
    //generates PropArray of a dataset for an Adduct 
    public void generateAdductPropArray(Dataset dataset) {
        double [] propArray = new double[getSession().getResolution()];

        
        for (int i = 0; i< dataset.getListofFiles().size(); i++) {
        Slice currentSlice = listofSlices.get(dataset.getListofFiles().get(i));
            currentSlice.generateWaveletProp();
            for (int j = 0; j < getSession().getResolution(); j++) {
                if (currentSlice.getPropArray()[j]+propArray[j]>1){
                    if (currentSlice.getPropArray()[j]>propArray[j]) {
                        propArray[j]=propArray[j]*0.1+currentSlice.getPropArray()[j];
                    } else {
                        propArray[j]=propArray[j]+currentSlice.getPropArray()[j]*0.1;
                    }
                } else {
                propArray[j] += currentSlice.getPropArray()[j];}
            }
        }
        
        AdductPropArray.put(dataset, propArray);
    }
    
    //generates average PropArray over all Adducts for a dataset
    //TODO: Avg?
    public void generateOGroupPropArray(Dataset dataset) {
        
        double [] propArray = new double[getSession().getResolution()];
        for (int i = 0; i<listofAdducts.size(); i++) {
            listofAdducts.get(i).generateAdductPropArray(dataset);
            for (int j = 0; j<session.getResolution(); j++) {
                if(listofAdducts.get(i).getAdductPropArray(dataset)[j]+propArray[j]>1){
                    if (listofAdducts.get(i).getAdductPropArray(dataset)[j]>propArray[j]) {
                        propArray[j]=propArray[j]*0.1+listofAdducts.get(i).getAdductPropArray(dataset)[j];
                    } else {
                        propArray[j]=propArray[j]+listofAdducts.get(i).getAdductPropArray(dataset)[j]*0.1;
                    }
                } else {
                propArray[j]+=listofAdducts.get(i).getAdductPropArray(dataset)[j];}
            
            
        }
        
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
        OGroupPropArray.put(dataset, propArray);
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

    /**
     * @param Score the Score to set
     */
    public void setScore(SimpleDoubleProperty Score) {
        this.Score = Score;
    }
        /**
     * @return the Num
     */
    public int getNum() {
        return Num.get();
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
     * @return the fittedShift
     */
    public int getFittedShift(Dataset dataset) {
        if (fittedShift.containsKey(dataset)){
        return fittedShift.get(dataset);}
        else {
            return 0;
        }
    }

    /**
     * @param fittedShift the fittedShift to set
     */
    public void setFittedShift(Dataset dataset, int shift) {
        this.fittedShift.put(dataset, shift);
    }

    /**
     * @return the AdductPropArray
     */
    public double[] getAdductPropArray(Dataset dataset) {
        return AdductPropArray.get(dataset);
    }

    /**
     * @param AdductPropArray the AdductPropArray to set
     */
    public void setAdductPropArray(HashMap<Dataset, double[]> AdductPropArray) {
        this.AdductPropArray = AdductPropArray;
    }

    /**
     * @return the OGroupPropArray
     */
    public double[] getOGroupPropArray(Dataset dataset) {
        return OGroupPropArray.get(dataset);
    }

    /**
     * @param OGroupPropArray the OGroupPropArray to set
     */
    public void setOGroupPropArray(HashMap<Dataset, double[]> OGroupPropArray) {
        this.OGroupPropArray = OGroupPropArray;
    }
      
    
    //Comparator to sort List of Entries
    public static class orderbyRT implements Comparator<Entry> {

        @Override
        public int compare(Entry o1, Entry o2) {
            return Double.valueOf(o1.getRT()).compareTo(Double.valueOf(o2.getRT()));
        }
    }


    
    
}
