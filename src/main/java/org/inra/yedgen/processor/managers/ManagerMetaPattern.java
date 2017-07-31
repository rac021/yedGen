
package org.inra.yedgen.processor.managers ;

import java.util.List ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.Comparator;
import java.util.Collections ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import org.inra.yedgen.properties.CsvProperties ;
import static org.inra.yedgen.processor.logs.Messages.* ;
/**
 *
 * @author ryahiaoui
 */
public class ManagerMetaPattern                {
    
    private final Integer metaPatternHash      ;
    private final String  metaPatternVariable  ;
    private final String  metaPatternContext   ;
    private final String  metaPatternParallel  ;
    
    private final CsvProperties  csvProperties ;
    
    private static String       CSV_SEPARATOR            ;
    public  static List<String> INTRA_COLUMN_SEPARATORS  ;
    
    private static final String  META_PATTERN_CONTEXT     = "##META_PATTERN_CONTEXT"  ;
    private static final String  META_PATTERN_PARALLEL    = "##META_PATTERN_PARALLEL" ;
    private static final String  META_VERIABLE            = "?META_VARIABLE"          ;
    private static final String  MATCHER_PATTERN_CONTEXT  = "##PATTERN_CONTEXT"       ;
    private static final String  MATCHER_PATTERN_PARALLEL = "##PATTERN_PARALLEL"      ;
    
    public ManagerMetaPattern( Integer metaPatternHash     ,
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
          
          INTRA_COLUMN_SEPARATORS = this.csvProperties.getConfig().getString("INTRA_COLUMN_SEPARATORS") != null ?
                  Arrays.asList(csvProperties.getConfig().getString("INTRA_COLUMN_SEPARATORS").split("(?!^)")) : 
                  Arrays.asList(",");
                  
                                         
        }
        else {
          CSV_SEPARATOR = "\t" ;
          INTRA_COLUMN_SEPARATORS = Arrays.asList(",") ;
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
        
        List<String> usedColumns = new ArrayList()                         ;
        Comparator<String> comp  = Comparator.comparing ( s -> s.length()) ;
        
        while ( m.find() ) {
          usedColumns.add(m.group().replace(" + ", "").trim()) ;
        }
       
        Collections.sort(usedColumns , comp.reversed() )       ;
         
        for ( String columnPattern : usedColumns )             {

          int num = Integer.parseInt( columnPattern.replaceAll("[^0-9]", "") ) ;
          String columnValue = csvLine.replaceAll(" +", " ")
                                      .split(CSV_SEPARATOR)[num]               ;
                       
          if ( columnPattern.trim().startsWith("'")  )         {
             columnValue = "'" + columnValue ;
             if ( columnPattern.trim().endsWith("'") ) columnValue += "'"      ;
          }
          else if ( columnPattern.trim().startsWith("\"") )    {
             columnValue = "\"" + columnValue ;
             if ( columnPattern.trim().endsWith("\"") ) columnValue += "\""    ;
          }
          
           variable = variable.replaceAll ( columnPattern, columnValue )       ;
        }

        return variable.replace(META_PATTERN_CONTEXT, MATCHER_PATTERN_CONTEXT  )
                       .replace(META_PATTERN_PARALLEL, MATCHER_PATTERN_PARALLEL) ;        
    }

        
    public String generatePatternContext ( String csvLine )                    {
        
        checkMetaPatternContext()                     ;
        
        if ( metaPatternContext == null ) return null ;
        
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

        String nums            = matcher.split("Q_")[1]               ;
        int    startQueryNum   = Integer.parseInt(nums.split("_")[0]) ;
        int    middleQueryNum  = Integer.parseInt(nums.split("_")[1]) ;
        int    endQueryNum     = Integer.parseInt(nums.split("_")[2]) ;
        int loop               = startQueryNum                        ;        
       
        if ( csvLine.split(CSV_SEPARATOR)[variablesColumnNum].trim().length() == 0 ) return null ;        
         
        String variableContextColumn = csvLine.split(CSV_SEPARATOR)[variablesColumnNum].trim()
                                              .replaceAll(" +", "")
                                              .trim() ;
        
        String[] variablesContext =  variableContextColumn.split  ( 
                                        findFirstIntraColumnSeparator ( variableContextColumn ) 
                                     ) ;
       
        Collections.reverse(Arrays.asList(variablesContext)) ;

        if( variablesContext.length == 1 ) loop += 2 ;
        
        for (int i = 0 ; i < variablesContext.length; i++ ) {
            
            String variable = variablesContext[i] ;
            
            pattern += "[ " + matcher.replace( column ,  variable )
                                     .replace(" Q_" + nums, " Q_" + String.valueOf(loop) ) + " ] " ;
            
            if( i == variablesContext.length - 2 || variablesContext.length ==  2 ) {
                loop = endQueryNum ;
            }
            else  if( i == 0 ) {
                loop = middleQueryNum ;
            }
        }
        
        return base + " " + pattern ;
          
    }
    
    public String generatePatternParallel ( String csvLine )  {
        checkMetaPatternParallel() ;
        return metaPatternParallel ;
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
          !metaPatternVariable.contains( META_PATTERN_CONTEXT ))  {
         printMessageMetaPatternErrorMustContains( META_VERIABLE, META_PATTERN_PARALLEL) ;
      }
    
    }
    
    private void checkMetaPatternContext() {

      if( metaPatternContext == null || metaPatternContext.isEmpty())   {
         printMessageMetaPatternError("metaPatternContext") ;
      }        
         
    }
    
    private void checkMetaPatternParallel() {
               
      if( metaPatternParallel == null || metaPatternParallel.isEmpty()) {
         printMessageMetaPatternError("metaPatternParallel") ;
      }
    }

    public String getCSV_SEPARATOR() {
        return CSV_SEPARATOR ;
    }
        
    public static String findFirstIntraColumnSeparator( String value ) {
      return INTRA_COLUMN_SEPARATORS.stream()
                                    .filter( separator -> value.contains(separator))
                                    .findFirst().orElse(" ") ;
    }
    
    /*
    
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
    
    */

}
