
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
    
    private ScriptEngine engine  = null ;
    
    public String evaluate( String methode, String ... words )   {
       
      if( engine == null ) return String.join ( " ", words )     ;
        
      try {
           Invocable invocable = (Invocable) engine                             ;
           Object  result      = invocable.invokeFunction (  methode , 
                                                             (Object[]) words ) ;
           return result.toString()                                             ;
            
       } catch ( Exception ex ) {
           Logger.getLogger(ScriptsEngine.class.getName())
                                               .log(Level.SEVERE, null, ex)     ;
           return String.join ( " ", words )                                    ;
         }
    }

    public ScriptsEngine ( String jsFile )     {
        
      try {
            this.engine = new ScriptEngineManager().getEngineByName("nashorn") ;
            this.engine.eval( new FileReader(jsFile) )                         ;
            
      }  catch ( Exception ex ) {
         Logger.getLogger(ScriptsEngine.class.getName())
                                             .log(Level.SEVERE, null, ex)      ;
        }
    }
    
}

