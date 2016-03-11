/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

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
    
    //analyses EIC, 
//    public getEICQuality () {
        
        
        
//    }
    
}
