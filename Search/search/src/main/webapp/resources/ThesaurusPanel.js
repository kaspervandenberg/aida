// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The function which draws the right-hand side, i.e., everything that has to do with thesurus interactions
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      3 April 2009
* @version   $Id$
*
* @license 
*/

function ThesaurusPanel() {

	// config
  var server_url  = loadState('server_url', getBaseURL() + '/openrdf-sesame');
	var ns          = loadState('ns', 'http://www.foodinformatics.nl/food_ontology.owl');
	var repository  = loadState('repository', '');
	var username    = loadState('username', 'testuser');
	var password    = loadState('password', 'opensesame');
	
	saveState('server_url', server_url);
	saveState('ns', ns);
	saveState('repository', repository);
	saveState('username', username);
	saveState('password', password);

	var Services_success = false;
  
  var concepts;
  var curPrefix = '';

	document.getElementById('loading-msg').innerHTML = 'Initializing Query Builder ...';


	// namespace
	var Tree = Ext.tree;
  
  ////////////////////////////////////////////////////////////////////////////////
  // Build the graphical query builder
  ////////////////////////////////////////////////////////////////////////////////

	
  // Recursive tree loader, used for the My Query drop field
	rtloader = new Tree.TreeLoader({
		timeout : 60000,
		preloadChildren : true,
		dataUrl : baseURL + '/Services/rest/thesaurusbrowser/narroweralts',
		baseParams : {
			ns : '',
			server_url : server_url,
			repository : repository,
			username : username,
			password : password,
			json : 'true',
			target : 'ThesaurusBrowser',
			recurseChildren : true
		},
		listeners : {
		  beforeload : function(th, n, cb) { // listener, catches empty requests
        if (n.id === '') {
          return false;        
        }
        
        // make sure we send the URI as request to the webservice
        if (n.attributes.URI) {
          th.baseParams.uri = n.attributes.URI;
        } else if (n.URI) {
          th.baseParams.uri = n.URI;
        } else {
          th.baseParams.uri = n.id;
        }
        
		  	// this will cause the server to load the full tree
        // and might time out
		  	//th.baseParams.recurseChildren = true;
        
		  }, 
		  load : function (th, n, r) {
		  	n.expand(true, false);
		  	if (! this.isLoading()) {
		  	  searchBar.onTrigger2Click();
		    }
		  }, 
		  loadException : function (th, n, r) {
		  	//console.log(th);
		  	//console.log(n);
		  	//console.log(r);
		  	//loadFailed(th.baseParams, r, 'ThesaurusBrowser (recursive)');
		  }
		}
	});

  advsearchpanel = new Tree.TreePanel({
    id : 'advsearchpanel',
    region : 'north',
    title : 'AIDA Concept Query Builder',
    animate : false,
    layout : 'fit',
    autoScroll : true,
    //autoHeight : true, 
    height : 140,
    margins : '5 5 0 0',
    cmargins : '5 5 0 0',
    collapsible : true,
    collapsed : false,
    containerScroll: true,
		minSize : 40,
    //items : view
		
		// bogus loader, but it's required 
    loader: new Ext.tree.TreeLoader({
      dataUrl : baseURL + '/search/jason',
      baseParams: { // custom http params
        test : 1,
        add : true
      }
    }),
    
    // The free-style query window
    tools : [{
      id : 'search',
      qtip : 'Open repository query window',
      handler : function(event, toolEl, panel){              
	      var testwin = new Ext.app.RepositorySearch({
	        repository : loadState('repository', '')
	      });
	      
	      testwin.show();
      }
    }],
    enableDragDrop : true,
    enableDD:true,
    //ddGroup : 'advquery',
    allowDrop : true,
    ddAppendOnly:false,
    dropConfig: {
    	
      allowContainerDrop : true,
      
      // fix to allow dropping on the container
      onContainerDrop : function (source, e, data) {
    
    
      	var new_n = new Tree.AsyncTreeNode({
			    text : data.node.text, 
			    draggable : true, 
			    id : data.node.id,
			    URI : data.node.attributes.URI,
			    iconCls : 'narrower-icon',
			    expanded : true,
			    loader : rtloader,
			    allowDrop : false
  			});
  			
        advsearchpanel.root.appendChild(new_n);
        advsearchpanel.root.expand();
        
      	return true;
      },
      onContainerOver : function(source, e, data){return this.dropAllowed;}
    }
    /*,tbar : [new Ext.Button({
			tooltip : '',
			iconCls : 'must-icon',
			text : '<b>and</b> node',
			icon : 'icons/add.png'
			//handler : thesaurusbrowser.showRoot
		}), new Ext.Button({
			tooltip : '',
			iconCls : 'not-icon',
			text : '<b>not</b> node'
			//,handler : function() {
		  //	thesaurusbrowser.root.eachChild(
	    //   function(n) {
	    //      if (n)
	    //        n.remove();
	    //    } 
	    //  );
			//}
		})]*/
		, root : 
      new Tree.AsyncTreeNode({
        expandable : false,
        isTarget : true,
        loader : rtloader,
        qtip: 'Drag thesaurus concepts here to search for them',
        text : 'My Query', 
        draggable : false, 
        id : 'queryroot',
        iconCls : 'query-icon',
        expanded : true,
        loaded : true,
        listeners : {
          insert : function(n) {
             
          },
          append : function() {
            
          },
          click : function(n, event) {
            searchBar.onTrigger2Click();
          }
        }
      }),
    listeners : {
    	beforechildrenrendered : function(node) {
    			node.eachChild(
		        function(n) {
		          if (n) {
		          	if (n.plainText) {
		          		
		          	} else {
  		          	n.plainText = n.text;
		          	}
		          	//n.setText(n.plainText + ' - <div class="concept-id"> &lt;<i>'+n.id+'</i>&gt;</div>');
                n.setText(n.plainText);
		          }
		        } 
		      );
    	},
      beforenodedrop : dropNode,
      nodedrop : function (dropEvent) {
      	//redrawAdvSearch();
        //console.log(dropEvent);
        //console.log(dropEvent.dropNode);
        //this.getRootNode().expandChildNodes(true);
        //this.getRootNode().collapse();
      },
      expandnode : function (n) {
        //redrawAdvSearch();
      },
      collapsenode : function (n) {
        //redrawAdvSearch();
      }
    }
  });
  
  function dropNode(e) {
  	
		e.dropNode = new Tree.AsyncTreeNode({
			    text : e.dropNode.text, 
			    plainText : e.dropNode.plainText, 
			    draggable : true, 
			    id : e.dropNode.id,
			    URI : e.dropNode.attributes.URI,
			    iconCls : 'narrower-icon',
			    expanded : true,
			    loader : rtloader,
			    allowDrop : false,
			    listeners : {
      			load : function(n) {
      				//n.expandChildNodes(true);
         			//n.collapseChildNodes(true)
      			}
      		}
  			});
  			//e.cancel = true;
  			//advsearchpanel.getRootNode().appendChild(newnode);
  			
        //e.dropNode = copyDropNode(e.dropNode);
        //e.dropNode.expandChildNodes(true);
  			
  			//e.dropNode.collapseChildNodes(true);
        advsearchpanel.root.expand();
  }
  
	function copyDropNode(node){
	  var newNode = new Tree.TreeNode(Ext.apply({}, node.attributes));
	    for(var i=0; i < node.childNodes.length; i++){
	      var n = node.childNodes[i];
	      if(n) {
	        newNode.appendChild(copyDropNode(n));
	      }
	  }
	  return newNode;
	}
    
  function redrawAdvSearch() {
  	
  	//if (rightview.lastSize.height > advsearchpanel.lastSize.height) {
    	//advsearchpanel.toggleCollapse(false);
    	//advsearchpanel.toggleCollapse(false);
    	
  	//}
    
    // TODO: IE bug which doesn't show a vertical scrollbar when expanding the thesaurus tree 
    //thesauruspanel.toggleCollapse(false);
    //thesauruspanel.toggleCollapse(false);
    //viewport.doLayout();
  }
  
  // Listener - context menu
  advsearchpanel.on({
    'contextmenu' : function(n, e) {
      
      // not very fast to do this here..
      

      // the functions
      function deleteNode(item) {
        item.node.remove();
        //redrawAdvSearch();
        searchDataStore.reload();
      }
      
      function toggleConcept(item) {
        
        // recursive:
        //toggleAllConcepts(item.node, item.prefix, item.iconcls);
        
        
        var node = item.node;
        var prefix = item.prefix;
        var cls = item.iconcls;
        
        if (cls) {
          // store the original
          if (!node.attributes.changed) {
            node.attributes.oldIconClass = node.ui.iconNode.className;
            node.attributes.changed = true;
          }
            
          node.ui.iconNode.className = 'x-tree-node-icon ' + cls;
          
        } else { // "may contain"
          // restore original
          if (node.attributes.oldIconClass)  {
            node.ui.iconNode.className = node.attributes.oldIconClass;
          }
        }
        
        node.attributes.prefix = prefix;
        
        
        searchDataStore.reload();
      }
      
      
		  function toggleAllConcepts (node, prefix, cls) {
        
        //console.log(node.childNodes.length );
        // current node:
        if (node.childNodes.length === 0) {
	        if (cls) {
	          // store the original
	          if (!node.attributes.changed) {
	            node.attributes.oldIconClass = node.ui.iconNode.className;
	            node.attributes.changed = true;
	          }
	            
	          node.ui.iconNode.className = 'x-tree-node-icon ' + cls;
	          
	        } else { // "may contain"
	          // restore original
	          node.ui.iconNode.className = node.attributes.oldIconClass;
	        }
	        
	        node.attributes.prefix = prefix;
        }
        
        
        // children nodes:
		    node.eachChild(
		      function(n) {
		        if (n) {
			        if (cls) {
			          // store the original
			          if (!n.attributes.changed) {
			            n.attributes.oldIconClass = n.ui.iconNode.className;
			            n.attributes.changed = true;
			          }
			            
			          n.ui.iconNode.className = 'x-tree-node-icon ' + cls;
			          
			        } else {
			          // restore original
			          n.ui.iconNode.className = n.attributes.oldIconClass;
			        }
		          n.attributes.prefix = prefix;
              
              // recurse
		          //if (!n.isLeaf())
		          toggleAllConcepts(n, prefix, cls);
		        }
		      } 
		    );
		  }
      
      var contextMenu = new Ext.menu.Menu({
        items : [
          new Ext.menu.Item({
	          id : n.id,
	          disabled : true,
	          text : n.text,
	          cls : 'nodename'
	        }), 
          
          new Ext.menu.Separator(), 
          
          new Ext.menu.Item({
            id : 'toggleMust',
            text : 'This concept <b>must</b> occur',
            icon : 'icons/add.png',
            handler : toggleConcept,
            node : n,
            prefix : '+',
            iconcls : 'must-icon'
          }),
          new Ext.menu.Item({
            id : 'toggleNot',
            text : 'This concept <b>must not</b> occur',
            icon : 'icons/delete.png',
            handler : toggleConcept,
            node : n,
            prefix : '-',
            iconcls : 'not-icon'
          }),
          new Ext.menu.Item({
            id : 'toggleOr',
            text : 'This concept <b>may</b> occur',
            icon : 'icons/magnifier.png',
            handler : toggleConcept,
            node : n,
            prefix : ''
          }),
          
          new Ext.menu.Separator(), 
          
          new Ext.menu.Item({
	          id : 'delete-node',
	          text : 'Remove this concept from the tree',
	          icon : 'icons/chart_organisation_delete.png',
	          handler : deleteNode,
	          node : n
          }),
          
          new Ext.menu.Separator(),
          
          new Ext.menu.Item({
	          text : 'Query GO repository (definitions)',
	          icon : 'icons/database_refresh.png',
	          node : n,
	          handler : function(item) {
	          	
	          	var node = item.node;
      	      var testwin = new Ext.app.RepositorySearch({
				        repository : loadState('repository', 'GO'),
				        query :
				          "prefix go: <http://purl.org/obo/owl/GO#>\n" +
							  	"prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
							  	"prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
							  	"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                  "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
							  	"select  distinct ?name  ?class ?deflabel\n" +
							  	"where\n" +
							  	"{\n" +
							  	"\t?class rdf:type owl:Class.\n" +
							  	"\t?class rdfs:label ?name.\n" +
							  	"\toptional {\n" +
							  	"\t\t?class obo:hasDefinition ?def.\n" +
							  	"\t\t?def rdfs:label ?deflabel \n" +
							  	"\t}\n" +
							  	"\tfilter ((?class = <"+node.id+">))\n" +
							  	"}\n"
				        // not yet used:
				        ,conceptid : node.id 
				      });
				      
				      testwin.show();
	          }
          }),
          
          new Ext.menu.Item({
	          text : 'Query GO repository (superClass)',
	          icon : 'icons/database_refresh.png',
	          node : n,
	          handler : function(item) {
	          	
	          	var node = item.node;
      	      var testwin = new Ext.app.RepositorySearch({
				        repository : loadState('repository', 'GO'),
				        query :
				          "prefix go: <http://purl.org/obo/owl/GO#>\n" +
							  	"prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
							  	"prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
							  	"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                  "select distinct ?name  ?class ?definition\n" +
                  "where\n" +
                  "{   <"+node.id+"> rdfs:subClassOf ?class.\n" +
                  "    ?class rdfs:label ?name.\n" +
                  "    ?class obo:hasDefinition ?def.\n" +
                  "    ?def rdfs:label ?definition \n" +
                  "}\n"
				        // not yet used:
				        ,conceptid : node.id 
				      });
				      
				      testwin.show();
	          }
          })
        ]
      });
      
      // don't show this for the root node
      if (n.id !== 'queryroot') { 
        contextMenu.show(n.ui.getAnchor());
      }
    },
    scope : this
  });
  
  ////////////////////////////////////////////////////////////////////////////////
  // Build the main thesaurus browser
  ////////////////////////////////////////////////////////////////////////////////
  
  document.getElementById('loading-msg').innerHTML = 'Initializing Thesaurus Browser...';
  
  /* 
  var thesaurusbrowser = new Ext.Panel({
    title : 'info',
    html : 'Select a repository first...',
    closable : 'true',
    cls : 'text'
  });
  
  // if a user selects a different server and then refreshes, the repository will be empty
   
   if (repository !== '') { 
  	// The panel with the tree
    thesaurusbrowser = new Ext.app.ThesaurusBrowser({
      //loader : tloader,
      id : server_url + '-' + repository,
      ns : ns,
      server_url : server_url,
      repository : repository,
      username : username,
      password : password
    });
  }
  */
  
  
/// also remove on delete
  /*
  var repoArray = new Array();
  var repos = loadObject('openrepositorylist2', {});
  
  if (repos.length == 0) {
    repoArray.push(
        new Ext.Panel({
          title : 'info',
          html : 'Select a repository first...',
          closable : 'true',
          cls : 'text'
        })
    );
  } else {
    for (var item in repos) {
      repoArray.push(
          new Ext.app.ThesaurusBrowser({
            //loader : tloader,
            id : server_url + '-' + item,
            ns : ns,
            server_url : server_url,
            repository : item,
            username : username,
            password : password
          })
      );
    } 
  }
  
  repoArray.push(
      new Ext.app.ThesauriSearch({
        //allowDrag : false
      })
  );
  */
  
  document.getElementById('loading-msg').innerHTML = 'Initializing Thesaurus Server ...';
    
  ////////////////////////////////////////////////////////////////////////////////
  // the server login formpanel
  ////////////////////////////////////////////////////////////////////////////////

  handleDirectLink();

  var thesaurusServer = new ThesaurusServerPanel(); 

  document.getElementById('loading-msg').innerHTML = 'Initializing Thesaurus Browser ...';
  
  ////////////////////////////////////////////////////////////////////////////////
  // Construct the thesaurus browser panel
  ////////////////////////////////////////////////////////////////////////////////
    
    
    // set for search: 
    /*
    {
      title: 'Tab 2',
      html: 'Fixed tab',
      allowDrag: false
    }
    */
    // allowDrag: false

  thesauruspanel = new Ext.ux.panel.DDTabPanel({
    id : 'thesauruspanel',
  	activeTab : 0,//loadState('active-tab', 0),
  	border : false,
  	//items : 
  	  //[thesaurusbrowser],
  	  //repoArray,
  	listeners : {
      remove : function(tp, i) {
        var r = i.repository;
        
        var repos = loadObject('openrepositorylist2', {});
        delete repos[r];
        saveObject('openrepositorylist2', repos);
        
      },
      tabchange : function(th, t) {
        if (t.loader) { 
          //saveState('active-tab', tabpanel.id);
          rtloader.baseParams = t.loader.baseParams;
          saveState('repository', t.loader.baseParams.repository);
          //rtloader.baseParams.recurseChildren = true;
        }
      }
    }
    ,plugins : new Ext.ux.DirectLinkMenu()
  });
  
  var repos = loadObject('openrepositorylist2', null);

   
  var b = new Ext.app.ThesauriSearch({ /*allowDrag : false*/ });

  thesauruspanel.add(b);
  thesauruspanel.setActiveTab(b);

  if (repos) {
   
	for (var item in repos) {
	  continue;
	  b = new Ext.app.ThesaurusBrowser({
	   //loader : tloader,
	   id : server_url + '-' + item,
	   ns : ns,
	   server_url : server_url,
	   repository : getLongURL(item),
	   username : username,
	   password : password
	 });

	 thesauruspanel.add(b);

	 thesauruspanel.setActiveTab(b);

	}
  } else {
    b = new Ext.Panel({
      title : 'info',
      html : 'Select a repository first...',
      closable : 'true',
      cls : 'text'
    });

    thesauruspanel.add(b);

    thesauruspanel.setActiveTab(b);

  }
  
  var thesaurusbrowser = thesauruspanel.getActiveTab();
  
  thesauruscontainerpanel = new Ext.Panel({
    region : 'center',
    title : 'AIDA Repository Browser',
    collapsible : false,
    layout : 'fit',
    margins : '5 5 0 0',
    //split : true,
    width : 400,
    minSize : 150,
    maxSize : 600,
    height : 600,
    items : [thesauruspanel]
  });
  
  document.getElementById('loading-msg').innerHTML = 'Initializing getBrowser ...';

	// getters
  this.getBrowser = function() {
		return thesaurusbrowser;
  };
	
  this.getThesaurusPanel = function() {
    return thesauruspanel;
  };
	
  document.getElementById('loading-msg').innerHTML = 'Initializing getPanel ...';

  this.getPanel = function() {
		return thesauruscontainerpanel;
  };
	
  document.getElementById('loading-msg').innerHTML = 'Initializing getFormPanel ...';
	
  this.getFormPanel = function() {
		return thesaurusServer;
  };
	
  document.getElementById('loading-msg').innerHTML = 'Initializing getQueryBuilder ...';
  
  this.getQueryBuilder = function() {
		return advsearchpanel;
  };
	
  document.getElementById('loading-msg').innerHTML = 'Initializing getTreeLoader ...';
  
  this.getTreeLoader = function() {
    return rtloader;
  };
  
  document.getElementById('loading-msg').innerHTML = 'Initializing getAllConcepts ...';

  this.getAllConcepts = function() {
    //concepts = new Array();
    concepts = '';
    getConcepts(advsearchpanel.getRootNode());
    //console.log(concepts);
    return concepts;
  };
  
  document.getElementById('loading-msg').innerHTML = 'Initializing getAltConcepts ...';
  
  getAltConcepts = function (node) {
  	
  	var alts;
  	if (node.plainText) {
  	  alts = '("' + node.plainText + '" ';
  	} else {
  		alts = '("' + node.text.split(" - ")[0] + '" ';
  	}
  	  
  	node.eachChild(
  	  function(n) {
  	  	if (n) {
  	  		if (n.attributes.iconCls === 'alt-icon') {
						if (n.plainText) {
				  	  alts += 'OR "' + n.plainText + '" ';
				  	} else {
				  		alts += 'OR "' + n.text + '" ';
				  	}
  	  			
  	  		}
  	  	}
  	  }
  	);
  	return alts + ') ';
  };
  
  document.getElementById('loading-msg').innerHTML = 'Initializing onlyHasAltConcepts ...';
  
  onlyHasAltConcepts = function (node) {
  	
  	var children = node.childNodes.length;
  	var alts = 0;
  	
  	node.eachChild(
  	  function(n) {
  	  	if (n) {
  	  		if (n.attributes.iconCls === 'alt-icon') {
  	  			alts++;
  	  		}
  	  	}
  	  }
  	);

  	if (alts == children) {
  	  return true;
  	} else {
  	  return false;
    }
  };
  
  document.getElementById('loading-msg').innerHTML = 'Initializing hasAltConcepts ...';
  
  hasAltConcepts = function (node) {
  	
  	node.eachChild(
  	  function(n) {
  	  	if (n) {
  	  		if (n.attributes.iconCls === 'alt-icon') {
  	  			return true;
  	  		}
  	  	}
  	  }
  	);
  	return false;
  };
  
  document.getElementById('loading-msg').innerHTML = 'Initializing getConcepts ...';
  
  getConcepts = function(node) {
    
    if (node.attributes.prefix && !node.isLeaf() && !onlyHasAltConcepts(node)) {    	
      concepts += node.attributes.prefix + '( ';
    }
    
    node.eachChild(
      function(n) {
        if (n) {
          if (n.attributes.prefix) { // toggled to MUST or NOT 
						//if (n.childNodes.length == 0)
							concepts += n.attributes.prefix + getAltConcepts(n);
						//else
							//concepts += '"' + n.plainText + '" ';
          } else {
						
          	if (n.attributes.iconCls === 'alt-icon') {
          		//concepts += '"==' + n.plainText + '" ';
          	}	else {
						  concepts += getAltConcepts(n);
          	}
          }
          
          // recurse
          if (!n.isLeaf()) {
            getConcepts(n);
          }
        }
      } 
    );
    
    if (node.attributes.prefix && !node.isLeaf() && !onlyHasAltConcepts(node)) {
      concepts += ') ';
    }
    
  };
  
  document.getElementById('loading-msg').innerHTML = 'Drawing interface ...';
}

