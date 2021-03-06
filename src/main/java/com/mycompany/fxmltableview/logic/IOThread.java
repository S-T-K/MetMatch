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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.LinkedBlockingQueue;
import static java.lang.Thread.sleep;

/**
 *
 * @author stefankoch
 */
public class IOThread implements Runnable{
    
    Thread t;
    boolean run;
    
    private Session session;
    private LinkedBlockingQueue<Slice> write;
    private LinkedBlockingQueue<Slice> read;
    private LinkedBlockingQueue<Slice> nextread;
    
    public IOThread(Session session) {
        this.run = true;
        this.session = session;
        this.write = new LinkedBlockingQueue();
        this.read = new LinkedBlockingQueue();
        this.nextread = new LinkedBlockingQueue();
    }    
    
    public void run() {
         while (run) {
             short count1 = 0;
             short count2 = 0;
             byte count3 = 0;
              short count4 = 0;
           
               //check if new Slices to write higher than crit
             while (count1 < 10000 && write.size()>2000000) {
                 try {
                     Slice slice = write.take();
                     try {
                         
                         if (!slice.isLocked()) {
                             slice.writeData();
                             
                             
                         }
                         else {
                             writeslice(slice);
                             //System.out.println("can't write, is locked");
                         }
                         count1++;
                     } catch (IOException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (InterruptedException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
            
             //then check if new Slices to read
             while (count2 < 10000 && read.size()>0) {
                 try {
                     Slice slice = read.take();
                     try {
                         slice.readData();
                         writeslice(slice);
                         count2++;
                         
                     } catch (IOException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (InterruptedException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     
                     
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 
             }
             
             
             //if nothing else to do 
             if (count2==0){
             //check if new Slices to write not crit
             while (count3 < 100 && write.size()>10000) {
                 try {
                     Slice slice = write.take();
                     try {
                         if (!slice.isLocked()) {
                             slice.writeData();
                             
                             
                         }
                         else {
                             writeslice(slice);
                             //System.out.println("can't write, is locked");
                         }
                         count3++;
                     } catch (IOException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (InterruptedException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
            
             //then check if next Slices to read
             while (count4 < 1000 && nextread.size()>0) {
                 try {
                     Slice slice = nextread.take();
                     try {
                         slice.readData();
                         writeslice(slice);
                         count4++;
                         
                     } catch (IOException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (InterruptedException ex) {
                         Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     

                 } catch (InterruptedException ex) {
                     Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 
             }
             }
//             System.out.println("Crit. Write: " + count1);
//             System.out.println("Read: " + count2);
//             System.out.println("Write: " + count3);
//             System.out.println("Next Read: " + count4);
//             System.out.println("Length of read: "+ read.size());
//             System.out.println("Length of write: "+ write.size());
             
             
             
             
             try {
                 if (count1==0&&count2==0&&count3==0&&count4==0) {
                     sleep(500);
                 }
             } catch (InterruptedException ex) {
                 Logger.getLogger(IOThread.class.getName()).log(Level.SEVERE, null, ex);
             }
         } 
    }
    
    public void writeslice(Slice slice) {
        write.add(slice);
        
       
    }
    
    public void readslice(Slice slice) {
      read.add(slice);
      
    }
    
    public void terminate() {
        run=false;
    }
    
    public void readAdduct(Entry adduct) {
        for(Map.Entry<RawDataFile,Slice> entry: adduct.getListofSlices().entrySet()) {
            if (entry.getKey().getActive()) {
                readslice(entry.getValue());
            }
        }
    }
    
    public void readOGroup(Entry ogroup) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            readAdduct(ogroup.getListofAdducts().get(i));
        }
    }
    
    public void nextslice(Slice slice) {
        nextread.add(slice);
        
    }
    
    public void nextadduct(Entry adduct) {
        for(Map.Entry<RawDataFile,Slice> entry: adduct.getListofSlices().entrySet()) {
            if (entry.getKey().getActive()) {
                nextslice(entry.getValue());
            }
        }
    }
    
    public void nextogroup(Entry ogroup) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            nextadduct(ogroup.getListofAdducts().get(i));
        }
    }
    
    public void clearnext() {
        nextread.clear();
    }
    
    public void nextfile(RawDataFile file) {
        for (int i = 0; i<file.getListofSlices().length; i++) {
            nextslice(file.getListofSlices()[i]);
        }
    }
    
    public void readfile(RawDataFile file) {
        for (int i = 0; i<file.getListofSlices().length; i++) {
            readslice(file.getListofSlices()[i]);
        }
    }
    
    public void readOGroup(Entry ogroup, RawDataFile file) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            if (ogroup.getListofAdducts().get(i).getListofSlices().containsKey(file)){
            readslice(ogroup.getListofAdducts().get(i).getListofSlices().get(file));
            }
        }
    }
    
    public void writefile(RawDataFile file) {
        for (int i = 0; i<file.getListofSlices().length; i++) {
            writeslice(file.getListofSlices()[i]);
        }
        
    }
    
    public void lockSlice(Slice slice, boolean lock) {
        slice.setLocked(lock);
        //System.out.println("Slice locked: " + lock);
    }
    
    public void lockFile(RawDataFile file, boolean lock) {
        for (int i = 0; i<file.getListofSlices().length; i++) {
            lockSlice(file.getListofSlices()[i],lock);
        }
    }
    
    public void lockOGroup(Entry ogroup, boolean lock) {
        for (int i = 0; i<ogroup.getListofAdducts().size(); i++) {
            lockAdduct(ogroup.getListofAdducts().get(i),lock);
        }
    }
    
    public void lockAdduct(Entry adduct, boolean lock) {
        for(Map.Entry<RawDataFile,Slice> entry: adduct.getListofSlices().entrySet()) {
            if (entry.getKey().getActive()) {
                lockSlice(entry.getValue(),lock);
            }
        }
    }
    
    public void checkStatus() {
        if (!t.isAlive()) {
            
        }
    }
}
