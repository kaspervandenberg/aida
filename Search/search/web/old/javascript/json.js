var searchObj;

if(typeof(XMLHttpRequest)!='undefined'){
  var getXMLHttpObj = function(){ return new XMLHttpRequest(); }
} else {
  var getXMLHttpObj = function(){
    var activeXObjects = ['Msxml2.XMLHTTP.6.0', 'Msxml2.XMLHTTP.5.0', 'Msxml2.XMLHTTP.4.0',
    'Msxml2.XMLHTTP.3.0', 'Msxml2.XMLHTTP', 'Microsoft.XMLHTTP'];
    for(var i=0; i<activeXObjects.length; i++){
      try{
        return new ActiveXObject(activeXObjects[i]);
      }catch(err){}
    }
  }
}

function removeElement(element) {
  var oNodeToRemove = document.getElementById(element);

  if (oNodeToRemove != null) {
    oNodeToRemove.parentNode.removeChild(oNodeToRemove);
  }
}

function getFields(index) {

if (index != "") {
  var oXML = getXMLHttpObj();
  toggleLoading('q');

  oXML.open('GET', '/search/jason?target=fields&index='+index, true);
  oXML.onreadystatechange = function(){

    if(oXML.readyState!=4) return;

    var json  = eval('('+oXML.responseText+')');
    var field = document.getElementById('index');
    var fieldSelect = document.createElement("select");
    var index_has_content_field = 0;

    fieldSelect.setAttribute('onMouseOver',"self.status='Next'; return true");
    fieldSelect.setAttribute('onChange',"searchObj.field = this.value;");
    fieldSelect.setAttribute('id',"fieldSelect");
    fieldSelect.setAttribute('size',"1");

    if (json.fields.length > 0) {

      for(var i=0; i<json.fields.length; i++) {
        var opt = document.createElement("option");
        var t = document.createTextNode(json.fields[i].field);
        opt.setAttribute("value", json.fields[i].field);

        // Auto select a content field
        if (json.fields[i].field == "content") {
          opt.setAttribute("selected", "selected");
          searchObj.field = "content";
          index_has_content_field = 1;
        }

        opt.appendChild(t);
        fieldSelect.appendChild(opt);
      }

      // defaulting
      if (index_has_content_field == 0) {
        searchObj.field = json.fields[0];
      }

    } else {
        var opt = document.createElement("option");
        var t = document.createTextNode("ERROR");
        opt.setAttribute("value", "ERROR");
        opt.appendChild(t);
        fieldSelect.appendChild(opt);
    }
    toggleLoading('q');
    field.appendChild(fieldSelect);
  }

  oXML.send('');
}
}

function updateFields() {
  searchObj.index = document.getElementById('indexSelect').value;
  removeElement('fieldSelect');
  getFields(searchObj.index);
}

function getIndices(){

  var oXML = getXMLHttpObj();
  toggleLoading('q');

  oXML.open('GET', '/search/jason?target=indexes', true);
  oXML.onreadystatechange = function(){

    if(oXML.readyState!=4) return;

    var json  = eval('('+oXML.responseText+')');
    var index = document.getElementById('index');
    var indexSelect = document.createElement("select");
    indexSelect['onchange'] = function() {updateFields();};
    indexSelect.setAttribute('id',"indexSelect");
    indexSelect.setAttribute('size',"1");

    if (searchObj.index == "") {
      var opt = document.createElement("option");
      var t = document.createTextNode("");
      opt.setAttribute("value", "");
      opt.setAttribute("selected", "selected");
      opt.appendChild(t);
      indexSelect.appendChild(opt);
    }

    if (json.indexes.length > 0) {

      for(var i=0; i<json.indexes.length; i++) {
        var opt = document.createElement("option");
        var t = document.createTextNode(json.indexes[i].index);
        opt.setAttribute("value", json.indexes[i].index);

        if (json.indexes[0] == searchObj.index) {
          opt.setAttribute("selected", "selected");
        }

        opt.appendChild(t);
        indexSelect.appendChild(opt);
      }
    } else {
        var opt = document.createElement("option");
        var t = document.createTextNode("ERROR");
        opt.setAttribute("value", "ERROR");
        opt.appendChild(t);
        indexSelect.appendChild(opt);
    }

    toggleLoading('q');
    index.appendChild(indexSelect);
  }

  oXML.send('');
}

function searchObj() {
  var query  = "";
  this.start  = 1;
  this.limit  = 10;
  this.index  = ""
  this.field  = "";
  this.hits   = 0;

  this.search = function(q, start, limit) {
    this.limit = limit;
    this.start = start;
    return this.search(q);
  }

  this.search = function(q) {

    var q2 = q;
    q = q2.replace( new RegExp( "\\+", "g" ), "%2B" );

    this.query = q;

    if (this.index == '') {
      alert("Select an index first");
      return;
    }

    var oXML = getXMLHttpObj();

    //alert ('/search/jason?target=search&index='+this.index+'&field='+this.field+'&query='+this.query+'&start='+this.start+'&limit=' + this.limit);
    toggleLoading('q');

    oXML.open('GET', '/search/jason?target=search&index='+this.index+'&field='+this.field+'&query='+this.query+'&start='+this.start+'&limit=' + this.limit, true);
    oXML.onreadystatechange = function(){ doneSearch(oXML); }
    oXML.send('');
  }

  this.first = function() {
    this.start = 1;
    this.limit = 10;
    this.search(this.query);
  }

  this.previous = function(limit) {
    this.start = this.start - this.limit;
    this.limit = limit;
    this.search(this.query);
  }

  this.next = function(limit) {
    this.start = this.start + this.limit;
    this.limit = limit;
    this.search(this.query);
  }

  this.last = function() {
    this.start = this.hits - this.limit;
    this.search(this.query);
  }
}

function toggleLoading(elem) {
  var id = document.getElementById(elem);
  var style = id.style;
  //style.backgroundColor = style.backgroundColor? "":"lightgrey";
  id.disabled = id.disabled?false:true;
  if(style && ('string' == typeof style.backgroundImage)) {
    style.backgroundImage = (-1 != style.backgroundImage.indexOf('search.png')) ? 'url(/search/images/search.png)': 'url(/search/images/anim_loading.gif)';
  } else {
    alert ("no");
  }
  style.backgroundImage = id.disabled?'url(/search/images/anim_loading.gif)':'url(/search/images/search.png)';
}

function toggleLayer(whichLayer) {
    if (document.getElementById) {
        // this is the way the standards work
        var style2 = document.getElementById(whichLayer).style;
        style2.display = style2.display? "":"block";
    } else if (document.all) {
        // this is the way old msie versions work
        var style2 = document.all[whichLayer].style;
        style2.display = style2.display? "":"block";
    } else if (document.layers) {
        // this is the way nn4 works
        var style2 = document.layers[whichLayer].style;
        style2.display = style2.display? "":"block";
    }
}

function createHeading(table) {
  var table = document.getElementById(table);
  table.parentNode.cellSpacing = 1; //reset from img

  var tr = document.createElement('TR');
  var th1 = document.createElement('TH');
  var th2 = document.createElement('TH');
  //var th3 = document.createElement('TH');

  //tr.style.backgroundColor = '#CCF';

  table.appendChild(tr);
  tr.appendChild(th1);
  tr.appendChild(th2);
  //tr.appendChild(th3);

  th1.appendChild(document.createTextNode("ID"));
  th2.appendChild(document.createTextNode("Title"));
  //th3.appendChild(document.createTextNode("Lucene docID"));

}

function createNav() {

  var facet = document.getElementById('facet');
  var facet_link = document.createElement('a');
  facet_link.setAttribute('id', 'facet_link');
  facet_link.setAttribute('href','/search/facet?target=search&index='+searchObj.index+'&field='+searchObj.field+'&start='+searchObj.start+'&count=100&query='+searchObj.query);
  facet_link.appendChild(document.createTextNode("(Faceted)"));
  facet.appendChild(facet_link);

  var td1 = document.createElement('DIV');

  var first_a = document.createElement('a');
  first_a.setAttribute('href','javascript:searchObj.first();');
  first_a.setAttribute('onMouseOver','self.status=\'First\'; return true');
  first_a.appendChild(document.createTextNode("<<"));

  var previous_a = document.createElement('a');
  previous_a.setAttribute('href','javascript:searchObj.previous(10);');
  previous_a.setAttribute('onMouseOver','self.status=\'Previous\'; return true');
  previous_a.appendChild(document.createTextNode("<"));

  var next_a = document.createElement('a');
  next_a.setAttribute('href','javascript:searchObj.next(10);');
  next_a.setAttribute('onMouseOver','self.status=\'Next\'; return true');
  next_a.appendChild(document.createTextNode(">"));

  var last_a = document.createElement('a');
  last_a.setAttribute('href','javascript:searchObj.last();');
  last_a.setAttribute('onMouseOver','self.status=\'Last\'; return true');
  last_a.appendChild(document.createTextNode(">>"));

  var span_1 = document.createElement('SPAN');
  span_1.setAttribute('title','First resultpage');
  span_1.setAttribute('class','popup');
  span_1.appendChild(first_a);
  var span_2 = document.createElement('SPAN');
  span_2.setAttribute('title','Previous resultpage');
  span_2.setAttribute('class','popup');
  span_2.appendChild(previous_a);
  var span_3 = document.createElement('SPAN');
  span_3.setAttribute('title','Next resultpage');
  span_3.setAttribute('class','popup');
  span_3.appendChild(next_a);
  var span_4 = document.createElement('SPAN');
  span_4.setAttribute('title','Last resultpage');
  span_4.setAttribute('class','popup');
  span_4.appendChild(last_a);

  var from = document.createElement('DIV');
  from.innerHTML = "Showing from <b>" + searchObj.start + "</b> through <b>" + (searchObj.start + searchObj.limit - 1) + "</b> of <b>" + searchObj.hits + "</b> hits";
  td1.appendChild(from);
  if (searchObj.start > searchObj.limit) {
    td1.appendChild(span_1);
    td1.appendChild(document.createTextNode(" "));
    td1.appendChild(span_2);
    td1.appendChild(document.createTextNode(" "));
  }

  if (searchObj.hits > searchObj.start + searchObj.limit) {
    td1.appendChild(span_3);
    td1.appendChild(document.createTextNode(" "));
    td1.appendChild(span_4);
  }

  return td1;
}

function createResultTable(container) {
  removeElement('res');

  var res = document.createElement('div');
  res.setAttribute('id', 'res');

  var h3 = document.createElement('h3');
  h3.appendChild(document.createTextNode("Results "));
  h3.setAttribute('id', 'facet');

  //var div_facet = document.createElement('div');
  //div_facet.setAttribute('id', 'facet');


  var table = document.createElement('table');
  table.setAttribute('id', 'topTable');
  table.setAttribute('cellspacing', '1');
  table.setAttribute('cellpadding', '3');
  var tbody = document.createElement('tbody');
  tbody.setAttribute('id', 'resultTable');

  table.appendChild(tbody);
  //h3.appendChild(div_facet);
  res.appendChild(h3);
  res.appendChild(table);

  var target = document.getElementById(container);
  target.appendChild(res);
}

function doneSearch(oXML) {
  if (oXML.readyState != 4) return;
  //alert (oXML.responseText);

  var json  = eval('('+oXML.responseText+')');

  // Put the '+' back
  var q = searchObj.query.replace( new RegExp( "%2B", "g" ), "\+" );
  searchObj.query = q;
  document.title = searchObj.query + " - AIDA Search";

  createResultTable('container');
  var table = document.getElementById('resultTable');

  for(var i=table.childNodes.length-1; i>0; i--){
    table.removeChild(table.childNodes[i]);
  }

  var tr  = document.createElement('TR');
  var td1 = document.createElement('TD');
  var td2 = document.createElement('TD');
  td2.style.textAlign="right";

  table.appendChild(tr);
  tr.appendChild(td1);
  tr.appendChild(td2);

  if (json.items == null || json.items.length == null || json.items.length == "") {
      toggleLoading('q');
      searchObj.hits = 0;
      td1.appendChild(document.createTextNode("0 hits"));
  } else {
    searchObj.hits = json.hits;

    if (searchObj.hits > 0) {
      var nav = createNav();
      td2.appendChild(nav);
    }

    createHeading('resultTable');

    for(var i=0; i<json.items.length; i++) {

      var tr  = document.createElement('TR');
      var td1 = document.createElement('TD');
      var td2 = document.createElement('TD');

      tr.style.backgroundColor = i%2?'#FFF':'#F2F2F2';
      table.appendChild(tr);
      tr.appendChild(td1);
      tr.appendChild(td2);

      td1.appendChild(document.createTextNode(json.items[i].id));
      td1.width='100';
      td2.innerHTML = "<div id=\"popover\" onmouseover=\"return overlib('" + json.items[i].description + "',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\">" + json.items[i].snippet + '</div>';

    }
    toggleLoading('q');
  }

}

function init() {
  var w = window.innerWidth;

  if (w >= 834 && w <= 1200) {
    w -= 900;
    if (w < 0) w *= -1;
  } else {
    w = 0;
  }
  w += 'px';

  document.getElementById('logo').style.paddingRight = w;

  var searchboxString = "";
  searchObj = new searchObj();

  searchboxString += "<input type='text' name='q' id='q' size='50' " +
        "onchange='searchObj.search(this.value, 1, 10);'" +
        " /><input type='submit' class='submit' value='Go' onclick=\"searchObj.search(document.getElementById('q').value, 1, 10);\"/>";
  document.getElementById("searchbox").innerHTML = searchboxString;

  //createHeading('resultTable');
  getIndices();
  getFields(searchObj.index);

  document.getElementById('q').focus();
}

window.onload = function(){
  init();
}
