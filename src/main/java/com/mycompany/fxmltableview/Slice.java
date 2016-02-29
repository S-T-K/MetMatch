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
    
    private String file;
    private int Num; //Num from Intput Matrix, each adduct has its own Num
    private float minRT, maxRT;
    private float minMZ, maxMZ;
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
   
    public Slice(String file, int Num, float MZ, float MZTolerance, float RT, float RTTolerance) {
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
            boolean found = false;
        if (listofScans.get(i).getRetentionTime()>= minRT && listofScans.get(i).getRetentionTime()<= maxRT) {
                        found = false;
                        
                        
                        //TODO binary search!!!!!!
                        for (int l=0; l<listofScans.get(i).getPeakscount(); l++) {
                            if (listofScans.get(i).getMassovercharge()[l] >= minMZ && listofScans.get(i).getMassovercharge()[l] <= maxMZ) {
                                getRetentionTimeList().add(listofScans.get(i).getRetentionTime());
                                getIntensityList().add(listofScans.get(i).getIntensity()[l]);
                                getMassList().add(listofScans.get(i).getMassovercharge()[l]);
                                found = true;
                                
                                
                                
                            }
                            
                        }
                        if (!found) {
                            getRetentionTimeList().add(listofScans.get(i).getRetentionTime());
                            getIntensityList().add(0.0f);
                            getMassList().add(null);
                            
                        }
                    }
        }
      System.out.println(getRetentionTimeList().size());
      System.out.println(getIntensityList().size());
      System.out.println(getMassList().size());
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
    
    
}
