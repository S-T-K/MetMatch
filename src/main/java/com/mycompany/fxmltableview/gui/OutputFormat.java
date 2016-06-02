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
 */
public class OutputFormat {
    private final SimpleStringProperty C1;
    private final SimpleStringProperty C2;
    private final SimpleStringProperty C3;
    private final SimpleStringProperty C4;
    private final SimpleStringProperty C5;
    private final SimpleStringProperty C6;
    private final SimpleStringProperty C7;
    private final SimpleStringProperty C8;
    private final SimpleStringProperty C9;
    private final SimpleStringProperty C10;
    
    public OutputFormat() {
        C1 = new SimpleStringProperty();
        C2 = new SimpleStringProperty();
        C3 = new SimpleStringProperty();
        C4 = new SimpleStringProperty();
        C5 = new SimpleStringProperty();
        C6 = new SimpleStringProperty();
        C7 = new SimpleStringProperty();
        C8 = new SimpleStringProperty();
        C9 = new SimpleStringProperty();
        C10 = new SimpleStringProperty();
    }
    
    
    
    public SimpleStringProperty C1Property() {
        return C1;
    }
    public String getC1() {
        return C1.get();
    }
    public void setC1(String c1) {
        C1.set(c1);
    }
    public SimpleStringProperty C2Property() {
        return C2;
    }
    public String getC2() {
        return C2.get();
    }
    public void setC2(String c2) {
        C2.set(c2);
    }
    public SimpleStringProperty C3Property() {
        return C3;
    }
    public String getC3() {
        return C3.get();
    }
    public void setC3(String c3) {
        C3.set(c3);
    }
    public SimpleStringProperty C4Property() {
        return C4;
    }
    public String getC4() {
        return C4.get();
    }
    public void setC4(String c4) {
        C4.set(c4);
    }
    public SimpleStringProperty C5Property() {
        return C5;
    }
    public String getC5() {
        return C5.get();
    }
    public void setC5(String c5) {
        C5.set(c5);
    }
    public SimpleStringProperty C6Property() {
        return C6;
    }
    public String getC6() {
        return C6.get();
    }
    public void setC6(String c6) {
        C6.set(c6);
    }
    public SimpleStringProperty C7Property() {
        return C7;
    }
    public String getC7() {
        return C7.get();
    }
    public void setC7(String c7) {
        C7.set(c7);
    }
    public SimpleStringProperty C8Property() {
        return C8;
    }
    public String getC8() {
        return C8.get();
    }
    public void setC8(String c8) {
        C8.set(c8);
    }
    public SimpleStringProperty C9Property() {
        return C9;
    }
    public String getC9() {
        return C9.get();
    }
    public void setC9(String c9) {
        C9.set(c9);
    }
    public SimpleStringProperty C10Property() {
        return C10;
    }
    public String getC10() {
        return C10.get();
    }
    public void setC10(String c10) {
        C10.set(c10);
    }
    
    public String[] getHeaders() {
        String[] headers = {C1.get(),C2.get(),C3.get(),C4.get(),C5.get(),C6.get(),C7.get(),C8.get(),C9.get(),C10.get()};
        return headers;
    }
}
