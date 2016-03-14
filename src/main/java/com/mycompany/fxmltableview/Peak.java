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
 * a peak is a part of an EIC, that has been detected as a peak
 * 
 */
public class Peak {
    
    private int RT, RTstart, RTend;
    private double[] IntensityArray;
    private double[] RTArray;
    
    //constructor
    public Peak(int rt, int rtstart, int rtend, double[] intensityArray, double[] RTArray) {
       this.RT=rt;
       this.RTstart = rtstart;
       this.RTend = rtend;
       this.IntensityArray = intensityArray;
       this.RTArray = RTArray;
    }

   

    
}
