
package org.inra.yedgen.graph.managers;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.inra.yedgen.graph.entities.Edge;

/**
 *
 * @author ryahiaoui
 */
public class ManagerEdge {

   /* Hash-File - Edge */
    private final Map< Integer, Set<Edge>> edges ;

    public ManagerEdge( Map< Integer, Set<Edge>> edges ) {
   
        this.edges = edges ;
    }
         
   public void put(Integer hash, Edge edge ) {
   
       if(edges.containsKey(hash)) {
           edges.get(hash).add(edge ) ;
       }
       else {
           Set edgeSet = new HashSet<>();
           edgeSet.add(edge)      ;
           edges.put(hash, edgeSet) ;
       }
   }

  public Set getEdgesByHash ( Integer hash ) {
      
      return edges.getOrDefault(hash, null) ;
  }

  public Set<Edge> getAll () {

      return edges.values()
                  .stream()
                  .collect( HashSet::new, HashSet::addAll, HashSet::addAll) ;
  }

  public Map<Integer, Set<Edge>> getEdges() {
      return edges;
  }
  
}
