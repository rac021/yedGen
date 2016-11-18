
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
    
    private final ScriptEngine engine ;
    
    public static String jsFile = "../data/csv-files/ola-scripts.js" ;
    
    public String evaluate( String methode, String line )  {
        
      try {
          
          ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn") ;
            
          engine.eval(new FileReader(jsFile))      ;
      
          Invocable invocable = (Invocable) engine ;

          Object  result = invocable.invokeFunction(methode , line) ;
           
          return result.toString() ;
            
        } catch ( Exception ex) {
            Logger.getLogger(ScriptsEngine.class.getName()).log(Level.SEVERE, null, ex);
            return null ;
        }
    }

    public ScriptsEngine() {
        
       this.engine = new ScriptEngineManager().getEngineByName("nashorn");        
    }
      
}
