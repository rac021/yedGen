
package org.inra.yedgen.properties;

import java.util.List ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import org.inra.yedgen.processor.io.Writer ;
import org.inra.yedgen.processor.output.Messages ;
import org.apache.commons.configuration.Configuration ;
import org.inra.yedgen.processor.scripts.ScriptsEngine ;
import org.apache.commons.configuration.ConfigurationException ;
import org.apache.commons.configuration.PropertiesConfiguration ;

/**
 *
 * @author ryahiaoui
 */
public class CsvProperties {
    
    private Configuration config        = null ;   
    private ScriptsEngine scriptsEngine = null ;
        
    public String process ( String key , String ... words )  {
      
       if (words.length == 0 ) return ""                 ;
       
       List<String> functions = getFunctions(key)        ;
        
       if ( functions != null && ! functions.isEmpty() ) {
           return processString( functions, words )      ;
       }
        
       return String.join ( ":", words ) ;
    }
    
    private List<String> getFunctions( String column ) {
       return config == null ? null   :
              config.getList(column ) ;
    }
    
    private String processString ( List<String> functions , String ... words ) {       
      
       if(scriptsEngine == null ) return String.join ( " ", words )      ;
        
       String tmpLine = scriptsEngine.evaluate(functions.get(0), words ) ;
       functions.remove(0)                                               ;

       for ( String function : functions ) {
         tmpLine = scriptsEngine.evaluate ( function, tmpLine ) ;
       }
        
       return tmpLine ;
    }
    
    public CsvProperties( String prFile, String jsFile  )    {
     
     String typeJs         = "js File "         ;
     String typeProperties = "properties File"  ;
     
     try {
          if ( Writer.existFile( jsFile ) )  {
               this.scriptsEngine = new ScriptsEngine ( jsFile ) ;
               Messages.printLoadingFile( typeJs , jsFile )      ;
          }   
          else if ( jsFile != null )  {
               Messages.printErrorLoadingFile( typeJs , jsFile)  ;
          }
         
         if ( Writer.existFile( prFile ) )  {
               this.config = new PropertiesConfiguration( prFile )  ;
               Messages.printLoadingFile( typeProperties , prFile ) ;
         }
          else if ( prFile != null ) {
             Messages.printErrorLoadingFile( typeProperties , prFile ) ;
          }
       
      } catch (ConfigurationException ex) {
            Logger.getLogger( CsvProperties.class.getName() )
                                           .log( Level.SEVERE, null, ex ) ;
      }
     
    }

    public Configuration getConfig() {
        return config;
    }

    public ScriptsEngine getScriptsEngine() {
        return scriptsEngine;
    }
    
    
    
}
