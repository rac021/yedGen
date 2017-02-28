
package org.inra.yedgen.processor.scripts ;

import java.io.FileReader ;
import javax.script.Invocable ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import javax.script.ScriptEngine ;
import javax.script.ScriptEngineManager ;
import org.inra.yedgen.processor.managers.MetaPatternManager;

/**
 *
 * @author ryahiaoui
 */
public class ScriptsEngine {
    
    private ScriptEngine engine  = null ;
    
    public String evaluate( String method, String ... words )   {
       
      if( engine == null ) return String.join ( " ", words )    ;
        
      try {
           Invocable invocable = (Invocable) engine             ;
           String intraColumnSeparator = MetaPatternManager.findFirstIntraColumnSeparator(String.join(" ", words )) ;
           Object  result      = invocable.invokeFunction (  method                                      , 
                                                             String.join ( intraColumnSeparator, words ) , 
                                                             intraColumnSeparator )                      ;
           return result.toString() ;
            
       } catch ( Exception ex )     {
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
