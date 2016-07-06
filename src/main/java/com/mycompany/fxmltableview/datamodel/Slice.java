/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;


import flanagan.analysis.CurveSmooth;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.rosuda.JRI.Rengine;

import java.io.IOException;


import java.nio.channels.FileChannel;

import java.io.FileNotFoundException;
import java.util.Collections;
import javafx.scene.chart.XYChart;
import org.apache.commons.lang3.ArrayUtils;
import org.rosuda.JRI.REXP;


/**
 *
 * @author stefankoch
 */
public class Slice {
    
    //flag
    //private boolean rw;
    private boolean stored;
    private boolean written;
    private boolean locked;
    private int size;
    private int position;
   
    private RawDataFile file;
    //private String name;
    
    
   
    //relative to RawDataFile RT
    private int RTstart;
    private int RTend;
    
    private float[] IntArray;
    private float[] MZArray;
    private float minIntensity;
    private float maxIntensity;
    private List<Peak> listofPeaks;
    private Short fittedpeak;
    private float avgInt;
    
    private float scorepeakclose = Float.POSITIVE_INFINITY;
    private float fitabove;
   
    private Entry adduct;
    private Dataset dataset;
    
    //processed information
    
    
    //private float[] Bins;
    
    private boolean empty;
   
    public Slice(RawDataFile file, Entry adduct) {
        this.file = file;
        this.dataset = file.getDataset();
        this.adduct = adduct;
        //this.name = (this+"intList.ser");
        this.fittedpeak = null;
        
        //rw=false;
        stored=false;
        locked = false;
    }
    
    //returns the length of information in bytes
     public void newbinaryExtractSlicefromScans(List<Scan> listofScans, float[] ScanRTs) {
         
        if (this.adduct.getNum()==44) {
            System.gc();
        }
         
       
         //for all Scans
       float minMZ = getMinMZ();
       float maxMZ = getMaxMZ();
       float minRT = getMinRT();
       float maxRT = getMaxRT();
       int numberofsignals=0;
         
        setRTstart(Arrays.binarySearch(ScanRTs, minRT));
        
       if (getRTstart()<0) {
            setRTstart((getRTstart() + 1) * (-1));
       }
       
        setRTend(Arrays.binarySearch(ScanRTs, maxRT)-1);
       
       if (getRTend()<0) {
            setRTend((getRTend() + 3) * (-1));
       }
       
            setIntArray(new float[getRTend()-getRTstart()+1]);
            setMZArray(new float[getRTend()-getRTstart()+1]);
     
       
      
       for (int i = getRTstart(); i<=getRTend(); i++) {
           float[] mz = listofScans.get(i).getMassovercharge();
           float[] intensity = listofScans.get(i).getIntensity();
           
           int minM = Arrays.binarySearch(mz, minMZ);
           
           if (minM<0) {
           minM = (minM+1)*(-1);
           }
           
           int length = mz.length;
          
          if (minM<length) {
           while (mz[minM]<=maxMZ) {
               
                       if (intensity[minM]>IntArray[i-getRTstart()]) {
                            MZArray[i-getRTstart()] = mz[minM];
                            IntArray[i-getRTstart()] = intensity[minM];
                            numberofsignals++;
                            
                       }
                   
                   minM++;
                   if (minM==length) {
                       break;
                   }
           }
          }
          
          
         
       }
       
       
           
           
           
           
           
           
           
       
       
    
       
       
       
       
        
    
       //Bins=null;
      
     //this.clean();
     //this.generateInterpolatedEIC();
     
     if (numberofsignals<5) {
         this.empty = true;
     } else {
         this.empty = false;
         size = MZArray.length;
     }
     
     
     
    }
    
     
   
    
    //interpolates intensities
//    public  void generateIntensityFunction() {
//       double[] RT = new double[this.retentionTimeList.size()];
//       double[] Intensity = new double[this.retentionTimeList.size()];
//       for (int i =0; i<RT.length; i++) {
//           RT[i] = this.retentionTimeList.get(i);
//           Intensity[i] = this.intensityList.get(i);
//       }
//        
//        
//        
//       LinearInterpolator interpolator = new LinearInterpolator();
//       
//       this.intensityFunction = interpolator.interpolate(RT, Intensity);
//    }
//    
    
//generates Array filled with "probabilities", correspond to wavelet peaks 
//caluclated with R MassSpecWavelet
    public void WaveletPeakPicking() throws InterruptedException {
//        float startc = System.currentTimeMillis();
            deleteAutoPeaks();
        
            //baseline correct IntensityArray
            float[] correctedIntArray = new float[IntArray.length];
            for ( int j = 0; j<IntArray.length; j++)  {
                
                    correctedIntArray[j]=IntArray[j];
                
                
            }

        
        //float start1 = System.currentTimeMillis();
        // Create an R vector in the form of a string.
        String EIC = Arrays.toString(correctedIntArray);
        EIC = EIC.substring(1, EIC.length()-1);
        //100 zeros at start and end, 50 are not enough
        EIC = "c(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,".concat(EIC);
        EIC = EIC.concat(",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");
        //System.out.println("EIC String processing: " + (System.currentTimeMillis()-start1));
       
        
        // Start Rengine.
        Rengine engine = adduct.getSession().getEngine();
        //System.out.println("get engine");

        // The vector that was created in JAVA context is stored in 'rVector' which is a variable in R context.
        //=INPUTS
        engine.eval("eic=" + EIC);
        //System.out.println("eic=" + EIC);
        //System.out.println("send data");
//       REXP s = engine.eval("capture.output( eic=" + EIC + ")");
//System.out.println(s.asString());
        
        
        //Retrieve values, see script for names
        //=OUTPUTS
//        float start3 = System.currentTimeMillis();
        String evalstring = "getMajorPeaks(eic, scales=c(5, 19), snrTh=";
         
//        System.out.println("getmajorpeaks");
        evalstring = evalstring.concat(file.getSession().getNoisethreshold().get() + ")");
//        s = engine.eval("paste(capture.output( " + evalstring + "),collapse='\\n')");
//        System.out.println(evalstring);
//System.out.println(s.asString());
        double[][] ret = engine.eval(evalstring).asDoubleMatrix();
//        System.out.println("Wavelet calculation: " + (System.currentTimeMillis()-start3));
        
        //Print output values, work with them...
        if (ret!=null) {
//            float start4 = System.currentTimeMillis();
        
            for (int j = 0; j<ret[0].length; j++) {
                //101 because of 100 zeros at start and R starts at 1
                if (((int)ret[0][j]-101)<IntArray.length) {
                
                addPeak(new Peak((short) ((short)ret[0][j]-101), (float)ret[1][j], (float)ret[2][j], (float)ret[3][j], this));
            }}
            
        
//        System.out.println("PropArray processing: " + (System.currentTimeMillis()-start4));
        }
        //end Rengine, otherwise thread doesn't terminate
        //engine.end();
         EIC=null;

//        System.out.println("Complete processing: " + (System.currentTimeMillis()-startc));
//        System.out.println("Slice processed.......");
    }
    
    
    
    //generates Array filled with probabilities, correspond to the probabiltiy of a guassian peak at this RT
 public void NaivePeakPicking() throws InterruptedException {
     //long startc = System.nanoTime();
         //initialize Array holding probabilities
    //long startp = System.nanoTime();
        double [] PropArray =(new double[IntArray.length]);
        //System.out.println("PropArray allocation: " + (System.nanoTime()-startp));
        
        //long startg = System.nanoTime();
        addGaussCorrelation(0.6f,PropArray);
        addGaussCorrelation(0.5f, PropArray);
        addGaussCorrelation(0.4f, PropArray);
        //System.out.println("Gauss: " + (System.nanoTime()-startg));
        
        //long start2 = System.nanoTime();
        generatePeakArray(PropArray);
       // System.out.println("PeakArrayGeneration: " + (System.nanoTime()-start2));
        PropArray = null;
         
        //System.out.println("Complete processing: " + (System.nanoTime()-startc));
        
    }
 
 public void SavitzkyGolayPeakPicking() throws InterruptedException {
      //baseline correct IntensityArray
         
//         if (adduct.getNum()==84823) {
//             System.out.println("Starting on Slice: " + adduct.getNum()) ;
//         }
            double[] correctedIntArray = new double[IntArray.length];
            for ( int j = 0; j<IntArray.length; j++)  {
                if (IntArray[j]>=adduct.getSession().getBaseline()) {
                    correctedIntArray[j]=IntArray[j]-adduct.getSession().getBaseline();
                }
                
            }
     
            
            CurveSmooth csm = new CurveSmooth(correctedIntArray);
            csm.savitzkyGolay(30);
           
            
           
            double[] max = csm.getMaximaSavitzkyGolay()[0];
     double[] minima = csm.getMinimaSavitzkyGolay()[0];

//CurveSmooth csm = new CurveSmooth(correctedIntArray);
//csm.movingAverage(30);
//
//        double[] max = csm.getMaximaMovingAverage()[0];
//     double[] minima = csm.getMinimaMovingAverage()[0];

    
     //create max and min, min holding the starts/ends of the peaks, and max holding the index
     double[]min = new double[max.length+1];
     int offsetmin;
     if (max.length>0) {
     if (minima.length==0||minima[0]>max[0]) {
             min[0]=0;
             offsetmin = 1;
         } else {
         min[0]=minima[0];
         offsetmin = 0;
     }
     for (int i = 0; i<minima.length; i++) {
        min[i+offsetmin]=minima[i];
     }
     //if last min specified
      if (minima.length>0&&minima[minima.length-1]>max[max.length-1]) {
//          min[min.length-1]=minima[minima.length-1];
     } else {
         min[min.length-1]=(short)IntArray.length-1;
     }
     
     
    //System.out.println(this + "Max: " + Arrays.toString(max));
    //System.out.println(this + "Min: " + Arrays.toString(min));
     
     if (listofPeaks==null) {
         listofPeaks = new ArrayList<Peak>();
     } else {
         deleteAutoPeaks();
     }
     short start = (short)min[0];
   for (int i = 0; i<max.length; i++) {
       short end = (short) min[i+1];
       short index = (short)max[i];
       addPeak(new Peak((short)max[i],start,end,this));
       start = end;
   }
     } 
 }
 
// adds correlation to PropArray calulated for a gaussian of length "length" (in minutes) from -2 to +2 std
 public void addGaussCorrelation(float length, double[] PropArray) throws InterruptedException {
     
      float valuesperminute = 60.0f/file.getScanspersecond();
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
         
            double[] correctedIntArray = new double[IntArray.length];
            for ( int j = 0; j<IntArray.length; j++)  {
                if (IntArray[j]>=adduct.getSession().getBaseline()) {
                    correctedIntArray[j]=IntArray[j]-adduct.getSession().getBaseline();
                }
                
            }
            
        for (int i = 0; i< (IntArray.length-peakArray.length); i++) {
            
        double corr = pear.correlation(peakArray ,Arrays.copyOfRange(correctedIntArray, i, i+peakArray.length));
        
                //scale according to maxIntensity
               //and weaken weak signals
               if (corr > 0) {
                   double newcorr = (corr*corr)*asymptoticFunction(IntArray[i+peakint]-minIntensity);
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
 public void generatePeakArray(double[] PropArray) throws InterruptedException {
//     for (int i = 0; i<PropArray.length; i++) {
//         if (PropArray[i]<0.2) {
//             PropArray[i] = 0;
//         }
//         
//     }
//if (adduct.getNum()==4716) {
//             System.out.println("Starting on Slice: " + adduct.getNum()) ;
//         }

     CurveSmooth csm = new CurveSmooth(PropArray);
    csm.movingAverage(3);
    csm.movingAverage(3);
    csm.movingAverage(3);
     double[] max = csm.getMaximaMovingAverage()[0];
     double[] minima = csm.getMinimaMovingAverage()[0];
     
      //create max and min, min holding the starts/ends of the peaks, and max holding the index
     double[]min = new double[max.length+1];
     int offsetmin;
     if (max.length>0) {
     if (minima.length==0||minima[0]>max[0]) {
             min[0]=0;
             offsetmin = 1;
         } else {
         min[0]=minima[0];
         offsetmin = 0;
     }
     for (int i = 0; i<minima.length; i++) {
        min[i+offsetmin]=minima[i];
     }
     //if last min specified
     if (minima.length>0&&minima[minima.length-1]>max[max.length-1]) {
//          min[min.length-1]=minima[minima.length-1];
     } else {
         min[min.length-1]=(short)IntArray.length-1;
     }
     
    
     
     if (listofPeaks==null) {
         listofPeaks = new ArrayList<Peak>();
     } else {
         deleteAutoPeaks();
     }
     short start = (short)min[0];
   for (int i = 0; i<max.length; i++) {
       short end = (short) min[i+1];
       short index = (short)max[i];
       if (PropArray[index]>0.2){
       addPeak(new Peak((short)max[i],start,end,this)); }
       start = end;
   }
     } 

     
     
//     for (int i = 0; i<maxima[0].length; i++) {
//         if (PropArray[(int)maxima[0][i]]<0.2) {
//             maxima[0][i] = -1;
//         }
//     }
//     
//     
//     for (int i = 0; i<PropArray.length; i++ ) {
//         PropArray[i] = 0;
//     }
//     
//     for (int i = 0; i<maxima[0].length; i++) {
//         if (maxima[0][i]>0) {
//         PropArray[(int)maxima[0][i]] = 1;
//         }
//     }
//     
//     for (int i = 0; i<minima[0].length; i++) {
//         PropArray[(int)minima[0][i]] = 0.3;
//         
//     }
//     
//     //now we have an Array with Marks at Max and Min
//     deleteAutoPeaks();
//     short start = 0; 
//    short index = 0;
//    short end = 0;
//     for (short i = 0; i< PropArray.length; i++) {
//         //search for non 0 entry
//        while (PropArray[i]==0) {
//            i++;
//            if (i == (PropArray.length-1)) {
//                break;
//            }
//        }
//        //if Min, mark start
//        if (PropArray[i]==0.3) {
//            PropArray[i]=0;
//            start = i;
//            //if Max, mark index and look for end
//        } else if (PropArray[i]==1) {
//            index = i;
//            i++;
//            //if end of Array
//            if (i==(PropArray.length-1)) {
//            end = i;
//            addPeak(new Peak(index,start,end, this));
//            }
//            //while not at end
//            while (i<PropArray.length-1 && PropArray[i]==0) {
//                i++;
//                //if end of Array
//                if (i==(PropArray.length-1)) {
//            end = i;
//            addPeak(new Peak(index,start,end, this)); 
//            }
//            }
//            //if end found, make peak
//            if (PropArray[i]==0.3) {
//            PropArray[i]=0;
//            end = i;
//            addPeak(new Peak(index,start,end, this));
//            //if another max, split
//            } else if (PropArray[i]==1) {
//                end = (short) ((index+i)/2);
//                addPeak(new Peak(index,start,end, this));
//                start = end;
//                i--;
//            } else if (PropArray[i]==0){
//                //do nothing
//            } else {
//                System.out.println("Unexpected value in PropArray");
//            }
//            
//        } else if (PropArray[i]==0){
//            //do nothing
//        } else {
//            System.out.println("Unexpected value in PropArray");
//        }
//        
//      
        
         
//     }
     
     
     
     
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

    

    

    


    
    
//    public List<Float> smooth(int iterations) {
//        List<Float> newIntList= new ArrayList<>(intensityList);
//        for (int i= 0; i<iterations; i++) {
//      
//            for (int j = 1; j< (retentionTimeList.size()-1); j++) {
//                if (newIntList.get(j)>100) {
//                newIntList.set(j, (newIntList.get(j-1)+ newIntList.get(j) + newIntList.get(j+1))/3);
//                
//            }
//            }
//        
//        }
//    return newIntList;
//}

    

   
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

//     
//     public void generateBins() {
//         float step = this.adduct.getMaxMZ()-this.adduct.getMinMZ();
//         step = step/file.getMzbins().length;
//         Bins = new float[file.getMzbins().length];
//         
//         //store upper limits for each bin
//         Bins[0]=this.adduct.getMinMZ()+step;
//         for (int i =1; i< Bins.length; i++) {
//             Bins[i] = Bins[i-1]+step;
//         }
//         
//     }
//     
//     public void addmasstoBin(float mass) {
//         //add mass to corresponding bin using binary search
//         int start = 0;
//         int end = Bins.length-1;
//         int middle;
//         
//         while (end > start) {
//             middle = (end+start)/2;
//             if (Bins[middle]<mass) {
//                 start = middle+1;
//             } else if (Bins[middle]>=mass) {
//                 end = middle;
//             }
//         }
//         middle = (end+start)/2;
//         file.addtoBin(middle);
//         
//     }
     
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
        float[] length = peak.gethalflength();
        float halflength =file.getSession().getMinPeakLength().floatValue()/2;
        boolean duplicate = false;
        for (Peak p: listofPeaks) {
           if (p.getIndex()==peak.getIndex()) {
               duplicate = true;
           }
        }
        if (!duplicate&&length[0]>=halflength&&length[1]>=halflength) {
        this.listofPeaks.add(peak);}
    }
    
    public void deleteSlice() {
       
        
        this.file= null;
        this.dataset = null;
        this.adduct = null;
        this.IntArray=null;
        this.MZArray=null;
        
        listofPeaks=null;
        
     
        
        
        
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


    
    public void setFittedPeak(float shift) {
        fitabove = Float.NaN;
        
       
       
        //TODO: range as function of RTTolerance
        fittedpeak = null;
        
       
        
        //first step is to find the peak
        //get maximum range
        if (listofPeaks != null) {
        float min = file.getSession().getPeakRTTolerance().floatValue();
        for (short i = 0; i<listofPeaks.size(); i++) {
            //if smaller than maximum range set peak
            if (Math.abs(listofPeaks.get(i).getIndexshift()-shift)<=min) {
                min = Math.abs(listofPeaks.get(i).getIndexshift()-shift);
                fittedpeak = i;
                    
             
            }
        }
        
        //calc score for close peaks, if within range, small score
        float range = file.getSession().getPeakRTTolerance().floatValue()*2;
        scorepeakclose = file.getSession().getRTTolerance();
        for (int i = 0; i<listofPeaks.size(); i++) {
            if (fittedpeak==null||i!=fittedpeak) {
                if (Math.abs(listofPeaks.get(i).getIndexshift()-shift)<scorepeakclose) {
                    scorepeakclose = Math.abs(listofPeaks.get(i).getIndexshift()-shift);
                }
            }
        }
        
        
        
        //System.out.println(adduct.getOGroup() + ":  Score peak close: " +  scorepeakclose);
//        System.out.println(adduct.getOGroup() + ":  Score peak found: " +  getScorepeakfound());
        }
        
        //calculate Noise Units of a hypothetical peak 
        if (fittedpeak==null){
            int RTindex = Math.abs(Arrays.binarySearch(file.getRTArray(), shift+adduct.getRT()))-RTstart;
            if (RTindex>=IntArray.length) {
            System.out.println(this.adduct.getNum());
            RTindex=IntArray.length-1;
            } else if (RTindex<0) {
                RTindex = 0;
            }
            
            float intensity = IntArray[RTindex];
            
            //check if we are "inside" another peak, if yes, do nothing
            boolean inside = false;
            if (listofPeaks!=null) {
            for (Peak peak:listofPeaks) {
                if (peak.getStart()<=RTindex&&peak.getEnd()>=RTindex) {
                    inside = true;
                }
            }
            }
            if (!inside) {
            
            //TODO get range
            float sheight=intensity;
            float eheight=intensity;
            for (int i = 1; i< 15; i++) {
                if (RTindex-i>0) {
                    sheight=Math.min(sheight, IntArray[RTindex-i]);
                }
                if (RTindex+i<IntArray.length) {
                    eheight = Math.min(eheight, IntArray[RTindex+i]);
                }
            }
            
            //avoid false detection on steep slopes
            if (eheight<intensity&&sheight<intensity) {
            
            float height = (sheight+eheight)/2;
           //calculate Noise Unit for peak
            float NU = (float) Math.sqrt(intensity)*file.getNoiseFactor();
            
            //calculate height above surrounding signals
            height=intensity-height;
            
            //calculate how many Noise Units the peak rises above the surrounding signals
            fitabove = (height/NU);
           
            }
            
            }
            
           
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
            return 1-((Math.abs(getFittedPeak().getIndexshift()))/(float)file.getSession().getPeakRTTolerance().floatValue());
            
        }
    }
    
    
    public int getfittedPeakStart() {
        return listofPeaks.get(fittedpeak).getStart();
    }
    
    public int getfittedPeakEnd() {
        return listofPeaks.get(fittedpeak).getEnd();
    }
    
    public XYChart.Series manualPeak(short start, short end) throws InterruptedException {
        file.getSession().getIothread().lockSlice(this, true);
        
        if (this.stored) {
        file.getSession().getIothread().readslice(this);
                }
        
        while (this.stored) {
            Thread.sleep(10);
        }
        
        if (start<0) {
            start=0;
        }
        if (end>=IntArray.length) {
            end = (short) (IntArray.length-1);
        }
        //Peak picking
        //takes intensity at start and end as a "baseline", subtracts the value of a line between those points from every intensity
        float delta = (IntArray[end]-IntArray[start])/(end-start);
        float current = IntArray[start];
        
        List<Float> intensity = new ArrayList<>();
        for (int i = start; i<=end; i++) {
            intensity.add(IntArray[i]-current);
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
        if (max<=0 || IntArray[start+maxint]<adduct.getSession().getBaseline()) {
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
        
        float[] RTArray = file.getRTArray();
        for ( int j = start; j<=index; j++) {
                    newSeries.getData().add(new XYChart.Data(RTArray[j+RTstart], IntArray[j]/maxIntensity));
                }
                newSeries.getData().add(new XYChart.Data(RTArray[index+RTstart], 0));
                for ( int j = index; j<=end; j++) {
                    newSeries.getData().add(new XYChart.Data(RTArray[j+RTstart], IntArray[j]/maxIntensity));
                }
              
                file.getSession().getIothread().lockSlice(this, false);
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

 
    
    public void writeData() throws IOException, InterruptedException {
        if (!written) {
            //setRw(true);
            file.writeData(this);
        }
        stored = true;
        MZArray = null;
        IntArray = null;
        
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
            //setRw(true);
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

//    /**
//     * @return the rw
//     */
//    public boolean isRw() {
//        return rw;
//    }
//
//    /**
//     * @param rw the rw to set
//     */
//    public void setRw(boolean rw) {
//        this.rw = rw;
//    }

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
        if (stored) {
        MZArray=null;
        IntArray=null;}
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

    /**
     * @return the IntArray
     */
    public float[] getIntArray() {
        return IntArray;
    }

    /**
     * @param IntArray the IntArray to set
     */
    public void setIntArray(float[] IntArray) {
        this.IntArray = IntArray;
    }

    /**
     * @return the MZArray
     */
    public float[] getMZArray() {
        return MZArray;
    }

    /**
     * @param MZArray the MZArray to set
     */
    public void setMZArray(float[] MZArray) {
        this.MZArray = MZArray;
    }
 
    public Entry getAdduct() {
        return adduct;
    }
 
    public boolean narrowMZ(float shift) {
        float newMZ = adduct.getMZ()+(adduct.getMZ()/1000000*shift);
        float dif = newMZ/1000000*file.getSession().getSliceMZTolerance();
        float maxMZ = newMZ+dif;
        float minMZ = newMZ-dif;
        empty = true;
        float max = Float.MIN_VALUE;
        //number of consecutive signals
        int nocs = 0;
        long sum = 0;
        int minnocs = (int) (file.getSession().getMinPeakLength().floatValue()*60*file.getScanspersecond());
        for (int i = 0; i<MZArray.length; i++) {
            if (MZArray[i]==0||MZArray[i]<minMZ||MZArray[i]>maxMZ) {
                MZArray[i] = 0;
                IntArray[i] = 0;
                
                if (nocs>=minnocs) {
                    empty = false;
                }
                nocs=0;
              
            } else {
                if (IntArray[i]>max) {
                    max = IntArray[i];
                }
                sum+=IntArray[i];
                nocs++;
            }
        }
        if (nocs>=minnocs) {
                    empty = false;
                }
        //rough baseline calculation
        sum/=IntArray.length;
        avgInt=sum;
        if (max-sum<file.getSession().getBaseline()) {
            //System.out.println("noise deleted" + this.adduct.getNum());
            empty = true;
        }
        
        if (!empty) {
            adduct.getListofSlices().put(file, this);
        }
        maxIntensity = max;
return empty;
    }

    /**
     * @return the RTstart
     */
    public int getRTstart() {
        return RTstart;
    }

    /**
     * @param RTstart the RTstart to set
     */
    public void setRTstart(int RTstart) {
        this.RTstart = RTstart;
    }

    /**
     * @return the RTend
     */
    public int getRTend() {
        return RTend;
    }

    /**
     * @param RTend the RTend to set
     */
    public void setRTend(int RTend) {
        this.RTend = RTend;
    }

    /**
     * @return the fittedpeak
     */
    public Short getFittedpeak() {
        return fittedpeak;
    }

    /**
     * @param fittedpeak the fittedpeak to set
     */
    public void setFittedpeak(Short fittedpeak) {
        this.fittedpeak = fittedpeak;
    }
    
  
    public Peak getFittedPeak() {
        if (fittedpeak!=null) {
            return listofPeaks.get(fittedpeak);
        }
        return null;
    }

    

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @return the avgInt
     */
    public float getAvgInt() {
        return avgInt;
    }

    /**
     * @param avgInt the avgInt to set
     */
    public void setAvgInt(float avgInt) {
        this.avgInt = avgInt;
    }

    /**
     * @return the fitabove
     */
    public float getFitabove() {
        return fitabove;
    }

    /**
     * @param fitabove the fitabove to set
     */
    public void setFitabove(float fitabove) {
        this.fitabove = fitabove;
    }
    
    
    
}