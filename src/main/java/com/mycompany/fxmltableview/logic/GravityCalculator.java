/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;

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
        
        float[] counts = new float[centroids.length];
        for (int i = 0; i<counts.length; i++) {
            counts[i]=1;
        }
        
        
         float[] ncentroids = new float[centroids.length];
        for (int i = 0; i<centroids.length; i++) {
            ncentroids[i]=centroids[i];
        }
      int step = (int) ((double)xRanges[count]/10.0);
      if (step<1) {
          step=1;
      }
        for (int i = 0; i<centroids.length; i=i+step) {
            
            int xstart = i-xRanges[count];
            if (xstart<0) {
                xstart = 0;
            }
            int xend = i+xRanges[count];
            if (xend>centroids.length-1) {
                xend = centroids.length-1;
            }
            int ystart = (int) centroids[i]-yRanges[count];
            if (ystart<0) {
                ystart = 0;
            }
            int yend = (int) centroids[i]+yRanges[count];
            if (yend>session.getResolution()-1) {
                yend = session.getResolution()-1;
            }
            
            int maxint = -1;
            float max = 0;
            for (int l = ystart; l<=yend; l++) {
                float nmax = 0;
            for (int j = xstart; j<=xend; j++) {
                for (int k = ystart; k<=yend; k++) {
                    float distance = Math.abs(k-l)+1;
                    nmax+=matrix[j][k]*(1/(distance*distance));
                }
            }
            if (nmax>max) {
                max =nmax;
                maxint = l;
            }
            }
            if (maxint>-1) {
            for (int l = xstart; l<=xend; l++) {
                ncentroids[l] = (ncentroids[l]*counts[l]+maxint*1)/(counts[l]+1);
                counts[l]++;
            }
            }
        }
       
        
       
        return ncentroids;
    }
}
