/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.util.Arrays;

/**
 *
 * @author stefankoch
 * holds information of a peak
 * a peak is a part of an EIC, that has been detected as a peak
 * 
 */
public class Peak {
    
    private int RT, RTstart, RTend;
    private double[] IntensityArray;
    private double[] RTArray;
    private double maxIntensity;
    private double quality;
            
          
         
   
    
    //constructor
    public Peak(int rt, int rtstart, int rtend, double[] intensityArray, double[] RTArray) {
       this.RT=rt;
       this.RTstart = rtstart;
       this.RTend = rtend;
       this.IntensityArray = Arrays.copyOfRange(intensityArray, RTstart, RTend+1);
       this.RTArray = Arrays.copyOfRange(RTArray, RTstart, RTend+1);
       
    }

   
    public void normalize() {
     double[] MaxArray  = Arrays.copyOf(getIntensityArray(), getIntensityArray().length);
     Arrays.sort(MaxArray);
        setMaxIntensity(MaxArray[getIntensityArray().length - 1]);
     
     for (int i = 0; i< getIntensityArray().length; i++) {
            getIntensityArray()[i] = getIntensityArray()[i]/getMaxIntensity();
         
     }
   
    }

    public void denormalize() {
        for (int i = 0; i< getIntensityArray().length; i++) {
            getIntensityArray()[i] = getIntensityArray()[i]*getMaxIntensity();
         
     }
        
    }
    /**
     * @return the RT
     */
    public int getRT() {
        return RT;
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(int RT) {
        this.RT = RT;
    }

    /**
     * @return the RTstart
     */
    public int getRTstart() {
        return RTstart;
    }

    /**
     * @param RTstart the RTstart to set
     */
    public void setRTstart(int RTstart) {
        this.RTstart = RTstart;
    }

    /**
     * @return the RTend
     */
    public int getRTend() {
        return RTend;
    }

    /**
     * @param RTend the RTend to set
     */
    public void setRTend(int RTend) {
        this.RTend = RTend;
    }

    /**
     * @return the IntensityArray
     */
    public double[] getIntensityArray() {
        return IntensityArray;
    }

    /**
     * @param IntensityArray the IntensityArray to set
     */
    public void setIntensityArray(double[] IntensityArray) {
        this.IntensityArray = IntensityArray;
    }

    /**
     * @return the RTArray
     */
    public double[] getRTArray() {
        return RTArray;
    }

    /**
     * @param RTArray the RTArray to set
     */
    public void setRTArray(double[] RTArray) {
        this.RTArray = RTArray;
    }

    /**
     * @return the maxIntensity
     */
    public double getMaxIntensity() {
        return maxIntensity;
    }

    /**
     * @param maxIntensity the maxIntensity to set
     */
    public void setMaxIntensity(double maxIntensity) {
        this.maxIntensity = maxIntensity;
    }

    /**
     * @return the quality
     */
    public double getQuality() {
        return quality;
    }

    /**
     * @param quality the quality to set
     */
    public void setQuality(double quality) {
        this.quality = quality;
    }

    
}
