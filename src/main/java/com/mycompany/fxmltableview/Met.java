/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author stefankoch
 */
public class Met {
    
    private final SimpleStringProperty name = new SimpleStringProperty("");
    private final DoubleProperty prop1 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty prop2 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty prop3 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty prop4 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty prop5 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty score = new SimpleDoubleProperty(0.0);
    
    
    public Met() {
   
    }
    
    public Met(String name, double score) {
       setName(name);
       setScore(score);
       setProp1(100);
        
        
        
    }

    /**
     * @return the name
     */
    public String getName() {
        return name.get();
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * @return the prop1
     */
    public double getProp1() {
        return prop1.get();
    }

    /**
     * @param prop1 the prop1 to set
     */
    public void setProp1(double prop1) {
        this.prop1.set(prop1);
    }

    /**
     * @return the prop2
     */
    public double getProp2() {
        return prop2.get();
    }

    /**
     * @param prop2 the prop2 to set
     */
     public void setProp2(double prop2) {
        this.prop2.set(prop2);
    }

    /**
     * @return the prop3
     */
    public double getProp3() {
        return prop3.get();
    }

    /**
     * @param prop3 the prop3 to set
     */
     public void setProp3(double prop3) {
        this.prop3.set(prop3);
    }

    /**
     * @return the prop4
     */
    public double getProp4() {
        return prop4.get();
    }

    /**
     * @param prop4 the prop4 to set
     */
   public void setProp4(double prop4) {
        this.prop4.set(prop4);
    }

    /**
     * @return the prop5
     */
    public double getProp5() {
        return prop5.get();
    }

    /**
     * @param prop5 the prop5 to set
     */
    public void setProp5(double prop5) {
        this.prop5.set(prop5);
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score.get();
    }

    /**
     * @param score the score to set
     */
  public void setScore(double score) {
        this.score.set(score);
    }
    
    
}
