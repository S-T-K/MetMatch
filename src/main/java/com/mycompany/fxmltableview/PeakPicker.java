/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import static java.lang.Math.abs;
import java.util.List;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
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
    
    //extract "typical" peak
    public void extractPeak(List<Entry> listofAdducts) {
        List<float[]> listofpeaks;
        List<Integer> starts = new ArrayList<>();
        List<Integer> peaks =  new ArrayList<>();
        List<Integer> ends =  new ArrayList<>();
        List<List<Float>> listofcurves =  new ArrayList<>();
        
        System.out.println(listofAdducts.size());
        //pick middle peak for all adducts
        for (int i =0; i<listofAdducts.size(); i++) {
            //get first slice(=first file)
            Slice slice = listofAdducts.get(i).getListofSlices().get(0);
            
            int size = slice.getIntensityList().size();
             List<Float> smooth = slice.smooth(10);
             listofcurves.add(smooth);
             
             //look for max in middle region
             float max =0;
             int peak = 0;
             for (int j = 0; j< 10; j++) {
                 //look rigth
                 if (smooth.get((size/2)+i) > max) {
                     max = smooth.get((size/2)+i);
                     peak = (size/2)+i;
                 }
                 //look left
                 if (smooth.get((size/2)-i) > max) {
                     max = smooth.get((size/2)-i);
                     peak = (size/2)-i;
                 }
             }
            
             peaks.add(peak);
             System.out.println("Peak: " + peak);
             //find upper end 
             int j = peak;
             while (j< size-1 && smooth.get(j)>= smooth.get(j+1)) {
                 j++;
             }
            int end = j;
            ends.add(end);
            System.out.println("End: " + end);
            //find lower end
            j = peak-1;
             while (j> 2 && smooth.get(j)>= smooth.get(j-1)) {
                 j--;
             }
            int start = j-1;
            starts.add(start);
            System.out.println("Start: " + start);
        }
        
        //determine length of final array
        int right = 0;
        int left = 0;
        for (int j = 0; j<starts.size(); j++) {
            if (peaks.get(j)-starts.get(j) > left) {
                left = peaks.get(j)-starts.get(j);
            }
            if (ends.get(j) - peaks.get(j) > right) {
                right = ends.get(j) - peaks.get(j);
            }
        }
        
        int length = right+left;
        
        //fill 
        List<Float> finalcurve = new ArrayList<Float>(Collections.nCopies(length, 0.0f));
        List<Integer> divide = new ArrayList<Integer>(Collections.nCopies(length, 0));
        
        
        for (int j = 0; j < starts.size(); j++) {
            System.out.println(peaks.get(j)-starts.get(j));
            for (int k =starts.get(j); k< ends.get(j); k++) {
                System.out.println(listofcurves.get(j).get(k));
                
            }
            System.out.println();
        }
        
       
        
        
        
    }
    
    //generates average peak
    //TODO smooth
    //TODO check for EIC quality
    
  
            
    
}
