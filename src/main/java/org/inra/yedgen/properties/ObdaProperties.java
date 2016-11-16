
package org.inra.yedgen.properties;

/**
 *
 * @author ryahiaoui
 */
public class ObdaProperties {
    
    public static       String  PREFIX_PREDICAT   =  "oboe-coreX"             ;
    public static final String  PREFIXDECLARATION = "[PrefixDeclaration]"     ;
    public static final String  PREF              =  "?pref		?uri" ;

    public static final String MAPPING_COLLECTION_BEGIN   = "[MappingDeclaration] @collection [[" ;

    public static final String MAPPING_COLLECTION_PATTERN =  "mappingId	?id\n"              +
                                                             "target		?target\n"  +
                                                             "source		?source"    ;
        
    public static String SOURCE_DEC_STRING = "[SourceDeclaration]\n"              +
                                              "sourceUri	?sourceUri\n"     +
                                              "connectionUrl	?connectionUrl\n" +
                                              "username	?username\n"              +
                                              "password	?password\n"              +
                                              "driverClass	?driverClass"     ;
    
    public static final String MAPPING_COLLECTION_END     = "]]" ;
    
    public final String SEPARATOR                         = "\t" ;
    
}
