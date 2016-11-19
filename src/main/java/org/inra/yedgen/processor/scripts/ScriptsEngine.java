
package org.inra.yedgen.processor.scripts;

import java.io.FileReader;
import javax.script.Invocable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author ryahiaoui
 */
public class ScriptsEngine {
    
    private ScriptEngine engine  = null                               ;
    private String       jsFile  = "../data/csv-files/ola-scripts.js" ;
    
    public String evaluate( String methode, String ... words )                  {
        
      try {
           Invocable invocable = (Invocable) engine                             ;
           Object  result      = invocable.invokeFunction (  methode , 
                                                             (Object[]) words ) ;
           return result.toString()                                             ;
            
       } catch ( Exception ex ) {
           Logger.getLogger(ScriptsEngine.class.getName())
                                               .log(Level.SEVERE, null, ex)     ;
           return String.join( " ", words )                                     ;
         }
    }

    public ScriptsEngine ( String jsFile )                                      {
        
      try {
            this.engine = new ScriptEngineManager().getEngineByName("nashorn")  ;
            this.jsFile = ( jsFile != null && ! jsFile.isEmpty() ) ? 
                                                jsFile : null                   ;
            if ( jsFile != null )
            this.engine.eval(new FileReader(jsFile))                            ;
            
      }  catch ( Exception ex ) {
         Logger.getLogger(ScriptsEngine.class.getName())
                                             .log(Level.SEVERE, null, ex)       ;
        }
    }
}
