
package org.inra.yedgen.splitter ;

import java.util.Map ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.ArrayList ;
import java.time.LocalDate ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import java.util.stream.Stream ;
import java.util.stream.IntStream ;
import java.util.stream.Collectors ;
import java.time.format.DateTimeFormatter ;
import static java.time.temporal.ChronoUnit.DAYS ;
import static java.util.stream.Collectors.toList ;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth ;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth ;

/**
 *
 * @author ryahiaoui
 */
public class MagicFilter {
    
    private final List< Object >         list                 = new ArrayList<>() ;
    private final Map< Integer, String > codeQueryByVariables = new HashMap<>()   ;
    private final boolean                isList                                   ;
    
    static Pattern patternFilter = Pattern.compile("\\{.*?\\}") ;
    static Pattern listFilter    = Pattern.compile("\\(.*?\\)") ;
    
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") ;

    public MagicFilter( String expresion ) {
       
        if(isPeek(expresion))       {
            isList = true ; 
            treateList( expresion)  ;
        }
        else if(isStep(expresion))  {
            isList = false ;
            treatRange( expresion)  ;
        }
        else {
            isList = false ;
        }
    }

    public List<Object> getList() {
        return list ;
    }
    
    public boolean getIsList() {
        return isList ;
    }

    public Map<Integer, String> getCodeQueryByVariables() {
        return codeQueryByVariables ;
    }
    
    private void treateList( String expresion ) {
        
      int step  = Integer.parseInt(expresion.toLowerCase().split(" peek ")[1].trim()) ;
               
      Matcher mListFilter     = listFilter.matcher( expresion) ;
               
      while ( mListFilter.find() ) {
                  
           List<String> asList = Arrays.asList(mListFilter.group()
                                       .replace("(", "").replace(")","")
                                       .trim().split(","))   ;
           List<String> peeker = peeker( step, asList)       ;
           list.addAll(peeker)                               ;
                    
      }
                
      Matcher mPatternQuery  = patternFilter.matcher( expresion) ;
      List<String> vars      = new ArrayList()                   ;
              
      while ( mPatternQuery.find()) {
                  
         String[] split1 = mPatternQuery.group().replace("{", "").replace("}","")
                                                                 .trim().split("&") ;
         
         String varName  = split1[0].trim() ; 
         
         Stream.of(split1[1].trim().replaceAll(" +", "").split(","))
               .filter( s -> ! s.isEmpty() )
               .map (s -> codeQueryByVariables.compute ( 
                          Integer.parseInt(s) ,
                          ( k,v)-> v != null ? v + " AND " + 
                          varName + " IN ?LIST " : varName + " IN ?LIST "))
               .count() ;
                                          
         vars.add( mPatternQuery.group().replace("{", "").replace("}","").trim() ) ;
      } 
    }
    
    private void treatRange( String expresion ) {
    
       /* Range values : null - d - D - m - M - y - Y*/
        
       Character range = expresion.trim()
                                  .toLowerCase().split(" step ")[1]
                                  .charAt(expresion.trim()
                                  .toLowerCase()
                                  .split(" step ")[1].length() - 1 ) ;
       
       boolean isLetter = ! Character.isDigit( range )               ;
       
       range            = isLetter ? range : null                    ;
        
       int step  = Integer.parseInt(expresion.toLowerCase().split(" step ")[1]
                                                           .replace( isLetter == true ?
                                                                     "" + range : "" , "" )
                                                           .trim()) ;
        
       List<List<String>> peekDates = peekDates( expresion.split("\\{")[0].trim().replaceAll(" +", "") ,
                                                 step                                                  ,
                                                 range                                               ) ;      
          
       list.addAll(peekDates) ;
      
        Matcher mPatternQuery  = patternFilter.matcher( expresion) ;
       
        while ( mPatternQuery.find()) {
            
           String[] split1 = mPatternQuery.group().replace("{", "")
                                          .replace("}","").trim().split("&") ;
           String varName  = split1[0].trim() ;

           Stream.of(split1[1].trim().replaceAll(" +", "").split(","))
                 .filter( s -> ! s.isEmpty() )                                                
                 .map(s -> codeQueryByVariables.compute ( Integer.parseInt(s),( k,v) -> 
                               v != null ? v + "AND ( " + varName + " BETWEEN ?DATE_0 AND ?DATE_1 ) " : 
                               " ( " + varName + " BETWEEN ?DATE_0 AND ?DATE_1 ) "))
                 .count() ;
       } 
    }
    
    public static boolean isCommented(String exp ) {
        return exp.trim().startsWith("--") ;
    }
    
    private static boolean isPeek(String string )      {
        return string.toLowerCase().contains(" peek ") ;
    }
    
    private static boolean isStep(String string)       {
        return string.toLowerCase().contains(" step ") ;
    }

    public static  List<String> peeker(int peek, List<String> elements ) {
        
       int listSize   = elements.size()                    ;
       int chunkSize  = peek > 0 ? peek : elements.size()  ;
            
       List<String> chunkedList = IntStream.range( 0 , ( listSize - 1 ) / peek + 1 )
                                           .mapToObj( i -> elements
                                           .subList ( i *= chunkSize ,
                                                      listSize - chunkSize >= i ? i + chunkSize : listSize ))
                                           .map( l -> "( " + String.join(" , ", l ) + " ) " )
                                           .collect(Collectors.toList());
       return chunkedList ;
    } 
  
    private static List<List<String>>  peekDates ( String    stringDates , 
                                                   int       step        ,
                                                   Character range     ) {
        
      if ( range == null || range == 'y' || range == 'Y' )          {
          
          List<Integer> dates  = Stream.of(stringDates.split("_")   )
                                       .filter ( s -> ! s.isEmpty() )                            
                                       .map(s -> Integer.parseInt(s))
                                       .collect(toList())           ;
          
         return peekDatesY( dates.get(0), dates.get(1), step )      ;
      }
      
      else if ( range == 'm' || range == 'M' )                      {
          
         List<String> dates  = Stream.of(stringDates.split("_")     )
                                     .filter ( s -> ! s.isEmpty()   )                            
                                     .collect(toList())             ;
          
         return peekDatesM( dates.get(0) , dates.get(1) , step )    ; 
      }
      
      else if ( range == 'd' || range == 'D' )                      {
          
          List<String> dates  = Stream.of(stringDates.split("_")    )
                                      .filter ( s -> ! s.isEmpty()  )                            
                                      .collect(toList())            ;
          
         return peekDatesD( dates.get(0), dates.get(1), step  )     ;
      }

       return null ;
       
    }

    private static LocalDate incMonth( LocalDate date , int nbMonths ) {
        return date.plusMonths(nbMonths)    ;
    }
    private static LocalDate decMonth( LocalDate date , int nbMonths ) {
        return date.minusMonths(nbMonths )  ;
    }
    private static LocalDate incDays( LocalDate date , int days )      {
        return date.plusDays(days )         ;
    }
    private static LocalDate getFistDayOfMonth( LocalDate date )       {
        return date.with(firstDayOfMonth()) ;
    }
    private static LocalDate getLastDateOfMonth( LocalDate date )     {
        return date.with(lastDayOfMonth())  ;
    }
    private static String toString( LocalDate date ) {
        return formatter.format(date)       ;
    }
      
    private static List<List<String>>  peekDatesM ( String    dateBeg , 
                                                    String    dateEnd , 
                                                    int       step  ) {
        
          List<List<String>> dates     =  new ArrayList<>()         ;
          LocalDate          _dateBeg  =  LocalDate.parse(dateBeg ) ;
          LocalDate          _dateEnd  =  LocalDate.parse(dateEnd ) ;
          
          LocalDate          mdlDate   =  _dateBeg                  ;
          
          LocalDate    lastDayOfMonth                               ;
         
          while(true) {
              
             lastDayOfMonth = incMonth( _dateBeg , step ) ;
             lastDayOfMonth = getLastDateOfMonth( lastDayOfMonth   ) ;
             
             if(  lastDayOfMonth.compareTo( _dateEnd ) >= 0 ) {
                 dates.add(Arrays.asList(toString( _dateBeg ) , toString( _dateEnd ) ) ) ;
                 break ;
             }
             else {
                  if( DAYS.between( _dateBeg , lastDayOfMonth ) > step * 31 ) {
                      lastDayOfMonth = decMonth( lastDayOfMonth , 1 )      ; 
                      lastDayOfMonth = getLastDateOfMonth(lastDayOfMonth ) ; 
                  }
                  
                  dates.add( Arrays.asList(toString( _dateBeg), toString(lastDayOfMonth) ) ) ;
                  
                 _dateBeg = incMonth( lastDayOfMonth, 1 ) ;
                 _dateBeg = getFistDayOfMonth( _dateBeg ) ;
             }
          }
          
          return dates ;
    }
    
    private static List<List<String>>  peekDatesD ( String   dateBeg , 
                                                    String   dateEnd , 
                                                    int      step  ) {
         step = step <= 0 ? 0 : step - 1 ;
         
          List<List<String>> dates     =  new ArrayList<>()     ;
          LocalDate     _dateBeg  =  LocalDate.parse(dateBeg )  ;
          LocalDate     _dateEnd  =  LocalDate.parse(dateEnd )  ;
          
          LocalDate     mdlDate   =  _dateBeg                   ;
          
          LocalDate     nextDay                                 ;
         
          while(true) {
              
             nextDay = incDays(_dateBeg , step ) ;
             
             if(  nextDay.compareTo(_dateEnd) >= 0 ) {
                 dates.add(Arrays.asList(toString(_dateBeg), toString(_dateEnd))) ;
                 break ;
             }
             else {
                  
                  dates.add ( Arrays.asList(toString( _dateBeg ), toString(nextDay) ) ) ;
                  
                 if( step == 0 ) {
                     _dateBeg = incDays( _dateBeg, 1 ) ;
                 } else {
                     _dateBeg = incDays( nextDay , 1 ) ;
                 }
             }
          }
          
          return dates ;
    }
    
    private static List<List<String>>  peekDatesY ( int       dateBeg , 
                                                    int       dateEnd , 
                                                    int       step  ) {
        
       List<List<String>> dates = new ArrayList<>()       ;
        
       for( int i =  dateBeg ; i<= dateEnd ; i += step  ) {

         int max = i + step > dateEnd ? dateEnd : i + step -1 ;
           
         dates.add(Arrays.asList(String.valueOf(i), String.valueOf(max) )) ;
          
       }
       
       return dates ;
    }
    
}
