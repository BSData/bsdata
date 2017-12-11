/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.battlescribedata.repository;

import java.io.IOException;

/**
 *
 * @author Jonskichov
 */
public class XmlException extends IOException {
    
    public XmlException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
    
    public XmlException(String message) {
        super(message);
    }
    
    public XmlException(String message, Throwable cause) {
        super (message, cause);
    }
    
}
