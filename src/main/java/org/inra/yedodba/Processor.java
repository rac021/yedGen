
package org.inra.yedodba ;

import org.json.XML ;
import java.io.File ;
import java.util.*  ;

import org.json.JSONArray  ;
import java.nio.file.Path  ;
import java.io.InputStream ;
import org.json.JSONObject ;

import java.io.IOException ;
import java.nio.file.Files ;
import java.util.regex.Pattern ;
import java.io.FileInputStream ;
import java.util.regex.Matcher;
import static java.util.stream.Collectors.toList ;

/**
 *
 * @author ryahiaoui 26-10-2016 17:20
 */
public class Processor {

    private final Set<Edge>             edges                     =  new  HashSet<>()   ;
    private final Map<String , Node  >  nodes                     =  new  HashMap<>()   ;
    private final Map<String , String>  prefix                    =  new  HashMap<>()   ;
    private final Map<Integer, String>  tmpUris                   =  new  HashMap<>()   ;
    private final Map<String,  String>  uris_queries              =  new  HashMap<>()   ;
    
    private final Map<String,  String>  uris_queries_parallel     =  new  HashMap<>()   ;
    private final Map<String,  String>  uris_queries_parallel_key =  new  HashMap<>()   ;
    private final Set<String>           uris_parallel_root        =  new  HashSet<>()   ;
    
    private final Map<String,  Integer> uris_num                  =  new  HashMap<>()   ;
    private final Map<String,  Integer> uris_num_parallel         =  new  HashMap<>()   ;
    
    private final Map<Integer, String>  source                    =  new  HashMap<>()   ;
    private final Map<Integer, Integer> sourceByCode              =  new  HashMap<>()   ;
      
    private final Map<String,  String>  target                    =  new  HashMap<>()   ;
    private final Map<String,  String>  target_parallel           =  new  HashMap<>()   ;
    private final Map<String,  String>  SourceDeclaration         =  new  HashMap<>()   ;
    
    private final Map<String,  String>  PATTERNS                  =  new  HashMap<>()   ;
    private final Map<String,  String>  PATTERNS_PARALLEL         =  new  HashMap<>()   ;
    
    private final List<String>          VARIABLES                 =  new  ArrayList<>() ;

    private static String  PREFIX_PREDICAT         =  "oboe-coreX"                  ;
    private static final String  PREFIXDECLARATION = "[PrefixDeclaration]"          ;
    private static final String  PREF              =  "?pref		?uri"       ;

    private static final String MAPPING_COLLECTION_BEGIN   = "[MappingDeclaration] @collection [[" ;

    private static final String MAPPING_COLLECTION_PATTERN =  "mappingId	?id\n"      +
                                                              "target		?target\n"  +
                                                              "source		?source"    ;

    private static final String MAPPING_COLLECTION_END     = "]]" ;

    private boolean existHeader      = false                  ;
    private boolean isGraphPattern   = false                  ;
    
    private static final String  MATCHER_VARIABLE         = "?VARIABLE"          ;
    private static final String  MATCHER_ENTITY           = "?ENTITY"            ;
    private static final String  MATCHER_PATTERN_CONTEXT  = "##PATTERN_CONTEXT"  ;
    private static final String  MATCHER_PATTERN_PARALLEL = "##PATTERN_PARALLEL" ;
    private static final String  OF_ENTITY_PATTERN        = "oboe-core:ofEntity" ;
    
    String linker  = null ;

    
    private  JSONObject loadJsonObject ( String pathFile ) throws IOException {

        String xml ;

        try ( InputStream inputStream = new FileInputStream(pathFile) )  {

            StringBuilder builder = new StringBuilder() ;

            int ptr ;

            while ((ptr = inputStream.read()) != -1 )
            {
                builder.append((char) ptr) ;
            }

            xml = builder.toString() ;
        }

        return XML.toJSONObject(xml) ;
    }


    private void loadNodes ( JSONObject jsonObj , int hash )      {

        JSONArray jsonArrayNodes = jsonObj.getJSONObject("graphml")
                                          .getJSONObject( "graph" )
                                          .getJSONArray( "node" ) ;

        for (int i = 0; i < jsonArrayNodes.length(); i++)      {

            Object obj                 = jsonArrayNodes.get(i) ;
            
            JSONObject jsonObjectNode  = (JSONObject) obj      ;

            if(obj != null) {

                if(obj.toString().startsWith("{\"data\":{")) {

                    String label = jsonObjectNode.getJSONObject("data")
                                                 .getJSONObject("y:ShapeNode")
                                                 .getJSONObject("y:NodeLabel")
                                                 .getString("content").trim()
                                                 .replaceAll(" +", " ") ;

                    String id       =  jsonObjectNode.getString("id") + "_"+ hash ;
                    String ofEntity =  null ;
                    int code        =  -1   ;

                    if(label.contains("(") && label.contains(")")) {
                        code =  Integer.parseInt(
                                  label.split(Pattern.quote("("))[1]
                                       .replaceAll("[^0-9]", ""))   ;
                        ofEntity = label.split(Pattern.quote("("))[0]  ;
                    }
                   
                    Node node ;
                    if(code == -1 && ! label.startsWith(MATCHER_PATTERN_CONTEXT) 
                                  && ! label.startsWith(MATCHER_PATTERN_PARALLEL ) ) {
                        node = new Node (id, code + hash , code , ofEntity, label ) ;
                    }
                    else {
                        if(label.startsWith(":"))
                            node = new Node( id, code + hash, code,
                                    ofEntity, label.split(Pattern.quote("("))[0] + "::#" ) ;
                        else
                            node = new Node ( id, code + hash, code, ofEntity,
                                              label.split(Pattern.quote("("))[0] )         ;
                    }

                    nodes.put(id, node) ;
                }

                if ( jsonObjectNode.has("graph")) {

                    if ( jsonObjectNode.getJSONObject("graph").toString().startsWith("{\"node\":[")) {

                        JSONArray jsonArrayGroupNodes =
                                jsonObjectNode.getJSONObject("graph")
                                              .getJSONArray("node") ;

                        for (int j = 0; j < jsonArrayGroupNodes.length(); j++) {

                            if ( jsonArrayGroupNodes.toString().startsWith("{\"data\":[")   ||
                                    jsonArrayGroupNodes.toString().startsWith("[{\"data\":[") ) {

                                if ( jsonArrayGroupNodes.getJSONObject(j)
                                                        .getJSONArray("data")
                                                        .getJSONObject(1)
                                                        .has("y:ShapeNode")) {

                                    String id = jsonArrayGroupNodes.getJSONObject(j)
                                                .getString("id") ;

                                    String label = jsonArrayGroupNodes.getJSONObject(j)
                                                                      .getJSONArray("data")
                                                                      .getJSONObject(1)
                                                                      .getJSONObject("y:ShapeNode")
                                                                      .getJSONObject("y:NodeLabel")
                                                                      .getString("content").trim()
                                                                      .replaceAll(" +", " ") ;
                                     
                                    int code ;

                                    if (label.toLowerCase(Locale.FRENCH).startsWith("query_(")) {
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(":"))[0]
                                                       .split(Pattern.quote("_"))[1]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        source.put(code+hash, label.split( Pattern
                                                                   .quote(": "))[1]
                                                                   .trim())       ;
                                                                   
                                        sourceByCode.put(code, code + hash )      ;
                                    }
                                    
                                    else if (label.toLowerCase()
                                                  .startsWith("(") && 
                                                  label.toLowerCase().contains(")") ) {
                                                      
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(")"))[0]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        tmpUris.put(code + hash , label.split( Pattern
                                                                       .quote(")"))[1]
                                                                       .trim())      ;
                                    }
                                }
                            }

                            else

                            if ( jsonArrayGroupNodes.toString().startsWith("{\"data\":{")   ||
                                 jsonArrayGroupNodes.toString().startsWith("[{\"data\":{") ) {

                                if ( jsonArrayGroupNodes.getJSONObject(j)
                                                        .getJSONObject("data")
                                                        .has("y:ShapeNode"))       {

                                    String id = jsonArrayGroupNodes.getJSONObject(j)
                                                                   .getString("id") ;
                                    
                                    String label = jsonArrayGroupNodes.getJSONObject(j)
                                                                      .getJSONObject("data")
                                                                      .getJSONObject("y:ShapeNode")
                                                                      .getJSONObject("y:NodeLabel")
                                                                      .getString("content").trim()
                                                                      .replaceAll(" +", " ") ;
                                    
                                    if (label.startsWith(MATCHER_PATTERN_CONTEXT) && label.contains(" ")) {
                                            isGraphPattern = true     ;
                                            PATTERNS.put(label.split(" ")[0], 
                                                         label.replaceFirst(Pattern.quote(label
                                                              .split(" ")[0]),"").trim()) ;
                                    }
                                    if (label.startsWith(MATCHER_PATTERN_PARALLEL) && label.contains(" ")) {
                                            isGraphPattern = true     ;
                                            PATTERNS_PARALLEL.put(label.split(" ")[0] ,
                                                         label.replaceFirst(Pattern.quote(label
                                                              .split(" ")[0]),"").trim()) ;
                                    }
                                    
                                    else if (label.startsWith(MATCHER_VARIABLE) && label.contains(" ")) {                                           
                                            VARIABLES.add(label.replaceFirst(Pattern.quote(MATCHER_VARIABLE),"")) ;
                                    }

                                    int code ;

                                    if (label.toLowerCase().startsWith("query_(")) {
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(":"))[0]
                                                       .split(Pattern.quote("_"))[1]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        source.put(code+hash, label.split(Pattern
                                              .quote(": "))[1].trim())             ;
                                              
                                        sourceByCode.put(code, code + hash )       ;
                                    }
                                    
                                    else
                                    
                                    if ( label.toLowerCase().startsWith("(") 
                                         && label.toLowerCase().contains(")") )   {
                                                
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(")"))[0]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        tmpUris.put(code+hash, label.split(Pattern
                                                                    .quote(")")) [1]
                                                                    .trim())       ;

                                        int co = Integer.parseInt(label
                                                        .split(Pattern
                                                        .quote(") ")) [0].trim()
                                                        .replace("(", ""))     ;
                                        
                                        if ( uris_num.values().contains( co ) ) {
                                          System.out.println("ALERT # Code : " + co + " Detected multiple times ! ") ;
                                        }
                                         
                                        uris_num.put( ":" + label.split( Pattern
                                                                .quote(")")) [1]
                                                                .trim() , co ) ;
                                    }
                                    else
                                    if (label.toLowerCase().startsWith("prefix ")) {
                                        String pref = label.split(Pattern.quote(" "))[1] ;
                                        String uri  = label.split(Pattern.quote(" "))[2] ;

                                        prefix.put(pref, uri) ;
                                    }
                                    else
                                    if (label.startsWith("PREDICAT_PREFIX :"))       {
                                        
                                        PREFIX_PREDICAT = label.split(Pattern
                                                               .quote("PREDICAT_PREFIX :"))[1] ;
                                        
                                    }
                                    else
                                    if (label.toLowerCase().startsWith("obda-"))    {

                                        if  ( label.split(Pattern.quote(" : ")) [0]
                                                   .equals("obda-sourceUri"))     {
                                                       
                                            SourceDeclaration.put("sourceUri",
                                                    label.split(Pattern
                                                         .quote(" : "))[1]) ;
                                        }
                                        else if ( label.split(Pattern.quote(" : ")) [0]
                                                       .equals("obda-connectionUrl")) {
                                                  
                                            SourceDeclaration.put("connectionUrl", label
                                                             .split(Pattern
                                                             .quote(" : "))[1]) ;
                                        }
                                        else if (label.split(Pattern.quote(" : "))[0]
                                                      .equals("obda-username"))     {
                                                        
                                            SourceDeclaration.put("username",  label
                                                             .split(Pattern
                                                             .quote(" : "))[1])    ;
                                        }
                                        else if (label.split(Pattern.quote(" : "))[0]
                                                      .equals("obda-password"))     {
                                                  
                                            SourceDeclaration.put("password", label
                                                             .split(Pattern
                                                             .quote(" : "))[1])   ;
                                        }
                                        else if ( label.split(Pattern.quote(" : "))[0]
                                                       .equals("obda-driverClass"))  {
                                                        
                                            SourceDeclaration.put("driverClass", label
                                                             .split(Pattern
                                                             .quote(" : "))[1])   ;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else

                    if(jsonObjectNode.getJSONObject("graph")
                                     .toString().startsWith("{\"node\":{"))    {

                        JSONObject jsonArrayGroupNodes = jsonObjectNode.
                                                         getJSONObject("graph" )
                                                        .getJSONObject("node") ;

                        if( jsonArrayGroupNodes.getJSONObject("data").has("y:ShapeNode")) {

                            String id = jsonArrayGroupNodes.getString("id") ;

                            String label = jsonArrayGroupNodes
                                            .getJSONObject("data")
                                            .getJSONObject("y:ShapeNode")
                                            .getJSONObject("y:NodeLabel")
                                            .getString("content").trim()
                                            .replaceAll(" +", " ") ;

                            int code ;

                            if (label.startsWith(MATCHER_PATTERN_CONTEXT) && label.contains(" ")) {
                                    isGraphPattern = true     ;
                                    PATTERNS.put(label.split(" ")[0], label.replaceFirst(label
                                                                           .split(" ")[0],"")
                                                                           .trim()) ;
                            }
                            else if (label.startsWith(MATCHER_VARIABLE) && label.contains(" ")) {                                  
                                  VARIABLES.add(label.replaceFirst(label.split(" ")[0],"")) ;
                            }
                            
                            if(label.toLowerCase().startsWith("query_("))  {
                                code =  Integer.parseInt(label
                                               .split(Pattern.quote(":"))[0]
                                               .split(Pattern.quote("_"))[1]
                                               .replaceAll("[^0-9]", ""))  ;

                                source.put(code+hash, label.split(Pattern
                                                           .quote(": "))[1]
                                                           .trim())       ;
                                                           
                                sourceByCode.put(code, code + hash )      ;
                            }
                            else
                            if( label.toLowerCase().startsWith("(") 
                                && label.toLowerCase().contains(")") )   {
                                      
                                code =  Integer.parseInt(label
                                               .split(Pattern.quote(")"))[0]
                                               .replaceAll("[^0-9]", ""))  ;

                                tmpUris.put( code+hash, label.split(Pattern
                                                             .quote(")"))[1]
                                                             .trim()) ;

                                if (uris_num.values().contains( code )) {
                                   System.out.println("ALERT # Code { " + code + " } Detected multiple times ! ") ;
                                }
                                         
                                uris_num.put( ":" + label.split( Pattern
                                                        .quote(")")) [1]
                                                        .trim() , code ) ;
                            }
                            
                            else
                            
                            if(label.toLowerCase().startsWith("prefix "))        {
                                String pref = label.split(Pattern.quote(" "))[1] ;
                                String uri  = label.split(Pattern.quote(" "))[2] ;
                                prefix.put(pref, uri) ;
                            }
                            
                            else
                            
                            if(label.startsWith("PREDICAT_PREFIX :"))       {
                                PREFIX_PREDICAT = label.split(Pattern
                                                       .quote("PREDICAT_PREFIX :"))[1]
                                                       .trim() ;
                            }

                        } // ShapeNode
                    }     // isNode Object
                }         // has Graph
            }             // dif null
        }                 // boucle iterator
    }


    private void loadEdges ( JSONObject jsonObj, int hash ) {

        if ( ! jsonObj.getJSONObject("graphml")
                      .getJSONObject("graph")
                      .has("edge") )    {
           return ;
        }

        JSONArray jsonArrayEdges = new JSONArray() ;

        if(jsonObj.getJSONObject("graphml")
                  .getJSONObject("graph")
                  .get("edge").toString().startsWith("{\"data\""))
        {
            jsonArrayEdges.put(jsonObj.getJSONObject("graphml")
                          .getJSONObject("graph")
                          .getJSONObject("edge"));
        }

        else {

            jsonArrayEdges = jsonObj.getJSONObject("graphml")
                                    .getJSONObject("graph")
                                    .getJSONArray("edge")   ;
        }

        
        for (int i = 0; i < jsonArrayEdges.length(); i++ ) {

            Object obj = jsonArrayEdges.get(i)        ;

            JSONObject jsonObject  = (JSONObject) obj ;

            if(obj.toString().startsWith("{\"data\":{")) {

                if(jsonObject.getJSONObject("data")
                             .has("y:PolyLineEdge"))
                {
                    String predicat = jsonObject.getJSONObject("data")
                                                .getJSONObject("y:PolyLineEdge")
                                                .getJSONObject("y:EdgeLabel")
                                                .getString("content") ;

                    String id    = jsonObject.getString("id")     + "_" + hash  ;

                    String sujet = jsonObject.getString("source") + "_" + hash  ;

                    String objet = jsonObject.getString("target") + "_" + hash  ;

                    Edge       e = new Edge(id, sujet, predicat, objet)         ;

                    edges.add(e) ;

                }
                
                else if ( jsonObject.getJSONObject("data").has("y:ArcEdge") )  {

                    String id    = jsonObject.getString("id")     + "_" + hash ;

                    String sujet = jsonObject.getString("source") + "_" + hash ;

                    String objet = jsonObject.getString("target") + "_" + hash ;

                    String predicat = jsonObject.getJSONObject("data")
                                                .getJSONObject("y:ArcEdge")
                                                .getJSONObject("y:EdgeLabel")
                                                .getString("content")          ;


                    Edge e = new Edge(id, sujet, predicat, objet)              ;
                    edges.add( e)                                              ;
                    
                }
                else {
                    
                    System.err.println(" ") ;
                    System.err.println(" Oops something went wrong !! ")       ;
                    System.err.println(" ") ;
                }

            }
            
            else
            
            if(obj.toString().startsWith("{\"data\":[")) {

                String predicat  = "" ;

                if(jsonObject.getJSONArray("data")
                             .getJSONObject(1).has("y:PolyLineEdge"))
                {
                    predicat = jsonObject.getJSONArray("data")
                                         .getJSONObject(1)
                                         .getJSONObject("y:PolyLineEdge")
                                         .getJSONObject("y:EdgeLabel")
                                         .getString("content") ;
                }
                else if(jsonObject.getJSONArray("data")
                                  .getJSONObject(1).has("y:QuadCurveEdge"))
                {
                    predicat = jsonObject.getJSONArray("data")
                                         .getJSONObject(1)
                                         .getJSONObject("y:QuadCurveEdge")
                                         .getJSONObject("y:EdgeLabel")
                                         .getString("content") ;
                }

                else if(jsonObject.getJSONArray("data")
                                  .getJSONObject(1).has("y:ArcEdge"))
                {
                    if(jsonObject.getJSONArray("data")
                                 .getJSONObject(1)
                                 .getJSONObject("y:ArcEdge")
                                 .has("y:EdgeLabel"))
                    {
                        predicat = jsonObject.getJSONArray("data")
                                             .getJSONObject(1)
                                             .getJSONObject("y:ArcEdge")
                                             .getJSONObject("y:EdgeLabel")
                                             .getString("content") ;
                    }
                }
                else {
                    System.out.println("Label not Found !!") ;
                }

                String id    = jsonObject.getString("id")     + "_" + hash ;

                String sujet = jsonObject.getString("source") + "_" + hash ;

                String objet = jsonObject.getString("target") + "_" + hash ;

                Edge   e     = new Edge(id, sujet, predicat, objet)        ;

                edges.add(e) ;

            }
            else {
                System.err.println(" ")          ;
                System.err.println(" Oups !! " ) ;
                System.err.println(" ")          ;
            }
        }
    }

    private boolean existPrefixStartWith ( String label ) {
        return prefix.keySet()
                     .stream()
                     .filter( pref -> !label.endsWith("::#"))
                     .filter(label::startsWith)
                     .findFirst()
                     .isPresent() ;
    }
    
    /* Write OBDA FILE */

    private void treatParallelPatterns( int hash ) {
     
        for (Map.Entry<String, String > patt : PATTERNS_PARALLEL.entrySet()) {
            
            String key     = patt.getKey()   ;
            String pattern = patt.getValue() ;
            
            String[] entities = pattern.split(Pattern.quote(".") ) ;
            
            boolean root = false ;
            
            for( String entity : entities ) {
                
                String[] subEntity = entity.trim().split(" ") ;
                
                String value = "" , uri = "" , tmpUri = ""  ;
                int codeQuery = -1 ;
                
                for( int i = 0 ; i < subEntity.length ; i++ ) {
                 
                    String token = subEntity[i] ;
                    
                    if( token.equals(",") || token.equals(";") ) {
                        value += token + " " ;
                        continue       ;
                    }
                    String type     =  null     ;
                    int code        =  -1       ;

                    if(token.contains("(") && token.contains(")")) {
                         code =  Integer.parseInt(
                                   token.split(Pattern.quote("("))[1]
                                        .replaceAll("[^0-9]", ""))  ;
                         type = token.split(Pattern.quote("("))[0]  ;
                    
                         tmpUri = tmpUris.get( code + hash )  ;                        
                    }
                            
                     if ( tmpUri == null ) return ;
                     
                     if(i == 0 )  { 
                        uri = tmpUri     ;
                        codeQuery = code ;
                        value += tmpUri + " a " +  PREFIX_PREDICAT + ":" + type + " ; " ;
                        if( ! root ) { 
                            uris_parallel_root.add( ":" + uri ) ; 
                            root = true ;
                        } 
                        
                     }
                     else {
                         if( code == -1 )
                            value += ( token.contains(":") || token.startsWith("?")) ? " " + token + " " : PREFIX_PREDICAT + ":" + token + " " ;
                         else 
                            value += ":" + tmpUri + " " ;
                     }
                }
               
                target_parallel.put( ":" + uri, value ) ;    
                if(codeQuery != -1 ) {
                  uris_num_parallel.put( ":" + uri , codeQuery )                               ;
                  uris_queries_parallel.put(    ":" + uri , source.get(hash + codeQuery) )     ;
                  uris_queries_parallel_key.put(    ":" + uri , key ) ;
                }
                
            }
                       
        }       
    }
    
    private void write(  String outFile ) throws Exception {
        
        for ( Edge edge : edges ) {

            Node sujet = nodes.get(edge.getSujet())      ;
            Node objet = nodes.get(edge.getObjet())      ;

            if(sujet == null || objet == null ) continue ;

            String objectProperty =  edge.getPredicat().contains(":") ? edge.getPredicat() :
                    
            PREFIX_PREDICAT + ":" + edge.getPredicat() ;

            if(!target.containsKey(tmpUris.get(sujet.getHash()))) {

                if( objet.getLabel().startsWith("<")           ||
                        objet.getLabel().startsWith("{")       ||
                        objet.getLabel().startsWith("\"")      ||
                        objet.getLabel().startsWith("?")       ||
                        existPrefixStartWith(objet.getLabel()) ||
                        (  objet.getLabel().startsWith(":")    &&
                           !objet.getLabel().endsWith("::#")
                        )
                ) {
                    if(!sujet.getType().startsWith(":"))      {

                        target.put( tmpUris.get(sujet.getHash())  ,
                                tmpUris.get(sujet.getHash())           +
                                        " a " +  PREFIX_PREDICAT + ":" +
                                        sujet.getType() + " ; "    +
                                        objectProperty  +  " "         +
                                        objet.getLabel() )             ;
                    }
                    else {

                        target.put( tmpUris.get(sujet.getHash())   ,
                                tmpUris.get(sujet.getHash())          +
                                        " a " + sujet.getType()   +
                                        " ; " + PREFIX_PREDICAT       +
                                        ":"   + edge.getPredicat()    +
                                        " "   + objet.getLabel() )    ;
                    }

                }

                else {
                    
                        String uri =  tmpUris.get(objet.getHash()) != null ?
                                              ":" + tmpUris.get(objet.getHash()) : 
                                              uris_num.entrySet()
                                                     .stream()
                                                     .filter(e -> e.getValue() == objet.getCode() )
                                                     .map(Map.Entry::getKey)
                                                     .findFirst()
                                                     .orElse(null) ;
                       
                        if( !sujet.getLabel().startsWith(MATCHER_PATTERN_CONTEXT)  &&
                            !sujet.getLabel().startsWith(MATCHER_PATTERN_PARALLEL )) {
                            target.put( tmpUris.get(sujet.getHash()) ,
                                 tmpUris.get(sujet.getHash())           +
                                         " a " + PREFIX_PREDICAT + ":"  +
                                         sujet.getType() + " ;  "   +
                                         objectProperty      + " "      +
                                         uri ) ;                        ;
                        }
                        else {
                            String targ = target.get(sujet.getLabel()) != null ? target.get(sujet.getLabel()) : " " ;
                            target.put( sujet.getLabel() ,
                               targ + objectProperty  + " " + uri + " _+_ " ) ;
                        }
                        
                        if( uri == null ) {
                            System.err.println(" ")         ;
                            System.err.println(" --------") ;
                            System.err.println("  Uri with code { "+ objet.getCode() + " }  not found ! ") ;
                            System.err.println("  Label { "+ objet.getLabel() + " }  ") ;
                            System.err.println("  Type  { "+ objet.getType()  + " }  ") ;
                            System.err.println(" --------") ;
                            System.err.println(" ")         ;
                        }                    
                    
                }

               if(tmpUris.get( sujet.getHash()) != null)
                    uris_queries.put( ":" + tmpUris.get( sujet.getHash() ) ,
                                    source.get(sujet.getHash()))   ;
            }
            else {               
               
                if ( objet.getLabel().startsWith("<")       ||
                     objet.getLabel().startsWith("{")       ||
                     objet.getLabel().startsWith("\"")      ||
                     objet.getLabel().startsWith("?")       ||
                     existPrefixStartWith(objet.getLabel()) ||
                     (  objet.getLabel().startsWith(":")    &&
                       !objet.getLabel().endsWith("::#") ))  {

                    if(!target.get(
                            tmpUris.get(sujet.getHash()))
                            .contains( objectProperty
                            + " " + objet.getLabel())
                    )

                        target.put( tmpUris.get(sujet.getHash()) ,
                                    target.get(
                                        tmpUris.get(sujet.getHash())) + " ; " +
                                        objectProperty +  " "                 +
                                        objet.getLabel() )                    ;
                }
                else {

                    if ( !target.get(tmpUris.get(sujet.getHash()))
                                .contains( objectProperty  + " :"
                                + tmpUris.get(objet.getHash()) )
                    )

                    { 
                        
                       String uri =  "" ;
                       
                       if( objet.getLabel().startsWith(MATCHER_PATTERN_CONTEXT)  || 
                           objet.getLabel().startsWith(MATCHER_PATTERN_PARALLEL) ||
                           objet.getLabel().startsWith(MATCHER_VARIABLE) ) {
                            
                              target.put( tmpUris.get(sujet.getHash())      ,
                                   target.get(tmpUris.get(sujet.getHash())) +
                                   " ; "  +  objectProperty   +  " "        +
                                   objet.getLabel() )       ;
                              
                       }
                       else {
                        uri =  tmpUris.get(objet.getHash()) != null ?
                                 ":" + tmpUris.get(objet.getHash()) : 
                                       uris_num.entrySet()
                                              .stream()
                                              .filter(e -> e.getValue() == objet.getCode() )
                                              .map(Map.Entry::getKey)
                                              .findFirst().get()  ;


                        target.put( tmpUris.get(sujet.getHash())             ,
                                    target.get(tmpUris.get(sujet.getHash())) +
                                    " ; "  +  objectProperty   +  " "        +
                                    uri )                                    ;
                      
                       }
                                               
                        if( uri == null ) {
                            System.err.println(" ") ;
                            System.err.println("  Uri with code { "+ objet.getCode() + " }  not found ! ") ;
                            System.err.println(" ") ;
                        }

                    }
                }
            }
        }

        for (Map.Entry<String, String> entrySet : target.entrySet()) {
            String key   = entrySet.getKey()    ;
            String value = entrySet.getValue()  ;
            
            if( !key.startsWith(MATCHER_PATTERN_CONTEXT) )
                target.put(key, ":" + value + " .") ;
            else
                target.put(key, value + " .") ;
        }

        List<String> outs    = new ArrayList<>() ;

        if( !existHeader ) {

            outs.add(PREFIXDECLARATION)  ;

            for (Map.Entry<String, String> entrySet : prefix.entrySet()) {
                String key   = entrySet.getKey()      ;
                String uri   = entrySet.getValue()    ;
                outs.add( PREF.replace("?pref", key)
                              .replace("?uri", uri))  ;
            }

            outs.add("") ;

            String SOURCE_DEC_STRING = "[SourceDeclaration]\n"              +
                                        "sourceUri	?sourceUri\n"       +
                                        "connectionUrl	?connectionUrl\n"   +
                                        "username	?username\n"        +
                                        "password	?password\n"        +
                                        "driverClass	?driverClass"       ;

            outs.add(SOURCE_DEC_STRING.replace("?sourceUri", SourceDeclaration.get("sourceUri"))
                                      .replace("?connectionUrl", SourceDeclaration.get("connectionUrl"))
                                      .replace("?username", SourceDeclaration.get("username"))
                                      .replace("?password", SourceDeclaration.get("password"))
                                      .replace("?driverClass", SourceDeclaration.get("driverClass"))
            )  ;

            outs.add("")                       ;
            outs.add(MAPPING_COLLECTION_BEGIN) ;
            outs.add("")                       ;

            existHeader = true                 ;

        }
      
        for (Map.Entry<String, String> entrySet : target.entrySet()) {

            String key    = entrySet.getKey()     ;
            String myTarget = entrySet.getValue() ;

            int num         = -10 ;
            String keyByURI = null ;
            
            if(!key.startsWith(MATCHER_PATTERN_CONTEXT)) {
                
                num = uris_num.get(myTarget.split(" ")[0]) ;
                 
                uris_num.get(myTarget.split(" ")[0]) ;

                if ( myTarget.contains ( ":null" ) ) {
                  System.err.println(" ") ;
                  System.err.println("  Null Value # Something went wrong with code { " + num + " } ") ;
                  System.err.println(" ") ;
                } 

                keyByURI = getKeyByURI("("+num+")_"+myTarget.split(" ")[0]) ;
            
            }
            else {
                 continue ;                
            }
            
            if(keyByURI.endsWith("_") ) {
                keyByURI = keyByURI.substring(0, keyByURI.length() - 1 ) ;
            }
            if ( ! myTarget.startsWith (":null") && ! myTarget.endsWith(" _+_  .") ) {

                if ( uris_queries.get(myTarget.split(" ")[0] ) == null ) {
                    throw new Exception(" No Query found for : " + myTarget.split(" ")[0] ) ;
                }

                outs.add( MAPPING_COLLECTION_PATTERN.replace("?id", keyByURI )
                                                    .replace("?target"  , myTarget)
                                                    .replace("?source"  ,
                          uris_queries.get(myTarget.split(" ")[0])).replace("  ", " " )
                ) ;

                outs.add("") ;
            }
            
        }

       if(!isGraphPattern ) {
            Writer.checkFile(outFile)           ;
            Writer.writeTextFile(outs, outFile) ;
            Writer.writeTextFile(Collections.singletonList(MAPPING_COLLECTION_END), outFile) ;
       }
       
       else  {           
                               
             List<String> copyOuts ;
             
             String _fileName = outFile.substring(0, outFile.lastIndexOf('.')) ;
             
             String extension = outFile.substring(outFile.lastIndexOf('.')) ;
             
             for(String vari : VARIABLES ) {
                
                String pattern_id = null , variable  ;

                if( vari.trim()
                        .split(Pattern.quote("&&"))[0]
                        .replaceAll(" +", " ")
                        .split(" ")[0]
                        .startsWith(MATCHER_PATTERN_CONTEXT))  {
                    
                    pattern_id = vari.trim().replaceAll(" +", " ").split(" ")[0] ;
                    variable   = vari.trim().replaceAll(" +", " ").split(" ")[1] ;
                                     
                }
                else {
                    variable   = vari.trim().replaceAll(" +", " ").split(" ")[0] ;
                }
                
                Pattern p = Pattern.compile("\\{.*?\\}") ;
                Matcher m = p.matcher(vari) ;

                copyOuts = new ArrayList<>(outs) ;
                
                if(pattern_id != null ) {
                  outs.addAll(getOutForPattern(pattern_id ));
                }
                else {
                    target.get(MATCHER_PATTERN_CONTEXT);
                }
                
                while (m.find()) {
                     
                    String param = m.group().replace("{", "")
                                    .replace("}","").trim().replaceAll(" +", " ") ;
                    
                    String param_0 = param.split("=")[0]              ;
                    String param_1 = param.split("=")[1]              ;
                    outs.replaceAll( x -> x.replace(param_0,param_1)) ;
                }
               
                if( linker != null ) {
                    outs.replaceAll( x -> x.replace( MATCHER_PATTERN_CONTEXT , linker )
                                           .replace( MATCHER_VARIABLE, variable  )
                                           .replace( MATCHER_ENTITY  , variable )
                    );
                }
                else {
                    
                    /* the ( ;.* ) is used to remove the object property of MATCHER_CONTEXT */
                    
                    outs.replaceAll( x -> x.replaceAll(";.* " + MATCHER_PATTERN_CONTEXT , target.get(MATCHER_PATTERN_CONTEXT) )
                                           .replace("_+_  .", "") 
                                           .replace(" _+_ ", " ; ")
                                           .replace( MATCHER_VARIABLE, variable  )
                                           .replace( MATCHER_ENTITY  , variable )
                    ) ;
                }

                
                /* PATTERN_PARALLEL */ 
                
                String[] patternParallel = null ;
                
                if(vari.contains("&&")) {                    
                    patternParallel = vari.trim()
                                          .split(Pattern.quote("&&"))[1]
                                          .replaceAll(" +", " ")
                                          .split(";") ;
                }
                                              
                int i = 1 ;
                 
                String parallel_root_uris     =   String.join(" , ", uris_parallel_root) ;
                
                String parallel_root_uris_out =   "" ;
                String parallel_root_uris_tmp =   "" ;
                
                if( patternParallel != null ) {
                    
                
                    for( String  pattern_parallels : patternParallel ) {

                        String pattern_key = pattern_parallels.trim().split(" ")[0];

                        Matcher matcher    = p.matcher(pattern_parallels) ;

                        parallel_root_uris_tmp = parallel_root_uris ;

                        List<String> uris_parallel = uris_queries_parallel_key.entrySet()
                                                                              .stream()
                                                                              .filter(map -> map.getValue()
                                                                              .equals(pattern_key))
                                                                              .map( val -> val.getKey().replaceFirst(":", ""))
                                                                              .collect(toList()) ;

                        Map<String,  String>  kay_values  =  new  HashMap<>()   ;

                        while (matcher.find()) {

                            String param = matcher.group()
                                                  .replace("{", "")
                                                  .replace("}","")
                                                  .trim().replaceAll(" +", " ") ;

                            String key   = param.split("=")[0]  ;
                            String value = param.split("=")[1]  ;

                            kay_values.put(key, value)          ;

                        }

                        for( String uri : uris_parallel ) {

                              String key_uri =  getKeyByURI ( "("+ uris_num_parallel.get(":"+uri)+")_"+variable ) ;
                              String targ    =  target_parallel.get( ":" + uri ) ;
                              String query   =  uris_queries_parallel.get(":" + uri ) ;

                              key_uri = key_uri + "_" + i++  ;

                              for (Map.Entry<String, String> entry : kay_values.entrySet()) {
                                  String key   = entry.getKey()   ;
                                  String value = entry.getValue() ;

                                  targ               = targ.replace(  key , value )           ;
                                  query              = query.replace( key , value )           ;
                                  parallel_root_uris_tmp = parallel_root_uris_tmp.replace(key, value ) ;

                            } 

                            outs.add( MAPPING_COLLECTION_PATTERN
                                      .replace("?id", MATCHER_PATTERN_PARALLEL.replace("##", "") + "_" + key_uri )
                                      .replace("?target"  , ":" + targ + "." )
                                      .replace("?source"  , query )
                            ) ;

                            outs.add( "");

                        }

                        parallel_root_uris_out = String.join(" , ", parallel_root_uris_out, parallel_root_uris_tmp ) ;

                    }
                }
                
                String final_root_uris =  parallel_root_uris_out.replaceFirst(", ", "") ;                                          
                
                outs.replaceAll( x -> x.replaceAll(MATCHER_PATTERN_PARALLEL,   final_root_uris )) ;
                
                String fileName =  _fileName + "_" + variable.replaceFirst(":", "") + extension ;
                
                Writer.checkFile( fileName )  ;
                
                Writer.writeTextFile(outs, fileName ) ;
                Writer.writeTextFile(Collections.singletonList(MAPPING_COLLECTION_END), fileName ) ;
                
                outs = new ArrayList<>(copyOuts) ;
                
             }  
       }
        
    }

    private List<String> getOutForPattern( String patternId ) {
      
        List<String> out      = new ArrayList<>() ;
        
        linker                = null              ;
        
        int num_start         = Integer.parseInt(PATTERNS.get(patternId).split(" ")[0]) ;
                  
        String URI_PATTERN    = PATTERNS.get(patternId).split(" ")[1]  ;
                  
        String objectProperty =  PATTERNS.get(patternId).split(" ")[2] ;
                  
        String  keyByURI      = getKeyByURI("("+ patternId.replace("##", "") +")") ;
        
        Pattern p             = Pattern.compile("\\[.*?\\]") ;
        
        Matcher m             = p.matcher(PATTERNS.get(patternId)) ;
       
        List<String> vars = new ArrayList();
        
        while (m.find()) {
        
            vars.add( m.group().replace("[", "").replace("]","").trim() ) ;
        
        }
            
        for (int i = 0; i < vars.size(); i++) {

            String entity = vars.get(i).split(" " )[0]  ;
            String type   = entity.split("_")[0]        ;
            String classe = entity.split("_")[1]        ;

            int numQuery = Integer.parseInt(vars.get(i)
                                  .split(" " )[1].split("_")[1]) ;
                
            String query = source.get(sourceByCode.get(numQuery)) ;
                
            if( query == null ) {
                System.out.println("")   ;
                System.out.println(" NumQuery [ "+numQuery+" ] not found in numUris Map !! " ) ;
                System.out.println("")   ;
            }
            
            String uri = URI_PATTERN.replace(MATCHER_ENTITY , classe.toLowerCase(Locale.FRENCH) ) ;

            if(! uri.startsWith(Pattern.quote(":"))) uri = ":" + uri  ;

            if(i == vars.size() -1 ) {

                  out.add( MAPPING_COLLECTION_PATTERN
                          .replace("?id", keyByURI+"_"+classe+ "_"+num_start++ )
                          .replace("?target"  ,  uri + " a " + type + " ; " +
                           OF_ENTITY_PATTERN + " :" + classe + " ; " + 
                           target.get(MATCHER_PATTERN_CONTEXT).replace("_+_  .", ".") )
                          .replace(" _+_ ", " ; ")
                          .replace("?source"  , query )
                  ) ;
                  
            }

            else {

              String nextEntityClass = vars.get(i+1).split(" ")[0].split("_")[1] ;
              String nextUri = URI_PATTERN.replace( MATCHER_ENTITY , nextEntityClass.toLowerCase() ) ;

              if( ! nextUri.startsWith(Pattern.quote(":")))
                  nextUri = " :" + URI_PATTERN.replace( MATCHER_ENTITY , nextEntityClass.toLowerCase() ) ;
              else
                  nextUri = " " + URI_PATTERN.replace( MATCHER_ENTITY , nextEntityClass.toLowerCase() ) ;

              out.add( MAPPING_COLLECTION_PATTERN
                       .replace("?id", keyByURI+"_"+classe+ "_"+num_start++ )
                       .replace("?target"  , uri + " a " + type + " ; " +
                        OF_ENTITY_PATTERN + " :" + classe + " ; " + objectProperty + nextUri + " ." )
                       .replace("?source"  , query )
              ) ;

            }
            
            if(i == 0 ) {
               linker = uri ;
            }

            out.add("") ;
        }
               
        return out ;
    }
    
    
    private String getKeyByURI(String target) {
        String code =  target.replaceAll(Pattern.quote("/{"), "_")
                             .replaceAll(Pattern.quote("-{"), "_")
                             .replaceAll(Pattern.quote("/"), "_" )
                             .replaceAll(Pattern.quote("{"), "_" )
                             .replaceAll(Pattern.quote("}"), "_" )
                             .replaceAll(Pattern.quote(":"), "_" )
                             .replaceAll("_+", "_")              
                             .replaceAll("##", "")              ;
                             
        if(code.startsWith("_")) return code.substring(1, code.length()) ;
        return code ;
    }

    private int getHash(String pathFile) {
        return  pathFile.hashCode() ;
    }

    private void process( String pathFile ) throws IOException {

        JSONObject jsonObject = loadJsonObject(pathFile) ;
        int        hash       = getHash(pathFile)        ;
        loadNodes (jsonObject, hash)                     ;
        loadEdges (jsonObject, hash )                    ;
        treatParallelPatterns( hash )                    ;
        
    }


    public void entryProcess( String directory       ,
                              String outObdaPathFile ,
                              String extensionFile ) throws Exception {

        existHeader       = false  ;

        boolean processed = false  ;

        List<Path> files = Files.list(new File(directory).toPath()).collect(toList()) ;

        for(Path path : files ) {
            if(path.toString().endsWith(extensionFile )) {
                process(path.toString() ) ;
                if ( ! processed ) processed = true ;
            }
        }

        if( processed ) {
           write(outObdaPathFile) ;
        }
        else {
            System.out.println ( " No File with extension '" +extensionFile + "' found !! " ) ;
            System.out.println ( "                                                        " ) ;
        }

    }
}
