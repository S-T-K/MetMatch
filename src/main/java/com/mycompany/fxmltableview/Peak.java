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
    
    private float mz, rt, area, rtstart, rtend;
    private Slice slice;
    
    public Peak(float mz, float rt, float rtstart, float rtend, Slice slice) {
        this.mz = mz;
        this.rt = rt;
        this.rtstart = rtstart;
        this.rtend = rtend;
        this.slice = slice;
        
        
    }
    
}
