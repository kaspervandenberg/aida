
var browser = new Object();

browser.default_left_ns = ''
browser.default_right_ns = ''

browser.refresh_side = 'left';

browser.server_info = new Object();
browser.mapping_server_info = new Object();

// used to look up the label of a concept using the ThesaurusBrowser servlet.
browser.mapping_term = "";

browser.left_request = new Object();
browser.right_request = new Object();

browser.queued_request = new Object();

browser.left_history = new Array();
browser.right_history = new Array();
browser.max_history_size = 5;

browser.username = "anonymous";
browser.password = "nabalzbhf"

browser.result_display_timeout = null;

try {
   netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
} catch (err) {
   if (netscape) {
      alert("This program requires requires you to entrust it with the extra privilege to load data from another computer than the one this program runs on. This is usually done with certificates, but since this is experimental software we would like to request the privileges without a certificate. This requires you to do the following:\n\nNavigate to \"about:config\" using the URL bar.\nSearch for the property named \"signed.applets.codebase_principal_support\" by typing \"codebase\" in the entry box and set it to \"true\".\n\nThis allows the program to ask for your permission.\n\nWillem van Hage <wrvhage@few.vu.nl>");
   } else {
      alert("This experimental software currently only runs in Mozilla browsers such as Firefox.\n\nWillem van Hage <wrvhage@few.vu.nl>");
   }
}

browser.loadConcept = function(url,params,no_history) {
   var req;
   req = new XMLHttpRequest();
   req.onreadystatechange = loadConceptReady;
   req.open("POST",url + browser.myescape(params),true);
   req.send(null);
   
// IF ANYTHING GOES WRONG WITH THE SIDE THINGS DISPLAY ON, COMMENT THIS
   var side = browser.refresh_side;

   browser.startActivityAnimation(side);

   var m;
   m = params.match(/mapping_server_url=([^&]+)/);
   if (m != null) browser.mapping_server_info.server_url = m[1];
      m = params.match(/mapping_repository=([^&]+)/);
   if (m != null) browser.mapping_server_info.repository = m[1];
      m = params.match(/mapping_username=([^&]+)/);
   if (m != null) browser.mapping_server_info.username = m[1];
      m = params.match(/mapping_password=([^&]+)/);
   if (m != null) browser.mapping_server_info.password = m[1];
      m = params.match(/term=([^&]+)/);
   if (m != null) browser.mapping_term = m[1];

      function loadConceptReady() {
         if (req.readyState == 4) {
            if (req.status == 200) {
               if (req.responseXML) {                   
                  var xmlDoc = req.responseXML.documentElement;

// IF ANYTHING GOES WRONG WITH THE SIDE THINGS DISPLAY ON, UNCOMMENT THIS
//                  var side = browser.refresh_side;
                  if (side == 'left') {
                     browser.left_request.url = url;
                     browser.left_request.params = params;
                  } else if (side == 'right') {
                     browser.right_request.url = url;
                     browser.right_request.params = params;
                  }

                  browser.server_info.server_url = xmlDoc.getElementsByTagName('server_url')[0].childNodes[0].nodeValue
                  browser.server_info.repository = xmlDoc.getElementsByTagName('repository')[0].childNodes[0].nodeValue
                  browser.server_info.username = xmlDoc.getElementsByTagName('username')[0].childNodes[0].nodeValue
                  browser.server_info.password = xmlDoc.getElementsByTagName('password')[0].childNodes[0].nodeValue

                  var ns = xmlDoc.getElementsByTagName('ns')[0].childNodes[0].nodeValue;
                  var uri = xmlDoc.getElementsByTagName('uri')[0].childNodes[0].nodeValue;

                  // create the replacement viewer div
                  var div = document.createElement('div');

                  var history_stub = document.createElement('div');
                  history_stub.setAttribute('id',side + '_history');
                  div.appendChild(history_stub);

                  var search = browser.addSearchBox(div,ns,side);

                  // create the term label
                  var pref_header = document.createElement('div');
                  var label = browser.getLabel(xmlDoc);
                  var pref_header_a = document.createElement("a");
                  pref_header_a.setAttribute('href',uri);
                  pref_header_a.setAttribute('class','uri');
                  var pref_header_txt = document.createTextNode(label[0]);
                  pref_header.setAttribute('class',label[1]);
                  pref_header_a.appendChild(pref_header_txt);
                  pref_header.appendChild(pref_header_a);
                  div.appendChild(pref_header);

                  var ns_label = document.createElement('div');
                  pref_header.appendChild(ns_label);
                  ns_label.setAttribute('class','ns_label');
                  search.elements[4].setAttribute('value',ns);
                  var ns_label_txt = document.createTextNode(ns);
                  ns_label.appendChild(ns_label_txt);

                  var args = params.split(/[\?\&]/);
                  var term = "";
                  for (i=0;i<args.length;i++) {
                     var m = args[i].match(/term=(.*)/);
                     if (m != null) {
                        term = m[1];
                     }
                  }
                  pref_header.setAttribute('ID',term);

                  // create the alternative term labels
                  var alt = document.createElement('div');
                  alt.setAttribute('class','altLabel');
                  div.appendChild(alt);
                  var alternates = xmlDoc.getElementsByTagName('altLabel');
                  for (i=0;i<alternates.length;i++) {
                     var alt_header = document.createElement('div');
                     alt.appendChild(alt_header);
                     var alt_header_txt = document.createTextNode(alternates[i].childNodes[0].nodeValue);
                     alt_header.appendChild(alt_header_txt);
                  }

                  // display definitions
                  var notes = xmlDoc.getElementsByTagName('definition');
                  for (i=0;i<notes.length;i++) {
                     var ddiv = document.createElement('div');
                     ddiv.setAttribute('class','definition');
                     var header = document.createElement('div');
                     header.setAttribute('class','definition_header');
                     header.appendChild(document.createTextNode('definition'));
                     ddiv.appendChild(header);
                     ddiv.appendChild(document.createTextNode(notes[i].childNodes[0].nodeValue));
                     div.appendChild(ddiv);
                  }

                  // display notes
                  var notes = xmlDoc.getElementsByTagName('note');
                  for (i=0;i<notes.length;i++) {
                     var ndiv = document.createElement('div');
                     ndiv.setAttribute('class','note');
                     var header = document.createElement('div');
                     header.setAttribute('class','note_header');
                     header.appendChild(document.createTextNode('note'));
                     ndiv.appendChild(header);
                     ndiv.appendChild(document.createTextNode(notes[i].childNodes[0].nodeValue));
                     div.appendChild(ndiv);
                  }

                  function displayRelation(rel) {
                     if (xmlDoc.getElementsByTagName(rel).length > 0) {
                            // create the relations
                        var rdiv = document.createElement('div');
                        rdiv.setAttribute('class',rel);
                        div.appendChild(rdiv);
                        var header = document.createElement('div');
                        rdiv.appendChild(header);
                        header.setAttribute('class','relation');
                        var label;
                        if (rel == 'scheme') {
                           label = "in concept scheme";
                        } else if (rel == 'member') {
                           label = "has collection member";
                        } else if (rel == 'collection') {
                           label = "member of collection";
                        } else {
                           label = rel + ' terms';
                        }
                        var header_txt = document.createTextNode(label);
                        header.appendChild(header_txt);
                        var ul = document.createElement('ul');
                        rdiv.appendChild(ul);
                        var relation = xmlDoc.getElementsByTagName(rel);                    
                        for (i=0;i<relation.length;i++) {
                           var li = document.createElement('li');
                           ul.appendChild(li);
                           var a = document.createElement('a');
                           var href = relation[i].childNodes[0].childNodes[0].nodeValue;
                           href = href.replace("javascript:","javascript:browser.refresh_side='" + side +"';");
                           a.setAttribute('href',href);
                           var linkTxt = document.createTextNode(relation[i].childNodes[1].childNodes[0].nodeValue);   
                           a.appendChild(linkTxt);
                           li.appendChild(a);
                           if (relation[i].childNodes[2] != null &&
                               relation[i].childNodes[2].childNodes[0] != null) {
                              var nrnt = relation[i].childNodes[2].childNodes[0].nodeValue;
                              var nrnt_div = document.createElement('div');
                              nrnt_div.setAttribute('class','number_of_narrower_terms');
                              nrnt_div.appendChild(document.createTextNode('(' + nrnt + ')'));
                              li.appendChild(nrnt_div);
                           }
                        }
                     }
                  }
                  displayRelation('scheme'); // this is a concept that is in a scheme
                  displayRelation('collection'); // this is a concept that is a member of a collection
                  displayRelation('broader');
                  displayRelation('member'); // this is a collection that has a member
                  displayRelation('narrower');
                  displayRelation('related');                  
               

                  // Mappings should always appear at the position of this stub,
                  // no matter how long it takes to look them up.
                  var mappings_stub = document.createElement('div');
                  div.appendChild(mappings_stub);
                  mappings_stub.setAttribute('id',side + '_mappings');

                  var mappings = [];                    
                  var mapping_relations = [];

                  var mapping_params = 
                     browser.serverInfoToParams(browser.mapping_server_info) + "\n" +
                     "&term=" + browser.mapping_term + "&ns=" + ns + 
                     "&mapping=1";

                  function loadMappingsReady() {
                     if (mreq.readyState == 4) {
                        if (mreq.status == 200) {
                           if (mreq.responseXML) {
                              var mapping_xmlDoc = mreq.responseXML.documentElement;
                              function displayMatch(rel) {
                                 if (mapping_xmlDoc.getElementsByTagName(rel).length > 0) {
                                    // create the match mappings
                                    var mdiv = document.createElement('div');
                                    mdiv.setAttribute('class',rel);
                                    document.getElementById(side + '_mappings').appendChild(mdiv);
                                    var header = document.createElement('div');
                                    mdiv.appendChild(header);
                                    header.setAttribute('class','relation');
                                    var header_txt = document.createTextNode(rel + ' terms');
                                    header.appendChild(header_txt);
                                    var ul = document.createElement('ul');
                                    mdiv.appendChild(ul);
                                    var done = new Object(); // remove duplicates (necessary due to lack of DISTINCT in RQL)
                                    var match = mapping_xmlDoc.getElementsByTagName(rel);
                                    for (i=0;i<match.length;i++) {
                                       if (!done[match[i].childNodes[0].childNodes[0].nodeValue]) {
                                          done[match[i].childNodes[0].childNodes[0].nodeValue] = true;
                                          var li = document.createElement('li');
                                          var a = document.createElement('a');
                                          var href = match[i].childNodes[0].childNodes[0].nodeValue;
                                          var term_uri = href.split(/[\?\&]/)[1].split(/=/)[1];
                                          var term_ns = term_uri.replace(/%23.*/,"");
                                          mappings.push(term_uri); // remember which mappings there are
                                          mapping_relations.push(browser.backOrForth(rel,side));
                                          var opposite_side = ((side == 'left') ? 'right' : 'left');
                                          href = href.replace("javascript:","javascript:browser.refresh_side='" + opposite_side +"';");
                                          href = href.replace(/server_url=.*?password=[^\&\']+/,browser.serverInfoToParams(browser.server_info));
                                          a.setAttribute('href',href);                            

                                          var mlreq;
                                          var label_lookup_url = url;
                                          var label_lookup_params = params;
                                          label_lookup_params = label_lookup_params.replace(/term=[^\&\']+/,"term=" + term_uri) + "&label_lookup=1" + "&ns=" + term_ns;
                                          function lookupMappingLabelReady() {
                                             if (mlreq.readyState == 4) {
                                                if (mlreq.status == 200) {
                                                   if (mlreq.responseXML) {
                                                      var label_xmlDoc = mlreq.responseXML.documentElement;
                                                      var label = browser.getLabel(label_xmlDoc);
                                                      if (//label[1] == "prefLabel" &&  // only show mappings to skos:Concepts
                                                          a.childNodes.length == 0) { // sometimes this lookup generates multiple responses, odd
                                                         var linkTxt = document.createTextNode(label[0]);
                                                         a.appendChild(linkTxt);
                                                         li.appendChild(a);
                                                         var mapping_ns = document.createElement('div');
                                                         mapping_ns.setAttribute('class','mapping_ns_label');
                                                         li.appendChild(mapping_ns);
                                                         var mapping_ns_txt = document.createTextNode(" " + browser.myunescape(term_ns));                               
                                                         mapping_ns.appendChild(mapping_ns_txt);
                                                         ul.appendChild(li);
                                                      }
                                                   } else {
                                                   }
                                                } else {
                                                }
                                             }
                                          }
                                          mlreq = new XMLHttpRequest();
                                          mlreq.onreadystatechange = lookupMappingLabelReady;
                                          mlreq.open("POST",label_lookup_url + browser.myescape(label_lookup_params),true);
                                          mlreq.send(null);

                                       }
                                    }
                                 }
                              }
                              displayMatch('exactMatch');
                              displayMatch('broadMatch');
                              displayMatch('narrowMatch');
                              displayMatch('partMatch');
                              displayMatch('wholeMatch');
                              displayMatch('relatedMatch');
                              displayMatch('disjointMatch');

                              left = document.getElementById('left');
                              right = document.getElementById('right');

                              if (browser.gui_mapping_enabled) {
                                 // check if a mapping already exists and offer to change or remove the existing mapping
                                 var matching_mapping = false;
                                 for (i=0;i<mappings.length;i++) {
                                    if (document.getElementById(browser.myunescape(mappings[i])) != null) {
                                       browser.addChangeDeleteMappingForm(mapping_relations[i]);
                                       matching_mapping = true;
                                    }
                                 }
                                 
                                 var left_ns = null;
                                 var right_ns = null;
                                 if (left.childNodes[2] && left.childNodes[2].childNodes[1]) {
                                    left_ns = left.childNodes[2].childNodes[1].textContent;
                                 }
                                 if (right.childNodes[3] && right.childNodes[3].childNodes[1]) {
                                    right_ns = right.childNodes[3].childNodes[1].textContent;
                                 }                    
                                 
                                 var r = browser.getMappingRelationSuggestions();
                                 
                              // offer to add a new one
                                 if (matching_mapping == false && left_ns != null && right_ns != null) {
                                    if(left.childNodes[2] && right.childNodes[3] &&
                                       left.childNodes[2].getAttribute('class') == 'prefLabel' &&
                                       right.childNodes[3].getAttribute('class') == 'prefLabel') {
                                          
                                          browser.addAssertMappingForm();
                                          
                                       } else {
                                          browser.emptyMappingDiv();
                                       }
                                 } else if (matching_mapping == false) {
                                    browser.emptyMappingDiv();
                                 }
                                 
                              } // end of if browser.gui_mapping_enabled
                              
                           } else if (mreq.responseText) {
                           }
                        } else {
                        }
                     } else {
                     }
                  }
                  var mreq;
                  mreq = new XMLHttpRequest();
                  mreq.onreadystatechange = loadMappingsReady;
                  mreq.open("POST",url + browser.myescape(mapping_params));               
                  mreq.send(null);

                  // EXTRA, insert links to indexed terms
                  if (browser.gui_dc_subject_docs_enabled) {
                     browser.insert_dc_subject_docs(div,side,uri);
                  }

                  // EXTRA, insert extra features such as Agris/Caris hits when the namespace is FAO
                  browser.insert_extra_features(div,side);

                  // EXTRA, like specific colors for specific namespaces
                  browser.insert_extra_style(div,ns);

                  // EXTRA, update the TODO list
                  if (browser.gui_todo_enabled) {
                     browser.showToDo(document.getElementById('todo'),'left',url,browser.username + "_todo.rdf",ns,uri);
                  }

                    // target div elements
                  var left = document.getElementById('left');
                  var right = document.getElementById('right');

                  if (side == 'left') {
                     div.setAttribute('ID','left');
                     search.setAttribute('action','javascript:browser.performSearch("left");');
                     left.parentNode.replaceChild(div,left);
                  } else if (side == 'right') {
                     div.setAttribute('ID','right');
                     search.setAttribute('action','javascript:browser.performSearch("right");');

                        // close button;
                     var close = document.createElement('div');
                     div.insertBefore(close,div.firstChild);
                     close.setAttribute('class','close');
                     var close_a = document.createElement('a');
                     close.appendChild(close_a);
                     var close_txt = document.createTextNode('close this view');
                     close_a.appendChild(close_txt);
                     close_a.setAttribute('href','javascript:browser.closeView()');
                     right.parentNode.replaceChild(div,right);
                     
                  } else { 
                  }
                  

                  if (browser.queued_request.url != null && browser.queued_request.params != null) {
                     var q = browser.queued_request.url;
                     var p = browser.queued_request.params;
                     browser.refresh_side = browser.queued_request.refresh_side;
                     browser.queued_request.url = null;
                     browser.queued_request.params = null;
                     browser.queued_request.refresh_side = null;
                     browser.loadConcept(q,p);
                  }
                  
                  div.replaceChild(browser.createHistoryDiv(side),history_stub);
                  if (no_history == null || no_history == false) {
                     browser.addToHistory(side,browser.getLabel(xmlDoc)[0],url,params);
                  }

                  browser.stopActivityAnimation(side);

               } else if (req.responseText) {
                  alert("response not of type XML");
               } else {
                  alert("no response XML or Text available");
               }
            } else {
               alert(req.statusText);
            }
         } else {
         }
      }   
}


browser.showTopConcepts = function(url,params) {
   var req;
   req = new XMLHttpRequest();
   req.onreadystatechange = showTopConceptsReady;
   req.open("POST",url + browser.myescape(params),true);
   req.send(null);

   // IF ANYTHING GOES WRONG WITH THE SIDE THINGS DISPLAY ON, COMMENT THIS
   var side = browser.refresh_side;

   browser.startActivityAnimation(side);
   
   var m;
   m = params.match(/mapping_server_url=([^&]+)/);
   if (m != null) browser.mapping_server_info.server_url = m[1];
      m = params.match(/mapping_repository=([^&]+)/);
   if (m != null) browser.mapping_server_info.repository = m[1];
      m = params.match(/mapping_username=([^&]+)/);
   if (m != null) browser.mapping_server_info.username = m[1];
      m = params.match(/mapping_password=([^&]+)/);
   if (m != null) browser.mapping_server_info.password = m[1];
      m = params.match(/term=([^&]+)/);
   if (m != null) browser.mapping_term = m[1];

      function showTopConceptsReady() {
         if (req.readyState == 4) {
            if (req.status == 200) {
               if (req.responseXML) {                   
                  var xmlDoc = req.responseXML.documentElement;

                  // IF ANYTHING GOES WRONG WITH THE SIDE THINGS DISPLAY ON, UNCOMMENT THIS
//                  var side = browser.refresh_side;
                  
                  browser.server_info.server_url = xmlDoc.getElementsByTagName('server_url')[0].childNodes[0].nodeValue
                  browser.server_info.repository = xmlDoc.getElementsByTagName('repository')[0].childNodes[0].nodeValue
                  browser.server_info.username = xmlDoc.getElementsByTagName('username')[0].childNodes[0].nodeValue
                  browser.server_info.password = xmlDoc.getElementsByTagName('password')[0].childNodes[0].nodeValue

                  var ns = xmlDoc.getElementsByTagName('ns')[0].childNodes[0].nodeValue

                  // target div elements
                  var left = document.getElementById('left');
                  var right = document.getElementById('right');

                  // create the replacement viewer div
                  var div = document.createElement('div');
                  
                  var history_stub = document.createElement('div');
                  div.appendChild(history_stub);

                  var search = browser.addSearchBox(div,ns,side);

                  if (xmlDoc.getElementsByTagName('concept_scheme').length > 0) {
                     // create the scheme terms
                     var scheme_div = document.createElement('div');
                     scheme_div.setAttribute('class','scheme');
                     div.appendChild(scheme_div);
                     var scheme_header = document.createElement('div');
                     scheme_div.appendChild(scheme_header);
                     scheme_header.setAttribute('class','relation');
                     var scheme_header_txt = document.createTextNode('concept schemes');
                     scheme_header.appendChild(scheme_header_txt);
                     var scheme_ul = document.createElement('ul');
                     scheme_div.appendChild(scheme_ul);
                     var scheme = xmlDoc.getElementsByTagName('concept_scheme');                    
                     for (i=0;i<scheme.length;i++) {
                        var li = document.createElement('li');
                        scheme_ul.appendChild(li);
                        var scheme_name = document.createElement('div');
                        li.appendChild(scheme_name);
                        scheme_name.setAttribute('class','concept_scheme');
                        var scheme_link = document.createElement('a');
                        var href = scheme[i].childNodes[0].childNodes[0].nodeValue;
                        var term_uri = href.split(/[\?\&]/)[1].split(/=/)[1];
                        var term_ns = term_uri.replace(/%23.*/,"");
                        href = href.replace("javascript:","javascript:browser.refresh_side='" + side +"';");
                        scheme_link.setAttribute('href',href);
                        scheme_name.appendChild(scheme_link);
                        var scheme_name_txt = document.createTextNode(scheme[i].childNodes[1].childNodes[0].nodeValue);   
                        scheme_link.appendChild(scheme_name_txt);
                        if (scheme[i].childNodes[2].childNodes[0] != null) {
                           var nrnt = scheme[i].childNodes[2].childNodes[0].nodeValue;
                           var nrnt_div = document.createElement('div');
                           nrnt_div.setAttribute('class','number_of_narrower_terms');
                           nrnt_div.appendChild(document.createTextNode('(' + nrnt + ')'));
                           li.appendChild(nrnt_div);
                        }
                        var scheme_ns = document.createElement('div');
                        scheme_ns.setAttribute('class','scheme_ns_label');
                        var scheme_ns_txt = document.createTextNode(" " + browser.myunescape(term_ns));
                        scheme_ns.appendChild(scheme_ns_txt);
                        li.appendChild(scheme_ns);
                     }
                  }
                  if (xmlDoc.getElementsByTagName('top_concepts').length > 0) {
                     var top_concept_div = document.createElement('div');
                     top_concept_div.setAttribute('class','top_concept');
                     div.appendChild(top_concept_div);
                     var top_concept_header = document.createElement('div');
                     top_concept_div.appendChild(top_concept_header);
                     top_concept_header.setAttribute('class','relation');
                     var top_concept_header_txt = document.createTextNode('top concepts');
                     top_concept_header.appendChild(top_concept_header_txt);

                     var concept_scheme_name_header = document.createElement('div');
                     top_concept_div.appendChild(concept_scheme_name_header);
                     concept_scheme_name_header.setAttribute('class','concept_scheme');
                     var concept_scheme_name_header_txt = document.createTextNode(xmlDoc.getElementsByTagName('top_concepts')[0].getAttribute('concept_scheme_name'));
                     concept_scheme_name_header.appendChild(concept_scheme_name_header_txt);

                     var top_concept_ul = document.createElement('ul');
                     top_concept_div.appendChild(top_concept_ul);
                     var top_concept = xmlDoc.getElementsByTagName('top_concept');
                     for (i=1;i<top_concept.length;i++) {                           
                        var li = document.createElement('li');
                        top_concept_ul.appendChild(li);
                        var a = document.createElement('a');
                        var href = top_concept[i].childNodes[0].childNodes[0].nodeValue;
                        href = href.replace("javascript:","javascript:browser.refresh_side='" + side +"';");
                        var term_uri = href.split(/[\?\&]/)[1].split(/=/)[1];
                        var term_ns = term_uri.replace(/%23.*/,"");
                        a.setAttribute('href',href);
                        var linkTxt = document.createTextNode(top_concept[i].childNodes[1].childNodes[0].nodeValue);   
                        a.appendChild(linkTxt);
                        li.appendChild(a);
                        if (top_concept[i].childNodes[2].childNodes[0] != null) {
                           var nrnt = top_concept[i].childNodes[2].childNodes[0].nodeValue;
                           var nrnt_div = document.createElement('div');
                           nrnt_div.setAttribute('class','number_of_narrower_terms');
                           nrnt_div.appendChild(document.createTextNode('(' + nrnt + ')'));
                           li.appendChild(nrnt_div);
                        }
                        var top_concept_ns = document.createElement('div');
                        top_concept_ns.setAttribute('class','top_concept_ns_label');
                        var top_concept_ns_txt = document.createTextNode(" " + browser.myunescape(term_ns));
                        top_concept_ns.appendChild(top_concept_ns_txt);
                        li.appendChild(top_concept_ns);
                     }
                  }

                  if (side == 'left') {
                     div.setAttribute('ID','left');
                     search.setAttribute('action','javascript:browser.performSearch("left");');
                     left.parentNode.replaceChild(div,left);
                  } else if (side == 'right') {
                     div.setAttribute('ID','right');
                     search.setAttribute('action','javascript:browser.performSearch("right");');

                        // close button;
                     var close = document.createElement('div');
                     div.insertBefore(close,div.firstChild);
                     close.setAttribute('class','close');
                     var close_a = document.createElement('a');
                     close.appendChild(close_a);
                     var close_txt = document.createTextNode('close this view');
                     close_a.appendChild(close_txt);
                     close_a.setAttribute('href','javascript:browser.closeView()');
                     right.parentNode.replaceChild(div,right);
                     
                  } else { 
                  }
               
                  browser.insert_extra_style(div,ns);
   
                  // EXTRA, update the TODO list
                  if (browser.gui_todo_enabled) {
                     browser.showToDo(document.getElementById('todo'),'left',url,browser.username + "_todo.rdf",ns,'');
                  }
                  
                  if (browser.gui_mapping_enabled) {
                     browser.emptyMappingDiv();
                  }
                  
                  browser.stopActivityAnimation(side);

               } else if (req.responseText) {
                  alert("response not of type XML");
               } else {
                  alert("no response XML or Text available");
               }
            } else {
               alert(req.statusText);
            }
         } else {
         }
      }
}

browser.performSearch = function(side) {
   browser.refresh_side;
   var form = ((side == 'left') ? 
               document.getElementById(side).childNodes[1] : 
               document.getElementById(side).childNodes[2]);
   var query = form.query.value;

   if (query == null || query == '') {
      var url = "ThesaurusBrowser?";
      var params = "&server_url=" + form.server_url.value + 
         "&repository=" + form.repository.value +
         "&username=" + form.username.value +
         "&password=" + form.password.value +
         "&ns=" + ((side == 'left') ? 
                   browser.default_left_ns :
                   browser.default_right_ns);
      browser.showTopConcepts(url,params);
   } else {
      var url = "ThesaurusSearch?";
      var params = "query=" + query +
         "&server_url=" + form.server_url.value + 
         "&repository=" + form.repository.value +
         "&username=" + form.username.value +
         "&password=" + form.password.value +
         "&ns=" + form.ns.value;

      var req;

      function performSearchReady() {
         if (req.readyState == 4) {
            if (req.status == 200) {
               if (req.responseXML) {                   
                  var xmlDoc = req.responseXML.documentElement;

                  browser.server_info.server_url = xmlDoc.getElementsByTagName('server_url')[0].childNodes[0].nodeValue
                  browser.server_info.repository = xmlDoc.getElementsByTagName('repository')[0].childNodes[0].nodeValue
                  browser.server_info.username = xmlDoc.getElementsByTagName('username')[0].childNodes[0].nodeValue
                  browser.server_info.password = xmlDoc.getElementsByTagName('password')[0].childNodes[0].nodeValue

                  var ns = xmlDoc.getElementsByTagName('ns')[0].childNodes[0].nodeValue

                        // target div elements
                  var left = document.getElementById('left');
                  var right = document.getElementById('right');

                        // create the replacement viewer div
                  var div = document.createElement('div');

                  var history_stub = document.createElement('div');
                  history_stub.setAttribute('id',side + '_history');
                  div.appendChild(history_stub);

                  if (side == 'right') {
                            // close button;
                     var close = document.createElement('div');
                     div.appendChild(close);
                     close.setAttribute('class','close');
                     var close_a = document.createElement('a');
                     close.appendChild(close_a);
                     var close_txt = document.createTextNode('close this view');
                     close_a.appendChild(close_txt);
                  }

                  var search = browser.addSearchBox(div,ns,side);

                  if (xmlDoc.getElementsByTagName('result').length > 0) {
                            // create a link for each result term
                     var result_div = document.createElement('div');
                     result_div.setAttribute('class','result');
                     div.appendChild(result_div);
                     var result_header = document.createElement('div');
                     result_div.appendChild(result_header);
                     result_header.setAttribute('class','relation');
                     var result_header_txt = document.createTextNode('result terms');
                     result_header.appendChild(result_header_txt);
                     var result_ul = document.createElement('ul');
                     result_div.appendChild(result_ul);
                     var result = xmlDoc.getElementsByTagName('result');                    
                     for (i=0;i<result.length;i++) {
                        var li = document.createElement('li');
                        result_ul.appendChild(li);
                        var a = document.createElement('a');
                        var href = result[i].childNodes[0].childNodes[0].nodeValue;
                        href = href.replace("javascript:","javascript:browser.refresh_side='" + side + "';");
                        a.setAttribute('href',href);
                        var link = result[i].childNodes[0].childNodes[0].nodeValue;
                        var link_label = result[i].childNodes[1].childNodes[0].nodeValue;
                        a.appendChild(document.createTextNode(link_label));
                        li.appendChild(a);
                        // append number of narrower terms (size of the concept)
                        if (result[i].childNodes[3].childNodes[0] != null) {
                           var nrnt = result[i].childNodes[3].childNodes[0].nodeValue;
                           var nrnt_div = document.createElement('div');
                           nrnt_div.setAttribute('class','number_of_narrower_terms');
                           nrnt_div.appendChild(document.createTextNode('(' + nrnt + ')'));
                           li.appendChild(nrnt_div);
                        }
                        var term_part = result[i].childNodes[2].childNodes[0].nodeValue;
                        var ns_part = ((term_part.match(/#/)) ?
                                       term_part.match(/(.*)#/)[1] : 
                                       term_part.match(/(.*)\//)[1]); 
                        var link_ns = document.createElement('div');
                        link_ns.setAttribute('class','search_ns_label');
                        var link_ns_txt = document.createTextNode(" " + browser.myunescape(ns_part));
                        link_ns.appendChild(link_ns_txt);
                        li.appendChild(link_ns);                        
                     }
                  }

                        // refresh the left or right viewer
                  if (side == 'left') {
                     div.setAttribute('ID','left');
                     search.setAttribute('action','javascript:browser.performSearch("left");');
                     left.parentNode.replaceChild(div,left);
                  } else if (side == 'right') {
                     div.setAttribute('ID','right');
                     search.setAttribute('action','javascript:browser.performSearch("right");');
                     close_a.setAttribute('href','javascript:browser.closeView()');
                     right.parentNode.replaceChild(div,right);
                  } else { 
                  }


                  // EXTRA, update the TODO list
                  if (browser.gui_todo_enabled) {
                     browser.showToDo(document.getElementById('todo'),'left',url,browser.username + "_todo.rdf",ns,'');
                  }
                  
                  // clear the mapping add/remove div, 
                  // because no mappings can be made to a search results page
                  if (browser.gui_mapping_enabled) {
                     browser.emptyMappingDiv();
                  }
                  
                  if (xmlDoc.getElementsByTagName('result').length == 0) {
                     if (! query.match(/\*/) && query != '') {
                        var result_div = document.createElement('div');
                        result_div.setAttribute('class','result');
                        div.appendChild(result_div);
                        var result_header = document.createElement('div');
                        result_div.appendChild(result_header);
                        result_header.setAttribute('class','relation');
                        var result_header_txt = document.createTextNode('trying prefix search: ' + query + '*');
                        result_header.appendChild(result_header_txt);
                        search.elements[5].setAttribute('value',query + '*');
                        search.submit();
                     } else {
                        var result_div = document.createElement('div');
                        result_div.setAttribute('class','result');
                        div.appendChild(result_div);
                        var result_header = document.createElement('div');
                        result_div.appendChild(result_header);
                        result_header.setAttribute('class','relation');
                        var result_header_txt = document.createTextNode('no results found');
                        result_header.appendChild(result_header_txt);
                        search.elements[5].setAttribute('value',query);

                        browser.stopActivityAnimation(side);

                     }
                  } else {
                     browser.stopActivityAnimation(side);
                  }

               } else if (req.responseText) {
                  alert("response not of type XML");
               } else {
                  alert("no response XML or Text available");
               }
            } else {
               alert(req.statusText);
            }
         } else {
         }
      }

      req = new XMLHttpRequest();
      req.onreadystatechange = performSearchReady;
      req.open("POST",url + browser.myescape(params),true);
      req.send(null);
      
      browser.startActivityAnimation(side);

   }
}

browser.startActivityAnimation = function(side) {
   if (document.getElementById(side + '_search_box') != null && // if there exists a search box to put it in
       document.getElementById(side + '_activity_animation') == null) { // and it doesn't already exist
      // add the loader animation
      var activity_anim = document.createElement('img');
      activity_anim.setAttribute('id', side + '_activity_animation');
      activity_anim.setAttribute('src', browser.gui_activity_animation_path);
      var search_form = document.getElementById(side + '_search_box');
      search_form.insertBefore(activity_anim,search_form.childNodes[7]);
   }
}

browser.stopActivityAnimation = function(side) {
   // remove the loader animation
   var activity_anim = document.getElementById(side + "_activity_animation");
   if (activity_anim != null) {
      activity_anim.parentNode.removeChild(activity_anim);
   }
}

browser.refresh = function(side) {
   if (side == 'left') {
      browser.loadConcept(browser.left_request.url,browser.left_request.params,true);
   } else if (side == 'right') {
      browser.loadConcept(browser.right_request.url,browser.right_request.params,true);
   } else if (side == 'both' && browser.right_request.url != null) {
      if (browser.refresh_side == 'left') {
         browser.queued_request.url = browser.right_request.url;
         browser.queued_request.params = browser.right_request.params;
         browser.queued_request.refresh_side = "right";
         browser.loadConcept(browser.left_request.url,browser.left_request.params,true);
      } else if (browser.refresh_side == 'right') {
         browser.queued_request.url = browser.left_request.url;
         browser.queued_request.params = browser.left_request.params;
         browser.queued_request.refresh_side = "left";
         browser.loadConcept(browser.right_request.url,browser.right_request.params,true);
      }
   } else {
      browser.queued_request.url = null;
      browser.queued_request.params = null;
      browser.queued_request.refresh_side = null;
      browser.loadConcept(browser.left_request.url,browser.left_request.params,true);
   }
}

browser.closeView = function() {
   var div = document.getElementById('right');
   var empty_div = document.createElement('div');
   empty_div.setAttribute('ID','right');

    // open button;
   var open = document.createElement('div');
   empty_div.appendChild(open);
   open.setAttribute('class','open');
   var open_a = document.createElement('a');
   open.appendChild(open_a);
   var open_txt = document.createTextNode('open this view');
   open_a.appendChild(open_txt);
   open_a.setAttribute('href','javascript:browser.openView()');

   div.parentNode.replaceChild(empty_div,div);    

   if (browser.gui_mapping_enabled) {
      browser.emptyMappingDiv();
   }
}

browser.openView = function() {
   browser.refresh_side = "right";
   browser.showTopConcepts('ThesaurusBrowser?',
                           browser.serverInfoToParams(browser.server_info) + "&" +
                           browser.serverInfoToParams(browser.mapping_server_info) + "&" +
                           '&ns=' + browser.default_right_ns);
/*
   var right = document.getElementById('right');
   var div = document.createElement('div');
   div.setAttribute('ID','right');

    // close button;
   var close = document.createElement('div');
   div.insertBefore(close,div.firstChild);
   close.setAttribute('class','close');
   var close_a = document.createElement('a');
   close.appendChild(close_a);
   var close_txt = document.createTextNode('close this view');
   close_a.appendChild(close_txt);
   close_a.setAttribute('href','javascript:browser.closeView()');

   var search = browser.addSearchBox(div);
   
   search.setAttribute('action','javascript:browser.performSearch("right");');

   right.parentNode.replaceChild(div,right); 
*/
}

browser.getMappingRelationSuggestions = function() {
   var relations = new Object();
   relations['exactMatch'] = 'http://www.w3.org/2004/02/skos/mapping#exactMatch';
   relations['narrowMatch'] = 'http://www.w3.org/2004/02/skos/mapping#narrowMatch';
   relations['broadMatch'] = 'http://www.w3.org/2004/02/skos/mapping#broadMatch';

    // supporting non-standard relations is one thing, propagating them is another...
/*
   relations['disjointMatch'] = 'http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch';
   relations['relatedMatch'] = 'http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch';
   relations['partMatch'] = 'http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch';
   relations['wholeMatch'] = 'http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch';
*/
   return relations;

}

browser.backOrForth = function(rel,side) {
   if (side == 'left') return rel;
   if (rel == 'partMatch') return 'wholeMatch';
   if (rel == 'wholeMatch') return 'partMatch';
   if (rel == 'narrowMatch') return 'broadMatch';
   if (rel == 'broadMatch') return 'narrowMatch';
   return rel;
}

browser.emptyMappingDiv = function() {
    // clear the mapping add/remove div, 
   var mapping = document.getElementById('mapping');
   var empty_mapping = document.createElement('div');
   empty_mapping.setAttribute('ID','mapping');
   mapping.parentNode.replaceChild(empty_mapping,mapping);
}

browser.logLine = function(line) {
   var lreq;
   lreq = new XMLHttpRequest();
   lreq.onreadystatechange = logLineReady;
   lreq.open("POST","ChangeLogger?what=" + escape(document.authority_username + "\t" + line),true);
   lreq.send(null);
   function logLineReady() {
      if (lreq.readyState == 4) {
         if (lreq.status == 200) {
            if (lreq.responseXML) {
            } else if (lreq.responseText) {
            } else {
               alert("no confirmation from Logger");
            }
         } else {
            alert("logger gave status:" + lreq.statusText);
         }
      }
   }   
}

browser.addStatement = function(server_info,subject,predicate,object) {
   var ssareq;
   var url = "RepositoryAddRdfStatementSVL";
   var params = "?" + browser.serverInfoToParams(server_info) +
      "&subject=" + subject +
      "&predicate=" + predicate +
      "&object=" + object;
   ssareq = new XMLHttpRequest();
   ssareq.onreadystatechange = addStatementReady;
   ssareq.open("POST",url + browser.myescape(params),true);
   ssareq.send(null);

   clearTimeout(browser.result_display_timeout);
   browser.setResultDisplay('Storing statement... ','thesaurus/loader2.gif',false);

   function addStatementReady() {
      if (ssareq.readyState == 4) {
         if (ssareq.status == 200) {
            if (ssareq.responseXML) {
            } else if (ssareq.responseText) {
//                    alert(ssareq.responseText);
               var result = ssareq.responseText.match(/true/);
               var result_text = "";
               if (result != null) { result_text = "Done storing statement."; }
               else { result_texts = "Failed to store statement."; }

               window.status = result_text;              
               browser.setResultDisplay(result_text,null,true);
               
            } else {
               alert("no response XML or Text available");
            }
         } else {
            alert(ssareq.statusText);
         }
      }
   }
}

browser.removeStatement = function(server_info,subject,predicate,object) {
   return browser.removeStatementThes(server_info,subject,predicate,object);
}

browser.removeStatementRep = function(server_info,subject,predicate,object) {
   var ssrreq;
   var url = "RepositoryRemoveRdfStatementSVL";
   var params = "?" + browser.serverInfoToParams(server_info) +
      "&subject=" + subject +
      "&predicate=" + predicate +
      "&object=" + object;
   ssrreq = new XMLHttpRequest();
   ssrreq.onreadystatechange = removeStatementReady;
   ssrreq.open("POST",url + browser.myescape(params),true);
   ssrreq.send(null);

   browser.setResultDisplay('Removing statement... ','thesaurus/loader2.gif',false);

   function removeStatementReady() {
      if (ssrreq.readyState == 4) {
         if (ssrreq.status == 200) {
            if (ssrreq.responseXML) {
            } else if (ssrreq.responseText) {
//                    alert(ssrreq.responseText);
               var result = ssrreq.responseText.match(/true/);
               var result_text = "";
               if (result != null) { result_text = "Done removing statement."; }
               else { result_texts = "Failed to remove statement."; }

               window.status = result_text;
               browser.setResultDisplay(result_text,null,true);

            } else {
               alert("no response XML or Text available");
            }
         } else {
            alert(ssrreq.statusText);
         }
      }
   }
}

browser.setResultDisplay = function(msg,img,destroy) {
   
   clearTimeout(browser.result_display_timeout);

   var result_display = document.getElementById('result_display');
   if (result_display == null) {
      result_display = document.createElement('div');
      result_display.setAttribute('id','result_display');
      result_display.appendChild(document.createTextNode(msg));
      if (img != null) {
         var loading_anim = document.createElement('img');
         loading_anim.setAttribute('src',img);
         result_display.appendChild(loading_anim);
      }
      document.body.appendChild(result_display);
   } else {
      var loading_text = document.createTextNode(msg);
      if (result_display.childNodes.length > 1) { // there's an image
         result_display.removeChild(result_display.childNodes[1]);
      }
      result_display.replaceChild(loading_text,result_display.firstChild);      
   }
   if (destroy) {
      browser.result_display_timeout = setTimeout("document.body.removeChild(document.getElementById('result_display'))",'3000');
   }
}

browser.removeStatementThes = function(server_info,subject,predicate,object) {
   var ssrreq;
   var url = "ThesaurusRepositorySVL";
   var relations = browser.getMappingRelationSuggestions();
   var relation = "";
   for (r in relations) {
      if (relations[r] == predicate) {
         relation = r;
      }
   }
   var operation = "remove" + relation.substring(0,1).toUpperCase() + relation.substring(1,relation.length);
   var params = "?" + browser.serverInfoToParams(server_info) +
      "&operation=" + operation + 
      "&subject=" + subject +
      "&object=" + object;
   ssrreq = new XMLHttpRequest();
   ssrreq.onreadystatechange = removeStatementReady;
   ssrreq.open("POST",url + browser.myescape(params),true);
   ssrreq.send(null);

   browser.setResultDisplay('Removing statement... ','thesaurus/loader2.gif',false);

   function removeStatementReady() {
      if (ssrreq.readyState == 4) {
         if (ssrreq.status == 200) {
            if (ssrreq.responseXML) {
            } else if (ssrreq.responseText) {
//                    alert(ssrreq.responseText);
               var result = ssrreq.responseText.match(/true/);
               var result_text = "";
               if (result != null) { result_text = "Done removing statement."; }
               else { result_texts = "Failed to remove statement."; }

               window.status = result_text;
               browser.setResultDisplay(result_text,null,true);

            } else {
               alert("no response XML or Text available");
            }
         } else {
            alert(ssrreq.statusText);
         }
      }
   }
}

browser.addMapping = function() {
   var subject = document.getElementById('left').childNodes[2].getAttribute('id');
   var object = document.getElementById('right').childNodes[3].getAttribute('id');
   var predicate = document.getElementById('mapping').childNodes[0].childNodes[0].mapping_relation.value;
   browser.addStatement(browser.mapping_server_info,subject,predicate,object);
   browser.logLine("ADD <" + subject + "> <" + predicate + "> <" + object + "> .");
   browser.refresh('both');
}

browser.removeMapping = function() {
   var subject = document.getElementById('left').childNodes[2].getAttribute('id');
   var object = document.getElementById('right').childNodes[3].getAttribute('id');
   var predicate = document.getElementById('mapping').childNodes[1].childNodes[0].mapping_relation.value;
   browser.removeStatement(browser.mapping_server_info,subject,predicate,object);
   browser.logLine("REMOVE <" + subject + "> <" + predicate + "> <" + object + "> .");
   browser.refresh('both');
}

browser.changeMapping = function() {
   var subject = document.getElementById('left').childNodes[2].getAttribute('id');
   var object = document.getElementById('right').childNodes[3].getAttribute('id');
   var predicate = document.getElementById('mapping').childNodes[1].childNodes[0].mapping_relation.value;
   var predicate2 = document.getElementById('mapping').childNodes[0].childNodes[0].mapping_relation.value;
   browser.removeStatement(browser.mapping_server_info,subject,predicate,object);
   browser.addStatement(browser.mapping_server_info,subject,predicate2,object);
   browser.logLine("REMOVE <" + subject + "> <" + predicate + "> <" + object + "> .");
   browser.logLine("ADD <" + subject + "> <" + predicate2 + "> <" + object + "> .");
   browser.refresh('both');
}

browser.myescape = function(str) {
   return str.replace(/#/g,"%23").replace(/:/g,"%3A");
}

browser.myunescape = function(str) {
   return str.replace(/%23/g,"#").replace(/%3A/g,":").replace(/%2F/g,"/");
}

browser.authenticate = function(username,password) {
   var password_coded = password.replace(/[a-zA-Z]/g, function(c){
      return String.fromCharCode((c <= "Z" ? 90 : 122) >= (c = c.charCodeAt(0) + 13) ? c : c - 26);
   });
   if (username == password_coded) {
      browser.username = username;
      browser.password = password;
      return true;
   }
   return false;
}

browser.addToHistory = function(side,label,url,params) {
   if (browser.gui_history_enabled) {
      var short_label = label.substring(0,10);
      if (short_label != label) {
         short_label = short_label + "...";
      }
      if (side == 'left') {
         if (browser.left_history.length >= browser.max_history_size) {
            browser.left_history.shift();
         }
         browser.left_history.push([short_label,url,params]);
      } else if (side == 'right') {
         if (browser.right_history.length >= browser.max_history_size) {
            browser.right_history.shift();
         }
         browser.right_history.push([short_label,url,params]);
      } else {
         alert('cannot add to history of side "' + side + '"');
      }
   }
}

browser.createHistoryDiv = function(side) {    
   var div = document.createElement('div');
   if (((side=='left') ? 
        browser.left_history.length : 
        browser.right_history.length) == 0) return div;
      div.setAttribute('class', side + '_history');
   var header = document.createElement('div');
   div.appendChild(header);
   header.setAttribute('class','history_header');
   var header_txt = document.createTextNode('History');
   header.appendChild(header_txt);
   var ol = document.createElement('ol');
   div.appendChild(ol);
   for (i=1;i<=((side == 'left') ? 
                browser.left_history.length : 
                browser.right_history.length);i++) {
      var record = (side == 'left') ? 
                   browser.left_history[browser.left_history.length-i] : 
                   browser.right_history[browser.right_history.length-i];
      var label = record[0];
      var url = record[1];
      var params = record[2];
      var li = document.createElement('li');
      ol.appendChild(li);
      var a = document.createElement('a');
      li.appendChild(a);
      a.setAttribute('href',"javascript:browser.refresh_side='" + side +"';browser.loadConcept('" + url + "','" + params + "');");
      var a_txt = document.createTextNode(label);
      a.appendChild(a_txt);
   }
   return div;
}

browser.getLabel = function(doc) {
   if (doc.getElementsByTagName('collection_members').length > 0) {
      // a collection
      return new Array("collection " + doc.getElementsByTagName('collection_members')[0].getAttribute('name'), "label");
   } else if (doc.getElementsByTagName('prefLabel').length > 0) {
      // regular concept
      return new Array(doc.getElementsByTagName('prefLabel')[0].childNodes[0].nodeValue, "prefLabel");
   } else if (doc.getElementsByTagName('label').length > 0) {
      // collection or other concept with RDFS label, in case of label lookup probably
      return new Array(doc.getElementsByTagName('label')[0].childNodes[0].nodeValue, "label");
   } else {
      return new Array('[error in label lookup]', null);
   }
}

browser.insert_dc_subject_docs = function(div,side,term_uri) {
   browser.getDcSubjectOf(browser.server_info,term_uri,div);
}

browser.getDcSubjectOf = function(server_info,subject,div) {
   var dcreq;
   var url = "RepositorySelectQueryTableSVL";
   var query = "select distinct D from {D} dc:subject {S} where S = <" + subject + "> using namespace dc = <http://purl.org/dc/elements/1.1/>";
   var params = "?" + browser.serverInfoToParams(server_info) +
      "&query=" + browser.myescape(query) + 
      "&query_language=serql" +
      "&xml=1";
   dcreq = new XMLHttpRequest();
   dcreq.onreadystatechange = getDcSubjectOfReady;
   dcreq.open("POST",url + browser.myescape(params),true);
   dcreq.send(null);

   function getDcSubjectOfReady() {
      if (dcreq.readyState == 4) {
         if (dcreq.status == 200) {
            if (dcreq.responseXML) {
               var xmlDoc = dcreq.responseXML.documentElement;
               var hits = xmlDoc.getElementsByTagName("col");
               if (hits.length > 0) {
                  var dc_subject_docs = document.createElement("div");
                  dc_subject_docs.setAttribute("class","dc_subject_docs");
                  div.appendChild(dc_subject_docs);               
                  var dc_subject_docs_header = document.createElement("div");
                  dc_subject_docs_header.setAttribute("class","relation");
                  dc_subject_docs_header.appendChild(document.createTextNode("subject of"));
                  dc_subject_docs.appendChild(dc_subject_docs_header);
                  var dc_subject_docs_ul = document.createElement("ul");
                  dc_subject_docs.appendChild(dc_subject_docs_ul);
                  for (i=0;i<hits.length;i++) {
                     var url = hits[i].firstChild.nodeValue;
                     var li = document.createElement("li");
                     var a = document.createElement("a");
                     li.appendChild(a);
                     a.appendChild(document.createTextNode(url));
                     a.setAttribute("href",url);
                     div.appendChild(li);
                  }
               }
            } else if (dcreq.responseText) {
//                    alert(dcreq.responseText);
            } else {
               alert("no response XML or Text available");
            }
         } else {
            alert(dcreq.statusText);
         }
      }
   }
}

browser.insert_extra_features = function(div,side) {
   var ns = "";
   var concept = "";
   var divs = div.getElementsByTagName("div");
   for (i=0;i<divs.length;i++) {
      if (divs[i].getAttribute("class") == "ns_label") {
         ns = divs[i].firstChild.nodeValue;
      }
      if (divs[i].getAttribute("class") == "label" ||
          divs[i].getAttribute("class") == "prefLabel") {
         concept = divs[i].firstChild.firstChild.nodeValue;
      }
   }

   if (browser.gui_agricola_enabled && ns == "http://www.fao.org/aos/agrovoc") {

      var hits_div = document.createElement('div');
      hits_div.setAttribute('class','hits_div');
      
     // open button
      var open = document.createElement('div');
      hits_div.appendChild(open);
      open.setAttribute('class','open_hits');
      var open_a = document.createElement('a');
      open.appendChild(open_a);
      hits_div.appendChild(open);
      div.appendChild(hits_div);

      // AGRIS/CARIS hits
      var query = "subject:" + concept + "";
      var hitcount = 5;

      open.setAttribute('id','agris_caris_open');
      hits_div.setAttribute('id','agris_caris_hits');
      var open_txt = document.createTextNode('search in AGRIS/CARIS with this concept');
      open_a.appendChild(open_txt);
      open_a.setAttribute('href',"javascript:document.getElementById('agris_caris_open').style.visibility='hidden';agriscaris.getAgrisCarisHits(document.getElementById('agris_caris_hits'),'" + query + "','" + hitcount + "');");

      // agriscaris.getAgrisCarisHits(div,query,hitcount);
   } else if (browser.gui_agriscaris_enabled && ns == 'http://agclass.nal.usda.gov/nalt/2006.xml') {

      var hits_div = document.createElement('div');
      hits_div.setAttribute('class','hits_div');
      
      // open button
      var open = document.createElement('div');
      hits_div.appendChild(open);
      open.setAttribute('class','open_hits');
      var open_a = document.createElement('a');
      open.appendChild(open_a);
      hits_div.appendChild(open);
      div.appendChild(hits_div);

      // AGRICOLA hits
      var query = "skey+" + concept + "";
      var hitcount = 5;

      open.setAttribute('id','agricola_open');
      hits_div.setAttribute('id','agricola_hits');
      var open_txt = document.createTextNode('search in AGRICOLA with this concept');
      open_a.appendChild(open_txt);
      open_a.setAttribute('href',"javascript:document.getElementById('agricola_open').style.visibility='hidden';agricola.getAgricolaHits(document.getElementById('agricola_hits'),'" + query + "','" + hitcount + "');");

      // agricola.getAgricolaHits(div,query,hitcount);
   }   
}

browser.insert_extra_style = function(div,ns) {
   if (ns == "http://www.fao.org/aos/agrovoc") {
      div.setAttribute('class','agrovoc');
   } else if (ns == "http://agclass.nal.usda.gov/nalt/2006.xml") {
      div.setAttribute('class','nalt');
   }
}



browser.addSearchBox = function(div,ns,side) {
   // add search box
   var search = document.createElement('form');
   search.setAttribute('id',side + '_search_box');
   div.appendChild(search);
   search.setAttribute('method','POST');

   browser.addHiddenServerInfo(search,browser.server_info);

   var ns_input = document.createElement('input');
   search.appendChild(ns_input);
   ns_input.setAttribute('name','ns');
   ns_input.setAttribute('type','hidden');
   ns_input.setAttribute('value',ns);
   var query_label = document.createElement('div');
   search.appendChild(query_label);
   query_label.setAttribute('class','query_label');
   var query_label_txt = document.createTextNode('Search:');
   query_label.appendChild(query_label_txt);                    
   var query_input = document.createElement('input');
   search.appendChild(query_input);
   query_input.setAttribute('name','query');
   query_input.setAttribute('type','text');
   query_input.setAttribute('size','20');

   var top_concepts = document.createElement('div');
   top_concepts.setAttribute('class','show_top_concepts');
   var top_concepts_link = document.createElement('a');
   top_concepts_link.setAttribute('href',"javascript:browser.refresh_side='" + side + "';browser.performSearch('" + side + "');");
   top_concepts_link.appendChild(document.createTextNode("show concept schemes"));
   top_concepts.appendChild(top_concepts_link);
   search.appendChild(top_concepts);

   return search;
}

browser.addAssertMappingForm = function() {
   var assert_mapping = document.createElement('div');
   assert_mapping.setAttribute('ID','mapping');
   assert_mapping.setAttribute('class','mapping_modifier');
   
   var form = document.createElement('form');
   assert_mapping.appendChild(form);
   form.setAttribute('method','POST');

   browser.addHiddenServerInfo(form,browser.mapping_server_info);

   var txt1 = document.createTextNode("Add a ");
   form.appendChild(txt1);
   
   var relation_input = document.createElement('select');
   relation_input.setAttribute('name','mapping_relation');
   form.appendChild(relation_input);
   var relations = browser.getMappingRelationSuggestions();
   for (var rel_label in relations) {
      var rel_uri = relations[rel_label];
      var option = document.createElement('option');
      relation_input.appendChild(option);
      option.setAttribute('value',rel_uri);
      var option_txt = document.createTextNode(rel_label);
      option.appendChild(option_txt);
   }                            
   
   var txt2 = document.createTextNode(" mapping between these concepts. ");                               
   form.appendChild(txt2);
   
   var submit = document.createElement('input');
   submit.setAttribute('type','button');
   submit.setAttribute('value','add');
   submit.setAttribute('onclick','javascript:browser.addMapping();');
   form.appendChild(submit);
   
   var assert_mapping_wrapper = document.createElement('div');
   assert_mapping_wrapper.appendChild(assert_mapping);
   
   var mapping = document.getElementById('mapping');
   mapping.parentNode.replaceChild(assert_mapping_wrapper,mapping);
   assert_mapping_wrapper.setAttribute('ID','mapping');
}

browser.addChangeDeleteMappingForm = function(existing_relation) {
   var change_mapping = document.createElement('div');
   change_mapping.setAttribute('class','mapping_modifier');
   
   var form = document.createElement('form');
   change_mapping.appendChild(form);
   form.setAttribute('method','POST');

   browser.addHiddenServerInfo(form,browser.mapping_server_info);

   var txt1 = document.createTextNode("Change the ");
   form.appendChild(txt1);
   var span = document.createElement('span');
   form.appendChild(span);
   span.setAttribute('class','existing_mapping_label');
   var txt2 = document.createTextNode(existing_relation)
   span.appendChild(txt2);
   var txt3 = document.createTextNode(" mapping between these concepts into a ");
   form.appendChild(txt3);
   
   var relation_input = document.createElement('select');
   relation_input.setAttribute('name','mapping_relation');
   form.appendChild(relation_input);
   var relations = browser.getMappingRelationSuggestions();
   for (var rel_label in relations) {
      var rel_uri = relations[rel_label];
      var option = document.createElement('option');
      relation_input.appendChild(option);
      option.setAttribute('value',rel_uri);
      var option_txt = document.createTextNode(rel_label);
      option.appendChild(option_txt);
   }                            
   
   var txt4 = document.createTextNode(" mapping. ");                               
   form.appendChild(txt4);
   
   var submit = document.createElement('input');
   submit.setAttribute('type','button');
   submit.setAttribute('value','change');
   submit.setAttribute('onclick','javascript:browser.changeMapping();');
   form.appendChild(submit);
   
   var remove_mapping = document.createElement('div');
   remove_mapping.setAttribute('class','mapping_modifier');
   
   var form2 = document.createElement('form');
   remove_mapping.appendChild(form2);
   form2.setAttribute('method','POST');

   browser.addHiddenServerInfo(form2,browser.mapping_server_info);

   var txt1 = document.createTextNode("Remove the " + existing_relation + " mapping between these concepts. ");
   form2.appendChild(txt1);
   
   var mapping_relation_input = document.createElement('input');
   mapping_relation_input.setAttribute('name','mapping_relation');
   mapping_relation_input.setAttribute('type','hidden');
   mapping_relation_input.setAttribute('value',relations[existing_relation]);
   form2.appendChild(mapping_relation_input);
   
   var submit = document.createElement('input');
   submit.setAttribute('type','button');
   submit.setAttribute('value','remove');
   submit.setAttribute('onclick','javascript:browser.removeMapping();');
   form2.appendChild(submit);
   
   var change_remove_mapping = document.createElement('div');                                
   change_remove_mapping.appendChild(change_mapping);
   change_remove_mapping.appendChild(remove_mapping);
   
   var mapping = document.getElementById('mapping');
   mapping.parentNode.replaceChild(change_remove_mapping,mapping);
   change_remove_mapping.setAttribute('ID','mapping');
}

browser.addHiddenServerInfo = function(form,server_info) {
   var server_url_input = document.createElement('input');
   server_url_input.setAttribute('name','server_url');
   server_url_input.setAttribute('type','hidden');
   server_url_input.setAttribute('value',server_info.server_url);
   form.appendChild(server_url_input);
   var repository_input = document.createElement('input');
   repository_input.setAttribute('name','repository');
   repository_input.setAttribute('type','hidden');
   repository_input.setAttribute('value',server_info.repository);
   form.appendChild(repository_input);
   var username_input = document.createElement('input');
   username_input.setAttribute('name','username');
   username_input.setAttribute('type','hidden');
   username_input.setAttribute('value',server_info.username);
   form.appendChild(username_input);
   var password_input = document.createElement('input');
   password_input.setAttribute('name','password');
   password_input.setAttribute('type','hidden');
   password_input.setAttribute('value',server_info.password);
   form.appendChild(password_input);
}

browser.serverInfoToParams = function(info) {
      return "server_url=" + info.server_url +
          "&repository=" + info.repository +
          "&username=" + info.username +
          "&password=" + info.password;
}

