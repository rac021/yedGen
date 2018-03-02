
package org.inra.yedgen.splitter ;

import java.io.File ;
import java.util.Map ;
import java.util.Set ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.io.IOException ;
import java.util.ArrayList ;
import java.util.regex.Matcher ;
import java.util.stream.Stream ;
import java.util.regex.Pattern ;
import java.util.stream.IntStream ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.io.Writer ;
import org.inra.yedgen.obda.header.ObdaHeader ;
import org.inra.yedgen.processor.entities.Node ;
import static java.util.stream.Collectors.toSet ;
import java.util.concurrent.atomic.AtomicInteger ;
import org.inra.yedgen.properties.ObdaProperties ;
import static java.util.stream.Collectors.toList ;
import org.inra.yedgen.processor.entities.Variable ;
import org.inra.yedgen.processor.managers.ManagerQuery ;
import static org.inra.yedgen.processor.Processor.okMatchersAndValidateMapping ;

/**
 *
 * @author ryahiaoui
 */
public class ObdaSplitter {
 
   
    /** Magic-Filter Exemples :
    
      ##Magic_Filter : ( 131073, 229390, 98304, 163841, 163842 , 1, 229378 ) 
                       { rootsite.rootzet_id & 1, 2, 200, 201, 202, 50, 51, 52   } PEEK 1 ; 
                       2000_2015 {  EXTRACT(YEAR FROM mfs_date) & 1, 2, 200, 201 } STEP 1
   
      ##Magic_Filter : ( 163841, 1, 1445, 124 ) { var_01 & 20  } { var_02 & 50 } PEEK 4 
      
      ##Magic_Filter : ('val_1','val_2','val_3' ) { var_01 & 20,8  } { var_02 & 51 } PEEK 1 ;
                       1900_2015 { EXTRACT( YEAR FROM mfs_date) & 20 }  
                                 { EXTRACT( YEAR FROM mfs_date_Plus ) &  50, 51 } STEP 1Y   ;

      ##Magic_Filter : 2014_2015 { EXTRACT( YEAR FROM mfs_date) & 20 } 
                                 { EXTRACT( YEAR FROM mfs_date_other_var ) &  21, 22 } STEP 1Y ;
     
    **/
    
    /**
     * 
     * @param magicFilter
     * @param nodes
     * @param variable
     * @param obdaHeader
     * @param managerQuery
     * @param outFile
     * @throws IOException 
     */

    public static void split ( String       magicFilter  ,
                               Set<Node>    nodes        ,
                               Variable     variable     ,
                               ObdaHeader   obdaHeader   ,
                               ManagerQuery managerQuery ,
                               String       outFile      ) throws IOException {
        
       
        List<String> outPut    = new ArrayList<>() ;

        String[] expressions = magicFilter.replaceAll(" +", " ").trim().split(";") ;
      
        Pattern patternFilter = Pattern.compile("\\{.*?\\}") ;
        Pattern listFilter    = Pattern.compile("\\(.*?\\)") ;
        
        List<String> peekerList       = new ArrayList<>() ;
        Map<Integer, String > filters = new HashMap<>()   ;
        List<Integer> dates           = new ArrayList<>() ;
      
        int peekerSize    = 0  ;
        String step       = "" ;
       
        for ( String expresion : expressions ) {

            if( isPeek(expresion) ) {
                
               peekerSize  = Integer.parseInt(expresion.toLowerCase().split(" peek ")[1].trim()) ;
               
               Matcher mListFilter  = listFilter.matcher( expresion)    ;
               
                while ( mListFilter.find() ) {
                    
                    List<String> asList = Arrays.asList(mListFilter.group()
                                                .replace("(", "").replace(")","")
                                                .trim().split(",")) ;
                    peekerList          = peeker( peekerSize, asList) ;
                    
                }
                
              Matcher mPatternQuery  = patternFilter.matcher( expresion) ;
              List<String> vars      = new ArrayList()                   ;
              
              while ( mPatternQuery.find()) {
                  
                   String[] split1 = mPatternQuery.group().replace("{", "").replace("}","")
                                                                           .trim().split("&") ;
                   String varName  = split1[0].trim() ;

                  Stream.of(split1[1].trim().replaceAll(" +", "").split(","))
                        .filter( s -> ! s.isEmpty() )
                        .map (s -> filters.compute ( 
                                Integer.parseInt(s) ,
                                ( k,v)-> v != null ? v + " AND " + 
                                  varName + " IN ( ?LIST ) " : varName + " IN ( ?LIST ) "))
                        .count() ;
                                                 
                vars.add( mPatternQuery.group().replace("{", "").replace("}","").trim() ) ;
              } 
                
            }
            
            else if(isStep(expresion)) {
                
              step  = expresion.toLowerCase().split(" step ")[1].trim() ;
               
              dates = Stream.of(expresion.split("\\{")[0].trim().replaceAll(" +", "").split("_"))
                            .filter( s -> ! s.isEmpty() )                            
                            .map(s -> Integer.parseInt(s))
                            .collect(toList()) ;
              
              Matcher mPatternQuery  = patternFilter.matcher( expresion) ;
              
              while ( mPatternQuery.find()) {
                   String[] split1 = mPatternQuery.group().replace("{", "")
                                                  .replace("}","").trim().split("&") ;
                   String varName  = split1[0].trim() ;

                   Stream.of(split1[1].trim().replaceAll(" +", "").split(","))
                         .filter( s -> ! s.isEmpty() )                                                
                         .map(s -> filters.compute( Integer.parseInt(s),( k,v)-> 
                                                    v != null ? v + "AND ( " + varName + " BETWEEN ?DATE_0 AND ?DATE_1 ) " : 
                                                    " ( " + varName + " BETWEEN ?DATE_0 AND ?DATE_1 ) "))
                    .count() ;
              } 
            }
        }
        
        System.out.println("                         " )      ;
        System.out.println(" Dates       --> " + dates )      ;
        System.out.println(" Peeker List --> " + peekerList ) ;
        System.out.println(" Peek        --> " + peekerSize)  ;
        System.out.println(" Steps       --> " + step)        ;
        System.out.println(" Filters     --> " + filters )    ;
        System.out.println("                         " )      ;
        
       Map<Integer, String >  copy = new HashMap(filters) ;
       
       if( ! step.isEmpty()      && 
           ! step.endsWith("D")  && 
           ! step.endsWith("d")  && 
           ! step.endsWith("m")  && 
           ! step.endsWith("M") ) {
           
           int _step = Integer.parseInt(step.replace("y", "").replace("Y", "")) ;

           int max   = 0 ;
           int index = 0 ;
              
           if( ! dates.isEmpty() ) {
                             
                for( int i = dates.get(0) ; i<= dates.get(1) ; i +=_step  ) {

                       copy = new HashMap(filters) ;
                       final int ii = i ;
                       max = i+_step > dates.get(1) ? dates.get(1) : i+_step -1 ;
                       final int maxx = max ;
                       copy.replaceAll ( (k, u) -> 
                                u.replaceAll(Pattern.quote("?DATE_0"), "" + ii  )
                                 .replaceAll(Pattern.quote("?DATE_1"), "" + maxx  )  
                              ) ;
                       
                    final Map<Integer, String >  copyCodeQueriesWithFilter   = new HashMap(copy) ;
                    
                    if( ! peekerList.isEmpty() )
                    for( String filterList : peekerList ) {
                        
                        Set<Node> copyNodes = nodes.stream()
                                                   .map( node -> node.copy() )
                                                   .collect(toSet()) ;
                           
                        copyCodeQueriesWithFilter.replaceAll ( (k, u) -> 
                                                               u.replaceAll( Pattern.quote("?LIST") , 
                                                                             filterList ) 
                        ) ;
              
                        outPut.clear() ;
                        outPut.addAll(obdaHeader.getHeaderOut()) ; 
                         
                        AtomicInteger ErrorCheckMatchersAndValidateMapping = new AtomicInteger(0) ;
                        
                        copyNodes.forEach (node -> {
                              
                            String filter = copyCodeQueriesWithFilter.get(node.getCode()) ;
                              
                            if( filter == null ) {
                                /* Then Probably codeNode != codeQuery */
                                /* Use managerQuery to resolve codeQuery */
                                filter = copyCodeQueriesWithFilter.get( managerQuery
                                        .getLinkedQueryCodeByCodeNode(node.getCode())) ;                                          
                            }
                                
                            if( filter != null ) {
                                  node.applyToQuery( filter ) ;
                            }
                            
                            outPut.add( node.outputObda()) ;
                            
                            boolean checkMatchersAndValidateMapping = okMatchersAndValidateMapping( node                       , 
                                                                                                    variable.getVariableName()
                                                                                                   )                           ;
                            
                            if( ! checkMatchersAndValidateMapping ) ErrorCheckMatchersAndValidateMapping.getAndIncrement()     ;
                                       
                       }) ;
                        
                       if( ErrorCheckMatchersAndValidateMapping.get() == 0 ) {
                 
                            outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;

                            String folder    = Writer.getFolder ( outFile )      ;
                            String fileName  = Writer.getfileName ( outFile )    ;
                            String extension = Writer.getFileExtension(fileName) ; 
                            String fileNameWithoutExtension = Writer.getFileWithoutExtension(fileName ) ;

                            String outF =  folder + File.separator + extrectIndexFromFileName(fileName) + 
                                           File.separator + fileNameWithoutExtension  + "_" + index ++  + extension ;

                            Writer.checkFile( outF )                ;

                            Writer.writeTextFile(outPut, outF )    ;

                            copyCodeQueriesWithFilter.clear()      ;
                            copyCodeQueriesWithFilter.putAll(copy) ;
                       }
            
                   } else {
                        
                        /* Peeker List Empty */
                             
                        Set<Node> copyNodes = nodes.stream()
                                                   .map( node -> node.copy() )
                                                   .collect(toSet()) ;
                         
                        outPut.clear();
                        outPut.addAll(obdaHeader.getHeaderOut())   ; 
                          
                        copyNodes.forEach (node -> {
                            
                            String filter = copyCodeQueriesWithFilter.get(node.getCode()) ;
                            
                            if( filter == null ) {
                                /* Then Probably codeNode != codeQuery */
                                /* Use managerQuery to resolve codeQuery */
                                filter = copyCodeQueriesWithFilter.get( managerQuery
                                        .getLinkedQueryCodeByCodeNode(node.getCode())) ;                                          
                                }
                                if( filter != null ) {
                                    node.applyToQuery( filter ) ;
                                }
                                outPut.add( node.outputObda()) ;
                                okMatchersAndValidateMapping( node                         ,
                                                              variable.getVariableName() ) ;
                                        
                           }) ;
                                   
                               
                       outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;
                      
                       String folder    = Writer.getFolder ( outFile )      ;
                       String fileName  = Writer.getfileName ( outFile )    ;
                       String extension = Writer.getFileExtension(fileName) ; 
                       
                       String fileNameWithoutExtension = Writer.getFileWithoutExtension(fileName ) ;
                        
                       String outF =  folder + File.separator + extrectIndexFromFileName(fileName) + 
                                      File.separator + fileNameWithoutExtension  + "_" + index ++ + extension ;
                       
                       Writer.checkFile( outF ) ;
                                            
                       Writer.writeTextFile(outPut, outF )    ;
                        
                       copyCodeQueriesWithFilter.clear()      ;
                       copyCodeQueriesWithFilter.putAll(copy) ;
                        
                    }

                }
          } else {
               
                  for ( String filterList : peekerList ) {
                        
                     copy.replaceAll ( ( k, u ) -> 
                                       u.replace("?LIST" , filterList ) 
                     ) ;
                      
                     final Map<Integer, String >  copyCodeQueriesWithFilter   = new HashMap(copy) ;
                    
                     Set<Node> copyNodes = nodes.stream()
                                                .map( node -> node.copy() )
                                                .collect(toSet()) ;
                         
                     outPut.clear() ;
                     outPut.addAll(obdaHeader.getHeaderOut()) ; 
                  
                     copyNodes.forEach (node -> {
                           
                         String filter = copyCodeQueriesWithFilter.get(node.getCode()) ;
                            
                         if( filter == null ) {
                               /* Then Probably codeNode != codeQuery */
                               /* Use managerQuery to resolve codeQuery */
                               filter = copyCodeQueriesWithFilter.get( managerQuery
                                       .getLinkedQueryCodeByCodeNode(node.getCode())) ;                                          
                         }
                         
                         if( filter != null ) {
                               node.applyToQuery( filter ) ;
                         }
                            
                         outPut.add( node.outputObda()) ;
                         okMatchersAndValidateMapping( node                     ,
                                                       variable.getVariableName() ) ;
                                      
                     }) ;
                                 
                             
                     outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;
                    
                     String folder    = Writer.getFolder ( outFile )   ;
                     String fileName  = Writer.getfileName ( outFile ) ;
                     String fileNameWithoutExtension = Writer.getFileWithoutExtension(fileName ) ;
                     String extension = Writer.getFileExtension(fileName) ; 
                      
                     String outF =  folder + File.separator + extrectIndexFromFileName(fileName) + 
                                      File.separator + fileNameWithoutExtension  + "_" + index ++ + extension ;
                       
                     Writer.checkFile( outF )             ;
                                            
                     Writer.writeTextFile( outPut, outF ) ;
                     copy = new HashMap(filters)          ;
            
                  }
           }
                   
       } else {
                /* Empty Steps */
                
                int index = 0   ;
                
                for( String filterList : peekerList ) {
                        
                    copy.replaceAll ( (k, u) -> 
                          u.replaceAll(Pattern.quote("?LIST") , filterList ) 
                    ) ;
            
                    final Map<Integer, String >  copyCodeQueriesWithFilter   = new HashMap(copy) ;  
                    
                    Set<Node> copyNodes = nodes.stream()
                                               .map( node -> node.copy() )
                                               .collect(toSet()) ;
                           
                    outPut.clear() ;
                    outPut.addAll(obdaHeader.getHeaderOut()) ; 
                    
                    copyNodes.forEach (node -> {
                        
                        String filter = copyCodeQueriesWithFilter.get(node.getCode()) ;
                        
                        if( filter == null ) {
                            /* Then Probably codeNode != codeQuery */
                            /* Use managerQuery to resolve codeQuery */
                            filter = copyCodeQueriesWithFilter.get( managerQuery
                                     .getLinkedQueryCodeByCodeNode(node.getCode())) ;                                          
                            }
                            if( filter != null ) {
                               node.applyToQuery( filter ) ;
                            }
                            outPut.add( node.outputObda()) ;
                            okMatchersAndValidateMapping( node                     ,
                                                          variable.getVariableName() ) ;
                    }) ;
                               
                    outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;
                      
                    String folder    = Writer.getFolder ( outFile )   ;
                    String fileName  = Writer.getfileName ( outFile ) ;
                    String fileNameWithoutExtension = Writer.getFileWithoutExtension(fileName ) ;
                    String extension = Writer.getFileExtension(fileName) ; 
                        
                    String outF =  folder + File.separator + extrectIndexFromFileName(fileName) + File.separator +
                                   fileNameWithoutExtension  + "_" + index ++ + extension ;
                       
                    Writer.checkFile( outF )            ;
                    Writer.writeTextFile(outPut, outF ) ;
                    
                    copy = new HashMap(filters)        ;
                                
                }
       }
    
    }
   	
    public static  List<String> peeker(int peek, List<String> elements ) {

        int listSize   = elements.size()                    ;
        int chunkSize  = peek > 0 ? peek : elements.size()  ;
            
        List<String> chunkedList = IntStream.range( 0 , ( listSize - 1 ) / peek + 1 )
                                            .mapToObj( i -> elements
                                            .subList ( i *= chunkSize ,
                                                       listSize - chunkSize >= i ? i + chunkSize : listSize))
                                            .map( l -> String.join(" , ", l ) )
                                            .collect(Collectors.toList());
        
        return chunkedList ;
    }   

    
    private static boolean isPeek(String string ) {
        return string.toLowerCase().contains(" peek ") ;
    }
    private static boolean isStep(String string) {
        return string.toLowerCase().contains(" step ") ;
    }

    private static String extrectIndexFromFileName(String fileName) {
       return fileName.split("_")[0] ;
    }

}
