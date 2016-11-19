
package org.inra.yedgen.processor ;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.inra.yedgen.graph.utils.Utils;
import org.inra.yedgen.processor.io.Writer;
import org.inra.yedgen.graph.entities.Edge;
import org.inra.yedgen.processor.io.ObdaHeader;
import org.inra.yedgen.processor.entities.Node;
import org.inra.yedgen.properties.CsvProperties;
import org.inra.yedgen.properties.ObdaProperties;
import org.inra.yedgen.graph.managers.ManagerEdge;
import org.inra.yedgen.processor.entities.Variable;
import org.inra.yedgen.processor.managers.ManagerUri;
import org.inra.yedgen.graph.managers.ManagerConcept;
import org.inra.yedgen.graph.managers.GraphExtractor;
import org.inra.yedgen.processor.managers.ManagerNode;
import org.inra.yedgen.processor.errors.MessageErrors;
import org.inra.yedgen.processor.factories.FactoryNode;
import org.inra.yedgen.processor.managers.ManagerQuery;
import org.inra.yedgen.processor.managers.ManagerVariable;
import org.inra.yedgen.processor.managers.MetaPatternManager;
import org.inra.yedgen.processor.managers.ManagerPatternContext;
import org.inra.yedgen.processor.managers.ManagerPatternParallel;
import static org.inra.yedgen.graph.managers.GraphExtractor.PREFIX_PREDICAT;

/**
 *
 * @author ryahiaoui 26-10-2016 17:20
 */
public class Processor {
    
    private final ManagerPatternParallel managerPatternParallel  ;
    private final ManagerPatternContext  managerPatternContext   ;
    private final MetaPatternManager     metaPatternManager      ;
    private final ManagerVariable        managerVariable         ;
    private final GraphExtractor         graphExtractor          ;
    private final ManagerNode            managerNode             ;
    private final ObdaHeader             obdaHeader              ;
    
    private final CsvProperties          csvProperties           ;
    
    private boolean                      verbose                 ;
    
    
    public Processor( String directory     , 
                      String extensionFile ,
                      String propertieFile ,
                      String jsFile        ) throws Exception                {
   
      this.graphExtractor  =  new GraphExtractor ()                          ;
      graphExtractor.genGraphPopulatingManagers( directory , extensionFile ) ;

      ManagerUri     managerUri     =  new ManagerUri( graphExtractor.getMapUris())          ;
      ManagerEdge    managerEdge    =  new ManagerEdge( graphExtractor.getMapEdges() )       ;
      ManagerQuery   managerQuery   =  new ManagerQuery( graphExtractor.getMapQueries() )    ;
      ManagerConcept managerConcept =  new ManagerConcept( graphExtractor.getMapConcepts() ) ;
      
      
      FactoryNode factoryNode     = new FactoryNode( managerEdge      ,
                                                     managerConcept   , 
                                                     managerUri       , 
                                                     managerQuery   ) ;
            
      this.managerNode            = instantiateManagerNode ( managerEdge, factoryNode )  ;
        
      this.obdaHeader             = new ObdaHeader(graphExtractor)                       ;
      
      this.managerPatternContext  = new ManagerPatternContext ( graphExtractor.getMapPatternContexts() , 
                                                                managerQuery     , 
                                                                factoryNode  )   ;
      
      this.csvProperties =        new CsvProperties( propertieFile, jsFile )     ;
        
      this.metaPatternManager     = new  MetaPatternManager( graphExtractor.getMetaPatternHash()     ,
                                                             graphExtractor.getMetaPatternVariable() , 
                                                             graphExtractor.getMetaPatternContext()  , 
                                                             graphExtractor.getMetaPatternParallel() ,
                                                             csvProperties                         ) ;
      
      this.managerPatternParallel = new ManagerPatternParallel ( graphExtractor.getMapPatternParallels() , 
                                                                 managerUri     , 
                                                                 factoryNode  ,
                                                                 metaPatternManager ) ;
      
      this.managerVariable        = new ManagerVariable( graphExtractor.getMapVariables() ,
                                                         managerNode                      , 
                                                         managerPatternContext            , 
                                                         managerPatternParallel)          ;
    }
    
    private ManagerNode instantiateManagerNode ( ManagerEdge managerEdge , FactoryNode factoryNode )  {
        
         ManagerNode localManager = new ManagerNode() ;
           
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
            
            Utils.putInMap(localManager.getNodes(), hash, subjectId, node) ;
        }
        
        localManager.cloneNodes() ;
        return  localManager      ;
    
    }
        
    public boolean processFull ( String outputFile, String csvFile )  {
        
        boolean processCSV       = processOnlyCSV(outputFile, csvFile)     ;
        
        boolean processVariables = processOnlyGraphVariables( outputFile ) ;
        
        return processVariables && processCSV ;
      
    }

    public boolean processOnlyGraphVariables ( String outputFile )        {
      
      MessageErrors.printMessageStartProcessVariableGraphGeneration()     ;
      
      for( Variable variable : managerVariable.getVariables()) {
     
         Set<Node> graph        = managerVariable.process( variable )     ;
          
         List<String> outPut    = new ArrayList<>()                       ;
                      
         // Prepare Output Header 
         outPut.addAll(obdaHeader.getHeaderOut())                         ; 
          
         graph.stream().forEach( node -> { outPut.add( node.outputObda())            ; 
                                           checkMatchers( variable.getVariableName() , 
                                                          node.outputObda() ) ; })   ;
          
         outPut.add(ObdaProperties.MAPPING_COLLECTION_END) ;
                
         try {
              
            String _fileName = outputFile.substring(0, outputFile.lastIndexOf('.')) ;
            String extension = outputFile.substring(outputFile.lastIndexOf('.'))    ;
             
            String outFile = _fileName + "_Graph_"                             + 
                             variable.getVariableName().replaceFirst(":", "")  + 
                             extension                                         ;
              
            Writer.checkFile( outFile )           ;
            Writer.writeTextFile(outPut, outFile) ;
            
            MessageErrors.printMessageInfoGeneratedVariable( variable.getVariableName() ,
                                                             outFile                  ) ;

         } catch (IOException ex) {
                Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex) ;
          }
      }
      
      return managerVariable.getVariables().size() > 0  ;
      
   }
    
    public boolean processOnlyCSV ( String outputFile , String csvFile ) {
         
         MessageErrors.printMessageStartProcessCsvVariableGeneration()     ;
         
         String pattContext  = metaPatternManager.getMetaPatternContext()  ;
         String pattVariable = metaPatternManager.getMetaPatternVariable() ;
         String pattParallel = metaPatternManager.getMetaPatternParallel() ;
         
         if( pattContext  == null   ||
             pattVariable == null   ||
             pattParallel == null    )                    {
                     
             MessageErrors.printMessageMetaPatternsNull() ;
             return false                                 ;
         }
  
        try {
            
            CsvProperties csvProperties = new CsvProperties("../ola.properties", 
                                                             "../ola.js") ;
            
            Files.lines ( Paths.get(csvFile) ).skip(1).forEach (
                    
                  (String line) -> {
                     
                      List<String> outPut    = new ArrayList<>() ;
                      
                      // Prepare Output Header 
                      outPut.addAll(obdaHeader.getHeaderOut()) ; 
                     
                     // Treat Variable
                     //String nLine = csvProperties.process( line )                             ;
                     String patternContext   = metaPatternManager.generatePatternContext(line)  ;
                     String patternVariable  = metaPatternManager.generatePatternVariable(line) ;
                     
                     Variable variable       = managerVariable.transformToVariable( patternVariable   ,
                                                                                    patternContext  ) ;
                        
                     Set<Node> nodes         = managerVariable.process( variable )    ;
                        
                     nodes.stream().forEach( node -> { outPut.add( node.outputObda())            ; 
                                                       checkMatchers( variable.getVariableName() , 
                                                                       node.outputObda() ) ; })  ;
                        
                     outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;
                                           
                     try {
              
                       String _fileName = outputFile.substring(0, outputFile.lastIndexOf('.')) ;
                       String extension = outputFile.substring(outputFile.lastIndexOf('.'))    ;

                       String outFile = _fileName + "_CSV_"                               + 
                                        variable.getVariableName().replaceFirst(":", "")  + 
                                        extension                                         ;
                       
                       Writer.checkFile( outFile )           ;
                       Writer.writeTextFile(outPut, outFile) ;
                       
                       MessageErrors.printMessageInfoGeneratedVariable( variable.getVariableName() ,
                                                                        outFile                  ) ;
                       
                     } catch (IOException ex) {
                          Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex)  ;
                     }
                  }
            ) ;
            
        } catch (IOException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex)  ;
        }
        
        try {
            return Files.lines( Paths.get(csvFile) ).skip(1).count() > 0 ;
        } catch ( IOException ex ) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex)  ;
        }
         
        return false ;
    }

    
    public boolean processOnlyGraphWithoutVariables ( String outputFile ) {
      
         Set<Node> all = managerNode.getAll()                             ;
      
         List<String> outPut    = new ArrayList<>()                       ;
                      
         // Prepare Output Header 
         outPut.addAll(obdaHeader.getHeaderOut())                         ; 
                      
         all.stream().forEach( node -> outPut.add( node.outputObda()) )   ;
         
         outPut.add( ObdaProperties.MAPPING_COLLECTION_END )              ;
                
         try {
              
            String _fileName = outputFile.substring(0, outputFile.lastIndexOf('.')) ;
            String extension = outputFile.substring(outputFile.lastIndexOf('.'))    ;
             
            String outFile   = _fileName + "_Graph_"                             + 
                               System.currentTimeMillis()                        +
                               extension                                         ;
              
            Writer.checkFile( outFile )           ;
            Writer.writeTextFile(outPut, outFile) ;
            
            MessageErrors.printMessageInfoGeneratedVariable( "Undefined Variable" ,
                                                             outFile            ) ;

         } catch (IOException ex) {
                Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex) ;
          }
     
      return all.size() > 0 ;
      
    }
    
    public void process ( String  outputFile              , 
                          String  csvFile                 , 
                          boolean includingGraphVariables ,
                          boolean verbose )               {
      
        this.verbose = verbose  ;
        
        boolean process = false ; 
        
        if( includingGraphVariables && csvFile != null ) {
            process = processFull( outputFile, csvFile )  ;
        }
        else if ( ! includingGraphVariables && 
                    csvFile != null )                      {
            process = processOnlyCSV( outputFile, csvFile) ;
        }
        else  {
            process = processOnlyGraphVariables( outputFile ) ;
        }
        
        if( ! process ) {
            processOnlyGraphWithoutVariables( outputFile )    ;
        }
    }

    
    private void checkMatchers( String variableName , String outLine ) {
   
        String[] patts = outLine.split(" ") ; 
        
        for ( String patt : patts ) {
           if ( patt.contains("?") )
           MessageErrors.printErrorMatcher( variableName , patt ) ; 
        }
    }

}
