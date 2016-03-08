/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author stefankoch
 */
public class PeakComparer {
    
    
    public PeakComparer() {
        
        
    }
    
    public double compare(Peak peak1, Peak peak2) {
        
        //calculate length of arrays
        int right = Math.min((peak1.getRtend()-peak1.getRt()),(peak2.getRtend()-peak2.getRt()));
        int left = Math.min((peak1.getRt()-peak1.getRtstart()),(peak2.getRt()-peak2.getRtstart()));
        
        double[] list1 = new double[right+left+1];
        double[] list2 = new double[right+left+1];
        
        for (int i = 0; i<= left; i++) {
            list1[i] = peak1.getSlice().getIntensityList().get(peak1.getRt()-left+i);
            list2[i] = peak2.getSlice().getIntensityList().get(peak2.getRt()-left+i);
        }
        
        for (int i = 1; i<=right; i++) {
            list1[left+i] = peak1.getSlice().getIntensityList().get(peak1.getRt()+i);
            list2[left+i] = peak2.getSlice().getIntensityList().get(peak2.getRt()+i);
            
        }
        
        PearsonsCorrelation pear = new PearsonsCorrelation();
        double corr = pear.correlation(list1, list2);
        
        return corr;
    }
    
}
