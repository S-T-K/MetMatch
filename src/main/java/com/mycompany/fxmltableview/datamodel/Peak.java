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
    
    
}
