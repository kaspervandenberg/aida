var server_url = "";
var repository = "";
var username = "";
var password = "";
var model = "";
var session = "";

var rars_servlet = "http://localhost:8080/OOO/AddTagSVL";
var gmd_servlet = "http://localhost:8080/OOO/GetTagLocationsSVL";

var context_default_ns = "http://www.vl-e.nl/aid/ns/annotation/context#";

var limit = 5;

var ssreq;
var sslabreq;

function storeStatement(subj,pred,obj) {
    try {
        netscape.security.PrivilegeManager.enablePrivilege('UniversalFileRead UniversalBrowserRead');
    } catch (err) {
        alert(err); 
        return;
    }
    ssreq = new XMLHttpRequest();
    ssreq.onreadystatechange = storeStatementReady;
    var docloc = subj.split("#");
    var doc = docloc[0];
    var loc = docloc[1];
    if (loc != null) loc = URLEncode(loc);
    alert("storing " + doc + " | " + loc + " | " + pred + " | " + obj + " | " + session);
    ssreq.open("GET",
        rars_servlet + 
            "?" + "server_url=" + URLEncode(server_url) + 
            "&" + "repository=" + URLEncode(repository) + 
            "&" + "username=" + URLEncode(username) + 
            "&" + "password=" + URLEncode(password) +            
            "&" + "model=" + URLEncode(model) +            
            "&" + "session=" + URLEncode(session) +            
            "&" + "document=" + URLEncode(doc) +
            "&" + "location=" + loc +
            "&" + "property=" + URLEncode(pred) +
            "&" + "concept=" + URLEncode(obj), 
        true);
    ssreq.send(null);
}

function storeStatementReady() {
    if (ssreq.readyState == 4) {
        if (ssreq.status == 200) {
            if (ssreq.responseXML) {
            } else if (ssreq.responseText) {
                alert(ssreq.responseText);
            } else {
                alert("no response XML or Text available");
            }
        } else {
            alert(ssreq.statusText);
        }
    }
}

function storeStatementLabelReady() {
    if (sslabreq.readyState == 4) {
        if (sslabreq.status == 200) {
            if (sslabreq.responseXML) {
            } else if (sslabreq.responseText) {
                alert(sslabreq.responseText);
            } else {
                alert("no response XML or Text available");
            }
        } else {
            alert(sslabreq.statusText);
        }
    }
}

var sdreq;

function getMatchingDocuments(id) {
    var property = document.annotate.property_hidden.value;
    if (property == null || property == "") property = document.annotate.property.value;
    if (property == "") return;
    property = createNiceUri(property,"property");
    var concept = document.annotate.concept_hidden.value;
    if (concept == null || concept == "") concept = document.annotate.concept.value;
    if (concept == null) return;
    concept = createNiceUri(concept,"concept");
    try {
        netscape.security.PrivilegeManager.enablePrivilege('UniversalFileRead UniversalBrowserRead');
    } catch (err) {
        alert(err); 
        return;
    }
    sdreq = new XMLHttpRequest();
    sdreq.onreadystatechange = getMatchingDocumentsReady;
    sdreq.open("GET",
        gmd_servlet + 
            "?" + "server_url=" + URLEncode(server_url) + 
            "&" + "repository=" + URLEncode(repository) + 
            "&" + "username=" + URLEncode(username) + 
            "&" + "password=" + URLEncode(password) +
            "&" + "id=" + URLEncode(id) +
            "&" + "model=" + URLEncode(model) +            
            "&" + "session=" + URLEncode(session) +            
            "&" + "property=" + URLEncode(property) +
            "&" + "concept=" + URLEncode(concept), 
        true);
    sdreq.send(null);
}

function getMatchingDocumentsReady() {
    if (sdreq.readyState == 4) {
        if (sdreq.status == 200) {
            if (sdreq.responseXML) {
                document.query.matching.options.length = 0;
                var xmlDoc = sdreq.responseXML.documentElement;
                var entries = xmlDoc.getElementsByTagName("entry");
                for (i=0;i<entries.length;i++) {
                    var text = entries[i].childNodes[0].firstChild.nodeValue;
                    var value = entries[i].childNodes[1].firstChild.nodeValue;
                    document.query.matching.options[i] = new Option(text,value);
                }
            } else if (sdreq.responseText) {
                alert("no");
            } else {
                alert("no response XML or Text available");
            }
        } else {
            alert(sdreq.statusText);
        }
    }
}

