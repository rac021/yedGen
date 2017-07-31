
package org.inra.yedgen.sql ;

import java.io.StringReader ;
import net.sf.jsqlparser.JSQLParserException ;
import net.sf.jsqlparser.expression.Expression ;
import net.sf.jsqlparser.parser.CCJSqlParserUtil ;
import net.sf.jsqlparser.statement.select.Select ;
import net.sf.jsqlparser.parser.CCJSqlParserManager ;
import net.sf.jsqlparser.statement.select.PlainSelect ;

/**
 *
 * @author ryahiaoui
 */

public class SqlAnalyzer {
 
    
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

}
