/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author stefankoch
 */
public class OGroup {
    //represents an OGroup consisting of rows

    private List<Adduct> listofAdducts;
    private SimpleDoubleProperty RT;
    private SimpleIntegerProperty OGroup;
    private SimpleDoubleProperty Score;

    //there can't be an empty OGroup
    public OGroup(Adduct adduct) {
        this.listofAdducts= new ArrayList<>();
        this.listofAdducts.add(adduct);
        this.RT = new SimpleDoubleProperty(adduct.getRT());
        this.OGroup = new SimpleIntegerProperty(adduct.getOGroup());
        this.Score = new SimpleDoubleProperty(0);

    }

    public void addAduct(Adduct adduct) {
        this.getListofAdducts().add(adduct);
        this.setRT(new SimpleDoubleProperty(((adduct.getRT() * (getListofAdducts().size() - 1)) + adduct.getRT()) / getListofAdducts().size()));

    }

    /**
     * @return the listofAdducts
     */
    public List<Adduct> getListofAdducts() {
        return listofAdducts;
    }

    /**
     * @return the RT
     */
    public double getRT() {
        return RT.get();
    }

    /**
     * @param RT the RT to set
     */
    public void setRT(SimpleDoubleProperty RT) {
        this.RT = RT;
    }

    /**
     * @return the OGroup
     */
    public int getOGroup() {
        return OGroup.get();
    }

    /**
     * @param OGroup the OGroup to set
     */
    public void setOGroup(SimpleIntegerProperty OGroup) {
        this.OGroup = OGroup;
    }

    /**
     * @return the Score
     */
    public double getScore() {
        return Score.get();
    }

    /**
     * @param Score the Score to set
     */
    public void setScore(SimpleDoubleProperty Score) {
        this.Score = Score;
    }

}
