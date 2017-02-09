
package org.inra.yedgen.processor.managers;

import java.util.List ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import org.inra.yedgen.properties.CsvProperties ;
import static org.inra.yedgen.processor.output.Messages.* ;
/**
 *
 * @author ryahiaoui
 */
public class MetaPatternManager                {
    
    private final Integer metaPatternHash      ;
    private final String  metaPatternVariable  ;
    private final String  metaPatternContext   ;
    private final String  metaPatternParallel  ;
    
    private final CsvProperties  csvProperties ;
    
    private final String CSV_SEPARATOR         ;
    
    private static final String  META_PATTERN_CONTEXT     = "##META_PATTERN_CONTEXT"  ;
    private static final String  META_PATTERN_PARALLEL    = "##META_PATTERN_PARALLEL" ;
    private static final String  META_VERIABLE            = "?META_VARIABLE"          ;
    private static final String  MATCHER_PATTERN_CONTEXT  = "##PATTERN_CONTEXT"       ;
    private static final String  MATCHER_PATTERN_PARALLEL = "##PATTERN_PARALLEL"      ;
    
    public MetaPatternManager( Integer metaPatternHash     ,
                               String metaPatternVariable  ,
                               String metaPatternContext   , 
                               String metaPatternParallel  ,
                               CsvProperties csvProperties ) {
        
        this.metaPatternHash     = metaPatternHash     ;
        this.metaPatternVariable = metaPatternVariable ;
        this.metaPatternContext  = metaPatternContext  ;
        this.metaPatternParallel = metaPatternParallel ;
        this.csvProperties       = csvProperties       ;
        
        if( this.csvProperties != null && this.csvProperties.getConfig() != null ) {
            
          CSV_SEPARATOR =  
          this.csvProperties.getConfig().getString("CSV_SEPARATOR") != null ?
          this.csvProperties.getConfig().getString("CSV_SEPARATOR") : "\t"  ;
        } else {
          CSV_SEPARATOR = "\t" ;
        }
        
         printMessage( " -> CSV_SEPARATOR :  " + CSV_SEPARATOR ) ;
    }

    public String getMetaPatternVariable() {
        return metaPatternVariable ;
    }

    public String getMetaPatternContext()  {
        return metaPatternContext ;
    }

    public String getMetaPatternParallel() {
        return metaPatternParallel ;
    }

    public Integer getMetaPatternHash()    {
        return metaPatternHash ;
    }
    
    public String generatePatternVariable ( String csvLine ) {
     
        checkMetaPatternVariable()                    ; 
        
        if( metaPatternVariable == null ) return null ;
        
        String variable = metaPatternVariable         ;
         
        Pattern p = Pattern.compile("[:'\"]?COLUMN_+\\w+['\"]?") ;
        Matcher m = p.matcher(metaPatternVariable )              ;
        
        List<String> columns    = new ArrayList() ;
        Comparator<String> comp = Comparator.comparing ( s -> s.length()) ;
        
        while (m.find())                                   {
          columns.add(m.group().replace(" + ", "").trim()) ;
        }
       
        Collections.sort(columns , comp.reversed() ) ;
         
        for( String params : columns )               {
          
          String[] combinedNums = params.split("_")  ;
            
          List<String> tmp = new ArrayList<>()       ;
            
          for( int i = 1 ; i < combinedNums.length ; i ++ )                         {              
             int num = Integer.parseInt( combinedNums[i].replaceAll("[^0-9]", "") ) ;
             tmp.add( csvLine.replaceAll(" +", " ")
                             .split(CSV_SEPARATOR)[num] )   ;
          }
            
          String resScript ;
          
          if(params.trim().startsWith(":") )                                         {
            tmp.add ( 0 , ": ") ;
            resScript = csvProperties.process( params.replaceFirst(":", "") , 
                                               tmp.toArray(new String[tmp.size()]) ) ;
          }
          else if( params.trim().startsWith("'") || params.trim().startsWith("\"") ) {
            
              resScript = csvProperties.process( params , 
                                                 tmp.toArray(new String[tmp.size()]) ) ; 
            
            if(resScript.contains(ManagerVariable.INTRA_COLUMN_SPLITTER )) {
                resScript = params.charAt(0) + 
                            resScript.replace(ManagerVariable.INTRA_COLUMN_SPLITTER, "','" ) + 
                            params.charAt(0) ;
            }            
          }
          
          else {
            resScript = csvProperties.process( params , 
                                               tmp.toArray(new String[tmp.size()]) ) ;
          }
         
          if(params.startsWith("'") && params.endsWith("'"))                     {
              variable = variable.replaceAll ( params, "'" + resScript + "'" )   ;
          }
          else if(params.startsWith("\"") && params.endsWith("\""))              {
              variable = variable.replaceAll ( params, "\"" + resScript + "\"" ) ;
          }
          else {
             variable = variable.replaceAll ( params, resScript ) ;
          }
         
        }

        return variable.replace(META_PATTERN_CONTEXT, MATCHER_PATTERN_CONTEXT  )
                       .replace(META_PATTERN_PARALLEL, MATCHER_PATTERN_PARALLEL) ;
    }

        
    public String generatePatternContext ( String csvLine ) {
        
        checkMetaPatternContext()                    ;
        
        if( metaPatternContext == null ) return null ;
        
        String pattern = "" ;
        String base    =  metaPatternContext.split(Pattern.quote("["))[0].trim() ;
        String matcher =  metaPatternContext.split(Pattern.quote("["))[1].trim()
                                            .replace("]", "")
                                            .trim() ;
        
        Pattern p = Pattern.compile("[:'\"]?COLUMN_+\\w+['\"]?") ;
        Matcher m = p.matcher( metaPatternContext )              ;
        
        m.find()                                                 ;
        String column = m.group().replace(" + ", "").trim()      ;
        
       int variablesColumnNum = Integer.parseInt( column.replaceAll("[^0-9]", "")) ;
       
       if( variablesColumnNum > csvLine.split(CSV_SEPARATOR).length ) {
            printMessageError( "-> Error : ColumnNumber > csvLine_Separator // Probably Bad CSV_SEPARATOR ! " ) ;
            System.exit( 0 ) ;
        }
        
       String  resScript = csvProperties.process( column.replace(":", "") , 
                             column.startsWith(":") ? 
                                " : " + ManagerVariable.INTRA_COLUMN_SPLITTER + " " + 
                                        csvLine.split(CSV_SEPARATOR)[variablesColumnNum].trim() :
                                csvLine.split(CSV_SEPARATOR)[variablesColumnNum].trim() )       ; 
        
        String nums            = matcher.split("Q_")[1]               ;
        int    startQueryNum   = Integer.parseInt(nums.split("_")[0]) ;
        int    middleQueryNum  = Integer.parseInt(nums.split("_")[1]) ;
        int    endQueryNum     = Integer.parseInt(nums.split("_")[2]) ;
        int loop               = startQueryNum                        ;
        
       
        if ( csvLine.split(CSV_SEPARATOR)[variablesColumnNum].trim().length() == 0 ) return null ;
        
         
        String[] variablesContext = resScript.replaceAll(" +", "")
                                             .trim()
                                             .split(ManagerVariable.INTRA_COLUMN_SPLITTER) ;
       
        Collections.reverse(Arrays.asList(variablesContext)) ;

        if( variablesContext.length == 1 ) loop += 2 ;
        
        for (int i = 0 ; i < variablesContext.length; i++ ) {
            
            String variable = variablesContext[i] ;
            
            pattern += "[ " + matcher.replace( column ,  variable )
                                     .replace(" Q_" + nums, " Q_" + String.valueOf(loop) ) + " ] " ;
            
            if( i == variablesContext.length - 2 || variablesContext.length ==  2 ) {
                loop = endQueryNum ;
            }
            else 
            if( i == 0 ) {
                loop = middleQueryNum ;
            }
        }
        
        return base + " " + pattern ;
     
    }

    
    public String generatePatternParallel ( String csvLine ) {
        checkMetaPatternParallel() ;
        return metaPatternParallel ;
    }

 
    private String validatePrefix ( String defaulPrefix , String entity ) {
        
        if ( entity == null                || 
             entity.startsWith("?")        ||
             entity.startsWith("##PATTERN") 
           )  
           
           return entity ;
        
        if ( entity.contains("/") ) return entity.startsWith(":") ? entity : ":" + entity ;
        if ( entity.contains(":") ) return entity ;
        
        return defaulPrefix == null ? ":" + entity : defaulPrefix + ":" + entity ;
        
    }


    private void checkMetaPatternVariable() {
        
      if( metaPatternVariable == null || metaPatternVariable.isEmpty()) {
         printMessageMetaPatternError("metaPatternVariable") ;
      }       
      if( metaPatternVariable != null && 
          !metaPatternVariable.contains( META_PATTERN_CONTEXT ) ) {
         printMessageMetaPatternErrorMustContains( META_VERIABLE, META_PATTERN_CONTEXT ) ;
      }
        
      if(  metaPatternVariable != null && 
           metaPatternParallel != null && 
          !metaPatternVariable.contains( META_PATTERN_CONTEXT )) {
         printMessageMetaPatternErrorMustContains( META_VERIABLE, META_PATTERN_PARALLEL) ;
      }
    
    }
    
    private void checkMetaPatternContext() {

      if( metaPatternContext == null || metaPatternContext.isEmpty()) {
         printMessageMetaPatternError("metaPatternContext") ;
      }        
         
    }
    
    private void checkMetaPatternParallel() {
               
      if( metaPatternParallel == null || metaPatternParallel.isEmpty()) {
         printMessageMetaPatternError("metaPatternParallel") ;
      }
    }

    public String getCSV_SEPARATOR() {
        return CSV_SEPARATOR;
    }
        
}
