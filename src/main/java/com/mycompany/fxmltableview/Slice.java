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
    
    public int Num; //Num from Intput Matrix, each adduct has its own Num
    public float minRT, maxRT;
    public float minMZ, maxMZ;
    public List<Float> retentionTimeList = new ArrayList<Float>();
    public List<Float> intensityList = new ArrayList<Float>();
    public List<Float> massList = new ArrayList<Float>();
   
    public Slice(int Num, float MZ, float MZTolerance, float RT, float RTTolerance) {
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
        if (listofScans.get(i).getRetentionTime()>= minRT && listofScans.get(i).getRetentionTime()<= maxRT) {
                        
                        
                        
                        //TODO binary search!!!!!!
                        for (int l=0; l<listofScans.get(i).getPeakscount(); l++) {
                            if (listofScans.get(i).getMassovercharge()[l] >= minMZ && listofScans.get(i).getMassovercharge()[l] <= maxMZ) {
                                retentionTimeList.add(listofScans.get(i).getRetentionTime());
                                intensityList.add(listofScans.get(i).getIntensity()[l]);
                                massList.add(listofScans.get(i).getMassovercharge()[l]);
                                
                                
                                
                            }
                            
                        }
                        
                    }
        }
      System.out.println(retentionTimeList.size());
      System.out.println(intensityList.size());
      System.out.println(massList.size());
    }
    
    
}
