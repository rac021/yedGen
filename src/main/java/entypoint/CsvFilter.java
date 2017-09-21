
package entypoint ;

import java.util.Map ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.Objects ;
import java.nio.file.Files ;
import java.nio.file.Paths ;
import java.util.ArrayList ;
import java.util.stream.Stream ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.io.Writer ;

/**
 *
 * @author ryahiaoui
 */
public class CsvFilter {
    
  public static void main (String[] args) throws Exception    {
        
    String        csvFile        = null ;
    String        outCsv         = null ;
    String        csv_sep        = null ;
    String        words_sep      = null ;
    
    List<String>  intra_csv_sep  = new ArrayList<>() ;
    Map<Integer, String> matcher = new HashMap<>()   ;
    
    for ( int i = 0 ; i < args.length ; i++ ) {
            
        String token = args[i] ;
           
        switch(token)   {
            
         case "-csv"           : csvFile   = args[i+1]                    ;
                                 break ;    
         case "-outCsv"        : outCsv    = args[i+1]                    ;
                                 break ;
         case "-match"         : matcher.put( toInt( args [ i + 1] )      ,
                                              args [ i + 2 ] )            ;
                                 break ;
         case "-csv_sep"       : csv_sep   = args[i+1]                    ;
                                 break ; 
         case "-words_sep"     : words_sep = args[i+1]                    ;
                                 break ; 
         case "-separator"     : intra_csv_sep.addAll (
                                           Arrays.asList (
                                                  args[i+1].split(" ")))  ;
                                 break ; 
       }
    }
    
    Objects.requireNonNull( csvFile ) ;
    Objects.requireNonNull( outCsv  ) ;
    
    String        _outCsv    = outCsv    ;
    String        _csv_sep   = csv_sep   ;
    String        _words_sep = words_sep ;
    
    System.out.println("                                       " ) ; 
    System.out.println(" **********************************    " ) ; 
    System.out.println(" - Input CSV File  : " + _outCsv         ) ; 
    System.out.println(" - csv_separator   : " + _csv_sep        ) ; 
    System.out.println(" - intra_csv_sep   : " + intra_csv_sep   ) ; 
    System.out.println(" - words_sep       : " + words_sep       ) ; 
    System.out.println(" - matcher         : " + matcher         ) ; 
    System.out.println(" **********************************    " ) ; 
    System.out.println("                                       " ) ;  
     
    List<String> outLines = new ArrayList<>()                             ;
    
    outLines.add ( Files.lines(Paths.get(csvFile)).findFirst().get())     ;
    
    try ( Stream<String> lines = Files.lines(Paths.get(csvFile)).skip(1)) {
             
        lines.forEach ( (String line)  ->  {
            
            String treateLine = treateLine ( line          ,
                                             matcher       ,
                                             _csv_sep      ,
                                             _words_sep    ,
                                             intra_csv_sep ) ;                         
            if( treateLine != null ) {
                outLines.add( line ) ;
            }
        } ) ;
       
    } catch (Exception ex) {
       Logger.getLogger(CsvFilter.class.getName()).log( Level.SEVERE, null, ex ) ;
    }
    
    if( outLines.size() > 1 )     {
        Writer.checkFile( _outCsv )                                   ;
        Writer.writeTextFile( outLines , _outCsv)                     ;
       System.out.println(" -> CSV File Generated at : " + _outCsv )  ; 
       System.out.println("     ")                                    ;  
    }
    else {
       System.out.println(" -> No CSV File ile Generated    ")        ; 
       System.out.println("     ")                                    ; 
    }
   }    
  
  private static int toInt( String val )                 {
    return Integer.parseInt( val.replaceAll(" +", " ") ) ;
  } 
  
  private static String getContainedSeparator( String column , 
                                               List<String> separators) {
          return separators.stream()
                           .filter( string -> column.contains(string))
                           .findFirst().orElse(null) ;
  }
  
  private static List split ( String word, String separator ) {
      
       String[] result = Arrays.stream(word.split( separator) )
                               .map(String::trim)
                               .toArray(String[]::new ) ;
       return Arrays.asList(result) ;
  }
  
  private static String treateLine( String line                  ,  
                                    Map<Integer, String> matcher ,
                                    String csv_sep               ,
                                    String word_sep              ,
                                    List<String> intra_csv_sep   )          {
      
      StringBuilder newLine = new StringBuilder()    ;
      
      List<String> columns  = split( line, csv_sep ) ;

      for ( int columnNum = 0 ; columnNum < columns.size() ; columnNum ++ ) {
      
          if( matcher.get(columnNum) != null ) {
              
              List<String> subWords = split(  matcher.get(columnNum) , word_sep ) ;
               
               String column = line.trim()
                                   .replaceAll(" +", " ")
                                   .split( csv_sep )[ columnNum ]
                                   .trim() ;
                        
              String intra_sep = getContainedSeparator( column, intra_csv_sep ) ;
                        
              List<String> subColumns = new ArrayList<>()   ;
              
              if( intra_sep != null ) {
                   subColumns = split( column , intra_sep ) ;
              } else {
                   subColumns.add( column.trim() ) ;
              }
                   
              List<String> okSubColumns = new ArrayList<>() ;
              
              subColumns.forEach( subCol -> {
                     if ( subWords.contains(subCol )) {                                    
                         okSubColumns.add(subCol) ;
                     } else {
                     }
              } ) ;                  
              
              if( !okSubColumns.isEmpty() ) {
                 newLine.append( okSubColumns.stream()
                                             .collect( Collectors
                                             .joining( intra_sep != null ? 
                                                       intra_sep : "" )  )
                               ) ;
                 
                 if( newLine.toString().split(csv_sep).length < columns.size() )
                         newLine.append(csv_sep) ;
              }
              
          } else {
              newLine.append(columns.get(columnNum)) ;
               if( newLine.toString().split(csv_sep).length < columns.size() )
                   newLine.append(csv_sep) ;
          }
        
      }
      
      int oldL = line.split(csv_sep).length               ;
      int newL = newLine.toString().split(csv_sep).length ;
          
      if( oldL == newL ) return newLine.toString()        ;
      
      return null ;
  }
}

