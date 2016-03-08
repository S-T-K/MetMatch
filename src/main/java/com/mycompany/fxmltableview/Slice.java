/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;

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
   
    public Slice(RawDataFile file, int Num, float MZ, float MZTolerance, float RT, float RTTolerance) {
        this.file = file;
        this.Num=Num;
        this.minRT = RT-RTTolerance;
        this.maxRT = RT+RTTolerance;
        this.minMZ = MZ-MZTolerance;
        this.maxMZ = MZ+MZTolerance;
        this.RT = RT;
        
        
    }
    
    public void extractSlice(List<Scan> listofScans) {
         //for all Scans
        for (int i = 0; i< listofScans.size(); i++) {
            //if RT is within tolerance
            boolean found;
            float currentRT = listofScans.get(i).getRetentionTime();
            
        if (currentRT>= getMinRT() && currentRT<= getMaxRT()) {
            
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
           float dist = abs(retentionTimeList.get(peakList.get(i).getRt())-RT);
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
    
    System.out.println(retentionTimeList.size());
    System.out.println(intensityList.size());
     System.out.println(massList.size());
    
    for (int i =0; i<massList.size(); i++) {
       float intensity = intensityList.get(i);
       System.out.println(i);
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
        
    retentionTimeList=newRTList;
        setIntensityList(newIntList);
    massList = newMZList;
        
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

}