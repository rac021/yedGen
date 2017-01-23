
package org.inra.yedgen.processor.factories;

import java.util.UUID ;
import java.util.regex.Pattern ;
import org.inra.yedgen.processor.entities.Node ;
import org.inra.yedgen.graph.managers.ManagerEdge ;
import org.inra.yedgen.graph.managers.GraphExtractor ;
import org.inra.yedgen.processor.managers.ManagerUri ;
import org.inra.yedgen.graph.managers.ManagerConcept ;
import org.inra.yedgen.processor.managers.ManagerQuery ;

/**
 *
 * @author ryahiaoui
 */
public class FactoryNode {
    
    private final ManagerUri            managerUri       ;
    private final ManagerQuery          managerQuery     ;
    private final ManagerConcept        managerConcept   ;

    public FactoryNode( ManagerEdge    managerEdge    , 
                        ManagerConcept managerConcept , 
                        ManagerUri     managerUri     ,
                        ManagerQuery   managerQuery   ) {
        
        this.managerConcept = managerConcept ;
        this.managerUri     = managerUri     ;
        this.managerQuery   = managerQuery   ;
    }
    
    
    public Node createNode( int    hash       , 
                            String subjectId  , 
                            String predicat   , 
                            String objectId   ,
                            String defaultPrefix )  {
        
        final String Labelsubject = managerConcept.getConcept( hash, subjectId )                        ;
        final String object       = managerConcept.getConcept( hash, objectId )                         ;
            
        int codeForSubject        = extractCode (Labelsubject)                                          ; 

        String uriSubject         = managerUri.getUriByHashAndCode( hash, codeForSubject, Labelsubject) ;
        
        String typeOfSubject      = extractType ( Labelsubject )                                        ;
        
        String queryForSubject    = managerQuery.getQueryByHashAndCode( hash, codeForSubject)           ;
        
        int codeForObject         = extractCode (object )                                               ; 

        String uriObject          = managerUri.getUriByHashAndCode( hash, codeForObject, object )       ;
        String queryForObject     = managerQuery.getQueryByHashAndCode( hash, codeForObject)            ;
        
        return new Node( hash            , 
                         subjectId       , 
                         codeForSubject  , 
                         uriSubject      , 
                         typeOfSubject   , 
                         Labelsubject    ,
                         predicat        , 
                         queryForSubject , 
                         uriObject       , 
                         queryForObject  ,
                         defaultPrefix ) ;
    }


    private String extractType (String subject )  {
      return subject.split(Pattern.quote("("))[0] ;
    }

    private int extractCode ( String concept ) {

        if(concept.contains("(") && concept.endsWith(")")) {
          return Integer.parseInt( concept
                        .split(Pattern.quote("("))[1]
                        .replace(")", "")) ;
        }
        return -1 ;
    }
    
    public Node createNode(  Integer hash             , 
                             String  id               , 
                             Integer code             , 
                             String uriSubject        , 
                             String typeOfSubject     , 
                             String entityName        , 
                             String predicat          , 
                             String queryForSubject   ,
                             String uriObject, 
                             String queryObject , 
                             String defaultPrefix )   {
    
            return new Node( hash            ,  
                             id              , 
                             code            , 
                             uriSubject      , 
                             typeOfSubject   , 
                             entityName      ,
                             predicat        , 
                             queryForSubject , 
                             uriObject       , 
                             queryObject     , 
                             defaultPrefix ) ;
    }
  
    
    public Node createNode( Integer hash , String token ) {
        
      String type =  extractType(token)                  ;
      int    code =  extractCode(token.split(" ")[0])    ;
      
      String uri    = managerUri.getUri(hash, code)      ;    
      String query  = managerQuery.getQuery(hash, code ) ;    

      String predicatObject = token.split(" ")[1] ;
      String object         = treateConcept( hash, token.split(" ")[2]) ;
      
      if( uri == null ) {
           System.err.println("")   ;
           System.err.println(" Error // No Uri found with Hash : " + hash + " // Code : " + code ) ;
           System.err.println("")   ;
           return null ;
       }
      
      return 
            createNode(hash, 
                        UUID.randomUUID().toString() , 
                        code                         , 
                        uri                          , 
                        type                         , 
                        type                         , 
                        predicatObject               , 
                        query                        , 
                        object                       , 
                        null                         , 
                        GraphExtractor.PREFIX_PREDICAT ) ;
    }
    
    
    private String treateConcept( int hash , String concept ) {
          
          if(concept.contains("(") && concept.endsWith(")")) {
              int code = extractCode(concept) ;
              return managerUri.getUri(hash, code) ;
          }
        return concept ;
    }
    
}
