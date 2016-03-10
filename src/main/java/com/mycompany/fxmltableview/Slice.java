/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;
import java.util.Arrays;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 *
 * @author stefankoch
 */
public class Slice {
    
    private RawDataFile file;
    private int Num; //Num from Intput Matrix, each adduct has its own Num
    private float minRT, maxRT, RT;
    private float minMZ, maxMZ;
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
    private List<Peak> peakList = new ArrayList<Peak>();
    private int bestPeak;
    private boolean hasPeaks = false;
    private PolynomialSplineFunction intensityFunction;
   
    
    private double[] RTArray ;
    private double[] IntensityArray;
    
    public Slice(RawDataFile file, int Num, float MZ, float MZTolerance, float RT, float RTTolerance) {
        this.file = file;
        this.Num=Num;
        this.minRT = RT-RTTolerance;
        this.maxRT = RT+RTTolerance;
        this.minMZ = MZ-MZTolerance;
        this.maxMZ = MZ+MZTolerance;
        this.RT = RT;
        
        
    }
    
    //Copy constructor, used when generating avgEIC
    public Slice(Slice slice) {
        this.file = slice.getFile();
        this.Num=slice.getNum();
        this.minRT = slice.getMinRT();
        this.maxRT = slice.getMaxRT();
        this.minMZ = slice.getMinMZ();
        this.maxMZ = slice.getMaxMZ();
        this.RT = slice.getRT();

    }
    
    public void extractSlicefromScans(List<Scan> listofScans) {
         //for all Scans
        for (int i = 0; i< listofScans.size(); i++) {
            //if RT is within tolerance
            boolean found;
            float currentRT = listofScans.get(i).getRetentionTime();
            
            //0.05 so that ranges for the interpolation are smaller than the actual ranges, otherwise out of range
        if (currentRT>= (getMinRT()) && currentRT<= (getMaxRT())) {
            
                        found = false;
                        
                        
                        //TODO binary search!!!!!!
                        for (int l=0; l<listofScans.get(i).getPeakscount(); l++) {
                            if (listofScans.get(i).getMassovercharge()[l] >= getMinMZ() && listofScans.get(i).getMassovercharge()[l] <= getMaxMZ()) {
                                getRetentionTimeList().add(currentRT);
                                getIntensityList().add(listofScans.get(i).getIntensity()[l]);
                                getMassList().add(listofScans.get(i).getMassovercharge()[l]);
                                found = true;
                                
                                
                                
                            }
                            
                        }
                        if (!found) {
                            getRetentionTimeList().add(currentRT);
                            getIntensityList().add(0.0f);
                            getMassList().add(0.0f);
                            
                        }
                    }
        }
      
     this.clean();
    }
    
    
    //interpolates intensities
    public  void generateIntensityFunction() {
       double[] RT = new double[this.retentionTimeList.size()];
       double[] Intensity = new double[this.retentionTimeList.size()];
       for (int i =0; i<RT.length; i++) {
           RT[i] = this.retentionTimeList.get(i);
           Intensity[i] = this.intensityList.get(i);
       }
        
        
        
       LinearInterpolator interpolator = new LinearInterpolator();
       
       this.intensityFunction = interpolator.interpolate(RT, Intensity);
    }

    /**
     * @return the retentionTimeList
     */
    public List<Float> getRetentionTimeList() {
        return retentionTimeList;
    }

    /**
     * @return the intensityList
     */
    public List<Float> getIntensityList() {
        return intensityList;
    }

    /**
     * @return the massList
     */
    public List<Float> getMassList() {
        return massList;
    }

    /**
     * @return the minRT
     */
    public float getMinRT() {
        return minRT;
    }

    /**
     * @return the maxRT
     */
    public float getMaxRT() {
        return maxRT;
    }

    /**
     * @return the minMZ
     */
    public float getMinMZ() {
        return minMZ;
    }

    /**
     * @return the maxMZ
     */
    public float getMaxMZ() {
        return maxMZ;
    }

    /**
     * @return the file
     */
    public RawDataFile getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(RawDataFile file) {
        this.file = file;
    }

    public void addPeak(Peak peak) {
        this.peakList.add(peak);
        
    }
    /**
     * @return the peakList
     */
    public List<Peak> getPeakList() {
        return peakList;
    }

    /**
     * @param peakList the peakList to set
     */
    public void setPeakList(List<Peak> peakList) {
        this.peakList = peakList;
    }

    /**
     * @return the bestPeak
     */
    public int getBestPeak() {
       bestPeak = 0; 
        
       float min = 1000;
     
        for (int i = 0; i< peakList.size(); i++) {
           float dist = abs(retentionTimeList.get(peakList.get(i).getRt())-getRT());
           if (dist < min) {
               bestPeak = i;
               min = dist;
               
           }
            
        }
     
        return bestPeak;
    }

    /**
     * @param bestPeak the bestPeak to set
     */
    public void setBestPeak(int bestPeak) {
        this.bestPeak = bestPeak;
    }
    
    //removes duplicate RT entries, only takes the max intensity
    public void clean() {
        
    List<Float> newRTList = new ArrayList<>();
    List<Float> newIntList= new ArrayList<>();
    List<Float> newMZList= new ArrayList<>();
    
    
    for (int i =0; i<massList.size(); i++) {
       float intensity = intensityList.get(i);
       
       float mz = massList.get(i);
       while (i<retentionTimeList.size()-1 && abs(retentionTimeList.get(i)-retentionTimeList.get(i+1))<0.001) {
           if (intensityList.get(i+1)> intensity) {
               intensity = intensityList.get(i+1);
               mz = massList.get(i+1);
           }
           i++; 
       }
       
       newRTList.add(retentionTimeList.get(i));
       newIntList.add(intensity);
       newMZList.add(mz);
        
        
        
        
    }
        
        setRetentionTimeList(newRTList);
        setIntensityList(newIntList);
        setMassList(newMZList);
        
}

    
    
    public List<Float> smooth(int iterations) {
        List<Float> newIntList= new ArrayList<>(intensityList);
        for (int i= 0; i<iterations; i++) {
      
            for (int j = 1; j< (retentionTimeList.size()-1); j++) {
                if (newIntList.get(j)>100) {
                newIntList.set(j, (newIntList.get(j-1)+ newIntList.get(j) + newIntList.get(j+1))/3);
                
            }
            }
        
        }
    return newIntList;
}

    /**
     * @param intensityList the intensityList to set
     */
    public void setIntensityList(List<Float> intensityList) {
        this.intensityList = intensityList;
    }

    /**
     * @return the hasPeaks
     */
    public boolean isHasPeaks() {
        return hasPeaks;
    }

    /**
     * @param hasPeaks the hasPeaks to set
     */
    public void setHasPeaks(boolean hasPeaks) {
        this.hasPeaks = hasPeaks;
    }

    /**
     * @return the Num
     */
    public int getNum() {
        return Num;
    }

    /**
     * @param Num the Num to set
     */
    public void setNum(int Num) {
        this.Num = Num;
    }

    /**
     * @return the RT
     */
    public float getRT() {
        return RT;
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(float RT) {
        this.RT = RT;
    }

    /**
     * @param retentionTimeList the retentionTimeList to set
     */
    public void setRetentionTimeList(List<Float> retentionTimeList) {
        this.retentionTimeList = retentionTimeList;
    }

    /**
     * @param massList the massList to set
     */
    public void setMassList(List<Float> massList) {
        this.massList = massList;
    }

    /**
     * @return the intensityFunction
     */
    public PolynomialSplineFunction getIntensityFunction() {
        return intensityFunction;
    }

    /**
     * @param intensityFunction the intensityFunction to set
     */
    public void setIntensityFunction(PolynomialSplineFunction intensityFunction) {
        this.intensityFunction = intensityFunction;
    }

    
    public void generateAvgEIC() {

        
        int resolution = 30;  
        double startRT = this.minRT+0.05;
        double endRT = this.maxRT-0.05;
       

        
       generateIntensityFunction();
        
        setRTArray(new double[resolution]);
        setIntensityArray(new double[resolution]);
      
     
      
      //fill Arrays
      for (int i = 0; i< resolution; i++) {
            getRTArray()[i] = startRT+(((endRT-startRT))/(resolution-1))*i;
            getIntensityArray()[i]=getIntensityFunction().value(getRTArray()[i]);

      }
    
     double[] MaxArray  = Arrays.copyOf(getIntensityArray(), resolution);
     Arrays.sort(MaxArray);
     
     for (int i = 0; i< resolution; i++) {
            getIntensityArray()[i] = getIntensityArray()[i]/MaxArray[resolution-1];
         
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
}