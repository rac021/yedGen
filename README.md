<h4>obdaYedGen-3.14.2</h5>

 OBDA file generator using yEd Graph Editor v 3.14.2

 - ` mvn clean install assembly:single `
 - ` cd target `
 
 - java -cp YedODBA-3.14.2-1.0-SNAPSHOT-jar-with-dependencies.jar Main  \  
   -d   '../src/main/resources'                                         \  -- 

   -out './map.txt'                                                     \  -- 
   
   -ext '.graphml'

