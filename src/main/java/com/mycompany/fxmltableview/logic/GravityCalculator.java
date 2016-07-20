/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import com.mycompany.fxmltableview.datamodel.RawDataFile;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author stefankoch
 */
public class GravityCalculator {
    
    private double[] xRanges;
    private double[] yRanges;
    
    Session session;
    
    public GravityCalculator(Session session) {
        this.session=session;
        xRanges = new double[9];
        yRanges = new double[9];
        xRanges[0]=65;
        xRanges[1]=6.5;
        xRanges[2]=0.65;
        yRanges[0]=100;
        yRanges[1]=20;
        yRanges[2]=3;
    }

    /**
     * @return the xRanges
     */
    public double[] getxRanges() {
        return xRanges;
    }

    /**
     * @param xRanges the xRanges to set
     */
    public void setxRanges(double[] xRanges) {
        this.xRanges = xRanges;
    }

    /**
     * @return the yRanges
     */
    public double[] getyRanges() {
        return yRanges;
    }

    /**
     * @param yRanges the yRanges to set
     */
    public void setyRanges(double[] yRanges) {
        this.yRanges = yRanges;
    }
    
     public float[] gravity(int count, float[][] matrix, float[] centroids) throws InterruptedException {
        
         int absolutex = (int) (xRanges[count]/100*matrix.length);
         int absolutey = (int) (yRanges[count]/100*matrix[0].length);
        
        long[] counts = new long[centroids.length];
         for (int i = 0; i < counts.length; i++) {
             counts[i] = 1;
         }

         //copy centroids. Otherwise results from one iteration would be used within the same iteration
         float[] ncentroids = new float[centroids.length];
         for (int i = 0; i < centroids.length; i++) {
             ncentroids[i] = centroids[i];
         }
         
         //don't calcualte every step for big xRanges, => increase performance
         int step = (int) ((double) xRanges[count] / 10.0);
         if (step < 1) {
             step = 1;
         }
         
         //for every OGroup ordered by RT (skip by step if xRange is big)
         for (int i = 0; i < centroids.length; i = i + step) {

             //check starts and ends
             int xstart = i - absolutex;
             if (xstart < 0) {
                 xstart = 0;
             }
             int xend = i + absolutex;
             if (xend > centroids.length - 1) {
                 xend = centroids.length - 1;
             }
             int ystart = (int) centroids[i] - absolutey;
             if (ystart < 0) {
                 ystart = 0;
             }
             int yend = (int) centroids[i] + absolutey;
             if (yend > session.getResolution() - 1) {
                 yend = session.getResolution() - 1;
             }

             //maxint is the y-position of the maximum
             //max is the max proximity along the yRange
             int maxint = -1;
             float max = 0;
             //for the whole yRange
             for (int l = ystart; l <= yend; l++) {
                 //nmax sums up the current y-position
                 float nmax = 0;
                 //for the current y-position, calculate proximity to all peaks within xRange and yRange
                 for (int j = xstart; j <= xend; j++) {
                     for (int k = ystart; k <= yend; k++) {
                         float distance = Math.abs(k - l) + 1;
                         //weight*distance squared, to prefer clusters to averages
                         nmax += matrix[j][k] / ((distance * distance));
                     }
                 }
                 if (nmax > max) {
                     max = nmax;
                     maxint = l;
                 }
             }
             if (maxint > -1) {
                 //use max for every centroid within the xRange
                 for (int l = xstart; l <= xend; l++) {
                     //calculate distance to current centroid, distance of 1 = no distance, distance of 0 = maxdistance+1
                     //each value is weighted according to its distance, and the result should be the weighted average of all values
                     //to avoid keeping a list, the average is updated continuously, which requires to keep track of the current sum of all weights = counts
                     float xdistance = absolutex + 1 - (Math.abs(i - l));
                     //normalize distance
                     xdistance /= (xRanges[count]+1);
                     //the weight is the sum of the proximity value and the distance
                     ncentroids[l] = (ncentroids[l] * counts[l] + (maxint * max * xdistance)) / (counts[l] + (max * xdistance));
                     counts[l] += max * xdistance;
                 }
             }
         }
       
        
       
        return ncentroids;
    }
     
     //function to test whether there is some sort of global maximum
     public void qualitycheck(float[][] matrix, float[] centroids, RawDataFile file) {
         //get weigtht sum of one peak
         float[][] pmatrix = new float[1][session.getResolution()];
         session.getProparraycalculator().calculate(10, 1, 10, pmatrix, 0);
         float w = 0;
         for (int i = 0; i<session.getResolution(); i++) {
             w+=pmatrix[0][i];
         }
         System.out.println(w);
         
         //get 10% range
         int range = (int) (matrix[0].length*0.05);
         
         //wf contains the weights along the fit, while wr contains the weights outside the fit
         float wf = 0f;
         float wr = 0f;
         for (int i = 0; i<matrix.length; i++) {
             int upperlimit = (int) (session.getResolution()*0.90);
             int limit1=matrix[i].length;
             if (limit1>=upperlimit) {
                 limit1=upperlimit;
             }
             for (int j = (int) (0+session.getResolution()*0.1); j<limit1; j++) {
                 wr+=matrix[i][j];  
             }
             int limit = (int) (centroids[i]+range);
             if (limit>=upperlimit) {
                 limit = upperlimit;
             }
             for (int j = (int) (centroids[i]-range); j<=limit; j++) {
                 wf+=matrix[i][j];
             }
             
             for (int j = (int) (centroids[i]+range+1); j<upperlimit; j++) {
                 wr+=matrix[i][j];
             }
         }
   
         
         
         System.out.println(file.getName() + ": anchor peaks: " + (wf-(wr/7))/w );
         
         if ((wf-(wr/7))/w<50) {
               Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                           Alert alert = new Alert(AlertType.WARNING, "");
         alert.setTitle("Warning");
         alert.setHeaderText(file.getName() + ": Very few peaks detected!");
         alert.setContentText("Only very few peaks could be used in calculating the retention time shift function. This indicates that the file contains very few of the metabolites specified in the reference data matrix or that the retention time shift function is far from optimal.\n\nPlease verify the correctness of the results!");
         alert.showAndWait();
                        }});
         
         
         }
     }
     
}
