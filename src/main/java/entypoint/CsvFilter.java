
package entypoint ;

import java.util.Map ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.Objects ;
import java.io.IOException ;
import java.nio.file.Files ;
import java.nio.file.Paths ;
import java.util.ArrayList ;
import java.util.stream.Stream ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.io.Writer ;
import java.util.concurrent.atomic.AtomicInteger ;

/**
 *
 * @author ryahiaoui
 */

public class CsvFilter {
    
  public static void main (String[] args) throws Exception    {
        
    String    csvFile       = null ;
    String    outCsv        = null ;
    String    csv_sep       = null ;
    String    match_sep     = null ;
    
    String    outFilterdMirrorCsv = null ;
    String    mirrorCsv           = null ;
    
    List<String>  intra_csv_sep  = new ArrayList<>() ;
    Map<Integer, String> matcher = new HashMap<>()   ;
    
    for ( int i = 0 ; i < args.length ; i++ )        {
            
        String token = args[i] ;
           
        switch(token)   {
            
         case "-csv"                     : csvFile    = args[i+1]                   ;
                                           break ;    
         case "-mirror_csv"              : mirrorCsv  = args[i+1]                   ;
                                           break ;    
         case "-outCsv"                  : outCsv     = args[i+1]                   ;
                                           break ;
         case "-outFilterdMirrorCsv"     : outFilterdMirrorCsv  = args[i+1]         ;
                                           break ; 
         case "-match"                   : matcher.put( toInt( args [ i + 1] )      ,
                                                        args [ i + 2 ] )            ;
                                           break ;
         case "-csv_sep"                 : csv_sep   = args[i+1]                    ;
                                           break ; 
         case "-match_sep"               : match_sep = args[i+1]                    ;
                                           break ; 
         case "-intra_sep"               : intra_csv_sep.addAll (
                                                     Arrays.asList (
                                                            args[i+1].split(" ")))  ;
                                           break ; 
       
       }
    }
    
    Objects.requireNonNull( csvFile ) ;
    Objects.requireNonNull( outCsv  ) ;
     
    if ( ( outFilterdMirrorCsv == null && mirrorCsv != null  )          ||
         ( outFilterdMirrorCsv != null && mirrorCsv == null  ) )         {
       System.out.println( "                                       " )   ;
       System.out.println( " -mirrorCsv AND -outFilterdMirrorCsv  MUST   "
                           + " be null [ OR NOT ] at the same time ! " ) ;
       System.out.println( "                                       "   ) ;
       System.exit( 2 )                                                  ;
    }
    
    String  _outCsv         = outCsv      ;
    String  _csv_sep        = csv_sep     ;
    String  _match_sep      = match_sep   ;
    String  _mirrorCsv      = mirrorCsv   ;
    
    System.out.println("                                       " ) ; 
    System.out.println(" **********************************    " ) ; 
    System.out.println(" - Input CSV File  : " + _outCsv         ) ; 
    System.out.println(" - csv_separator   : " + _csv_sep        ) ; 
    System.out.println(" - intra_csv_sep   : " + intra_csv_sep   ) ; 
    System.out.println(" - match_sep       : " + match_sep       ) ; 
    System.out.println(" - matcher         : " + matcher         ) ; 
    System.out.println(" **********************************    " ) ; 
    System.out.println("                                       " ) ;  
     
    List<String> outLines = new ArrayList<>()                             ;
    
    outLines.add ( Files.lines(Paths.get(csvFile)).findFirst().get())     ;
    
    List<String> mirrorLines = new ArrayList<>() ;
    
    if( outFilterdMirrorCsv != null ) {
       mirrorLines.add(Files.lines ( Paths.get( mirrorCsv ) )
                            .findFirst().get() )                          ;
    }
   
    try ( Stream<String> lines = Files.lines(Paths.get(csvFile)).skip(1)) {
         
        AtomicInteger numLine  = new AtomicInteger( 1 ) ;
        
        lines.forEach ( (String line)  ->                  {
            
            List<String> treateLine = treateLine ( line          ,
                                                   matcher       ,
                                                   _csv_sep      ,
                                                   _match_sep    ,
                                                   intra_csv_sep ,
                                                   readLine( _mirrorCsv, numLine ) ) ;
            
            if( treateLine != null )          {
                
                outLines.add( treateLine.get(0) )  ;
                mirrorLines.add( treateLine.get(1) )  ;
            }
            
            numLine.getAndIncrement() ;
            
        } ) ;
       
    } catch (Exception ex) {
       Logger.getLogger(CsvFilter.class.getName()).log( Level.SEVERE, null, ex ) ;
    }
    
    if( outLines.size() > 1 )     {
        
       Writer.checkFile( _outCsv )                                     ;
       Writer.writeTextFile( outLines , _outCsv)                       ;
       System.out.println(" -> CSV File Generated at : " + _outCsv )   ;
       
       if ( outFilterdMirrorCsv != null ) {
           Writer.checkFile( outFilterdMirrorCsv )                     ;
           Writer.writeTextFile( mirrorLines , outFilterdMirrorCsv )   ;
           System.out.println("     ")                                                 ;
           System.out.println(" -> Mirror CSV Generated at : " + outFilterdMirrorCsv ) ;
       }
       
       System.out.println("     ")                                     ;  
    }
    else {
       System.out.println(" -> No CSV File ile Generated    ")         ; 
       System.out.println("     ")                                     ; 
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
      
       if( word == null ) return null ;
      
       String[] result = Arrays.stream(word.split( separator) )
                               .map(String::trim)
                               .toArray(String[]::new ) ;
       return Arrays.asList(result) ;
  }
  
  private static List<String> treateLine( String line                  ,  
                                          Map<Integer, String> matcher ,
                                          String csv_sep               ,
                                          String match_sep             ,
                                          List<String> intra_csv_sep   ,
                                          String csvMirrorLine )       {
      
      StringBuilder newLine       = new StringBuilder()    ;
      StringBuilder mirrorNewLine = new StringBuilder()    ;
      
      List<String> columns        = split( line, csv_sep ) ;
      List<String> mirrorColumns  = split( csvMirrorLine, csv_sep ) ;

      for ( int columnNum = 0 ; columnNum < columns.size() ; columnNum ++ ) {
      
          if( matcher.get(columnNum) != null ) {
              
              List<String> subWords = split(  matcher.get(columnNum) , match_sep ) ;
               
               String column = line.trim()
                                   .replaceAll(" +", " ")
                                   .split( csv_sep )[ columnNum ]
                                   .trim() ;
               
               String mirrorColumn =  null ;
               
               if( csvMirrorLine != null ) {
                 mirrorColumn = csvMirrorLine.trim()
                                             .replaceAll(" +", " ")
                                             .split( csv_sep )[ columnNum ]
                                             .trim() ;
              }
              
               String intra_sep = getContainedSeparator( column, intra_csv_sep ) ;
                        
              List<String> subColumns       = new ArrayList<>() ;
              List<String> mirrorSubColumns = new ArrayList<>() ;
              
              if( intra_sep != null ) {
                   subColumns = split( column , intra_sep ) ;
                   mirrorSubColumns = split( mirrorColumn , intra_sep ) ;
              } else {
                   subColumns.add( column.trim() ) ;
                   if( csvMirrorLine != null ) {
                      mirrorSubColumns.add( mirrorColumn.trim() ) ;
                   }
              }
                   
              List<String> okSubColumns       = new ArrayList<>() ;
              List<String> okMirrorSubColumns = new ArrayList<>() ;
              
              for ( int i = 0; i < subColumns.size() ; i++ )    {

                if ( subWords.contains(subColumns.get(i) ) )    {
                    okSubColumns.add(subColumns.get(i) )                ;
                    if( csvMirrorLine != null ) {
                       okMirrorSubColumns.add(mirrorSubColumns.get(i) ) ;
                    }
                } else {
                }
              }              
              
              if( !okSubColumns.isEmpty() ) {
                  
                 newLine.append( okSubColumns.stream()
                                             .collect( Collectors
                                             .joining( intra_sep != null ? 
                                                       intra_sep : "" )  )
                 ) ;
                 
                 if( csvMirrorLine != null ) {
                    mirrorNewLine.append( okMirrorSubColumns.stream()
                                                            .collect( Collectors
                                                            .joining( intra_sep != null ?
                                                            intra_sep : "" )  )
                    ) ;
                 }
                 
                 if( newLine.toString().split(csv_sep).length < columns.size() ) {
                     newLine.append(csv_sep)          ;
                     if( csvMirrorLine != null )      {
                        mirrorNewLine.append(csv_sep) ;
                     }
                 }
              }
              
          } else {
              
              newLine.append(columns.get(columnNum))                ;
              if( csvMirrorLine != null ) {
                 mirrorNewLine.append(mirrorColumns.get(columnNum)) ;
              }
             
              if( newLine.toString().split(csv_sep).length < columns.size() ) {
                 newLine.append(csv_sep)          ;
                 if( csvMirrorLine != null )      {
                    mirrorNewLine.append(csv_sep) ;
                 }
              }
          }
        
      }
      
      int oldL = line.split(csv_sep).length               ;
      int newL = newLine.toString().split(csv_sep).length ;
          
      if( oldL == newL ) return Arrays.asList( newLine.toString() , 
                                               mirrorNewLine.toString() ) ;
      
      return null ;
  }
  
  
  private static String readLine( String mirrorCsv, AtomicInteger numLine ) {
      
    if( mirrorCsv == null ) return null ;
    
    try (Stream<String> lines = Files.lines(Paths.get(mirrorCsv))) {
       return lines.skip(numLine.get()).findFirst().get() ;
    } catch (IOException ex) {
      Logger.getLogger(CsvFilter.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null ;
  
  }
  
}
