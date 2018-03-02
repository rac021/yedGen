
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
      
    public static String extractFullyQualifiedNameAndAliases ( String sqlQuery ) throws JSQLParserException {
          
        if( sqlQuery == null ) return null         ;
        StringBuilder fields = new StringBuilder() ;

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
    }            

}
