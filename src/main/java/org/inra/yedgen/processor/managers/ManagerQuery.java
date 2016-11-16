

package org.inra.yedgen.processor.managers;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryahiaoui
 */
public class ManagerQuery {

   /* Hash-File - Code_Query - Query */
    private final Map< Integer, Map< Integer, String>> queries ;

    public ManagerQuery( Map< Integer, Map< Integer, String>> queries ) {

        this.queries = queries ;
    }
         
   public void registerQuery (Integer hash, Integer code, String query ) {
   
       if(queries.containsKey(hash)) {
           queries.get(hash).put( code, query ) ;
       }
       else {
           Map<Integer, String > queryMap = new HashMap<>() ;
           queryMap.put(code, query )  ;
           queries.put(hash, queryMap) ;
       }
   }

  public String getQuery ( Integer hash, Integer code ) {
      
       /* if hash == null then full_search  */ 
        if( hash == null ) {
           return queries.entrySet()
                         .stream()
                         .filter( s ->  s.getValue().containsKey(code)  )
                         .map( s -> { return s.getValue().get(code) ; } )
                         .findFirst()
                         .orElse(null) ;
        }
        
        return queries.getOrDefault(hash, null).getOrDefault( code, null ) ;
  }

   
  public String getQueryByHashAndCode ( int hash , int codeQuery ) {

      if( codeQuery == -1 )                      return null  ;
      
      String query = getQuery ( hash, codeQuery )             ;
      
      if(query == null ) printErrorMessage( hash, codeQuery ) ;
      
      return query ;
  }

  private void printErrorMessage( int hash , int codeQuery ) {
     System.err.println("")   ;
     System.err.println(" Note // Query with code : " + codeQuery + " - Hash : " + hash +" not found !!" ) ;
     System.err.println("")   ;
  }

}
