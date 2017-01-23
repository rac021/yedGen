

package org.inra.yedgen.graph.entities;

import java.util.Objects;

/**
 *
 * @author ryahiaoui
 */
public class Edge {
    
    private final int    hash     ;
    private final String id       ;
    private final String sujetId  ;
    private final String objetId  ;
    private final String predicat ;
    
    public Edge( int hash, 
                 String id, 
                 String sujetId, 
                 String predicat, 
                 String objetId )    {
        
        this.hash      = hash       ;
        this.id        = id         ;
        this.sujetId     = sujetId  ;
        this.predicat  = predicat   ;
        this.objetId     = objetId  ;
    }

    public String getId() {
        return id ;
    }

    public String getSujetId() {
        return sujetId ;
    }

    public String getObjetId() {
        return objetId ;
    }

    public String getPredicat() {
        return predicat ;
    }

    public int getHash() {
        return hash ;
    }
       
    @Override
    public String toString() {
        return "Edge{" + "id=" + id       + 
               ", sujet="      + sujetId  + 
               ", predicat="   + predicat + 
               ", objet="      + objetId  + '}' ;
    }

    @Override
    public int hashCode() {
        int hash = 7 ;
        return hash  ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true ;
        }
        if (obj == null) {
            return false ;
        }
        if (getClass() != obj.getClass()) {
            return false ;
        }
        final Edge other = (Edge) obj ;
        if (this.hash != other.hash)  {
            return false ;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false ;
        }
        if (!Objects.equals(this.sujetId, other.sujetId)) {
            return false ;
        }
        if (!Objects.equals(this.objetId, other.objetId)) {
            return false ;
        }
        if (!Objects.equals(this.predicat, other.predicat)) {
            return false ;
        }
        return true ;
    }
    
}
