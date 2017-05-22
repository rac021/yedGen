
package org.inra.yedgen.processor ;

import java.io.StringReader ;
import net.sf.jsqlparser.JSQLParserException ;
import net.sf.jsqlparser.expression.Expression ;
import net.sf.jsqlparser.parser.CCJSqlParserUtil ;
import net.sf.jsqlparser.statement.select.Select ;
import net.sf.jsqlparser.parser.CCJSqlParserManager ;
import net.sf.jsqlparser.statement.select.PlainSelect ;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression ;

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
               AndExpression e  = (AndExpression) ps.getWhere()                        ;
               Expression filt = CCJSqlParserUtil.parseCondExpression( whereFilter )   ;
               e.setRightExpression(filt) ;             
               return ps.toString()       ;
            }
            else  {       
                     try {
                          Expression expr = CCJSqlParserUtil.parseCondExpression(   whereFilter ) ;
                          ((PlainSelect) select.getSelectBody()).setWhere(expr) ; 
                          return select.toString()  ;
                     } catch( JSQLParserException x ) {
                         x.printStackTrace()      ;
                         return select.toString() ;
                     }
            }     
           
        } catch( Exception x ) {
              x.printStackTrace();
        }
          
        return null ;
    }

}
