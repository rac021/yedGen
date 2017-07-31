

package org.inra.yedgen.processor.managers ;

import java.util.Map ;
import java.util.HashMap ;

/**
 *
 * @author ryahiaoui
 */
public class ManagerQuery {

    /* Hash-File - Code_Query - Query */
    private final Map< Integer, Map< Integer, String>> queries ;

    /* Code_Node  - Code_Query  ( exists if code_node != code_query ) */
    private final Map< Integer,Integer> linkerNodeQuery        ;

    public ManagerQuery( Map< Integer, Map< Integer, String>> queries ) {

        this.queries         = queries         ;
        this.linkerNodeQuery = new HashMap<>() ;
    }
         
   public void registerQuery (Integer hash, Integer code, String query ) {
   
       if(queries.containsKey(hash))           {
          queries.get(hash).put( code, query ) ;
       }
       else {
           Map<Integer, String > queryMap = new HashMap<>() ;
           queryMap.put(code, query )                       ;
           queries.put(hash, queryMap)                      ;
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
        
        String res = queries.getOrDefault(hash, null)
                            .getOrDefault( code, null ) ;
        
        return (res != null ) ? res :
                                queries.getOrDefault(hash, null)
                                       .getOrDefault( linkerNodeQuery.get(code), null ) ;
  }

   
  public String getQueryByHashAndCode ( int hash , int codeQuery ) {

      if( codeQuery == -1 )                      return null  ;
      
      String query = getQuery ( hash, codeQuery )             ;
      
      if(query == null ) printErrorMessage( hash, codeQuery ) ;
      
      return query ;
  }
  
  public void registerLink( int codeNode, int codeQuery )   {
      this.linkerNodeQuery.putIfAbsent(codeNode, codeQuery) ;
  } 
  
  public Integer getLinkedQueryCodeByCodeNode( int codeNode )   {
     return this.linkerNodeQuery.getOrDefault(codeNode , null ) ;
  }  

  private void printErrorMessage( int hash , int codeQuery )    {
     System.err.println("")   ;
     System.err.println(" Note // Query with code : " + codeQuery + " - Hash : " + hash +" not found !!" ) ;
     System.err.println("")   ;
  }

}
