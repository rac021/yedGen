
package org.inra.yedgen.processor.managers ;

import java.util.Map ;
import java.util.Set ;
import java.util.List ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.ArrayList ;
import java.io.Serializable ;
import java.util.function.Predicate ;
import org.inra.yedgen.processor.entities.Node ;
import org.apache.commons.lang.SerializationUtils ;

/**
 *
 * @author ryahiaoui
 */
public class ManagerNode {

   /* Hash-File - ID_Node - Node */
    private Map< Integer, Map< String, Node>> nodes ;
    
   /* Hash-File - ID_Node - Node */
    private Map< Integer, Map< String, Node>> nodes_cloned  ;
    
    public ManagerNode() {
   
        nodes        = new HashMap<>() ;
        nodes_cloned = new HashMap<>() ;
    }
         
   public void registerNode ( int hash, String id, Node node ) {
       
       if(node == null ) return ;
       
       if( nodes.containsKey(hash)) {
             Node existingNode = nodes.get(hash).get(id) ;
             if(existingNode != null ) {
                // Update node predicats 
                existingNode.addPredicatWithObjects(node.getPredicatsValues()) ;
                nodes.get(hash).put(id, existingNode) ;
             }
             else {
               nodes.get(hash).put(id, node) ;
             }
       }
       else {
           Map nodeMap = new HashMap<>() ;
           nodeMap.put(id, node)         ;
           nodes.put(hash, nodeMap)      ;
       }
   }

  public Map getNodesByHash ( int hash )    {
      
      return nodes.getOrDefault(hash, null) ;
  }

  public Node getNodeByHashAndId ( int hash , String id ) {
  
      if(nodes.get(hash) != null ) {
        return nodes.get(hash)
                    .values()
                    .stream()
                    .filter( node -> node.getId().equals(id))
                    .findFirst()
                    .orElse (null) ;
      }
      return null ;
      
  }

  public Set<Node> getAll () {
 
      return nodes.values()
                  .stream()
                  .map( nodeList -> nodeList.values())
                  .collect( HashSet::new, HashSet::addAll, HashSet::addAll) ;
  }
  
  public Node find(  Predicate<Node> filter )  {
     return getAll().stream().filter(filter).findFirst().orElse(null) ;
  }

  public void removeNode ( Node node )  {
       nodes.values()
            .stream()
            .map(entry -> { return entry.values() ; } )
            .map( values -> { values.remove(node) ; return values ; } )
            .findFirst() ;
  }
  
  public void cloneNodes() {
     nodes_cloned =  ( Map<Integer, Map<String, Node>> ) 
                      SerializationUtils.clone((Serializable) nodes) ;
 }

  public void restoreOriginalNodes() {
     nodes =  ( Map<Integer, Map<String, Node>> ) 
              SerializationUtils.clone((Serializable) nodes_cloned) ;
  }
  
  public void clear() {
       nodes.clear() ;
  }

  public Map<Integer, Map<String, Node>> getNodes() {
      return nodes ;
  }

  void removeEmptyOptionalEntries(String oprionnalValue ) {
       
      nodes.entrySet()
           .stream()
           .flatMap( m -> m.getValue().values().stream())
           .forEach(node -> node.removeEmptyOptionalEntry( oprionnalValue )) ;
  }

  public Node findNodeByURI( String concept ) {
      
    List<Node> l = new ArrayList() ;
    
    nodes.forEach( ( hash_node , id_node_nodes ) ->         {
           
     id_node_nodes.forEach(( String id_node, Node node ) -> {
         if(  node.getUri() != null && node.getUri().trim().equals(concept)) {
             if( l.isEmpty()) l.add(node) ;
         }
     }) ;       
    }) ;
     
    return l.isEmpty() ? null : l.get(0) ;
  }
   
 /*
    public static <K1, K2, V> Map<K1, Map<K2, V>> genericDeepCopy( Map<K1, Map<K2, V>> original) {
      Objects.requireNonNull(original) ;
      Map<K1, Map<K2, V>> copy = new HashMap<>() ;
       for(Entry<K1, Map<K2, V>> entry : original.entrySet()) {
          copy.put(entry.getKey(), new HashMap<>(entry.getValue())) ;
      }
      return copy ;
   }
*/
   
}

