/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.model;

import java.io.Serializable;

/**
 *
 * @author Jonskichov
 */
public class DataFile implements Serializable {
    
    private transient byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
}
