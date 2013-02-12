// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* Various utility functions
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      4 April 2009
* @version   $Id$
*
* @license 
*/

Ext.tree.TreeLoader.override({
    requestData : function(node, callback){
        if(this.fireEvent("beforeload", this, node, callback) !== false){
            this.transId = Ext.Ajax.request({
                method:this.requestMethod,
                url: this.dataUrl||this.url,
                success: this.handleResponse,
                failure: this.handleFailure,
                timeout: this.timeout || 30000,
                scope: this,
                argument: {callback: callback, node: node},
                params: this.getParams(node)
            });
        }else{
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function"){
                callback();
            }
        }
    }
}); 

Array.prototype.indexAt = function(what) {
	var i = 0;
	while(i < this.length){
		if(this[i]=== what) return i;
		++i;
	}
	return -1;
}

Array.prototype.add = function(wot){
	if (this.indexAt(wot) == -1) this.push(wot);
	return this;
}

Array.prototype.unique = function () {
	var r = new Array();
	o:for(var i = 0, n = this.length; i < n; i++)
	{
		for(var x = 0, y = r.length; x < y; x++)
		{
			if(r[x]==this[i])
			{
				continue o;
			}
		}
		r[r.length] = this[i];
	}
	return r;
}
 
var baseurl = null;

/// parses a URL and retrieves the values for a parameter
function getParamValue( name ) {
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results === null ) {
    return "";
  } else {
    return results[1];
  }
}

// parses a URL and returns various regex'd groups
function parseURL (url) {
	var regex = new RegExp( /https?:\/\/([-\w\.]+)+:?(\d+)?(\/([\w/_\.]*(\?\S+)?)?)?/ );
  return regex.exec( url );
}

/// returns the current port number
function getPortnumber() {
  var results = parseURL(window.location.href);
  if (results[2]) {
    return results[2];
  } else {
    return 80;
  }
//  if( (typeof results[2] === "undefined") || (results[2] === null) ) {
//    return 80;
//  } else {
//    return results[2];
//  }
}

/// returns the current hostname
function getHostName() {
  var results = parseURL(window.location.href);
  if( results[1] === null ) {
    return "";
  } else {
    return results[1];
  }
}

/// returns the current base URL
function getBaseURL() {
  if (baseurl === null) {
    var port = getPortnumber();
    if (port === 80) {
      baseurl = 'http://' + getHostName();
    } else {
      baseurl = 'http://' + getHostName() + ":" + getPortnumber();
    }
  }
  return baseurl;
}

/// Displays an error message window
function errorMessage(msg) {
	
  // on OK, refresh	    	
	Ext.Msg.show({
			title:'Error',
			msg: msg,
			buttons: Ext.Msg.OK,
			icon: Ext.Msg.ERROR,
			fn: function(btn, text) {
					location.reload(true);
			}
	});
  //Ext.MessageBox.alert('Error', msg);

}

// http://underthefold.com/underthefeed/?id=23
if ( !(new String).trim ){
	String.prototype.trim = function() { return this.replace(/^\s+|\s+$/g,''); };
}	
if ( !(new String).normalize && (new String).trim ){
	String.prototype.normalize = String.prototype.normalise = function() { return this.trim().replace(/\s+/g,' '); };
}
if ( !(new String).startsWith ){
	String.prototype.startsWith = function(str,i){ i=(i)?'i':'';var re=new RegExp('^'+str,i);return (this.normalize().match(re)) ? true : false ; };
}	
if ( !(new String).endsWith ){
	String.prototype.endsWith = function(str,i){ i=(i)?'gi':'g';var re=new RegExp(str+'$',i);return (this.normalize().match(re)) ? true : false ; };
}

/// loads a particular setting from the cookie
function loadState(key, defaultValue) {
  return Ext.state.Manager.get(key, defaultValue);
}

/// saves a particular setting to the cookie
function saveState(key, value) {
  Ext.state.Manager.set(key, value);
}

/// loads a particular object from the cookie
function loadObject(key, defaultValue) {
	var record = Ext.state.Manager.get(key);
  if ( record ) {
    return Ext.util.JSON.decode(record);
  } else {
  	return defaultValue;
  }
}

/// saves a particular object to the cookie
function saveObject(key, value) {
  Ext.state.Manager.set(key, Ext.util.JSON.encode(value));
}

function addToStore( store, item ) {
	//check if the item to add already exists
	store = loadStore('server_url_ds');
	var res = store.query('url', item, false);
  if (res.length === 0) {
 	  var URL = Ext.data.Record.create([{name : 'url'}]);
	  store.add(new URL(Ext.util.JSON.decode('{"url":"'+item+'"}')));
  }
  
}

/// Loads a Server Data.Store from information stored in the cookie
function loadStore( name ) {
	
  var store = new Ext.data.Store();
  var URL = Ext.data.Record.create([{name : 'url'}]);
	var record = Ext.state.Manager.get(name);
  if ( record ) {
    var savedData = Ext.util.JSON.decode(record);
    for (var i in savedData) {
      if (typeof savedData[i] == 'object') {
        store.add(new URL(savedData[i]));
      	//addToStore(store, savedData[i].url);
      }
    }
  }
  
  var defaultValue = getBaseURL() + '/openrdf-sesame';
  //addToStore(store, defaultValue);
  //store.insert(0, new URL(Ext.util.JSON.decode('{"url":"'+defaultValue+'"}')));
  store.add(new URL(Ext.util.JSON.decode('{"url":"'+defaultValue+'"}')));
  
  return store;
}

/// Encodes a Data.Store
function storeToJSON(store) {
  var j = "[ ";
  store.each(
    function(r) {
      j += Ext.util.JSON.encode(r.data) + ",";
    }
  );
  
  j = j.substring(0, j.length - 1) + "]";
  
  return j;
}

function addToArray(array, item) {
	var exists = false;
	for (var i in array) {
		
		if (array[i] == [item]) {
			exists = true;
		}
	}
	if (!exists) {
		array.push(item);
	}
}
    
/// Saves a Data.Store to the cookie
function saveStore( name, store ) {
	Ext.state.Manager.set(name, storeToJSON(store));
}

/// Displays an information window
function showWindow(title, text) {
  if (text.length > 500) {
    
    var win = new Ext.Window({
      title : title,
      closable : true,
      width : 600,
      height : 350,
      plain : true,
      layout : 'fit',
      items: [
        new Ext.Panel({
          margins:'3 0 3 3',
          cmargins:'3 3 3 3',
          autoScroll: true,
          html: text
        })
      ]
    });
    
    win.show();
    
  } else {
  	Ext.Msg.show({
			title: title,
			msg: text,
			buttons: Ext.Msg.OK
			//,icon: Ext.Msg.ERROR
		});
  }
}

/**
 * Handle the occurrence of an exception that occurs in the Proxy during data
 * loading. This event can be fired for one of two reasons:
 * The load call returned success: false. This means the server logic returned
 * a failure status and there is no data to read. In this case, this event will
 * be raised and the fourth parameter (read error) will be null.
 * The load succeeded but the reader could not read the response. This means the
 * server returned data, but the configured Reader threw an error while reading
 * the data. In this case, this event will be raised and the caught error will be
 * passed along as the fourth parameter of this event.
 * @param {Object} proxy The HttpProxy object.
 * @param {Object) options The loading options that were specified (see load for details).
 * @param {Object} response The XMLHttpRequest object containing the response data.
 *        See [http://www.w3.org/TR/XMLHttpRequest/] for details about accessing elements
 *        of the response.
 * @param {Error} error The JavaScript Error object caught if the configured Reader
 *        could not read the data. If the load call returned success: false, this parameter
 *        will be null.
 */
function loadFailed(proxy, options, response, error, servlet) {

  var errorMessage = "Time out.";
  
  /*if (error) {
    errorMessage = "I was able to connect to the <b>"+options.params.target+
        "</b> servlet at <a href='" + options.url + "'>"+options.url+"</a>" +
          ", but it responded with an error: <br/><br/>" + 
          error;
  } else */ if (response) {
    var object = Ext.util.JSON.decode(response.responseText);
    // If the load from the server was successful and this method was called then
    // the reader could not read the response.
    if (object) {
      if (object.success) {
        errorMessage = "The data returned from the server is in the wrong format. " +
            "Please notify support with the following information: " + response.responseText;
      } else {
      
        if (options.params.target) {
          // Error on the server side will include an error message in the response.
          errorMessage = "I was able to connect to the <b>"+options.params.target+
          "</b> servlet at <a href='" + options.url + "?"+Ext.urlEncode(options.params)+"'>"+options.url+"</a>" +
            ", but it responded with an error: <br/><br/>" + 
            object.errors.reason;
        } else {
          errorMessage = object.errors.reason;      
        }
      }
    } else {
      errorMessage = "I was able to connect to the <b>"+options.params.target+
          "</b> servlet at <a href='" + options.url + "?"+Ext.urlEncode(options.params)+"'>"+options.url+"</a>" +
          ", but it timed out. <br/><br/>" + 
          "Please redo your action or contact an administrator.";
    }
  }
  
  if (options.params.target == 'search') {
    if (typeof view !== "undefined") {
    	view.hide();
    }
  }
  
  showWindow('Error', errorMessage);
  return errorMessage;
}

  
function loadServerList() {
  var storedserverlist = loadObject('storedserverlist', new Array(0));
  storedserverlist.add(getBaseURL() + '/openrdf-sesame');	  
  storedserverlist.add('http://dev.adaptivedisclosure.org/openrdf-sesame');
  storedserverlist.add('http://vocab.maastro.nl/sesame');
  storedserverlist.add('http://localhost/sesame');
  storedserverlist.add('http://amc-app2.amc.sara.nl/openrdf-sesame');
  storedserverlist.add('http://hcls.deri.org/sparql');
  storedserverlist.add('http://tarski.duhs.org:8080/openrdf-sesame');

  return storedserverlist;
}

function loadServers() {
  var storedserverlist = loadServerList();
  var storedservers = new Array(0);
		  
  for (var i in storedserverlist) {
  	if (typeof storedserverlist[i] !== 'function') {
  	  storedservers.add([storedserverlist[i]]);
  	}
  }
  
  return storedservers;
}

function loadRepositories() {
	var items = Ext.getCmp('sesame-panel').getForm().getValues();
		
	if (items.server_url === 'Select or type in a server...' || items.server_url === '' ) {
			showWindow('Error', 'Select or type in a server first.');
			return false;
	}

    // connect to the Services servlet to connect to the sesame server
    Ext.getCmp('sesame-panel').getForm().submit({
      method : 'POST',
      waitTitle : 'Connecting',
      waitMsg : 'Getting list of repositories ...',
      
      // Functions that fire (success or failure) when the server responds.
      // The one that executes is determined by the
      // response that comes from login.asp as seen below. The server would
      // actually respond with valid JSON,
      // something like: response.write "{ success: true}" or
      // response.write "{ success: false, errors: { reason:
      // 'Login failed. Try again.' }}" depending on the logic contained within your server script.
      // If a success occurs, the user is notified with an alert messagebox,
      // and when they click "OK", they are redirected to whatever page
      // you define as redirect.
      success : function(form, action) {
        
        if (action.response.getResponseHeader['org.vle.aid.metadata.exception.SystemQueryException']) {
        	showWindow('Error', action.response.getResponseHeader['org.vle.aid.metadata.exception.SystemQueryException']);
        	return;
        }
        
        var obj = Ext.util.JSON.decode(action.response.responseText);
        //Ext.Msg.alert('Status', 'Login Successful:<br/>' + action.response.responseText);
  
        if (obj.repositories.length === 0) {
          showWindow('Error', 'No repositories found. Check the Sesame URL, username and password');
          return;
        }
        
        // succesful connection, so we can add this URL to the cache
      	var storedserverlist = loadServerList();
      	storedserverlist.add(items.server_url);
      	saveObject('storedserverlist', storedserverlist);
      	
      	// save current info
      	saveState('server_url', items.server_url);
      	saveState('repository', '');
      
		shortToLongRepositoryMap = {};
		// Shortening the URL
		for(i =0; i< obj.repositories.length;i++){
			cur = ""+obj.repositories[i];

			// Take last part which is neither # nor /
			pattern = /[^#\/]+$/gi;
			
			// obj.repositories are array of array of string
			tmp = [""+cur.match(pattern)];

			// Store first this into map
			shortToLongRepositoryMap[tmp] = obj.repositories[i];

			// now use the shorter one
			obj.repositories[i] = tmp;
		}
        saveObject('storedrepositorylist', obj.repositories);
		saveObject('shorttolongrepositorymap', shortToLongRepositoryMap);

		// This is only needed for direct link;

	  var browseServerUrl = checkParameter('server_url');
	  var browseRepositoryName = checkParameter('repository_name');
	  if(browseServerUrl && browseRepositoryName){
			thesauruspanel = Ext.getCmp('thesauruspanel');

			item = getLongURL(browseRepositoryName);
			var  b = new Ext.app.ThesaurusBrowser({
			   //loader : tloader,
			   id : server_url + '-' + item,
			   ns : loadState("ns"),
			   server_url : browseServerUrl,
			   repository : item,
			   username : loadState("username"),
			   password : loadState("password") 
			 });

			thesauruspanel.add(b);
			thesauruspanel.setActiveTab(b);
			b.showRoot();
		}
      },
      // Failure function, see comment above re: success and failure.
      // You can see here, if login fails, it throws a messagebox
      // at the user telling him / her as much.
      failure : function(form, action) {
        if (action.failureType == 'server') {
          var obj = Ext.util.JSON.decode(action.response.responseText);
          showWindow('Login Failed!', obj.errors.reason);
        } else {
          showWindow('Error', 'Server is unreachable : ' + action.response.responseText + " or timed out");
        }
      }
    });
  }
/* AW: Additional functions */


function parse_prefix(node){
	 id = node.id;
	 if(id.endsWith('-root')){
		id = node.childNodes[0].id;
	 }

	 prefix = "prefix "+ getPrefix(loadState('repository','rdf-db'))+": <"+id.split("#")[0]+"#>\n";
	 return prefix;
}

function skos_top_query(node){
	 id = node.id
	 result = "";

	 if(id.endsWith('-root'))
		return skos_top_query(node.childNodes[0]);

	 result = "#Sample Query Top Concept\n";
	 if(id.indexOf("2008") >= 0)
		result += "prefix skos:<http://www.w3.org/2008/05/skos#> \n";
	 else
		result += "prefix skos:<http://www.w3.org/2004/02/skos/core#> \n";

	 prefixStr = getPrefix(loadState('repository','rdf-db'));

	 result +=  "prefix "+prefixStr+": <"+id.split("#")[0]+"#>\n";	

	 result +=  "SELECT ?T ?L where { \n" +
				"  { "+prefixStr+":scheme  skos:hasTopConcept ?T } union \n" +
				"  { ?T skos:topConceptOf "+prefixStr+":scheme } .\n" +
				"  ?T skos:prefLabel ?L\n" +
				"}\n" 
	 return result;
}

function owl_top_query(node) {
	 id = node.id

	 if(id.endsWith('-root'))
		return owl_top_query(node.childNodes[0]);

	 result = "#Sample Query Top Concept\n";
	 result += "prefix owl: <http://www.w3.org/2002/07/owl#> \n"+
		  "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
		  "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
	 	  "select distinct ?S ?L where { \n"+
     		  "    ?S rdf:type owl:Class . \n"+
     		  "    OPTIONAL { \n "+
		  "       ?S owl:subClassOf ?Z . \n"+
                  "       ?Z rdf:type owl:Class . \n" +
                  "       FILTER ( ?Z != owl:Thing && ?Z!=?S) \n" +
     		  "    } . \n" +
     		  "    OPTIONAL {?S rdfs:label ?L}  \n "+
		  "    FILTER (!isBlank(?S) && ?S!=owl:Thing ) \n" +
		  " } limit 25 ";

	return result;
}

function top_query_template(node){
	 id = node.id;
	 if(id.endsWith('-root'))
		return top_query_template(node.childNodes[0]);
	
	 if(id.indexOf("rdf")>=0 || id.indexOf("Owl")>=0)
	 	return owl_top_query(node);
	
	 return skos_top_query(node);

}

function owl_narrow_query(node) {
	id = node.id

	if(id.endsWith('-root'))
		return owl_narrow_query(node.childNodes[0]);

	prefixStr = getPrefix(loadState('repository','rdf-db'));
	prefixDef = "prefix "+prefixStr+": <"+id.split("#")[0]+"#>\n";	
	strnode = prefixStr+":"+id.split("#")[1];

	result = "#Sample Query Narrower\n" +
			 prefixDef + 
			"prefix owl: <http://www.w3.org/2002/07/owl#>\n "+
			"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n "+
			"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n "+
			"\n "+
			"select distinct ?T ?L where { \n "+
			"  { ?T rdfs:subClassOf  "+ strnode+" } union \n "+
			"  { ?T rdf:type "+strnode+" } .\n "+
			"    ?T rdfs:label ?L\n "+
			"\n "+
			"}\n "

	return result;
}

function skos_narrow_query(node){
    id = node.id
	if(id.endsWith('-root'))
		return skos_narrow_query(node.childNodes[0]);

	prefixStr = getPrefix(loadState('repository','rdf-db'));
	prefixDef = "prefix "+prefixStr+": <"+id.split("#")[0]+"#>\n";	
	strnode = prefixStr+":"+id.split("#")[1];

	result = "#Sample Query Narrower\n";
	result += "prefix skos:<http://www.w3.org/2004/02/skos/core#> \n" +
		 prefixDef + 
		 "select ?T ?L where { \n" +
		 " { "+strnode+" skos:narrower ?T } union \n" +
		 " { ?T skos:broader "+strnode+" } .\n" +
		 " ?T skos:prefLabel ?L\n" +
		 "}\n" 
				
	return result;
}

function narrow_query_template(node){
	 id = node.id;
	 if(id.endsWith('-root'))
		return top_query_template(node.childNodes[0]);
	 
	 if(id.indexOf("rdf")>=0 || id.indexOf("Owl")>=0)
	 	return owl_narrow_query(node);
	
	 return skos_narrow_query(node);

}

function skos_alternate_query(node){
    id = node.id
	prefixStr = getPrefix(loadState('repository','rdf-db'));
	prefixDef = "prefix "+prefixStr+": <"+id.split("#")[0]+"#>\n";	
	strnode = prefixStr+":"+id.split("#")[1];

	result = "#Sample Query Alternate Label\n";
	result += "prefix skos:<http://www.w3.org/2004/02/skos/core#> \n" +
				 prefixDef + 
				"select ?L where { \n" +
				"   { "+strnode+" skos:altLabel ?L } union \n" +
				"   { ?L skos:altLabel "+strnode+" } \n" +
				"}\n" ;

}

function owl_alternate_query(node){
    id = node.id
	prefixStr = getPrefix(loadState('repository','rdf-db'));
	prefixDef = "prefix "+prefixStr+": <"+id.split("#")[0]+"#>\n";	
	strnode = prefixStr+":"+id.split("#")[1];

	result = "#Sample Query Alternate Label\n" +
			prefixDef + 
			"prefix owl: <http://www.w3.org/2002/07/owl#>\n "+
			"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n "+
			"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n "+
			"\n "+
			"select distinct ?T ?L where { \n "+
			"  { ?T rdfs:subClassOf  "+ strnode+" } union \n "+
			"  { ?T rdf:type "+strnode+" } .\n "+
			"    ?T rdfs:label ?L\n "+
			"\n "+
			"}\n "
}
function alternate_query_template(node){
	 if(id.endsWith('-root'))
		return "";

	 if(id.indexOf("rdf")>=0 || id.indexOf("Owl")>=0)
	 	return owl_alternate_query(node);
	
	 return skos_alternate_query(node);

}

 /* Trying to check if index.html is given a parameter of server_url and repository/ontology
 * in such cases, initializes cookies and states so that repository browser will be opened accordingly 
 * Sample code from : http://www.netlobo.com/url_query_string_javascript.html
 */


function checkParameter( name )
{
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}
 
function debugMap(msg){

	  test = loadObject("shorttolongrepositorymap", {}); 
	  t=""; for(x in test) t += " "+x; 
	  //Ext.MessageBox.alert(msg,  t);	

}

function getLongURL(shortURL){
	  map = loadObject("shorttolongrepositorymap", {}); 
	  return map[shortURL] ? map[shortURL] : shortURL;
}

function getShortURL(longURL){
	  // pattern match last part of url not containing # or /
	  pattern = /[^#\/]+$/gi; 
	  shortURL = (""+longURL).match(pattern);
	  return shortURL;
}

function getPrefix(longURL){
	  return "aida";
}

function handleDirectLink(){
  var browseServerUrl = checkParameter('server_url');
  var browseRepositoryName = checkParameter('repository_name');
  if(browseServerUrl && browseRepositoryName){
  	  var repos = loadObject('openrepositorylist2', null);
	  if(repos == null) repos = {};
	  repos[browseRepositoryName] = 1;
	  server_url = browseServerUrl;

	  // Needed for synchronizing what is shown on repository box and server list with what is provided in direct link.
	  saveState('direct_repository', browseRepositoryName);
	  saveState('direct_server_url', browseServerUrl);
	  saveState('server_url', browseServerUrl);
	  saveObject('openrepositorylist2', repos);

	  debugMap("direct");

  }
}

function setUserLensInfo (obj) {
	 	info = loadObject('detectInfo');
		lens = loadObject('curLens');
		obj.top_concept = lens["concept"];
		obj.narrower_predicate = lens["predicate"];
		obj.skos_version = info.skosVersion;
		obj.virtuoso_namedgraph = '';
}


/* Library 2.0 Beta 1
 * Copyright(c) 2006-2007, Ext JS, LLC.
 * licensing@extjs.com
 *
 * http://extjs.com/license
 */


// Very simple plugin for adding a close context menu to tabs
Ext.ux.DirectLinkMenu= function(){
    var tabs, menu, ctxItem;
    this.init = function(tp){
        tabs = tp;
        tabs.on('contextmenu', onContextMenu);
    }

    function onContextMenu(ts, item, e){
		if(item.tabTip) {
			menu = new Ext.menu.Menu([
				{
					id: tabs.id + '-direct',
					text: 'Direct Link',
					handler : function(){
							window.open(item.tabTip);
					}
			 	} ]);
				
			menu.showAt(e.getPoint());
		}
    }
};
