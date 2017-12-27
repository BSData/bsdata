
package org.battlescribedata.repository;

import java.io.IOException;


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
