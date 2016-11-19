
function camelize(str) {
   return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function(letter, index) {
     return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
   }).replace(/\s+/g, '').capitalizeFirstLetter() ; 
}

String.prototype.capitalizeFirstLetter = function()     {
    return this.charAt(0).toUpperCase() + this.slice(1) ;
}

// Concat varargs with linker = ":"
function colonLinker() {
  var linker = ":" ;
  var args = Array.prototype.slice.call(arguments) ;
  return args.join(linker) ;
}
