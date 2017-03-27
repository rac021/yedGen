
package entypoint ;

import java.io.File ;
import org.inra.yedgen.processor.Processor ;

/**
 *
 * @author ryahiaoui
 */
 
public class Main {
    
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
 
  public static void main (String[] args) throws Exception    {
        
    String directory    = null ,  outFile     = null , ext = null , csv = null ;
    String prf          = null ,  js          = null                           ;
    String classe       = null ;  int column  = -1                             ;
    String prefixFile   = null ,  connecFile  = null , def_prefix  = null      ;
    
    boolean includingGraphVariables = false ,  verbose = false ;
       
    int     nbParams                = 0       ;
       
    for ( int i = 0 ; i < args.length ; i++ ) {
            
       String token = args[i] ;
           
        switch(token)   {
         
         case "-d"          :  directory = args[i+1] ; nbParams += 2  ;
                               break ;
         case "-out"        :  outFile   = args[i+1] ; nbParams += 2  ;
                               break ;
         case "-ext"        :  ext       = args[i+1] ; nbParams += 2  ;
                               break ;            
         case "-csv"        :  csv       = args[i+1] ; nbParams += 2  ;
                               break ;    
         case "-prf"        :  prf       = args[i+1] ; nbParams += 2  ;
                               break ;            
         case "-js"         :  js        = args[i+1] ; nbParams += 2  ;
                               break ;            
         case "-class"      :  classe    = args[i+1] ; nbParams += 2  ;
                               break ;    
         case "-column"     :  column = Integer.parseInt(args [ i+1 ]
                                               .replaceAll(" +", "")) ; 
                               nbParams += 2                          ; 
                               break ;    
         case "-ig"         :  includingGraphVariables = true         ;  
                               nbParams += 1                          ;
                               break ;            
         case "-v"          :  verbose   = true                       ;
                               nbParams += 1                          ;
                               break ;         
         case "-def_prefix" :  def_prefix = args[i+1] ; nbParams += 2 ;                            
                               break ;         
         case "-connecFile" :  connecFile = args[i+1] ; nbParams += 2 ;
                               break ;
         case "-prefixFile" :  prefixFile = args[i+1] ; nbParams += 2 ;
                               break ;
       }
    }
       
    if( nbParams < 6 ) {
            
       System.out.println ( " missing parameters " ) ;
       return ;
    }
        
    if( directory == null || directory.isEmpty() || 
        outFile   == null || outFile.isEmpty())   {
            
        System.out.println (" directory or outFile is Empty " ) ;
        return ;
    }
        
    if(ext == null || ext.length() == 0 ) ext = ".graphml"     ;
        
      long startTime = System.currentTimeMillis()              ;  
        
      Processor processor = new Processor ( directory          ,
                                            ext                ,
                                            prf                ,
                                            js                 ,
                                            connecFile         ,
                                            prefixFile         ,
                                            def_prefix )       ;
        
      processor.process ( outFile                  , 
                          csv                      , 
                          includingGraphVariables  , 
                          classe                   ,
                          column                   ,
                          verbose )  ;
        
      long executionTime = System.currentTimeMillis() - startTime ;
        
      System.out.println(" Elapsed seconds : "                    + 
                                    executionTime / 1000 + " s" ) ; 
        
      System.out.println(" ")                                     ;
    }
 
}
