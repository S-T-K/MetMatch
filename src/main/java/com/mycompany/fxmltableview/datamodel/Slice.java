/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;


import flanagan.analysis.CurveSmooth;
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
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author stefankoch
 */
public class Slice {
    
    private RawDataFile file;
    
    private List<Float> retentionTimeList = new ArrayList<Float>();
    private List<Float> intensityList = new ArrayList<Float>();
    private List<Float> massList = new ArrayList<Float>();
    private PolynomialSplineFunction intensityFunction;
    private float minIntensity;
    private float maxIntensity;
   
    private Entry adduct;
    private Dataset dataset;
    
    
    private double[] IntensityArray;
    private double[] NormIntensityArray;
    private double[] PropArray;
    
    public Slice(RawDataFile file, Entry adduct) {
        this.file = file;
        this.dataset = file.getDataset();
        this.adduct = adduct;
        
        
    }
    

    
    public void extractSlicefromScans(List<Scan> listofScans) {
         //for all Scans
        for (int i = 0; i< listofScans.size(); i++) {
            //if RT is within tolerance
            boolean found;
            setMinIntensity(900000000);
            setMaxIntensity(0);
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
                                if (listofScans.get(i).getMassovercharge()[l]>getMaxIntensity()) {
                                    setMaxIntensity(listofScans.get(i).getMassovercharge()[l]);
                                }
                                if (listofScans.get(i).getMassovercharge()[l] < getMinIntensity()) {
                                    setMinIntensity(listofScans.get(i).getMassovercharge()[l]);
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
      
     this.clean();
     this.generateInterpolatedEIC();
     
    }
    
    //not yet working...
    public void binaryExtractSlicefromScans(List<Scan> listofScans) {
         //for all Scans
         int start = 0;
         int end = listofScans.size()-1;
         int middle = end/2;
         boolean found = false;
         
         while (!found) {
             if (listofScans.get(middle).getRetentionTime() < getMinRT()) {
                 start = middle +1;
             } else if (listofScans.get(middle).getRetentionTime() > getMaxRT()) {
                 end = middle-1;
             } else {
                 found = true;
             }
             middle = (start+end)/2;
         }
         
         start = middle;
         end = middle;
         while (listofScans.get(start).getRetentionTime()>getMinRT()) {
             start--;
         }
         start++;
         
         while (listofScans.get(end).getRetentionTime()<getMaxRT()) {
             end++;
         }
         end--;
         
         for (int i = start; i<= end; i++) {
             found = false;
             float currentRT = listofScans.get(i).getRetentionTime();
                        
             int startp = 0;
             int endp = listofScans.get(i).getPeakscount();
             middle = endp/2;
             
             while ((startp<endp) && !found) {
                 if (listofScans.get(i).getMassovercharge()[middle] < getMinMZ()) {
                     startp = middle+1;
                 } else if (listofScans.get(i).getMassovercharge()[middle] > getMaxMZ()) {
                     endp = middle-1;
                 } else {
                     found = true;
                 }
                 middle = (startp+endp)/2;
             }
             startp = middle;
             endp = middle;
             
             
             if (found) {
                 while (listofScans.get(i).getMassovercharge()[startp]>getMinMZ()) {
                     getRetentionTimeList().add(currentRT);
                     getIntensityList().add(listofScans.get(i).getIntensity()[startp]);
                     getMassList().add(listofScans.get(i).getMassovercharge()[startp]);
                     startp--;
                 }
                 while (listofScans.get(i).getMassovercharge()[endp]<getMaxMZ()) {
                     getRetentionTimeList().add(currentRT);
                     getIntensityList().add(listofScans.get(i).getIntensity()[endp]);
                     getMassList().add(listofScans.get(i).getMassovercharge()[endp]);
                     endp++;
                     
                 }
                     
                 
             } else {
             getRetentionTimeList().add(currentRT);
                            getIntensityList().add(0.0f);
                            getMassList().add(0.0f);
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
    
    //generates Array filled with probabilities, correspond to the probabiltiy of a guassian peak at this RT
 public void generateGaussProp() {
         //initialize Array holding probabilities
        setPropArray(new double[this.IntensityArray.length]);
         double[] peakArray = {0.30562389380800614, 0.4045593930181101, 0.5010142078557377, 0.5697809675082599, 0.7126271863152996, 0.7675927216635093, 0.8845355890078511, 0.9218788811348794, 0.9336462411287345, 1.0, 0.9712913331424721, 0.7660152062379959, 0.7391207258124926, 0.6103812352993977, 0.47315901034928215, 0.4162911178032002, 0.30054754596741007}; 
         int peakint = 9;

         
         
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
                getPropArray()[i+peakint]= corr*Math.log10(maxIntensity);
        
        }
        //generatePeakArray();
    }
 
 //generates Array filled with Peak probabilites
 //TODO: negative Maxima
 public void generatePeakArray() {
     CurveSmooth csm = new CurveSmooth(PropArray);
     double[][] maxima = csm.getMaximaUnsmoothed();
     
     
     
     
     //delete array except for region around maxima
     int current = 0;
     for (int i = 0; i<maxima[0].length; i++) {
         while (current < PropArray.length && current<maxima[0][i]-1) {
             PropArray[current] = 0;
             current++;
         }
         current = current+3;
     }
     while (current<PropArray.length) {
         PropArray[current] = 0;
             current++;
     }
 }

    /**
     * @return the PropArray
     */
    public double[] getPropArray() {
        return PropArray;
    }

    /**
     * @param PropArray the PropArray to set
     */
    public void setPropArray(double[] PropArray) {
        this.PropArray = PropArray;
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
        setNormIntensityArray(new double[resolution]);
      
     
      
      //fill Arrays
      for (int i = 0; i< resolution; i++) {
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
    
}