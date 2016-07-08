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
    
    private int[] xRanges;
    private int[] yRanges;
    
    Session session;
    
    public GravityCalculator(Session session) {
        this.session=session;
        xRanges = new int[9];
        yRanges = new int[9];
        xRanges[0]=1000;
        xRanges[1]=100;
        xRanges[2]=10;
        yRanges[0]=100;
        yRanges[1]=20;
        yRanges[2]=3;
    }

    /**
     * @return the xRanges
     */
    public int[] getxRanges() {
        return xRanges;
    }

    /**
     * @param xRanges the xRanges to set
     */
    public void setxRanges(int[] xRanges) {
        this.xRanges = xRanges;
    }

    /**
     * @return the yRanges
     */
    public int[] getyRanges() {
        return yRanges;
    }

    /**
     * @param yRanges the yRanges to set
     */
    public void setyRanges(int[] yRanges) {
        this.yRanges = yRanges;
    }
    
     public float[] gravity(int count, float[][] matrix, float[] centroids) throws InterruptedException {
        
        long[] counts = new long[centroids.length];
         for (int i = 0; i < counts.length; i++) {
             counts[i] = 1;
         }

         float[] ncentroids = new float[centroids.length];
         for (int i = 0; i < centroids.length; i++) {
             ncentroids[i] = centroids[i];
         }
         int step = (int) ((double) xRanges[count] / 10.0);
         if (step < 1) {
             step = 1;
         }
         for (int i = 0; i < centroids.length; i = i + step) {

             int xstart = i - xRanges[count];
             if (xstart < 0) {
                 xstart = 0;
             }
             int xend = i + xRanges[count];
             if (xend > centroids.length - 1) {
                 xend = centroids.length - 1;
             }
             int ystart = (int) centroids[i] - yRanges[count];
             if (ystart < 0) {
                 ystart = 0;
             }
             int yend = (int) centroids[i] + yRanges[count];
             if (yend > session.getResolution() - 1) {
                 yend = session.getResolution() - 1;
             }

             int maxint = -1;
             float max = 0;
             for (int l = ystart; l <= yend; l++) {
                 float nmax = 0;
                 for (int j = xstart; j <= xend; j++) {
                     for (int k = ystart; k <= yend; k++) {
                         float distance = Math.abs(k - l) + 1;
//                         if (matrix[j][k]>100) {
//                         System.out.println();
//                         }
//TODO: other distance penalties than square
                         nmax += matrix[j][k] / ((distance * distance));
                     }
                 }
                 if (nmax > max) {
                     max = nmax;
                     maxint = l;
                 }
             }
             if (maxint > -1) {
                 for (int l = xstart; l <= xend; l++) {
                     float xdistance = xRanges[count] + 1 - (Math.abs(i - l));
                     xdistance /= xRanges[count];
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
         alert.setContentText("Only very few peaks could be used in calculating the retention time shift function. This indicated that the file contains very few of the metabolites specified in the reference data matrix.\n\nPlease verify the correctness of the results!");
         alert.showAndWait();
                        }});
         
         
         }
     }
     
}
