
package org.inra.yedgen.processor.managers;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.inra.yedgen.processor.entities.Node;
import org.inra.yedgen.graph.managers.GraphExtractor;
import org.inra.yedgen.processor.errors.Messages;
import org.inra.yedgen.processor.factories.FactoryNode;

/**
 *
 * @author ryahiaoui
 */
public class ManagerPatternContext {
    
    private final ManagerQuery managerQuery  ;
    private final FactoryNode  factoryNode   ;
    
    private static final String  MATCHER_ENTITY  = "?ENTITY"  ;
    
    /* Hash-File - ID_PATTERN - PATTERN */
    private final Map< Integer, Map< String, String>> PATTERNS_CONTEXT  ;
    
    
    public void registerPatternContext( int hash, String id_pattern , String pattern ) {
        
       if( PATTERNS_CONTEXT.containsKey( hash ) ) {
             PATTERNS_CONTEXT.get(hash).put( id_pattern, pattern ) ;
       }
       else {
            
           Map<String, String > patternMap = new HashMap<>() ;
           patternMap.put(id_pattern, pattern )  ;
           PATTERNS_CONTEXT.put(hash, patternMap) ;
       }
    }
    
    
    public List<Node> genereatePatternContext( Integer hash, String id_pattern ) {
      
        List<Node> nodes = new ArrayList<>()    ;
        
        if ( id_pattern == null ) {
            
            return nodes  ;
        }  
        
         String pattern  ;
         
        /* if hash == null then full_search  */ 
        if( hash == null ) {
           pattern = findContextPatternByID( id_pattern ) ;
        }
        else if ( PATTERNS_CONTEXT.get(hash) == null ) {

             Messages.printMessageErrorContext(id_pattern) ;
             return nodes                                       ;
        }
        else {
             pattern  = PATTERNS_CONTEXT.get(hash).get(id_pattern) ;
        }
        
        
        if( pattern == null ) {
            Messages.printMessageErrorContext(id_pattern) ;
            return nodes                                       ;
        }
         
        String URI_PATTERN  = pattern.split(" ") [1]       ;
                  
        String predicat     =  pattern.split(" ")[2]       ;
                  
        Pattern p           = Pattern.compile("\\[.*?\\]") ;
        
        Matcher m           = p.matcher(pattern)           ;
       
        List<String> vars   = new ArrayList()              ;
         
        while (m.find()) {
        
            vars.add( m.group().replace("[", "").replace("]","").trim() ) ;
        
        }
            
        Collections.reverse(vars) ;
        
        int startCode   = Integer.parseInt(pattern.split(" ")[0]) + vars.size() ;
        
        for (int i = 0 ; i < vars.size(); i++) {

            String entity         = vars.get(i).split(" ") [0] ;
            String typeOfSubject  = entity     .split("_") [0] ;
            String entityName     = entity     .split("_") [1] ;

            int numQuery           = Integer.parseInt(vars.get(i)
                                            .split(" " )[1].split("_")[1])    ;
                
            String queryForSubject =  managerQuery.getQuery( hash, numQuery ) ; 
            
            if( queryForSubject == null ) {
               Messages.printErrorNumQueryNotFound( numQuery );
            }
            
            String uriSubject = URI_PATTERN.replace(MATCHER_ENTITY , cleanName(entityName) ) ;

            Node node ;
            
            if( i == 0 ) {
               
                node = factoryNode.createNode( hash , 
                                               UUID.randomUUID().toString()  , 
                                               startCode--     , 
                                               uriSubject      , 
                                               typeOfSubject   , 
                                               entityName      ,
                                               predicat        , 
                                               queryForSubject , 
                                               null   , 
                                               null , 
                                               GraphExtractor.PREFIX_PREDICAT ) ;
            }
            
            else {
                
                node = factoryNode.createNode( hash , 
                                               UUID.randomUUID().toString()  , 
                                               startCode--     , 
                                               uriSubject      , 
                                               typeOfSubject   , 
                                               entityName      ,
                                               predicat        , 
                                               queryForSubject , 
                                               nodes.get(i-1).getUri()   , 
                                               nodes.get(i-1).getQuery() , 
                                               nodes.get(i-1).getDefaultPrefix() ) ;
            }

            node.addPredicatWithObject( GraphExtractor.OF_ENTITY_PATTERN , entityName ) ;
            nodes.add(node) ;
        }
               
        Collections.reverse( nodes ) ;
        return      nodes            ;

    }
    
    public List<Node> genereatePatternContext( String patternContext ) {
      
        List<Node> nodes = new ArrayList<>()      ;
         
        if( patternContext == null ) return nodes ;
        
        String URI_PATTERN  = patternContext.split(" ") [1]       ;
                  
        String predicat     =  patternContext.split(" ")[2]       ;
                  
        Pattern p           = Pattern.compile("\\[.*?\\]")        ;
        
        Matcher m           = p.matcher(patternContext)           ;
       
        List<String> vars   = new ArrayList()                     ;
         
        while (m.find()) {
        
            vars.add( m.group().replace("[", "").replace("]","").trim() ) ;
        
        }
            
        Collections.reverse(vars) ;
        
        int startCode   = Integer.parseInt(patternContext.split(" ")[0]) + vars.size() -1 ;
        
        for (int i = 0 ; i < vars.size(); i++) {

            String entity         = vars.get(i).split(" ") [0] ;
            String typeOfSubject  = entity     .split("_") [0] ;
            String entityName     = entity     .split("_") [1] ;

            int numQuery           = Integer.parseInt(vars.get(i)
                                            .split(" " )[1].split("_")[1]) ;
                
            String queryForSubject =  managerQuery.getQuery( null , numQuery ) ; 
            
            if( queryForSubject == null ) {
                System.out.println("")   ;
                System.out.println(" NumQuery [ "+numQuery+" ] not found in numUris Map !! " ) ;
                System.out.println("")   ;
            }
            
            String uriSubject = URI_PATTERN.replace(MATCHER_ENTITY , cleanName(entityName) ) ;

            Node node ;
            
            if( i == 0 ) {
               
                node = factoryNode.createNode( null , 
                                               UUID.randomUUID().toString()  , 
                                               startCode--     , 
                                               uriSubject      , 
                                               typeOfSubject   , 
                                               entityName      ,
                                               predicat        , 
                                               queryForSubject , 
                                               null   , 
                                               null , 
                                               GraphExtractor.PREFIX_PREDICAT ) ;
            }
            
            else {
                
                node = factoryNode.createNode( null , 
                                               UUID.randomUUID().toString()  , 
                                               startCode--     , 
                                               uriSubject      , 
                                               typeOfSubject   , 
                                               entityName      ,
                                               predicat        , 
                                               queryForSubject , 
                                               nodes.get(i-1).getUri()   , 
                                               nodes.get(i-1).getQuery() , 
                                               nodes.get(i-1).getDefaultPrefix() ) ;
            }

            node.addPredicatWithObject( GraphExtractor.OF_ENTITY_PATTERN , entityName ) ;
            nodes.add(node) ;
        }
               
        Collections.reverse( nodes ) ;
        return      nodes            ;

    }

    
    public ManagerPatternContext( Map< Integer, Map< String, String>> PATTERNS_CONTEXT ,
                                  ManagerQuery managerQuery , 
                                  FactoryNode factoryNode ) {
        
        this.managerQuery     = managerQuery     ;
        this.factoryNode      = factoryNode      ;
        this.PATTERNS_CONTEXT = PATTERNS_CONTEXT ;
    }

    public List<Node> linkNodes ( Node parentContextNode , 
                                          Node patternNode       , 
                                          String pattern         , 
                                          List<Node> patternContextNodes ) {
        
        if(patternContextNodes.isEmpty() ) {
            Map<String, Set<String>> patternContextValues = patternNode.getPredicatsValuesIgnoringType() ;
            parentContextNode.updatePatternValues( pattern, patternContextValues ) ;
        }

        else {
            String firstUri = patternContextNodes.get(0).getUri()     ;
            parentContextNode.updatePatternValue( pattern, firstUri ) ;
            
            Node LastNode = patternContextNodes.get(patternContextNodes.size() - 1 )  ;
            Map<String, Set<String>> patternContextValues = patternNode.getPredicatsValuesIgnoringType() ;
            LastNode.addPredicatWithObjects(patternContextValues) ;
        }
       
        return patternContextNodes ;
        
    }

    private String cleanName(String entityName ) {
        
        String cleanName =  entityName.contains(":")   ?  
                            entityName.startsWith(":") ? 
                                 entityName.replaceFirst(":", "").replaceAll(":", "-")
                                 : entityName.replaceAll(":", "-") 
                            : entityName ;
        
        return cleanName.toLowerCase(Locale.FRENCH) ;
    }
 
    public String findContextPatternByID( String id_pattern ) {

        if(id_pattern == null) return null ;
        
        return 
        PATTERNS_CONTEXT.entrySet()
                        .stream()
                        .filter( s -> { return s.getValue().containsKey(id_pattern) ; } )
                        .map( s -> { return s.getValue().get(id_pattern) ; } )
                        .findFirst()
                        .orElse(null) ;
        
    }
}
