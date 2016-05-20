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
