/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

/**
 *
 * @author stefankoch
 */
public class Adduct {
    //represents a single row
    
    
    private int Num;
    private double MZ;
    private double RT;
    private int Xn;
    private int OGroup;
    private String Ion;
    private double M;
    private OGroup OGroupObject;
    
    public Adduct(int Num, double MZ, double RT, int Xn, int OGroup, String Ion, double M) {
        this.Num = Num;
        this.MZ = MZ;
        this.RT = RT;
        this.Xn = Xn;
        this.OGroup = OGroup;
        this.Ion = Ion;
        this.M = M;
        
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
     * @return the MZ
     */
    public double getMZ() {
        return MZ;
    }

    /**
     * @param MZ the MZ to set
     */
    public void setMZ(double MZ) {
        this.MZ = MZ;
    }

    /**
     * @return the RT
     */
    public double getRT() {
        return RT;
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(double RT) {
        this.RT = RT;
    }

    /**
     * @return the Xn
     */
    public int getXn() {
        return Xn;
    }

    /**
     * @param Xn the Xn to set
     */
    public void setXn(int Xn) {
        this.Xn = Xn;
    }

    /**
     * @return the OGroup
     */
    public int getOGroup() {
        return OGroup;
    }

    /**
     * @param OGroup the OGroup to set
     */
    public void setOGroup(int OGroup) {
        this.OGroup = OGroup;
    }

    /**
     * @return the Ion
     */
    public String getIon() {
        return Ion;
    }

    /**
     * @param Ion the Ion to set
     */
    public void setIon(String Ion) {
        this.Ion = Ion;
    }

    /**
     * @return the M
     */
    public double getM() {
        return M;
    }

    /**
     * @param M the M to set
     */
    public void setM(double M) {
        this.M = M;
    }

    /**
     * @return the OGroupObject
     */
    public OGroup getOGroupObject() {
        return OGroupObject;
    }

    /**
     * @param OGroupObject the OGroupObject to set
     */
    public void setOGroupObject(OGroup OGroupObject) {
        this.OGroupObject = OGroupObject;
    }
    
    
    
}
