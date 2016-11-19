
package org.inra.yedgen.processor.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 *
 * @author ryahiaoui
 */
public class Writer {
    
      
    public List<String> readTextFile(String fileName) throws IOException {
    Path path = Paths.get(fileName);
    return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void writeTextFile(List<String> strLines, String fileName) throws IOException {
    Path path = Paths.get(fileName);
    Files.write(path, strLines, StandardCharsets.UTF_8,  StandardOpenOption.APPEND);
    }
        
    public static void checkFile(String path ) throws IOException {
       
       String directory = path.substring(0 , path.lastIndexOf("/"));
       
       boolean exists = existFile ( path ) ;
       
       if(!exists) {
          checkDirectory(directory) ;
       }
       else {
           deleteFile(path);
       }
       
       createFile(path);
    }

    public static boolean existFile( String path ) {
      
     if( path == null ) return false ;
     Path pat = Paths.get( path )    ;
     return Files.exists( pat, new LinkOption[]{ LinkOption.NOFOLLOW_LINKS}) ;
        
    }
    
    private static void checkDirectory( String directory ) throws IOException {
      
     Path path = Paths.get(directory);
     if(!Files.exists(path, new LinkOption[]{ LinkOption.NOFOLLOW_LINKS}))
       Files.createDirectory(path);
    }
    
    private static void deleteFile( String path ) throws IOException {
      Path pat = Paths.get(path);
      Files.delete(pat);
   }
    
    private static void createFile( String path ) throws IOException {
        File file = new File(path);
        file.createNewFile();
    }
    
    
    
}
