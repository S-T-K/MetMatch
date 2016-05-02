/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import com.mycompany.fxmltableview.datamodel.Entry;
import com.mycompany.fxmltableview.datamodel.RawDataFile;
import com.mycompany.fxmltableview.datamodel.Slice;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;

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
    private LinkedList<Slice> nextread;
    
    public IOThread(Session session) {
        this.run = true;
        this.session = session;
        this.write = new LinkedList();
        this.read = new LinkedList();
        this.nextread = new LinkedList();
    }    
    
    public void run() {
         while (run) {
             short count1 = 0;
             short count2 = 0;
             byte count3 = 0;
              byte count4 = 0;
           
               //check if new Slices to write higher than crit
             while (count1 < 1000 && write.size()>2000000) {
                 Slice slice = write.pop();
                 try {
                    
                     slice.writeData();
                     count1++;
                 } catch (IOException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
            
             //then check if new Slices to read
             while (count2 < 1000 && read.size()>0) {
                 Slice slice = read.pop();
                 try {
                     slice.readData();
                     addwrite(slice);
                     count2++;
                    
                 } catch (IOException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 
             }
             
             
             //if nothing else to do 
             if (count2==0){
             //check if new Slices to write not crit
             while (count3 < 100 && write.size()>1) {
                 Slice slice = write.pop();
                 try {
                     
                     slice.writeData();
                     count3++;
                 } catch (IOException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
            
             //then check if next Slices to read
             while (count4 < 500 && nextread.size()>0) {
                 Slice slice = nextread.pop();
                 try {
                     slice.readData();
                     addwrite(slice);
                     count4++;
                     
                 } catch (IOException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 
             }
             }
             System.out.println("Crit. Write: " + count1);
             System.out.println("Read: " + count2);
             System.out.println("Write: " + count3);
             System.out.println("Next Read: " + count4);
             
             
             
             
             try {
                 if (count1==0&&count2==0&&count3==0&&count4==0) {
                     sleep(3000);
                 }
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
    
    public void addtonext(Slice slice) {
        nextread.add(slice);
    }
    
    public void addadducttonext(Entry adduct) {
        for(Map.Entry<RawDataFile,Slice> entry: adduct.getListofSlices().entrySet()) {
            if (entry.getKey().getActive()) {
                addtonext(entry.getValue());
            }
        }
    }
    
    public void addogrouptonext(Entry ogroup) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            addadducttonext(ogroup.getListofAdducts().get(i));
        }
    }
    
    public void clearnext() {
        nextread.clear();
    }
    
    public void addfiletonext(RawDataFile file) {
        for (int i = 0; i<file.getListofSlices().size(); i++) {
            addtonext(file.getListofSlices().get(i));
        }
    }
    
    
    public void addOGroup(Entry ogroup, RawDataFile file) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            if (ogroup.getListofAdducts().get(i).getListofSlices().containsKey(file)){
            addread(ogroup.getListofAdducts().get(i).getListofSlices().get(file));
        }
        }
    }
}