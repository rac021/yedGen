
package org.inra.yedgen.processor.errors;

/**
 *
 * @author ryahiaoui
 */
public class MessageErrors {
    
    
     public static void printMessageErrorContext(String id_pattern) {
        System.err.println("")   ;
        System.err.println(" Error // No Pattern context found with ID : " + id_pattern ) ;
        System.err.println(" This will be considered as an Empty_Pattern_Context for the rest ") ;
        System.err.println("")   ;
    }
     
    public static void printMessageErrorContext(String id_pattern, String variableName ) {
        System.err.println("")   ;
        System.err.println(" Error // No Pattern context found with ID : " + id_pattern ) ;
        System.err.println(" The variable : " + variableName + " -> will be generated with Empty_Pattern_Context " ) ;
        System.err.println("")   ;
    }
    
    public static void printMessageMetaPatternError(String idPattern ) {
        System.err.println("")   ;
        System.err.println(" Error // MetaPattern " + idPattern +" not found !! ") ;
        System.err.println("")   ;
    }
    
    public static void printMessageMetaPatternErrorMustContain( String pattenId_1, String pattenId_2 ) {
        System.err.println("")   ;
        System.err.println(" Error // MetaPattern // " + pattenId_1 + " Must contain --> " + pattenId_2 ) ;
        System.err.println("")   ;
    }
     
    public static void printMessageErrorParallel ( String id_pattern ) {
       System.err.println("")   ;
       System.err.println(" Error // No Pattern Prallel found with ID : " + id_pattern ) ;
       System.err.println("")   ;
    }
    
    public static void printMessageMetaPatternErrorMustContains( String pattern_1 , String pattern_2 ) {
       System.err.println("")   ;
       System.err.println(" Error // "+ pattern_1 + " Must Contains  " + pattern_2 ) ;
       System.err.println("")   ;
     }
    
    public static void printMessageMetaPatternsNull() {
       System.err.println("")   ;
       System.err.println(" --> Error // MetaPatterns are null // CSV Generation abort ") ;
       System.err.println("")   ;
     }

    public static void printMessageStartProcessVariableGraphGeneration() {
       System.err.println("")   ;
       System.err.println(" ************************** ") ;
       System.err.println(" Process Graph Variables .. ") ;
       System.err.println(" **************************** ") ;
       System.err.println("")   ;
     }
    
    public static void printMessageStartProcessCsvVariableGeneration() {
       System.err.println("")   ;
       System.err.println(" ************************ ") ;
       System.err.println(" Process CSV Variables .. ") ;
       System.err.println(" ************************ ") ;
       System.err.println("")   ;
     }

    public static void printMessageInfoGeneratedVariable( String variableName, String fileName ) {
       System.err.println("")   ;
       System.err.println(" Info // Variable : " + variableName + " Generated in Obda file -->  "
                                                 + fileName )                                   ;
       System.err.println("")   ;
    }
    
    public static void printErrorNumQueryNotFound( Integer numQuery ) {
       System.out.println("")   ;
       System.out.println(" NumQuery [ "+numQuery+" ] not found in numUris Map !! " ) ;
       System.out.println("")   ;
    }
    
    public static void printErrorMatcher(String variableName, String subLine ) {
        
       System.out.println(" Error Matcher // Variable [ " + variableName   + 
                          " ] doesn't contains Matcher for : " + subLine ) ; 
    }
    
     public static void printMessageErrorCSV( String csvFile ) {
        System.out.println (" -> Error CSV File not found at path : " + csvFile ) ;
    }
    
}
