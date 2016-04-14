/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

/**
 *
 * @author stefankoch
 * 
 * hold information of a peak detected by MassSpecWavelet
 */
public class Peak {
    private int index;
    private int start;
    private int end;
    private double scale;
    private double SNR;
    private double area;
    private Slice slice;
    
    
    public Peak(int index, double scale, double SNR, double area, Slice slice) {
        this.index = index;
        this.scale = scale;
        this.SNR = SNR;
        this.area = area;
        this.slice = slice;
        
    }

    public Peak(int index, int start, int end, Slice slice) {
        this.index = index;
        this.start = start;
        this.end = end;
        this.slice = slice;
        trimPeak();
        calculateArea();
        
    }
    
    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @return the SNR
     */
    public double getSNR() {
        return SNR;
    }

    /**
     * @param SNR the SNR to set
     */
    public void setSNR(double SNR) {
        this.SNR = SNR;
    }

    /**
     * @return the area
     */
    public double getArea() {
        return area;
    }

    /**
     * @param area the area to set
     */
    public void setArea(double area) {
        this.area = area;
    }

    /**
     * @return the slice
     */
    public Slice getSlice() {
        return slice;
    }

    /**
     * @param slice the slice to set
     */
    public void setSlice(Slice slice) {
        this.slice = slice;
    }
    
    public void trimPeak() {
        //max distance from middle to end in minutes
        //TODO: as parameter
        int maxdist = 15;
        if (index-start>15) {
            start = index-15;
        }
        if (end-index>15) {
            end = index+15;
        }
        
    }
    
    public void calculateArea() {
        area = 0;
        //TODO: Unit of a time step? Area?
        int unitofstep = 1;
        
       for (int i = start; i<end; i++) {
           area+=(slice.getIntensityArray()[i]+ slice.getIntensityArray()[i+1])*unitofstep/2;
       }
        
    }
    
}
