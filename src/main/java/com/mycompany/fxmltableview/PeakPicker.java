/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import static java.lang.Math.abs;
import java.util.List;

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
    public void pick(Slice slice, float baseline, float minlength) {

        List<Float> intensityList = slice.getIntensityList();
        List<Float> rtList = slice.getRetentionTimeList();
        List<Float> mzList = slice.getMassList();

        for (int i = 0; i < intensityList.size()-2; i = getnextvalue(rtList, intensityList, i)) {
System.out.println(i);
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
                slice.addPeak(new Peak(mz, rtList.get(maxint), rtList.get(i), rtList.get(j - 1), slice));
                System.out.println("MZ: " + mz);
                System.out.println("RT: " + rtList.get(maxint));
                System.out.println("int: " + intensityList.get(maxint));
                System.out.println("start: " + rtList.get(i));
                System.out.println("end: " + rtList.get(j - 1));
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

}
