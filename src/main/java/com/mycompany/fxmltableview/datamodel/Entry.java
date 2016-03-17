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
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.XYChart;
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
    private List<Slice> listofSlices;   //stores all slices
    private List<Slice> listofRefSlices;    //stores only reference slices, 
    private Session session;
    

    
    
    //for peak probability
    private double[] PropArray;

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
        this.listofSlices = new ArrayList<Slice>();
        this.listofRefSlices = new ArrayList<Slice>();
        this.session=session;
        this.OGroupObject=ogroup;
        
    }
    
   
    //constructor for OGroup/Metabolite
    public Entry(int OGroup, Session session) {
        this.listofAdducts= new ArrayList<>();
        this.RT = new SimpleDoubleProperty(0);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Score = new SimpleDoubleProperty(0);
        this.session = session;
        this.OGroupObject=null;

    }
    
    //add Slice to Adduct
    public void addSlice(Slice slice) {
        getListofSlices().add(slice);
        
    }
    
    //add Reference Slice
    public void addRefSlice(Slice slice) {
        getListofRefSlices().add(slice);
        slice.generateRefPeak();
        
    }
    
    //add adduct to an OGroup
    public void addAdduct(Entry adduct) {
        this.getListofAdducts().add(adduct);
        this.setRT(new SimpleDoubleProperty(((this.getRT() * (getListofAdducts().size() - 1)) + adduct.getRT()) / getListofAdducts().size()));

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
    public List<Slice> getListofSlices() {
        return listofSlices;
    }

    
    
    public void generateAvgEIC() {

        
        
        double startRT = this.getOGroupRT()-session.getRTTolerance()+0.05;
        double endRT = (this.getOGroupRT()+(session.getRTTolerance()-0.05));
       

        
        //generate intensityFunction for all slices
      for (int i = 0; i< this.getListofRefSlices().size(); i++) {
          this.getListofRefSlices().get(i).generateInterpolatedEIC();
      }
      int resolution = this.getListofRefSlices().get(0).getIntensityArray().length;
        
      RTArray = new double[resolution];
      IntensityArray = new double[resolution];
      double[] currentint = new double[this.getListofRefSlices().size()];
     
      
      //fill Arrays
      RTArray = this.getListofRefSlices().get(0).getRTArray();
      for (int i = 0; i< resolution; i++) {
          currentint = new double[this.getListofRefSlices().size()];
          for (int j =0; j<this.getListofRefSlices().size(); j++) {
              currentint[j] = (this.getListofRefSlices().get(j).getIntensityArray()[i]);
          }
          Arrays.sort(currentint);
          IntensityArray[i]=summ(currentint);

      }
    
     double[] MaxArray  = Arrays.copyOf(IntensityArray, resolution);
     Arrays.sort(MaxArray);
     
     for (int i = 0; i< resolution; i++) {
         IntensityArray[i] = IntensityArray[i]/MaxArray[resolution-1];
         
     }
      
    }
    
    
    
    //generates an array with gaussian peak probability through correlation with such a peak over all slices
    public void generateGaussProp() {
         //initialize Array holding probabilities
        setPropArray(new double[this.IntensityArray.length]);
         double[] peakArray = {0.30562389380800614, 0.4045593930181101, 0.5010142078557377, 0.5697809675082599, 0.7126271863152996, 0.7675927216635093, 0.8845355890078511, 0.9218788811348794, 0.9336462411287345, 1.0, 0.9712913331424721, 0.7660152062379959, 0.7391207258124926, 0.6103812352993977, 0.47315901034928215, 0.4162911178032002, 0.30054754596741007}; 
         int peakint = 9;

         
         
         PearsonsCorrelation pear = new PearsonsCorrelation();

        for (int j = 0; j< this.listofSlices.size(); j++) {
        for (int i = 0; i< (this.getListofSlices().get(j).getIntensityArray().length-peakArray.length); i++) {
        double corr = pear.correlation(peakArray ,Arrays.copyOfRange(this.getListofSlices().get(j).getIntensityArray(), i, i+peakArray.length));
                getPropArray()[i+peakint]+= corr;
        
        }
    }
    }
    
    public void generateEntryGaussProp() {
        for (int i = 0; i< this.listofAdducts.size(); i++) {
            
            
        }
        
        
    }
    

    /**
     * @return the RTArray
     */
    public double[] getRTArray() {
        return RTArray;
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

    /**
     * @return the listofRefSlices
     */
    public List<Slice> getListofRefSlices() {
        return listofRefSlices;
    }

    /**
     * @param listofRefSlices the listofRefSlices to set
     */
    public void setListofRefSlices(List<Slice> listofRefSlices) {
        this.listofRefSlices = listofRefSlices;
    }

   
    

    /**
     * @return the PropArray
     */
    public double[] getPropArray() {
        return PropArray;
    }

    /**
     * @param PropArray the PropArray to set
     */
    public void setPropArray(double[] PropArray) {
        this.PropArray = PropArray;
    }
    
    public double getMinRT() {
        if(OGroupObject!=null){
            return (OGroupObject.getRT()-session.getRTTolerance());
        } else {
            return (getRT()-session.getRTTolerance());
        }
    }
     public double getMaxRT() {
        if(OGroupObject!=null){
            return (OGroupObject.getRT()+session.getRTTolerance());
        } else {
            return (getRT()+session.getRTTolerance());
        }
    }
     public double getMinMZ() {
        return (getMZ()-session.getMZTolerance());
    }
      public double getMaxMZ() {
        return (getMZ()+session.getMZTolerance());
    }
      
      
}
