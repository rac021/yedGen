<h5>obda-YedGen-3.14.2</h5>

 > OBDA file generator using yEd Graph Editor v 3.14.2

- Install procedure :

   - ` mvn clean install assembly:single `

- Arguments :
 
   - `-d   : Directory where graphml files are located `
   - `-out : Path of output mapping obda file `
   - `-ext : extension of files involved in the process `


- Example ( ` cd target ` ) :

 - java -cp YedODBA-3.14.2-1.0-SNAPSHOT-jar-with-dependencies.jar Main  \  
   -d '../src/main/resources'                                           \  
   -out './mapping.obda'                                                \  
   -ext '.graphml'



### Graphml files example :
 
   - Graph

![graphchunks](https://cloud.githubusercontent.com/assets/7684497/17357917/617c5234-595f-11e6-8b72-5f0ee9615828.jpg)


   - URIs
 
![uris](https://cloud.githubusercontent.com/assets/7684497/17358066/27b5ed2a-5960-11e6-887f-3b2cb5641e4f.jpg)

   - Queries

![queries](https://cloud.githubusercontent.com/assets/7684497/17358127/8560d2fa-5960-11e6-84bb-dca580e70cb5.jpg)


   - Variables
 
![variables_declaration](https://cloud.githubusercontent.com/assets/7684497/17358262/470792ea-5961-11e6-9a60-fbf46de3d60c.jpg)

   - Connexion 

![connexion](https://cloud.githubusercontent.com/assets/7684497/17358431/4cb8b362-5962-11e6-9dce-3ccb9a59e9c4.jpg)
