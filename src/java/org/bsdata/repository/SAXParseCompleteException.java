/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.repository;

import org.xml.sax.SAXException;

/**
 *
 * @author Jonskichov
 */
public class SAXParseCompleteException extends SAXException {
    
    public SAXParseCompleteException(String message) {
        super(message);
    }
    
}
