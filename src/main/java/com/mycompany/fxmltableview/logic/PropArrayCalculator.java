/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

/**
 *
 * @author stefankoch
 */
public class PropArrayCalculator {
    
    private Session session;
    private int middle;
    //number of minutes from one step to the next
    private float step;
    
    
    
    public PropArrayCalculator(Session session) {
        middle = (session.getResolution()/2)-1;
        step = (float) (session.getRTTolerance()*2*0.9/session.getResolution());
    }
    
    //gets RT of peak
    public float[][] calculate(float peak, float ogroupRT, float[][] matrix, int row) {
        //calculate the index
        int index = middle+(int) ((peak-ogroupRT)/step);
        int tol = session.getIntPeakRTTol();
        
        if (index>0&&index<session.getResolution()) {
            matrix[row][index] = 1.0f;
        }
        
        for (int k = 1; k<=tol; k++) {
                //calculate the value
                float value = 1*((float)tol-k)/(float)tol;
                
                //check for borders and insert new value of old value is smaller
                if((index-k)>0&&matrix[row][index-k]<value) {
                    matrix[row][index-k]=value;
                }
                if ((index+k)<session.getResolution()&&matrix[row][index+k]<value) {
                    matrix[row][index+k]=value;
                }
        
    }
    
    
}
