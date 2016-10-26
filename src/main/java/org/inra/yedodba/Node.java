
package org.inra.yedodba;

/**
 *
 * @author ryahiaoui
 */
public class Node {
    
    private final String id     ;
    private final int    hash   ;
    private final String label  ;
    private final String type   ;
    private final int    code   ;
      
    public Node(String id, int hash , int code ,String type , String label) {
        this.id     = id     ;
        this.hash   = hash   ;
        this.code   = code   ;
        this.type   = type   ;
        this.label  = label  ;
    }

    public String getId() {
        return id ;
    }

    public String getLabel() {
        return label ;
    }

    public int getHash() {
        return hash ;
    }

    public int getCode() {
        return code;
    }
    
    public String getType() {
        if(type == null ) return ""      ;
        return type.replaceAll(" ", "" ) ;
    }

    @Override
    public String toString() {
           return "Node { "    + 
                  "id=" + id   + 
                  ", code = "  + hash   +
                  ", num = "   + code    +
                  ", label = " + label  +
                  ", ofEntity = " + type + 
                  " } " ;
    }
}
