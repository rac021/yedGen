
package entypoint ;

import java.io.File ;
import java.util.List ;
import java.util.Arrays ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.Processor ;

/**
 *
 * @author ryahiaoui
 */ 

public class Main {
    
  public static enum VERSION { V1 , V3 } ;
  
  static {
     
    String property =  System.getProperty("log") ;
    
    if ( property == null ) {

        String directory = new File ( Main.class.getProtectionDomain()
                                                .getCodeSource()
                                                .getLocation()
                                                .getPath())
                                                .getParent() ;

        System.setProperty("log", directory + "/logs/yedGen/logs.log") ;

    }
  
  }
 
  public static VERSION checkVersion ( String version ) {
  
      try {
         return VERSION.valueOf(version.toUpperCase() )                 ;
      } catch( Exception ex ) {
          System.out.println("       ")                                 ;
          System.out.println(" ERROR VERSION ")                         ;
          System.err.println("  " +  ex       )                         ;
          System.err.println("  --> Retained Version : " + VERSION.V1 ) ;
          System.out.println("                                 " )      ;
          return VERSION.V1                                             ;
      }
  }
  
  
  public static void main (String[] args) throws Exception    {
        
    String directory    = null ,  outFile     = null , ext = null , csv = null ;
    String prf          = null ,  js          = null                           ;
    String classe       = null ;  int column  = -1                             ;
    String prefixFile   = null ,  connecFile  = null , def_prefix  = null      ,
    magicFilterFile     = null , predic_pattern_context = null                 ;
    VERSION version     = null ;
 
    Integer matchColumn = null ; String  _matchWord  = null   ;
      
    boolean includingGraphVariables = false ,  verbose = false ;
       
    int     nbParams                = 0       ;
       
    for ( int i = 0 ; i < args.length ; i++ ) {
            
        String token = args[i] ;
           
        switch(token)   {
         
         case "-d"           :  directory = args[i+1] ; nbParams += 2      ;
                                break ;
         case "-out"         :  outFile   = args[i+1] ; nbParams += 2      ;
                                break ;
         case "-ext"         :  ext       = args[i+1] ; nbParams += 2      ;
                                break ;            
         case "-csv"         :  csv       = args[i+1] ; nbParams += 2      ;
                                break ;    
         case "-prf"         :  prf       = args[i+1] ; nbParams += 2      ;
                                break ;            
         case "-js"          :  js        = args[i+1] ; nbParams += 2      ;
                                break ;            
         case "-class"       :  classe    = args[i+1] ; nbParams += 2      ;
                                break ;    
         case "-column"      :  column   = Integer.parseInt(args [ i+1 ]
                                                .replaceAll(" +", ""))     ; 
                                nbParams += 2                              ; 
                                break ;    
         case "-ig"          :  includingGraphVariables = true             ;  
                                nbParams += 1                              ;
                                break ;            
         case "-verbose"     :  verbose   = true                           ;
                                nbParams += 1                              ;
                                break ;         
         case "-def_prefix"  :  if( ! args[i+1].startsWith("-") ) {
                                  def_prefix = args[i+1] ; nbParams += 2   ;
                                } else {
                                  def_prefix = "" ; nbParams += 1          ;
                                }
                                break ;         
         case "-version"     :  version = checkVersion( args[i+1] )        ;
                                nbParams += 2                              ;
                                break ;         
         case "-connecFile"  :  connecFile = args[i+1] ; nbParams += 2     ;
                                break ;
         case "-prefixFile"  :  prefixFile = args[i+1] ; nbParams += 2     ;
                                break ;
         case "-matchWord"   :  _matchWord = args[i+1] ; nbParams += 2     ;
                                break ;
         case "-matchColumn" :  matchColumn = Integer.parseInt(args [ i+1 ]
                                                 .replaceAll(" +", ""))    ; 
                                nbParams += 2                              ; 
                                break ;
         case "-magicFilter" :  magicFilterFile = args[i+1]                ;
                                nbParams += 2                              ;
                                break ;
                                
         case "-predicat_pattern_context" : 
                                predic_pattern_context = args[i+1]         ;
                                nbParams += 2                              ;
                                break ;                              
       }
    }
       
    if( nbParams < 6 ) {
            
       System.out.println ( " missing parameters " ) ;
       return ;
    }
        
    if( directory == null || directory.isEmpty() || 
        outFile   == null || outFile.isEmpty())   {
            
        System.out.println (" directory or outFile is Empty " )  ;
        return ;
    }
       
    if ( version == VERSION.V3 && 
         ( connecFile ==  null || 
           connecFile.isEmpty() ) ) {
        
        System.out.println ( " You Have to Provide a "
                           + "ConnecFile with the VERSION 3 "
                           + "of OBDA " )                        ;
        return ;
        
    }
    
    if ( prefixFile == null ) {
        
        System.out.println("  ")                                 ;
        System.out.println ( " Warning : No PrefixFile "
                           + " Provided " )                      ;
        System.out.println("  ")                                 ;
        return ;
        
    }
    
    if(ext == null || ext.length() == 0 ) ext = ".graphml"       ;
       
    List<String> wordList = null  ;
    
    if( _matchWord != null && ! _matchWord.isEmpty() )           {
        
        wordList = Arrays.asList( _matchWord.trim()
                                            .replaceAll(" +", " ")
                                            .split(","))
                         .stream()
                         .map( word -> word.trim() )
                         .collect(Collectors.toList()) ;
    }
      
    long startTime = System.currentTimeMillis()                  ;
        
    Processor processor = new Processor ( directory              ,
                                          ext                    ,
                                          prf                    ,
                                          js                     ,
                                          connecFile             ,
                                          prefixFile             ,
                                          def_prefix             ,
                                          magicFilterFile        ,
                                          version                ,
                                          predic_pattern_context ) ;
        
    processor.process ( outFile                  , 
                        csv                      , 
                        includingGraphVariables  , 
                        classe                   ,
                        column                   ,
                        matchColumn              ,
                        wordList               ) ;
        
    long executionTime = System.currentTimeMillis() - startTime ;
        
    System.out.println(" Elapsed seconds : "                    + 
                                  executionTime / 1000 + " s" ) ; 
        
    System.out.println(" ")                                     ;
    
  }
 
}
