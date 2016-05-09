<h5>obdaYedGen-3.14.2</h5>

 OBDA file generator using yEd Graph Editor v 3.14.2

 - ` mvn clean install assembly:single `
 - ` cd target `
 
 - java -cp YedODBA-3.14.2-1.0-SNAPSHOT-jar-with-dependencies.jar Main \

   -d '../src/main/resources'                                          \

   -out './map.txt'                                                    \

   -ext '.graphml'


java -Xms1024M -Xmx2048M -cp ontop-materializer-1.17.jar ontop.Main                  \

  -owl root-ontology.owl -nt pools.rdf -q " SELECT DISTINCT ?S ?P ?O { ?S ?P ?O } "  \ 

  -out out/coreseInferedTriples.nt -f 100000 -ilv t -F n3                            \

  -q " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>                          \ 

       PREFIX : <http://www.anaee/fr/soere/ola#>                                     \ 

       PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>    \ 

       SELECT ?uriVariableSynthesis ?measu ?value  {                                 \ 

       ?uriVariableSynthesis a oboe-core:Observation ;                               \  

       oboe-core:ofEntity :VariableSynthesis ; oboe-core:hasMeasurement ?measu .     \ 

       ?measu oboe-core:hasValue ?value . Filter ( regex( ?value, 'ph', 'i')) } "    \

       -out out/portail/coreseInferedTriples.nt -f 0 -ilv t -F xml                   \

  -e
