/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.gui;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author stefankoch
 * This class represents the various information that can be read from the input
 * It maps informations to headers
 */
public class Information {
    
    private final SimpleStringProperty information;
    private final SimpleStringProperty header;
 
    Information(String info, String header) {
        this.information = new SimpleStringProperty(info);
        this.header = new SimpleStringProperty(header);
    }
 
    public String getInformation() {
        return information.get();
    }
    public void setInformation(String info) {
        information.set(info);
    }
        
    public String getHeader() {
        return header.get();
    }
    public void setHeader(String head) {
        header.set(head);
    }
    
        
}

