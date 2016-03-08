/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import static java.lang.Math.abs;
import java.util.List;
import static java.lang.Math.abs;

/**
 *
 * @author stefankoch
 */
public class PeakPicker {

    //TODO: implement different methods
    private String method;

    public PeakPicker() {

    }

    //simple Baseline peak detection
    //picks continuous peaks above the baseline
    public void simplepick(Slice slice, float baseline, float minlength) {

        List<Float> intensityList = slice.getIntensityList();
        List<Float> rtList = slice.getRetentionTimeList();
        List<Float> mzList = slice.getMassList();

        for (int i = 0; i < intensityList.size()-2; i = getnextvalue(rtList, intensityList, i)) {

            boolean longenough = false;
            int length = 0;
            float mz = 0;
            int maxint = 0;
            if (intensityList.get(i) <= baseline) {
                continue;
            }
            mz = mzList.get(i);
            maxint = i;
            int j = getnextvalue(rtList, intensityList, i);
            while (j < intensityList.size() && intensityList.get(j) > baseline) {
                mz = ((mz * length) + mzList.get(j)) / (length + 1);

                if (intensityList.get(j) > intensityList.get(maxint)) {
                    maxint = j;
                }

                if (!longenough && rtList.get(j) - rtList.get(i) > minlength) {
                    longenough = true;
                }
                
                j=getnextvalue(rtList, intensityList, j);
                length++;
            }

            if (longenough) {
                slice.addPeak(new Peak(maxint, i, j - 1, slice));
//                System.out.println("MZ: " + mz);
//                System.out.println("RT: " + rtList.get(maxint));
//                System.out.println("int: " + intensityList.get(maxint));
//                System.out.println("start: " + rtList.get(i));
//                System.out.println("end: " + rtList.get(j - 1));
                i = j;

            }

        }
    }

    public int getnextvalue(List<Float> rtList, List<Float> intensityList, int i) {
       
        
        int t = i;
        while (abs(rtList.get(t) - rtList.get(t+1)) < 0.001) {
            t++; 
        }
        if (t < rtList.size()) {
        t++; }
        
        float max = intensityList.get(t);
        int maxint = t;
        while (abs(rtList.get(t) - rtList.get(t+1)) < 0.001) {
            if (max < intensityList.get(t+1))  {
                max = intensityList.get(t+1);
                maxint = t+1;
                
            }
            t++;
        }
        
        
   
        return maxint;
    }
    
    
    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    public void pick(Slice slice, float baseline) {
        //climbs up the peaks
        slice.clean();
        int start;
        int peak;
        int end;
        List<Float> smooth = slice.smooth(5);
        
        
        
        for (int i = 0; i< slice.getIntensityList().size(); i++) {
            
            
            //we go down until minimum
            while (i < (slice.getIntensityList().size()-2) && smooth.get(i)>= smooth.get(i+1)) {
                i++;
            }
            start = i;
            
            //we go up until maximum
            while (i < (slice.getIntensityList().size()-2) && smooth.get(i)<= smooth.get(i+1)) {
                i++;
            }
            
            if (slice.getIntensityList().get(i)> baseline) {
            peak = i;
            
            //we go down until minimum
            while (i < (slice.getIntensityList().size()-2) && smooth.get(i)>= smooth.get(i+1)) {
                i++;
            }
            end = i;
         
            if (end - start > 10) {
            slice.addPeak(new Peak(peak, start,end,slice ));
            slice.setHasPeaks(true); }
      
            
            }
        }
        
        
    }
    
}
