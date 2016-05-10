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
    private float scale;
    private float SNR;
    private float area;
    private Slice slice;
    private boolean manual;
    
    
    public Peak(short index, float scale, float SNR, float area, Slice slice) throws InterruptedException {
        this.index = index;
        this.scale = scale;
        this.SNR = SNR;
        this.area = area;
        this.slice = slice;
        this.manual = false;
        this.start =(short) (index-1.5*scale);
        if (start<0) {
            start = 0;
        }
        this.end = (short) (int) (index+1.5*scale);
        if (end >=slice.getIntArray().length) {
            end = (short) (slice.getIntArray().length-1);
        }
        //calculateArea();
        
    }

    public Peak(short index, short start, short end, Slice slice) throws InterruptedException {
        this.index = index;
        this.start = start;
        this.end = end;
        this.slice = slice;
        this.manual = false;
        trimPeak();
        calculateArea();
        
    }
    
    public Peak(boolean manual, short index, short start, short end, Slice slice, int non) throws InterruptedException {
        this.index = index;
        this.start = start;
        this.end = end;
        this.slice = slice;
        this.manual = manual;
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
    public void setIndex(short index) {
        this.index = index;
    }

    /**
     * @return the scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * @return the SNR
     */
    public float getSNR() {
        return SNR;
    }

    /**
     * @param SNR the SNR to set
     */
    public void setSNR(float SNR) {
        this.SNR = SNR;
    }

    /**
     * @return the area
     */
    public float getArea() {
        return area;
    }

    /**
     * @param area the area to set
     */
    public void setArea(float area) {
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
    
    public void trimPeak() throws InterruptedException {
        //max distance from middle to end in minutes
        //TODO: as parameter
        int maxdist = slice.getFile().getMaxPeakLengthInt();
        float[] intensity = slice.getIntArray();
        if (index-start>maxdist) {
            setStart((index-maxdist));
            
            //look for minima in EIC
            while (intensity[start+1]<intensity[start]&&start<index) {
                start++;
            }
            
        }
        if (end-index>maxdist) {
            setEnd((index+maxdist));
            
            //look for minima in EIC
            while (intensity[end-1]<intensity[end]&&end>index) {
                end--;
            }
        }
        
        float min = Float.MAX_VALUE;
        int mini= index;
        for (int i = index-1; i>start; i--) {
            if (intensity[i]<min) {
                min = intensity[i];
                mini = i;
            }
        }
        setStart(mini);
        
        min = Integer.MAX_VALUE;
        mini= index;
        for (int i = index+1; i<end; i++) {
            if (intensity[i]<min) {
                min = intensity[i];
                mini = i;
            }
        }
        setEnd( mini);
        
        
       
        
    }
    
    public void calculateArea() throws InterruptedException {
        area = 0;
        
        float[] intensity = slice.getIntArray();
       for (int i = start; i<=end; i++) {
           area+=intensity[i];
       }
        
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * @return the manual
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * @param manual the manual to set
     */
    public void setManual(boolean manual) {
        this.manual = manual;
    }
    
    //returns the RT value of the peak index
    public float getIndexRT() {
        return slice.getFile().getRTArray()[slice.getRTstart()+index];
    }
    
    //returns the RT shift relative to the Ogroup RT
    public float getIndexshift() {
        return getIndexRT()-slice.getAdduct().getOGroupObject().getRT();
    }
    
    
}
