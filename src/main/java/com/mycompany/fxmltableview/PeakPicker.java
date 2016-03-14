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
import static java.lang.Math.abs;
import static java.lang.Math.abs;
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
