// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An extended TreePanel which searches through multiple repositories
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      Sep 2009
* @version   $Id$
*
* @license 
*/

// namespaces
Ext.ns('Ext.app');
var Tree = Ext.tree;

////////////////////////////////////////////////////////////////////////////////
//Thesauri search field
////////////////////////////////////////////////////////////////////////////////
Ext.app.TermField = Ext.extend(Ext.form.TwinTriggerField, {
  
  initComponent : function() {
    
    Ext.apply( this, {
      tree : this.tree,
      validationEvent : false,
      validateOnBlur : false,
      trigger1Class : 'x-form-clear-trigger',
      trigger2Class : 'x-form-search-trigger',
      hideTrigger1 : true,
      width : 180,
      hasSearch : false,
      query_value : '',
      emptyText: 'Enter a string to search for...'
    }); // end apply
    
    Ext.app.SearchField.superclass.initComponent.call(this);
    
    this.on('specialkey', function(f, e) {
      if (e.getKey() == e.ENTER) {
        this.onTrigger2Click();
      }
    }, this);
  },

  onTrigger1Click : function() { /// clear
    if (this.hasSearch) {
      this.el.dom.value = '';
      this.triggers[0].hide();
      this.hasSearch = false;
    }
  },

  onTrigger2Click : function() { /// search
    
    var v = this.getRawValue();
    if (v.length < 1) {
      this.onTrigger1Click();
      return;
    }
    
    // uber root node
    var rootN = this.tree.root;

    // for each open tab   
    //var repos = thesauruspanel.items;
    //repos.each(function(item) {
    //var repos = loadObject('storedrepositorylist');
    var repos = loadObject('openrepositorylist2', {});
    
    //array:
    //for (var i = 0; i < repos.length; i++) {
      //var item = r[0];
      //var r = repos[i];
      //var item = r;
    
    for (var item in repos) {
        
      // only consider repositories
      //if (item.id.substring(4,0) === 'http') {
      
        connection.request({
          url : baseURL + '/Services/rest/thesaurusbrowser/suggest',
          method : 'POST',
          params : {
            node        : v,
            server_url  : loadState('server_url'),
            repository  : item,
            username    : loadState('username'),
            password    : loadState('password')
          },
          success : function(response, options) {
            
            // fix this
            suggestions = eval(response.responseText);
           
            // sconsole.log(options);
            // add a node for this repository
            var root = new Ext.tree.AsyncTreeNode({
              expanded : true,
              loader : new Ext.tree.TreeLoader({
                dataUrl : baseURL + '/Services/rest/thesaurusbrowser/narroweralts',
                baseParams : {
                  server_url  : loadState('server_url'),
                  repository  : options.params.repository,
                  username    : loadState('username'),
                  password    : loadState('password')
                },
                listeners : {
                  beforeload : function(th, n, cb) {
                    return false;
                  }
                }
              }),
              draggable : false,
              iconCls : 'broader-icon',
              text : options.params.repository,
              leaf : false,
              disabled : true
            });
            
            rootN.appendChild(root);
           
            if (suggestions != null) {
              
              var loader = new Tree.TreeLoader({
                timeout : 60000,
                preloadChildren : true,
                dataUrl : baseURL + '/Services/rest/thesaurusbrowser/narroweralts',
                baseParams : {
                  server_url : loadState('server_url'),
                  repository : options.params.repository,
                  username    : loadState('username'),
                  password    : loadState('password')
                },
                listeners : {
                  beforeload : function(th, n, cb) { // listener, catches empty requests
                    
                    // make sure we send the URI as request to the webservice
                    if (n.attributes.URI) {
                      th.baseParams.uri = n.attributes.URI;
                    } else if (n.URI) {
                      th.baseParams.uri = n.URI;
                    } else {
                      th.baseParams.uri = n.id;
                    }
                    
                  }, 
                  load : function (th, n, r) {
                    n.expand(true, false);
                  }, 
                  loadException : loadFailed
                }
              });
     
              for(var i=0; i < suggestions.length; i++){
                var suggestion = new Ext.tree.AsyncTreeNode({
                  loader : loader,
                  draggable : true,
                  iconCls : 'broader-icon',
                  qtip : suggestions[i].id,
                  text : suggestions[i].text,
                  id : suggestions[i].id,
                  URI : suggestions[i].URI,
                  leaf : false
                });
            
                root.appendChild(suggestion);
                //suggestion.expand(false, false);
              }
            }
          },
         failure : loadFailed
        });
    
      }
    //};, this);
    
    this.hasSearch = true;
    this.triggers[0].show();
    
    }
  }
);
  
// application main entry point
Ext.app.ThesauriSearch = Ext.extend(Tree.TreePanel, {
  
  clear : function() {
    var old_root = this.root;
    if (old_root) {
      while(old_root.firstChild) {
        old_root.removeChild(old_root.firstChild);
      }
    }
  },
  
  initComponent : function() {
    
    this.rootID = this.rootID ? this.rootID : Ext.id();
    
    var searchfield = new Ext.app.TermField({
      emptyText : '1. Enter a concept to search for...'
    }); 
    
    Ext.apply( this, {
      loader : 
        new Tree.TreeLoader({
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
            }
          }
        }),
      closable : false,
      title : "Search",
      animate : false,
      border : false,
      enableDrag : true,
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
        new Ext.Button({
          tooltip : 'Clear the tree',
          iconCls : 'btn-clear',
          scope: this,
          handler : this.clear
        }),
        new Ext.app.TermField({
          tree : this
        })
      ]
    }
    ); // end apply
    
    Ext.app.ThesauriSearch.superclass.initComponent.call(this, arguments);
    
    // listeners
    this.on({
      'beforechildrenrendered' : {
        fn : function(node) {
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
      }
    });
  }
 
}); 

Ext.reg('thesaurisearch', Ext.app.ThesauriSearch);