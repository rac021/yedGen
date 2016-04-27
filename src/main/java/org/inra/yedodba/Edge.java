

package org.inra.yedodba;

/**
 *
 * @author ryahiaoui
 */
public class Edge {
    
    private final String id ;
    
    private final String sujet    ;
    private final String objet    ;
    private final String predicat ;

    
    public Edge(String id, String sujet, String predicat, String objet) {
        this.id        = id       ;
        this.sujet     = sujet    ;
        this.predicat  = predicat ;
        this.objet     = objet    ;
    }

    public String getId() {
        return id;
    }

    public String getSujet() {
        return sujet;
    }

    public String getObjet() {
        return objet;
    }

    public String getPredicat() {
        return predicat;
    }

   
    @Override
    public String toString() {
        return "Edge{" + "id=" + id       + 
               ", sujet="      + sujet    + 
               ", predicat="   + predicat + 
               ", objet=" + objet + '}'   ;
    }
    
}
