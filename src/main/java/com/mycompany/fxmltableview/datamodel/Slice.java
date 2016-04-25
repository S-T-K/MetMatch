/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;


import flanagan.analysis.CurveSmooth;
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

import static java.lang.Math.abs;
import java.util.Collections;
import javafx.scene.chart.XYChart;

/**
 *
 * @author stefankoch
 */
public class Slice {
    
    
    private RawDataFile file;
    private String name;
    
    //TODO: one RTList/OGroup or calculation every time
    //TODO: maybe less precision for Intensity?
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
    private PolynomialSplineFunction intensityFunction;
    private float minIntensity;
    private float maxIntensity;
    private List<Peak> listofPeaks;
    private Integer fittedpeak;
    
    private double scorepeakclose = 1;
   
    private Entry adduct;
    private Dataset dataset;
    
    //processed information
    private double[] MZArray;
    private double[] IntensityArray;
    private double[] PropArray;
    
    private double[] Bins;
    
    public Slice(RawDataFile file, Entry adduct) {
        this.file = file;
        this.dataset = file.getDataset();
        this.adduct = adduct;
        this.name = (this+"intList.ser");
        this.fittedpeak = null;
        
    }
    

    
    public void extractSlicefromScans(List<Scan> listofScans) {
        double start = System.currentTimeMillis();
        generateBins();
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
     Bins = null;
     double end = System.currentTimeMillis();
     //System.out.println("Extraction: " + (end-start));
    }
    
    //not yet working...
    public void binaryExtractSlicefromScans(List<Scan> listofScans) {
       generateBins();
         //for all Scans
         setMinIntensity(900000000);
         setMaxIntensity(0);
       double minMZ = getMinMZ();
       double maxMZ = getMaxMZ();
       double minRT = getMinRT();
       double maxRT = getMaxRT();
         
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
         
    
       
      
     //this.clean();
     //this.generateInterpolatedEIC();
     
    }
    
    
    //interpolates intensities
    public  void generateIntensityFunction() {
       double[] RT = new double[this.retentionTimeList.size()];
       double[] MZ = new double[this.massList.size()];
       double[] Intensity = new double[this.retentionTimeList.size()];
       for (int i =0; i<RT.length; i++) {
           RT[i] = this.retentionTimeList.get(i);
           Intensity[i] = this.intensityList.get(i);
           MZ[i]=this.massList.get(i);
       }
        
        
        
       LinearInterpolator interpolator = new LinearInterpolator();
       
       this.intensityFunction = interpolator.interpolate(RT, Intensity);
    }
    
    
//generates Array filled with "probabilities", correspond to wavelet peaks 
//caluclated with R MassSpecWavelet
    public void WaveletPeakPicking() {
        double startc = System.currentTimeMillis();
        if (PropArray == null) {
            PropArray = (new double[this.IntensityArray.length]);
            deleteAutoPeaks();
        
            //baseline correct IntensityArray
            double[] correctedIntArray = new double[IntensityArray.length];
            for ( int j = 0; j<IntensityArray.length; j++)  {
                if (IntensityArray[j]>=adduct.getSession().getBaseline()) {
                    correctedIntArray[j]=IntensityArray[j]-adduct.getSession().getBaseline();
                }
                
            }

        
        double start1 = System.currentTimeMillis();
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
        double start3 = System.currentTimeMillis();
        double[][] ret = engine.eval("getMajorPeaks(eic, scales=c(5, 12), snrTh=3)").asDoubleMatrix();
        //System.out.println("Wavelet calculation: " + (System.currentTimeMillis()-start3));
        
        //Print output values, work with them...
        if (ret!=null) {
            double start4 = System.currentTimeMillis();
        
            for (int j = 0; j<ret[0].length; j++) {
                //101 because of 100 zeros at start and R starts at 1
                if (((int)ret[0][j]-101)<100) {
                PropArray[(int)ret[0][j]-101]=1;
                addPeak(new Peak(((int)ret[0][j]-101), ret[1][j], ret[2][j], ret[3][j], this));
            }}
            
        
        //System.out.println("PropArray processing: " + (System.currentTimeMillis()-start4));
        }
        //end Rengine, otherwise thread doesn't terminate
        //engine.end();
        
}
        PropArray = null;
        System.out.println("Complete processing: " + (System.currentTimeMillis()-startc));
    }
    
    
    
    //generates Array filled with probabilities, correspond to the probabiltiy of a guassian peak at this RT
 public void NaivePeakPicking() {
     double startc = System.currentTimeMillis();
         //initialize Array holding probabilities
        if (PropArray==null){ 
        PropArray =(new double[this.IntensityArray.length]);
        
        addGaussCorrelation(0.6);
        addGaussCorrelation(0.5);
        addGaussCorrelation(0.4);
        
        }
        generatePeakArray();
        PropArray = null;
        //System.out.println("Complete processing: " + (System.currentTimeMillis()-startc));
    }
 
 //adds correlation to PropArray calulated for a gaussian of length "length" (in minutes) from -2 to +2 std
 public void addGaussCorrelation(double length) {
     
      double valuesperminute = adduct.getSession().getResolution()/(adduct.getSession().getRTTolerance()*2);
      int arraylength = (int) (valuesperminute*length);
        if (arraylength%2==0) {
                   arraylength++;
        }
        
        double[] peakArray = new double[arraylength];
        int peakint = Math.floorDiv(arraylength, 2);
        NormalDistribution normdist = new NormalDistribution();
        //edge of peak is at X std
        double peakedge = 2;
        double increment = peakedge/(peakint-1);
        for (int i = 0; i<=peakint; i++) {
            peakArray[i]=normdist.density(peakedge-i*increment);
            peakArray[(arraylength-1)-i]=peakArray[i];
        }
                
         PearsonsCorrelation pear = new PearsonsCorrelation();

         
         //baseline correct IntensityArray
            double[] correctedIntArray = new double[IntensityArray.length];
            for ( int j = 0; j<IntensityArray.length; j++)  {
                if (IntensityArray[j]>=adduct.getSession().getBaseline()) {
                    correctedIntArray[j]=IntensityArray[j]-adduct.getSession().getBaseline();
                }
                
            }
            
        for (int i = 0; i< (IntensityArray.length-peakArray.length); i++) {
            
        double corr = pear.correlation(peakArray ,Arrays.copyOfRange(correctedIntArray, i, i+peakArray.length));
        
                //scale according to maxIntensity
               //and weaken weak signals
               if (corr > 0) {
                   double newcorr = (corr*corr)*asymptoticFunction(IntensityArray[i+peakint]-minIntensity);
                   if ((PropArray[i+peakint]<newcorr)) {
                       PropArray[i+peakint]= newcorr;}
               }
        
        }
     
 }
 
 
 //returns value between 0 and 1, rapidly falling for values lower than the baseline
 public double asymptoticFunction(double intensity) {
     float baseline = adduct.getSession().getBaseline();
     
     return (intensity-baseline/(baseline*10))/(1+intensity-baseline/(baseline*10));
 }
 
 //generates Array filled with Peak probabilites
 //TODO: negative Maxima
 public void generatePeakArray() {
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
     int start = 0; 
    int index = 0;
    int end = 0;
     for (int i = 0; i< PropArray.length; i++) {
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
                end = (index+i)/2;
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
    public double getMinRT() {
        return adduct.getMinRT();
    }

    /**
     * @return the maxRT
     */
    public double getMaxRT() {
        return adduct.getMaxRT();
    }

    /**
     * @return the minMZ
     */
    public double getMinMZ() {
        return adduct.getMinMZ();
    }

    /**
     * @return the maxMZ
     */
    public double getMaxMZ() {
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
    
    double shiftedMZ = adduct.getMZ()-adduct.getMZ()/1000000*file.getMzshift();
    double maxMZ = shiftedMZ+shiftedMZ/1000000*adduct.getSession().getSliceMZTolerance();
    double minMZ = shiftedMZ-shiftedMZ/1000000*adduct.getSession().getSliceMZTolerance();
    
    
    for (int i =0; i<massList.size(); i++) {
        
       float intensity = 0;
       
       float mz =0;
       if (massList.get(i)<=maxMZ&&massList.get(i)>=minMZ){
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
    public double getRT() {
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

    
    public void generateInterpolatedEIC() {

        
        int resolution = adduct.getSession().getResolution();
       

        
       generateIntensityFunction();
        
        setIntensityArray(new double[resolution]);
        setMZArray(new double[resolution]);
       
      
     
      
      //fill Intensity Array
      for (int i = 0; i< resolution; i++) {
           try { getIntensityArray()[i]=getIntensityFunction().value(getRTArray()[i]);
            }
           catch (OutOfRangeException e) {
               getIntensityArray()[i] = 0;
               
           }
      }
      
      //fill MZArray
      //get half of delta
      double RTdeltah = (adduct.getOGroupObject().getRTArray()[1]-adduct.getOGroupObject().getRTArray()[0])/2;
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
              }
              MZArray[RT]+=massList.get(i);
              values++;  
          }

      }
      
      if (values>0) {
                      MZArray[RT]=MZArray[RT]/values;
                  }
   
    
     
     //delete originals
    this.intensityList = null;
    this.intensityFunction = null;
    this.massList = null;
    this.retentionTimeList = null;
     
    }

    /**
     * @return the RTArray
     */
    public double[] getRTArray() {
        return adduct.getRTArray();
    }

 

    /**
     * @return the IntensityArray
     */
    public double[] getIntensityArray() {
        return IntensityArray;
    }

    /**
     * @param IntensityArray the IntensityArray to set
     */
    public void setIntensityArray(double[] IntensityArray) {
        this.IntensityArray = IntensityArray;
    }

    

   
    
    public void generateRefPeak () {
        double quality = 0;
        
        int resolution = this.getIntensityArray().length;
        int middle = resolution/2;
        double[] smooth = this.getIntensityArray().clone();
        
        
        CurveSmooth csm = new CurveSmooth(smooth);
       smooth = csm.savitzkyGolay(50);
        
        
        
        
        //smooth = movingAverageSmooth(smooth);
        
        
        
        //look for highest point in the middle +-10%
        double max = smooth[middle];
        int peakint = middle;
        for (int i = 0; i < resolution/10; i++) {
            //look left and right
            if (max < smooth[middle+i]) {
                max = smooth[middle+i];
                peakint = middle+i;
            }
            if (max < smooth[middle-i]) {
                max = smooth[middle-i];
                peakint = middle-i;
            }
        }
  
        //if peak found, check the range of the peak
        int end = peakint;
        int start = peakint;
        
            //look right while the slope is steep enough
            double difend=0;
            while (end < resolution - 2 && (smooth[end]-smooth[end+1]>=difend)) {
                difend = (smooth[end]-smooth[end+1])*0.8;
                if (difend < 0) {
                    difend = 0;                   
                }
                end = end + 1;
            }
            
            //look left while the slope is steep enough
            double difstart = 0;
            while (start > 1 && (smooth[start]-smooth[start-1]>=difstart)) {
                difstart = (smooth[start]-smooth[start-1])*0.8;
                if (difstart < 0) {
                    difstart = 0;                   
                }
                start = start - 1;

            }

            
            //calculate quality
            //heigth quality
            double height = 0;
            if (this.getIntensityArray()[peakint] > 500000) {
                height = 1;
            } else if (this.getIntensityArray()[peakint] < 5000) {
                height = 0;
            } else {
                height = Math.log10(this.getIntensityArray()[peakint]) / Math.log10(500000);
            }

            //width quality
            double width = 0;
            if (end - start < 5 || end-peakint<2 || peakint-start<2) {
                width = 0;
            } else if (this.getRTArray()[end] - this.getRTArray()[start] < 0.6) {
                width = 1;
            } else {
                width = 0.6/(this.getRTArray()[end] - this.getRTArray()[start]);
            }
            
            //heigth above baseline
            double heightabove = 0;
            double bheigth = (this.getIntensityArray()[start]+this.getIntensityArray()[end])/2;
            if (bheigth/this.getIntensityArray()[peakint]<=0.2) {
                heightabove = 1;
            } else { 
                heightabove = 1- (bheigth/this.getIntensityArray()[peakint]);
            }
                
                
                quality = height*width*heightabove;
                
                
                
                
                
                
                
                
                
        }
    
     public double[] movingAverageSmooth(double[] smooth) {
        int resolution = smooth.length;
        double[] construction = new double[resolution];
        
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
         double step = this.adduct.getMaxMZ()-this.adduct.getMinMZ();
         step = step/file.getMzbins().length;
         Bins = new double[file.getMzbins().length];
         
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
    
    public void storeIntensityList() {
        try
      {
         FileOutputStream fileOut =
         new FileOutputStream(name);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.IntensityArray);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in " + name);
      }catch(IOException i)
      {
          i.printStackTrace();
      }
        
    }
    
    public void loadIntensityList() {
        try
      {
         FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
         ObjectInputStream in = new ObjectInputStream(fileIn);
         this.intensityList = (List<Float>) in.readObject();
         in.close();
         fileIn.close();
      }catch(IOException i)
      {
         i.printStackTrace();
         return;
      }catch(ClassNotFoundException c)
      {
         System.out.println("List class not found");
         c.printStackTrace();
         return;
      }
      System.out.println("Deserialized List...");
        
    }

    /**
     * @return the MZArray
     */
    public double[] getMZArray() {
        return MZArray;
    }

    /**
     * @param MZArray the MZArray to set
     */
    public void setMZArray(double[] MZArray) {
        this.MZArray = MZArray;
    }
    
    public Integer setFittedPeak(int shift) {
        //TODO: range as function of RTTolerance
        fittedpeak = null;
        
        
        //first step is to find the peak
        //get maximum range
        if (listofPeaks != null) {
        int min = adduct.getSession().getIntPeakRTTol();
        for (int i = 0; i<listofPeaks.size(); i++) {
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
                        setScorepeakclose((1.0/(double)(adduct.getSession().getIntPeakRTTol()+range+1))*min);
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
    public Double getfittedArea() {
        if (fittedpeak == null) {
            return (null);
        } else {
            return listofPeaks.get(fittedpeak).getArea();
        }  
    }

    /**
     * @return the scorepeakfound
     */
    public double getScorepeakfound() {
        if (fittedpeak!=null) {
            return 1;
        } else {
            return 0;
        }
        
    }

    

    /**
     * @return the scorepeakclose
     */
    public double getScorepeakclose() {
        return scorepeakclose;
    }

    /**
     * @param scorepeakclose the scorepeakclose to set
     */
    public void setScorepeakclose(double scorepeakclose) {
        this.scorepeakclose = scorepeakclose;
    }

    double getScoredistance() {
        if (fittedpeak == null) {
            return 0;
        } else {
            //System.out.println((Math.abs((double)adduct.getOGroupObject().getOGroupFittedShift(file)-listofPeaks.get(fittedpeak).getIndex()))/(double)file.getSession().getIntPeakRTTol());
            return 1-((Math.abs((double)adduct.getOGroupObject().getOGroupFittedShift(file)-listofPeaks.get(fittedpeak).getIndex()))/(double)file.getSession().getIntPeakRTTol());
            
        }
    }
    
    //returns list filled with indexes of peaks
    public List<Integer> getPeakIndex() {
        List<Integer> list = new ArrayList<>();
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
    
    public XYChart.Series manualPeak(int start, int end) {
        
        //Peak picking
        //takes intensity at start and end as a "baseline", subtracts the value of a line between those points from every intensity
        double delta = (IntensityArray[end]-IntensityArray[start])/(end-start);
        double current = IntensityArray[start];
        
        List<Double> intensity = new ArrayList<>();
        for (int i = start; i<=end; i++) {
            intensity.add(IntensityArray[i]-current);
            current = current + delta;
        }
        double max = 0;
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
        if (max<=0 || IntensityArray[start+maxint]<adduct.getSession().getBaseline()) {
            return null;
        }
        
        
        
        int index = maxint+start;
        
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
        
        double[] RTArray = adduct.getRTArray();
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getStart()], 1.2));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getStart()], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getStart()], 1.17));
                newSeries.getData().add(new XYChart.Data(RTArray[newPeak.getIndex()], 1.05));
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
    
}