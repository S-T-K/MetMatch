/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 *
 * @author stefankoch
 */
public class Session {
    private File ReferenceTsv;
    private List<OGroup> listofOGroups;
    
    public Session() {
        
        
    }

    /**
     * @return the ReferenceTsv
     */
    public File getReferenceTsv() {
        return ReferenceTsv;
    }

    /**
     * @param ReferenceTsv the ReferenceTsv to set
     */
    public void setReferenceTsv(File ReferenceTsv) {
        this.ReferenceTsv = ReferenceTsv;
    }
    
    public ObservableList<OGroup> parseReferenceTsv() throws FileNotFoundException {
        ObservableList<OGroup> obsList = FXCollections.observableArrayList();
        
        
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");

        TsvParser parser = new TsvParser(settings);
        FileReader reader = new FileReader(ReferenceTsv);
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
        
        
        String lastOGroup = "0";
        for (int i = 1; i < allRows.size(); i++) {
            Num = Integer.parseInt(allRows.get(i)[indexNum]);
            MZ = Double.parseDouble(allRows.get(i)[indexMZ]);
            RT = Double.parseDouble(allRows.get(i)[indexRT]);
            Xn = Integer.parseInt(allRows.get(i)[indexXn]);
            OGroup = Integer.parseInt(allRows.get(i)[indexOGroup]);
            Ion = allRows.get(i)[indexIon];
            M = parseDoubleSafely(allRows.get(i)[indexM]);
            Adduct adduct = new Adduct(Num,MZ,RT,Xn,OGroup,Ion,M);
            
            
            if (lastOGroup.equals(allRows.get(i)[indexOGroup])) {
                obsList.get(obsList.size()-1).addAduct(adduct);
            } else {
                obsList.add(new OGroup(adduct));
            }
     
            lastOGroup = allRows.get(i)[indexOGroup];
            
        }
        
        
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
    
}
