
import org.inra.yedodba.ProcessorCsv;

/**
 *
 * @author ryahiaoui
 */
 
public class MainCsv {
    
       public static void main(String[] args) throws Exception {
        
        String directory = "" , outFile = "" , ext = "" , csv = "" ;
       
        int nbParams = 0 ;
       
        for ( int i = 0 ; i < args.length ; i++ ) {
            
            String token = args[i] ;
           
            switch(token)   {
                case "-d"   :  directory = args[i+1] ; nbParams += 2 ;
                                break ;
                case "-out" :  outFile   = args[i+1] ; nbParams += 2 ;
                                break ;
                case "-ext" :  ext       = args[i+1] ; nbParams += 2 ;
                                break ;            
                case "-csv" :  csv       = args[i+1] ; nbParams += 2 ;
                                break ;            
            }
        }
       
        ProcessorCsv processor = new ProcessorCsv() ;

        if( nbParams < 8 ) {
            System.out.println ( " missing parameters " ) ;
            return ;
        }
        
        if(directory.isEmpty() || outFile.isEmpty()) {
            System.out.println (" directory or outFile is Empty " ) ;
            return ;
        }
        
        if(ext.length() == 0 ) ext = ".graphml"                     ;
        
        long startTime = System.currentTimeMillis()                 ;  
        processor.entryProcess(directory, outFile, ext, csv )       ;
        long executionTime = System.currentTimeMillis() - startTime ;
        System.out.println(" Elapsed seconds : " + 
                                      executionTime / 1000 + " s" ) ; 
        System.out.println(" ")                                     ;
    }
}
