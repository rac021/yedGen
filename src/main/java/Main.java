
import org.inra.yedodba.Processor;

/**
 *
 * @author ryahiaoui
 */
 
public class Main {
    
       public static void main(String[] args) throws Exception {
        
        String directory = "" , outFile = "" , ext = ""  ;
       
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
            }
        }
       
        Processor processor = new Processor() ;

        if( nbParams < 6 ) {
            System.out.println ( " missing parameters " ) ;
            return ;
        }
        
        if(directory.isEmpty() || outFile.isEmpty()) {
            System.out.println (" directory or outFile is Empty " ) ;
            return ;
        }
        
        if(ext.length() == 0 ) ext = ".graphml" ;
        
        processor.entryProcess(directory, outFile, ext ) ;
    }
}
