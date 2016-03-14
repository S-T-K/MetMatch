/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

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

/**
 *
 * @author stefankoch
 */
public class Slice {
    
    private RawDataFile file;
    private int Num; //Num from Intput Matrix, each adduct has its own Num
    private float minRT, maxRT, RT;
    private float minMZ, maxMZ;
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
    private PolynomialSplineFunction intensityFunction;
   
    
    private Peak peak;
    //for Batch Slices, to be compared with the reference AVGEIC 
    private double[] RTArray ;
    private double[] IntensityArray;
    private double[] NormIntensityArray;
    
    public Slice(RawDataFile file, int Num, float MZ, float MZTolerance, float RT, float RTTolerance) {
        this.file = file;
        this.Num=Num;
        this.minRT = RT-RTTolerance;
        this.maxRT = RT+RTTolerance;
        this.minMZ = MZ-MZTolerance;
        this.maxMZ = MZ+MZTolerance;
        this.RT = RT;
        
        
    }
    

    
    public void extractSlicefromScans(List<Scan> listofScans) {
         //for all Scans
        for (int i = 0; i< listofScans.size(); i++) {
            //if RT is within tolerance
            boolean found;
            float currentRT = listofScans.get(i).getRetentionTime();
            
            //0.05 so that ranges for the interpolation are smaller than the actual ranges, otherwise out of range
        if (currentRT>= (getMinRT()) && currentRT<= (getMaxRT())) {
            
                        found = false;
                        
                        
                        //TODO binary search!!!!!!
                        for (int l=0; l<listofScans.get(i).getPeakscount(); l++) {
                            if (listofScans.get(i).getMassovercharge()[l] >= getMinMZ() && listofScans.get(i).getMassovercharge()[l] <= getMaxMZ()) {
                                getRetentionTimeList().add(currentRT);
                                getIntensityList().add(listofScans.get(i).getIntensity()[l]);
                                getMassList().add(listofScans.get(i).getMassovercharge()[l]);
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
      
     this.clean();
     this.generateInterpolatedEIC();
     
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
        return minRT;
    }

    /**
     * @return the maxRT
     */
    public float getMaxRT() {
        return maxRT;
    }

    /**
     * @return the minMZ
     */
    public float getMinMZ() {
        return minMZ;
    }

    /**
     * @return the maxMZ
     */
    public float getMaxMZ() {
        return maxMZ;
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
    public void clean() {
        
    List<Float> newRTList = new ArrayList<>();
    List<Float> newIntList= new ArrayList<>();
    List<Float> newMZList= new ArrayList<>();
    
    
    for (int i =0; i<massList.size(); i++) {
       float intensity = intensityList.get(i);
       
       float mz = massList.get(i);
       while (i<retentionTimeList.size()-1 && abs(retentionTimeList.get(i)-retentionTimeList.get(i+1))<0.001) {
           if (intensityList.get(i+1)> intensity) {
               intensity = intensityList.get(i+1);
               mz = massList.get(i+1);
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
        return Num;
    }

    /**
     * @param Num the Num to set
     */
    public void setNum(int Num) {
        this.Num = Num;
    }

    /**
     * @return the RT
     */
    public float getRT() {
        return RT;
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(float RT) {
        this.RT = RT;
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

        
        int resolution = 100;  
        double startRT = this.minRT+0.05;
        double endRT = this.maxRT-0.05;
       

        
       generateIntensityFunction();
        
        setRTArray(new double[resolution]);
        setIntensityArray(new double[resolution]);
        setNormIntensityArray(new double[resolution]);
      
     
      
      //fill Arrays
      for (int i = 0; i< resolution; i++) {
            getRTArray()[i] = startRT+(((endRT-startRT))/(resolution-1))*i;
            getIntensityArray()[i]=getIntensityFunction().value(getRTArray()[i]);

      }
    
     double[] MaxArray  = Arrays.copyOf(getIntensityArray(), resolution);
     Arrays.sort(MaxArray);
     
     for (int i = 0; i< resolution; i++) {
            getNormIntensityArray()[i] = getIntensityArray()[i]/MaxArray[resolution-1];
         
     }
      
    }

    /**
     * @return the RTArray
     */
    public double[] getRTArray() {
        return RTArray;
    }

    /**
     * @param RTArray the RTArray to set
     */
    public void setRTArray(double[] RTArray) {
        this.RTArray = RTArray;
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

    /**
     * @return the NormIntensityArray
     */
    public double[] getNormIntensityArray() {
        return NormIntensityArray;
    }

    /**
     * @param NormIntensityArray the NormIntensityArray to set
     */
    public void setNormIntensityArray(double[] NormIntensityArray) {
        this.NormIntensityArray = NormIntensityArray;
    }

    /**
     * @return the peak
     */
    public Peak getPeak() {
        return peak;
    }

    /**
     * @param peak the peak to set
     */
    public void setPeak(Peak peak) {
        this.peak = peak;
    }
    
    public void generateRefPeak () {
        double quality = 0;
        
        int resolution = this.getIntensityArray().length;
        int middle = resolution/2;
        double[] smooth = this.getIntensityArray().clone();
        
        
        //CurveSmooth csm = new CurveSmooth(smooth);
       //smooth = csm.savitzkyGolay(25);
        
        
        
        
        smooth = movingAverageSmooth(smooth);
        
        
        
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
                
                this.setPeak(new Peak(peakint, start, end, this.getIntensityArray(), this.getRTArray()));
                
                
                
                //Test: Delete everything but peak
                for (int i = 0; i< start; i++) {
                    this.getIntensityArray()[i] = -1.0;
                }
                
                for (int i = end; i< resolution; i++) {
                    this.getIntensityArray()[i] = -1.0;
                }
                
                
                
                this.getPeak().setQuality(quality);
        }
    
     public double[] movingAverageSmooth(double[] smooth) {
        int resolution = smooth.length;
        double[] construction = new double[resolution];
        
        //smoothing
        //we want 5% windows around each point, minimum 1 point
        int range = (int) Math.ceil(resolution/40);
        
       System.out.println("Range: " + (range*2+1));
        
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
    
}