package com.mycompany.fxmltableview;

// class that holds the information of a scan
public class Scan {

    private int number;
    private int peakscount;
    private float retentionTime;
    private float[] massovercharge;
    private float[] intensity;
    private String scanType;
    private int msLevel;
    private char polarity;
    private int msInstrumentID;

    public Scan() {

    }

    public Scan(int number, int peakscount, float retentionTime, float[] massovercharge, float[] intensity, String scanType, int msLevel, char polarity, int instrumentID) {
        this.number = number;
        this.peakscount = peakscount;
        this.retentionTime = retentionTime;
        this.massovercharge = massovercharge; 
        this.intensity = intensity;
        this.scanType = scanType;
        this.msLevel = msLevel;
        this.polarity = polarity;
        this.msInstrumentID = instrumentID;
    }

//-----------------------------------------------------------------------------
// Getters/Setters       
    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the peakscount
     */
    public int getPeakscount() {
        return peakscount;
    }

    /**
     * @param peakscount the peakscount to set
     */
    public void setPeakscount(int peakscount) {
        this.peakscount = peakscount;
    }

    /**
     * @return the retentionTime
     */
    public float getRetentionTime() {
        return retentionTime;
    }

    /**
     * @param retentionTime the retentionTime to set
     */
    public void setRetentionTime(int retentionTime) {
        this.retentionTime = retentionTime;
    }


    /**
     * @return the scantype
     */
    public String getScanType() {
        return scanType;
    }

    /**
     * @param scantype the scantype to set
     */
    public void setScanType(String scantype) {
        this.scanType = scantype;
    }

    /**
     * @return the msLevel
     */
    public int getMsLevel() {
        return msLevel;
    }

    /**
     * @param msLevel the msLevel to set
     */
    public void setMsLevel(int msLevel) {
        this.msLevel = msLevel;
    }

    /**
     * @return the polarity
     */
    public char getPolarity() {
        return polarity;
    }

    /**
     * @param polarity the polarity to set
     */
    public void setPolarity(char polarity) {
        this.polarity = polarity;
    }

    /**
     * @return the instrumentID
     */
    public int getMsInstrumentID() {
        return msInstrumentID;
    }

    /**
     * @param instrumentID the instrumentID to set
     */
    public void setMsInstrumentID(int instrumentID) {
        this.msInstrumentID = instrumentID;
    }

    /**
     * @return the massovercharge
     */
    public float[] getMassovercharge() {
        return massovercharge;
    }

    /**
     * @param massovercharge the massovercharge to set
     */
    public void setMassovercharge(float[] massovercharge) {
        this.massovercharge = massovercharge;
    }

    /**
     * @return the intensity
     */
    public float[] getIntensity() {
        return intensity;
    }

    /**
     * @param intensity the intensity to set
     */
    public void setIntensity(float[] intensity) {
        this.intensity = intensity;
    }

}
