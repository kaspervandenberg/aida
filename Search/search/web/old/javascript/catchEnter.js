var nn4 = (navigator.appName.indexOf("Netscape") > -1 && navigator.appVersion.indexOf("4") > -1) ? true : false
 

 //function for forcing form submission
 function checkEnter(e){
	 var characterCode
 	 if(e && e.which){
	 	 e = e
		 characterCode = e.which
	 }
 	 else{
	 	 e = event
	 	 characterCode = e.keyCode
 	 }	 
	 if(characterCode == 13){
	  	 document.forms[0].submit()
	  	 return false
 	 }
	 return true
					 	
}