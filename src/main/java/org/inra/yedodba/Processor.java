
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
import static java.util.stream.Collectors.toList ;

/**
 *
 * @author ryahiaoui
 */
public class Processor {

    private final Set<Edge>             edges               =  new  HashSet<>() ;
    private final Map<String , Node  >  nodes               =  new  HashMap<>() ;
    private final Map<String , String>  prefix              =  new  HashMap<>() ;
    private final Map<Integer, String>  tmpUris             =  new  HashMap<>() ;
    private final Map<String,  String>  uris                =  new  HashMap<>() ;
    private final Map<String,  Integer> numUris             =  new  HashMap<>() ;
    private final Map<Integer, String>  source              =  new  HashMap<>() ;
    private final Map<String,  String>  target              =  new  HashMap<>() ;
    private final Map<String,  String>  SourceDeclaration   =  new  HashMap<>() ;

    private static String  PREFIX_PREDICAT         =  "oboe-coreX"             ;
    private static final String  PREFIXDECLARATION = "[PrefixDeclaration]"     ;
    private static final String  PREF              =  "?pref		?uri"  ;

    private static final String MAPPING_COLLECTION_BEGIN   = "[MappingDeclaration] @collection [[" ;

    private static final String MAPPING_COLLECTION_PATTERN =  "mappingId	?id\n"      +
                                                              "target		?target\n"  +
                                                              "source		?source"    ;

    private static final String MAPPING_COLLECTION_END     = "]]" ;

    private boolean existHeader = false                           ;

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


    private void loadNodes ( JSONObject jsonObj , int hash) {

        JSONArray jsonArrayNodes = jsonObj.getJSONObject("graphml")
                                          .getJSONObject("graph")
                                          .getJSONArray("node") ;

        for (int i = 0; i < jsonArrayNodes.length(); i++) {

            Object obj                 = jsonArrayNodes.get(i) ;
            
            JSONObject jsonObjectNode  = (JSONObject) obj      ;

            if(obj != null) {

                if(obj.toString().startsWith("{\"data\":{")) {

                    String label = jsonObjectNode.getJSONObject("data")
                                                 .getJSONObject("y:ShapeNode")
                                                 .getJSONObject("y:NodeLabel")
                                                 .getString("content") ;

                    String id       =  jsonObjectNode.getString("id") + "_"+ hash ;
                    String ofEntity =  null ;
                    int code        =  -1   ;

                    if(label.contains("(") && label.contains(")")) {
                        code =  Integer.parseInt(
                                label.split(Pattern.quote("("))[1]
                                     .replaceAll("[^0-9]", "")) ;
                        ofEntity = label.trim().split(Pattern.quote("("))[0]  ;
                    }

                    Node node ;
                    if(code == -1 ) {
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
                                                                      .getString("content") ;

                                    int code ;

                                    if (label.toLowerCase().startsWith("query_(")) {
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(":"))[0]
                                                       .split(Pattern.quote("_"))[1]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        source.put(code+hash, label.split( Pattern
                                                                   .quote(": "))[1]
                                                                   .trim())       ;
                                    }
                                    else if (label.toLowerCase().trim(
                                    ).startsWith("(") & label.toLowerCase()
                                            .contains(")") )                      {
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

                            if ( jsonArrayGroupNodes.toString().startsWith("{\"data\":{")  ||
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
                                                                      .getString("content") ;

                                    int code ;

                                    if (label.toLowerCase().startsWith("query_(")) {
                                        code =  Integer.parseInt(label
                                                       .split(Pattern.quote(":"))[0]
                                                       .split(Pattern.quote("_"))[1]
                                                       .replaceAll("[^0-9]", ""))  ;

                                        source.put(code+hash, label.split(Pattern
                                                .quote(": "))[1].trim())        ;
                                    }
                                    
                                    else
                                    
                                    if ( label.toLowerCase().trim().startsWith("(") 
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
                                        
                                        if(numUris.values().contains( co )) {
                                          System.out.println("ALERT # Code : " + co + " Detected multiple times ! ") ;
                                        }
                                         
                                        numUris.put( ":" + label.split( Pattern
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
                                    if (label.replaceAll(" +", " ")
                                            .startsWith("PREDICAT_PREFIX :"))       {
                                        PREFIX_PREDICAT = label.replaceAll(" +", " ")
                                                               .split(Pattern
                                                               .quote("PREDICAT_PREFIX :"))[1] ;
                                    }
                                    else
                                    if (label.toLowerCase().startsWith("obda-"))    {

                                        if  ( label.replaceAll(" +", " ")
                                                   .split(Pattern.quote(" : ")) [0]
                                                   .equals("obda-sourceUri"))     {
                                                       
                                            SourceDeclaration.put("sourceUri",
                                                    label.replaceAll(" +", " ")
                                                         .split(Pattern
                                                         .quote(" : "))[1]) ;
                                        }
                                        else if ( label.replaceAll(" +", " ")
                                                       .split(Pattern.quote(" : ")) [0]
                                                       .equals("obda-connectionUrl")) {
                                                  
                                            SourceDeclaration.put("connectionUrl", label
                                                             .replaceAll(" +", " ")
                                                             .split(Pattern
                                                             .quote(" : "))[1]) ;
                                        }
                                        else if (label.replaceAll(" +", " ")
                                                      .split(Pattern.quote(" : "))[0]
                                                      .equals("obda-username"))     {
                                                        
                                            SourceDeclaration.put("username", label
                                                             .replaceAll(" +", " ")
                                                             .split(Pattern
                                                             .quote(" : "))[1])   ;
                                        }
                                        else if (label.replaceAll(" +", " ")
                                                      .split(Pattern.quote(" : "))[0]
                                                      .equals("obda-password"))     {
                                                  
                                            SourceDeclaration.put("password", label
                                                             .replaceAll(" +", " ")
                                                             .split(Pattern
                                                             .quote(" : "))[1])   ;
                                        }
                                        else if ( label.replaceAll(" +", " ")
                                                       .split(Pattern.quote(" : "))[0]
                                                       .equals("obda-driverClass"))  {
                                                        
                                            SourceDeclaration.put("driverClass", label
                                                             .replaceAll(" +", " ")
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
                                     .toString().startsWith("{\"node\":{"))   {

                        JSONObject jsonArrayGroupNodes = jsonObjectNode.
                                                         getJSONObject("graph")
                                                        .getJSONObject("node") ;

                        if( jsonArrayGroupNodes.getJSONObject("data")
                                               .has("y:ShapeNode")) {

                            String id = jsonArrayGroupNodes.getString("id") ;

                            String label = jsonArrayGroupNodes
                                            .getJSONObject("data")
                                            .getJSONObject("y:ShapeNode")
                                            .getJSONObject("y:NodeLabel")
                                            .getString("content") ;

                            int code ;

                            if(label.toLowerCase().startsWith("query_("))  {
                                code =  Integer.parseInt(label
                                               .split(Pattern.quote(":"))[0]
                                               .split(Pattern.quote("_"))[1]
                                               .replaceAll("[^0-9]", ""))  ;

                                source.put(code+hash, label.split(Pattern
                                                           .quote(": "))[1]
                                                           .trim())       ;
                            }
                            else
                            if( label.toLowerCase().trim().startsWith("(") 
                                && label.toLowerCase().contains(")") )   {
                                      
                                code =  Integer.parseInt(label
                                               .split(Pattern.quote(")"))[0]
                                               .replaceAll("[^0-9]", ""))  ;

                                tmpUris.put(code+hash, label.split(Pattern
                                                            .quote(")"))[1]
                                                            .trim()) ;

                                if (numUris.values().contains( code )) {
                                   System.out.println("ALERT # Code { " + code + " } Detected multiple times ! ") ;
                                }
                                         
                                numUris.put( ":" + label.split( Pattern
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
                            
                            if(label.replaceAll(" +", " ")
                                    .startsWith("PREDICAT_PREFIX :"))       {
                                PREFIX_PREDICAT = label.replaceAll(" +", " ")
                                                       .split(Pattern
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
                      .has("edge") )
        return ;

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
                  
                    System.err.println(" Oops something went wrong !! ")       ;
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

                Edge e = new Edge(id, sujet, predicat, objet) ;

                edges.add(e) ;

            }
            else {
                System.err.println(" Oups !!!!!! " ) ;
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
                        existPrefixStartWith(objet.getLabel()) ||
                        (  objet.getLabel().startsWith(":")    &&
                           !objet.getLabel().endsWith("::#")
                        )
                ) {
                    if(!sujet.getOfEntity().startsWith(":"))      {

                        target.put( tmpUris.get(sujet.getHash())  ,
                                tmpUris.get(sujet.getHash())           +
                                        " a " +  PREFIX_PREDICAT + ":" +
                                        sujet.getOfEntity() + " ; "    +
                                        objectProperty  +  " "         +
                                        objet.getLabel() )             ;
                    }
                    else {

                        target.put( tmpUris.get(sujet.getHash())   ,
                                tmpUris.get(sujet.getHash())          +
                                        " a " + sujet.getOfEntity()   +
                                        " ; " + PREFIX_PREDICAT       +
                                        ":"   + edge.getPredicat()    +
                                        " "   + objet.getLabel() )    ;
                    }

                }

                else {
                    target.put( tmpUris.get(sujet.getHash()) ,
                            tmpUris.get(sujet.getHash())           +
                                    " a " + PREFIX_PREDICAT + ":"  +
                                    sujet.getOfEntity() + " ;  "   +
                                    objectProperty   +  " :"       +
                                    tmpUris.get(objet.getHash()) ) ;
                }

                uris.put( ":" + tmpUris.get( sujet.getHash() ) ,
                                source.get(sujet.getHash()))   ;
            }
            else {
                if ( objet.getLabel().startsWith("<")       ||
                     objet.getLabel().startsWith("{")       ||
                     objet.getLabel().startsWith("\"")      ||
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

                       if (tmpUris.get(objet.getHash()) != null) {

                            target.put( tmpUris.get(sujet.getHash()),
                                    target.get(tmpUris.get(sujet.getHash())) +
                                          " ; " +  objectProperty + " :"     +
                                          tmpUris.get(objet.getHash()) )     ;
                       }

                        else {

                            numUris.forEach(( uri, value) -> {

                                if (value == objet.getCode() ) {

                                    target.put( tmpUris.get(sujet.getHash())             ,
                                                target.get(tmpUris.get(sujet.getHash())) +
                                                 " ; " +  objectProperty + " "           +
                                                 uri 
                                              ) ;
                                }
                            }) ;
                        }
                }
            }
        }

        for (Map.Entry<String, String> entrySet : target.entrySet()) {
            String key   = entrySet.getKey()    ;
            String value = entrySet.getValue()  ;
            target.put(key, ":" + value + " .") ;
        }

        List<String> outs = new ArrayList<>() ;

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

            int num = numUris.get(myTarget.split(" ")[0]) ;

            String keyByURI = getKeyByURI("("+num+")_"+myTarget.split(" ")[0]) ;

            if(keyByURI.endsWith("_") )
                keyByURI = keyByURI.substring(0, keyByURI.length() - 1 ) ;

            if( !myTarget.startsWith(":null")) {

                if( uris.get(myTarget.split(" ")[0]) == null ) {
                    throw new Exception(" No Query found for : " + myTarget.split(" ")[0] ) ;
                }

                outs.add( MAPPING_COLLECTION_PATTERN.replace("?id", keyByURI )
                                                    .replace("?target"  , myTarget)
                                                    .replace("?source"  ,
                          uris.get(myTarget.split(" ")[0])).replace("  ", " " )
                ) ;

                outs.add("") ;
            }
        }

        Writer.writeTextFile(outs, outFile) ;
    }

    private String getKeyByURI(String target) {
        String code =  target.replaceAll(Pattern.quote("/{"), "_")
                             .replaceAll(Pattern.quote("-{"), "_")
                             .replaceAll(Pattern.quote("/"), "_" )
                             .replaceAll(Pattern.quote("{"), "_" )
                             .replaceAll(Pattern.quote("}"), "_" )
                             .replaceAll(Pattern.quote(":"), "_" )
                             .replaceAll("_+", "_")              ;
                             
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
    }


    public void entryProcess( String directory ,
                              String outObdaPathFile ,
                              String extensionFile ) throws Exception {

        Writer.checkFile(outObdaPathFile) ;
        existHeader = false               ;

        boolean processed = false ;

        List<Path> files = Files.list(new File(directory).toPath()).collect(toList()) ;

        for(Path path : files ) {
            if(path.toString().endsWith(extensionFile )) {
                process(path.toString() ) ;
                if ( ! processed ) processed = true ;
            }
        }

        if( processed ) {
            write(outObdaPathFile) ;
            Writer.writeTextFile(Collections.singletonList(MAPPING_COLLECTION_END), outObdaPathFile) ;
        }
        else {
            System.out.println ( " No File with extension '" +extensionFile + "' found !! " ) ;
            System.out.println ( "                                                        " ) ;
        }

    }
}
