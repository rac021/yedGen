
package org.inra.yedodba;

/**
 *
 * @author ryahiaoui
 */
public class Node {
    
    private final String id        ;
    private final int    hash      ;
    private final String label     ;
    private final String ofEntity  ;
    private final int  code        ;
      
    public Node(String id, int hash , int code ,String ofEntity, String label) {
        this.id       = id       ;
        this.hash     = hash     ;
        this.code     = code     ;
        this.ofEntity = ofEntity ;
        this.label    = label    ;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getHash() {
        return hash;
    }

    public int getCode() {
        return code;
    }
    
    public String getOfEntity() {
        if(ofEntity == null ) return ""      ;
        return ofEntity.replaceAll(" ", "" ) ;
    }

    @Override
    public String toString() {
           return "Node { "    + 
                  "id=" + id   + 
                  ", code = "  + hash   +
                  ", num = "   + code   +
                  ", label = " + label  +
                  ", ofEntity = " + ofEntity + 
                  " } " ;
    }
}
