/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

/**
 *
 * @author stefankoch
 * holds information of a peak
 * belongs to a slice, and therefore to a file
 */
public class Peak {
    
    private int rt, rtstart, rtend;
    private Slice slice;
    
    public Peak(int rt, int rtstart, int rtend, Slice slice) {
        this.rt = rt;
        this.rtstart = rtstart;
        this.rtend = rtend;
        this.slice = slice;
        
        
    }

    /**
     * @return the rt
     */
    public int getRt() {
        return rt;
    }

    /**
     * @param rt the rt to set
     */
    public void setRt(int rt) {
        this.rt = rt;
    }

    /**
     * @return the rtstart
     */
    public int getRtstart() {
        return rtstart;
    }

    /**
     * @param rtstart the rtstart to set
     */
    public void setRtstart(int rtstart) {
        this.rtstart = rtstart;
    }

    /**
     * @return the rtend
     */
    public int getRtend() {
        return rtend;
    }

    /**
     * @param rtend the rtend to set
     */
    public void setRtend(int rtend) {
        this.rtend = rtend;
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
