
package org.inra.yedgen.splitter ;

import java.io.File ;
import java.util.Map ;
import java.util.Set ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.io.IOException ;
import java.util.ArrayList ;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern ;
import org.inra.yedgen.processor.io.Writer ;
import org.inra.yedgen.processor.entities.Node ;
import org.inra.yedgen.obda.header.ObdaHeader ;
import static java.util.stream.Collectors.toSet ;
import org.inra.yedgen.properties.ObdaProperties ;
import org.inra.yedgen.processor.entities.Variable ;
import org.inra.yedgen.processor.managers.ManagerQuery ;
import static org.inra.yedgen.processor.Processor.okMatchersAndValidateMapping ;

/**
 *
 * @author ryahiaoui
 */
public class ObdaSplitter_V1 {
 
   
    /** Magic-Filter Exemples :

     ##Magic_Filter : (124 , 125) { myList_01 & 1  } { myList_02 &  20  } PEEK 1 ;
                      2015-2017 {rac_range & 1 } STEP 1Y ;
                      ( 'YY' , 'RR', 'AA' )  { value_01 & 1  } { value_02 & 2  } PEEK 1 ;
                      
     ##Magic_Filter : (124 , 125) { myList_01 & 1  } { myList_02 &  20  } PEEK 1 ;
                      2017-05-01_2017-05-10 {rac_range & 1 } STEP 1M ;
                      ( 'YY' , 'RR', 'AA' )  { value_01 & 1  } { value_02 & 2  } PEEK 1 ;
     
     ##Magic_Filter : (124 , 125) { myList_01 & 1  } { myList_02 &  20  } PEEK 1 ;
                      2017-05-01_2017-05-10 {rac_range & 1 } STEP 5D ;
                      ( 'YY' , 'RR', 'AA' )  { value_01 & 1  } { value_02 & 2  } PEEK 1 ;
     
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
              
        List<MagicFilter> magicFilters = new ArrayList<>() ;
        
        List<String> subFilters = Arrays.asList( magicFilter.trim()
                                        .replaceAll(" +", " ").split(";")) ;
        
        for ( String filter : subFilters )       {
            if( ! MagicFilter.isCommented(filter))         {
               magicFilters.add( new MagicFilter(filter) ) ;
            }
        }
        
        cartesianProduct ( magicFilter   ,
                           nodes         ,
                           variable      ,
                           obdaHeader    ,
                           managerQuery  ,
                           outFile       ,
                           magicFilters) ;
    }

    private static String extrectIndexFromFileName(String fileName) {
       return fileName.split("_")[0] ;
    }
    
    public static <T> void cartesianProduct ( String            magicFilter  ,
                                              Set<Node>         nodes        ,
                                              Variable          variable     ,
                                              ObdaHeader        obdaHeader   ,
                                              ManagerQuery      managerQuery ,
                                              String            outFile      ,
                                              List<MagicFilter> magicFilters ) throws IOException {
        
      List<List<Object>> lists = new ArrayList<>()       ;
      magicFilters.forEach( m -> lists.add(m.getList())) ; 
         
      int solutions = 1 ;
      
      for( int i = 0; i < lists.size(); i++ ) solutions *= lists.get(i).size() ;
      
      for(int i = 0; i < solutions; i++ ) {
          
          int  j                = 1                 ;
          int  indexMagicFilter = 0                 ;
          
          Set<Node> copyNodes = nodes.stream()
                                     .map( node -> node.copy() )
                                     .collect(toSet()) ;
         
          List<String> outPut    = new ArrayList<>() ;
          outPut.addAll(obdaHeader.getHeaderOut())   ; 
                
          List< Map<Integer, String> > combined = new ArrayList<>() ;

          for( List<Object> list : lists)  {
             
             boolean isList = magicFilters.get(indexMagicFilter).getIsList() ;
             
             if( isList) {
               combined.add( applyList( magicFilters.get(indexMagicFilter).getCodeQueryByVariables() , 
                             list.get( (i/j) % list.size() ) ) ) ;
             }
             else {
               combined.add( applyRange( magicFilters.get(indexMagicFilter).getCodeQueryByVariables() , 
                             list.get( (i/j) % list.size() ) ) ) ;
             }
     
             AtomicInteger ErrorCheckMatchersAndValidateMapping = new AtomicInteger(0) ;
             
             if( indexMagicFilter == lists.size() -1 ) {

              copyNodes.forEach (node -> {
          
                combined.forEach( applyedValues -> {
                     
                      String filter = applyedValues.get(node.getCode()) ;

                      if( filter == null ) {
                          /* Then Probably codeNode != codeQuery */
                          /* Use managerQuery to resolve codeQuery */
                          filter = applyedValues.get( managerQuery
                                  .getLinkedQueryCodeByCodeNode(node.getCode())) ;                                          
                      }

                      if( filter != null ) {
                            node.applyToQuery( filter ) ;
                      }
                }) ;
         
                outPut.add ( node.outputObda()) ;
                
                boolean checkMatchersAndValidateMapping = okMatchersAndValidateMapping( node  ,
                                                                                        variable.getVariableName() ,
                                                                                        node.outputObda()        ) ;
                
                if( ! checkMatchersAndValidateMapping ) ErrorCheckMatchersAndValidateMapping.getAndIncrement()     ;
                
              }) ;
             }                        
          
             if( ErrorCheckMatchersAndValidateMapping.get() == 0 ) {
             
               if ( indexMagicFilter == lists.size() -1 ) {
         
                    outPut.add( ObdaProperties.MAPPING_COLLECTION_END ) ;

                    String folder    = Writer.getFolder ( outFile )      ;
                    String fileName  = Writer.getfileName ( outFile )    ;
                    String extension = Writer.getFileExtension(fileName) ; 
                    String fileNameWithoutExtension = Writer.getFileWithoutExtension(fileName ) ;

                    String outF =  folder + File.separator + extrectIndexFromFileName(fileName)     + 
                                   File.separator + fileNameWithoutExtension  + "_" + i + extension ;

                    Writer.checkFile( outF )             ;

                    Writer.writeTextFile( outPut, outF ) ;
               }
             }
             
             System.out.println( " Applying Filters -> "           + 
                                 list.get( (i/j) % list.size() ) ) ;
   
             j *= list.size()     ;
             indexMagicFilter ++  ;
             
         }
          
         outPut.clear()    ;
         copyNodes.clear() ;
         System.out.println( " *********************************** ") ;
         
      }
      
      System.out.println( "                                           " ) ;
      System.out.println( " Total Combinations : " + solutions  + "\n " ) ;
      System.out.println( " ======================================="    + 
                          "======================= "                    ) ;
      System.out.println( "                                           " ) ;
    
    }
    
    private static Map<Integer, String> applyRange( Map<Integer, String> codeQueryByVariables , 
                                                    Object get)                               {
       List l = (List) get ;
       
       Map<Integer, String> copy = new HashMap<>(codeQueryByVariables)      ; 
       
       copy.replaceAll ( (k, u) ->  u.replaceAll( Pattern.quote("?DATE_0")  ,
                                                 "'" + l.get(0).toString()  + "'" )
                                     .replaceAll( Pattern.quote("?DATE_1" ) , 
                                                  "'" + l.get(1).toString() + "'" )
       ) ;
       return copy ;
    }

    private static Map<Integer, String> applyList( Map<Integer, String> codeQueryByVariables , 
                                                   Object string )                           {
     
      Map<Integer, String> copy = new HashMap<>(codeQueryByVariables) ; 
        
      copy.replaceAll ( (k, u) ->  u.replaceAll ( Pattern.quote("?LIST") , 
                                                  string.toString() ) )  ;
      return copy ;
    }

}
