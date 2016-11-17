

package org.inra.yedgen.processor.managers;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author ryahiaoui
 */
public class ManagerUri {

   /* Hash-File - Code_URI - URI */
    private final Map< Integer, Map< Integer, String>> uris ;

    public ManagerUri( Map< Integer, Map< Integer, String>> uris ) {

        this.uris = uris ;
    }
         
   public void registerUri (Integer hash, Integer code, String uri ) {
   
       if(uris.containsKey(hash)) {
           uris.get(hash).put( code, uri ) ;
       }
       else {
           Map<Integer, String > uriMap = new HashMap<>();
           uriMap.put(code, uri)  ;
           uris.put(hash, uriMap) ;
       }
   }

  public String getUri( Integer hash, Integer code ) {
      
     if( hash != null ) {
      return uris.getOrDefault(hash, null).getOrDefault(code, null) ;
     }
  
     return findUriByID(code);
  }

  public String findUriByID( Integer code ) {

    if(code == null) return null ;
        
    return 
     uris.entrySet()
         .stream()
         .filter( s -> { return s.getValue().containsKey(code) ; } )
         .map( s -> { return s.getValue().get(code) ; } )
         .findFirst()
         .orElse(null) ;
  }
     
  public String getUriByHashAndCode( int hash , int codeUri , String label ) {
        
     if(codeUri == -1 ) return label ;
     
     if(label.startsWith("(") && label.endsWith(")")) {
        return getUri( null, codeUri) ;
     }
     
     return getUri( hash, codeUri)   ;
  }
  
}
