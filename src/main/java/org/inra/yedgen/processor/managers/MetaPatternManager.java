
package org.inra.yedgen.processor.managers;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.inra.yedgen.properties.CsvProperties;
import static org.inra.yedgen.processor.errors.MessageErrors.* ;
/**
 *
 * @author ryahiaoui
 */
public class MetaPatternManager {
    
    private final Integer metaPatternHash      ;
    private final String  metaPatternVariable  ;
    private final String  metaPatternContext   ;
    private final String  metaPatternParallel  ;
    
    private final CsvProperties  csvProperties ;
    
    private final String SEPARATOR  = "\t"     ;
    
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
        
        String variable = metaPatternVariable ;
         
        Pattern p = Pattern.compile("COLUMN_+\\w+") ;
        Matcher m = p.matcher(metaPatternVariable ) ;

        while (m.find()) {
                   
          String params    = m.group()
                              .replace(" + ", "")
                              .trim()           ;
          String[] nums    = params.split("_")  ;
            
          List<String> tmp = new ArrayList<>();
            
          for( int i = 1 ; i < nums.length ; i ++ ) {
              
             int num = Integer.parseInt(nums[i])    ;
             tmp.add(csvLine.split(SEPARATOR)[num]) ;
          }
            
          String resScript = csvProperties.process( params , 
                                                    tmp.toArray(new String[tmp.size()]) ) ;
          variable = variable.replaceAll ( params, resScript ) ;

        }

        return variable.replace(META_PATTERN_CONTEXT, MATCHER_PATTERN_CONTEXT  )
                       .replace(META_PATTERN_PARALLEL, MATCHER_PATTERN_PARALLEL);
    }

        
    public String generatePatternContext ( String csvLine ) {
        
        checkMetaPatternContext()                    ;
        
        if( metaPatternContext == null ) return null ;
        
        String pattern = "" ;
        String base    =  metaPatternContext.split(Pattern.quote("["))[0].trim() ;
        String matcher =  metaPatternContext.split(Pattern.quote("["))[1].trim()
                                            .replace("]", "")
                                            .trim() ;
        
        int variablesColumnNum = Integer.parseInt( matcher.split(" ")[0].split("_COLUMN_")[1]) ;
        
        String nums            = matcher.split("Q_")[1] ;
        int startQueryNum      = Integer.parseInt(nums.split("_")[0])         ;
        int middleQueryNum     = Integer.parseInt(nums.split("_")[1])         ;
        int endQueryNum        = Integer.parseInt(nums.split("_")[2])         ;
        
        int loop                  = startQueryNum ;
        
        if(csvLine.split(SEPARATOR)[variablesColumnNum].trim().length() == 0 ) return null ;
        
        String[] variablesContext = csvLine.split(SEPARATOR)[variablesColumnNum].replaceAll(" +", "").trim().split(",") ;
       
        Collections.reverse(Arrays.asList(variablesContext)) ;

        if( variablesContext.length == 1 ) loop += 2 ;
        
        for (int i = 0 ; i < variablesContext.length; i++ ) {
            
            String variable = variablesContext[i] ;
            
            pattern += "[ " + matcher.replace( "COLUMN_" + variablesColumnNum , validatePrefix( null, variable) )
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
        
}
