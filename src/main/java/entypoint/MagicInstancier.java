
package entypoint ;

import java.util.Map ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.nio.file.Files ;
import java.nio.file.Paths ;
import java.util.regex.Pattern ;
import java.util.stream.Stream ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.io.Writer ;

/**
 *
 * @author ryahiaoui
 */ 
public class MagicInstancier {
    
  public static void main (String[] args) throws Exception  {

    Map<String, String> variables      = new HashMap<>() ;
    String inTemplateMagicFilterFile   = null            ;
    String outInstanceMagicFilterFile  = null            ;
    int    nbParams                    = 0               ;
    
    for ( int i = 0 ; i < args.length ; i++ ) {
            
        String token = args[i] ;
           
        switch(token)   {
         
        case "-inTemplateMagicFilterFile"  :  inTemplateMagicFilterFile = args[i+1]    ;
                                              nbParams += 2                            ;
                                              break ;
        case "-outInstanceMagicFilterFile" :  outInstanceMagicFilterFile = args[i+1]   ;
                                              nbParams += 2                            ;
                                              break ;
        default                            :  if(args[i].trim().startsWith("?"))       {
                                               variables.put( args[i].trim()           , 
                                                              args[i+1].trim()       ) ;
                                               nbParams += 2                           ;
                                              }
                                              break ;
       }
    }
       
    if( nbParams < 6 ) {
            
       System.out.println ( " missing parameters " ) ;
       return ;
    }
        
    if ( inTemplateMagicFilterFile   == null || inTemplateMagicFilterFile.isEmpty()   ||
         outInstanceMagicFilterFile  == null || outInstanceMagicFilterFile.isEmpty())  {
            
        System.out.println ( " inTemplateMagicFilterFile AND outInstanceMagicFilterFile " + 
                             " can't be NULL or EMPTY " )                                 ;
        System.out.println ("                                                         " ) ;
        return ;
    }
        
    if ( variables.isEmpty() )  {
            
        System.out.println ( " Empty Variables !! " ) ;
        System.out.println ( "                    " ) ;
        return                                        ;
    }
        
    
    long startTime = System.currentTimeMillis()       ;  
    
    String magicContent = new String( Files.readAllBytes ( Paths.get(inTemplateMagicFilterFile))) ;
    
    magicContent        = Stream.of(magicContent.trim().split(";"))
                                .filter( l -> ! l.trim().startsWith("--"))
                                .collect(Collectors.joining(";")) ;
    
    boolean ok = true ; 
    
    for (Map.Entry<String, String> entry : variables.entrySet())                  {
        
        if( ! magicContent.contains(entry.getKey()) ) {
            ok = false ;
        } else {
            magicContent = magicContent.replaceAll( Pattern.quote(entry.getKey()) ,
                                                   entry.getValue())              ;
        }
    }
    
    magicContent = Stream.of(magicContent.trim().split(";"))
                         .filter( l -> ! l.contains("?"))
                         .collect(Collectors.joining(";")) ;
      
    if( Writer.existFile(outInstanceMagicFilterFile)) {
        Writer.deleteFile(outInstanceMagicFilterFile) ;
    }
   
    if( ok ) {
      Writer.checkFile(outInstanceMagicFilterFile)      ;
      Writer.writeTextFile( Arrays.asList(magicContent) , 
                            outInstanceMagicFilterFile) ;
    } else {
      System.out.println ( "                                 " ) ;
      System.out.println ( " Magic Filter doesn't matches !! " ) ;
    }
    
    System.out.println("");
                            
    long executionTime = System.currentTimeMillis() - startTime ;
        
    System.out.println(" Elapsed seconds : "                    + 
                                  executionTime / 1000 + " s" ) ; 
        
    System.out.println(" ")                                     ;
    
  }
 
}
