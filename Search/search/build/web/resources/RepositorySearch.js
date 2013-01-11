// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An extended Window which displays the results of a passed-in search or concept
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      21 April 2009
* @version   $Id$
*
* @license 
*/

// namespaces
Ext.ns('Ext.app');

// application main entry point
Ext.app.RepositorySearch = Ext.extend(Ext.Window, {
  
  runNewQuery : function () {
	  this.query = this.items.get(0).getValue();
	  this.addResultGrid(this);
  },

  toggleTemplateQuery : function (){
	  this.query = this.items.get(0).getValue();
	  if(this.query.indexOf("skos")>=0){
	     if(this.query.indexOf("Narrower")>=0)
		  this.query = owl_narrow_query(this.conceptnode)
         else
		  this.query = owl_top_query(this.conceptnode)
	  }
      else{
	     if(this.query.indexOf("Narrower")>=0)
		  this.query = skos_narrow_query(this.conceptnode)
		 else
		  this.query = skos_top_query(this.conceptnode)
	  }
	  this.items.get(0).setValue(this.query); 
	  this.addResultGrid(this);
  },

  runNewRepository : function (cb, record, index) {
	  this.repository = record.get('repository');
	  this.addResultGrid(this);
  },
  
  switchProxy : function (item, pressed) {
    this.useProxy = pressed;
	  this.runNewQuery();
  },
	
  addResultGrid : function (th) {
		
		var proxy;
		var url = baseURL + '/Services/RepositorySelectQuerySVL';
		
		if (this.useProxy) {
		  proxy = new Ext.data.ScriptTagProxy({url: url, timeout : 60000});
		} else {
		  proxy = new Ext.data.HttpProxy({url: url, timeout : 60000});
		}
		
		// create the Data Store
    var ds = new Ext.data.Store({
      proxy : proxy,
      reader : new Ext.data.DynamicJsonReader({root : 'results.bindings'}),
  		baseParams: {
        axis_url : baseURL + '/axis',
        server_url : loadState('server_url', getBaseURL() + '/openrdf-sesame'),
        repository : this.repository,
        username : loadState('username', 'testuser'),
        password : loadState('password', 'opensesame'),
        query_language : 'sparql',
        select_output_format : 'json',
        query : this.query,
        format : 'json',
        remote_url: 'http://sparql.neurocommons.org:8890/sparql'
      },
      remoteSort : false
    });
   
    ds.on('beforeload', function() {
      th.startTime = new Date();
    });
         
    ds.on('load', function() {
      // Reset the Store's recordType
      ds.recordType = ds.reader.recordType;
      ds.fields = ds.recordType.prototype.fields;
      
			var repositorysearchpanel = new Ext.grid.GridPanel({
	      ds: ds,
	      margins : '0 0 5 0',
	      title : 'Results',
	      viewConfig: {forceFit: true},  
				enableDragDrop : true,
				autoScroll : true,
				containerScroll : true,
	      //autoWidth : true,
	      //height : 10,
		  loadMask : true,
	      anchor : '100%, -195',
	      cm: new Ext.grid.DynamicColumnModel(ds),
	      selModel: new Ext.grid.RowSelectionModel({singleSelect:true}),
	      enableColLock:true,
	      sm : new Ext.grid.RowSelectionModel({singleSelect:true})
		  });
		  
			if (th.items.get(1)) {
			  th.remove(th.items.get(1));
			}
		  
		  th.add(repositorysearchpanel);
		  th.doLayout();
		  
		  var diff = (new Date().getTime() - th.startTime.getTime()) / 1000;
		  th.getBottomToolbar().setStatus({
        text: 'OK, execution time: ' + diff + ' seconds',
        iconCls: 'x-status-custom',
        clear: false 
      });
    
    });
    
    ds.on({
    	'loadexception' : function(th, opts, r, e) {
    		var error = loadFailed(th, opts, r, e);
			  this.getBottomToolbar().setStatus({
	        text: 'Error: ' + error,
	        iconCls: 'x-status-custom',
	        clear: false 
	      });
	      
				if (this.items.get(1)) {
					this.items.get(1).getStore().removeAll();
				  this.items.get(1).loadMask.hide();
				}
	      
    	}, scope : this
    });
        
		if (th.items.get(1)) {
		  th.items.get(1).loadMask.show();
		}
    th.getBottomToolbar().showBusy();
    ds.load(); 
		
	},
	
  initComponent : function() {     
  	
  	this.useProxy = false;
  	
  	this.query = this.query ? this.query : 
  	/*
  	"prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
  	"prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
  	"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
  	"select  distinct ?name  ?class ?deflabel\n" +
  	"where\n" +
  	"{\n" +
  	"\t?class rdf:type owl:Class.\n" +
  	"\t?class rdfs:label ?name.\n" +
  	"\toptional {\n" +
  	"\t\t?class obo:hasDefinition ?def.\n" +
  	"\t\t?def rdfs:label ?deflabel \n" +
  	"\t}\n" +
  	"\tfilter ((?class = <http://purl.org/obo/owl/GO#GO_0004888>))\n" +
  	"}\n"
  	*/
  	'select * where { ?q ?t ?v } limit 200';
  	
  	//this.proxy = this.proxy ? this.proxy : new Ext.data.HttpProxy({url: baseURL + '/Services/RepositorySelectQuerySVL'});

  	this.repository = this.repository ? this.repository : loadState('repository', 'mem-rdf');
  	this.title = this.title ? this.title : 'AIDA Repository Query Window';
  
		var repositoryBox = new Ext.form.ComboBox({
	    fieldLabel : 'Repository',
	    forceSelection: true,
	    displayField: 'repository',
	    mode: 'local',
	    width : 150,
	    typeAhead : true,    
	    lazyInit : false,
	    value : loadState('repository', ''),
	    emptyText : 'Select a repository...',
	    //anchor : '100%, -100',
	    store: new Ext.data.SimpleStore({
		    data: loadObject('storedrepositorylist', []),
		    fields: [{name: 'repository'}],
		    listeners : {
		      beforeload : function (th, opts) {
		      	//loadRepositories();
		      	rstore.sort('repository'); 
		      	return false;
		      }
		    }
		  }),
			listeners : {
				focus : function(f) {
					//loadRepositories();
					this.store.loadData(loadObject('storedrepositorylist', []), false);
					f.setValue('');
				},
				blur : function(f) {
					if (!f.getValue()) {
						f.setValue(loadState('repository', ''));
	        }
				}
			}
		});
		
		repositoryBox.on('select', this.runNewRepository, this);
	    
    Ext.apply( this, {
      closable : true,
      maximizable : true,
      width : 640,
      height : 480,
      plain : true,
      layout : 'anchor',
      items: [
        new Ext.form.TextArea({
        	height : 350,
        	anchor : '100%, -200',
        	//autoHeight: true,
          autoScroll: true,
          value : this.query,
          enableKeyEvents : true,
          listeners : {
            keypress : function (th, e) {
            	if (e.ctrlKey && e.keyCode == e.ENTER) {
            		this.findParentByType('respositorysearch').runNewQuery();
            	} 
            }
          }
        })
      ],
      bbar : new Ext.StatusBar({

        // defaults to use when the status is cleared:
        defaultText: 'Ready',
				// defaultIconCls: 'default-icon',
        
        // values to set initially:
        text: this.defaultText,
        // iconCls: 'ready-icon',
        items: [
        '->',
        {
          text: 'Use proxy',
          enableToggle: true,
	        scope: this,
          toggleHandler: this.switchProxy
        } 
        ]
      }),
      tbar : [
        //'Repository: ', ' ',
        repositoryBox,
				{
  	      text : 'Run query (ctrl+enter)',
	        //tooltip : 'Run',
	        iconCls : 'btn-refreshdb',
	        scope: this,
	        handler : this.runNewQuery
	      },
		  {
  	        text : 'Skos/Non-Skos',
			tooltip: 'Toggle between Skos or non Skos query template',
	        iconCls : 'btn-refreshdb',
	        scope: this,
	        handler : this.toggleTemplateQuery
	      }
		
		  ]
    }
    ); // end apply
    
    Ext.app.RepositorySearch.superclass.initComponent.call(this, arguments);
    
    // listeners
    this.on({
    	'render' : this.addResultGrid
    });
    
    this.on({
    	'resize' : function(th, w, h) {
    		this.items.get(0).setWidth(w);
    	}
    });
    
  } // end initComponent

 
 
}); 

Ext.reg('respositorysearch', Ext.app.RepositorySearch);
