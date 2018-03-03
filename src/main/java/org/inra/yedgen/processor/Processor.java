
package org.inra.yedgen.processor ;

import java.io.File ;
import java.util.Map ;
import java.util.Set ;
import java.util.List ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Optional ;
import java.io.IOException ;
import java.nio.file.Files ;
import java.nio.file.Paths ;
import java.util.ArrayList ;
import entypoint.Main.VERSION ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import java.util.stream.Stream ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import java.util.function.Consumer ;
import org.inra.yedgen.sql.SqlAnalyzer ;
import org.inra.yedgen.graph.utils.Utils ;
import org.inra.yedgen.processor.io.Writer ;
import org.inra.yedgen.graph.entities.Edge ;
import org.inra.yedgen.obda.header.ObdaHeader ;
import org.inra.yedgen.processor.entities.Node ;
import org.inra.yedgen.processor.logs.Messages ;
import org.inra.yedgen.splitter.ObdaSplitter_V1 ;
import org.inra.yedgen.properties.CsvProperties ;
import java.util.concurrent.atomic.AtomicInteger ;
import org.inra.yedgen.properties.ObdaProperties ;
import org.inra.yedgen.graph.managers.ManagerEdge ;
import org.inra.yedgen.processor.entities.Variable ;
import org.inra.yedgen.processor.managers.ManagerUri ;
import org.inra.yedgen.graph.managers.ManagerConcept ;
import org.inra.yedgen.graph.managers.GraphExtractor ;
import org.inra.yedgen.processor.managers.ManagerNode ;
import org.inra.yedgen.processor.factories.FactoryNode ;
import org.inra.yedgen.processor.managers.ManagerQuery ;
import org.inra.yedgen.processor.managers.ManagerVariable ;
import org.inra.yedgen.processor.managers.ManagerMetaPattern ;
import org.inra.yedgen.processor.managers.ManagerPatternContext ;
import org.inra.yedgen.processor.managers.ManagerPatternParallel ;
import static org.inra.yedgen.graph.managers.GraphExtractor.PREFIX_PREDICAT ;

/**
 *
 * @author ryahiaoui 
 */

public class Processor {

    private final ManagerPatternParallel managerPatternParallel  ;
    private final ManagerPatternContext  managerPatternContext   ;
    private final ManagerMetaPattern     metaPatternManager      ;
    private final ManagerVariable        managerVariable         ;
    private final GraphExtractor         graphExtractor          ;
    private final ManagerQuery           managerQuery            ;
    private final ManagerNode            managerNode             ;
    private final ObdaHeader             obdaHeader              ;
    
    private final CsvProperties          csvProperties           ;
    
    private boolean                      verbose                 ;
    
    private final VERSION                version                 ;
    
    static enum PART { SUBJECT , PREDICAT_VALUES, QUERY  }       ;
    
    static  final Pattern  PATTERN_KEY_VALUES  =  Pattern.compile( "\\{(.*?)\\}"    , 
                                                                   Pattern.DOTALL ) ;
    
    public Processor( String  directory       , 
                      String  extensionFile   ,
                      String  propertieFile   ,
                      String  jsFile          ,
                      String  connecFile      ,
                      String  prefixFile      ,
                      String  default_prefix  ,
                      String  magicFilterFile ,
                      VERSION version         ,
                      String predic_pattern_context ) throws Exception  {
        
      this.version         =  version                ;
   
      this.graphExtractor  =  new GraphExtractor ()  ;
        
      graphExtractor.genGraphPopulatingManagers( directory , extensionFile )      ;
          
      if(default_prefix != null ) GraphExtractor.PREFIX_PREDICAT = default_prefix ;
     
      /* Add External Prefixs if prefixFile not null */ 
      updateConnection( connecFile, this.graphExtractor.getSourceDeclaration())   ;
              
      /* Add External Prefixs if prefixFile not null */ 
      updatePrefixs(prefixFile, this.graphExtractor.getPrefixs())          ;

      /* Override Magic Filter if magicFilterFile not null  */
      String updateMagicFilter = updateMagicFilter ( magicFilterFile )     ;
      
      /* Override Predicate_Pattern_Context if predic_pattern_context not null */
      if( predic_pattern_context != null )  
          GraphExtractor.PREDICAT_PATTERN_CONTEXT = predic_pattern_context ;
      
      if( updateMagicFilter != null ) {
          graphExtractor.setMagicFilter(updateMagicFilter)                 ;
      }
      
      ManagerUri     managerUri     =  new ManagerUri( graphExtractor.getMapUris())          ;
      ManagerEdge    managerEdge    =  new ManagerEdge( graphExtractor.getMapEdges() )       ;
      
                     managerQuery   =  new ManagerQuery( graphExtractor.getMapQueries() )    ;

      ManagerConcept managerConcept =  new ManagerConcept( graphExtractor.getMapConcepts() ) ;
      
      
      FactoryNode factoryNode     = new FactoryNode( managerEdge      ,
                                                     managerConcept   , 
                                                     managerUri       , 
                                                     managerQuery   ) ;
            
      this.managerNode            = instantiateManagerNode ( managerConcept   , 
							     managerEdge      , 
							     factoryNode )    ;
      
      boolean isRealyMetaGraph    = checkAgainIfIsMetaGraph()                 ;
	    
      this.obdaHeader             = new ObdaHeader(graphExtractor , version ) ;
      
      this.managerPatternContext  = new ManagerPatternContext ( graphExtractor.getMapPatternContexts() , 
                                                                managerQuery     , 
                                                                factoryNode  )   ;
      
      this.csvProperties          = new CsvProperties( propertieFile, jsFile )   ;
        
      this.metaPatternManager     = new  ManagerMetaPattern( graphExtractor.getMetaPatternHash()     ,
                                                             graphExtractor.getMetaPatternVariable() , 
                                                             graphExtractor.getMetaPatternContext()  , 
                                                             graphExtractor.getMetaPatternParallel() ,
                                                             csvProperties                           ,
                                                             isRealyMetaGraph                        ,
                                                             GraphExtractor.containsPaternContext()  ,
                                                             GraphExtractor.containsPaternParralel() ,
                                                             GraphExtractor.containsVariables()    ) ;
      
      this.managerPatternParallel = new ManagerPatternParallel ( graphExtractor.getMapPatternParallels() , 
                                                                 managerUri     , 
                                                                 factoryNode    ,
                                                                 metaPatternManager ) ;
      
      this.managerVariable        = new ManagerVariable( graphExtractor.getMapVariables() ,
                                                         managerNode                      , 
                                                         managerPatternContext            , 
                                                         managerPatternParallel)          ;
    }
    
    private ManagerNode instantiateManagerNode ( ManagerConcept managerConcept , 
                                                 ManagerEdge    managerEdge    , 
                                                 FactoryNode factoryNode    )  {
        
        ManagerNode localManager = new ManagerNode() ;
          
        if ( managerEdge.getAll().isEmpty() ) {
            
            if ( ! managerConcept.getConcepts().isEmpty() ) {
               
                managerConcept.getConcepts().forEach( ( hash, concepts ) -> { 
                    
                       concepts.forEach ( ( subjectId, label ) -> {
                           
                               Node node = factoryNode.createNode( hash      , 
                                                                   subjectId , 
                                                                   null      , 
                                                                   null      ,
                                                                   PREFIX_PREDICAT ) ; 

                               localManager.registerNode(hash, subjectId, node )     ;
                       });
                }) ;
            }
        }
	    
        Map< Integer, Set<String>>  potentialLeafNodesWithURI = new HashMap<>() ;
         
        for ( Edge edge : Utils.getAll(managerEdge.getEdges()) ) {
            
            int    hash      = edge.getHash()      ;
            String subjectId = edge.getSujetId()   ;
            String objectId  = edge.getObjetId()   ;
            String predicat  = edge.getPredicat()  ;
            
            Node node = factoryNode.createNode( hash      , 
                                                subjectId , 
                                                predicat  , 
                                                objectId  , 
                                                PREFIX_PREDICAT ) ; 
           
            localManager.registerNode(hash, subjectId, node) ;

            if( potentialLeafNodesWithURI.get(hash) != null &&
                potentialLeafNodesWithURI.get(hash).contains(subjectId)) {
                // Not a Ldeaf, retrives from map
                 potentialLeafNodesWithURI.get(hash).remove(subjectId) ;
            }
            
            final String object = managerConcept.getConcept( hash, objectId ) ;
		
            if ( ( object.contains("(")            && 
                   ! object.trim().startsWith("(") &&
                   object.trim().endsWith(")") )   ||
                   object.trim()
	                 .equals( ManagerMetaPattern.getMATCHER_PATTERN_CONTEXT()) )
	    {
                // Potential leaf Node with URI
                if ( potentialLeafNodesWithURI.containsKey(hash) )      {
                     potentialLeafNodesWithURI.get(hash).add(objectId ) ;
                }
                else {
                   Set idNodeSet = new HashSet<>() ;
                   idNodeSet.add(objectId)         ;
                   potentialLeafNodesWithURI.put(hash, idNodeSet) ;
                }
            }
        }
        
        potentialLeafNodesWithURI.forEach ( ( hash , setNodes ) -> {
            
            setNodes.forEach( subjectId -> {
                
             Node node = factoryNode.createNode( hash      , 
                                                 subjectId , 
                                                 null      , 
                                                 null      , 
                                                 PREFIX_PREDICAT ) ; 
           
              localManager.registerNode(hash, subjectId, node)     ;
            
            }) ;
            
        }) ;
        
        localManager.cloneNodes() ;
        
        return  localManager      ;    
    }
        
    private boolean checkAgainIfIsMetaGraph() {
        
      if ( GraphExtractor.isMetaGraph()  ) return true ;
    
       // check again if there's URI or QUERIES
       // tnat contains "?"
       Optional<Node> node = managerNode.getAll()
                                        .stream()
                                        .filter( n -> {
                                                 return ( n.getUri().contains("?") ||
                                                          n.getQuery().contains("?")); })
                                        .findAny() ;
	    
       if ( node.isPresent()) { return true  ;  }
       return false ;
    }
    
    public boolean processFull ( String       outputFile  , 
                                 String       csvFile     , 
                                 String       classe      ,
                                 int          column      ,
                                 Integer      matchColumn ,
                                 List<String> matchWord   ,
                                 VERSION      version  )  {
        
        boolean processCSV       = processOnlyCSV ( outputFile  , 
                                                    csvFile     , 
                                                    classe      ,
                                                    column      ,
                                                    matchColumn ,
                                                    matchWord   ,
                                                    version   ) ;
        
        boolean processVariables = processOnlyGraphVariables( outputFile ,
                                                              version  ) ;
        
        return processVariables && processCSV ;
      
    }

    public boolean processOnlyGraphVariables ( String  outputFile ,
                                               VERSION version    )  {
      
      Messages.printMessageStartProcessVariableGraphGeneration()     ;
      
      for( Variable variable : managerVariable.getVariables())       {
     
         System.out.println( " ** Processing Graph Variable : "
                             + " [ " + variable.getVariableName()
                                               .replaceFirst(":", "")
                             + " ]  -------------- \n " )  ;
         
         Set<Node> graph      = managerVariable.process( variable )  ;
          
         List<String> outPut  = new ArrayList<>()                    ;
                      
         // Prepare Output Header 
         outPut.addAll(obdaHeader.getHeaderOut())                    ; 
          
         String _fileName = outputFile.substring( 0, outputFile.lastIndexOf('.')) ;
         String extension = outputFile.substring( outputFile.lastIndexOf('.'))    ;
             
         String outFile = _fileName + "_Graph_"                             + 
                          variable.getVariableName().replaceFirst(":", "")  + 
                          extension    ;       
         
         try {          
                if ( graphExtractor.getMagicFilter() != null &&  
                     ! graphExtractor.getMagicFilter().trim().isEmpty() )      {

                        /* Split if MagicFilter Enabled */

                        ObdaSplitter_V1.split( graphExtractor.getMagicFilter() ,
                                               graph                           ,
                                               variable                        ,
                                               obdaHeader                      ,
                                               managerQuery                    ,
                                               outFile )                       ;

                } else {

                    AtomicInteger ErrorCheckMatchersAndValidateMapping = new AtomicInteger(0) ;
             
                    graph.stream().forEach( node -> { 
                                            outPut.add ( node.outputObda() )   ; 
                                            boolean checkMatchersAndValidateMapping 
                                                    = okMatchersAndValidateMapping( node  ,
                                                                                    variable.getVariableName() ) ;
                                            if( ! checkMatchersAndValidateMapping )
                                                ErrorCheckMatchersAndValidateMapping.getAndIncrement()           ;
                                                       
                    } )  ;
                    
                    if( ErrorCheckMatchersAndValidateMapping.get() == 0 ) {
                 
                        outPut.add(ObdaProperties.MAPPING_COLLECTION_END) ;

                        Writer.checkFile( outFile )           ;
                        Writer.writeTextFile(outPut, outFile) ;

                        Messages.printMessageInfoGeneratedVariable( variable.getVariableName()   ,
                                                                    outFile                    ) ;
                    }
                }

         } catch (IOException ex) {
                Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex) ;
          }
      }
      
      return managerVariable.getVariables().size() > 0       ;
      
   }
    
    public boolean processOnlyCSV ( String       outputFile  ,
                                    String       csvFile     , 
                                    String       classe      ,
                                    int          column      ,
                                    Integer      matchColumn ,
                                    List<String> matchWord   ,
                                    VERSION      version )   {
         
     Messages.printMessageStartProcessCsvVariableGeneration( csvFile ) ;
     
     if( ! Writer.existFile ( csvFile ) )  {
        Messages.printMessageErrorCSV ( csvFile) ;
        return true ;
     }
      
     /* Check and skip if patterns are null */ 
     
     if( ( metaPatternManager.getMetaPatternContext()  == null && 
           metaPatternManager.containsPaternContext() ) ||
         ( metaPatternManager.getMetaPatternVariable() == null && 
           metaPatternManager.containsVariables() )     ||
         ( metaPatternManager.getMetaPatternParallel() == null &&
           metaPatternManager.containsPaternParralel() )
       )  {
             Messages.printMessageMetaPatternsNull() ;
             return true                             ;
     }
       
     try {
         
	 AtomicInteger treatedLine   = new AtomicInteger(-1) ;
         	 
         try ( Stream<String> lines = Files.lines(Paths.get(csvFile)).skip(1)) {
             
             lines.forEach (new Consumer<String>() {
                 
                int counter = 0 ;               
                
                @Override
                public void accept(String line ) {
                    
                 if( classe != null ) {
                      
                   if ( column < 0 ) throw new IllegalArgumentException(" Column Num can't be negative ") ;
                    
                   if (line.trim().isEmpty() ) {  counter ++ ; return   ;  }                              ;
			    
                   if ( line.split( metaPatternManager.getCSV_SEPARATOR()).length < column + 1 )          {
                        System.out.println(" + CSV Column size = " + 
                                              line.split(metaPatternManager.getCSV_SEPARATOR()).length)   ;
                       throw new IllegalArgumentException(" Column [ " + column + " ] Does't exists ! ")  ;
                   }
                    
                   if( ! line.split(metaPatternManager.getCSV_SEPARATOR())[column].trim()
                                                      .replaceAll(" +", " ").equalsIgnoreCase(classe.trim())) {
                      counter ++ ;

                      return     ;
                   }
                 }
                 
                 if(  matchColumn != null &&   matchColumn > 0   && 
                      matchWord   != null && ! matchWord.isEmpty() ) {
                     
                    if( ! matchWord.contains( line.split( metaPatternManager
                                                  .getCSV_SEPARATOR())[ matchColumn ]
                                                  .trim()
                                                  .replaceAll(" +", " "))) {
                       counter ++ ;

                       return     ;
                    }
                 }
                    
                 try {
                              
                     System.out.println( " ** Processing CSV Line :"
                                         + " [ " + counter  + " ]  -------------- "
                                                 + "\n " ) ;
                     
                      List<String> outPut    = new ArrayList<>() ;

                      // Prepare Output Header 
                      outPut.addAll(obdaHeader.getHeaderOut())   ; 

                      String patternContext   = null ;
                      String patternVariable  = null ;
                      
                      // Treat Variable

                      if( metaPatternManager.containsPaternContext() ) {
                        patternContext   = metaPatternManager.generatePatternContext(line)  ;
                      }
                      
                      patternVariable    = metaPatternManager.generatePatternVariable(line) ;
                      
                      Variable variable  = managerVariable.transformToVariable( patternVariable   ,
                                                                                patternContext  ) ;

                      Set<Node> nodes    = managerVariable.process( variable )    ;

                      String folder    = Writer.getFolder ( outputFile )   ;
                      String fileName  = Writer.getfileName ( outputFile ) ;
                      String fileNameWithoutExtension = Writer.getFileWithoutExtension(fileName ) ;
                      String extension = Writer.getFileExtension(fileName) ; 
                         
                      String outFile = folder    + File.separator     + 
                                       counter++ + "_"                +
                                       fileNameWithoutExtension       +
                                       "_CSV_"                        + 
                                      variable.getVariableName()  
			                      .replaceFirst( ":", ""  )
                                              .replace( "." , "_"     )
                                              .replace( "<" , ""      )
                                              .replace( ">" , ""      )
			                      .replaceAll( "/" , "_"  ) 
			               + extension                    ;
                     
                      if( graphExtractor.getMagicFilter() != null  &&
			  ! graphExtractor.getMagicFilter()
				          .trim().isEmpty() )       {
                          
                            /* Split if MagicFilter Enabled */
                            
                            ObdaSplitter_V1.split( graphExtractor.getMagicFilter() , 
                                                   nodes                           ,
                                                   variable                        , 
                                                   obdaHeader                      ,
                                                   managerQuery                    ,
                                                   outFile )                       ;
                      } else {
                          
                         AtomicInteger ErrorCheckMatchersAndValidateMapping = new AtomicInteger(0) ;
                          
                          nodes.stream().forEach( node -> {
                              
                                                      outPut.add( node.outputObda())            ;
                                                      boolean checkMatchersAndValidateMapping  =
                                                      okMatchersAndValidateMapping (
                                                                     node                         ,
                                                                     variable.getVariableName() ) ; 
                                                      if ( ! checkMatchersAndValidateMapping ) {
                                                        ErrorCheckMatchersAndValidateMapping.getAndIncrement() ;
                                                      }
                          })  ;
                          
                          if( ErrorCheckMatchersAndValidateMapping.get() == 0 )     {
                          
                                outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;

                                Writer.checkFile( outFile )           ;
                                Writer.writeTextFile(outPut, outFile) ;
                                Messages.printMessageInfoGeneratedVariable( variable.getVariableName() ,
                                                                            outFile                  ) ;
                          } else {
                              System.out.println(" Errors were detected.. " ) ;
                              System.out.println("                        " ) ;
                          }
                      }

		      treatedLine.getAndAdd(1) ;
                      
                    } catch (IOException e) {
                         Logger.getLogger(Processor.class.getName())
                               .log(Level.SEVERE, null, e)         ;
                    }
                }
            }) ;
           
        }
        catch (Exception ex) {
          Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex) ;
        } 
	     
        if( treatedLine.get() >= 0 ) {
          Messages.printInfoCSVTreatment( csvFile                 ,
                                          treatedLine.get() >= 0  ? 
                                            treatedLine.get() + 1 : 
                                            0                     ,
                                          classe            , 
                                          column          ) ;
        } else {
            
          Messages.printInfoCSVEmptyTreatment( csvFile      ,
                                               classe       , 
                                               column     ) ;
        }
        
	return treatedLine.get() >= 0 ;
        
      } catch (Exception ex) {
          Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex) ;
     }
        
     return false ;
        
    }
    
    public boolean processOnlyGraphWithoutVariables ( String outputFile ) {
      
	 String _fileName = outputFile.substring(0, outputFile.lastIndexOf('.')) ;
         String extension = outputFile.substring(outputFile.lastIndexOf('.')   ) ;
             
         String outFile   = _fileName + "_Graph_"       + 
                            System.currentTimeMillis()  +
                            extension                   ;
	    
         Set<Node> all = managerNode.getAll()           ;      
                
         try {
               if ( graphExtractor.getMagicFilter() != null &&  
                    ! graphExtractor.getMagicFilter().trim().isEmpty() )  {

                  /* Split if MagicFilter Enabled */

                  ObdaSplitter_V1.split( graphExtractor.getMagicFilter() ,
                                         all                             ,                                     
                                         new Variable( -9999       , 
                                                       "idUndefVa" , 
                                                       "UndefVar"  ,
                                                       null        , 
                                                       null        ,
                                                       null        ,
                                                       null        )     ,
                                         obdaHeader                      ,
                                         managerQuery                    ,
                                         outFile )                       ;
               }

               else {

                   List<String> outPut    = new ArrayList<>()            ;

                   // Prepare Output Header 
                   outPut.addAll(obdaHeader.getHeaderOut())              ;
        
                   AtomicInteger ErrorCheckMatchersAndValidateMapping = new AtomicInteger(0) ;
                   
                   all.stream().forEach( node -> { 
                       
                                            outPut.add ( node.outputObda() )      ; 
                                            boolean checkMatchersAndValidateMapping 
                                                    = okMatchersAndValidateMapping( node              ,
                                                                                    "UndefVar"      ) ;
                                            if( ! checkMatchersAndValidateMapping )
                                                ErrorCheckMatchersAndValidateMapping.getAndIncrement()  ;
                                                       
                   })  ;
                   
                   if( ErrorCheckMatchersAndValidateMapping.get() == 0 ) {
                       
                       outPut.add( ObdaProperties.MAPPING_COLLECTION_END )              ;

                       Writer.checkFile( outFile )                                      ;
                       Writer.writeTextFile(outPut, outFile)                            ;

                       System.err.println( "                                        " ) ;
                       Messages.printMessageInfoGeneratedVariable( "Undefined Variable" ,
                                                                    outFile          )  ;        
                   }  else {
                       System.out.println(" Errors were detected.. " ) ;
                       System.out.println("                        " ) ;
                   }
               }
	    
         } catch (IOException ex) {
           Logger.getLogger(Processor.class.getName())
		                     .log(Level.SEVERE, null, ex  ) ;
          }
     
      return all.size() > 0 ;
      
    }
    
    private void generateEmptyMappingOBDAFile( String outputFile ) {
        
         String _fileName = outputFile.substring(0, outputFile.lastIndexOf('.')) ;
         String extension = outputFile.substring(outputFile.lastIndexOf('.')   ) ;
             
         String outEmptyFile   = _fileName + "_EMPTY_MAPPING_" +  extension      ;
         
         // Generate EmptyFile Once 
         if( Writer.existFile(outEmptyFile)) {
             return ;
         }
         
         try {
		List<String> outPut    = new ArrayList<>()                       ;
                      
                // Prepare Output Header 
                outPut.addAll(obdaHeader.getHeaderOut())                         ;                      
         
                outPut.add( ObdaProperties.MAPPING_COLLECTION_END )              ;
		Writer.checkFile( outEmptyFile )                                 ;
                Writer.writeTextFile(outPut, outEmptyFile)                       ;
            
                System.err.println( "                                        " ) ;
                Messages.printMessageInfoGeneratedVariable( "Undefined Variable" ,
                                                            outEmptyFile      )  ;      
	    
         } catch (IOException ex) {
           Logger.getLogger(Processor.class.getName())
		                     .log(Level.SEVERE, null, ex  ) ;
          }
    }
    
    public void process ( String       outputFile              , 
                          String       csvFile                 , 
                          boolean      includingGraphVariables ,
                          String       classe                  ,
                          int          column                  ,
                          Integer      matchColumn             ,
                          List<String> matchWord             ) {
      
        boolean process = false    ; 
        
        if( includingGraphVariables           && 
            csvFile != null                   && 
            metaPatternManager.isMetaGraph() ) {
            
            process = processFull( outputFile  , 
                                   csvFile     , 
                                   classe      , 
                                   column      ,
                                   matchColumn ,
                                   matchWord   ,
                                   version   ) ;
        }
        else if ( ! includingGraphVariables     && 
                    csvFile != null             &&
                   metaPatternManager.isMetaGraph()  ) {
            
            process = processOnlyCSV( outputFile  , 
                                      csvFile     , 
                                      classe      , 
                                      column      ,
                                      matchColumn ,
                                      matchWord   ,
                                      version   ) ;
        }
        else  {
            process = processOnlyGraphVariables( outputFile , version  ) ;
        }
        
        if ( ! process  )                                            {
         
            process = processOnlyGraphWithoutVariables( outputFile ) ;              
        } 
	    
	if ( process && ! obdaHeader.ok() ) {
           System.out.println("                                                           ") ;
           System.out.println(" <<<< Warning : Connection informations not Provided >>>> " ) ;
           System.out.println("                                                           ") ;
        }
	    
    }
    
    public static boolean okMatchersAndValidateMapping ( Node node           , 
                                                         String variableName )  {
     
	if( node.getUri() == null ) {
            Messages.printErrorNoURIProvided( node.getCode()  ,
                                              variableName  ) ;
            return false ;
        }
	    
        String[] uri_tokens              =  toTokens ( node.getUri() )                     ;
        String[] predicate_values_tokens =  toTokens ( node.outputOnlyPredicatesValues() ) ;
        String[] queries_tokens          =  toTokens ( node.getQuery() )                   ;
        
        return tokensChecker   ( node, variableName, uri_tokens             , PART.SUBJECT         ) &&
               tokensChecker   ( node, variableName, predicate_values_tokens, PART.PREDICAT_VALUES ) &&
               tokensChecker   ( node, variableName, queries_tokens         , PART.QUERY           ) &&
               validateMapping ( node,               node.outputTurtle()    , node.getQuery()      )  ;
    }

    private static String[] toTokens ( String token ) {
        
        return  token.replaceAll ( " +", " ")
                     .replace ( "\t", " ")
                     .replace ( "\n", " ")
                     .split (" ") ; 
    }
    
    private static boolean tokensChecker ( Node   node         , 
                                           String variableName ,  
                                           String[] tokens     , 
                                           PART part           ) {
         boolean ok = true ;
         
         for ( String token : tokens ) {
            
           if ( token.contains("?") ) {
               
               if( null !=  part ) switch ( part ) {
                   
                   case SUBJECT :
                       Messages.printErrorMatcherOnSubject( node.getCode() ,
                                                            variableName   ,
                                                            token  )       ;
                       break ;
                       
                   case PREDICAT_VALUES :
                       Messages.printErrorMatcherOnObject( node.getCode() ,
                                                           variableName   ,
                                                           token  )       ;
                       break ;
                       
                   case QUERY :
                       Messages.printErrorMatcherOnQuery( node.getCode() ,
                                                          variableName   ,
                                                          token  )       ; 
                       break ;
                       
                   default :
                       break ;
                       
               }
             
             if( ok ) ok = false ;
           }
        }
         return ok ;
    }
    
    /* Override Prefixs */
    
    private void updatePrefixs(String prefixFile, Map<String, String> prefixMap ) {
       
        if ( prefixFile == null || prefixMap == null ) return ;
         
        if ( ! Writer.existFile ( prefixFile ) )      return  ;
	    
        try ( Stream<String> lines = Files.lines(Paths.get(prefixFile))) {
            
            lines.forEach ( line  -> {

                 String[] splitedLines = line.replaceAll(" +", " ").trim().split(" ") ;
                 
                 if( splitedLines.length >= 3 ) 
                   prefixMap.put(splitedLines[1].trim(), splitedLines[2].trim().replace("<", "")
                                                                               .replace(">","")) ;
            }) ;
                        
	} catch (IOException e)  {
	     e.printStackTrace() ;
	}
    }

    /* Override Conection Info */ 
    
    private void updateConnection(String connecFile, Map<String, String> sourceDeclarationMap) {
        
        if( connecFile == null || sourceDeclarationMap == null ) return ;
        
        if( ! Writer.existFile ( connecFile ) ) return                  ;
	    
        try (Stream<String> lines = Files.lines(Paths.get(connecFile))) {

	  lines.forEach ( line  -> {
              if(line.replaceAll(" +", " ").trim().startsWith("obda-") && line.contains(":"))  {
                String[] splitedLines = line.replaceAll(" +", "").trim().split(":", 2) ;
                if( splitedLines.length >= 2 ) 
                sourceDeclarationMap.put(  splitedLines[0].trim().replace("obda-", "") , 
                                           splitedLines[1].trim()) ;
              }
          }) ;

	} catch (IOException e)  {
	     e.printStackTrace() ;
	}
    }
    
    /* Override magicFilter  */
    
    private String updateMagicFilter ( String magicFilterFile ) throws IOException {
     
       if ( magicFilterFile == null || magicFilterFile.isEmpty() )       {
          return null ;
       }
       
       if( ! Writer.existFile ( magicFilterFile ) ) return null          ;
	    
       return new String(Files.readAllBytes(Paths.get(magicFilterFile))) ;
    }
    
    private static boolean validateMapping( final Node    node         ,
                                            final String  outputTurtle ,
                                            final String  query        ) {
        
       if ( node == null ) return false                                ;
       int  code  =        node.getCode()                              ;
       
       Matcher sql_params = PATTERN_KEY_VALUES.matcher (outputTurtle ) ;
       
       String aliasesAndNames = "" ;
       
       try {
           
         aliasesAndNames = SqlAnalyzer.extractFullyQualifiedNameAndAliases( query ) ; 
         
       } catch( Exception ex ) {
           
            System.out.println( " ==> [X] Error Mapping on the node ( " + code + " ). " ) ;

            System.out.println( "   --> It seems that the QUERY of the Node ( " + code + " ) "
                                + "Generated a Malformed_SQL_Exception " )         ;
            System.out.println( "   --> Please, correct the QUERY and Re-try  " )  ;
            System.out.println("   --> Message Exception : " + ex.getMessage()  )  ;
            System.out.println(" ")   ;
            return false              ;
       } 
       
       while ( sql_params.find() ) {
           
           String sql_param = sql_params.group(1).replaceAll("\" +", "\"") ;
           
           if( ! aliasesAndNames.contains( sql_param + " " )        &&
               ! aliasesAndNames.contains( " " + sql_param + " " )  &&
               ! aliasesAndNames.contains( "." + sql_param + " " ) ) {
               
               
               if( query == null || query.equals(ManagerQuery.SPEACIAL_SQL_QUERY )) {
                   
                    System.out.println( " ==> [X] Error Mapping on the node code [ " + code + " ]. " ) ;
                    
                    System.out.println( "   --> It seems that  [ " + sql_param + " ] was used in the "  +
                                        "[ URI ( " + code + " ) ] but no [ QUERY ( " + code + " ) ] was Provided ! " ) ;
                    System.out.println( "   --> Please, add a QUERY with the code [ " + code + " " +
                                        "] and make sure you SELECT the field name  [ " + sql_param + " ]" ) ;
               
                    System.out.println("       ** ACTUAL SELECT : [ " + aliasesAndNames + " ] " ) ;
                    System.out.println(" ") ;
               
               } else {
                    
                     if ( node.getUri().contains("{"+ sql_param + "}")) {
                       
                        System.out.println( " ==> [X] Error Mapping on the node code [ " + code + " ]. " ) ;
                       
                        System.out.println( "   --> It seems that  [ " + sql_param + " ] was used in the " +
                                           "[ URI ( " + code + " ) ] but not FOUND in the [ QUERY ( " + code + " ) ] ! " ) ;
                        System.out.println( "   --> Please, add to the SLECT of the QUERY ( " 
                                            + code + " ) the field name [ " + sql_param + " ]" )     ;
                        System.out.println("       ** ACTUAL SELECT : [ " + aliasesAndNames + " ] " ) ;
                        System.out.println(" ") ;
               
                     } else {
                         /*
                        System.out.println( "   --> It seems that  Node ( "  + code + 
                           " ) is probably linked to another Node that requires the field name [ " + sql_param + " ] " ) ;
                        System.out.println( "   --> Please, add to the SLECT of the QUERY ( " 
                                            + code + " )  the field name [ " + sql_param + " ]" )         ;
                         */
                     }
               }
              
               return false ;
           }
       }
      
       return true ;
    }    
    
}
