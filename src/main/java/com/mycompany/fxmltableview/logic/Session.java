/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import com.mycompany.fxmltableview.datamodel.Batch;
import com.mycompany.fxmltableview.datamodel.Dataset;
import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Reference;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.rosuda.JRI.Rengine;


/**
 *
 * @author stefankoch
 * 
 * Holds entire information
 */
public class Session {
   
    private List<Entry> listofOGroups;
    private Reference reference;
    private List<Batch> listofBatches;
    private float RTTolerance;
    private float MZTolerance;
    private int resolution;
    private float baseline;
    private Dataset currentdataset;
    private ObservableList<RawDataFile> selectedFiles;
    private Rengine engine;
    private double SliceMZTolerance;
    
    
    public Session() {
        this.reference= new Reference();
        this.listofBatches = new ArrayList<>();
        this.resolution = 100;
        this.baseline = 1000;
        SliceMZTolerance = 2.5;
        this.currentdataset=reference;
        engine = new Rengine(new String[] { "--no-save" }, false, null);
        engine.eval("source(\"C:/Users/stefankoch/Desktop/MassSpecWaveletIdentification.r\")");
    }

    /**
     * @return the ReferenceTsv
     */
    public File getReferenceTsv() {
        return getReference().getMatrixFile();
    }

    /**
     * @param ReferenceTsv the ReferenceTsv to set
     */
    public void setReferenceTsv(File ReferenceTsv) {
        
        getReference().setMatrixFile(ReferenceTsv);
        
    }
    
    // returns List of Ogroups, with their adducts
    public ObservableList<Entry> parseReferenceTsv() throws FileNotFoundException {
        ObservableList<Entry> obsList = FXCollections.observableArrayList();
        
        
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");

        TsvParser parser = new TsvParser(settings);
        FileReader reader = new FileReader(this.getReference().getMatrixFile());
        List<String[]> allRows = parser.parseAll(reader);
        
        //get Headers
        List<String> headers = Arrays.asList(allRows.get(0));
        int indexNum = headers.indexOf("Num");
        int indexMZ = headers.indexOf("MZ");
        int indexRT = headers.indexOf("RT");
        int indexXn = headers.indexOf("Xn");
        int indexOGroup = headers.indexOf("OGroup");
        int indexIon = headers.indexOf("Ion");
        int indexM = headers.indexOf("M");
        int Num;
        double MZ;
        double RT;
        int Xn;
        int OGroup;
        String Ion;
        double M;
        
        
        String lastOGroup = "-1";
        Entry ogroup = null;
        for (int i = 1; i < allRows.size(); i++) {
            Num = Integer.parseInt(allRows.get(i)[indexNum]);
            MZ = Double.parseDouble(allRows.get(i)[indexMZ]);
            RT = Double.parseDouble(allRows.get(i)[indexRT]);
            Xn = Integer.parseInt(allRows.get(i)[indexXn]);
            OGroup = Integer.parseInt(allRows.get(i)[indexOGroup]);
            Ion = allRows.get(i)[indexIon];
            M = parseDoubleSafely(allRows.get(i)[indexM]);
            
            //if new Ogroup, make new Ogroup
            if (!(lastOGroup.equals(allRows.get(i)[indexOGroup]))) {
                if (ogroup!=null){
                ogroup.generateRTArray();}
                ogroup = new Entry(OGroup, this);
                lastOGroup = allRows.get(i)[indexOGroup];
                obsList.add(ogroup);
            }
            //add Adduct to current Ogroup
            Entry adduct = new Entry(Num,MZ,RT,Xn,OGroup,Ion,M,this,ogroup);
            ogroup.addAdduct(adduct);
            
            
           
        }
        ogroup.generateRTArray();
        
        this.listofOGroups= obsList;
        return obsList;
        
    }
    
    public static double parseDoubleSafely(String str) {
    double result = 0;
    try {
        result = Double.parseDouble(str);
    } catch (NullPointerException | NumberFormatException npe) {
    }
    return result;
}

    /**
     * @return the reference
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }
    
    
    public void addBatch(Batch batch) {
        this.getListofBatches().add(batch);
        
    }

    /**
     * @return the listofOGroups
     */
    public List<Entry> getListofOGroups() {
        return listofOGroups;
    }

    /**
     * @return the RTTolerance
     */
    public float getRTTolerance() {
        return RTTolerance;
    }

    /**
     * @param RTTolerance the RTTolerance to set
     */
    public void setRTTolerance(float RTTolerance) {
        this.RTTolerance = RTTolerance;
    }

    /**
     * @return the MZTolerance
     */
    public float getMZTolerance() {
        return MZTolerance;
    }

    /**
     * @param MZTolerance the MZTolerance to set
     */
    public void setMZTolerance(float MZTolerance) {
        this.MZTolerance = MZTolerance;
    }

    /**
     * @return the resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * @param resolution the resolution to set
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    /**
     * @return the baseline
     */
    public float getBaseline() {
        return baseline;
    }

    /**
     * @param baseline the baseline to set
     */
    public void setBaseline(float baseline) {
        this.baseline = baseline;
    }

    /**
     * @return the currentdataset
     */
    public Dataset getCurrentdataset() {
        return currentdataset;
    }

    /**
     * @param currentdataset the currentdataset to set
     */
    public void setCurrentdataset(Dataset currentdataset) {
        this.currentdataset = currentdataset;
    }

    /**
     * @return the engine
     */
    public Rengine getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    public void setEngine(Rengine engine) {
        this.engine = engine;
    }

    /**
     * @return the SliceMZTolerance
     */
    public double getSliceMZTolerance() {
        return SliceMZTolerance;
    }

    /**
     * @param SliceMZTolerance the SliceMZTolerance to set
     */
    public void setSliceMZTolerance(double SliceMZTolerance) {
        this.SliceMZTolerance = SliceMZTolerance;
    }

    /**
     * @return the selectedFiles
     */
    public ObservableList<RawDataFile> getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * @param selectedFiles the selectedFiles to set
     */
    public void setSelectedFiles(ObservableList<RawDataFile> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    /**
     * @return the listofBatches
     */
    public List<Batch> getListofBatches() {
        return listofBatches;
    }

    /**
     * @param listofBatches the listofBatches to set
     */
    public void setListofBatches(List<Batch> listofBatches) {
        this.listofBatches = listofBatches;
    }
}
