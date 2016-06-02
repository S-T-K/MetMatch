/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import com.mycompany.fxmltableview.datamodel.Peak;
import com.mycompany.fxmltableview.datamodel.Slice;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefankoch
 * Calculates Signal/Noise ratio
 * Detects regions that could potentially be signals and distort the noise according to Stein (An Integrated Method for Spectrum Extraction
and Compound Identification from Gas
Chromatography/Mass Spectrometry Data) 1999
 * 
 * 
 * 
 */
public class SNCalculator {
    
    public SNCalculator() {
        
    }
    
    
    public void calculate(Slice slice) {
        if (slice.getListofPeaks()==null||slice.getListofPeaks().size()<1) {
            return;
        }
        
        float[] Int = slice.getIntArray();
        List<float[]> NInt = new ArrayList<>();
        
        int start = 0;
        //analyze region in front of every peak
        for (Peak peak:slice.getListofPeaks()) {
            float[] segment = new float[peak.getStart()-start];
            System.arraycopy(Int, start, segment, 0, peak.getStart()-start);
                int s = 0;
                int e = start+12;
                while (e<segment.length) {
                    //do calculation
                    float[] sub = new float[13];
                    System.arraycopy(segment, s, sub, 0, 13);
                    
                    //get avg
                    float avg=0;
                    for (float f:sub) {
                        avg+=f;
                    }
                    avg/=13;
                    
                    byte crossings = 0;
                    boolean lower;
                    
                    if (sub[0]<avg) {
                        lower = true;
                    } else {
                        lower = false;
                    }
                    
                    for(float f:sub) {
                        if (f<avg) {
                            if (!lower) {
                                crossings++;
                            }
                            lower=true;
                        } else {
                            if (lower) {
                                crossings++;
                            }
                            lower = false;
                        }
                    }
                    
                    if (crossings>=7) {
                        NInt.add(sub);
                    }
                    s=e+1;
                    e=s+12;
                }
                start = peak.getEnd()+1;
        }
        
        //analyze the remaining rest
        float[] lsegment = new float[Int.length-start];
        System.arraycopy(Int, start, lsegment, 0, Int.length-start);
        int s = 0;
        int e = start+12;
        while (e<lsegment.length) {
                    //do calculation
                    float[] sub = new float[13];
                    System.arraycopy(lsegment, s, sub, 0, 13);
                    //get avg
                    float avg=0;
                    for (float f:sub) {
                        avg+=f;
                    }
                    avg/=13;
                    
                    byte crossings = 0;
                    boolean lower;
                    
                    if (sub[0]<avg) {
                        lower = true;
                    } else {
                        lower = false;
                    }
                    
                    for(float f:sub) {
                        if (f<avg) {
                            if (!lower) {
                                crossings++;
                            }
                            lower=true;
                        } else {
                            if (lower) {
                                crossings++;
                            }
                            lower = false;
                        }
                    }
                    
                    if (crossings>=7) {
                        NInt.add(sub);
                    }
                    s=e+1;
                    e=s+12;
                }
        
        //calculate Noise Range and avg
        
        float max=Float.MIN_VALUE;
        float min=Float.MAX_VALUE;
        float sum=0;
        int count=0;
        
        for (float[] segment : NInt) {
            for (float f:segment) {
                if (f>max) {
                    max = f;
                }
                if (f<min) {
                    min = f;
                }
                sum+=f;
                count++;
            }
        }
        
        float avg = sum/count;
        float range = max-min;
        
        //calculate S/N ratio for each peak
        for (Peak peak:slice.getListofPeaks()) {
            peak.setSNR((Int[peak.getIndex()]-avg)/range);
            
        }
        
        
    }
    
    
}
