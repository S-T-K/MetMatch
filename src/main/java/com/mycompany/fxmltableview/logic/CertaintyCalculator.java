/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author stefankoch calculates certainty of fittedshift the penalty value that
 * has to be added to all fittedshifts for the fittedshift of interest to change
 */
public class CertaintyCalculator {

    private Session session;

    public CertaintyCalculator(Session session) {
        this.session = session;

    }

    public void calculate() {
        
        float cert = 0.05f; //value that get's subtracted from original/iteration (from theoretical 1)
        //penalty for change in position
        float distpen = 1.0f/session.getResolution();
        
        for (int i = 0; i < session.getListofOGroups().size(); i++) {
                            //delete old certainties
                            session.getListofOGroups().get(i).setCertainties(new HashMap<RawDataFile,Float>());                
        }
        
        for (int d = 0; d < session.getListofDatasets().size(); d++) {
            if (session.getListofDatasets().get(d).getActive()) {
                for (int f = 0; f < session.getListofDatasets().get(d).getListofFiles().size(); f++) {
                    RawDataFile currentfile = session.getListofDatasets().get(d).getListofFiles().get(f);
                    if (currentfile.getActive().booleanValue()) {

                        //build original weight matrix
                        Collections.sort(session.getListofOGroups(), new Entry.orderbyRT());
                        float[][] matrix = new float[session.getListofOGroups().size()][session.getResolution()];

                        for (int i = 0; i < session.getListofOGroups().size(); i++) {   
                            //add best possible certainty 
                            session.getListofOGroups().get(i).getCertainties().put(currentfile, 1.0f);
                            
                            float[] PropArray = session.getListofOGroups().get(i).getOGroupPropArraySmooth(currentfile);
                            for (int j = 0; j < session.getResolution(); j++) {
                                matrix[i][j] = PropArray[j];
                            }
                        }

                        //iterate
                        for (int p = 0; p < 20; p++) {
                            

                            //add penalty to fittedpeaks
                            //subtracts p*cert*value from value in original matrix, linear penalty
                            for (int i = 0; i < session.getListofOGroups().size(); i++) {
                                int start = session.getListofOGroups().get(i).getOGroupFittedShift(currentfile)-session.getIntPeakRTTol();
                                int end = session.getListofOGroups().get(i).getOGroupFittedShift(currentfile)+session.getIntPeakRTTol();
                                if (start<0) {
                                    start = 0;
                                }
                                if (end>=session.getResolution()) {
                                    end = session.getResolution()-1;
                                }
                                for (int t = start; t<=end; t++) {
                                    matrix[i][t] = matrix[i][t]*(1.0f/(1.0f-p*cert))*(1-(p+1)*cert) ;
                                }
                            }
                            
                            

                            //build new weight matrix
                            float[][] weights = new float[session.getListofOGroups().size()][session.getResolution()];
                            //fill first row
                            for (int j = 0; j < session.getResolution(); j++) {
                                weights[0][j] = matrix[0][j];

                            }
                            //fill rest of weights matrix
                            float penalty = session.getListofDatasets().get(0).getPenalty();
                            for (int i = 1; i < session.getListofOGroups().size(); i++) {
                                for (int j = 0; j < session.getResolution(); j++) {
                                    float max = 0;
                                    if (weights[i - 1][j] > max) {
                                        max = weights[i - 1][j] + matrix[i][j];
                                    }
                                    if ((j - 1) > 0 && weights[i - 1][j - 1] + matrix[i][j] - penalty > max) {
                                        max = weights[i - 1][j - 1] + matrix[i][j] - penalty;
                                    }
                                    if ((j + 1) < session.getResolution() && weights[i - 1][j + 1] + matrix[i][j] - penalty > max) {
                                        max = weights[i - 1][j + 1] + matrix[i][j] - penalty;
                                    }
                                    weights[i][j] = max;

                                }

                            }
                            
                            //calculate path
                            //get max in last row
                                float max = 0;
                                int maxint = 0;
                                for (int j = 0; j < session.getResolution(); j++) {
                                    if (weights[session.getListofOGroups().size() - 1][j] > max) {
                                        maxint = j;
                                        max = weights[session.getListofOGroups().size() - 1][j];
                                    }
                                }

                                //TODO: calculate range as function of time
                                for (int i = session.getListofOGroups().size() - 2; i > -1; i--) {
                                    max = 0;

                                    int j = maxint;
                                    if ((j - 1) > 0 && weights[i][j - 1] > max) {
                                        max = weights[i][j - 1];
                                        maxint = j - 1;
                                    }
                                    if (weights[i][j] > max) {
                                        max = weights[i][j];
                                        maxint = j;
                                    }
                                    if ((j + 1) < session.getResolution() && weights[i][j + 1] > max) {
                                        //max = weights[i][j+1];
                                        maxint = j + 1;
                                    }
                            
                                    //check if changed
                                    int dist = Math.abs(maxint-session.getListofOGroups().get(i).getOGroupFittedShift(currentfile));
                                    if (dist>session.getIntPeakRTTol()) {
                                        //calculate certainty
                                        
                                        //first part is what fraction of the original value has been subtracted. High value = high certainty
                                        //second part is the distance from the original fittedshift, high distance = low certainty
                                        float fp = (p+1)*cert;
                                        float sp = 1-(dist*distpen);
                                        
                                        if (session.getListofOGroups().get(i).getCertainties().get(currentfile)>fp*sp) {
                                           session.getListofOGroups().get(i).getCertainties().put(currentfile,fp*sp);
                                        }
                                    }
                                        
                                    }
                            

                        }
currentfile.calculateScore();

                        
                        
                    }

                }
            }
        }

    }

}
