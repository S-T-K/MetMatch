/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import flanagan.analysis.CurveSmooth;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author stefankoch
 */
public class EICComparer {
    
    
    public EICComparer() {
        
        
    }
    
    public double compare(Entry reference, Slice batch) {
        
        
        
        
        
        PearsonsCorrelation pear = new PearsonsCorrelation();
        double corr = pear.correlation(reference.getIntensityArray(), batch.getIntensityArray());
        
        return corr;
    }
    
    //analyses EIC, a perfect peak has quality 1, no detectable peak has quality 0
    //a perfect peak is high, and falls off steeply to both sides, until it reaches the baseline
    public double getEICQuality (Slice slice) {
        double quality = 0;
        
        int resolution = slice.getIntensityArray().length;
        int middle = resolution/2;
        double[] smooth = slice.getIntensityArray().clone();
        
        
        //CurveSmooth csm = new CurveSmooth(smooth);
       //smooth = csm.savitzkyGolay(25);
        
        
        
        
        smooth = movingAverageSmooth(smooth);
        
        
        
        //look for highest point in the middle +-10%
        double max = smooth[middle];
        int peakint = middle;
        for (int i = 0; i < resolution/10; i++) {
            //look left and right
            if (max < smooth[middle+i]) {
                max = smooth[middle+i];
                peakint = middle+i;
            }
            if (max < smooth[middle-i]) {
                max = smooth[middle-i];
                peakint = middle-i;
            }
        }
  
        //if peak found, check the range of the peak
        int end = peakint;
        int start = peakint;
        
            //look right while the slope is steep enough
            double difend=0;
            while (end < resolution - 2 && (smooth[end]-smooth[end+1]>=difend)) {
                difend = (smooth[end]-smooth[end+1])*0.8;
                if (difend < 0) {
                    difend = 0;                   
                }
                end = end + 1;
            }
            
            //look left while the slope is steep enough
            double difstart = 0;
            while (start > 1 && (smooth[start]-smooth[start-1]>=difstart)) {
                difstart = (smooth[start]-smooth[start-1])*0.8;
                if (difstart < 0) {
                    difstart = 0;                   
                }
                start = start - 1;

            }

            
            //calculate quality
            //heigth quality
            double height = 0;
            if (slice.getIntensityArray()[peakint] > 500000) {
                height = 1;
            } else if (slice.getIntensityArray()[peakint] < 5000) {
                height = 0;
            } else {
                height = Math.log10(slice.getIntensityArray()[peakint]) / Math.log10(500000);
            }

            //width quality
            double width = 0;
            if (end - start < 5 || end-peakint<2 || peakint-start<2) {
                width = 0;
            } else if (slice.getRTArray()[end] - slice.getRTArray()[start] < 0.6) {
                width = 1;
            } else {
                width = 0.6/(slice.getRTArray()[end] - slice.getRTArray()[start]);
            }
            
            //heigth above baseline
            double heightabove = 0;
            double bheigth = (slice.getIntensityArray()[start]+slice.getIntensityArray()[end])/2;
            if (bheigth/slice.getIntensityArray()[peakint]<=0.2) {
                heightabove = 1;
            } else { 
                heightabove = 1- (bheigth/slice.getIntensityArray()[peakint]);
            }
                
                
                quality = height*width*heightabove;
                
                
                
                //Test: Delete everything but peak
                for (int i = 0; i< start; i++) {
                    slice.getIntensityArray()[i] = -1.0;
                }
                
                for (int i = end; i< resolution; i++) {
                    slice.getIntensityArray()[i] = -1.0;
                }
                
                
                return quality; 
        }
    
    
    public double[] movingAverageSmooth(double[] smooth) {
        int resolution = smooth.length;
        double[] construction = new double[resolution];
        
        //smoothing
        //we want 5% windows around each point, minimum 1 point
        int range = (int) Math.ceil(resolution/40);
        
       System.out.println("Range: " + (range*2+1));
        
        for (int i = 0; i< 3; i++) {    //iterations
            for (int j = range; j<(resolution-range-1); j++) {
                construction[j]=0;
                for (int k = j-range; k<j+range; k++) {
                    construction[j]+=smooth[k];           
                }
                construction[j] =  construction[j]/(range*2+1);
            }
            smooth = construction.clone();
           
        }
        
        return smooth;
    }
    
    
    }






