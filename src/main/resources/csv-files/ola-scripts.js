
function camelize(str) {
   return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function(letter, index) {
     return index == 0 ? letter.toLowerCase() : letter.toUpperCase() ;
   }).replace(/\s+/g, '').capitalizeFirstLetter() ; 
}

String.prototype.capitalizeFirstLetter = function()     {
   return this.charAt(0).toUpperCase() + this.slice(1) ;
}


function sufixCamelize( str ) {
       
   var pref = " " ;
   var SPLITER = arguments [ arguments.length  -1 ] ;
   var vars = str.split(SPLITER);
   
   if( vars[0].trim() == ":" || vars[0].trim().length == 0 ) {            
      if( vars[0].trim() == ":") {
          pref = ":" ;              
       }
       vars.shift() ;     
   }    
        
   if( vars[1].trim().length == 0 ) {
      vars.shift() ;                 
   }    
        
   var res_0 = vars[0].trim() ;
   var res_1 = vars[1].trim() ;
  
   return res_0  + camelize( pref + res_1 ) ;
   
}


function contextCamelize( str ) {
  return categCamelize( str, arguments [ arguments.length  -1 ]  ) ;
}

function categCamelize( str ) {
         
   var spliter = arguments [ arguments.length  -1 ] ;
   var regex = new RegExp( spliter ,"g") ;
   var splited = str.split(spliter) ;
   var prefix = " " ;
   var ret  = str;
            
   if( splited[0].trim() == ":"  ) {
        splited.shift() ;      
        prefix = " :" ;                  
   }          
            
   ret = splited.toString().replace(/(?:^\w|[A-Z]|\b\w)/g, function(letter, index) {   
           return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
         }).trim() ;               
            
   return prefix.trim() + 
          ret.replace(/\s+/g, '').charAt(0).toUpperCase() + 
          ret.slice(1).replace(/\s+/g, '')
             .replace( regex, ' ' + spliter + prefix ).trim() ;    
}

// Concat varargs with linker = ":"
function colonLinker() {
  var linker = ":" ;
  var args = Array.prototype.slice.call(arguments) ;
  return args.join(linker) ;
}

