
package org.inra.yedgen.processor.entities ;

import java.util.List ;
import java.util.Set ;
import java.util.Map ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Objects ;
import java.util.Iterator ;
import java.util.Map.Entry ;
import java.util.Comparator ;
import java.io.Serializable ;
import java.util.regex.Pattern ;
import java.util.stream.Collectors ;
import org.apache.commons.lang.StringUtils ;
import org.inra.yedgen.processor.SqlAnalyzer;
import org.inra.yedgen.properties.ObdaProperties ;
import static java.util.stream.Collectors.joining ;
import org.inra.yedgen.processor.managers.ManagerVariable ;

/**
 *
 * @author ryahiaoui
 */
public final class Node implements Serializable  {
    
    private final Integer hash          ;
    private final String  id            ;
    private       int     code          ;
    private       String  uri           ;
    private       String  type          ;
    private final String  label         ;
    private       String  query         ;
    private final String  uriObject     ;
    private       String  queryObject   ;
    private       String  predicat      ;
    private final String  defaultPrefix ; 
     
    private final Map<String , Set<String> > predicatsValues = new HashMap<>() ;
    
    public Node( Integer hash          , 
                 String  id            , 
                 int     codeSubject   , 
                 String  uri           , 
                 String  type          , 
                 String  label         ,
                 String  predicat      ,
                 String  query         ,
                 Integer codeObject    ,
                 String  uriObject     ,
                 String  queryObject   ,
                 String  defaultPrefix )  {

        Objects.requireNonNull(codeSubject ) ;
        this.defaultPrefix   = defaultPrefix ;
        this.hash            = hash          ;
        this.id              = id            ;
        this.code            = codeSubject   ;
        this.label           = label         ;
        this.query           = cleanQ(query) ;
        this.queryObject     = queryObject   ;
        this.uri             = validatePrefix( defaultPrefix, uri )       ;
        this.type            = validatePrefix( defaultPrefix, type      ) ;
        this.uriObject       = validatePrefix( defaultPrefix, uriObject ) ;
        
        
        this.addPredicatWithObject( "a", type )                         ;
        this.predicat      = validatePrefix( defaultPrefix, predicat  ) ;
        
        if ( codeObject == null || codeObject != codeSubject ) {
            
            this.addPredicatWithObject( predicat , uriObject )          ;
            
        } else {
            
            /* Recursivity */
            
             this.predicat  = this.predicat.replaceAll(" +", " ").trim()
                       .split(Pattern.quote("{"))[0].trim() ;
             
            List<String> expp =  extractPredicatePattern( predicat )    ;
            
            this.addPredicatWithObject( this.predicat , 
                                        uriObject.replace( expp.get(0).trim()   , 
                                                          expp.get(1).trim()) ) ;
        }
        
    }

    public String getLabel() {
        return label ;
    }

    public Integer getHash() {
        return hash ;
    }

    public Integer getCode() {
        return code ;
    }
    
    public String getId() {
        return id ;
    }

    public String getUri() {
        return uri ;
    }
    
    public String getType() {
        if(type == null ) return ""      ;
        return type.replaceAll(" ", "" ) ;
    }

    public String getQuery() {
        return query ;
    }

    public String getUriObject() {
        return uriObject ;
    }

    public String getQueryObject() {
        return queryObject ;
    }

    public String getPredicat() {
        return predicat;
    }
    public String getDefaultPrefix() {
        return defaultPrefix ;
    }

    public Map<String, Set<String>> getPredicatsValues() {
        return predicatsValues ;
    }
    
    public Map<String, Set<String>> getPredicatsValuesIgnoringType() {
        
        return predicatsValues.entrySet().stream()
                              .filter(entry -> !entry.getKey().startsWith("a"))
                              .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet(e.getValue())));
    }

    public boolean hasPredicateWithValue ( String value ) {
        
        return predicatsValues.values()
                              .stream()
                              .anyMatch( set -> set.contains(value) ) ;
    }
    
    public String getPredicatContainingValue ( String value ) {
        
        return predicatsValues.entrySet()
                              .stream()
                              .filter( entry -> entry.getValue().contains(value) )
                              .map( s -> s.getKey() ).findFirst().orElse(null)   ;
    }

    
    public void addPredicatWithObject( String predicat, String object ) {
    
        if(predicat == null || object == null ) return ;
        
        predicat = validatePrefix(defaultPrefix, predicat ) ;
        object   = validatePrefix(defaultPrefix, object )   ;
        
        if( predicatsValues.containsKey(predicat) )     {
            predicatsValues.get(predicat).add( object ) ;
        }
        else {
            Set<String> values = new HashSet()     ;
            values.add( object )                   ;
            predicatsValues.put(predicat, values ) ;
        }
    }
    
    public void addPredicatWithObjects( Map<String , Set<String> > predicatsVals ) {

         // predicatsVals = Predicat + Set of Objects URI
         
        predicatsVals.entrySet().stream().forEach( 
               entry -> {
                   String key = entry.getKey() ;
                   if( this.predicatsValues.containsKey(key)) {
                       this.predicatsValues.get(key).addAll(predicatsVals.get(key)) ;
                   }
                   else {
                       this.predicatsValues.put(key, predicatsVals.get(key)) ;
                   }
               }
        ) ;
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
        final Node other = (Node) obj;
        if (!Objects.equals(this.hash, other.hash)) {
            return false ;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false ;
        }
        if (!Objects.equals(this.code, other.code)) {
            return false ;
        }
        return true ;
    }
    
    /**
     *
     * @return
     */
    public Node copy() {
      return (Node) org.apache.commons.lang.SerializationUtils.clone(this) ;
    }
    
    @Override
    public String toString() {
       
           return " Node { "         + 
                  " Hash = "         + hash            +
                  ", Id = "          + id              +
                  ", Code = "        + code            +
                  ", Uri = "         +  uri            +
                  ", Type = "        + type            + 
                  ", Label = "       + label           +
                  ", Predicat = "    + predicatsValues +
                  ", Query = "       + query           +
                  ", uriObject = "   + uriObject       +
                  ", queryObject = " + queryObject     +
                  " } " ;
    }
    
    
    public String outputTurtle () {
    
      return
          uri + " " + 
          predicatsValues.entrySet()
                         .stream()
                         .map( entry -> entry.getKey() + " " + String.join( " , ", entry.getValue()))
                         .collect(joining(" ; ")) + " . " ;
    }

    public String outputObda() {
   
         return formatObda ( 
          code ,
          uri + " " + 
          predicatsValues.entrySet()
                         .stream()
                         .map( entry -> entry.getKey() + " " + String.join( " , ", entry.getValue()))
                         .collect(joining(" ; ")) + " . "  ,
          query  ) ;
    }

    public String outputOnlyPredicatesValues() {
    
      return
          predicatsValues.entrySet()
                         .stream()
                         .map( entry -> entry.getKey().startsWith("a") ? "" :
                               entry.getKey() + " " + String.join( " , ", entry.getValue()) )
                         .collect(joining(" ; ")) + " . " ;
    }
 
    public void updatePatternValues( String pattern, Map<String, Set<String>> patternContextValues ) {

        Entry<String, Set<String>> predicatKey = 
                predicatsValues.entrySet()
                               .stream()
                               .filter( entry -> entry.getValue().contains(pattern) )
                               .map( entry -> { entry.getValue().remove(pattern) ; return entry ; } )
                               .findFirst()
                               .orElse(null) ;
        
      Set<String> remove = predicatsValues.get(predicatKey.getKey()).isEmpty() ? 
                           predicatsValues.remove(predicatKey.getKey()) :  null ;
      this.addPredicatWithObjects(patternContextValues ) ;
    
    }
    
    
    public void removeEmptyOptionalEntry( String value ) {

        predicatsValues.entrySet()
                       .stream()
                       .filter( entry -> entry.getValue().contains(value) )
                       .map (   entry -> { entry.getValue().remove(value) ; return entry ; } )
                       .map (   entry -> { return entry.getKey() ; } )
                       .count() ;
                
        predicatsValues.values().removeIf( set ->  set.isEmpty() ) ;
      
    }

    public void updatePatternValue( String pattern, String uri ) {

        Entry<String, Set<String>> predicatKey = 
                predicatsValues.entrySet()
                               .stream()
                               .filter( entry -> entry.getValue().contains(pattern) )
                               .map( entry -> { entry.getValue().remove(pattern) ; return entry ; } )
                               .findFirst()
                               .orElse(null) ;
        
      Set<String> remove = predicatsValues.get(predicatKey.getKey()).isEmpty() ? 
                           predicatsValues.remove(predicatKey.getKey()) : null ;
      this.addPredicatWithObject(predicatKey.getKey() , uri )                  ;
    
    }

    public void removePredicat( String predicat ) {
       this.predicatsValues.remove(predicat)      ;
    }
    
    private String validatePrefix ( String defaulPrefix , String entity ) {
                
        if ( entity == null                 || 
             entity.startsWith("?")         ||
             entity.startsWith("##PATTERN") ||
             entity.startsWith("<")         ||
             entity.trim().equals ("a") 
           )  
           
           return entity ;
        
        if ( entity.contains("/") )   return entity.startsWith(":") ? entity : ":" + entity ;
        if ( entity.contains(":") )    return entity ;
        
        return defaulPrefix == null ? ":" + entity : defaulPrefix + ":" + entity ;
        
    }
   
    private boolean isUri( String uri) {
        return  uri.contains(":") || 
                uri.contains("/") ||
                uri.startsWith("<") && uri.endsWith(">") ;
    }
    
    public void applyKeyValue ( String pattern , String value ) {
        
        if( value  == null     ||
            value.isEmpty()    || 
            value.equals(":") ) {
            
            value = ManagerVariable.OPTIONAL_NODE ;
        }
        
        for (Iterator< Set<String> > iterator = this.predicatsValues.values().iterator(); iterator.hasNext();) {
             
            Set<String> set = iterator.next() ;
            
            for (Iterator<String> iterator1 = set.iterator() ; iterator1.hasNext() ; )       {
            
                String line = iterator1.next() ;
                
                if(line.contains(pattern))     {
                    set.remove(line )                                                         ;
                    set.add(line.replace(pattern, isUri(line) ? cleanValue(value) : value  )) ;
                }
            }
            
        }
        
        uri         =  uri         != null ? uri.replace( pattern, cleanValue(value) ) : uri         ;
        type        =  type        != null ? type.replace( pattern, value )            : type        ;
        query       =  query       != null ? query.replace( pattern, value )           : query       ;
        predicat    =  predicat    != null ? predicat.replace( pattern, value )        : predicat    ;
        queryObject =  queryObject != null ? queryObject.replace( pattern, value )     : queryObject ;
        
    }

    public void applyKeyValues ( Map<String, String > values )                   {
    
       // Comparator< Entry<String,String> > comp1 = 
       // (a, b) -> Integer.compare(a.getKey().length(), b.getKey().length()) ;
      
        Comparator< Entry<String,String> > comp = Comparator.comparing( e -> e.getKey().length()) ;
         
        values.entrySet()
              .stream()
              .sorted(comp.reversed())
              .forEach( entry -> applyKeyValue(entry.getKey(),entry.getValue())) ;
    }

    public void addToCode( int number ) {
        this.code += number ;
    }

    private String formatObda( Integer code, String target, String query) {
    
      return ObdaProperties.MAPPING_COLLECTION_PATTERN
                           .replace("?id", getKeyByURI( "(" + String.valueOf(code) + ")_" + uri ))
                           .replace("?target", target )
                           .replace( "?source", query == null ? "null" : query  )   +  "\n"      ;
    }
    
    private String getKeyByURI(String target )            {

        return StringUtils.removeEnd (
                target.replaceAll(Pattern.quote("/{"), "_")
                      .replaceAll(Pattern.quote("-{"), "_")
                      .replaceAll(Pattern.quote("/"), "_" )
                      .replaceAll(Pattern.quote("{"), "_" )
                      .replaceAll(Pattern.quote("}"), "_" )
                      .replaceAll(Pattern.quote(":"), "_" )
                      .replaceAll("_+", "_")             
               , "_") ;
       
   }
   
   private String cleanQ ( String query ) {
     return query == null ? null : 
                     query.replaceAll("\n", " ")
                          .replaceAll("[\n\r]", "")
                          .replaceAll(" +", " ") ;
    
   }
   
   private List<String> extractPredicatePattern( String predicate ) {
    // index 0 : sourcerPredicatePattern
    // index 1 : targetPredicatePattern
    return  Arrays.asList(predicate.replace("}", "")
                  .replaceAll(" +", " ")
                  .trim()
                  .split(Pattern.quote("{"))[1].split(">")) ;
   }
   
   
   public static String cleanValue( String value ) {
        
      return value.startsWith(":")        ?
             value.replace(":" , "")
                  .replace("'" , "")
                  .replace("\"", "")
                  .replaceAll(" +" , "")  :
             value.replace(":" , "-")
                  .replace("'" , "")
                  .replace("\"", "")
                  .replaceAll(" +" , "")  ;
   }
   
   public void applyToQuery(String filterQuery ) {
       
        if(filterQuery != null ) {
            query = SqlAnalyzer.treatQuery(getQuery(), filterQuery ) ;
        }
        
    }
}

