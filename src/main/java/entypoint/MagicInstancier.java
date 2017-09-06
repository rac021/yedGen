
package entypoint ;

import java.util.Map ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.ArrayList;
import java.nio.file.Paths ;
import java.nio.file.Files ;
import java.util.regex.Pattern ;
import java.util.stream.Stream ;
import java.util.stream.Collectors ;
import org.inra.yedgen.processor.io.Writer ;

/**
 *
 * @author ryahiaoui
 * 
  inMagicFilterFile Exemple :
     ?years { year & 1, 10  } { annee & 20 }                 STEP 1 ;
     ( ?sites )     { site_01_id & 1  } { sites_02_id & 2  } PEEK 1 ;
      -- ( ?sites ) { sites_01 & 1  }   { sites_02 & 2     } PEEK 3 ;
        
  Invocation :   
   -inTemplateMagicFilterFile      "magicFilter.txt"
   -outInstanceMagicFilterFile     "magicFilter_Instance.txt" 
   -filters "?years := 1981_2010 ; ?sites := 'site_1', 'site_2'"
 * 
 */ 

public class MagicInstancier {

  static Map<String, String> variables  = new HashMap<>()   ;
  private static final String OPERATOR  = ":="              ;
  private static final String SPLITER   = ";"               ;
  
  public static void main (String[] args) throws Exception  {

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
        case "-filters"                    :  treatParams( args[ i + 1] )              ;
                                              nbParams += 2                            ;
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
    
    magicContent        = Stream.of( magicContent.trim().split("\n") )
                                .filter( l -> ! l.trim().startsWith ("--") )
                                .filter( l -> ! l.trim().isEmpty()  )
                                .collect(Collectors.joining( "\n" ) ) ;
    
    boolean ok          = true              ; 
    List<String> errors = new ArrayList<>() ;
    
    for (Map.Entry<String, String> entry : variables.entrySet()) {
        
        if( ! magicContent.contains( entry.getKey() + " "  ) &
            ! magicContent.contains( entry.getKey() + "\n" ) )   {
            ok = false                  ;
            errors.add(entry.getKey() ) ;
        } else {
            magicContent = magicContent.replaceAll( Pattern.quote(entry.getKey()) ,
                                                    entry.getValue() )            ;
        }
    }
    
    magicContent = Stream.of( magicContent.trim().split("\n") )
                         .filter( l -> l.trim().startsWith("#") 
                                       || ! l.contains("?")   )
                         .collect(Collectors.joining("\n") )  ;
      
    if( Writer.existFile(outInstanceMagicFilterFile)) {
        Writer.deleteFile(outInstanceMagicFilterFile) ;
    }
   
    if( ok ) {
      Writer.checkFile(outInstanceMagicFilterFile)          ;
      Writer.writeTextFile( Arrays.asList(magicContent)     ,
                            outInstanceMagicFilterFile)     ;
      System.out.println ( "                            " ) ;
      System.out.println ( " Instance Magic Filter Path : " + 
                             outInstanceMagicFilterFile   ) ;
      System.out.println ( "                            " ) ;
        
    } else {
      System.out.println ( "                                 " ) ;
      System.out.println ( " Magic Filter doesn't matches !! " ) ;
      System.out.println ( " Error Variables :               " ) ;
      System.out.println ( " --> " + errors                    ) ;
    }
    
    System.out.println("");
                            
    long executionTime = System.currentTimeMillis() - startTime ;
        
    System.out.println(" Elapsed seconds : "                    + 
                                  executionTime / 1000 + " s" ) ; 
        
    System.out.println(" ")                                     ;
    
  }
 
  private static void treatParams(String vars) {
     Arrays.asList(vars.split(SPLITER))
           .stream()
           .forEach( line -> {
               variables.put( line.split(OPERATOR)[0].trim().replaceAll(" +", " ") ,
                              line.split(OPERATOR)[1].trim().replaceAll(" +", " ") ) ;
           }) ;
  }
  
}
