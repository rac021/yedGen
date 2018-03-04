
package org.inra.yedgen.processor.managers ;

import java.util.Set ;
import java.util.Map ;
import java.util.List ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import org.inra.yedgen.processor.entities.Node ;
import org.inra.yedgen.processor.logs.Messages ;
import org.inra.yedgen.processor.entities.Variable ;
import org.inra.yedgen.processor.entities.PatternParallel ;
import static org.inra.yedgen.graph.managers.GraphExtractor.* ;

/**
 *
 * @author ryahiaoui
 */
public class ManagerVariable {
  
    public  static final String OPTIONAL_NODE    = "##NULL##"          ;
    
    private final ManagerPatternContext  managerPatternContext         ;
    private final ManagerPatternParallel managerPatternParallel        ;
    private final ManagerNode            managerNode                   ;
    
    private final Set<Variable>          variables                     ;
    
    static  final Pattern  PATTERN_KEY_VALUES  =  Pattern.compile( "\\{(.*)\\}"   , 
                                                                   Pattern.DOTALL ) ;
    
    public ManagerVariable( Map< Integer, Map< String, String>> mapVariable ,
                            ManagerNode            managerNode              ,
                            ManagerPatternContext  managerPatternContext    , 
                            ManagerPatternParallel managerPatternParallel ) {
        
        this.managerNode            = managerNode                         ;
        this.managerPatternContext  = managerPatternContext               ;
        this.managerPatternParallel = managerPatternParallel              ;
        this.variables              = transformToVariables( mapVariable ) ;
    }

    private Set<Variable> transformToVariables ( Map< Integer, Map<String, String>> mapVariable ) {
    
       if ( mapVariable == null ) return null       ;

       Set<Variable> resVariables = new HashSet<>() ;

       mapVariable.forEach( (hash, mapVal) -> mapVal.forEach( (id, value )  -> 
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
        return transformToVariable ( hash , id   ,
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
    
       String  patternContextId ,  variableName   ;
       String  keyValuesVariable = ""             ;
       
       String stringValue      =  patternValue == null ? "" : 
                                  patternValue.trim()
                                              .replace("{", " { ")
                                              .replaceAll(" +", " ") ;
       
       if( stringValue.split(" ")[0].trim().startsWith(PATTERN_CONTEXT))   {
           
           patternContextId  = stringValue.split(" ")[0].trim()            ;
           variableName      = stringValue.split(" ")[1].trim()            ;
       }
                   
       else {
           
            patternContextId  = null ;
           
            if( stringValue.isEmpty() || 
                stringValue.trim().startsWith("{"))            {
                variableName = "" + System.currentTimeMillis() ;
            } else {
               variableName = stringValue.split(" ")[0].trim() ;
            }
       }
          
        Matcher matcher_keys_values = PATTERN_KEY_VALUES.matcher ( 
                                         stringValue.split(Pattern.quote("&&"))[0] 
                                      ) ;
        
        if (matcher_keys_values.find()) {
            keyValuesVariable = "{ " + matcher_keys_values.group(1) + "}" ;
        }
   
       Matcher m = p.matcher(keyValuesVariable )                   ;
           
       Map<String, String>  mapKeyValuesVariable = new HashMap<>() ;
       Set<PatternParallel> setpPatternParallel  = new HashSet<>() ;
           
       while (m.find()) {
                     
           String param = m.group().replace("{", "")
                                   .replace("}","").trim()
                                   .replaceAll(" +", " ") ;
                   
            String key   = param.split("=")[0].trim() ;
            String value = null                       ;
            
            if(param.split("=").length > 1 ) {
                   value = param.split("=")[1].trim() ;
            }
           
            mapKeyValuesVariable.put(key, value)       ;
       }
           
       String[] patternParallel       ;
               
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
             
                   while (matcher.find())                                  {
                       
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
            
           Messages.printMessageErrorContext( patternContextId , variableName ) ;
        
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
        
        Map<String, String> keyValues = patternParallel.getKeyValues() ;
        
        String deepest = keyValues.values()
                                  .stream()
                                  .max( ( s1, s2 ) -> s1.split( ManagerMetaPattern
                                                        .findFirstIntraColumnSeparator(s1)).length >
                                                      s2.split( ManagerMetaPattern
                                                        .findFirstIntraColumnSeparator(s2)).length ? 1 : -1 )
                                  .orElse("") ;
        
        int repeat = deepest.split(ManagerMetaPattern.findFirstIntraColumnSeparator(deepest)).length ;
        
        for ( int parallelIndex = 0 ; parallelIndex < repeat ; parallelIndex++ ) {
                
            List<Node> generatePatternParallel = managerPatternParallel.generatePatternParallel ( variable.getHash()        ,
                                                                                                  patternParallel.getId() ) ;
            if( nbPatternParallel != 0 )  {
                int updateCode = nbPatternParallel ;
                generatePatternParallel.stream()
                                       .forEach( node -> node.addToCode( updateCode ) ) ;
            }

            if( ! generatePatternParallel.isEmpty() ) {

                managerPatternParallel.applyKeyValuesAtIndex( new HashSet<>(generatePatternParallel) , 
                                                              patternParallel.getKeyValues()         ,
                                                              parallelIndex                        ) ;

                StickPatternParallelNodes( managerNode.find( node -> node.hasPredicateWithValue ( PATTERN_PARALLEL )) , 
                                           generatePatternParallel.get(0)) ;

                generatedGraphNodes.addAll(generatePatternParallel) ;

                nbPatternParallel += generatePatternParallel.size() ;
            }
        }
      }
      
      // Remove Pattern Parallel Node
      generatedGraphNodes.stream()
                         .map( set -> { return set.getPredicatsValues().values() ; } )
                         .flatMap( set -> set.stream())
                         .map( s -> s.remove(PATTERN_PARALLEL))
                         .count() ;
        
      managerPatternParallel.applyKeyValues( new HashSet<>(generatedGraphNodes)  , 
                                                 variable.getKeyValues()  )      ;
      
      // RAC021
      managerNode.removeEmptyOptionalEntries( OPTIONAL_NODE ) ;
      
      // Restoring original nodes for next process
      managerNode.restoreOriginalNodes()  ;
      
      return  generatedGraphNodes         ;
      
    }
    
    private Set<Node> generateGraphIncludingContext( String patternContext      ,
                                                     String patternContextValue )          {
             
      Node patternContextNode = managerNode.find ( node -> node.getUri() != null && 
                                                           node.getUri()
                                                               .equals(patternContext) )   ;
      
      Node parentContextNode  = managerNode.find ( 
                                    node -> node.getUri() != null                && 
                                          ! node.getUri().equals(patternContext) && 
                                            node.hasPredicateWithValue(patternContext) )   ;
      
      List<Node> generatedContextNodes = managerPatternContext
                                         .generatePatternContext( patternContextValue )    ;
      
      List<Node> homogenizedContext = managerPatternContext.linkNodes( parentContextNode   , 
                                                                       patternContextNode  , 
                                                                       patternContext      , 
                                                                       generatedContextNodes ) ;
      managerNode.removeNode( patternContextNode )        ;
     
      HashSet resultSet = new HashSet(homogenizedContext) ;
      
      resultSet.addAll(managerNode.getAll())              ;

      return resultSet                                    ;
      
    }
   
    private void StickPatternParallelNodes ( Node parentParallelNode  ,
                                             Node uriRootParallelNode )                    {
      if( parentParallelNode != null )  {
         String predicat = parentParallelNode.getPredicatContainingValue(PATTERN_PARALLEL) ;
         parentParallelNode.addPredicatWithObject(predicat, uriRootParallelNode.getUri() ) ;
      }
    }

   
    public void processAll() {

        variables.forEach(variable -> process(variable)) ;
    }
    
    public Set<Variable> getVariables() {
        return variables ;
    }

//    public void applyKeyValue ( Set<Node> nodes , String key , String value ) {
//      nodes.stream()
//           .forEach( node -> node.applyKeyValue( key, value )) ;
//    }
    
//    public void applyKeyValues ( Set<Node> nodes , Map<String, String > values ) {
//         nodes.stream()
//              .forEach( node -> node.applyKeyValues( values )) ;
//    }

 }
