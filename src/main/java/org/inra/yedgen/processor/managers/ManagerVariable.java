
package org.inra.yedgen.processor.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.inra.yedgen.processor.entities.Node;
import org.inra.yedgen.processor.entities.PatternParallel;
import org.inra.yedgen.processor.entities.Variable;
import org.inra.yedgen.processor.errors.MessageErrors;

/**
 *
 * @author ryahiaoui
 */
public class ManagerVariable {
    
    public static final String PATTERN_CONTEXT  = "##PATTERN_CONTEXT"  ;
    public static final String PATTERN_PARALLEL = "##PATTERN_PARALLEL" ;
    public static final String PATTERN_VARIABLE = "?VARIABLE"          ;

    private final ManagerPatternContext  managerPatternContext         ;
    private final ManagerPatternParallel managerPatternParallel        ;
    private final ManagerNode            managerNode                   ;
    
    private final Set<Variable>          variables                     ;
    
    
    public ManagerVariable( Map< Integer, Map< String, String>> mapVariable ,
                            ManagerNode            managerNode              ,
                            ManagerPatternContext  managerPatternContext    , 
                            ManagerPatternParallel managerPatternParallel ) {
        
        this.managerNode            = managerNode            ;
        this.managerPatternContext  = managerPatternContext  ;
        this.managerPatternParallel = managerPatternParallel ;
        this.variables              = transformToVariables( mapVariable ) ;
    }

    private Set<Variable> transformToVariables ( Map< Integer, Map<String, String>> mapVariable ) {
    
       if ( mapVariable == null ) return null       ;

       Set<Variable> resVariables = new HashSet<>() ;

       mapVariable.forEach( (hash, mapVal) -> mapVal.forEach( (id, value ) -> 
                      resVariables.add(transformToVariable(hash, id, value)) )) ;
        
      return resVariables ;
   }
    
   private Variable transformToVariable ( Integer hash   , 
                                          String  id     , 
                                          String  stringValue ) {
  
        String  patternContextId = null ;
        
        if( stringValue.split(" ")[0].trim().startsWith(PATTERN_CONTEXT)) {
             patternContextId  = stringValue.split(" ")[0].trim()         ;
       }
        return transformToVariable ( hash , id ,
                                     stringValue , 
                                     managerPatternContext.
                                             findContextPatternByID( patternContextId )) ;
   }
  
   public Variable transformToVariable ( String stringValue ) {
       
      return  transformToVariable( null, null, stringValue )  ;
   }
    
   
   public Variable transformToVariable ( String patternValue, String patternContext ) {
       
      return  transformToVariable ( null, null, patternValue, patternContext ) ;
   }

   
   private Variable transformToVariable ( Integer hash         ,               
                                          String  id           , 
                                          String  patternValue , 
                                          String  patternContext ) {
        
       Pattern p = Pattern.compile("\\{.*?\\}") ;
    
       String patternContextId , variableName , keyValuesVariable = null ;
       
       String stringValue      = patternValue.trim().replaceAll(" +", " ") ;
       
       if( stringValue.split(" ")[0].trim().startsWith(PATTERN_CONTEXT)) {
           
           patternContextId  = stringValue.split(" ")[0].trim()          ;
           variableName      = stringValue.split(" ")[1].trim()          ;
       }
                   
       else {
           
           patternContextId  = null ;
           variableName      = stringValue.split(" ")[0].trim()          ;
       }
          
       keyValuesVariable = stringValue.split(Pattern.quote("&&"))[0] 
                                                    .split(variableName, 2 )[1] ;
       
       Matcher m = p.matcher(keyValuesVariable ) ;
           
       Map<String, String> mapKeyValuesVariable = new HashMap<>() ;
       Set<PatternParallel> setpPatternParallel = new HashSet<>() ;
           
       while (m.find()) {
                     
           String param = m.group().replace("{", "")
                                   .replace("}","").trim()
                                   .replaceAll(" +", " ") ;
                   
           String key   = param.split("=")[0].trim() ;
           String value = param.split("=")[1].trim() ;
           mapKeyValuesVariable.put(key, value)      ;
           
       }
           
       String[] patternParallel ;
               
       if(stringValue.contains("&&")) {   
               
           patternParallel = stringValue.trim()
                                        .split(Pattern.quote("&&"))[1]
                                        .replaceAll(" +", " ")
                                        .split(";")          ;
                   
           if( patternParallel != null ) {
               
              for( String  pattern_parallels : patternParallel ) {
                   
                  String patternParallelKey = pattern_parallels.trim()
                                                               .split(" ")[0]
                                                               .trim()                ;
                   
                   Matcher matcher          = p.matcher(pattern_parallels)            ;
                  
                   Map<String, String>  mapKeyValuePatternParallel = new  HashMap<>() ;
             
                   while (matcher.find()) {
                       
                        String param = matcher.group()
                                             .replace("{", "")
                                             .replace("}","")
                                             .trim().replaceAll(" +", " ") ;
                        
                        String key   = param.split("=")[0].trim()  ;
                        String value = param.split("=")[1].trim()  ;
                        mapKeyValuePatternParallel.put(key, value) ;
                   }
                   
                    PatternParallel pParallel = new PatternParallel( patternParallelKey, 
                                                                     mapKeyValuePatternParallel ) ;
                    setpPatternParallel.add ( pParallel ) ;
              }
           }
       }
        
        patternContext = patternContext == null ? 
                            managerPatternContext.findContextPatternByID( patternContextId ) : 
                            patternContext ;
        
        if( hash              != null &&  
            id                != null && 
            patternContextId  != null 
            && patternContext == null  )
            
        MessageErrors.printMessageErrorContext( patternContextId , variableName ) ;
        
       return new Variable( hash                    , 
                            id                      ,  
                            variableName            , 
                            patternContextId        , 
                            patternContext          , 
                            mapKeyValuesVariable    , 
                            setpPatternParallel  )  ;
    }
    
    
    public Set<Node> process ( Variable variable ) {
       
      /*
      if patternContextID is null -> the parentContextNode will be automatically
      linked with the predicats of ##PATTEN_CONTEXT Node
      */
              
      Set<Node> generatedGraphNodes = generateGraphIncludingContext( PATTERN_CONTEXT                 , 
                                                                     variable.getPatternContext()  ) ;
      int nbPatternParallel = 0 ;
      
      for ( PatternParallel patternParallel : variable.getPatternParallel() ) {
          
        List<Node> generatePatternParallel = managerPatternParallel.
                                             genereatePatternParallel ( variable.getHash()        , 
                                                                        patternParallel.getId() ) ;
        if( nbPatternParallel != 0 ) {
            int updateCode = nbPatternParallel ;
            generatePatternParallel.stream().forEach( node -> node.addToCode( updateCode ) ) ;
        }
        
        if( ! generatePatternParallel.isEmpty() ) {
         
            managerPatternParallel.applyKeyValues( new HashSet<>(generatePatternParallel) , 
                                                   patternParallel.getKeyValues() )       ;
        
            StickPatternParallelNodes( managerNode.find( node -> node.hasPredicateWithValue ( PATTERN_PARALLEL )) , 
                                       generatePatternParallel.get(0)) ;
        
            generatedGraphNodes.addAll(generatePatternParallel) ;
        
            nbPatternParallel += generatePatternParallel.size() ;
        }
 
      }
      
      // Remove Pattern Parallel Node
      generatedGraphNodes.stream()
                         .map( set -> { return set.getPredicatsValues().values() ; } )
                         .flatMap( set -> set.stream())
                         .map( s -> s.remove(PATTERN_PARALLEL))
                         .count() ;

      // How ?VARIABLE will be updated after applying KeyValues method 
      variable.getKeyValues().put( PATTERN_VARIABLE, variable.getVariableName()) ;
        
      managerPatternParallel.applyKeyValues( new HashSet<>(generatedGraphNodes)  , 
                                                 variable.getKeyValues()  )      ;
        
      // Restoring original nodes for next process
      managerNode.restoreOriginalNodes()  ;
      
      return  generatedGraphNodes         ;
      
    }
    
    private Set<Node> generateGraphIncludingContext( String patternContext      ,
                                                     String patternContextValue ) {
        
      Node patternContextNode = managerNode.find ( node -> node.getUri()
                                                               .equals(patternContext) ) ;
      
      Node parentContextNode  = managerNode.find ( 
                                    node -> !node.getUri().equals(patternContext) && 
                                    node.hasPredicateWithValue(patternContext) )   ;
      
      List<Node> generatedContextNodes = managerPatternContext
                                         .genereatePatternContext( patternContextValue )   ;
      
      List<Node> homogenizedContext = managerPatternContext.linkNodes( parentContextNode   , 
                                                                       patternContextNode  , 
                                                                       patternContext      , 
                                                                       generatedContextNodes ) ;
      managerNode.removeNode( patternContextNode ) ;
     
      HashSet resultSet = new HashSet(homogenizedContext) ;
      
      resultSet.addAll(managerNode.getAll())              ;

      return resultSet                                    ;
      
    }
   
    private void StickPatternParallelNodes ( Node parentParallelNode  ,
                                             Node uriRootParallelNode )                 {
        
      String predicat = parentParallelNode.getPredicatContainingValue(PATTERN_PARALLEL) ;
      parentParallelNode.addPredicatWithObject(predicat, uriRootParallelNode.getUri() ) ;
    }

   
    public void processAll() {

        variables.forEach(variable -> process(variable)) ;
    }
    
    public Set<Variable> getVariables() {
        return variables ;
    }

    public void applyKeyValue ( Set<Node> nodes , String key , String value ) {
      nodes.stream()
           .forEach( node -> node.applyKeyValue( key, value )) ;
    }
    
    public void applyKeyValues ( Set<Node> nodes , Map<String, String > values ) {
         nodes.stream()
              .forEach( node -> node.applyKeyValues( values )) ;
    }

    

/*
    public List<Node> homogenize ( Node parentContextNode ,
                                   Node patternNode       ,
                                   String pattern         ,
                                   List<Node> patternContextNodes ) {
        
        if(patternContextNodes.isEmpty() ) {
            Map<String, Set<String>> patternContextValues = patternNode.getPredicatsValuesIgnoringType() ;
            parentContextNode.updatePatternValues( pattern, patternContextValues )                       ;
        }

        else {
            String firstUri = patternContextNodes.get(0).getUri()     ;
            parentContextNode.updatePatternValue( pattern, firstUri ) ;
            
            Node LastNode = patternContextNodes.get(patternContextNodes.size() - 1 )                     ;
            Map<String, Set<String>> patternContextValues = patternNode.getPredicatsValuesIgnoringType() ;
            LastNode.addPredicatWithObjects(patternContextValues)                                        ;
        }
       
        return patternContextNodes ;
    }

*/

 }
