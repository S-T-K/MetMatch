/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;


import flanagan.analysis.CurveSmooth;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;
import java.util.Arrays;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.rosuda.JRI.Rengine;
import java.io.FileOutputStream;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;

import java.io.FileReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;


import static java.lang.Math.abs;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumSet;
import javafx.scene.chart.XYChart;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;

/**
 *
 * @author stefankoch
 */
public class Slice {
    
    //flag
    private boolean rw;
    private boolean stored;
    private boolean written;
   
    private RawDataFile file;
    //private String name;
    
    //TODO: one RTList/OGroup or calculation every time
    //TODO: maybe less precision for Intensity?
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
    private PolynomialSplineFunction intensityFunction;
    private float minIntensity;
    private float maxIntensity;
    private List<Peak> listofPeaks;
    private Short fittedpeak;
    
    private float scorepeakclose = 1;
   
    private Entry adduct;
    private Dataset dataset;
    
    //processed information
    private float[] MZArray;
    private byte[] byteMZArray;
    private int[] IntensityArray;
    private double[] PropArray;
    
    private float[] Bins;
    
    private boolean empty;
   
    public Slice(RawDataFile file, Entry adduct) {
        this.file = file;
        this.dataset = file.getDataset();
        this.adduct = adduct;
        //this.name = (this+"intList.ser");
        this.fittedpeak = null;
        rw=false;
        stored=false;
    }
    

    
    public void extractSlicefromScans(List<Scan> listofScans) {
        float start = System.currentTimeMillis();
        
         //for all Scans
         setMinIntensity(900000000);
         setMaxIntensity(0);
         boolean found;
         
         
        for (int i = 0; i< listofScans.size(); i++) {
            //if RT is within tolerance
            
            
            float currentRT = listofScans.get(i).getRetentionTime();
            
            //0.05 so that ranges for the interpolation are smaller than the actual ranges, otherwise out of range
           
        if (currentRT>= (getMinRT()) && currentRT<= (getMaxRT())) {
            
                        found = false;
                        
                        
                        //TODO binary search!!!!!!
                        for (int l=0; l<listofScans.get(i).getPeakscount(); l++) {
                           float mz = listofScans.get(i).getMassovercharge()[l];
                            if (mz >= getMinMZ() && mz <= getMaxMZ()) {
                                getRetentionTimeList().add(currentRT);
                                getIntensityList().add(listofScans.get(i).getIntensity()[l]);
                                getMassList().add(mz);
                                //add mass to bins for "median" calculation
                                addmasstoBin(mz);
                                
                                if (listofScans.get(i).getIntensity()[l]>getMaxIntensity()) {
                                    setMaxIntensity(listofScans.get(i).getIntensity()[l]);
                                }
                                if (listofScans.get(i).getIntensity()[l] < getMinIntensity()) {
                                    setMinIntensity(listofScans.get(i).getIntensity()[l]);
                                }
                                found = true;
                                
                                
                                
                            }
                            
                        }
                        if (!found) {
                            getRetentionTimeList().add(currentRT);
                            getIntensityList().add(0.0f);
                            getMassList().add(0.0f);
                            
                        }
                    }
        
        }
      
     //this.clean();
     //this.generateInterpolatedEIC();
    
     float end = System.currentTimeMillis();
     //System.out.println("Extraction: " + (end-start));
    }
    
    //not yet working...
    public void binaryExtractSlicefromScans(List<Scan> listofScans) {
        
       generateBins();
         //for all Scans
         setMinIntensity(900000000);
         setMaxIntensity(0);
       float minMZ = getMinMZ();
       float maxMZ = getMaxMZ();
       float minRT = getMinRT();
       float maxRT = getMaxRT();
       int numberofsignals=0;
         
         //for all Scans
         int start = 0;
         int end = listofScans.size()-1;
         int middle = end/2;
         boolean foundRT = false;
         int lower = start;
         
         while (!foundRT && (end-start)>0) {
             if (listofScans.get(middle).getRetentionTime() < getMinRT()) {
                 start = middle +1;
                 lower = middle;
             } else if (listofScans.get(middle).getRetentionTime() > getMaxRT()) {
                 end = middle-1;
             } else {
                 foundRT = true;
             }
             middle = start + (end - start)/2;
         }
         
         
         if (foundRT) {
                end = middle;
                middle = (lower+end)/2;
             boolean foundMinRT = false;
             while (!foundMinRT) {
             if (listofScans.get(middle).getRetentionTime() < getMinRT()) {
                 lower = middle+1;
             } else if (listofScans.get(middle-1).getRetentionTime() < getMinRT()) {
                 foundMinRT = true;
             } else {
                 end = middle-1;
             }
             middle = lower + (end - lower)/2;
             if(middle==0) {
                     foundMinRT = true;
                 }
         }
             //System.out.println("RT search done");
             //middle is the lowest RT
             int current = middle;
             Scan currentScan = listofScans.get(middle);
             while (currentScan.getRetentionTime() < getMaxRT()) {
                 boolean foundMZ = false;
                 start = 0;
                 end = currentScan.getMassovercharge().length-1;
                 
                 while (!foundMZ && start<=end) {
                     middle = start + (end - start)/2;
                     if (currentScan.getMassovercharge()[middle] < getMinMZ()) {
                         start = middle + 1;
                         //System.out.println("MZ too low");
                         
                     } else if (currentScan.getMassovercharge()[middle] > getMaxMZ()) {
                         end = middle - 1;
                         //System.out.println("MZ too high");
                         
                     } else {
                         foundMZ = true;
                        // System.out.println("MZ found");
                         middle = start + (end - start)/2;
                         break;
                     }
                     
                 }
                 //System.out.println("MZ search done");
                 
                 //if MZ found
                 if (foundMZ) {
                     
                     numberofsignals++;
                     start = middle;
                     end = middle+1;
                     
                     while (start>0 &&currentScan.getMassovercharge()[start]>=getMinMZ()) {
                     getRetentionTimeList().add(currentScan.getRetentionTime());
                                getIntensityList().add(currentScan.getIntensity()[start]);
                                getMassList().add(currentScan.getMassovercharge()[start]);
                                //add mass to bins for "median" calculation
                                addmasstoBin(currentScan.getMassovercharge()[start]);  
                                if (currentScan.getIntensity()[start]>getMaxIntensity()) {
                                    setMaxIntensity(currentScan.getIntensity()[start]);
                                }
                                if (currentScan.getIntensity()[start] < getMinIntensity()) {
                                    setMinIntensity(currentScan.getIntensity()[start]);
                                }
                                start--;
                                //System.out.println("lower MZ found");
                 }
                     while (end<currentScan.getMassovercharge().length && currentScan.getMassovercharge()[end]<=getMaxMZ()) {
                     getRetentionTimeList().add(currentScan.getRetentionTime());
                                getIntensityList().add(currentScan.getIntensity()[end]);
                                getMassList().add(currentScan.getMassovercharge()[end]);
                                //add mass to bins for "median" calculation
                                addmasstoBin(currentScan.getMassovercharge()[end]);
                                if (currentScan.getIntensity()[end]>getMaxIntensity()) {
                                    setMaxIntensity(currentScan.getIntensity()[end]);
                                }
                                if (currentScan.getIntensity()[end] < getMinIntensity()) {
                                    setMinIntensity(currentScan.getIntensity()[end]);
                                }
                                end++;
                                //System.out.println("upper MZ found");
                 }
                 } else {
                 getRetentionTimeList().add(currentScan.getRetentionTime());
                            getIntensityList().add(0.0f);
                            getMassList().add(0.0f);
                            //System.out.println("MZ not found");
             }

                 current++;
                 if (current == listofScans.size()){
                 break;
                 }
                 currentScan = listofScans.get(current);
                  //System.out.println("next Scan");
             }


             
         }
         
    
       Bins=null;
      
     //this.clean();
     //this.generateInterpolatedEIC();
     
     if (numberofsignals<5) {
         this.empty = true;
     } else {
         this.empty = false;
     }
     
     
    }
    
    
    //interpolates intensities
    public  void generateIntensityFunction() {
       double[] RT = new double[this.retentionTimeList.size()];
       double[] Intensity = new double[this.retentionTimeList.size()];
       for (int i =0; i<RT.length; i++) {
           RT[i] = this.retentionTimeList.get(i);
           Intensity[i] = this.intensityList.get(i);
       }
        
        
        
       LinearInterpolator interpolator = new LinearInterpolator();
       
       this.intensityFunction = interpolator.interpolate(RT, Intensity);
    }
    
    
//generates Array filled with "probabilities", correspond to wavelet peaks 
//caluclated with R MassSpecWavelet
    public void WaveletPeakPicking() throws InterruptedException {
        float startc = System.currentTimeMillis();
        if (PropArray == null) {
            PropArray = (new double[this.getIntensityArray().length]);
            deleteAutoPeaks();
        
            //baseline correct IntensityArray
            float[] correctedIntArray = new float[getIntensityArray().length];
            for ( int j = 0; j<getIntensityArray().length; j++)  {
                if (getIntensityArray()[j]>=adduct.getSession().getBaseline()) {
                    correctedIntArray[j]=getIntensityArray()[j]-adduct.getSession().getBaseline();
                }
                
            }

        
        float start1 = System.currentTimeMillis();
        // Create an R vector in the form of a string.
        String EIC = Arrays.toString(correctedIntArray);
        EIC = EIC.substring(1, EIC.length()-1);
        //100 zeros at start and end, 50 are not enough
        EIC = "c(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,".concat(EIC);
        EIC = EIC.concat(",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");
        //System.out.println("EIC String processing: " + (System.currentTimeMillis()-start1));
       
        
        // Start Rengine.
        Rengine engine = adduct.getSession().getEngine();

        // The vector that was created in JAVA context is stored in 'rVector' which is a variable in R context.
        //=INPUTS
        engine.eval("eic=" + EIC);
       
        
        
        //Retrieve values, see script for names
        //=OUTPUTS
        float start3 = System.currentTimeMillis();
        double[][] ret = engine.eval("getMajorPeaks(eic, scales=c(5, 12), snrTh=3)").asDoubleMatrix();
        //System.out.println("Wavelet calculation: " + (System.currentTimeMillis()-start3));
        
        //Print output values, work with them...
        if (ret!=null) {
            float start4 = System.currentTimeMillis();
        
            for (int j = 0; j<ret[0].length; j++) {
                //101 because of 100 zeros at start and R starts at 1
                if (((int)ret[0][j]-101)<100) {
                PropArray[(int)ret[0][j]-101]=1;
                addPeak(new Peak((short) ((short)ret[0][j]-101), (float)ret[1][j], (float)ret[2][j], (float)ret[3][j], this));
            }}
            
        
        //System.out.println("PropArray processing: " + (System.currentTimeMillis()-start4));
        }
        //end Rengine, otherwise thread doesn't terminate
        //engine.end();
         EIC=null;
}
        PropArray = null;
       
        System.out.println("Complete processing: " + (System.currentTimeMillis()-startc));
    }
    
    
    
    //generates Array filled with probabilities, correspond to the probabiltiy of a guassian peak at this RT
 public void NaivePeakPicking() throws InterruptedException {
     float startc = System.currentTimeMillis();
         //initialize Array holding probabilities
        if (PropArray==null){ 
        PropArray =(new double[this.getIntensityArray().length]);
        
        addGaussCorrelation(0.6f);
        addGaussCorrelation(0.5f);
        addGaussCorrelation(0.4f);
        
        }
        generatePeakArray();
        PropArray = null;
        //System.out.println("Complete processing: " + (System.currentTimeMillis()-startc));
    }
 
 //adds correlation to PropArray calulated for a gaussian of length "length" (in minutes) from -2 to +2 std
 public void addGaussCorrelation(float length) throws InterruptedException {
     
      float valuesperminute = adduct.getSession().getResolution()/(adduct.getSession().getRTTolerance()*2);
      int arraylength = (int) (valuesperminute*length);
        if (arraylength%2==0) {
                   arraylength++;
        }
        
        double[] peakArray = new double[arraylength];
        int peakint = Math.floorDiv(arraylength, 2);
        NormalDistribution normdist = new NormalDistribution();
        //edge of peak is at X std
        float peakedge = 2;
        float increment = peakedge/(peakint-1);
        for (int i = 0; i<=peakint; i++) {
            peakArray[i]= normdist.density(peakedge-i*increment);
            peakArray[(arraylength-1)-i]=peakArray[i];
        }
                
         PearsonsCorrelation pear = new PearsonsCorrelation();

         
         //baseline correct IntensityArray
            double[] correctedIntArray = new double[getIntensityArray().length];
            for ( int j = 0; j<getIntensityArray().length; j++)  {
                if (getIntensityArray()[j]>=adduct.getSession().getBaseline()) {
                    correctedIntArray[j]=getIntensityArray()[j]-adduct.getSession().getBaseline();
                }
                
            }
            
        for (int i = 0; i< (getIntensityArray().length-peakArray.length); i++) {
            
        double corr = pear.correlation(peakArray ,Arrays.copyOfRange(correctedIntArray, i, i+peakArray.length));
        
                //scale according to maxIntensity
               //and weaken weak signals
               if (corr > 0) {
                   double newcorr = (corr*corr)*asymptoticFunction(getIntensityArray()[i+peakint]-minIntensity);
                   if ((PropArray[i+peakint]<newcorr)) {
                       PropArray[i+peakint]= (float) newcorr;}
               }
        
        }
     
 }
 
 
 //returns value between 0 and 1, rapidly falling for values lower than the baseline
 public float asymptoticFunction(float intensity) {
     float baseline = adduct.getSession().getBaseline();
     
     return (intensity-baseline/(baseline*10))/(1+intensity-baseline/(baseline*10));
 }
 
 //generates Array filled with Peak probabilites
 //TODO: negative Maxima
 public void generatePeakArray() throws InterruptedException {
//     for (int i = 0; i<PropArray.length; i++) {
//         if (PropArray[i]<0.2) {
//             PropArray[i] = 0;
//         }
//         
//     }


     CurveSmooth csm = new CurveSmooth(PropArray);
     PropArray = csm.movingAverage(3);
     PropArray = csm.movingAverage(3);
     PropArray = csm.movingAverage(3);
     double[][] maxima = csm.getMaximaMovingAverage();
     double[][] minima = csm.getMinimaMovingAverage();
     
     for (int i = 0; i<maxima[0].length; i++) {
         if (PropArray[(int)maxima[0][i]]<0.2) {
             maxima[0][i] = -1;
         }
     }
     
     
     for (int i = 0; i<PropArray.length; i++ ) {
         PropArray[i] = 0;
     }
     
     for (int i = 0; i<maxima[0].length; i++) {
         if (maxima[0][i]>0) {
         PropArray[(int)maxima[0][i]] = 1;
         }
     }
     
     for (int i = 0; i<minima[0].length; i++) {
         PropArray[(int)minima[0][i]] = 0.3;
         
     }
     
     //now we have an Array with Marks at Max and Min
     deleteAutoPeaks();
     short start = 0; 
    short index = 0;
    short end = 0;
     for (short i = 0; i< PropArray.length; i++) {
         //search for non 0 entry
        while (PropArray[i]==0) {
            i++;
            if (i == (PropArray.length-1)) {
                break;
            }
        }
        //if Min, mark start
        if (PropArray[i]==0.3) {
            PropArray[i]=0;
            start = i;
            //if Max, mark index and look for end
        } else if (PropArray[i]==1) {
            index = i;
            i++;
            //if end of Array
            if (i==(PropArray.length-1)) {
            end = i;
            addPeak(new Peak(index,start,end, this));
            }
            //while not at end
            while (i<PropArray.length-1 && PropArray[i]==0) {
                i++;
                //if end of Array
                if (i==(PropArray.length-1)) {
            end = i;
            addPeak(new Peak(index,start,end, this)); 
            }
            }
            //if end found, make peak
            if (PropArray[i]==0.3) {
            PropArray[i]=0;
            end = i;
            addPeak(new Peak(index,start,end, this));
            //if another max, split
            } else if (PropArray[i]==1) {
                end = (short) ((index+i)/2);
                addPeak(new Peak(index,start,end, this));
                start = end;
                i--;
            } else if (PropArray[i]==0){
                //do nothing
            } else {
                System.out.println("Unexpected value in PropArray");
            }
            
        } else if (PropArray[i]==0){
            //do nothing
        } else {
            System.out.println("Unexpected value in PropArray");
        }
        
      
        
         
     }
     
     
     
     
     //delete array except for region around maxima
//     int current = 0;
//     for (int i = 0; i<maxima[0].length; i++) {
//         while (current < PropArray.length && current<maxima[0][i]) {
//             PropArray[current] = 0;
//             current++;
//         }
//         current = current+2;
//     }
//     while (current<PropArray.length) {
//         PropArray[current] = 0;
//             current++;
//     }
 }

    
    
    
    
    /**
     * @return the retentionTimeList
     */
    public List<Float> getRetentionTimeList() {
        return retentionTimeList;
    }

    /**
     * @return the intensityList
     */
    public List<Float> getIntensityList() {
        return intensityList;
    }

    /**
     * @return the massList
     */
    public List<Float> getMassList() {
        return massList;
    }

    /**
     * @return the minRT
     */
    public float getMinRT() {
        return adduct.getMinRT();
    }

    /**
     * @return the maxRT
     */
    public float getMaxRT() {
        return adduct.getMaxRT();
    }

    /**
     * @return the minMZ
     */
    public float getMinMZ() {
        return adduct.getMinMZ();
    }

    /**
     * @return the maxMZ
     */
    public float getMaxMZ() {
        return adduct.getMaxMZ();
    }

    /**
     * @return the file
     */
    public RawDataFile getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(RawDataFile file) {
        this.file = file;
    }

    

    

    
    //removes duplicate RT entries, only takes the max intensity
    //within tolerance range around mzshift
    public void clean() {
        
    List<Float> newRTList = new ArrayList<>();
    List<Float> newIntList= new ArrayList<>();
    List<Float> newMZList= new ArrayList<>();
    
    float shiftedMZ = adduct.getMZ()-adduct.getMZ()/1000000*file.getMzshift();
    float maxMZ = shiftedMZ+shiftedMZ/1000000*adduct.getSession().getSliceMZTolerance();
    float minMZ = shiftedMZ-shiftedMZ/1000000*adduct.getSession().getSliceMZTolerance();
    
    int numberofsignals = 0;
    
    for (int i =0; i<massList.size(); i++) {
        
       float intensity = 0;
       
       float mz =0;
       if (massList.get(i)<=maxMZ&&massList.get(i)>=minMZ){
           numberofsignals++;
       mz = massList.get(i); 
       intensity = intensityList.get(i);}
       while (i<retentionTimeList.size()-1 && abs(retentionTimeList.get(i)-retentionTimeList.get(i+1))<0.001) {
           if (massList.get(i+1)<=maxMZ&&massList.get(i+1)>=minMZ){
           if (intensityList.get(i+1)> intensity) {
               
               intensity = intensityList.get(i+1);
               mz = massList.get(i+1);
           }
           }
           i++; 
       
       }
       
       newRTList.add(retentionTimeList.get(i));
       newIntList.add(intensity);
       newMZList.add(mz);
        
        
        
        
    }
       
        
        setRetentionTimeList(newRTList);
        setIntensityList(newIntList);
        setMassList(newMZList);
        
    if (numberofsignals<5||maxIntensity<adduct.getSession().getBaseline()) {
        this.empty=true;
    }
}

    
    
    public List<Float> smooth(int iterations) {
        List<Float> newIntList= new ArrayList<>(intensityList);
        for (int i= 0; i<iterations; i++) {
      
            for (int j = 1; j< (retentionTimeList.size()-1); j++) {
                if (newIntList.get(j)>100) {
                newIntList.set(j, (newIntList.get(j-1)+ newIntList.get(j) + newIntList.get(j+1))/3);
                
            }
            }
        
        }
    return newIntList;
}

    /**
     * @param intensityList the intensityList to set
     */
    public void setIntensityList(List<Float> intensityList) {
        this.intensityList = intensityList;
    }

   
    /**
     * @return the Num
     */
    public int getNum() {
        return adduct.getNum();
    }

 

    /**
     * @return the RT
     */
    public float getRT() {
        return adduct.getRT();
    }

 

    /**
     * @param retentionTimeList the retentionTimeList to set
     */
    public void setRetentionTimeList(List<Float> retentionTimeList) {
        this.retentionTimeList = retentionTimeList;
    }

    /**
     * @param massList the massList to set
     */
    public void setMassList(List<Float> massList) {
        this.massList = massList;
    }

    /**
     * @return the intensityFunction
     */
    public PolynomialSplineFunction getIntensityFunction() {
        return intensityFunction;
    }

    /**
     * @param intensityFunction the intensityFunction to set
     */
    public void setIntensityFunction(PolynomialSplineFunction intensityFunction) {
        this.intensityFunction = intensityFunction;
    }

    
    public void generateInterpolatedEIC() throws InterruptedException {
        
        if (!empty) {
            

        
        int resolution = adduct.getSession().getResolution();
       

        
       generateIntensityFunction();
        
        setIntensityArray(new int[resolution]);
        setMZArray(new float[resolution]);
       
      
     
      
      //fill Intensity Array
      for (int i = 0; i< resolution; i++) {
           try { getIntensityArray()[i]=(int) getIntensityFunction().value(getRTArray()[i]);
            }
           catch (OutOfRangeException e) {
               getIntensityArray()[i] = 0;
               
           }
      }
      
      //fill MZArray
      //get half of delta
      float RTdeltah = (adduct.getOGroupObject().getRTArray()[1]-adduct.getOGroupObject().getRTArray()[0])/2;
      int RT = 0;
      int values = 0;
      //for all M/Z values
      for (int i = 0; i<massList.size(); i++) {
          //if M/Z is greater than 0
          if (massList.get(i)>0) {
              //while not correct bin
              while(retentionTimeList.get(i)>adduct.getOGroupObject().getRTArray()[RT]+RTdeltah) {
                  if (values>0) {
                      MZArray[RT]=MZArray[RT]/values;
                  }
                  RT++;
                  values = 0;
                  if (RT==adduct.getSession().getResolution()) {
                      RT=adduct.getSession().getResolution()-1;
                      break;
                  }
              }
               
              MZArray[RT]+=massList.get(i);
              values++;  
          }

      }
      
      if (values>0) {
                      MZArray[RT]=MZArray[RT]/values;
                  }
   
    //use byteMZArray instead
    float step = adduct.getSession().getMZTolerance()/100*adduct.getMZ()/1000000f;
            setByteMZArray(new byte[MZArray.length]);
    
    for (int i = 0; i<MZArray.length; i++) {
                getByteMZArray()[i] = (byte) ((MZArray[i]-adduct.getMZ())/step);
    }
      
      
     
     //delete originals
    this.intensityList = null;
    this.intensityFunction = null;
    this.massList = null;
    this.retentionTimeList = null;
    this.MZArray=null;
     
    }
    }

    /**
     * @return the RTArray
     */
    public float[] getRTArray() {
        return adduct.getRTArray();
    }

 

    /**
     * @return the IntensityArray
     */
    public int[] getIntensityArray() throws InterruptedException {
        if (stored) {
            System.out.println("Adding Read Slice from getIntensity");
           adduct.getSession().getIothread().addread(this);
           while(stored) {
               System.out.println("Waiting.......................");
               //Thread.sleep(10);
           }
        }
        return IntensityArray;
    }

    /**
     * @param IntensityArray the IntensityArray to set
     */
    public void setIntensityArray(int[] IntensityArray) {
        this.IntensityArray = IntensityArray;
    }

    

   
    
//    public void generateRefPeak () {
//        float quality = 0;
//        
//        int resolution = this.getIntensityArray().length;
//        int middle = resolution/2;
//        float[] smooth = this.getIntensityArray().clone();
//        
//        
//        CurveSmooth csm = new CurveSmooth(smooth);
//       smooth = csm.savitzkyGolay(50);
//        
//        
//        
//        
//        //smooth = movingAverageSmooth(smooth);
//        
//        
//        
//        //look for highest point in the middle +-10%
//        float max = smooth[middle];
//        int peakint = middle;
//        for (int i = 0; i < resolution/10; i++) {
//            //look left and right
//            if (max < smooth[middle+i]) {
//                max = smooth[middle+i];
//                peakint = middle+i;
//            }
//            if (max < smooth[middle-i]) {
//                max = smooth[middle-i];
//                peakint = middle-i;
//            }
//        }
//  
//        //if peak found, check the range of the peak
//        int end = peakint;
//        int start = peakint;
//        
//            //look right while the slope is steep enough
//            float difend=0;
//            while (end < resolution - 2 && (smooth[end]-smooth[end+1]>=difend)) {
//                difend = (smooth[end]-smooth[end+1])*0.8;
//                if (difend < 0) {
//                    difend = 0;                   
//                }
//                end = end + 1;
//            }
//            
//            //look left while the slope is steep enough
//            float difstart = 0;
//            while (start > 1 && (smooth[start]-smooth[start-1]>=difstart)) {
//                difstart = (smooth[start]-smooth[start-1])*0.8;
//                if (difstart < 0) {
//                    difstart = 0;                   
//                }
//                start = start - 1;
//
//            }
//
//            
//            //calculate quality
//            //heigth quality
//            float height = 0;
//            if (this.getIntensityArray()[peakint] > 500000) {
//                height = 1;
//            } else if (this.getIntensityArray()[peakint] < 5000) {
//                height = 0;
//            } else {
//                height = Math.log10(this.getIntensityArray()[peakint]) / Math.log10(500000);
//            }
//
//            //width quality
//            float width = 0;
//            if (end - start < 5 || end-peakint<2 || peakint-start<2) {
//                width = 0;
//            } else if (this.getRTArray()[end] - this.getRTArray()[start] < 0.6) {
//                width = 1;
//            } else {
//                width = 0.6/(this.getRTArray()[end] - this.getRTArray()[start]);
//            }
//            
//            //heigth above baseline
//            float heightabove = 0;
//            float bheigth = (this.getIntensityArray()[start]+this.getIntensityArray()[end])/2;
//            if (bheigth/this.getIntensityArray()[peakint]<=0.2) {
//                heightabove = 1;
//            } else { 
//                heightabove = 1- (bheigth/this.getIntensityArray()[peakint]);
//            }
//                
//                
//                quality = height*width*heightabove;
//                
//                
//                
//                
//                
//                
//                
//                
//                
//        }
    
     public float[] movingAverageSmooth(float[] smooth) {
        int resolution = smooth.length;
        float[] construction = new float[resolution];
        
        //smoothing
        //we want 5% windows around each point, minimum 1 point
        int range = (int) Math.ceil(resolution/40);
        

        
        for (int i = 0; i< 3; i++) {    //iterations
            for (int j = range; j<(resolution-range-1); j++) {
                construction[j]=0;
                for (int k = j-range; k<j+range; k++) {
                    construction[j]+=smooth[k];           
                }
                construction[j] =  construction[j]/(range*2+1);
            }
            smooth = construction.clone();
           
        }
        
        return smooth;
    }

     
     public void generateBins() {
         float step = this.adduct.getMaxMZ()-this.adduct.getMinMZ();
         step = step/file.getMzbins().length;
         Bins = new float[file.getMzbins().length];
         
         //store upper limits for each bin
         Bins[0]=this.adduct.getMinMZ()+step;
         for (int i =1; i< Bins.length; i++) {
             Bins[i] = Bins[i-1]+step;
         }
         
     }
     
     public void addmasstoBin(float mass) {
         //add mass to corresponding bin using binary search
         int start = 0;
         int end = Bins.length-1;
         int middle;
         
         while (end > start) {
             middle = (end+start)/2;
             if (Bins[middle]<mass) {
                 start = middle+1;
             } else if (Bins[middle]>=mass) {
                 end = middle;
             }
         }
         
         file.addtoBin(start);
         
     }
     
    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * @return the minIntensity
     */
    public float getMinIntensity() {
        return minIntensity;
    }

    /**
     * @param minIntensity the minIntensity to set
     */
    public void setMinIntensity(float minIntensity) {
        this.minIntensity = minIntensity;
    }

    /**
     * @return the maxIntensity
     */
    public float getMaxIntensity() {
        return maxIntensity;
    }

    /**
     * @param maxIntensity the maxIntensity to set
     */
    public void setMaxIntensity(float maxIntensity) {
        this.maxIntensity = maxIntensity;
    }

    /**
     * @return the listofPeaks
     */
    public List<Peak> getListofPeaks() {
        return listofPeaks;
    }

    /**
     * @param listofPeaks the listofPeaks to set
     */
    public void setListofPeaks(List<Peak> listofPeaks) {
        this.listofPeaks = listofPeaks;
    }
    
    public void addPeak(Peak peak){
        this.listofPeaks.add(peak);
    }
    
    public void deleteSlice() {
       
        
        this.file= null;
        this.dataset = null;
        this.adduct = null;
        this.retentionTimeList = null;
        this.intensityList = null;
        this.massList = null;
        
        if(this.IntensityArray!=null){
            this.IntensityArray=null;
        }
        
        if(this.PropArray!=null){
            this.PropArray=null;
        }
        if(this.intensityFunction!=null){
            this.intensityFunction=null;
        }
        
        
        
    }
    
//    public void storeIntensityList() {
//        try
//      {
//         FileOutputStream fileOut =
//         new FileOutputStream(name);
//         ObjectOutputStream out = new ObjectOutputStream(fileOut);
//         out.writeObject(this.IntensityArray);
//         out.close();
//         fileOut.close();
//         System.out.printf("Serialized data is saved in " + name);
//      }catch(IOException i)
//      {
//          i.printStackTrace();
//      }
//        
//    }
//    
//    public void loadIntensityList() {
//        try
//      {
//         FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
//         ObjectInputStream in = new ObjectInputStream(fileIn);
//         this.intensityList = (List<Float>) in.readObject();
//         in.close();
//         fileIn.close();
//      }catch(IOException i)
//      {
//         i.printStackTrace();
//         return;
//      }catch(ClassNotFoundException c)
//      {
//         System.out.println("List class not found");
//         c.printStackTrace();
//         return;
//      }
//      System.out.println("Deserialized List...");
//        
//    }

    /**
     * @return the MZArray
     */
    public float[] getMZArray() {
        return MZArray;
    }
    
    /**
     * @return the MZArray
     */
    public float getMZValue(int i) {
        return getByteMZArray()[i]*adduct.getMZ()/1000000*adduct.getSession().getMZTolerance()/100+adduct.getMZ();
    }

    /**
     * @param MZArray the MZArray to set
     */
    public void setMZArray(float[] MZArray) {
        this.MZArray = MZArray;
    }
    
    public Short setFittedPeak(int shift) {
        
       
        //TODO: range as function of RTTolerance
        fittedpeak = null;
        
        
        //first step is to find the peak
        //get maximum range
        if (listofPeaks != null) {
        int min = adduct.getSession().getIntPeakRTTol();
        for (short i = 0; i<listofPeaks.size(); i++) {
            //if smaller than maximum range or already found peak, set peak
            if (Math.abs(listofPeaks.get(i).getIndex()-shift)<=min) {
                min = Math.abs(listofPeaks.get(i).getIndex()-shift);
                fittedpeak = i;
                    
                //test to see if found or not
             
            }
        }
        
        //calc score for close peaks, if within range, small score
        int range = 5;
        min = adduct.getSession().getIntPeakRTTol()+range;
        for (int i = 0; i<listofPeaks.size(); i++) {
            if (fittedpeak==null||i!=fittedpeak) {
                if (Math.abs(listofPeaks.get(i).getIndex()-shift)<=min) {
                    min = Math.abs(listofPeaks.get(i).getIndex()-shift);
                        setScorepeakclose((1.0f/(float)(adduct.getSession().getIntPeakRTTol()+range+1))*min);
                }
            }
        }
        
        
        
//        System.out.println(adduct.getOGroup() + ":  Score peak close: " +  scorepeakclose);
//        System.out.println(adduct.getOGroup() + ":  Score peak found: " +  getScorepeakfound());
        }
        
        if (fittedpeak == null) {
            return 0;
            
        } else {
            return listofPeaks.get(fittedpeak).getIndex();
        }
    }
    
    //return the area of the fitted peak, or -1 if no fitted peak
    public Float getfittedArea() {
        if (fittedpeak == null) {
            return (null);
        } else {
            return listofPeaks.get(fittedpeak).getArea();
        }  
    }

    /**
     * @return the scorepeakfound
     */
    public float getScorepeakfound() {
        if (fittedpeak!=null) {
            return 1;
        } else {
            return 0;
        }
        
    }

    

    /**
     * @return the scorepeakclose
     */
    public float getScorepeakclose() {
        return scorepeakclose;
    }

    /**
     * @param scorepeakclose the scorepeakclose to set
     */
    public void setScorepeakclose(float scorepeakclose) {
        this.scorepeakclose = scorepeakclose;
    }

    float getScoredistance() {
        if (fittedpeak == null) {
            return 0;
        } else {
            //System.out.println((Math.abs((float)adduct.getOGroupObject().getOGroupFittedShift(file)-listofPeaks.get(fittedpeak).getIndex()))/(float)file.getSession().getIntPeakRTTol());
            return 1-((Math.abs((float)adduct.getOGroupObject().getOGroupFittedShift(file)-listofPeaks.get(fittedpeak).getIndex()))/(float)file.getSession().getIntPeakRTTol());
            
        }
    }
    
    //returns list filled with indexes of peaks
    public List<Short> getPeakIndex() {
        List<Short> list = new ArrayList<>();
        if (listofPeaks!=null) {
            for (int i = 0; i<listofPeaks.size(); i++) {
                list.add(listofPeaks.get(i).getIndex());
            }
        }
        
        return list;
    }
    
    public int getfittedPeakStart() {
        return listofPeaks.get(fittedpeak).getStart();
    }
    
    public int getfittedPeakEnd() {
        return listofPeaks.get(fittedpeak).getEnd();
    }
    
    public XYChart.Series manualPeak(short start, short end) throws InterruptedException {
        
        //Peak picking
        //takes intensity at start and end as a "baseline", subtracts the value of a line between those points from every intensity
        float delta = (getIntensityArray()[end]-getIntensityArray()[start])/(end-start);
        float current = getIntensityArray()[start];
        
        List<Float> intensity = new ArrayList<>();
        for (int i = start; i<=end; i++) {
            intensity.add(getIntensityArray()[i]-current);
            current = current + delta;
        }
        float max = 0;
        int maxint = -1;
        
        //only look for max in the middle 70% region
        int pstart = (int) (0.15*(intensity.size()-1));
        int pend = (int) (0.85*(intensity.size()-1));
        
        for (int i = pstart; i<= pend; i++) {
            if (intensity.get(i)>max) {
                max = intensity.get(i);
                maxint = i;
            }
        }
        
        
        
        //no max or max at edge
        if (max<=0 || getIntensityArray()[start+maxint]<adduct.getSession().getBaseline()) {
            return null;
        }
        
        
        
        short index = (short) (maxint+start);
        
        //don't add Peak if there is already a similar peak
        if (listofPeaks != null) {
            for (int i = 0; i<listofPeaks.size(); i++) {
                if (Math.abs(listofPeaks.get(i).getIndex()-index)<adduct.getSession().getIntPeakRTTol()) {
                    return null;
                }
            }
        }
        
        if (listofPeaks == null) {
            setListofPeaks(new ArrayList<>());
        }
        Peak newPeak = new Peak(true,index, start,end, this, 1);
        listofPeaks.add(newPeak);
        XYChart.Series newSeries = new XYChart.Series();
        
        float[] RTArray = adduct.getRTArray();
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getStart()], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getStart()], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getStart()], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getIndex()], 1.05));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getIndex()], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getIndex()], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getIndex()], 1.05));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getEnd()], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getEnd()], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getEnd()], 1.2));
              
        
                return newSeries;
    }
    
    public void deleteAutoPeaks() {
        if (listofPeaks!=null) {
        for (int i= 0; i<listofPeaks.size(); i++) {
           if (!listofPeaks.get(i).isManual()) {
               listofPeaks.remove(i);
               i--;
           }
            
        }
        } else {
            setListofPeaks(new ArrayList<>());
        }
    }

    /**
     * @return the empty
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @return the byteMZArray
     */
    public byte[] getByteMZArray() {
        if (stored) {
             System.out.println("Adding Read Slice from getMZ");
           adduct.getSession().getIothread().addread(this);
           while(stored) {
               //wait
           }
        }
        return byteMZArray;
    }

    /**
     * @param byteMZArray the byteMZArray to set
     */
    public void setByteMZArray(byte[] byteMZArray) {
        this.byteMZArray = byteMZArray;
    }
    
    public void writeData() throws IOException, InterruptedException {
        if (!written&&!stored) {
            setRw(true);
            file.writeData(this);
        }
        
//        if (!written&&!stored) {
//        setRw(true);
//ByteBuffer buffer = ByteBuffer.allocate(5 * IntensityArray.length);
//                for (int i : IntensityArray) {
//                        buffer.putInt(i); }
//                for (byte i :byteMZArray) {
//                    buffer.put(i);}
// 
//                FileChannel fc = null;
//                IntensityArray = null;
//                byteMZArray = null;
//                try {
//                        fc = new FileOutputStream("C:\\Users\\stefankoch\\Documents\\tmp\\" + this.toString().substring(44)+"fcalt.out").getChannel();
//                        buffer.flip();
//                        fc.write(buffer);
//                        
//                        
//                } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                } catch (IOException e) {
//                        e.printStackTrace();
//                } finally {
//                    setStored(true);
//                    setRw(false);
//                    setWritten(true);
//                        safeClose(fc);
//                }
//    }


    }
    
    public void readData() throws FileNotFoundException, IOException, InterruptedException{
        if (stored) {
            setRw(true);
            file.readData(this);
            
        }
        
//        if (stored) {
//        
//        setRw(true);
//    
//    Path path = Paths.get("C:\\Users\\stefankoch\\Documents\\tmp\\" + this.toString().substring(44)+"fcalt.out");
//    byte[] arr = Files.readAllBytes(path);
//    IntBuffer ib = ByteBuffer.wrap(arr).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
//    IntensityArray = new int[100];
//    for (int i = 0; i<100; i++) {
//        IntensityArray[i]=ib.get(i);
//    }
//    byteMZArray = new byte[100];
//    for (int i = 400; i<500; i++) {
//        byteMZArray[i-400] = arr[i];
//    }
//    
//    setStored(false);
//        setRw(false);
//        
//    }


    }
    private static void safeClose(FileChannel out) {
                try {
                        if (out != null) {
                                out.close();
                        }
                } catch (IOException e) {
                        // do nothing
                }
        }

    /**
     * @return the rw
     */
    public boolean isRw() {
        return rw;
    }

    /**
     * @param rw the rw to set
     */
    public void setRw(boolean rw) {
        this.rw = rw;
    }

    /**
     * @return the stored
     */
    public boolean isStored() {
        return stored;
    }

    /**
     * @param stored the stored to set
     */
    public void setStored(boolean stored) {
        this.stored = stored;
    }

    /**
     * @return the written
     */
    public boolean isWritten() {
        return written;
    }

    /**
     * @param written the written to set
     */
    public void setWritten(boolean written) {
        this.written = written;
    }
 
   
}