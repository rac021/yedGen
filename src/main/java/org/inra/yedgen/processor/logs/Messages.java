
package org.inra.yedgen.processor.logs ;

/**
 *
 * @author ryahiaoui
 */
public class Messages {
    
     public static void printMessageErrorContext(String id_pattern) {
        System.err.println("")                                                                   ;
        System.err.println(" Error // No Pattern context found with ID : " + id_pattern )        ;
        System.err.println(" This will be considered as an Empty_Pattern_Context for the rest ") ;
        System.err.println("")                                                                   ;
    }
     
    public static void printMessageErrorContext(String id_pattern, String variableName ) {
        System.err.println("")                                                                                       ;
        System.err.println(" Error // No Pattern context found with ID : " + id_pattern )                            ;
        System.err.println(" The variable : " + variableName + " -> will be generated with Empty_Pattern_Context " ) ;
        System.err.println("")                                                                                       ;
    }
    
    public static void printMessageMetaPatternError(String idPattern )             {
        System.err.println("")                                                     ;
        System.err.println(" Error // MetaPattern " + idPattern +" not found !! ") ;
        System.err.println("")                                                     ;
    }
    
    public static void printMessageMetaPatternErrorMustContain( String pattenId_1, String pattenId_2 ) {
        System.err.println("")                                                                            ;
        System.err.println(" Error // MetaPattern // " + pattenId_1 + " Must contain --> " + pattenId_2 ) ;
        System.err.println("")                                                                            ;
    }
     
    public static void printMessageErrorParallel ( String id_pattern ) {
       System.err.println("")                                                            ;
       System.err.println(" Error // No Pattern Prallel found with ID : " + id_pattern ) ;
       System.err.println("")                                                            ;
    }
    
    public static void printMessageMetaPatternErrorMustContains( String pattern_1 , String pattern_2 ) {
       System.err.println("")                                                        ;
       System.err.println(" Error // "+ pattern_1 + " Must Contains  " + pattern_2 ) ;
       System.err.println("")                                                        ;
     }
    
    public static void printMessageMetaPatternsNull() {
       System.err.println("")                                                                           ;
       System.err.println(" --> Warning // MetaPatterns Expressions are null // CSV Generation abort ") ;
       System.err.println("")                                                                           ;
    }

    public static void printMessageStartProcessVariableGraphGeneration() {
       System.err.println("                             " ) ;
       System.err.println(" --------------------------- " ) ;
       System.err.println(" Process Graph Variables ... " ) ;
       System.err.println(" --------------------------- " ) ;
       System.err.println("                             " ) ;
     }
    
    public static void printMessageStartProcessCsvVariableGeneration( String path ) {
       System.err.println("                          " ) ;
       System.err.println(" ------------------------ " ) ;
       System.err.println(" Process CSV Variables .. " ) ;
       System.err.println(" Path : " +  path           ) ;
       System.err.println(" ------------------------ " ) ;
       System.err.println("                          " ) ;
     }

    public static void printMessageInfoGeneratedVariable( String variableName, String fileName ) {
       System.err.println( " Info // Variable : [ " + variableName + " ] Generated in Obda file "
                           + "-->  "   + fileName )                                             ;
       System.err.println( "                                                                " ) ;
    }
    
    public static void printErrorNumQueryNotFound( Integer numQuery ) {
       System.out.println("")                                                         ;
       System.out.println(" NumQuery [ "+numQuery+" ] not found in numUris Map !! " ) ;
       System.out.println("")                                                         ;
    }
    
    public static void printErrorMatcherOnSubject( Integer code ,String variableName, String token )  {
        
       System.out.println( " ==> Error Matcher // Variable [ "  + variableName   + 
                           " ] doesn't contains Matcher for the URI ( " + code + " ) \n     "
                           + "URI ( " + code + " ) : [ " 
                           + token + " ] \n ") ; 
    }
    
    public static void printErrorNoURIProvided( Integer code ,String variableName  )  {
        
       System.out.println( " ==> Error URI // Variable [ "  + variableName   + 
                           " ] : NO URI provided for the Node ( " + code + " ) \n  ") ; 
    }
    
    public static void printErrorMatcherOnObject( Integer code ,String variableName, String token ) {
        
       System.out.println( " ==> Error Matcher // Variable [ "  + variableName + 
                           " ] contains a Node with the URI ( " + code + " ) \n     Which "
                           + " is probably linked to another Node that doesn't contains Matcher. \n "
                           + "    Token : [ " + token + " ] \n ") ; 
    }
    
    public static void printErrorMatcherOnQuery(Integer code, String variableName, String token) {
             
       System.out.println( " ==> Error Matcher // Variable [ "  + variableName + 
                           " ] contains on the Node ( " + code + " ) \n     a "
                           + " Query that doesn't have Matcher. \n "
                           + "    Token : [ " + token + " ] \n ") ; 
    }
     
    public static void printMessageErrorCSV( String csvFile ) {
      System.out.println (" -> Error CSV File not found at path : " + csvFile ) ;
    }
    
    public static void printMessageExtractGraph( String directory ) {
      System.out.println ( " " ) ;  printSeparator() ;     
      System.out.println(" Extracting Graph from Directory  : " + directory + "  ... " ) ;
    }
    
    public static void printMessageProcessingGraphFile( String path ) {
       System.out.println(" Processing Graph file            : " +path + " ... "      ) ;
    }
    
    public static void printMessageFilesNotFoundExtentsion( String directory , String extension ) {
      System.out.println ( " No File with extension ['" + extension + "' ]  found "  +
                           " in the Directory  : " + directory )                     ;
      System.out.println ( "                                                     " ) ;  
    }
    
    public static void printLoadingFile(String type , String name) {
      System.out.println (" -> Loading "+ type + " : " + name )     ;
    }
    
    public static void printWarningLoadingFile(String type , String name ) {
      System.out.println (" -> Warning : " + type + " [ "  + 
                            name + " ] doesn't exist " )   ;
    }
    
    public static void printSeparator() {
      System.out.println( " ------------------------------------------------------------ " ) ;
    }
    
    public static void printExtractionError() {
      System.err.println(" ")                                                ;
      System.err.println(" Oops something went wrong during Extraction !! ") ;
      System.err.println(" ")                                                ;
    }
    
    public static void printNotFoundLabelError()   {
      System.out.println( " Label not Found !! " ) ;
    }
    
    public static void printMessage( String message ) {
      System.out.println( " \n " + message   )        ;
    }
    
    public static void printMessageError( String message ) {
      System.out.println( " \n " + message + " \n " )      ;
    }
    
        
    public static void printInfoCSVTreatment( String csvFile, int nbLines , String classe, int column ) {
      System.out.println( " " ) ;
      System.out.println( " --> Info CSV : " + nbLines + " "
                          + "line(s) treated for the class [ " 
                          + (classe == null ? "*" : classe )
                          + " ] in the CSV : " + csvFile 
                          + " //  Discriminator Column : " + column  ) ;
      System.out.println( " " ) ;
    }
    
    public static void printInfoCSVEmptyTreatment( String csvFile, String classe, int column ) {
      System.out.println( " " ) ;
      System.out.println( " --> Info CSV : NO LINE Treated " 
                          + " for the class [ " + classe + " ] in the CSV : " 
                          + csvFile + " //  Discriminator Column : " + column  ) ;
      System.out.println( " " ) ;
    }
    
    public static void printEmptyDeclarationWarning() {
      System.err.println(" ")                                                     ;
      System.err.println(" Note : No PrefixDeclaration Detected for OBDA files ") ;
      System.err.println(" ")                                                     ;
    }
 
}
