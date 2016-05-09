<h4>obdaYedGen-3.14.2</h5>

 OBDA file generator from yEd Graph Editor v 3.14.2

 - ``` mvn clean install assembly:single ```
 - ``` cd target ```

 - java -cp YedODBA-3.14.2-1.0-SNAPSHOT-jar-with-dependencies.jar Main  \ <br />
  -d   '../src/main/resources'                                          \  <br />
  -out './map.txt'                                                      \  <br />
  -ext '.graphml'

