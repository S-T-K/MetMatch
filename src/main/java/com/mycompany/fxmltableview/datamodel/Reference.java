/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.datamodel;

import java.io.File;

/**
 *
 * @author stefankoch
 */
public class Reference extends Dataset{
    
    private File matrixFile;
    
    
    public Reference () {
        
        
    }

    /**
     * @return the matrixFile
     */
    public File getMatrixFile() {
        return matrixFile;
    }

    /**
     * @param matrixFile the matrixFile to set
     */
    public void setMatrixFile(File matrixFile) {
        this.matrixFile = matrixFile;
    }
    
}
