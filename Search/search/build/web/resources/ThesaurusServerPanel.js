// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The function which draws the Server panel
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      5 April 2009
* @version   $Id$
*
* @license 
*/

function ThesaurusServerPanel() {
	
	document.getElementById('loading-msg').innerHTML = 'Initializing Thesaurus Server ...';
  
  /*
  var task = {
    run: function() {
        console.log("repository: " + loadState('repository', ''));
    },
    interval: 900 //.3 seconds
  }
  
  Ext.TaskMgr.start(task);
  */

	
  function repositorySelect(item, checked){
    
    var repo = item.value;
    if (repo === '') {
      return;
    }

	// Saving the longer URL 
    saveState('repository', getLongURL(repo));
    
    // add repo to the cache of opened repos
    var repoHash = loadObject('openrepositorylist2', {});
    if (! repoHash[repo] ) {
      repoHash[repo] = 1;
    }
    saveObject('openrepositorylist2', repoHash);
    
    // check to see whether we already have a tab for this server and repository
    var repotab = Ext.getCmp(loadState('server_url') + '-' + loadState('repository'));
    if (repotab) {
    	thesauruspanel.activate(Ext.getCmp(loadState('server_url') + '-' + loadState('repository')));
    	formpanel.collapse(true);
    	return;
    }
    
    var tb = new Ext.app.ThesaurusBrowser({
      ns : loadState('ns'),
      server_url : loadState('server_url'),
      repository : loadState('repository'),
      username : loadState('username'),
      password : loadState('password')
    });
    
    //var tp = thesauruspanel.items.itemAt(0);
    var tab = thesauruspanel.add(tb);
    thesauruspanel.setActiveTab(tab);
    tb.showRoot();
    formpanel.collapse(true);
  }
  
	var sstore = new Ext.data.SimpleStore({
    fields: ['url'],
    data: loadServers(),
    listeners : {
      beforeload : function (th, opts) {
        //showWindow('servers',loadServers());
      	return false;
      }
    }
  });

  // Loading direct server url if provided, if it is not there, then default base server_url
  loadState('direct_server_url', loadState('server_url',getBaseURL() + '/openrdf-sesame'))

  /// the server combobox
  var serverbox = new Ext.form.ComboBox({
    id : 'server_url',
    fieldLabel : 'Server',
		name : 'server_url',
		// Just in case we're using direct link, select it
		value : loadState('direct_server_url', loadState('server_url',getBaseURL() + '/openrdf-sesame')),
		enableKeyEvents: true,
		width : 290,
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		emptyText : 'Select or type in a server...',
		selectOnFocus : true,
		displayField : 'url',
		store : sstore,
		lazyInit : false,
		listeners : {
		  
		  render : function(th) {
		    var url = th.value;
			// If we use direct link, current server_url will be overwritten
			var dirurl = loadState("direct_server_url");
			if(dirurl) url = dirurl;

		    formpanel.getForm().setValues({server_url : url});
		    loadRepositories();
		    //saveState('repository', null);
		  },
		
			select : function(cb, record, index) {
				var server_url = record.get('url');

				if (server_url !== '' && server_url !== loadState('server_url', '')) {
					loadRepositories();
					saveState('repository', '');
					repositoryBox.clearValue();
        		}
			},
			focus : function(f) {
				f.setValue('');
			  sstore.load(loadServers(), false);
			},
			blur : function(f) {
				if (!f.getValue() && loadState('server_url')) {
					f.setValue(loadState('server_url'));
        }
			},
      specialkey : function(th, e) {
        if (e.getKey() == e.ENTER) {
          if (th.getValue() !== '') {
          	loadRepositories();
          	saveState('repository', '');
          	sstore.load(loadServers(), false);
          }
        }
      }
		}
	});
	
	
  /// the repository combobox
	var repositoryBox = new Ext.form.ComboBox({
		value : loadState('direct_repository', loadState('repository','dodol')),
		id : 'repository-box',
		fieldLabel : 'Repository',
		forceSelection: true,
		displayField: 'repository',
		mode: 'local',
		width : 290,
		typeAhead : true,    
		lazyInit : false,
		store: new Ext.data.SimpleStore({
	    //proxy : new Ext.data.MemoryProxy(repositories),
		id : 'repositoryDS',
	    data: [],
	    fields: [{name: 'repository'}],
	    listeners : {
	      beforeload : function (th, opts) {
			    //loadRepositories();
			    this.loadData(loadObject('storedrepositorylist', []), false);
	      		this.sort('repository','ASC');
	      		return false;
	      }
	    }
	  }),
		listeners : {
			blur : function(f) {
				if (!f.getValue() || f.getValue() === '') {
					this.setValue(loadState('repository', ''));
	      } 
			}, 
			focus : function(f) {
				this.clearValue();
				this.store.reload();
			},
	    select : repositorySelect
		}
    /*
		//value : loadState('server_url', ''),
		enableKeyEvents: true,
		width : 260,
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		emptyText : 'Select or type in a server...',
		selectOnFocus : true,
		displayField : 'url',
		store : serverds,
		listeners : {
			select : function(cb, record, index) {
				server_url = record.get('url');
				if (server_url !== '') { 
					saveState('server_url', server_url);
					repository = '';
					tabpanel.activate('repository-tab');
        }
				
			},
			focus : function(f) {
				f.setValue('');
				//repositoriestab.remove('repositorylist');
			},
			blur : function(f) {
				if (!f.getValue() && this.server_url) {
					f.setValue(this.server_url);
        }
        
        if (f.getValue() !== '') { 
        	saveState('server_url', f.getValue());
          addToStore(serverds, f.getValue());
          saveStore('server_url_ds', serverds);
        }
			},
      specialkey : function(th, e) {
        if (e.getKey() == e.ENTER) {
        	
          if (th.getValue() !== '') {	
          	server_url = th.getValue();
          	saveState('server_url', server_url);
          	addToStore(serverds, th.getValue());
          	saveStore('server_url_ds', serverds);
          	repository = '';
            tabpanel.activate('repository-tab');
          }
        }
      }
		}
		*/
	});
  
  // the password field
  // TODO: toggle clear password
  var passwordfield = new Ext.form.TextField({
    fieldLabel : 'Password',
    name : 'password',
    enableKeyEvents: true,
    id : 'password',
    allowBlank : false,
    inputType : 'password',
    value : loadState('password', 'opensesame'),
    width : 290,
    listeners : {
      change : function(th, newVal, oldVal) {
        saveState('password', newVal);
      },
      specialkey : function(th, e) {
        if (e.getKey() == e.ENTER) {
          loadRepositories();
        }
      }
    }
  });
  
  // the actual panel
  var formpanel = new Ext.FormPanel({
  	id : 'sesame-panel',
  	bodyStyle : 'padding:5px',
    defaults : {
      bodyStyle : 'padding:5px',
      autoHeight : true
    },
    defaultType : 'textfield',
    labelWidth : 75,
    collapsible : true,
    // titleCollapse : true,
    // Must keep this to false, otherwise the comboboxes get mixed up
    collapsed : false, //loadState('sesame-panel-collapsed', false),
    autoScroll: true,
    split : true,
    margins : '0 5 30 0',
    cmargins : '5 5 5 0',
    title: 'AIDA Repository Server',
    height: 250,
    url : getBaseURL() + '/Services/RepositoryGetRepositoriesSVL',
    monitorValid : true,
    region : 'south',
    lazyInit : false,
    items : [{
      xtype : 'fieldset',
      title : 'Server information',
      defaultType : 'textfield',
      name : 'serverinfo',
      id : 'serverinfo',
      items : [
        {name : 'read_write', value : 'r', inputType : 'hidden'}, 
        {name : 'json', value : 'true', inputType : 'hidden'},
				serverbox,
			  {
				id : 'username',
				enableKeyEvents: true,
				fieldLabel : 'Username',
				name : 'username',
				allowBlank : false,
				width: 290,
				value : loadState('username', 'testuser'),
				listeners : {
				  change : function(th, newVal, oldVal) {
					saveState('username', newVal);
				  },
				  specialkey : function(th, e) {
					if (e.getKey() == e.ENTER) {
						saveState('username', th.getValue());
					  loadRepositories();
					}
				  }
				}
	      }, 
	      passwordfield
      ]
    },{
      xtype : 'fieldset',
      title : 'Repositories',
      items : repositoryBox
      }
    ],
      listeners : {
		//  resize : function (th, adjWidth, adjHeight, rawWidth, rawHeight) {
		//    Ext.get('server_url').setWidth(adjWidth);
		//  }
		  collapse : function (panel) {
			//saveState('sesame-panel-collapsed', true);
		  },
		  expand : function (panel) {
			//saveState('sesame-panel-collapsed', false);
		  },
		  // Let's ignore state for this panel, otherwise the comboboxes get mixed up.
		  beforestaterestore : function (th, state) {
			return false;
		  }

    }
  });
  
  //formpanel.doLayout();
  
  return formpanel;
	
}
