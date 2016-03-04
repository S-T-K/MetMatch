/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefankoch
 */
public class Slice {
    
    private RawDataFile file;
    private int Num; //Num from Intput Matrix, each adduct has its own Num
    private float minRT, maxRT;
    private float minMZ, maxMZ;
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
   
    public Slice(RawDataFile file, int Num, float MZ, float MZTolerance, float RT, float RTTolerance) {
        this.file = file;
        this.Num=Num;
        this.minRT = RT-RTTolerance;
        this.maxRT = RT+RTTolerance;
        this.minMZ = MZ-MZTolerance;
        this.maxMZ = MZ+MZTolerance;
        
        
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
                            getMassList().add(null);
                            
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
    
    
}
