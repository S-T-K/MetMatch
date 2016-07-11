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
import java.util.TreeSet;

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
    
    Session session;
    
    public SNCalculator(Session session) {
        this.session=session;
    }
    
    
    public void calculatetrueSN(Slice slice) {
        if (slice.getListofPeaks()==null||slice.getListofPeaks().size()<1) {
            return;
        }
        
        float[] Int = slice.getIntArray();
        List<float[]> NInt = new ArrayList<>();
        
        int start = 0;
        //analyze region in front of every peak
        for (Peak peak:slice.getListofPeaks()) {
            if (start<peak.getStart()){
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
    //Noise factor calculation according to Stein
    public float calculateNoiseFactor(Slice[] listofSlices) {
        TreeSet<Float> Ns = new TreeSet<>();
        
        for (Slice slice:listofSlices) {
            float[] Int = slice.getIntArray();
            int start = 0; 
            int end = start+12;
            while (end<Int.length) {
                float avg = 0;
                boolean reject=false;
                for (int i=0; i<13; i++) {
                    float f = Int[start+i];
                    if (f<0.001) {
                        reject=true;
                        break;
                    }else{
                    avg+=Int[start+i]; }
                }
                avg/=13;
                //if no zero values
                if (!reject) {
                    TreeSet<Float> set = new TreeSet<>();
                    byte crossings = 0;
                    boolean lower;
                    float f = Int[start]-avg;
                    if (f>0) {
                        lower = false;
                        set.add(f);
                    } else {
                        lower = true;
                        set.add((f*-1));
                    }
                    
                    //count number of crossings
                    for (int i = 1; i<13; i++) {
                        f = Int[start+i]-avg;
                        if (f>0) {
                            if (lower) {
                                crossings++;
                            }
                            set.add(f);
                            lower = false;
                        } else {
                            if (!lower) {
                                crossings++;
                            }
                            set.add((f*-1));
                            lower = true;
                        }
                    }
                    
                    if (crossings>=7) {
                        //get median deviation
                        float med = (float)set.toArray()[7];
                        med/=Math.sqrt(avg);
                        Ns.add(med);
                    }
                    
                    
                    
                    
                    
                }
                
                
                start = end+1;
                end = start+12;
            }
            
            
        }
        //check if enough symbols have been found
        if (Ns.size()>10) {
        float N = (float) Ns.toArray()[Ns.size()/2];
        System.out.println(N);
        
        return N;
        } else {
            System.out.println("Warning: Noise Units could not be correctly calculated");
             for (Slice slice:listofSlices) {
            float[] Int = slice.getIntArray();
            int start = 0; 
            int end = start+12;
            while (end<Int.length) {
                float avg = 0;
                boolean reject=false;
                for (int i=0; i<13; i++) {
                    float f = Int[start+i];
                    if (f<0.001) {
                        reject=true;
                        break;
                    }else{
                    avg+=Int[start+i]; }
                }
                avg/=13;
                //if no zero values
                if (!reject) {
                    TreeSet<Float> set = new TreeSet<>();
                    byte crossings = 0;
                    boolean lower;
                    float f = Int[start]-avg;
                    if (f>0) {
                        lower = false;
                        set.add(f);
                    } else {
                        lower = true;
                        set.add((f*-1));
                    }
                    
                    //count number of crossings
                    for (int i = 1; i<13; i++) {
                        f = Int[start+i]-avg;
                        if (f>0) {
                            if (lower) {
                                crossings++;
                            }
                            set.add(f);
                            lower = false;
                        } else {
                            if (!lower) {
                                crossings++;
                            }
                            set.add((f*-1));
                            lower = true;
                        }
                    }
                    
                    if (crossings>=2) {
                        //get median deviation
                        float med = (float)set.toArray()[7];
                        med/=Math.sqrt(avg);
                        Ns.add(med);
                    }
                    
                    
                    
                    
                    
                }
                
                
                start = end+1;
                end = start+12;
            }
            
            
        }
             float N = (float) Ns.toArray()[Ns.size()/2];
        System.out.println(N);
        
        return N;
        }
        
    }
    
    //calculates Noise Units for each peak, and deletes peaks with NU lower than threshold
    public void calculateNoiseUnits(Slice slice) {
        if (slice.getListofPeaks()==null||slice.getListofPeaks().size()<1) {
            return;
        }
        
//        if (slice.getAdduct().getNum()==8957) {
//            System.out.println();
//        }
        float Noisefactor = slice.getFile().getNoiseFactor();
        float[] Int = slice.getIntArray();
        
        
      List<Peak> goodpeaks = new ArrayList<Peak>();
        
        for (Peak peak:slice.getListofPeaks()) {
            //estimate height of peak above its surrounding signals
            //takes the min of the 3 closest signals to the peak borders, including the peak borders themselves to ensure we have at least one value
            float height;
            
            int start = peak.getStart();
            int end = peak.getEnd();
            
            float sheight=Int[start];
            float eheight =Int[end];
            
            for (int i = 1; i<=3; i++) {
                if (start-i>0) {
                    sheight=Math.min(sheight, Int[start-i]);
                }
                if (end+i<Int.length) {
                   eheight=Math.min(eheight, Int[end+i]);
                }
            }
            
            height=(sheight+eheight)/2;
            
            //calculate Noise Unit for peak
            float NU = (float) Math.sqrt(Int[peak.getIndex()])*Noisefactor;
            
            //calculate height above surrounding signals
            height=Int[peak.getIndex()]-height;
            
            //calculate how many Noise Units the peak rises above the surrounding signals
            peak.setNoiseUnits(height/NU);
            
            if (height/NU>=session.getNoisethreshold().get()) {
                //System.out.println(slice.getAdduct().getOGroup() + ": " + slice.getAdduct().getNum() + ": Noisy");
                goodpeaks.add(peak);
            }
            
            
            
        }
        slice.setListofPeaks(goodpeaks);
        
    }
    
    
}
