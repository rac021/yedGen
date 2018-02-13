
package org.inra.yedgen.sql ;

import java.util.List ;
import java.io.StringReader ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import net.sf.jsqlparser.schema.Column ;
import net.sf.jsqlparser.expression.Function ;
import net.sf.jsqlparser.JSQLParserException ;
import net.sf.jsqlparser.expression.Expression ;
import net.sf.jsqlparser.parser.CCJSqlParserUtil ;
import net.sf.jsqlparser.statement.select.Select ;
import net.sf.jsqlparser.parser.CCJSqlParserManager ;
import net.sf.jsqlparser.statement.select.PlainSelect ;
import net.sf.jsqlparser.statement.select.SelectExpressionItem ;

/**
 *
 * @author ryahiaoui
 */

public class SqlAnalyzer {
 
       
      final static Pattern  PATTERN_KEY_VALUES  =  Pattern.compile( "(^\t| | \n)AS *(\" *)?\\w+.(\" *| *\n|\t)?" , 
                                                   Pattern.DOTALL | Pattern.CASE_INSENSITIVE )   ;
    
      public static String treatQuery( String sqlQuery , String whereFilter ) {
           
        if( whereFilter == null || whereFilter.isEmpty() ) return sqlQuery ;
          
        try {

            CCJSqlParserManager parserManager = new CCJSqlParserManager()              ;
            Select select   = (Select) parserManager.parse(new StringReader(sqlQuery)) ;
            PlainSelect ps  = (PlainSelect) select.getSelectBody()                     ;
            
            Expression wher = ps.getWhere()                                            ;

            if( wher != null ) {
                return ps.toString().replace( wher.toString(), 
                                         wher.toString() + " AND " + whereFilter )
                                    .replaceAll(" + ", " ") ;
            }
            else  {       
                     try {
                          Expression expr = CCJSqlParserUtil.parseCondExpression ( whereFilter ) ;
                          ((PlainSelect) select.getSelectBody()).setWhere(expr) ; 
                          return select.toString().replaceAll(" + ", " ")  ;
                     } catch( JSQLParserException x ) {
                         x.printStackTrace()      ;
                         return select.toString().replaceAll(" + ", " ") ;
                     }
            }     
           
        } catch( Exception x ) {
              x.printStackTrace();
        }
          
        return null ;
    }
      
    public static String extractFullyQualifiedNameAndAliases ( String sqlQuery )       {
          
        if( sqlQuery == null ) return null         ;
        StringBuilder fields = new StringBuilder() ;

        try {
             CCJSqlParserManager parserManager = new CCJSqlParserManager() ;
             Select select=(Select) parserManager.parse( new StringReader(sqlQuery ) ) ;

             PlainSelect plain       = (PlainSelect)select.getSelectBody() ;
             List        selectitems = plain.getSelectItems()              ;
             
             for ( int i=0 ; i < selectitems.size() ; i++ ) {

                Expression expression=((SelectExpressionItem) selectitems.get(i)).getExpression() ;  

                if( expression instanceof Column)  {
                    
                       Column col = (Column) expression ;
                       fields.append( col.getFullyQualifiedName() ).append(" " ) ;
                }
                else if (expression instanceof Function)  {
                       Function function = (Function) expression;
                       fields.append(function.getParameters()).append(" " ) ;
                }
             }

             Matcher sql_params = PATTERN_KEY_VALUES.matcher ( sqlQuery ) ;
             
             while ( sql_params.find() ) {
           
                String sql_param = sql_params.group() ;
                
                fields.append( sql_param.replace("\n", " ")
                                        .replace("\t", " ")
                                        .replaceAll("(?i) as ", " ")
                                        .replace(",", "")
                                        .replace("\"", " ")
                                        .replaceAll(" +", " ")
                                        .trim() )
                      .append(" " ) ;
             }
             
             return fields.toString() ;
            
        } catch( Exception x )  {
           x.printStackTrace() ;
        }

        return fields.toString() ;
    }            

    public static void main(String[] args) {
        
        String aliases = "  SELECT site_id, bloc_absolute_id, parent_absolute_id\n" +
"FROM (\n" +
"WITH RECURSIVE bloctree AS\n" +
"	(SELECT id, bloc_id, code, nom, parent_id, bloc_id::varchar AS bloc_id_fullname, COALESCE(parent_id::varchar,'') AS \"  RAC\t\"\n " +
"FROM bloc_bloc\n" +
"	WHERE parent_id IS NULL\n" +
"\n" +
"	UNION ALL\n" +
"\n" +
"	SELECT bc.id, bc.bloc_id,bc.code,bc.nom,\n" +
"		bc.parent_id,\n" +
"		bct.bloc_id_fullname || '_of_' || bc.bloc_id::varchar AS bloc_id_fullname,\n" +
"		CASE bct.parent_id_fullname\n" +
"			WHEN '' THEN bc.parent_id::varchar\n" +
"			ELSE bct.parent_id_fullname || '_of_' || bc.parent_id::varchar\n" +
"		END\n" +
"\n" +
"	FROM bloc_bloc AS bc\n" +
"		INNER JOIN bloctree AS bct\n" +
"		ON (bc.parent_id = bct.bloc_id)\n" +
"	)\n" +
"	SELECT\n" +
"\n" +
"	CASE WHEN parent_id IS NULL THEN id ELSE id END AS site_id, \n" +
"\n" +
"	id::varchar || '_' || bloc_id_fullname AS bloc_absolute_id,\n" +
"	CASE parent_id_fullname\n" +
"		WHEN '' THEN NULL\n" +
"		ELSE id::varchar || '_' || parent_id_fullname\n" +
"	END AS parent_absolute_id\n" +
"	FROM bloctree\n" +
") AS test\n" +
"order by bloc_absolute_id ";
     
          String extractFullyQualifiedNameAndAliases = extractFullyQualifiedNameAndAliases(aliases);
          
          System.out.println("extractFullyQualifiedNameAndAliases = " + extractFullyQualifiedNameAndAliases);
          
          //boolean contains = aliases.contains(" id_agent ");
       //   System.out.println("contains = " + contains);
    }
    
}
