
package org.inra.yedgen.processor.io ;

import java.util.Map ;
import java.util.List ;
import java.util.ArrayList ;
import org.inra.yedgen.properties.ObdaProperties ;
import org.inra.yedgen.graph.managers.GraphExtractor ;

/**
 *
 * @author ryahiaoui
 */
public class ObdaHeader {
    
    private  final List<String>  headerOut  =  new ArrayList<>() ;

    
    public ObdaHeader( GraphExtractor graphExtractor ) {
    
    
    headerOut.add(ObdaProperties.PREFIXDECLARATION) ;
    
     for (Map.Entry<String, String> entrySet : graphExtractor.getPrefixs().entrySet()) {
          String key   = entrySet.getKey()      ;
          String uri   = entrySet.getValue()    ;
          headerOut.add(ObdaProperties.PREF.replace("?pref", key)
                                           .replace("?uri", uri)) ;
     }
                      
     headerOut.add("");
     Map<String, String> sourceDeclaration = graphExtractor.getSourceDeclaration() ;
                      
      headerOut.add( ObdaProperties.SOURCE_DEC_STRING
                                   .replace("?sourceUri"    , sourceDeclaration.get("sourceUri")     != null ? 
                                                              sourceDeclaration.get("sourceUri")     : "?sourceUri"      )
                                   .replace("?connectionUrl", sourceDeclaration.get("connectionUrl") != null ? 
                                                              sourceDeclaration.get("connectionUrl") : "?connectionUrl"  )
                                   .replace("?username"     , sourceDeclaration.get("username")      != null ? 
                                                              sourceDeclaration.get("username")      : "?username"       )
                                   .replace("?password"     , sourceDeclaration.get("password")      != null ? 
                                                              sourceDeclaration.get("password")      : "?password"       )
                                   .replace("?driverClass"  , sourceDeclaration.get("driverClass")   != null ? 
                                                              sourceDeclaration.get("driverClass")   : "?driverClass" )) ;
                     
     headerOut.add("")                                        ;
                      
     headerOut.add( ObdaProperties.MAPPING_COLLECTION_BEGIN ) ;
                      
     headerOut.add("")                                        ;
     
    }

    public List<String> getHeaderOut() {
        return headerOut ;
    }
    
}
