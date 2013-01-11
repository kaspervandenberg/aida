var prefs = Components.classes["@mozilla.org/preferences-service;1"]
				.getService(Components.interfaces.nsIPrefBranch);

// FIXME: for the query buttons that disable when clicked there should be a timeout to reset them.
//        preferably, they should be colored red when the HTTP request gets an error code back.


function loadUrl(url) {
	window._content.document.location = url;
	window.content.focus();
}

function conceptKeyhandler(event) {
    if (event.keyCode == event.DOM_VK_RETURN) {
        prependDefaultNamespace("aidaac-property");
        prependDefaultNamespace("aidaac-concept");
        findDocumentSuggestions();
    }
}

function conceptSuggestions() {
    var concept = document.getElementById("aidaac-concept");
    var property = document.getElementById("aidaac-property");
    var servlet = prefs.getCharPref("aidaac.servlet.select");
    var server_url = prefs.getCharPref("aidaac.kb.server.url");
    var repository = prefs.getCharPref("aidaac.kb.repository");
    var username = prefs.getCharPref("aidaac.kb.username");
    var password = prefs.getCharPref("aidaac.kb.password");
    var limit = 10;
    var query = "select distinct C, localName(C), namespace(C) from " + 
            "{C} P {L}, {D} Q {C}, {C} rdf:type {Class} " +
            "where " +
             // Show only concepts with the set property.
             ((concept.value == "" && property.value != "") ?
                "Q like \"" + property.value + "*\" ignore case and "  : 
                "") +
            "( L like \"" + concept.value + "*\" ignore case and " +
              "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
                "P = <http://www.w3.org/2004/02/skos/core#prefLabel> or " + 
                "P = <http://www.w3.org/2004/02/skos/core#altLabel> ) ) or " +
            "( C like \"" + concept.value + "*\" ignore case or " +
               "localName(C) like \"" + concept.value + "*\" ignore case and " +
              "( Class = <http://www.w3.org/2000/01/rdf-schema#Class> or " +
                "Class = <http://www.w3.org/2002/07/owl#Class> or " + 
                "Class = <http://www.w3.org/2004/02/skos/core#Concept> ) )" +
            " limit " + limit;
    getSuggestions(servlet,server_url,repository,username,password,"aidaac-concept-menu",query);
}

function documentSuggestions() {

}

function propertyKeyhandler(event) {
    if (event.keyCode == event.DOM_VK_RETURN) {
        prependDefaultNamespace("aidaac-property");
        prependDefaultNamespace("aidaac-concept");
        findDocumentSuggestions();
    }
}

function propertySuggestions() {
    var concept = document.getElementById("aidaac-concept");
    var property = document.getElementById("aidaac-property");
    var servlet = prefs.getCharPref("aidaac.servlet.select");
    var server_url = prefs.getCharPref("aidaac.kb.server.url");
    var repository = prefs.getCharPref("aidaac.kb.repository");
    var username = prefs.getCharPref("aidaac.kb.username");
    var password = prefs.getCharPref("aidaac.kb.password");
    var limit = 10;
    var query = 
        "select distinct P, localName(P), namespace(P) from " +
        "{P} rdfs:subClassOf {rdf:Property} " +
        "where " +
        "( localName(P) like \"" + property.value + "*\" ignore case or " +
        "  P like \"" + property.value + "*\" ignore case ) limit " + limit +
        " union select distinct P, localName(P), namespace(P) from " +
        "{P} rdf:type {rdf:Property} " +
        "where " +
        "( localName(P) like \"" + property.value + "*\" ignore case or " +
        "  P like \"" + property.value + "*\" ignore case ) limit " + limit +
        " union select distinct Q, localName(Q), namespace(Q) from " +
        "{Q} rdf:type {P} " +
        "where " + 
        "( localName(P) like \"" + property.value + "*\" ignore case or " +
        "  P like \"" + property.value + "*\" ignore case ) limit " + limit;
    getSuggestions(servlet,server_url,repository,username,password,"aidaac-property-menu",query);
}

function getSuggestionsSOAP(servlet,server_url,repository,username,password,menu_id,query) {
    function getClient() {
      return Components.classes['@mozilla.org/xml-rpc/client;1']
          .createInstance(Components.interfaces.nsIXmlRpcClient);
    }

    var xmlRpcClient;
    function getXmlRpc() {
      if (!xmlRpcClient) xmlRpcClient = getClient();
      return xmlRpcClient;
    }

    function callAsync() {
      dump('Call Async\n');
      var xmlRpc = getXmlRpc();
      xmlRpc.init('http://localhost:8080/axis/services/RepositoryWS');
      var serv = xmlRpc.createType(xmlRpc.STRING, {});
      serv.data = server_url;
      var repo = xmlRpc.createType(xmlRpc.STRING, {});
      repo.data = repository;
      var user = xmlRpc.createType(xmlRpc.STRING, {});
      user.data = username;
      var pass = xmlRpc.createType(xmlRpc.STRING, {});
      pass.data = password;
      var query_language = xmlRpc.createType(xmlRpc.STRING, {});
      query_language.data = "serql";
      var query = xmlRpc.createType(xmlRpc.STRING, {});
      query.data = query;

      xmlRpc.asyncCall(Listener, null, 'RepositoryWS.selectQuery', [serv,repo,user,pass,query_language,query], 6);
    }

    var Listener = {
      onResult: function(client, ctxt, result) {
          alert('result!\n');
          //result = result.QueryInterface(Components.interfaces.nsISupportsCString);
          //document.getElementById('statename').setAttribute('value', result.data);
      },

      onFault: function(client, ctxt, fault) {
          dump('Fault! ' + fault + '\n');
      },

      onError: function(client, ctxt, status, errorMsg) {
          dump('Error! <(' + status.toString(16) + ') ' + errorMsg + '>\n');
      }
    };
    
    callAsync();
}

function getSuggestions(servlet,server_url,repository,username,password,menu_id,query,limit) {
    var ssreq;
    ssreq = new XMLHttpRequest();
    ssreq.onreadystatechange = getSuggestionsReady;
    ssreq.open("GET",
        servlet + 
            "?" + "server_url=" + escape(server_url) + 
            "&" + "repository=" + escape(repository) + 
            "&" + "username=" + escape(username) + 
            "&" + "password=" + escape(password) +
            "&" + "query_language=serql" +
            "&" + "xml=1" +
            "&" + "query=" + escape(query),
        true);
    ssreq.send(null);

    function getSuggestionsReady() {
        if (ssreq.readyState == 4) {
            if (ssreq.status == 200) {
                if (ssreq.responseXML) {
                    var menu = document.getElementById(menu_id);
                    for(var i=menu.childNodes.length - 1; i >= 0; i--) {
                        menu.removeChild(menu.childNodes.item(i));
                    }

                    var hash = {};
                    var xmlDoc = ssreq.responseXML.documentElement;
                    var rows = xmlDoc.getElementsByTagName("row");
                    for (i=0;i<rows.length;i++) {
                        var cols = rows[i].getElementsByTagName("col");
                        var value = cols[0].firstChild.nodeValue;
                        var description = cols[2].firstChild.nodeValue;
                        hash[value] = description;
                    }
                    for (var key in hash) {
                        var tempItem = document.createElement("menuitem");
                        tempItem.setAttribute("label", key, lookupNamespace(hash[key]));
                        tempItem.setAttribute("crop", "left");
                        menu.appendChild(tempItem);
                    }
                } else if (ssreq.responseText) {
                    alert(ssreq.responseText);
                } else {
                    alert("no response XML or Text available");
                }
            } else {
                alert(ssreq.statusText);
            }
        } else {
        }
    }
}


function loadDocumentKeyhandler(event) {
    if (event.keyCode == event.DOM_VK_RETURN)
            loadUrl(document.getElementById("aidaac-load-document").value);
}


function loadDocumentSuggestions() {
    // FIXME: add file name autocompletion
}

function findDocumentSuggestions() {
    var property = document.getElementById("aidaac-property");
    var concept = document.getElementById("aidaac-concept");
    var load_doc = document.getElementById("aidaac-load-document");
    var servlet = prefs.getCharPref("aidaac.servlet.select");
    var server_url = prefs.getCharPref("aidaac.kb.server.url");
    var repository = prefs.getCharPref("aidaac.kb.repository");
    var username = prefs.getCharPref("aidaac.kb.username");
    var password = prefs.getCharPref("aidaac.kb.password");
    var limit = 5;
    var query = "select distinct D, D, D, SC, SUB, C, P, P from " + 
            "{D} P {SC}, {D} rdf:type {<http://www.vl-e.nl/aid#Document>}, {SC} SUB {C} " +
            "where P = <" + property.value + "> " + 
            "and ( C = <" + concept.value + "> and " + 
            "( SUB = <http://www.w3.org/2000/01/rdf-schema#subClassOf> or " +
              "SUB = <http://www.w3.org/2004/02/skos/core#broader> ) ) or " +
            "SC = <" + concept.value + "> " +
            "limit " + limit + 
            " union " +
            "select distinct D, D, D, SC, SUB, C, P, Q from " + 
            "{D} P {SC}, {D} rdf:type {<http://www.vl-e.nl/aid#Document>}, {SC} SUB {C}, {P} rdf:type {Q} " +
            "where Q = <" + property.value + "> " + 
            "and ( C = <" + concept.value + "> and " + 
            "( SUB = <http://www.w3.org/2000/01/rdf-schema#subClassOf> or " +
              "SUB = <http://www.w3.org/2004/02/skos/core#broader> ) ) or " +
            "SC = <" + concept.value + "> " +
            "limit " + limit;
    var search_button = document.getElementById("aidaac-find-documents-button");
    search_button.disabled = true;
    load_doc.open = true;
    getSuggestions(servlet,server_url,repository,username,password,"aidaac-load-document-menu",query,limit);
    search_button.disabled = false;
}

function storeAnnotation() {
    if (document.getElementById("aidaac-property").value == "") {
        alert("Please fill in which property you want to describe of this document.");
        document.getElementById("aidaac-property").focus();
        return;
    }
    if (document.getElementById("aidaac-concept").value == "") {
        alert("Please fill in which concept describes the property of this document.");
        document.getElementById("aidaac-concept").focus();
        return;
    }

    var servlet = prefs.getCharPref("aidaac.servlet.addrdf");

    var server_url = prefs.getCharPref("aidaac.kb.server.url");
    var repository = prefs.getCharPref("aidaac.kb.repository");
    var username = prefs.getCharPref("aidaac.kb.username");
    var password = prefs.getCharPref("aidaac.kb.password");

    var ns_def = prefs.getCharPref("aidaac.kb.namespace.default");

    var annotate_button = document.getElementById("aidaac-annotate-button");
    annotate_button.disabled = true;

    prependDefaultNamespace("aidaac-property");
    prependDefaultNamespace("aidaac-concept");

    var subject = window._content.document.location.href;
    var predicate = document.getElementById("aidaac-property").value;
    var object = document.getElementById("aidaac-concept").value;

    var subj_ns_id = subject.split(/([\/\#\?])/);
    var subj_ns = "";
    for (i=0;i<subj_ns_id.length-1;i++) subj_ns = subj_ns + subj_ns_id[i];
    
    var pred_ns_id = predicate.split(/([\/\#\?])/);
    var pred_ns = "";
    for (i=0;i<pred_ns_id.length-1;i++) pred_ns = pred_ns + pred_ns_id[i];

    var obj_ns_id = object.split(/([\/\#\?])/);
    var obj_ns = "";
    for (i=0;i<obj_ns_id.length-1;i++) obj_ns = obj_ns + obj_ns_id[i];

    var snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rdf:RDF\n" +
        "       xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
        "       xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
        "       xmlns:ns1=\"" + pred_ns + "\"\n" +
        "       xmlns:ns2=\"" + obj_ns + "\">\n" +
        "<rdf:Description rdf:about=\"" + subj_ns + escape(subj_ns_id[subj_ns_id.length-1]) + "\">\n" +
        "        <ns1:" + pred_ns_id[pred_ns_id.length-1] + " rdf:resource=\"" + object + "\"/>\n" +
        "        <rdf:type rdf:resource=\"http://www.vl-e.nl/aid#Document\"/>\n" +
        "</rdf:Description>\n" +
        ((ns_def == obj_ns) ?
            "<rdf:Description rdf:about=\"" + object + "\">\n" +
            "        <rdf:type rdf:resource=\"http://www.w3.org/2000/01/rdf-schema#Class\"/>\n" +
            "        <rdfs:label>" + obj_ns_id[obj_ns_id.length-1] + "</rdfs:label>\n" +
            "</rdf:Description>\n" : "") +
        "</rdf:RDF>\n";

    var ssreq;
    ssreq = new XMLHttpRequest();
    ssreq.onreadystatechange = storeAnnotationReady;
    ssreq.open("GET",
        servlet + 
            "?" + "server_url=" + escape(server_url) + 
            "&" + "repository=" + escape(repository) + 
            "&" + "username=" + escape(username) + 
            "&" + "password=" + escape(password) +
            "&" + "rdf_format=rdfxml" +
            "&" + "data_uri=" + escape((ns_def.split(/\#/))[0]) +
            "&" + "data=" + escape(snippet),
        true);
    ssreq.send(null);

    function storeAnnotationReady() {
        if (ssreq.readyState == 4) {
            if (ssreq.status == 200) {
                if (ssreq.responseXML) {
                } else if (ssreq.responseText) {
                } else {
                    alert("no response XML or Text available");
                }
                annotate_button.disabled = false;
            } else {
                alert(ssreq.statusText);
            }
        }
    }
}

function lookupNamespace(ns) {
    // FIXME: optimize this... for each lookup we have to parse the entire string of pairs
    abbr_pref = prefs.getCharPref("aidaac.kb.namespace.abbreviations");
    var lines = abbr_pref.split(/\s*\n\s*/);
    for (i=0;i<lines.length;i++) {
        var abbr = lines[i].split(/\s+/);
        if (ns.match(abbr[1])) {
            return abbr[0];
        }
    }
    return ns;
}

function prependDefaultNamespace(id) {
    var item = document.getElementById(id);
    var ns_def = prefs.getCharPref("aidaac.kb.namespace.default");
    if (!item.value.match(/^http:/) && !item.value.match(/^\s*$/)) {
        item.value = ns_def + item.value;
    }
}

function exploreAnnotation() {
    var servlet = prefs.getCharPref("aidaac.servlet.select");

    var server_url = prefs.getCharPref("aidaac.kb.server.url");
    var repository = prefs.getCharPref("aidaac.kb.repository");
    var username = prefs.getCharPref("aidaac.kb.username");
    var password = prefs.getCharPref("aidaac.kb.password");

    var explore_button = document.getElementById("aidaac-explore-button");
    explore_button.disabled = true;

    prependDefaultNamespace("aidaac-property");
    prependDefaultNamespace("aidaac-concept");

    var predicate = document.getElementById("aidaac-property").value;
    var object = document.getElementById("aidaac-concept").value;

    var query = "select Subject, Predicate, Object " + 
        "from {Subject} Predicate {Object}, {Subject} rdf:type {<http://www.vl-e.nl/aid#Document>} " + 
        "where Predicate like \"" + predicate + "*\" ignore case and " + 
        "Object like \"" + object + "*\" ignore case";

    var ssreq;
    ssreq = new XMLHttpRequest();
    ssreq.onreadystatechange = exploreAnnotationReady;
    ssreq.open("GET",
            servlet + 
            "?" + "server_url=" + escape(server_url) + 
            "&" + "repository=" + escape(repository) + 
            "&" + "username=" + escape(username) + 
            "&" + "password=" + escape(password) +
            "&" + "query_language=serql" +
            "&" + "html=1" +
            "&" + "query=" + escape(query),
        true);
    ssreq.send(null);

    function exploreAnnotationReady() {
        if (ssreq.readyState == 4) {
            if (ssreq.status == 200) {
                if (ssreq.responseXML) {
                    alert(ssreq.responseXML);
                } else if (ssreq.responseText) {
                    window._content.document.body.innerHTML = ssreq.responseText;
                    explore_button.disabled = false;
                } else {
                    alert("no response XML or Text available");
                }
            } else {
                alert(ssreq.statusText);
            }
        } else {
        }
    }

}

