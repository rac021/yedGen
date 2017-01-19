
package org.inra.yedgen.processor.managers;

import java.util.Set ;
import java.util.Map ;
import java.util.List ;
import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.regex.Pattern ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.entities.Node ;
import org.inra.yedgen.processor.output.Messages ;
import org.inra.yedgen.processor.factories.FactoryNode ;

/**
 *
 * @author ryahiaoui
 */
public class ManagerPatternParallel {
    
    private final ManagerUri         managerUri         ;
    private final FactoryNode        factoryNode        ;
    private final MetaPatternManager metaPatternManager ;
    
    
    /* Hash-File - ID_PATTERN - PATTERN */
    private final Map< Integer, Map< String, String>> PATTERNS_PARALLEL ;
    
    
    public ManagerPatternParallel( Map< Integer, Map< String, String>> PATTERNS_PARALLEL ,
                                   ManagerUri managerUri   , 
                                   FactoryNode factoryNode ,
                                   MetaPatternManager metaPatternManager )              {
        
        this.managerUri         = managerUri         ;
        this.factoryNode        = factoryNode        ;
        this.PATTERNS_PARALLEL  = PATTERNS_PARALLEL  ;
        this.metaPatternManager = metaPatternManager ;
    }

        
    public void registerPatternParrallel ( int hash, String id_pattern , String pattern ) {
        
        if( PATTERNS_PARALLEL.containsKey(hash)) {
             PATTERNS_PARALLEL.get(hash).put( id_pattern, pattern ) ;
       }
       else {
           Map<String, String > patternMap = new HashMap<>() ;
           patternMap.put(id_pattern, pattern )    ;
           PATTERNS_PARALLEL.put(hash, patternMap) ;
       }
    }
    
    public List<Node> genereatePatternParallel( Integer hashNode, String id_pattern ) {
      
        List<Node> nodes      = new ArrayList<>() ;
        
        if ( id_pattern == null )  return nodes   ;
        
        String pattern  = null                    ;
        Integer hash    = hashNode                ;
        
        if( hash == null ) {
            
         hash    = metaPatternManager.getMetaPatternHash()     ;
         pattern = metaPatternManager.getMetaPatternParallel() ;
          
        }
        
        else {
            
            hash = findParallelPatternByID ( id_pattern ) ;
   
            if( hash == null ) {
                Messages.printMessageErrorParallel( id_pattern ) ;
                return nodes ;
            }
            
            pattern = PATTERNS_PARALLEL.get(hash).get( id_pattern )   ;
        }
        
        if( pattern == null ) {
             Messages.printMessageErrorParallel(id_pattern) ;
             return nodes ;
        }
         
        String[] entities = pattern.split(Pattern.quote(".") ) ;
            
            for( int i = 0 ; i< entities.length ; i++ ) {
                
                String predicat_object = entities[i] ;
                
                Node node = null ;
                
                String[] subEntity = predicat_object.trim().split(";") ;
                
                for( int j = 0 ; j < subEntity.length ; j++ ) {
                 
                    if( j == 0 ) {
                        
                      node = factoryNode.createNode( hash, subEntity[j] ) ;
                      continue ;
                      
                    }
                     
                    if(node != null ) {
                        
                    node.addPredicatWithObject( subEntity[j].trim().split(" ") [0]  , 
                                                treateConcept( hashNode , subEntity[j].trim()
                                                                                      .split(" ")[1]) ) ;
                    }
                 }
                
                nodes.add(node);
            }
            
     return nodes ; 

    }

    private Integer findParallelPatternByID( String id_pattern ) {

        return 
        PATTERNS_PARALLEL.entrySet()
                         .stream()
                         .filter( entry -> entry.getValue().get(id_pattern) != null  )
                         .map(entry -> entry.getKey())
                         .findFirst().orElse(null);
    }
   
  
    private String treateConcept( Integer hash , String concept ) {
          
          if(concept.contains("(") && concept.endsWith(")")) {
              int code = extractCode(concept)      ;
              return managerUri.getUri(hash, code) ;
          }
        return concept ;
    }

     private int extractCode ( String concept ) {

        if(concept.contains("(") && concept.endsWith(")")) {
          return Integer.parseInt( concept
                        .split(Pattern.quote("("))[1]
                        .replace(")", "")) ;
        }
        return -1 ;
    }
   
     
    public void applyKeyValue ( Set<Node> nodes , String key , String value ) {
      nodes.stream()
           .forEach( node -> node.applyKeyValue( key, value )) ;
    }
    
    public void applyKeyValues ( Set<Node> nodes , Map<String, String > values ) {
        nodes.stream()
             .forEach( node -> node.applyKeyValues( values )) ;
    }
    
  
    public void applyKeyValuesAtIndex ( Set<Node> nodes             ,
                                        Map<String, String > values ,
                                        int index , 
                                        String splitter )    {
       
        Map<String, String> valuesIndexI = values.entrySet()
                                                 .stream()
                                                 .collect(Collectors.toMap( Map.Entry::getKey ,
                                                                            e -> e.getValue().contains(",") ? 
                                                                                 e.getValue().split(",")[index].trim() : 
                                                                                 e.getValue().trim() ) )               ;
       nodes.stream()
            .forEach( node -> node.applyKeyValues( valuesIndexI )) ;
    }
   
}
