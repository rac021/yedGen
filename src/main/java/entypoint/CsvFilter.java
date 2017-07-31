
package entypoint ;

import java.util.List ;
import java.util.Arrays ;
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
        
    String  csvFile     = null ;
    String  outCsv      = null ;
    Integer matchColumn = null ; 
    String  matchWord   = null ;
    String  separator   = null ;
        
    for ( int i = 0 ; i < args.length ; i++ ) {
            
        String token = args[i] ;
           
        switch(token)   {
            
         case "-csv"         :  csvFile                    = args[i+1]      ;
                                break ;    
         case "-outCsv"      :  outCsv                     = args[i+1]      ;
                                break ;
         case "-matchWord"   :  matchWord                  = args[i+1]      ;
                                break ;
         case "-matchColumn" :  matchColumn = Integer.parseInt(args [ i+1 ]
                                                 .replaceAll(" +", ""))     ; 
                                break ;
         case "-separator"   :  separator   = args[i+1]                     ;
                                break ;    
       }
    }
    
    Objects.requireNonNull( csvFile ) ;
    Objects.requireNonNull( outCsv  ) ;
    
    List<String> wordList = null      ;
    
    if( matchWord != null && ! matchWord.isEmpty() )             {
        
        wordList = Arrays.asList(  matchWord.trim()
                                            .replaceAll(" +", " ")
                                            .split(","))
                         .stream()
                         .map( word -> word.trim() )
                         .collect(Collectors.toList()) ;
    }
    
    /* Read File and Filter */

    String        _outCsv      = outCsv         ;
    Integer       _matchColumn = matchColumn    ; 
    List<String>  _wordList    = wordList       ;
    String        _separator   = separator      ;
    
    
    System.out.println("                                    " ) ; 
    System.out.println(" ********************************** " ) ; 
    System.out.println(" - Input CSV File  : " + _outCsv      ) ; 
    System.out.println(" - scv_separator   : " + _separator   ) ; 
    System.out.println(" - matchColumn     : " + _matchColumn ) ; 
    System.out.println(" - wordList        : " + _wordList    ) ; 
    System.out.println(" ********************************** " ) ; 
    System.out.println("                                    " ) ;  
    
     
    List<String> outLines   = new ArrayList<>() ;
    
    outLines.add( Files.lines(Paths.get(csvFile)).findFirst().get())      ;
    
    try ( Stream<String> lines = Files.lines(Paths.get(csvFile)).skip(1)) {
             
        lines.forEach ( line  ->  {
            
                 if(  _matchColumn != null &&   _matchColumn > 0    &&
                         _wordList != null && ! _wordList.isEmpty() ) {
                     
                     if( _wordList.contains( line.split(_separator)[ _matchColumn ]
                                                 .trim()
                                                 .replaceAll(" +", " ")))  {
                        outLines.add(line ) ;
                     }
                 }
        }) ;
        
      } catch (Exception ex) {
          Logger.getLogger(CsvFilter.class.getName()).log(Level.SEVERE, null, ex)   ;
     }
    
    Writer.checkFile( _outCsv )               ;
    Writer.writeTextFile( outLines , _outCsv) ;
  
    if( outLines.size() == 1 )      {
       System.out.println(" -> Empty CSV File Generated    ")  ; 
       System.out.println("     ")  ; 
    }
    else {
       System.out.println(" -> CSV File Generated at : " + _outCsv )  ; 
       System.out.println("     ")  ;  
    }
   }    
  
}
