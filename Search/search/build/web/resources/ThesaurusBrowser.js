// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An extended TreePanel which displays the tree-like structure of a repository
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      20 March 2009
* @version   $Id$
*
* @license 
*/

// namespaces
Ext.ns('Ext.app');
var Tree = Ext.tree;
  
// application main entry point
Ext.app.ThesaurusBrowser = Ext.extend(Tree.TreePanel, {
	
	//repository : '',
	
  initComponent : function() {
  	
  	this.rootID = this.rootID ? this.rootID : Ext.id();
  	
    ////////////////////////////////////////////////////////////////////////////////
    // The thesaurus concept finder
    ////////////////////////////////////////////////////////////////////////////////
  	/*
    var termfield = new Ext.app.TermField({
      //id : this.repository + '-concept-search',
      name : 'concept-search',
      width : 175,
      browser : this,
        baseParams : {
          ns : this.ns,
          server_url : this.server_url,
          repository : this.repository,
          username : this.username,
          password : this.password
        },
      sloader : new Tree.TreeLoader({
        preloadChildren : true,
        dataUrl : baseURL + '/Services/ThesaurusBrowser',
        baseParams : {
          ns : this.ns,
          server_url : this.server_url,
          repository : this.repository,
          username : this.username,
          password : this.password,
          json : 'true',
          sibling : 'true',
          target : 'ThesaurusBrowser'
        },
        clearOnLoad : false
      }),
      emptyText : 'Type in a concept...'
    });
    */
    var conceptcompl = new Ext.app.ConceptCompletion({
    	browser : this,
	    baseParams : {
        ns : this.ns,
        server_url : this.server_url,
        repository : this.repository,
        username : this.username,
        password : this.password
	    },
	    //width:300,
	    emptyText : 'Start typing in a concept...'
		}); 
  	
	this.directLink=getBaseURL()+"/search/?server_url="+this.server_url+"&repository_name="+this.repository;

	// This is for maximum worker node on checking for leaves
	this.wait_queue = new Array();
	this.currently_checking = 0;
    this.currently_expanding = 0;
    var bah = this;
    Ext.apply( this, {
      loader : 
        new Tree.TreeLoader({
          //preloadChildren : true,
          dataUrl : baseURL + '/Services/rest/thesaurusbrowser/narroweralts',
          
          baseParams : {
            ns : '',
            server_url : this.server_url,
            repository : this.repository,
            username : this.username,
            password : this.password,
            json : 'true',
            target : 'ThesaurusBrowser'
          },
          clearOnLoad : false,
          listeners : {
            beforeload : function(th, n, cb) { // listener, catches empty requests

              var id = '' + n.id; 
              if (id.startsWith('ext-gen') || id.endsWith('-root')) { 
              //  Ext.get('loading').remove();
              //  Ext.get('loading-mask').fadeOut({
              //    remove : true
              //  });
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
              
              if (! th.baseParams.repository || th.baseParams.repository === '' ) {
              	return false;
              }

			  // Avoid background process of checking node leaves to take precedence
			  bah.currently_expanding = 1;

            },
            load: function(th, n, r) {
				// Release back to checking
			    bah.currently_expanding = 0;
				bah.attemptChecking();
            	/*
            	 * Just to try out the treegrid. Doesn't work
            	 * 
					    var store = new Ext.ux.maximgb.treegrid.AdjacencyListStore({
					    	//baseParams : this.baseParams,
    	          url: baseURL + '/Services/ThesaurusBrowser',
					    	autoLoad : true,
								reader: new Ext.data.JsonReader({id: 'id'}, Ext.data.Record.create([{name: 'text'},{name: 'id'}]))
								//,proxy: new Ext.data.MemoryProxy(Ext.util.JSON.decode(r.responseText))
					    });
					    
					    var vp = new Ext.Window({
					    	layout : 'fit',
					    	width : 600,
                height : 350,
					    	items : 
	              new Ext.ux.maximgb.treegrid.GridPanel({
						      store: store,
						      master_column_id : 'id',
						      columns: [
										{id:'text', header: 'text', width: 75, sortable: true, dataIndex: 'text'},
						        {header: 'ID', width: 160, sortable: true, dataIndex: 'id'}
						      ],
						      
						      autoExpandColumn: 'text',
						      title: 'Array Grid',
						      root_title: this.baseParams.repository, 
						      viewConfig : {
						      	enableRowBody : true
						      }
						    })
					    });
					    
					    
					    vp.show();
            	*/
            },
			loadexception : function(th, node, resp){
					bah.currently_expanding = 0;
					bah.attemptChecking();
			}

          }
        }),
      closable : true,
      title : getShortURL(this.repository),
	  tabTip: this.directLink,
      animate : false,
      border : false,
      enableDrag : true,
      //ddGroup : 'advquery',
      containerScroll : true,
      autoScroll : true,
      root : new Tree.AsyncTreeNode({
        expanded : false,
        draggable : false,
        iconCls : 'broader-icon',
        id : this.rootID,
        loader : this.loader
      }),
      rootVisible : false,
      tbar : [
      	//termfield, 
        new Ext.Button({
	        tooltip : 'Refresh the tree',
	        iconCls : 'btn-browse',
	      //  text : 'Refresh',
	        scope: this,
	        handler : this.showRoot
	      }), 
	      new Ext.Button({
	        tooltip : 'Clear the tree',
	        iconCls : 'btn-clear',
	      //  text : 'Clear',
	        scope: this,
	        handler : this.clear
	      }),
	      new Ext.Toolbar.Separator(),
	      conceptcompl
      ]
    }
    ); // end apply
    
    Ext.app.ThesaurusBrowser.superclass.initComponent.call(this, arguments);
    
    // listeners
    this.on({
    	append : function(tree, dad, node, idx){
				/* when showing root the first time this will be called*/
				//this.checkThisNode(node);
				//setTimeout(this.checkThisNode(node),idx*5000);
				this.wait_queue.push(node);

				// Don't check root
				if(node.id.endsWith("-root"))
					return;
				
				// Check top nodes later
				if(dad.id.endsWith("-root"))
					return;

				this.attemptChecking();
		}, 


    	'beforechildrenrendered' : {
    		fn : function(node) {
			node.select();
    			node.eachChild(
		        function(n) {
		          if (n) {
		          	n.plainText = n.text;
		            //n.setText(n.text + ' - <div class="concept-id"> &lt;<i>'+n.id+'</i>&gt;</div>');
                n.setText(n.text);
		          }
		        } 
		      );
    		}
    	},
    	
      // Listener - context menu
      'contextmenu' : { 
        fn : function(n, e) {
          n.expand(false);
          
          function deleteNode (item) {
            item.node.remove();
          }
          
					function collapseAll (item) {
						var node = item.node;
						if(node && !node.isLeaf()) {
							node.select();
							node.collapse.defer(1, node, [true]);
						}
					}
          
          function expandAll (item) {
						var node = item.node;
						if ( node && !node.isLeaf() ) {
						  node.select();
						  node.expand.defer(1, node, [true]);
						}
					}
					
          function expandOne (item) {
						var node = item.node;
						if ( node && !node.isLeaf() ) {
							
						  node.select();
							node.eachChild(
				        function(n) {
				          if (n) {
				            n.expand.defer(1, n, [false]);
				          }
				        } 
				      );
						}
					}

          var contextMenu = new Ext.menu.Menu({
            items : [
            new Ext.menu.Item({
              id : n.id,
              disabled : true,
              //text : n.text,
              // to also show the id:
              text : n.text + ' &lt;<i>' + n.id + '</i>&gt;',
              cls : 'nodename'
            }), 
            new Ext.menu.Separator(),
            new Ext.menu.Item({
              id : 'expand-one-node',
              text : 'Expand one level deep',
              icon : 'icons/expand-all.gif',
              handler : expandOne,
              node : n
            }),
            new Ext.menu.Item({
              id : 'expand-all-node',
              text : 'Expand all children',
              icon : 'icons/expand-all.gif',
              handler : expandAll,
              node : n
            }),
            new Ext.menu.Item({
              id : 'collapse-one-node',
              text : 'Collapse all children',
              icon : 'icons/collapse-all.gif',
              handler : collapseAll,
              node : n
            }),
            new Ext.menu.Separator(), 
            new Ext.menu.Item({
              id : 'delete-node',
              text : 'Remove this concept from the tree',
              icon : 'icons/chart_organisation_delete.png',
              handler : deleteNode,
              node : n
            }),
   			// AW's modification 
		    new Ext.menu.Separator(), 
		    new Ext.menu.Item({
			  text : 'Sample Query Top Concepts',
		      icon : 'icons/database_refresh.png',
		      node : n,
		      handler : function(item) {
			var node       = item.node;
			var testwin = new Ext.app.RepositorySearch({
			  repository : loadState('repository', 'rdf-db'),
			  query : top_query_template(node)
				// not yet used:
				,conceptid : node.id 
				,conceptnode : node
			});
			
			testwin.show();
		      }
		    }),

		   new Ext.menu.Item({
			  text : 'Sample Query Narrower ',
		      icon : 'icons/database_refresh.png',
		      node : n,
		      handler : function(item) {
			
			var node = item.node;
			var prefixDef  = parse_prefix(node);
			var prefixStr  = loadState('repository','rdf-db');

			var testwin = new Ext.app.RepositorySearch({
			  repository : loadState('repository', 'GO'),
			  query : narrow_query_template(node)
				// not yet used:
				,conceptid : node.id 
				,conceptnode : node
			});
			
			testwin.show();
		      }
		    }),

		   new Ext.menu.Item({
			  text : 'Sample Query Alternate Label ',
		      icon : 'icons/database_refresh.png',
		      node : n,
		      handler : function(item) {
			
			var node = item.node;
			var prefixDef  = parse_prefix(node);
			var prefixStr  = loadState('repository','rdf-db');

			var strnode = prefixStr+":"+node.id.split("#")[1];
			var testwin = new Ext.app.RepositorySearch({
			  repository : loadState('repository', 'GO'),
			  query :
				"prefix skos:<http://www.w3.org/2004/02/skos/core#> \n" +
				 prefixDef + 
				"select ?L where { \n" +
				"   { "+strnode+" skos:altLabel ?L } union \n" +
				"   { ?L skos:altLabel "+strnode+" } \n" +
				"}\n" 
				// not yet used:
				,conceptid : node.id 
				,conceptnode : node
			});
			
			testwin.show();
		      }
		    }),	
			//End of AW's Modification
            new Ext.menu.Separator(), 
            new Ext.menu.Item({
  	          text : 'Query GO repository (definitions)',
              icon : 'icons/database_refresh.png',
              node : n,
              handler : function(item) {
                
                var uri = item.node.attributes.URI;
                var testwin = new Ext.app.RepositorySearch({
                  repository : loadState('repository', 'GO'),
                  query :
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
                    "\tfilter ((?class = <"+uri+">))\n" +
                    "}\n"
                  // not yet used:
                  ,conceptid : uri
                });
                
                testwin.show();
              }
            }),
          
            new Ext.menu.Item({
              text : 'Query GO repository (superClass)',
              icon : 'icons/database_refresh.png',
              node : n,
              handler : function(item) {
                  
                var uri = item.node.attributes.URI;
                var testwin = new Ext.app.RepositorySearch({
                  repository : loadState('repository', 'GO'),
                  query :
                    "prefix go: <http://purl.org/obo/owl/GO#>\n" +
                    "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
                    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "select distinct ?name  ?class ?definition\n" +
                    "where\n" +
                    "{   <"+uri+"> rdfs:subClassOf ?class.\n" +
                    "    ?class rdfs:label ?name.\n" +
                    "    ?class obo:hasDefinition ?def.\n" +
                    "    ?def rdfs:label ?definition \n" +
                    "}\n"
                  // not yet used:
                  ,conceptid : uri 
                });
                
                testwin.show();
              }
            })
            ]
          });
          
          contextMenu.show(n.ui.getAnchor());
        }
      }
    });
  },
  
  clear : function() {
  	
  	var old_root = this.root;
  	
    if (old_root) {
      while(old_root.firstChild) {
        old_root.removeChild(old_root.firstChild);
      }
    }
    
    //this.showRoot();
    /*	
    	for (var i=0; i<this.root.childNodes.length;i++){
    		this.root.removeChild(this.root.childNodes[i]);
    	  this.root.childNodes[i].remove();
    	}
    
      this.root.eachChild(
        function(n) {
        	console.log(n);
          if (n) {
            console.log("removing " + n.id);
            n.remove();
          }
        } 
      );
    }
    */
  },
  
  showRoot : function (th) {
  	
    if (this.repository !== '') {
  	
	    var repository = this.repository;
	    
	    var old_root = this.root.findChild('id', repository + '-root');
	    if (old_root) {
	      old_root.remove();
	    }
	    
	    this.root.appendChild(new Ext.tree.AsyncTreeNode({
	      expanded : true,
	      draggable : false,
	      disabled : true,
	      iconCls : 'loading-icon',
	      text : repository,
	      leaf : false,
	      id : repository + '-root',
	      cls : 'focusnode'
	    })); 
	    
	    var new_root = this.root; 
	    var tloader = this.loader;
		var browser = this;
    
	    var conn = new Ext.data.Connection({
	      defaultHeaders : {
	        'Accept': 'application/json'
	      }
	      ,method : 'POST'
	      
	    });

	    conn.request({
	      url : baseURL + '/Services/rest/thesaurusbrowser/rootnodes',
	      method : 'POST',
	      timeout : 60000,
	      params : {
	        ns          : this.ns,
	        server_url  : this.server_url,
      		repository  : getLongURL(this.repository),
	        username    : this.username,
	        password    : this.password,
	        rootnodes : 'true',
	        json : 'true',
	        target : 'ThesaurusBrowser'  
	      },
	      success : function(response, options) {
	        
	        // remove loading icon
	        var toAppend =    new_root.findChild('id', repository + '-root');
	        toAppend.ui.iconNode.className = 'x-tree-node-icon ' + 'broader-icon';

	        try {
	          //var _topterms = eval('(' + response.responseText + ')');
	          var decoded = Ext.util.JSON.decode(response.responseText);
	          var _topterms = decoded.topterms;
	          // foreach id in object
	          for (var i = 0; i < _topterms.length; i++) {
	            
	            //new_root.findChild('id', repository + '-root')
				toAppend.appendChild(new Ext.tree.AsyncTreeNode({
	                  //expanded : true,
	                  loader : tloader,
	                  draggable : true,
	                  iconCls : 'broader-icon',
	                  //text : _topterms[i].term + ' - <div class="concept-id"> &lt;<i>'+_topterms[i].id+'</i>&gt;</div>',
	                  text : _topterms[i].term,
	                  plainText : _topterms[i].term,
	                  id : _topterms[i].id,
	                  URI : _topterms[i].id,
	                  leaf : false,
	                  allowDrop : false
	                }));
	          }
	        } catch (err) {
	          Ext.MessageBox.alert('ERROR', 'Could not decode ' + response.responseText);
	        }
			browser.attemptChecking();
	      }
	      ,failure : loadFailed
	    });
    }
  },

  //Perform checking nodes that has just been appended

  attemptChecking: function(){
		if(this.wait_queue == undefined) return;
		if(this.wait_queue.length == 0) return; // we're done
		if(this.currently_checking > 1 || this.currently_expanding) {
			setTimeout(this.attemptChecking,300);
			return;
		}
		this.currently_checking ++;
		var curNode = this.wait_queue.pop(); // Should've use shift so this is stack now

		// At the end of check this node there is another call back to this function.
		this.checkThisNode(curNode);

 },


 checkThisNode: function(node){

		var conn = new Ext.data.Connection({ defaultHeaders : { 'Accept': 'application/json' } ,
											 method : 'POST' });
	
		var browser = this;

		conn.request({ 
          url :  '/Services/rest/thesaurusbrowser/narrower',
	      method : 'POST',
	      timeout : 60000,
	      params : {
	     	ns : '',
			uri : node.id,
            server_url : this.server_url,
            repository : this.repository,
            username : this.username,
            password : this.password,
	        
            json : 'true',
            target : 'ThesaurusBrowser'
           },
		   success : function(response, options){
				  var decoded = Ext.util.JSON.decode(response.responseText);

				  if(decoded.length == 0){
						node.leaf = true;
						if(node)
							node.ui.updateExpandIcon();
				  } 
					
				  // Decrease counter of running and call again next perform checking
				  browser.currently_checking--;
				  browser.attemptChecking();
			},
			failure :function(response,options){ 
						 //alert(response.responseText);
			} 

		});
	}
 
}); 

Ext.reg('thesaurusbrowser', Ext.app.ThesaurusBrowser);
