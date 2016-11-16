
package org.inra.yedgen.processor.entities;

import java.util.Map;

/**
 *
 * @author ryahiaoui
 */
public class PatternParallel {
    
    private final String               id                   ;
    private final Map<String, String > keyValues            ;

    public PatternParallel(String id,Map<String, String> keyValues) {
        this.id        = id        ;
        this.keyValues = keyValues ;
    }

    public String getId() {
        return id ;
    }

    public Map<String, String> getKeyValues() {
        return keyValues ;
    }

}
