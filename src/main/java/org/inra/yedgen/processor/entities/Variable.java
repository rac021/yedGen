
package org.inra.yedgen.processor.entities;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author ryahiaoui
 */
public class Variable {

    private final Integer hash             ;
    private final String  id               ;
    private final String  variableName     ;
    private final String  patternContextID ;
    private final String  patternContext   ;
    
    private final Map<String, String > keyValues       ;
    private final Set<PatternParallel> patternParallel ;

    public Variable( Integer hash                    , 
                     String  id                      , 
                     String  variableName            , 
                     String  patternContextID        ,
                     String  patternContext          ,
                     Map<String, String >  keyValues , 
                     Set<PatternParallel> patternParallel ) {

        this.hash              = hash                    ;
        this.id                = trim(id)                ;
        this.variableName      = trim(variableName)      ;
        this.patternContextID  = trim(patternContextID)  ;
        this.patternContext    = trim(patternContext  )  ;
        this.keyValues         = keyValues               ;
        this.patternParallel   = patternParallel         ;
    }

    public Integer getHash() {
        return hash ;
    }

    public String getId() {
        return id ;
    }

    public Map<String, String> getKeyValues() {
        return keyValues ;
    }

    public Set<PatternParallel> getPatternParallel() {
        return patternParallel ;
    }

    public String getPatternContextID() {
        return patternContextID ;
    }

    public String getVariableName() {
        return variableName ;
    }

    public void addPatternParallel ( PatternParallel patternParallel ) {
        this.patternParallel.add(patternParallel);
    }

    public String getPatternContext() {
        return patternContext;
    }

    private String trim(String value) {
        return value == null ? value : value.trim() ;
    }
    
 }
