var agricola = new Object();

try {
   netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
} catch (err) {
   if (netscape) {
      alert("This program requires requires you to entrust it with the extra privilege to load data from another computer than the one this program runs on. This is usually done with certificates, but since this is experimental software we would like to request the privileges without a certificate. This requires you to do the following:\n\nNavigate to \"about:config\" using the URL bar.\nSearch for the property named \"signed.applets.codebase_principal_support\" by typing \"codebase\" in the entry box and set it to \"true\".\n\nThis allows the program to ask for your permission.\n\nWillem van Hage <wrvhage@few.vu.nl>");
   } else {
      alert("This experimental software currently only runs in Mozilla browsers such as Firefox.\n\nWillem van Hage <wrvhage@few.vu.nl>");
   }
}

agricola.getAgricolaHits = function(context,query,hitcount) {
   var params = "?query=" + escape(query) +
      ((hitcount != null) ? "&hitcount=" + hitcount : "");

   var req;
   req = new XMLHttpRequest();
   req.onreadystatechange = getAgricolaHitsReady;
   req.open("POST","GetAgricolaHits" + params,true);
   req.send(null);

   function getAgricolaHitsReady() {
      if (req.readyState == 4 && req.status == 200 && req.responseXML) {                   
         var xmlDoc = req.responseXML.documentElement;
         var div = document.createElement("div");
         div.setAttribute("class","hits");
         var hits = xmlDoc.getElementsByTagName("resource");
         if (hits.length > 0) {
            var agricola_header = document.createElement('div');
            agricola_header.setAttribute('class','agricola_hits_header');
            agricola_header.appendChild(document.createTextNode('AGRICOLA Search results'));
            var agricola_link = document.createElement('a');
            agricola_link.setAttribute('class','agricola_home_link');
            agricola_link.setAttribute('href','http://agricola.nal.usda.gov/');
            agricola_link.appendChild(document.createTextNode('NAL Catalog (AGRICOLA) Homepage'));
            div.appendChild(agricola_header);
            agricola_header.appendChild(agricola_link);
         }
         for (i=0;i<hits.length;i++) {
            var hit = document.createElement("div");
            hit.setAttribute("class","hit");

            var titles = document.createElement("div");
            titles.setAttribute("class","hit_title");
            hit.appendChild(titles);
            var t_label = document.createElement("div");
            t_label.setAttribute("class","hit_title_label");
            t_label.appendChild(document.createTextNode("title"));
            titles.appendChild(t_label);
            var t_list = document.createElement("ul");
            t_list.setAttribute("class","hit_title_list");
            titles.appendChild(t_list);
            var t = hits[i].getElementsByTagName("title");
            var tit = "";
            for (j=0;j<t.length;j++) {
               var title_li = document.createElement("li");
               title_li.setAttribute("class","hit_title_li");
               title_li.appendChild(document.createTextNode(t[j].firstChild.nodeValue));
               t_list.appendChild(title_li);
            }

            var creators = document.createElement("div");
            creators.setAttribute("class","hit_creator");
            hit.appendChild(creators);
            var c_label = document.createElement("div");
            c_label.setAttribute("class","hit_creator_label");
            c_label.appendChild(document.createTextNode("creator"));
            creators.appendChild(c_label);
            var c_list = document.createElement("ul");
            c_list.setAttribute("class","hit_creator_list");
            creators.appendChild(c_list);            
            var c = hits[i].getElementsByTagName("creatorPersonal");
            var crea = "";
            for (j=0;j<c.length;j++) {
               var creator_div = document.createElement("li");
               creator_div.setAttribute("class","hit_creator_li");            
               creator_div.appendChild(document.createTextNode(c[j].firstChild.nodeValue));
               c_list.appendChild(creator_div);
            }

            var dates = document.createElement("div");
            dates.setAttribute("class","hit_date");
            hit.appendChild(dates);
            var d_label = document.createElement("div");
            d_label.setAttribute("class","hit_date_label");
            d_label.appendChild(document.createTextNode("date"));
            dates.appendChild(d_label);
            var d_list = document.createElement("ul");
            d_list.setAttribute("class","hit_date_list");
            dates.appendChild(d_list);            
            var d = hits[i].getElementsByTagName("dateIssued");
            var date = "";
            for (j=0;j<d.length;j++) {
               var date_div = document.createElement("li");
               date_div.setAttribute("class","hit_date_li");
               date_div.appendChild(document.createTextNode(d[j].firstChild.nodeValue));
               d_list.appendChild(date_div);
            }

            div.appendChild(hit);
         }
         context.appendChild(div);
      }
   }
}


