<h5>obda-YedGen-3.14.2</h5>

 > OBDA file generator using yEd Graph Editor v 3.14.2

- Install procedure :

   - ` mvn clean install assembly:single `

- Arguments :
 
   - `-d   : Directory where graphml files are located `
   - `-out : Path of output mapping obda file `
   - `-ext : extension of files involved in the process `


- Example :

```
❯   java -cp target/yedGen-1.0-SNAPSHOT-jar-with-dependencies.jar Main  \
   -d 'src/main/resources/demo'                                         \
   -out './mapping.obda'                                                \
   -ext '.graphml'
```

- Supports features :

   - **FullGraph** : One graph -> One obda file mapping
   - **GraphChunks** : Multiple graphs -> one obda file mapping
   - **PatternGraphChunks** : One [ multiple ] graph(s) -> multiple obda files
 
----------------------------------------------------------------------------------

- Description of [**PatternGraphChunks**]( https://github.com/rac021/yedGen/blob/master/README.md#graphml-files-example-) format :

   *  **1- Description of Patterns declarations :**
   
      `##PATTERN_CODE` : Declare a specific pattern

      `NUM` : Index of the first Entity ( the following entities will have an incremental index )
      
      `IRI` : pattern URI for variables. Do not touch the **?VARIABLE**
      
      `ObjectProperty` : used as object property between entities of the pattern
      
      `[ ObjectProperty_Entity SqlQueryIndex ]` : Object Property "_" Entity SqlQueryIndex

   * Example :

      - The following pattern 
   
```
        ##PATTERN_1  20  ola/observation/variable/?VARIABLE/{dty_code}/{mesure_id} 
           oboe-core:hasContext 
            [ oboe-core:Observation_Ammonium Q_20 ] 
            [ oboe-core:Observation_Solutes Q_21 ] 
            [ oboe-core:Observation_Water Q_22 ]

```  

 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *Gives the following mapping :*
         
 
 ```
      - mappingId	(20)_ola_observation_variable_ammonium 
      
      - target ola/observation/variable/Ammonium/{dty_code}/{mesure_id} a oboe-core:Observation 
        oboe-core:ofEntity Ammonium 
        oboe-core:hasContext ola/observation/variable/Solutes/{dty_code}/{mesure_id}
      
      - source : Query_20
```  

```  
      - mappingId	(21)_ola_observation_variable_solute
      
      - target ola/observation/variable/Solutes/{dty_code}/{mesure_id} a oboe-core:Observation
        oboe-core:ofEntity Solutes 
        oboe-core:hasContext ola/observation/variable/Water/{dty_code}/{mesure_id}
      
      - source : Query_21
   
```

```  
      - mappingId	(22)_ola_observation_variable_solute
      
      - target ola/observation/variable/Water/{dty_code}/{mesure_id} a oboe-core:Observation 
        oboe-core:ofEntity Water oboe-core:hasContext ...`
      
      - source : Query_22
   
```  
   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; *  **2- Description of the Variables declarations :**
   
         `?VARIABLE` : Declare Variable
   
         `##PATTERN_CODE` : Link current Variable to the Pattern PATTERN_CODE.
        
         `:Variable` : name of the Entity concerned by this Variable
         
         `{ ?Matcher=value}` : replace all matchers in the result mapping by value ( for each variable )
   

   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; * Example :

   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - The following declaration 

```  
         ?VARIABLE  ##PATTERN_1 :Nitrogen 
         { ?VAR_URI=nitrogen } 
         { ?standardVar=oboe-standard:MilligramPerLiter } 
         { ?NUM_DTYPE=11 } 
         { ?FILTER_VAR='azote ammoniacal'  } 
      
```  


 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; *reliying to the ##PATTERN_1 will Replace :*

   ```  
         ?VARIABLE     in the Graph BY   ":Nitrogen"
         ?VAR_URI                   BY   nitrogen   ( for example in sql queries )
         ?standardVar               BY   oboe-standard:MilligramPerLiter
    
``` 
----------------------------------------------------------------------------------


### Graphml files example :
 
   - Graph
<p align="center"> 
![graphchunks](https://cloud.githubusercontent.com/assets/7684497/17357917/617c5234-595f-11e6-8b72-5f0ee9615828.jpg)
</p>

   - URIs
<p align="center"> 
![uris](https://cloud.githubusercontent.com/assets/7684497/17358066/27b5ed2a-5960-11e6-887f-3b2cb5641e4f.jpg)
</p>
   - Queries
<p align="center"> 
![pattern_queries](https://cloud.githubusercontent.com/assets/7684497/17855399/d4be1c3c-6878-11e6-969a-f98deb9c0005.png)
</p>

   - Variables
 <p align="center"> 
![variables_declaration](https://cloud.githubusercontent.com/assets/7684497/17358262/470792ea-5961-11e6-9a60-fbf46de3d60c.jpg)
</p>
   - Connexion 
<p align="center">
![connexion](https://cloud.githubusercontent.com/assets/7684497/17358431/4cb8b362-5962-11e6-9dce-3ccb9a59e9c4.jpg)
</p>
