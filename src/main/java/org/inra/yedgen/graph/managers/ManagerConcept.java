

package org.inra.yedgen.graph.managers ;

import java.util.Map ;
import java.util.HashMap ;

/**
 *
 * @author ryahiaoui
 */
public class ManagerConcept {

   /* Hash-File - ID_Concept - Label_Concept */
    private final Map< Integer, Map< String, String>> concepts ;

    public ManagerConcept(  Map< Integer, Map< String, String>> concepts  ) {
   
        this.concepts = concepts;
    }
         
   public void put(Integer hash, String id , String label ) {
   
       if( concepts.containsKey(hash))          {
           concepts.get(hash).put( id , label ) ;
       }
       else {
           Map<String, String > uriMap = new HashMap<>() ;
           uriMap.put(id , label)     ;
           concepts.put(hash, uriMap) ;
       }
   }

  public String getConcept( Integer hash, String id ) {
      
      return concepts.getOrDefault(hash, null).getOrDefault(id, null) ;
  }

}
