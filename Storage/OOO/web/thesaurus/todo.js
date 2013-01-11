browser.todo_ns = "http://www.few.vu.nl/~wrvhage/2007/06/todo";
browser.todo_server_info = browser.mapping_server_info;

browser.setDone = function(side,url,todo_url,source_ns,current_uri) {
   browser.addStatement(browser.todo_server_info,current_uri,browser.todo_ns + "#doneBy",browser.username);
   setTimeout("browser.showToDo(document.getElementById('todo'),'"+side+"','"+url+"','"+todo_url+"','"+source_ns+"','"+current_uri+"');",2000);
}

browser.setToDo = function(side,url,todo_url,source_ns,current_uri) {
   browser.removeStatementRep(browser.todo_server_info,current_uri,browser.todo_ns + "#doneBy",browser.username);
   setTimeout("browser.showToDo(document.getElementById('todo'),'"+side+"','"+url+"','"+todo_url+"','"+source_ns+"','"+current_uri+"');",2000);
}

browser.addToDo = function(side,url,todo_url,source_ns,current_uri) {
   browser.addStatement(browser.todo_server_info,current_uri,browser.todo_ns + "#todoFor",browser.username);
   setTimeout("browser.showToDo(document.getElementById('todo'),'"+side+"','"+url+"','"+todo_url+"','"+source_ns+"','"+current_uri+"');",2000);
}

browser.showToDo = function(old_div,side,url,todo_url,source_ns,current_uri) {

   var div = document.createElement('div');

   var myRDF=new RDF()
   myRDF.getRDFURL(todo_url,show_todo);

   function show_todo() {
      // First load the entire TODO list,
      // then load the done items,
      // then show the todo items except the done items.

      // load TODO list
      var todo_items = myRDF.Match(null,null,browser.todo_ns + "#todoFor",browser.username);

      // prepare user interface
      var todo_div = document.createElement('div');
      todo_div.setAttribute('class','todo');
      todo_div.setAttribute('id',side + '_todo');
      div.appendChild(todo_div);
      var todo_header = document.createElement('div');
      todo_div.appendChild(todo_header);
      todo_header.setAttribute('class','relation');
      var todo_header_txt = document.createTextNode('todo items');
      todo_header.appendChild(todo_header_txt);

      var ul = document.createElement('ul');
      todo_div.appendChild(ul);

      // request done items
      var done_req;
      var done_url = "RepositorySelectQueryTableSVL";
      var done_params = "?" + browser.serverInfoToParams(browser.todo_server_info) +
         "&xml=1" + 
         "&query_language=serql" +
         "&query=select distinct WHAT,REL from {WHAT} REL {WHO} where namespace(REL) = <" + browser.todo_ns + "#> and WHO = \"" + browser.username + "\"";

      done_req = new XMLHttpRequest();
      done_req.onreadystatechange = getDoneStatementsReady;
      done_req.open("POST",done_url + browser.myescape(done_params),true);
      done_req.send(null);
      
      function getDoneStatementsReady() {
         if (done_req.readyState == 4) {
            if (done_req.status == 200) {
               if (done_req.responseXML) {
                  var xmlDoc = done_req.responseXML.documentElement;
                  var done_items = [];
                  var done_returned = xmlDoc.getElementsByTagName('row');
                  for (i=0;i<done_returned.length;i++) {
                     var what_rel = new Array(2);
                     what_rel[0] = done_returned[i].childNodes[1].firstChild.nodeValue;
                     what_rel[1] = done_returned[i].childNodes[3].firstChild.nodeValue;
                     if (what_rel[1] == browser.todo_ns + '#doneBy') {
                        done_items[what_rel[0]] = 1;
                     } else if (what_rel[1] == browser.todo_ns + '#todoFor') {
                        todo_items.push(new Triple(what_rel[0],what_rel[1],browser.username,"literal"));
                     }
                  }

                  var todo_index = new Array();
                  // show remaining items, except done items
                  for (i=0;i<todo_items.length;i++) {
                     todo_index[todo_items[i].subject] = i;
                     var source_url = todo_items[i].subject;
                     var source_label = "[no label]";
                     var labels = myRDF.Match(null,source_url,"http://www.w3.org/2000/01/rdf-schema#label",null);
                     if (labels.length > 0) {
                        source_label = labels[0].object;
                     } else {
                        var prefLabels = myRDF.Match(null,source_url,"http://www.w3.org/2004/02/skos/core#prefLabel",null);
                        if (prefLabels.length > 0) {
                           source_label = prefLabels[0].object;
                        } else {
                           source_label = source_url;
                        }
                     }
                     
                     var params = browser.serverInfoToParams(browser.server_info) + "\n" +
                        "&term=" + source_url + "&ns=" + source_ns;
                     var li = document.createElement('li');
                     ul.appendChild(li);
                     var a = document.createElement('a');
                     
                     var href = "javascript:browser.refresh_side='" + side +"';browser.loadConcept('" + url + "','" + browser.myescape(params) + "',true);";
                     a.setAttribute('href',href);
                     if (done_items[todo_items[i].subject]) {
                        li.appendChild(document.createTextNode("[done] "));
                     }
                     var linkTxt = document.createTextNode(source_label);
                     a.appendChild(linkTxt);
                     li.appendChild(a);

                     var todo_item_ns = ((source_url.match(/#/)) ?
                                        source_url.match(/(.*)#/)[1] : 
                                        source_url.match(/(.*)\//)[1]);                      
                     var todo_ns_div = document.createElement('div');
                     todo_ns_div.setAttribute('class','todo_ns_label');
                     var todo_ns_txt = document.createTextNode(" " + browser.myunescape(todo_item_ns));
                     todo_ns_div.appendChild(todo_ns_txt);
                     li.appendChild(todo_ns_div);
                  }
  
                  // this IF-statement limits adding todo's to things already on the todo list.
//                  if (todo_index[current_uri] != null) {
// FIXME: implement filter for Concepts.
                  var concept_display = document.getElementById(side);
                  if (side == 'left') { concept_display = concept_display.childNodes[2]; }
                  else { concept_display = concept_display.childNodes[3]; }
                  if (concept_display != null &&
                      concept_display.childNodes[0] != null &&
                      concept_display.childNodes[0].getAttribute('class') == 'uri') {
                     // add a button to say this concept is done
                     var done_button = document.createElement('input');
                     done_button.setAttribute('type','button');
                     if (done_items[current_uri] == null && todo_index[current_uri] != null) {
                        done_button.setAttribute('value',"I'm done with this concept.");
                        done_button.setAttribute('onclick',"javascript:browser.setDone('" +
                                                 side+"','"+url+"','"+todo_url+"','"+
                                                 source_ns+"','"+current_uri+"');");
                        todo_div.insertBefore(done_button,ul);   
                     } else if (done_items[current_uri] == null && todo_index[current_uri] == null) {
                        done_button.setAttribute('value',"Add this concept to my todo list.");
                        done_button.setAttribute('onclick',"javascript:browser.addToDo('" +
                                                 side+"','"+url+"','"+todo_url+"','"+
                                                 source_ns+"','"+current_uri+"');");
                        todo_div.insertBefore(done_button,ul);   
                     } else {
                        done_button.setAttribute('value',"I'm not done with this concept yet.");
                        done_button.setAttribute('onclick',"javascript:browser.setToDo('" +
                                                 side+"','"+url+"','"+todo_url+"','"+
                                                 source_ns+"','"+current_uri+"');");
                        todo_div.insertBefore(done_button,ul);   
                     }
                  }
//                }
               } else if (done_req.responseText) {
                  //alert(done_req.responseText);
                  
               } else {
                  alert("no response XML or Text available");
               }
            } else {
               alert(done_req.statusText);
            }
         }
      }      
   }

   div.setAttribute('ID','todo');
   old_div.parentNode.replaceChild(div,old_div);

}


