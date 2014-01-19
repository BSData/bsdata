/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.bsdata.constants.PropertiesConstants;

/**
 *
 * @author Jonskichov
 */
public class ApplicationProperties {
    
    private static ApplicationProperties appProperties;
    private static Properties properties;
    
    private ApplicationProperties() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(PropertiesConstants.PROPERTIES_FILE_PATH);

        properties = new Properties();
        properties.load(inputStream);
    }

    public synchronized static Properties getProperties() throws IOException{
        if (appProperties == null) {
            appProperties = new ApplicationProperties();
        }
        return appProperties.properties;
    }
}
