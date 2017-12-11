
package org.battlescribedata.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.battlescribedata.constants.PropertiesConstants;
import org.battlescribedata.dao.GitHubDao;
import org.battlescribedata.service.GitHubService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * Jersey REST Application class. This is the "master" class for the app/site, 
 * used to perform configuration and startup tasks.
 */
public class BattleScribeDataRestConfig extends ResourceConfig {
    
    public BattleScribeDataRestConfig() {
        super();
        
        setup();
    }
    
    /**
     * Configure the app and perform startup tasks. Called upon application startup.
     */
    private void setup() {
        // Grab a Logger to log startup stuff. We can't use the injected logger since it's not set up yet!
        Logger logger = Logger.getLogger(PropertiesConstants.LOGGER_NAME);
        logger.setLevel(Level.INFO);
        
        
        // Specify the package containing REST resource classes: http://javaarm.com/file/glassfish/jersey/doc/userguide/Jersey-2.26-User-Guide.htm#environmenmt.appmodel
        logger.log(Level.INFO, "+++ Startup: Find REST resource classes +++");
        packages("org.battlescribedata.rest");
        
        
        // Registeer Jersey Multipart so we can use file uploads: http://javaarm.com/file/glassfish/jersey/doc/userguide/Jersey-2.26-User-Guide.htm#multipart
        logger.log(Level.INFO, "+++ Startup: Register Jersey Multipart for file uploads +++");
        register(MultiPartFeature.class);
        
        
        // Register an AbstractBinder to bind classes that will be available for dependency injection (@Inject): http://javaarm.com/file/glassfish/jersey/doc/userguide/Jersey-2.26-User-Guide.htm#ioc
        logger.log(Level.INFO, "+++ Startup: Register classes for dependency injection +++");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                
                // Factories
                // Bind a Factory<T> so we can @Inject instances of T
                // Use a factory if you want to bind a class from a library
                
                bindFactory(PropertiesFactory.class)
                        .to(Properties.class)
                        .in(Singleton.class);
                
                bindFactory(LoggerFactory.class)
                        .to(Logger.class)
                        .in(Singleton.class);
                
                
                // Other classes
                // Bind a class so we can @Inject instances of it
                // You can directly bind classes that are part of the project
                
                bind(GitHubService.class)
                        .to(GitHubService.class)
                        .in(Singleton.class);
                
                bind(GitHubDao.class)
                        .to(GitHubDao.class)
                        .in(Singleton.class);
            }
        });
        
    }
    
    /**
     * Convenience class to define Factories with an empty dispose() method.
     * @param <T> 
     */
    private static abstract class NoDisposeFactory <T extends Object> implements Factory<T> {

        @Override
        public void dispose(T t) {}
        
    }
    
    private static class LoggerFactory extends NoDisposeFactory<Logger> {
        
        @Override
        public Logger provide() {
            Logger logger = Logger.getLogger(PropertiesConstants.LOGGER_NAME);
            
            logger.log(Level.INFO, "+++ Startup: Logger created +++");
            
            return logger;
        }   
    }
    
    private static class PropertiesFactory extends NoDisposeFactory<Properties> {
        
        @Inject
        private Logger logger;
        
        @Override
        public Properties provide() {
            
            Properties properties;
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                        PropertiesConstants.PROPERTIES_FILE_PATH);

                properties = new Properties();
                properties.load(inputStream);
            }
            catch (IOException e) {
                logger.log(Level.SEVERE, "+++ ERROR: Failed to load properties file (" + PropertiesConstants.PROPERTIES_FILE_PATH + "): {0}", e.getMessage());
                throw new RuntimeException(e); // We can't start up the app without loading the properties file. KILL IT WITH FIRE.
            }
            
            logger.log(Level.INFO, "+++ Startup: Loaded propertis file (" + PropertiesConstants.PROPERTIES_FILE_PATH + ") +++");

            return properties;
        }   
    }
}
