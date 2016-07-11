/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview.logic;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class B64Coder {

    //extract whole array from encoded String
    public float[] extractArray(String input) {
        byte[] decoded = Base64.getDecoder().decode(input);  //decoded bytes
        ByteBuffer buffer = ByteBuffer.wrap(decoded);
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        float[] result = new float[floatBuffer.remaining()];
        floatBuffer.get(result);
        return result;
    }

    // extract single float from encoded String
    public float extractSinglefloat(String input, int number) {
        byte[] decoded = Base64.getDecoder().decode(input);  //decoded bytes
        ByteBuffer buffer = ByteBuffer.wrap(decoded);
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        return floatBuffer.get(number);
    }
    
    public String encodeArray(float[] array) {
        return Base64.getEncoder().encodeToString(FloatArray2ByteArray(array));
    }
    
    
    //from: http://stackoverflow.com/questions/14619653/converting-a-float-to-a-byte-array-and-vice-versa-in-java
    public static byte[] FloatArray2ByteArray(float[] values){
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);

        for (float value : values){
            buffer.putFloat(value);
        }

        return buffer.array();
    }
    
}
