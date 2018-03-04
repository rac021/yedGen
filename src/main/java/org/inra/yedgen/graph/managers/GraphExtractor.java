
package org.inra.yedgen.graph.managers ;

import org.json.XML ;
import java.io.File ;
import java.util.Map ;
import java.util.Set ;
import java.util.List ;
import java.util.Locale ;
import java.util.HashMap ;
import org.json.JSONArray ;
import java.nio.file.Path ;
import org.json.JSONObject ;
import java.io.IOException ;
import java.nio.file.Files ;
import java.nio.file.Paths ;
import java.util.regex.Pattern ;
import org.inra.yedgen.graph.utils.Utils ;
import org.inra.yedgen.graph.entities.Edge ;
import org.inra.yedgen.processor.logs.Messages ;
import static java.util.stream.Collectors.toList ;

/**
 *
 * @author ryahiaoui
 */
public class GraphExtractor {
 
    private final Map< Integer, Map< Integer, String>> mapUris             ;
    private final Map< Integer, Set<Edge>>             mapEdges            ;
    private final Map< Integer, Map< Integer, String>> mapQueries          ;
    private final Map< Integer, Map< String, String>>  mapConcepts         ;
    private final Map< Integer, Map< String, String>>  mapVariables        ;
    private final Map< Integer, Map< String, String>>  mapPatternContexts  ;
    private final Map< Integer, Map< String, String>>  mapPatternParallels ;

    
    private String metaPatternVariable                                     ;
    private String metaPatternContext                                      ;
    private String metaPatternParallel                                     ;
    private String magicFilter                                             ;
    
    private Integer metaPatternHash                                        ;
    
    private final Map<String,  String>  SourceDeclaration                  ;
    private final Map<String , String>  prefixs                            ;

    public static       String  PREFIX_PREDICAT          =  "NULL-PREFIX"       ;
   
    public static final String  PATTERN_CONTEXT          = "##PATTERN_CONTEXT"  ;
    public static final String  PATTERN_PARALLEL         = "##PATTERN_PARALLEL" ;
    public static       String  PREDICAT_PATTERN_CONTEXT = "oboe-core:ofEntity" ;
    
    public static final String  META_PATTERN_CONTEXT  = "##META_PATTERN_CONTEXT"  ;
    public static final String  META_PATTERN_PARALLEL = "##META_PATTERN_PARALLEL" ;
    public static final String  META_VERIABLE         = "?META_VARIABLE"          ;
    public static final String  VARIABLE              = "?VARIABLE"               ;
    public static final String  MAGIC_FILTER          = "##Magic_Filter "         ;
    
    private static boolean  isMetaGraph                = false                     ;
    private static boolean  containsPaternParralel     = false                     ;
    private static boolean  containsPaternContext      = false                     ;
    private static boolean  containsVariables          = false                     ;
    
    private  JSONObject loadJsonObject ( String pathFile ) throws IOException  {

        /** String xml ;

        try ( InputStream inputStream = new FileInputStream(pathFile) )  {

            StringBuilder builder = new StringBuilder() ;

            int ptr ;

            while ((ptr = inputStream.read()) != -1 )
            {
                builder.append((char) ptr) ;
            }

            xml = builder.toString()       ;
        } **/
     
        String xml  = new String ( Files.readAllBytes( Paths.get(pathFile) ) ) ;
        
        return XML.toJSONObject(xml)       ;
    }

    private void loadConcepts ( JSONObject jsonObj , Integer hash )  {

        JSONArray jsonArrayConcepts = jsonObj.getJSONObject("graphml")
                                             .getJSONObject( "graph" )
                                             .getJSONArray( "node" ) ;

        for (int i = 0; i < jsonArrayConcepts.length(); i++)         {

            Object obj                    = jsonArrayConcepts.get(i) ;
            
            JSONObject jsonObjectConcept  = (JSONObject) obj         ;

            if(obj != null) {

                if(obj.toString().startsWith("{\"data\":{"))                    {

                    String label = jsonObjectConcept.getJSONObject("data")
                                                    .getJSONObject("y:ShapeNode")
                                                    .getJSONObject("y:NodeLabel")
                                                    .getString("content").trim()
                                                    .replaceAll(" +", " ") ;
  
                    checkAndSetIfIsMetaGraph( label ) ;
                   
                    // String  id    =  jsonObjectConcept.getString("id")  ;
                    // Utils.putInMap(mapConcepts, hash, id , label )      ;
                 
                    String  id    =  jsonObjectConcept.getString("id")     ;
                    
                    Integer code                                           ;
                 
                    if ( label.startsWith(PATTERN_CONTEXT) && label.contains(" ")) {
                        // For the PATTERN_CONTEXT Declared in the Graph
                        Utils.putInMap( mapPatternContexts  , 
                                        hash                , 
                                        label.split(" ")[0] , 
                                        label.replaceFirst(Pattern.quote(label
                                             .split(" ")[0]),"").trim() ) ; 
                    }
                                    
                    else if ( label.startsWith( PATTERN_PARALLEL ) && label.contains(" ")) {
                        // For the PATTERN_PARALLEL Declared in the Graph                   
                        Utils.putInMap( mapPatternParallels , 
                                        hash                , 
                                        label.split(" ")[0] , 
                                        label.replaceFirst(Pattern.quote(label
                                             .split(" ")[0]),"").trim() ) ;  
                    }
                                    
                    else if ( label.startsWith( VARIABLE ) && label.contains(" "))    {
                                        
                        Utils.putInMap( mapVariables , 
                                        hash         , 
                                        id           , 
                                        label.trim().replaceFirst( Pattern.quote ( 
                                                                   VARIABLE ) , "") ) ;  
                    }
                                    
                    else if (label.startsWith(META_VERIABLE) && label.contains(" "))  {
                        metaPatternVariable = label.replaceFirst (
                                                    Pattern.quote(META_VERIABLE),"" ) ;
                        metaPatternHash     = hash                                    ;
                    }
                    
                    else if ( label.startsWith(META_PATTERN_CONTEXT) && label.contains(" "))  {
                        metaPatternContext = label.replaceFirst (
                                                   Pattern.quote( META_PATTERN_CONTEXT),"" )  ;
                    }
                    
                    else if ( label.startsWith(META_PATTERN_PARALLEL) && label.contains(" ")) {
                        metaPatternParallel = label.replaceFirst(
                                                    Pattern.quote(META_PATTERN_PARALLEL),"" ) ;
                    }
                    
                    else if ( label.trim().replaceAll(" +", " ").startsWith(MAGIC_FILTER) && 
                              label.contains(":"))                                         { 
                        magicFilter = label.trim().replaceAll(" +", " ")
                                           .replaceFirst(Pattern.quote(MAGIC_FILTER),"")
                                           .split(Pattern.quote(":"))[1] ;
                    }

                    else if (label.toLowerCase().startsWith("query_(")) {
                                        
                        code =  Integer.parseInt(label
                                       .split(Pattern.quote(":"))[0]
                                       .split(Pattern.quote("_"))[1]
                                       .replaceAll("[^0-9]", ""))  ;

                        Utils.putInMap( mapQueries , 
                                        hash , 
                                        code , 
                                        label.split( Pattern.quote(": "))[1]
                                             .trim()
                                             .replaceAll("--.*\\n", "")
                                             .replaceAll("--.*", ""   ) 
                                             .replaceAll(" +", " ")   ) ;
                    }
                     
                    else if (  label.toLowerCase().startsWith("(")   && 
                               label.toLowerCase().contains(")")     &&
                             ! label.toLowerCase().trim().endsWith(")") ) {
                                                
                        code =  Integer.parseInt(label
                                       .split(Pattern.quote(")"))[0]
                                       .replaceAll("[^0-9]", ""))  ;

                        Utils.putInMap( mapUris , 
                                        hash    , 
                                        code    , 
                                        label.split(Pattern
                                             .quote(")")) [1]
                                             .trim())       ;
                    }
                     
                    else if (label.toLowerCase().startsWith("prefix "))  {
                        String pref = label.split(Pattern.quote(" "))[1] ;
                        String uri  = label.split(Pattern.quote(" "))[2] ;

                        prefixs .put(pref, uri) ;
                    }
                    
                    else  if (label.startsWith("PREDICAT_PREFIX :"))           {
                                        
                        PREFIX_PREDICAT = label.split(Pattern
                                               .quote("PREDICAT_PREFIX :"))[1] ;
                    }
                    
                    else if (label.toLowerCase().startsWith("obda-"))   {

                        if  ( label.split(Pattern.quote(" : ")) [0]
                                   .equals("obda-sourceUri"))     {
                                                       
                            SourceDeclaration.put("sourceUri" ,
                                    label.split( Pattern
                                         .quote(" : "))[1])   ;
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
                                             .quote(" : "))[1])      ;
                        }
                    }
                    
                    else {
                            Utils.putInMap(mapConcepts, hash, id , label ) ;
                    }                    
                }

                if ( jsonObjectConcept.has("graph")) {

                    if ( jsonObjectConcept.getJSONObject("graph")
                                          .toString().startsWith("{\"node\":["))  {

                        JSONArray jsonArrayGroupConcepts =
                                jsonObjectConcept.getJSONObject("graph")
                                                 .getJSONArray("node") ;

                        for (int j = 0; j < jsonArrayGroupConcepts.length(); j++) {

                            if ( jsonArrayGroupConcepts.toString().startsWith("{\"data\":[")    ||
                                    jsonArrayGroupConcepts.toString().startsWith("[{\"data\":[") ) {

                                if ( jsonArrayGroupConcepts.getJSONObject(j)
                                                           .getJSONArray("data")
                                                           .getJSONObject(1)
                                                           .has("y:ShapeNode")) {

                                    String id = jsonArrayGroupConcepts.getJSONObject(j)
                                                                      .getString("id") ;

                                    String label = jsonArrayGroupConcepts.getJSONObject(j)
                                                                         .getJSONArray("data")
                                                                         .getJSONObject(1)
                                                                         .getJSONObject("y:ShapeNode")
                                                                         .getJSONObject("y:NodeLabel")
                                                                         .getString("content").trim()
                                                                         .replaceAll(" +", " ") ;
                                    
                                    checkAndSetIfIsMetaGraph( label ) ;
                                     
                                    Integer code ;

                                    if (label.toLowerCase(Locale.FRENCH).startsWith("query_(")) {
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(":"))[0]
                                                       .split(Pattern.quote("_"))[1]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        Utils.putInMap( mapQueries, hash, code , label.split( Pattern
                                                                                .quote(": "))[1]
                                                                                .trim()
                                                                                .replaceAll("--.*\\n", "")
                                                                                .replaceAll("--.*", ""   ) 
                                                                                .replaceAll(" +", " ")   ) ;
                                    }
                                    
                                    else if (label.toLowerCase()
                                                  .startsWith("(") && 
                                                  label.toLowerCase().contains(")") ) {
                                                      
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(")"))[0]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        Utils.putInMap( mapUris, hash, code , label.split( Pattern
                                                                                   .quote(")"))[1]
                                                                                   .trim())      ;
                                    }
                                }
                            }

                            else

                            if ( jsonArrayGroupConcepts.toString().startsWith("{\"data\":{")   ||
                                 jsonArrayGroupConcepts.toString().startsWith("[{\"data\":{") ) {

                                if ( jsonArrayGroupConcepts.getJSONObject(j)
                                                        .getJSONObject("data")
                                                        .has("y:ShapeNode"))                            {

                                    String id = jsonArrayGroupConcepts.getJSONObject(j).getString("id") ;
                                    
                                    String label = jsonArrayGroupConcepts.getJSONObject(j)
                                                                         .getJSONObject("data")
                                                                         .getJSONObject("y:ShapeNode")
                                                                         .getJSONObject("y:NodeLabel")
                                                                         .getString("content").trim()
                                                                         .replaceAll(" +", " ") ;
                                    
                                    checkAndSetIfIsMetaGraph( label ) ;
                                    
                                    if (label.startsWith(PATTERN_CONTEXT) && label.contains(" ")) {
                                            // For the PATTERN_CONTEXT Declared in the Graph
                                            Utils.putInMap( mapPatternContexts, 
                                                            hash, 
                                                            label.split(" ")[0] , 
                                                            label.replaceFirst(Pattern.quote(label
                                                                 .split(" ")[0]),"").trim() ) ; 
                                    }
                                    
                                    if (label.startsWith(PATTERN_PARALLEL) && label.contains(" ")) {
                                             // For the PATTERN_PARALLEL Declared in the Graph 
                                            Utils.putInMap( mapPatternParallels, 
                                                            hash, 
                                                            label.split(" ")[0] , 
                                                            label.replaceFirst(Pattern.quote(label
                                                                 .split(" ")[0]),"").trim() ) ;  
                                    }
                                    
                                    else if (label.startsWith( VARIABLE ) && label.contains(" ") )  {
                                        
                                        Utils.putInMap( mapVariables, 
                                                        hash, 
                                                        id , 
                                                        label.trim().replaceFirst( Pattern.quote ( 
                                                                                   VARIABL ), "" ) ) ;  
                                    }
                                    
                                    else if (label.startsWith(META_VERIABLE) && label.contains(" "))              {
                                        metaPatternVariable = label.replaceFirst(Pattern.quote(META_VERIABLE),"") ;
                                        metaPatternHash     = hash                                                ;
                                    }
                                    else if ( label.startsWith(META_PATTERN_CONTEXT) && label.contains(" "))  {
                                        metaPatternContext = label.replaceFirst(Pattern.quote(META_PATTERN_CONTEXT),"")   ;
                                    }
                                    else if ( label.startsWith(META_PATTERN_PARALLEL) && label.contains(" ")) {
                                        metaPatternParallel = label.replaceFirst(Pattern.quote(META_PATTERN_PARALLEL),"") ;
                                    }
                                    else if ( label.trim().replaceAll(" +", " ").startsWith(MAGIC_FILTER) && 
                                              label.contains(":"))                                         { 
                                        magicFilter = label.trim().replaceAll(" +", " ")
                                                           .replaceFirst(Pattern.quote(MAGIC_FILTER),"")
                                                           .split(Pattern.quote(":"))[1] ;
                                    }

                                    int code ;

                                    if (label.toLowerCase().startsWith("query_(")) {
                                        
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(":"))[0]
                                                       .split(Pattern.quote("_"))[1]
                                                       .replaceAll("[^0-9]", ""))  ;

                                         Utils.putInMap( mapQueries , 
                                                         hash , 
                                                         code , 
                                                         label.split( Pattern.quote(": "))[1]
                                                              .trim()
                                                              .replaceAll("--.*\\n", "")
                                                              .replaceAll("--.*", ""   ) 
                                                              .replaceAll(" +", " ")   ) ;
                                    }
                                    
                                    else
                                    
                                    if ( label.toLowerCase().startsWith("(") 
                                         && label.toLowerCase().contains(")") )   {
                                                
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(")"))[0]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        Utils.putInMap( mapUris , 
                                                        hash    , 
                                                        code    , 
                                                        label.split(Pattern
                                                             .quote(")")) [1]
                                                             .trim())       ;
                                    }
                                    else
                                    if (label.toLowerCase().startsWith("prefix "))       {
                                        String pref = label.split(Pattern.quote(" "))[1] ;
                                        String uri  = label.split(Pattern.quote(" "))[2] ;

                                        prefixs .put(pref, uri) ;
                                    }
                                    else
                                    if (label.startsWith("PREDICAT_PREFIX :"))                 {
                                        
                                        PREFIX_PREDICAT = label.split(Pattern
                                                               .quote("PREDICAT_PREFIX :"))[1] ;
                                        
                                    }
                                    else
                                    if (label.toLowerCase().startsWith("obda-"))   {

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
                                                             .quote(" : "))[1])      ;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else

                    if(jsonObjectConcept.getJSONObject("graph")
                                        .toString().startsWith("{\"node\":{")) {

                        JSONObject jsonArrayGroupNodes = jsonObjectConcept.
                                                         getJSONObject("graph" )
                                                        .getJSONObject("node") ;

                        if( jsonArrayGroupNodes.getJSONObject("data").has("y:ShapeNode")) {

                            String id = jsonArrayGroupNodes.getString("id") ;

                            String label = jsonArrayGroupNodes.getJSONObject("data")
                                                              .getJSONObject("y:ShapeNode")
                                                              .getJSONObject("y:NodeLabel")
                                                              .getString("content").trim()
                                                              .replaceAll(" +", " ")     ;

                            checkAndSetIfIsMetaGraph( label ) ;

                            int code ;

                            if (label.startsWith(PATTERN_CONTEXT) && label.contains(" ")) {
                                // For the PATTERN_CONTEXT Declared in the Graph    
                                Utils.putInMap( mapPatternContexts     , 
                                                hash                   , 
                                                label.split(" ")[0]    , 
                                                label.replaceFirst(Pattern.quote(label
                                                                           .split(" ")[0]),"").trim() ) ; 
                                
                            }
                            else if (label.startsWith(PATTERN_PARALLEL) && label.contains(" ")) {
                                // For the PATTERN_PARALLEL Declared in the Graph 
                                Utils.putInMap( mapPatternParallels , 
                                                hash                ,
                                                label.split(" ")[0] , 
                                                label.replaceFirst(Pattern.quote( label
                                                     .split(" ")[0]),"").trim() ) ; 

                            }
                            
                            else if (label.startsWith( VARIABLE ) && label.contains(" ") )      {  

                                Utils.putInMap( mapVariables , 
                                                hash         , 
                                                id           , 
                                                label.trim().replaceFirst(label.split(" ")[0],"") ) ;  
                            }
                            
                            else if (label.startsWith(META_VERIABLE) && label.contains(" ")) {
                                  metaPatternVariable = label.replaceFirst(Pattern.quote(META_VERIABLE),"")        ;
                                  metaPatternHash     = hash                                                       ;
                            } 
                            else if (label.startsWith(META_PATTERN_CONTEXT) && label.contains(" ")) { 
                                  metaPatternContext = label.replaceFirst(Pattern.quote(META_PATTERN_CONTEXT),"")  ;
                            }
                            else if (label.startsWith(META_PATTERN_PARALLEL) && label.contains(" ")) { 
                                 metaPatternParallel = label.replaceFirst(Pattern.quote(META_PATTERN_PARALLEL),"") ;
                            }
                            else if ( label.trim().replaceAll(" +", " ").startsWith(MAGIC_FILTER) && label.contains(":")) { 
                                        magicFilter = label.trim().replaceAll(" +", " ")
                                                           .replaceFirst(Pattern.quote(MAGIC_FILTER),"")
                                                           .split(Pattern.quote(":"))[1] ;
                            }

                            else if(label.toLowerCase().startsWith("query_("))  {
                                
                                code =  Integer.parseInt(label
                                               .split(Pattern.quote(":"))[0]
                                               .split(Pattern.quote("_"))[1]
                                               .replaceAll("[^0-9]", ""))  ;

                                Utils.putInMap( mapQueries , 
                                                hash ,
                                                code , 
                                                label.split( Pattern
                                                     .quote(": "))[1]
                                                     .trim()
                                                     .replaceAll("--.*\\n", "")
                                                     .replaceAll("--.*", ""   ) 
                                                     .replaceAll(" +", " ")   ) ;
                            }
                            
                            else
                                
                            if( label.toLowerCase().startsWith("(") 
                                && label.toLowerCase().contains(")") )     {
                                      
                                code =  Integer.parseInt(label
                                               .split(Pattern.quote(")"))[0]
                                               .replaceAll("[^0-9]", ""))  ;

                                Utils.putInMap( mapUris , 
                                                hash    ,
                                                code    , 
                                                label.split(Pattern
                                                     .quote(")"))[1]
                                                     .trim()) ;
                            }
                            
                            else
                            
                            if(label.toLowerCase().startsWith("prefix "))        {
                                String pref = label.split(Pattern.quote(" "))[1] ;
                                String uri  = label.split(Pattern.quote(" "))[2] ;
                                prefixs .put(pref, uri)                          ;
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
                      .has("edge") )         {
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

                    String id    = jsonObject.getString("id")         ;

                    String sujet = jsonObject.getString("source")     ;

                    String objet = jsonObject.getString("target")     ;

                    Edge       e = new Edge( hash, id, sujet, predicat, objet ) ;

                    Utils.putInMap(mapEdges, hash, e) ;

                }
                
                else if ( jsonObject.getJSONObject("data").has("y:ArcEdge") )  {

                    String id    = jsonObject.getString("id")     ;

                    String sujet = jsonObject.getString("source") ;

                    String objet = jsonObject.getString("target") ;

                    String predicat = jsonObject.getJSONObject("data")
                                                .getJSONObject("y:ArcEdge")
                                                .getJSONObject("y:EdgeLabel")
                                                .getString("content")          ;

                    Edge e = new Edge( hash , id, sujet, predicat, objet)      ;

                    Utils.putInMap(mapEdges, hash, e) ; 
                    
                }
                else {
                    Messages.printExtractionError()   ;
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
                    Messages.printNotFoundLabelError()         ;
                }

                String id    = jsonObject.getString("id")      ;

                String sujet = jsonObject.getString("source")  ;

                String objet = jsonObject.getString("target")  ;

                Edge   e     = new Edge( hash     , 
                                         id       , 
                                         sujet    , 
                                         predicat , 
                                         objet    ) ;

                Utils.putInMap( mapEdges, hash, e ) ; 

            }
            else {
                Messages.printExtractionError()     ;
            }
        }
    }

    private void process( String pathFile ) throws IOException {

        JSONObject jsonObject = loadJsonObject(pathFile) ;
        int        hash       = Utils.getHash(pathFile)  ;
        loadConcepts ( jsonObject, hash )                ;
        loadEdges ( jsonObject, hash )                   ;
    }   
    
    public void genGraphPopulatingManagers( String directory     ,
                                            String extensionFile ) throws Exception   {

        Messages.printMessageExtractGraph( directory ) ;
        
        boolean processed = false  ;

        List<Path> files = Files.list(new File(directory).toPath()).collect(toList()) ;

        for(Path path : files ) {
         
            if(path.toString().endsWith(extensionFile )) {
             
                Messages.printMessageProcessingGraphFile( path.toAbsolutePath()
                                                                   .toString())      ;
             
                process(path.toString() )                                            ;
                if ( ! processed ) processed = true                                  ;
            }
        }
      
        if ( ! processed ) {
         
            Messages.printMessageFilesNotFoundExtentsion( directory, 
                                                          extensionFile )           ;
            System.exit ( 0 )                                                       ; 
        }
     
        Messages.printSeparator() ;
    }

    /*
    private void checkAndSetIfIsMetaGraph( String label )  {
        
      if ( label.trim().equals(MATCHER_PATTERN_CONTEXT)  ||
           label.trim().equals(MATCHER_PATTERN_PARALLEL) ||
           ( label.trim().startsWith("?") && 
             ! label.trim().contains(" ")   ) 
         ) {
             if ( ! isMetaGraph ) isMetaGraph = true ;
      }     
    }
    */
    
    private void checkAndSetIfIsMetaGraph( String label )  {
        
      if ( label.trim().equals(PATTERN_CONTEXT) )          {
           if ( ! isMetaGraph )           isMetaGraph           = true ;            
           if ( ! containsPaternContext ) containsPaternContext = true ;            
      }
      else if ( label.trim().equals(PATTERN_PARALLEL ))    {
           if ( ! isMetaGraph )            isMetaGraph            = true ;
           if ( ! containsPaternParralel ) containsPaternParralel = true ;            
      }
      else if ( label.trim().startsWith("?") && ! label.trim().contains(" ") ) {
           if ( ! isMetaGraph )       isMetaGraph       = true ;
           if ( ! containsVariables ) containsVariables = true ;            
      }
    }
    
    public Map<Integer, Map<Integer, String>> getMapUris() {
        return mapUris ;
    }

    public Map<Integer, Set<Edge>> getMapEdges()           {
        return mapEdges ;
    }

    public Map<Integer, Map<Integer, String>> getMapQueries() {
        return mapQueries ;
    }

    public Map<Integer, Map<String, String>> getMapConcepts() {
        return mapConcepts ;
    }

    public Map<Integer, Map<String, String>> getMapPatternContexts()  {
        return mapPatternContexts ;
    }

    public Map<Integer, Map<String, String>> getMapPatternParallels() {
        return mapPatternParallels ;
    }

    public Map<Integer, Map<String, String>> getMapVariables()        {
        return mapVariables ;
    }

    public String getMetaPatternVariable()  {
        return metaPatternVariable ;
    }

    public String getMetaPatternContext()   {
        return metaPatternContext ;
    }

    public String getMetaPatternParallel()  {
        return metaPatternParallel ;
    }

    public Map<String, String> getPrefixs() {
        return prefixs ;
    }

    public Map<String, String> getSourceDeclaration() {
        return SourceDeclaration ;
    }

    public Integer getMetaPatternHash()  {
        return metaPatternHash ;
    }

    public String getMagicFilter()      {
        return magicFilter ;
    }
    
    public void setMagicFilter( String magicFilter ) {
        this.magicFilter =  magicFilter ;
    }

    public static boolean isMetaGraph() {
        return isMetaGraph ;
    }

    public static boolean containsPaternParralel() {
        return containsPaternParralel ;
    }

    public static boolean containsPaternContext() {
        return containsPaternContext ;
    }

    public static boolean containsVariables() {
        return containsVariables ;
    }
       
    /* Constructor */
    
    public GraphExtractor ()                  {
        
       mapUris              = new HashMap<>() ;
       mapEdges             = new HashMap<>() ;
       mapQueries           = new HashMap<>() ;
       mapConcepts          = new HashMap<>() ;
       mapVariables         = new HashMap<>() ;
       mapPatternContexts   = new HashMap<>() ;
       mapPatternParallels  = new HashMap<>() ;       
       SourceDeclaration    = new HashMap<>() ;
       prefixs              = new HashMap<>() ;
    }

}
