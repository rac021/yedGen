
package org.inra.yedgen.properties;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.inra.yedgen.processor.scripts.ScriptsEngine;

/**
 *
 * @author ryahiaoui
 */
public class CsvProperties {
    
    private Configuration config  = null                               ;
    private String        csvFile = "../data/csv-files/ola-properties" ;
    
    private ScriptsEngine scriptsEngine ;
    
    List<String> getPropety( String column ) {
      return config.getList( column ) ;
    }

    public CsvProperties( String csvFile, String jsFile  ) {
        
     try {
            this.scriptsEngine = new ScriptsEngine( jsFile )          ;
            this.config        = new PropertiesConfiguration(csvFile) ;
        } catch (ConfigurationException ex) {
            Logger.getLogger( CsvProperties.class.getName() )
                                          .log( Level.SEVERE, null, ex ) ;
        }
    }

    public String processString ( List<String> functions , String line ) {
        
      String tmpLine = line ;
        
      for ( String function : functions ) {
        
         tmpLine = scriptsEngine.evaluate(function, tmpLine ) ;
      }
        
        return tmpLine ;
    }

}
