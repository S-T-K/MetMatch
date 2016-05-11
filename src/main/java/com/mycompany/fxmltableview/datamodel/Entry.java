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
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleFloatProperty;
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
    
    private SimpleFloatProperty RT;
    private SimpleFloatProperty Score;
    private SimpleFloatProperty Scorepeakfound;
    private SimpleFloatProperty Scorepeakclose;
    private SimpleFloatProperty Scorecertainty;
    private IntegerProperty Num;
    private SimpleFloatProperty MZ;
    private IntegerProperty Xn;
    private IntegerProperty OGroup;
    private StringProperty Ion;
    private SimpleFloatProperty M;
    private int Charge;
    private String ScanEvent;
    private String Ionisation;
    private Entry OGroupObject;
    private List<Entry> listofAdducts;
    private HashMap<RawDataFile, Slice> listofSlices;   //stores all slices
    private Session session;
    private HashMap<RawDataFile, Float> Scores;
    private HashMap<RawDataFile, Float> Certainties;
    private Entry originalAdduct;
    private boolean empty;

    
    
    //for peak probability
    private HashMap<RawDataFile, Short> Interpolatedshift;
    private HashMap<RawDataFile, Float> OgroupShift;
    
    //for penalties and bonuses in certain regions
    private HashMap<RawDataFile, short[]> PenArray;
    
    //maxIntensity of all Slices
    private float maxIntensity;

//    //Interpolated Arrays
//    private float[] RTArray;
//    private float[] IntensityArray;
    
    public Entry() {
    }

    //constructor for Adduct
    public Entry(int Num, float MZ, float RT, int Xn, int OGroup, String Ion, float M, int Charge, String scanEvent, String Ionisation, Session session, Entry ogroup) {
        this.Num = new SimpleIntegerProperty(Num);
        this.MZ = new SimpleFloatProperty((float) MZ);
        this.RT = new SimpleFloatProperty((float) RT);
        this.Xn = new SimpleIntegerProperty(Xn);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Ion = new SimpleStringProperty(Ion);
        this.M = new SimpleFloatProperty((float) M);
        this.Charge=Charge;
        this.ScanEvent=scanEvent;
        this.Ionisation=Ionisation;
        this.Score = new SimpleFloatProperty(0);
        this.Scorepeakclose = new SimpleFloatProperty(0);
        this.Scorepeakfound = new SimpleFloatProperty(0);
        this.Scorecertainty = new SimpleFloatProperty(Float.NaN);
        this.listofSlices = new HashMap<RawDataFile, Slice>();

        this.Scores = new HashMap<RawDataFile, Float>();
        
        this.session=session;
        this.OGroupObject=ogroup;
        this.maxIntensity = 0;
     
    }
    //constructor for generated Adduct
    public Entry(int Num, float MZ, float RT, int Xn, int OGroup, String Ion, float M, Session session, Entry ogroup, Entry orig) {
        this.Num = new SimpleIntegerProperty(Num);
        this.MZ = new SimpleFloatProperty((float) MZ);
        this.RT = new SimpleFloatProperty((float) RT);
        this.Xn = new SimpleIntegerProperty(Xn);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Ion = new SimpleStringProperty(Ion);
        this.M = new SimpleFloatProperty((float) M);
        this.Score = new SimpleFloatProperty(0);
        this.Scorepeakclose = new SimpleFloatProperty(0);
        this.Scorepeakfound = new SimpleFloatProperty(0);
        this.Scorecertainty = new SimpleFloatProperty(1.0f);
        this.listofSlices = new HashMap<RawDataFile, Slice>();

        this.Scores = new HashMap<RawDataFile, Float>();
        
        this.session=session;
        this.OGroupObject=ogroup;
        this.maxIntensity = 0;
       
        this.originalAdduct = orig;
    }
    
   
    //constructor for OGroup/Metabolite
    public Entry(int OGroup, Session session) {
        this.listofAdducts= new ArrayList<>();
        this.RT = new SimpleFloatProperty(0);
        this.OGroup = new SimpleIntegerProperty(OGroup);
        this.Score = new SimpleFloatProperty(0);
        this.Scorepeakclose = new SimpleFloatProperty(0);
        this.Scorepeakfound = new SimpleFloatProperty(0);
        this.Scorecertainty = new SimpleFloatProperty((float) 1.0);
        this.session = session;
        this.OGroupObject=null;

        Interpolatedshift = new HashMap<>();
        OgroupShift = new HashMap<>();
        this.Scores = new HashMap<RawDataFile, Float>();
        this.Certainties = new HashMap<RawDataFile, Float>();
        PenArray = new HashMap<RawDataFile, short[]>();

    }
    
    //add Slice to Adduct
    public void addSlice(Slice slice) {
        listofSlices.put(slice.getFile(), slice);
        if (slice.getMaxIntensity()>maxIntensity) {
            maxIntensity = slice.getMaxIntensity();
        }
        
    }
    
    public void delteemptySlices() {
HashMap<RawDataFile, Slice> newlist = new HashMap<>();
for (Map.Entry<RawDataFile, Slice> entry:listofSlices.entrySet()) {
    if (!entry.getValue().isEmpty()) {
        newlist.put(entry.getKey(), entry.getValue());
    }
}
listofSlices=newlist;
        
    }
    
    //add adduct to an OGroup
    public void addAdduct(Entry adduct) {
        this.getListofAdducts().add(adduct);
        this.setRT(new SimpleFloatProperty((float) (((this.getRT() * (getListofAdducts().size() - 1)) + adduct.getRT()) / getListofAdducts().size())));
    }
    
//    public void generateRTArray() {
//        
//        int resolution = getSession().getResolution();
//        float startRT = (float) this.getMinRT();
//        float endRT = (float) this.getMaxRT();
//        setRTArray(new float[resolution]);
//        
//     
//      
//      //fill Arrays
//      for (int i = 0; i< resolution; i++) {
//            getRTArray()[i] = startRT+(((endRT-startRT))/(resolution-1))*i;
//      }
//        
//        
//    }
    
    //generates PropArray of a dataset for an Adduct 
    public void peakpickAdduct(RawDataFile file) throws InterruptedException {
//        float [] propArray = new float[getSession().getResolution()];
//
if (listofSlices.containsKey(file)) {
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
//        float [] sliceArray = currentSlice.getPropArray();
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
    }}
    
    //generates average PropArray over all Adducts for a dataset
    //TODO: Avg?
    public void peakpickOGroup(RawDataFile file) throws InterruptedException {
        
        //float [] propArray = new float[getSession().getResolution()];
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
//        float max = (float) Collections.max(asList);
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
    public float getRT() {
        return RT.get();
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(SimpleFloatProperty RT) {
        this.RT = RT;
    }

    /**
     * @return the Score
     */
    public float getScore() {
        return Score.get();
    }

    public SimpleFloatProperty ScoreProperty() {
        return Score;
    }
    /**
     * @param Score the Score to set
     */
    public void setScore(SimpleFloatProperty score) {
        this.Score = score;
    }
    /**
     * @return the Score
     */
    public float getScorepeakfound() {
        return Scorepeakfound.get();
    }

    public SimpleFloatProperty ScorepeakfoundProperty() {
        return Scorepeakfound;
    }
    /**
     * @param Score the Score to set
     */
    public void setScorepeakfound(SimpleFloatProperty score) {
        this.Scorepeakfound = score;
    }
    
    /**
     * @return the Score
     */
    public float getScorecertainty() {
        return Scorecertainty.get();
    }

    public SimpleFloatProperty ScorecertaintyProperty() {
        return Scorecertainty;
    }
    /**
     * @param Score the Score to set
     * set Score for all adducts
     */
    public void setScorecertainty(SimpleFloatProperty score) {
        this.Scorecertainty = score;
        for (int i = 0; i<listofAdducts.size(); i++) {
            listofAdducts.get(i).Scorecertainty= score;
        }
    }
    /**
     * @return the Score
     */
    public float getScorepeakclose() {
        return Scorepeakclose.get();
    }

    public SimpleFloatProperty ScorepeakcloseProperty() {
        return Scorepeakclose;
    }
    /**
     * @param Score the Score to set
     */
    public void setScorepeakclose(SimpleFloatProperty score) {
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
    public float getMZ() {
        if (this.MZ==null) {
            return 0;
        }
        return MZ.get();
    }

    /**
     * @param MZ the MZ to set
     */
    public void setMZ(SimpleFloatProperty MZ) {
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
    public float getM() {
        return M.get();
    }

    /**
     * @param M the M to set
     */
    public void setM(SimpleFloatProperty M) {
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
   
    
   

//    /**
//     * @return the RTArray
//     */
//    public float[] getRTArray() {
//       if(OGroupObject!=null){
//            return (OGroupObject.getRTArray());
//        } else {
//            return (RTArray);
//        }
//    }
//
//    /**
//     * @param RTArray the RTArray to set
//     */
//    public void setRTArray(float[] RTArray) {
//        this.RTArray = RTArray;
//    }
//
//    /**
//     * @return the IntensityArray
//     */
//    public float[] getIntensityArray() {
//        return IntensityArray;
//    }
//
//    /**
//     * @param IntensityArray the IntensityArray to set
//     */
//    public void setIntensityArray(float[] IntensityArray) {
//        this.IntensityArray = IntensityArray;
//    }

    
    public float median(float[] m) {
    int middle = m.length/2;
    if (m.length%2 == 1) {
        return m[middle];
    } else {
        return (m[middle-1] + m[middle]) / 2.0f;
    }
}
    
    public float summ(float[] m) {
        float sum =0;
        for (int i =0; i<m.length; i++) {
            sum+=m[i];
            
        }
        return sum;
    }
    
    public float getOGroupRT() {
        
        return this.OGroupObject.getRT();
    }

  

   
    

    
    public float getMinRT() {
        if(OGroupObject!=null){
            return (OGroupObject.getRT()-getSession().getRTTolerance());
        } else {
            return (getRT()-getSession().getRTTolerance());
        }
    }
     public float getMaxRT() {
        if(OGroupObject!=null){
            return (OGroupObject.getRT()+getSession().getRTTolerance());
        } else {
            return (getRT()+getSession().getRTTolerance());
        }
    }
     public float getMinMZ() {
        return (getMZ()*(1-(getSession().getMZTolerance()/1000000)));
    }
      public float getMaxMZ() {
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
     * @param fittedShift the OGroupfittedShift to set
     */
    public void setFittedShift(RawDataFile file, short shift) {
        this.Interpolatedshift.put(file,shift);
        float shiftintime = session.getProparraycalculator().getshiftintime(shift);
        
        this.OgroupShift.put(file, shiftintime);
        
        
        
        
        for (int i = 0; i<listofAdducts.size(); i++) {
            if (listofAdducts.get(i).getListofSlices().containsKey(file)) {
        listofAdducts.get(i).getListofSlices().get(file).setFittedPeak(shiftintime);
    }
        }
    }

//    /**
//     * @return the AdductPropArray
//     */
//    public float[] getAdductPropArray(RawDataFile file) {
//        float[] PropArray = new float[session.getResolution()];
//        List<Short> list = new ArrayList<>();
//        if (listofSlices.containsKey(file)) {
//        list.addAll(listofSlices.get(file).getPeakIndex());
//        }
//        for (int i = 0; i< list.size(); i++) {
//            PropArray[list.get(i)] = 1;
//        }
//        
//        return PropArray;
//    }

   
//    /**
//     * @return the OGroupPropArray
//     * with only 1 and 0
//     */
//    public float[] getOGroupPropArray(RawDataFile file) {
//        float[] PropArray = new float[session.getResolution()];
//        List<Short> list = new ArrayList<>();
//        for (int i = 0; i<listofAdducts.size(); i++) {
//            if (listofAdducts.get(i).listofSlices.containsKey(file)){
//        list.addAll(listofAdducts.get(i).listofSlices.get(file).getPeakIndex());
//        }}
//        
//        for (int i = 0; i< list.size(); i++) {
//            PropArray[list.get(i)] = 1;
//        }
//        
//        return PropArray;
//    }
    
    //returns a "smooth" PropArray
    public void getOGroupPropArraySmooth(RawDataFile file, float[][] matrix, int row) {
        for (int j = 0; j<listofAdducts.size(); j++) {
        Slice slice = listofAdducts.get(j).listofSlices.get(file);
        if (slice!=null) {
        for (int i = 0; i<slice.getListofPeaks().size(); i++) {
        session.getProparraycalculator().calculate(slice.getListofPeaks().get(i).getIndexRT(), RT.floatValue(), matrix, row);
                }
        }
        }
    }

    

    /**
     * @return the Scores
     */
    public HashMap<RawDataFile, Float> getScores() {
        return Scores;
    }

    /**
     * @param Scores the Scores to set
     */
    public void setScores(HashMap<RawDataFile, Float> Scores) {
        this.Scores = Scores;
    }

   

    
    /**
     * @return the PenArray
     */
    public HashMap<RawDataFile, short[]> getPenArray() {
        return PenArray;
    }

    /**
     * @param PenArray the PenArray to set
     */
    public void setPenArray(HashMap<RawDataFile, short[]> PenArray) {
        this.PenArray = PenArray;
    }

    /**
     * @return the Certainties
     */
    public HashMap<RawDataFile, Float> getCertainties() {
        return Certainties;
    }

    /**
     * @param Certainties the Certainties to set
     */
    public void setCertainties(HashMap<RawDataFile, Float> Certainties) {
        this.Certainties = Certainties;
    }

    /**
     * @return the originalAdduct
     */
    public Entry getOriginalAdduct() {
        return originalAdduct;
    }

    /**
     * @param originalAdduct the originalAdduct to set
     */
    public void setOriginalAdduct(Entry originalAdduct) {
        this.originalAdduct = originalAdduct;
    }

    /**
     * @return the Charge
     */
    public int getCharge() {
        return Charge;
    }

    /**
     * @param Charge the Charge to set
     */
    public void setCharge(int Charge) {
        this.Charge = Charge;
    }

    /**
     * @return the ScanEvent
     */
    public String getScanEvent() {
        return ScanEvent;
    }

    /**
     * @param ScanEvent the ScanEvent to set
     */
    public void setScanEvent(String ScanEvent) {
        this.ScanEvent = ScanEvent;
    }

    /**
     * @return the Ionisation
     */
    public String getIonisation() {
        return Ionisation;
    }

    /**
     * @param Ionisation the Ionisation to set
     */
    public void setIonisation(String Ionisation) {
        this.Ionisation = Ionisation;
    }

    /**
     * @return the empty
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @param empty the empty to set
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    /**
     * @return the Interpolatedshift
     */
    public HashMap<RawDataFile, Short> getInterpolatedshift() {
        return Interpolatedshift;
    }

    /**
     * @param Interpolatedshift the Interpolatedshift to set
     */
    public void setInterpolatedshift(HashMap<RawDataFile, Short> Interpolatedshift) {
        this.Interpolatedshift = Interpolatedshift;
    }

    /**
     * @return the OgroupShift
     */
    public HashMap<RawDataFile, Float> getOgroupShift() {
        return OgroupShift;
    }

    /**
     * @param OgroupShift the OgroupShift to set
     */
    public void setOgroupShift(HashMap<RawDataFile, Float> OgroupShift) {
        this.OgroupShift = OgroupShift;
    }

    public int getInterpolatedshift(RawDataFile currentfile) {
        return Interpolatedshift.get(currentfile);
    }

    
    
    //Comparator to sort List of Entries
    public static class orderbyRT implements Comparator<Entry> {

        @Override
        public int compare(Entry o1, Entry o2) {
            return Float.valueOf(o1.getRT()).compareTo(Float.valueOf(o2.getRT()));
        }
    }

public void addScore(RawDataFile file, float score) {
    this.getScores().put(file, score);
}

public void setScore(RawDataFile file) {
    if (getScores().containsKey(file)) {
        setScore(new SimpleFloatProperty(getScores().get(file)));
    }
    
}

public float getScore(RawDataFile file) {
    return getScores().get(file);
}
    
//for Shiftview, returns max over all Adducts in OGroup
public float getmaxScorepeakfound(RawDataFile file) {
    float max = 0;
    for (int i  = 0; i<listofAdducts.size(); i++) {
            if (listofAdducts.get(i).getListofSlices().containsKey(file)) {
        if (listofAdducts.get(i).getListofSlices().get(file).getScorepeakfound()>max) {
            max = listofAdducts.get(i).getListofSlices().get(file).getScorepeakfound();
        }
    }}
    return max;
}

//for Shiftview, returns min over all Adducts in OGroup
public float getminScorepeakclose(RawDataFile file) {
    float min = 1;
    for (int i  = 0; i<listofAdducts.size(); i++) {
        if (listofAdducts.get(i).getListofSlices().containsKey(file)){
        if (listofAdducts.get(i).getListofSlices().get(file).getScorepeakclose()<min) {
            min = listofAdducts.get(i).getListofSlices().get(file).getScorepeakclose();
        }}
    }
    return min;
}

public float getmaxScoredistance(RawDataFile file) {
       float max = 0; 
       for (int i  = 0; i<listofAdducts.size(); i++) {
           if (listofAdducts.get(i).getListofSlices().containsKey(file)) {
        if (listofAdducts.get(i).getListofSlices().get(file).getScoredistance()>max) {
            max = listofAdducts.get(i).getListofSlices().get(file).getScoredistance();
        }}
    }
    return max;  
}
      

public float getPeakfound(RawDataFile file) {
    
    for (int i = 0; i< listofAdducts.size(); i++) {
        if (listofAdducts.get(i).listofSlices.containsKey(file)) {
        if (listofAdducts.get(i).listofSlices.get(file).getScorepeakfound()==1)
         return 1;
        
    }}
    
    return 0;
}

public XYChart.Series manualPeak(RawDataFile file, float start, float end) throws InterruptedException {
    
    //switch if wrong direction
    if (start>end) {
        float temp = start;
        start = end;
        end = temp; 
    }
    
    
    
    start =  Arrays.binarySearch(file.getRTArray(), start);
        
       if (start<0) {
            start=(start + 1) * (-1);
       }
       
       
    
    end = Arrays.binarySearch(file.getRTArray(), end)-1;
       
       if (end<0) {
            end = (end + 3) * (-1);
       }
    
    if ((end!=start)&&listofSlices.containsKey(file)){
        Slice slice = listofSlices.get(file);
    return slice.manualPeak((short)(start-slice.getRTstart()), (short) (end-slice.getRTstart()));
    } else {
        return null;
    }

}

public boolean isStored(RawDataFile file) {
    boolean stored = false;
    for (int i = 0; i<listofAdducts.size(); i++) {
        if (listofAdducts.get(i).getListofSlices().containsKey(file)&&listofAdducts.get(i).getListofSlices().get(file).isStored()) {
            stored = true;
            break;
        }
        
    }
    
    return stored;
}
    




}
