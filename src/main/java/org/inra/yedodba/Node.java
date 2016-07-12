
package org.inra.yedodba;

/**
 *
 * @author ryahiaoui
 */
public class Node {
    
    private final String id        ;
    private final int    code      ;
    private final String label     ;
    private final String ofEntity  ;
    private final int  num         ;
      
    public Node(String id, int code , int num ,String ofEntity, String label) {
        this.id       = id       ;
        this.code     = code     ;
        this.num      = num      ;
        this.ofEntity = ofEntity ;
        this.label    = label    ;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public int getNum() {
        return num;
    }
    
    public String getOfEntity() {
        if(ofEntity == null ) return ""      ;
        return ofEntity.replaceAll(" ", "" ) ;
    }

    @Override
    public String toString() {
           return "Node { "    + 
                  "id=" + id   + 
                  ", code = "  + code   +
                  ", num = "   + num    +
                  ", label = " + label  +
                  ", ofEntity = " + ofEntity + 
                  " } " ;
    }
}
