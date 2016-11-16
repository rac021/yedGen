

package org.inra.yedgen.graph.utils;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Objects;
import org.inra.yedgen.graph.entities.Edge;
import org.inra.yedgen.processor.entities.Node;

/**
 *
 * @author ryahiaoui
 */
public class Utils {
    
    
    public static int getHash ( String pathFile ) {
       
        Objects.requireNonNull(pathFile) ;
        return  pathFile.hashCode()      ;
   
    }

    
   public static void putInMap( Map< Integer, Map< String, String>> map , 
                                Integer hash, 
                                String  id , 
                                String  value )                         {
   
       if( map.containsKey(hash)) {
           map.get(hash).put( id , value ) ;
       }
       else {
           Map<String, String > tmpMap = new HashMap<>();
           tmpMap.put(id , value)   ;
           map.put(hash, tmpMap)    ;
       }
   }
   
   
   public static void putInMap( Map< Integer, Map< Integer, String>> map , 
                                Integer hash, 
                                Integer code , 
                                String  value )                         {
   
       if( map.containsKey(hash)) {
           map.get(hash).put( code , value ) ;
       }
       else {
           Map<Integer, String > tmpMap = new HashMap<>();
           tmpMap.put(code , value) ;
           map.put(hash, tmpMap)    ;
       }
   }

   public static void putInMap( Map< Integer, 
                                Set<Edge>> map , 
                                Integer hash, Edge edge ) {
   
       if(map.containsKey(hash)) {
           map.get(hash).add(edge ) ;
       }
       else {
           Set set = new HashSet<>();
           set.add(edge)      ;
           map.put(hash, set) ;
       }
   }

   public static void putInMap ( Map< Integer, Map< String, Node>> map , 
                                 int hash, 
                                 String id, 
                                 Node node )                           {
       
       if(node == null ) return ;
       
       if( map.containsKey(hash)) {
             Node existingNode = map.get(hash).get(id) ;
             if(existingNode != null ) {
                // Update node predicats 
                existingNode.addPredicatWithObjects(node.getPredicatsValues());
                map.get(hash).put(id, existingNode) ;
             }
             else {
               map.get(hash).put(id, node) ;
             }
       }
       else {
           Map nodeMap = new HashMap<>() ;
           nodeMap.put(id, node)         ;
           map.put(hash, nodeMap)      ;
       }
   }
   
   public static Set<Edge> getAll ( Map< Integer, Set<Edge>> mapEdges ) {

      return mapEdges.values()
                     .stream()
                     .collect( HashSet::new, HashSet::addAll, HashSet::addAll) ;
  }
   
   
}
