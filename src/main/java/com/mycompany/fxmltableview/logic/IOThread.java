/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author stefankoch
 */
public class IOThread implements Runnable{
    
    Thread t;
    boolean run;
    
    private Session session;
    private LinkedList<Slice> write;
    private LinkedList<Slice> read;
    
    public IOThread(Session session) {
        this.run = true;
        this.session = session;
        this.write = new LinkedList();
        this.read = new LinkedList();
    }    
    
    public void run() {
         while (run) {
           
             //check if new Slices to write
             while (write.size()>1000) {
                 Slice slice = write.pop();
                 try {
                     System.out.println("writing Data");
                     slice.writeData();
                 } catch (IOException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             
             
             //then check if new Slices to read
             while (read.size()>0) {
                 Slice slice = read.pop();
                 try {
                     slice.readData();
                     addwrite(slice);
                     System.out.println("reading Data");
                 } catch (IOException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 
             }
             
             
             
             
             
             
             
             
             try {
                 sleep(100);
             } catch (InterruptedException ex) {
                 Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
             }
         } 
    }
    
    public void addwrite(Slice slice) {
        write.add(slice);
        
    }
    
    public void addread(Slice slice) {
        read.add(slice);
    }
    
    public void terminate() {
        run=false;
    }
    
    public void addAdduct(Entry adduct) {
        for(Map.Entry<RawDataFile,Slice> entry: adduct.getListofSlices().entrySet()) {
            if (entry.getKey().getActive()) {
                addread(entry.getValue());
            }
        }
    }
    
    public void addOGroup(Entry ogroup) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            addAdduct(ogroup.getListofAdducts().get(i));
        }
    }
    
    
}
