var server_url = "";
var repository = "";
var username = "";
var password = "";

var rars_servlet = "http://localhost:8080/Services/RepositoryAddRdfStatementSVL";
var rsqt_servlet = "http://localhost:8080/Services/RepositorySelectQueryTableSVL";
var gmd_servlet = "http://localhost:8080/Services/GetMatchingDocumentsSVL";

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
    alert("storing [ " + subj + " | " + pred + " | " + obj + " ]");
    ssreq = new XMLHttpRequest();
    ssreq.onreadystatechange = storeStatementReady;
    ssreq.open("GET",
        rars_servlet + 
            "?" + "server_url=" + URLEncode(server_url) + 
            "&" + "repository=" + URLEncode(repository) + 
            "&" + "username=" + URLEncode(username) + 
            "&" + "password=" + URLEncode(password) +
            "&" + "subject=" + URLEncode(subj) +
            "&" + "predicate=" + URLEncode(pred) +
            "&" + "object=" + URLEncode(obj), 
        true);
    ssreq.send(null);
    if (obj.match(concept_default_ns) || obj.match(property_default_ns)) {
        var p = "http://www.w3.org/2000/01/rdf-schema#label";
        var o = obj;
        o = o.replace(/^.*\#/,"");
        if (o.match(/^(http|https|file|srb):\/\//)) {
            o = o.replace(/^.*\//,"");
        }
        alert("storing [ " + obj + " | " + p + " | " + o + " ]");
        sslabreq = new XMLHttpRequest();
        sslabreq.onreadystatechange = storeStatementLabelReady;
        sslabreq.open("GET",
            rars_servlet + 
                "?" + "server_url=" + URLEncode(server_url) + 
                "&" + "repository=" + URLEncode(repository) + 
                "&" + "username=" + URLEncode(username) + 
                "&" + "password=" + URLEncode(password) +
                "&" + "subject=" + URLEncode(obj) +
                "&" + "predicate=" + URLEncode(p) +
                "&" + "object=" + URLEncode(o), 
            true);
        sslabreq.send(null);
    }
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
            "&" + "property=" + URLEncode(property) +
            "&" + "concept=" + URLEncode(concept), 
        true);
    sdreq.send(null);
}

function getMatchingDocumentsReady() {
    if (sdreq.readyState == 4) {
        if (sdreq.status == 200) {
            if (sdreq.responseXML) {
                document.query.matching.options.length = 0
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

