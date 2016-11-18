/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
    public String evaluate( String methode, String line )  {
        
      try {
           Invocable invocable = (Invocable) engine                         ;
           Object  result      = invocable.invokeFunction( methode , line ) ;
           return result.toString()                                         ;
            
       } catch ( Exception ex ) {
           Logger.getLogger(ScriptsEngine.class.getName())
                                         .log(Level.SEVERE, null, ex ) ;
           return null ;
         }
    }

    public ScriptsEngine( String jsFile ) {
        
      try {
            this.engine = new ScriptEngineManager().getEngineByName("nashorn")     ;
            this.jsFile = ( jsFile != null && ! jsFile.isEmpty() ) ? jsFile : null ;
            this.engine.eval(new FileReader(jsFile))                               ;
            
      }  catch ( Exception ex ) {
         Logger.getLogger(ScriptsEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
