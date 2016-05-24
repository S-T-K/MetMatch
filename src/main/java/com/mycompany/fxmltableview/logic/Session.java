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
import com.mycompany.fxmltableview.datamodel.Slice;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
   
    private IOThread iothread;
    private List<Entry> listofOGroups;
    private Reference reference;
    private List<Dataset> listofDatasets;
    private SimpleFloatProperty RTTolerance;
    private SimpleFloatProperty MZTolerance;
    private SimpleIntegerProperty resolution;
    private SimpleFloatProperty baseline;
    private SimpleFloatProperty PeakRTTolerance;
    private SimpleFloatProperty minPeakLength;
    private int IntPeakRTTol;
    private String PeackPick;
    private SimpleFloatProperty maxPeakLength;
    //max points from middle to end of peak
    private short maxPeakLengthint;
    private SimpleFloatProperty start;
    private SimpleFloatProperty end;
   
    private int numberofadducts;
    private int numberofFiles;
    private Rengine engine;
    private SimpleFloatProperty SliceMZTolerance;
    
    private boolean peakPickchanged;
    private boolean peakschanged;
    
    private List<SimpleStringProperty> listofadductnameproperties;
    private List<SimpleFloatProperty> listofadductmassproperties;
    private List<String> listofadductnames;
    private List<Float> listofadductmasses;
    
    private PropArrayCalculator proparraycalculator;
    private GravityCalculator gravitycalculator;
    
    private String[] labels;
    private int[] indices;
    
    public Session() {
        
        
        startIOThread();
       this.gravitycalculator=new GravityCalculator(this);
        
        
        this.reference= new Reference();
        this.listofDatasets = new ArrayList<>();
        this.resolution = new SimpleIntegerProperty(100);
        this.baseline = new SimpleFloatProperty(1000);
        SliceMZTolerance = new SimpleFloatProperty (3.0f);
        RTTolerance = new SimpleFloatProperty(1.5f);
        MZTolerance = new SimpleFloatProperty(11.0f);
        PeakRTTolerance = new SimpleFloatProperty(0.15f);
        maxPeakLength = new SimpleFloatProperty(0.9f);
        minPeakLength = new SimpleFloatProperty(0.2f);
        engine = new Rengine(new String[] { "--no-save" }, false, null);
        engine.eval("source(\"C:/Users/stefankoch/Desktop/MassSpecWaveletIdentification.r\")");
        peakPickchanged = true;
        peakschanged = true;
        start = new SimpleFloatProperty (3.0f);
        end = new SimpleFloatProperty (30.0f);
        
        proparraycalculator=new PropArrayCalculator(this);
        
        listofadductnameproperties= new ArrayList<>();
        listofadductnameproperties.add(new SimpleStringProperty("H"));
        listofadductnameproperties.add(new SimpleStringProperty("NH4"));
        listofadductnameproperties.add(new SimpleStringProperty("Na"));
        listofadductnameproperties.add(new SimpleStringProperty("CH3OH+H"));
        listofadductnameproperties.add(new SimpleStringProperty("K"));
        listofadductnameproperties.add(new SimpleStringProperty(""));
        listofadductnameproperties.add(new SimpleStringProperty(""));
        listofadductmassproperties= new ArrayList<>();
        listofadductmassproperties.add(new SimpleFloatProperty(1.007276f));
        listofadductmassproperties.add(new SimpleFloatProperty(18.033823f));
        listofadductmassproperties.add(new SimpleFloatProperty(22.989218f));
        listofadductmassproperties.add(new SimpleFloatProperty(33.033489f));
        listofadductmassproperties.add(new SimpleFloatProperty(38.963158f));
        listofadductmassproperties.add(new SimpleFloatProperty());
        listofadductmassproperties.add(new SimpleFloatProperty());
        
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
        //get Labels
        parseLables(headers);
        int[] indexLabelsXn = new int[this.labels.length];
        for (int i = 0; i<labels.length; i++) {
            indexLabelsXn[i] = headers.indexOf(labels[i].concat("_Xn"));
        }
        int indexNum = headers.indexOf("Num");
        int indexMZ = headers.indexOf("MZ");
        int indexRT = headers.indexOf("RT");
        int indexXn = headers.indexOf("Xn");
        int indexOGroup = headers.indexOf("OGroup");
        int indexIon = headers.indexOf("Ion");
        int indexM = headers.indexOf("M");
        int indexCharge = headers.indexOf("Charge");
        int indexEvent = headers.indexOf("ScanEvent");
        int indexIonisation = headers.indexOf("Ionisation_Mode");
        indices = new int[] {indexNum,indexMZ,indexRT,indexXn,indexOGroup,indexIon,indexM,indexCharge,indexEvent,indexIonisation};
        int Num;
        int[] labeledXn = new int[labels.length];
        float MZ;
        float RT;
        int Xn;
        int OGroup;
        String Ion;
        float M;
        int Charge;
        String ScanEvent;
        String Ionisation;
        
        
        String lastOGroup = "-1";
        Entry ogroup = null;
        for (int i = 1; i < allRows.size(); i++) {
            for (int j = 0; j<labels.length; j++) {
               try { labeledXn[j] = (int)Float.parseFloat(allRows.get(i)[indexLabelsXn[j]]); }
               catch(NullPointerException e) {
                   labeledXn[j] = 0;
               }
               //if "Several" take first one
               //TODO: change
               catch (NumberFormatException e) {
                   int end = allRows.get(i)[indexLabelsXn[j]].indexOf(',');
                   labeledXn[j] = (int)Float.parseFloat(allRows.get(i)[indexLabelsXn[j]].substring(9,end));
               }
            }
            Num = Integer.parseInt(allRows.get(i)[indexNum]);
            MZ = Float.parseFloat(allRows.get(i)[indexMZ]);
            RT = Float.parseFloat(allRows.get(i)[indexRT]);
            Xn = Integer.parseInt(allRows.get(i)[indexXn]);
            OGroup = Integer.parseInt(allRows.get(i)[indexOGroup]);
            Ion = allRows.get(i)[indexIon];
            M = parseFloatSafely(allRows.get(i)[indexM]);
            Charge = Integer.parseInt(allRows.get(i)[indexCharge]);
            ScanEvent = allRows.get(i)[indexEvent];
            Ionisation = allRows.get(i)[indexIonisation];
            
            if (RT>=start.floatValue()&&RT<=end.floatValue()) {
            //if new Ogroup, make new Ogroup
            if (!(lastOGroup.equals(allRows.get(i)[indexOGroup]))) {
                if (ogroup!=null){
                //ogroup.generateRTArray();
                }
                ogroup = new Entry(OGroup, this);
                lastOGroup = allRows.get(i)[indexOGroup];
                obsList.add(ogroup);
            }
            //add Adduct to current Ogroup
            Entry adduct = new Entry(Num,MZ,RT,Xn,OGroup,Ion,M,Charge,ScanEvent,Ionisation, labeledXn, i, this,ogroup);
            ogroup.addAdduct(adduct);
            //System.out.println(labeledXn[0]+ "   " + labeledXn[1]);
            
            } 
        }
        //ogroup.generateRTArray();
        
        this.listofOGroups= obsList;
        return obsList;
        
    }
    
    public static float parseFloatSafely(String str) {
    float result = 0;
    try {
        result = Float.parseFloat(str);
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
    
    
    public void addDataset(Dataset batch) {
        this.getListofDatasets().add(batch);
        this.numberofFiles+=batch.getListofFiles().size();
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
        return RTTolerance.floatValue();
    }

    /**
     * @param RTTolerance the RTTolerance to set
     */
    public void setRTTolerance(float RTTolerance) {
        this.RTTolerance = new SimpleFloatProperty(RTTolerance);
    }

    /**
     * @return the MZTolerance
     */
    public float getMZTolerance() {
        return MZTolerance.floatValue();
    }

    /**
     * @param MZTolerance the MZTolerance to set
     */
    public void setMZTolerance(float MZTolerance) {
        this.MZTolerance = new SimpleFloatProperty(MZTolerance);
    }

    /**
     * @return the resolution
     */
    public int getResolution() {
        return resolution.get();
    }

    /**
     * @param resolution the resolution to set
     */
    public void setResolution(int resolution) {
        this.resolution = new SimpleIntegerProperty(resolution);
    }

    /**
     * @return the baseline
     */
    public float getBaseline() {
        return baseline.floatValue();
    }

    /**
     * @param baseline the baseline to set
     */
    public void setBaseline(float baseline) {
        this.baseline = new SimpleFloatProperty(baseline);
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
    public float getSliceMZTolerance() {
        return SliceMZTolerance.floatValue();
    }

    /**
     * @param SliceMZTolerance the SliceMZTolerance to set
     */
    public void setSliceMZTolerance(float SliceMZTolerance) {
        this.SliceMZTolerance = new SimpleFloatProperty(SliceMZTolerance);
    }


    /**
     * @return the listofBatches
     */
    public List<Dataset> getListofDatasets() {
        return listofDatasets;
    }

    /**
     * @param listofBatches the listofBatches to set
     */
    public void setListofDatasets (List<Dataset> listofBatches) {
        this.listofDatasets = listofBatches;
    }

 

    /**
     * @return the numberofFiles
     */
    public int getNumberofFiles() {
        return numberofFiles;
    }

    /**
     * @param numberofFiles the numberofFiles to set
     */
    public void setNumberofFiles(int numberofFiles) {
        this.numberofFiles = numberofFiles;
    }
    
    public List<RawDataFile> getAllFiles() {
        List<RawDataFile> list = new ArrayList<>();
        for (int i = 0; i<listofDatasets.size(); i++) {
            for (int j = 0; j< listofDatasets.get(i).getListofFiles().size(); j++) {
                list.add(listofDatasets.get(i).getListofFiles().get(j));
            }
        }
        return list;
    }
    
    public List<RawDataFile> getSelectedFiles() {
        List<RawDataFile> list = new ArrayList<>();
        for (int i = 0; i<listofDatasets.size(); i++) {
            for (int j = 0; j< listofDatasets.get(i).getListofFiles().size(); j++) {
                if (listofDatasets.get(i).getListofFiles().get(j).isselected()) {
                list.add(listofDatasets.get(i).getListofFiles().get(j));}
            }
        }
        return list;
    }
    
    public SimpleFloatProperty getRTTolProp() {
        return RTTolerance;
    }
    
    public SimpleFloatProperty getMZTolProp() {
        return MZTolerance;
    }
    
    public SimpleFloatProperty getBaseProp() {
        return baseline;
    }
    
    public SimpleIntegerProperty getResProp() {
        return resolution;
    }
    
    public SimpleFloatProperty getMaxPeakLengthProp() {
        return maxPeakLength;
    }
    
    public SimpleFloatProperty getSliceMZTolProp() {
        return SliceMZTolerance;
    }

    /**
     * @return the PeackPick
     */
    public String getPeackPick() {
        return PeackPick;
    }

    /**
     * @param PeackPick the PeackPick to set
     */
    public void setPeackPick(String PeackPick) {
        this.PeackPick = PeackPick;
        System.out.println(PeackPick);
    }

    /**
     * @return the PeakRTTolerance
     */
    public SimpleFloatProperty getPeakRTTolerance() {
        return PeakRTTolerance;
    }

    /**
     * @param PeakRTTolerance the PeakRTTolerance to set
     */
    public void setPeakRTTolerance(SimpleFloatProperty PeakRTTolerance) {
        this.PeakRTTolerance = PeakRTTolerance;
    }

    /**
     * @return the IntPeakRTTol
     */
    public int getIntPeakRTTol() {
        return IntPeakRTTol;
    }

    /**
     * @param IntPeakRTTol the IntPeakRTTol to set
     */
    public void setIntPeakRTTol(int IntPeakRTTol) {
        this.IntPeakRTTol = IntPeakRTTol;
    }
    

    /**
     * @return the peakPickchanged
     */
    public boolean isPeakPickchanged() {
        return peakPickchanged;
    }

    /**
     * @param peakPickchanged the peakPickchanged to set
     */
    public void setPeakPickchanged(boolean peakPickchanged) {
        this.peakPickchanged = peakPickchanged;
        System.out.println("Peak pick changed: " + peakPickchanged);
    }
    
    public void addPenalty(float startX, float startY, float endX, float endY) {
        List<RawDataFile> list = getSelectedFiles();
        short[] PenArray;
        
        //make sure start is smaller than end
        if (startX>endX) {
            float temp = startX;
            startX = endX;
            endX = temp;
        }
        
        if (startY>endY) {
            float temp = startY;
            startY = endY;
            endY = temp;
        }
        
        
        //calculate Shift interval
        int middle = (int)((float)resolution.getValue()-1)/2;
        float interval = RTTolerance.floatValue()*2/resolution.getValue()*60;
        int start = (int) (startY/interval)+middle;
        int end = (int) (endY/interval)+middle;
        
        
        
        for (int i = 0; i<listofOGroups.size(); i++) {
            if (listofOGroups.get(i).getRT()>=startX && listofOGroups.get(i).getRT()<=endX){
                for (int j = 0; j< list.size(); j++) {
                    if (listofOGroups.get(i).getPenArray().containsKey(list.get(j))) {
                        PenArray = listofOGroups.get(i).getPenArray().get(list.get(j));
                    } else {
                        PenArray = new short[resolution.getValue()];
                    }
                    for (int k = start; k<=end; k++) {
                        if(k>0 && k<PenArray.length) {
                        PenArray[k]+=-100;
                    }}
                    listofOGroups.get(i).getPenArray().put(list.get(j), PenArray);
                }
                
            }
        }
        
    }

    /**
     * @return the maxPeakLength
     */
    public SimpleFloatProperty getMaxPeakLength() {
        return maxPeakLength;
    }

    /**
     * @param maxPeakLength the maxPeakLength to set
     */
    public void setMaxPeakLength(SimpleFloatProperty maxPeakLength) {
        this.maxPeakLength = maxPeakLength;
    }

    /**
     * @return the maxPeakLengthint
     */
    public short getMaxPeakLengthint() {
        return maxPeakLengthint;
    }
    
    //final steps when Parameters are fixed
    public void prepare() {
        maxPeakLengthint = (short) ((maxPeakLength.floatValue()/(RTTolerance.floatValue()*2/resolution.floatValue()))/2);
        
        float delta = (RTTolerance.floatValue()*2)/resolution.floatValue();
        IntPeakRTTol = (int) (PeakRTTolerance.floatValue()/delta);
        System.out.println("IntPeakRTTol: " + IntPeakRTTol);
        
    }

    /**
     * @return the listofadductnameproperties
     */
    public List<SimpleStringProperty> getListofadductnameproperties() {
        return listofadductnameproperties;
    }

    /**
     * @param listofadductnameproperties the listofadductnameproperties to set
     */
    public void setListofadductnameproperties(List<SimpleStringProperty> listofadductnameproperties) {
        this.listofadductnameproperties = listofadductnameproperties;
    }

    /**
     * @return the listogadductmasses
     */
    public List<SimpleFloatProperty> getListofadductmassproperties() {
        return listofadductmassproperties;
    }

    /**
     * @param listogadductmasses the listogadductmasses to set
     */
    public void setListofadductmassproperties(List<SimpleFloatProperty> listogadductmasses) {
        this.listofadductmassproperties = listogadductmasses;
    }
    
    
    public void testdeletearray() throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        for (int i = 0; i<listofOGroups.size(); i++) {
            for (int j = 0; j<listofOGroups.get(i).getListofAdducts().size(); j++) {
                Entry adduct = listofOGroups.get(i).getListofAdducts().get(j);
                for (Slice slice:adduct.getListofSlices().values()) {
                    
                    slice.writeData();
                    
//                    slice.setIntensityArray(null);
//                    slice.setByteMZArray(null);
                    //slice.readData();
                 
                }
            }
        }
        System.out.println("Time: " + (System.currentTimeMillis()-start));
    }

    /**
     * @return the iothread
     */
    public IOThread getIothread() {
        return iothread;
    }

    /**
     * @param iothread the iothread to set
     */
    public void setIothread(IOThread iothread) {
        this.iothread = iothread;
    }
    
    //starts IOThread, is used to easily start new one if it crashes
    public void startIOThread() {
        iothread = new IOThread(this);
        Thread t = new Thread(getIothread());
         t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

   public void uncaughtException(Thread t, Throwable e) {
   System.out.println("Uncaught IOThread exception, trying to restart IOThread....");
   //iothread.terminate();
   restartIOThread();
   }
   });
        // this will call run() function
        t.start();
        iothread.t=t;
        t.setPriority(1);
        System.out.println("New IOThread started");
        
    }
    
    public void restartIOThread() {
        Thread t = new Thread(getIothread());
         t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

   public void uncaughtException(Thread t, Throwable e) {
   System.out.println("Uncaught IOThread exception, trying to restart IOThread....");
   //iothread.terminate();
   restartIOThread();
   }
   });
        // this will call run() function
        t.start();
        iothread.t=t;
        t.setPriority(1);
        System.out.println("New IOThread started");
        
    }
    
    
    public void finalizeAdducts() {
        listofadductnames = new ArrayList<String>();
        listofadductmasses = new ArrayList<Float>();
        for (int i = 0; i<listofadductnameproperties.size(); i++) {
            if (!listofadductnameproperties.get(i).get().isEmpty()&&listofadductmassproperties.get(i).floatValue()>0) {
                listofadductnames.add(listofadductnameproperties.get(i).get());
                listofadductmasses.add(listofadductmassproperties.get(i).floatValue());
            }
            
        }
        
        
    }

    /**
     * @return the listofadductnames
     */
    public List<String> getListofadductnames() {
        return listofadductnames;
    }

    /**
     * @param listofadductnames the listofadductnames to set
     */
    public void setListofadductnames(List<String> listofadductnames) {
        this.listofadductnames = listofadductnames;
    }

    /**
     * @return the listofadductmasses
     */
    public List<Float> getListofadductmasses() {
        return listofadductmasses;
    }

    /**
     * @param listofadductmasses the listofadductmasses to set
     */
    public void setListofadductmasses(List<Float> listofadductmasses) {
        this.listofadductmasses = listofadductmasses;
    }

    /**
     * @return the start
     */
    public SimpleFloatProperty getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(SimpleFloatProperty start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public SimpleFloatProperty getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(SimpleFloatProperty end) {
        this.end = end;
    }

    /**
     * @return the numberofadducts
     */
    public int getNumberofadducts() {
        return numberofadducts;
    }

    /**
     * @param numberofadducts the numberofadducts to set
     */
    public void setNumberofadducts(int numberofadducts) {
        this.numberofadducts = numberofadducts;
    }

    /**
     * @return the proparraycalculator
     */
    public PropArrayCalculator getProparraycalculator() {
        return proparraycalculator;
    }

    /**
     * @param proparraycalculator the proparraycalculator to set
     */
    public void setProparraycalculator(PropArrayCalculator proparraycalculator) {
        this.proparraycalculator = proparraycalculator;
    }

    /**
     * @return the minPeakLength
     */
    public SimpleFloatProperty getMinPeakLength() {
        return minPeakLength;
    }

    /**
     * @param minPeakLength the minPeakLength to set
     */
    public void setMinPeakLength(SimpleFloatProperty minPeakLength) {
        this.minPeakLength = minPeakLength;
    }

    /**
     * @return the peakschanged
     */
    public boolean isPeakschanged() {
        return peakschanged;
    }

    /**
     * @param peakschanged the peakschanged to set
     */
    public void setPeakschanged(boolean peakschanged) {
        this.peakschanged = peakschanged;
    }

    /**
     * @return the gravitycalculator
     */
    public GravityCalculator getGravitycalculator() {
        return gravitycalculator;
    }

    /**
     * @param gravitycalculator the gravitycalculator to set
     */
    public void setGravitycalculator(GravityCalculator gravitycalculator) {
        this.gravitycalculator = gravitycalculator;
    }
    
    public void parseLables(List<String> headers) {
        
        List<String> lab = new ArrayList<>();
        for (int i = 0; i< headers.size(); i++) {
            if (headers.get(i).matches("(.*)_Xn")) {
                lab.add(headers.get(i).split("_")[0]);
            }
        }
        
        this.labels = lab.toArray(new String[0]);
        
        for (String label : labels) {
            System.out.println("Label detected: " + label + "     Added to list of Labels");
        }
       
        
    }

    /**
     * @return the indices
     */
    public int[] getIndices() {
        return indices;
    }

    /**
     * @param indices the indices to set
     */
    public void setIndices(int[] indices) {
        this.indices = indices;
    }
    
}
