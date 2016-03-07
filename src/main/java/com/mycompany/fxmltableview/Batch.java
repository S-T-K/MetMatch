/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

/**
 *
 * @author stefankoch
 * Holds information of a batch
 */
public class Batch extends Dataset{
    
    //number of batch in sessions listofbatches
    private int batchnumber;
    
    
    public Batch(int batchnumber) {
        this.batchnumber = batchnumber;
        
    }
    
    
}
