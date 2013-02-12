// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The main entry point to the interface. Everything gets instantiated etc. from here.
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      20 March 2009
* @version   $Id$
*
* @license 
* @TODO (some are quite old :) )
*  - addlisteners to index en field toolbars
*  - hide page navigation at loading time
*  - same for 'searching'
*  - Show nothing when there are no results
*  - Add clear buttons to field and index comboboxes
*  - when hits = 0, rutrun also [{}], *add* hits: '0'
*  - handle parseexceptions
*  - jason.java: if no title, use id
*  - check results for My_Index
*  - indexer: medline files use pmid as id, add "path" field = PMID
*  - SHOW FILE LOCATION
*  - add json term suggestions a la http://extjs.com/deploy/dev/examples/form/forum-search.html
*  - add loading image div (api: loading-indicator)
*  - thesaurus browser: browse from root
*  - make getRoot() function, perhaps after thes. selection
*  - Update URL for easy bookmarking
*  - Advanced search box
*/

// Holds the search results as returned from the server to facilitate cache and source viewing
var cache = new DocCache();

// Some global search parameters
var curIndex = '';
var curField = 'content';
var curQuery = '';

// The request URL
var baseURL = getBaseURL();

// check if we can make a connection
var thesaurussuccess = null;
var searchsuccess = null;

var tokenDelimiter = ':';

// Let's go
Ext.onReady(function() {

  document.getElementById('loading-msg').innerHTML = 'Initializing Cache ...';
  
  Ext.get('center').hide();
  
  // cookie-based state manager
  Ext.state.Manager.setProvider(
    new Ext.state.CookieProvider({
      expires: new Date(new Date().getTime()+(1000*60*60*24*365)) //1 year from now
    })
  );
  
  //Ext.History.init();


  // Needed for faster tooltip displaying
  Ext.QuickTips.init();
  Ext.QuickTips.enable();

  
  document.getElementById('loading-msg').innerHTML = 'Initializing Search ...';
  
////////////////////////////////////////////////////////////////////////////////
// testing connection to servlets
////////////////////////////////////////////////////////////////////////////////
  connection = new Ext.data.Connection();  

  connection.request({
    url : baseURL + '/Services/ThesaurusBrowser',
    params : {test:1},
    success : function(response, options) {
      this.thesaurussuccess = true;
    },
    failure : function(response, options) {
      this.thesaurussuccess = false;
    }
  });
  
  connection.request({
    url : baseURL + '/search/jason',
    params : {test:1},
    success : function(response, options) {
      this.searchsuccess = true;
    },
    failure : function(response, options) {
      this.searchsuccess = false;
    }
  });

////////////////////////////////////////////////////////////////////////////////
// Search Results data store and parser
////////////////////////////////////////////////////////////////////////////////
  // Reader for the search results
  var searchResultsReader = new Ext.data.JsonReader({
    totalProperty : "hits", // The property which contains the total dataset size (optional)
    root : "items", // The property which contains an Array of row objects
    id : "id" // The property within each row object that provides an ID for the record (optional)
  }, // Record defining the json search results:
    Ext.data.Record.create([{
      name : 'title'
    }, {
      name : 'snippet'
    }, {
      name : 'score',
      type : 'float'
    }, {
      name : 'id'
    }, {
      name : 'content',
      mapping : 'description'
    }, {
      name : 'file',
      mapping : 'path'
    }, {
      name : 'URL',
      mapping : 'uri'
    }, {
      name : 'content',
      mapping : 'description'
    }])
  );

  // create the Data Store
  searchDataStore = new Ext.data.Store({
    proxy : new Ext.data.HttpProxy({
      url : baseURL + '/search/jason'
    }),
    reader : searchResultsReader,
    // turn on server-side sorting
    remoteSort : true,
    baseParams : {
      count : 10,
      target : 'search',
      index : this.curIndex,
      field : 'content',
      query : this.curQuery
    },
    listeners : {
      // Fires if an exception occurs in the Proxy during data loading.
      // Called with the signature of the Proxy's "loadexception" event.
      // Handle any exception that may occur in the Proxy during data loading.
      loadexception : loadFailed,
      beforeload : function(store, opts) {
  
        this.curQuery = searchBar.query_value;
        // add the concepts from the query builder
        /*
        var concepts = thesaurus.getAllConcepts();
        var conceptquery = '';
        //for ( var i in concepts ) {
        for(var i=0; i < concepts.length; i++){
          conceptquery += ' ' + concepts[i];
        }
        store.baseParams.query = store.baseParams.query + conceptquery;
        */
        store.baseParams.query = searchBar.query_value + ' ' + thesaurus.getAllConcepts();
        
        if (store.baseParams.query.length <= 1) {
          return false;
        }
    
        if (store.baseParams.index.length < 1) {
          return false;
        }
    
        if (store.baseParams.field.length < 1) {
          return false;
        }
        
        // clear welcome text
        Ext.get('center').dom.innerHTML  = ''; 
        view.emptyText = '<div class="nohits">Your search <b>'+searchDataStore.baseParams.query+'</b> did not match any documents.</div>';
        view.show();
        
      },
      datachanged : function(store) {
        //this.curQuery = store.baseParams.query;
        //this.curQuery = searchResultsReader.jsonData.query;
    
        // Shorten the query to make it fit in the bottombar
        var shortQuery = store.baseParams.query;
        if (shortQuery.length > 50) {
          shortQuery = shortQuery.substring(0, 50) + '...';
        }
    
        if (store.baseParams.index !== '' && store.baseParams.query !== '') {
        
          searchpanel.getBottomToolbar().displayMsg = 'Results {0} - {1} of {2} for <b>' + shortQuery + '</b>';
          
          // update json cache
          cache.setJSON(searchResultsReader.jsonData);
        }
    
      }
    }
  });
  
////////////////////////////////////////////////////////////////////////////////
// Index combobox
////////////////////////////////////////////////////////////////////////////////
  // create the Data Store for the indices
  var indexds = new Ext.data.Store({
    reader : new Ext.data.JsonReader({
      root : 'indexes'
    }, [{
      name : 'index'
    }]),
    proxy : new Ext.data.HttpProxy({
      url : baseURL + '/search/jason'
    }),
    baseParams : {
      target : 'indexes'
    },
    listeners : {
      load : function(th, records, opts) {
        var index = loadState('curIndex');
        ////console.log(th);
        ////console.log(records);
        ////console.log(opts);
        if (this.find('index', index) != -1) {
          curIndex = index;
          indexBox.setValue(curIndex);
          searchDataStore.baseParams.index = curIndex;
          
          fieldds.baseParams = {
            target : 'fields',
            index : curIndex
          };
          fieldds.reload();
          //curField = loadState('curField', 'content');
          //fieldBox.setValue(curField);
        }
        
      },
      // Fires if an exception occurs in the Proxy during data loading.
      // Called with the signature of the Proxy's "loadexception" event.
      // Handle any exception that may occur in the Proxy during data loading.
      loadexception: loadFailed
    }
  });
  
  indexds.setDefaultSort('index', 'asc');
  indexds.load();

  // create the inputbox
  var indexBox = new Ext.form.ComboBox({
    id : 'indexBox',
    name : 'index',
    width : 150,
    typeAhead : true,
    mode : 'local',
    //triggerAction: 'all',
    emptyText : 'Select an index...',
    selectOnFocus : true,
    displayField : 'index',
    lazyInit : false,
    triggerAction : 'all',
    store : indexds,
    forceSelection : true,
    tabIndex : 1,
    title : 'Indexes:',
    listeners : {
      select : function(cb, record, index) {
        curIndex = record.get('index');
        fieldds.baseParams = {
          target : 'fields',
          index : curIndex
        };
        
        fieldds.reload();
        fieldBox.setValue('content');
        curField = 'content';
    
        saveState('curIndex', curIndex);
        saveState('curField', curField);
        
        searchDataStore.baseParams.index = curIndex;
        if (searchDataStore.baseParams.query !== "") {
          searchDataStore.reload();
        }
        
        searchBar.focus(true);
      },
      focus : function(f) {
        f.setValue('');
      },
      blur : function(f) {
        if (!f.getValue() && curIndex) {
          f.setValue(curIndex);
        }
      }
    }
  });

////////////////////////////////////////////////////////////////////////////////
// Field combobox
////////////////////////////////////////////////////////////////////////////////
  
  // create the Data Store for the fields
  var fieldds = new Ext.data.Store({
    reader : new Ext.data.JsonReader({
      root : 'fields'
    }, [{
      name : 'field'
    }]),
    proxy : new Ext.data.HttpProxy({
      url : '/search/jason'
    }),
    remoteSort : true,
    baseParams : {
      target : 'fields',
      index : this.curIndex
    },
    listeners : {
      load : function(th, records, opts) {
        var field = loadState('curField');
        ////console.log(th);
        ////console.log(records);
        ////console.log(opts);
        if (this.find('field', field) != -1) {
          curField = field;
          fieldBox.setValue(field);
          searchDataStore.baseParams.field = curField;
        }
      },
      // listener, catches empty requests
      beforeload : function(th, opts) {
        if (th.baseParams.index === '') { // empty index, no need to query        
          return false;
        }
      },
      // Fires if an exception occurs in the Proxy during data loading.
      // Called with the signature of the Proxy's "loadexception" event.
      // Handle any exception that may occur in the Proxy during data loading.
      loadexception : loadFailed
    }
  });
  fieldds.setDefaultSort('field', 'asc');
  fieldds.load();

  var fieldBox = new Ext.form.ComboBox({
    id : 'fieldBox',
    name : 'field',
    width : 125,
    typeAhead : true,
    lazyInit : false,
    mode : 'local',
    emptyText : 'Select a field...',
    //selectOnFocus : true,
    displayField : 'field',
    store : fieldds,
    forceSelection : true,
    tabIndex : 2,
    title : 'Defined fields:',
    listeners : {
      select : function(cb, record, index) {
        curField = record.get('field');
        saveState('curField', curField);
        searchDataStore.baseParams.field = curField;
        searchBar.focus(true);
      },
      blur : function(f) {
        if (!f.getValue()) {
          if (f.selectByValue('content')) {
            if (curField) {
              f.setValue(curField);
            } else {
              f.setValue('content');
              curField = 'content';
            }
          } else {
            curField = '';
          }
          saveState('curField', curField);
        }
      }, 
      focus : function(f) {
        f.setValue('');
      } 
    }
  });
  
////////////////////////////////////////////////////////////////////////////////
// Search input box
////////////////////////////////////////////////////////////////////////////////
  searchBar = new Ext.app.SearchField({
    store : searchDataStore,
    width : 100,//1100 - indexBox.width - fieldBox.width,
    id : 'querybox'
  });
  
  ////////////////////////////////////////////////////////////////////////////////
// Functions
////////////////////////////////////////////////////////////////////////////////
  
  function resizeSearchBox(th, adjWidth, adjHeight, rawWidth, rawHeight) {
    var newWidth = adjWidth -100 - Ext.get('indexBox').getWidth() - Ext.get('fieldBox').getWidth();
    searchBar.setWidth(newWidth);
    searchBar.repaintTriggers();
    
    searchpanel.getBottomToolbar().progBarWidth = newWidth;
  }
  
////////////////////////////////////////////////////////////////////////////////
// Result field
////////////////////////////////////////////////////////////////////////////////

/*
 * http://www.extjs.com/deploy/dev/examples/dd/dragdropzones.js
 * 
 * Here is where we "activate" the DataView.
 * We have decided that each node with the class "patient-source" encapsulates a single draggable
 * object.
 *
 * So we inject code into the DragZone which, when passed a mousedown event, interrogates
 * the event to see if it was within an element with the class "patient-source". If so, we
 * return non-null drag data.
 *
 * Returning non-null drag data indicates that the mousedown event has begun a dragging process.
 * The data must contain a property called "ddel" which is a DOM element which provides an image
 * of the data being dragged. The actual node clicked on is not dragged, a proxy element is dragged.
 * We can insert any other data into the data object, and this will be used by a cooperating DropZone
 * to perform the drop operation.
 */
function initializeResultDragZone(v) {
    v.dragZone = new Ext.dd.DragZone(v.getEl(), {

//      On receipt of a mousedown event, see if it is within a draggable element.
//      Return a drag data object if so. The data object can contain arbitrary application
//      data, but it should also contain a DOM element in the ddel property to provide
//      a proxy to drag.
        getDragData: function(e) {
            var sourceEl = e.getTarget(v.itemSelector, 10);
            if (sourceEl) {
                d = sourceEl.cloneNode(true);
                d.id = Ext.id();
                return v.dragData = {
                    sourceEl: sourceEl,
                    repairXY: Ext.fly(sourceEl).getXY(),
                    ddel: d,
                    result: v.getRecord(sourceEl).data
                }
            }
        },

//      Provide coordinates for the proxy to slide back to on failed drag.
//      This is the original XY coordinates of the draggable element.
        getRepairXY: function() {
            return this.dragData.repairXY;
        }
    });
}
  
  // Custom rendering Template for the Results view
  // uses custom String.endsWith method, see utils.js 
  var resultTpl = new Ext.XTemplate(
    '<tpl for=".">',
    '<div class="search-item">',
    '<a class="download" alt="download" title="Download this document" href="{URL}" target="_blank"><img src="icons/application_put.png"/></a>',
    '<h3>',
      //'<tpl if "this.isPDF(file) == true">',
        '<a ext:qtip="<img src=\'{URL}&thumbnail=true\'/>" class="a" href="#" onclick="cache.showHTML(\'{URL}\')">',
      //'</tpl>',
      //'<tpl if "this.isPDF(file) == false">',
      //  '<a class="a" href="#" onclick="cache.showHTML(\'{URL}\')">',
      //'</tpl>',
      '{title}',
      '</a>',
    '</h3>',
    '{snippet}',
    '<div class="g">{file} <a class="c" href="#" onclick="cache.showDoc({[xindex-1]})">cached</a></div>',
    '</div></tpl>'
    //,{
    //  isPDF: function(file) {
    //    //console.log(file);
    //    return file.toLowerCase().endsWith('pdf'); 
    //  }
    //}
  );
      
  resultTpl.compile();
  
  var view = new Ext.DataView({
    tpl : resultTpl,
    store : searchDataStore,
    itemSelector : 'div.search-item',
    emptyText : '<div class="nohits">Your search did not match any documents.</div>',
    //multiSelect : false,
    //singleSelect : true,
    loadingText : "Searching...",
    loadMask : true,
    autoWidth : true,
    autoHeight : true
      //plugins: [
      //    new Ext.DataView.DragSelector()
      //]
    ,listeners: {
      //render: initializeResultDragZone
    }
  });
  
  function showQuery(th, e) {
    showWindow('Query', searchDataStore.baseParams.query);
  } 

////////////////////////////////////////////////////////////////////////////////
// Build the search panel 
////////////////////////////////////////////////////////////////////////////////
  var viewsize = Ext.getBody().getViewSize();
  
  var searchpanel = new Ext.Panel({
    id : 'searchpanel',
    region : 'center',
    title : 'AIDA Search',
    autoExpandColumn : 'snippet',
    maxRowsToMeasure : 2,
    layout : 'fit',
    //height : viewsize.height - 300,
    //enableColumnMove : true,
    //autoWidth: true,
    //width : viewsize.width - 600,
    enableDragDrop : true,
    enableRowHeightSync : true,
    autoScroll : true,
    containerScroll : true,
    margins: '5 0 30 5',
    //collapsible:true,
    items : view,
    tbar : ['Search: ', ' ', indexBox, fieldBox, searchBar
    //,login
    ],
    contentEl:'center'
    ,bbar : new Ext.ux.plugins.ProgressPagingToolbar({
              progBarWidth : 800 - indexBox.width - fieldBox.width,
              defaultText : '',
      store : searchDataStore,
      pageSize : 10,
      displayInfo : true,
      displayMsg : 'Results {0} - {1} of {2} for <b>' + searchDataStore.baseParams.query + '</b>',
      emptyMsg : "",
      paramNames : {
        start : 'start',
        limit : 'count'
      },
      items : [new Ext.Button({
        tooltip : 'Show query',
        iconCls : 'query-icon',
        handler : showQuery
      })]
    })
    ,listeners : {
      resize : resizeSearchBox,
      render : function(th) {
        view.hide();
      }
    }
  });

  // fill the cache for the cached viewer
  cache.setDS(searchDataStore);
  
  // Thesaurus panel, see ThesaurusPanel.js
  thesaurus = new ThesaurusPanel();
  
////////////////////////////////////////////////////////////////////////////////
// Let's paint the interface
////////////////////////////////////////////////////////////////////////////////

  setTimeout(function() {
    // good to go?
    if (this.searchsuccess === false) { // something wrong with search
      errorMessage("I couldn't connect to the search interface at " + 
                  "<a href='" + baseURL + "/search/jason?"+Ext.urlEncode(searchDataStore.baseParams)+"'> " + baseURL + "/search/jason</a>" + 
                  " in time. <br/><br/>Please refresh the page, check the installation, or contact an administrator.");
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({
          remove : true
        });
    } else if (this.thesaurussuccess === false) { // something wrong with thesaurus
      errorMessage("I couldn't connect to the thesaurus interface at " + 
                  "<a href='" + baseURL + "/Services/ThesaurusBrowser?"+Ext.urlEncode(thesaurus.getTreeLoader().baseParams)+"'> " + baseURL + "/Services/ThesaurusBrowser</a>" +
                  " in time. <br/><br/>Please refresh the page, check the installation, or contact an administrator.");
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({
          remove : true
        });                  
    } else if (!this.searchsuccess && !this.thesaurussuccess) {// something completely wrong!
      errorMessage("I could't connect to the thesaurus interface at " + 
                  "<a href='" + baseURL + "/Services/ThesaurusBrowser?"+Ext.urlEncode(thesaurus.getTreeLoader().baseParams)+"'> " + baseURL + "/Services/ThesaurusBrowser</a>" +
                  " or to the search interface at " + 
                  "<a href='" + baseURL + "/search/jason?"+Ext.urlEncode(searchDataStore.baseParams)+"'> " + baseURL + "/search/jason</a>" +
                  " in time. <br/><br/>Please refresh the page, check the installation, or contact an administrator.");
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({
          remove : true
        }); 
    } else {

      Ext.get('center').show();
      
      rightview = new Ext.Panel({
        layout: 'border',
        id: 'thesaurus-port',
        region: 'east',
        border: false,
        split: true,
        autoWidth: false,
        width: loadState('thesaurus-port-width', 425),
        minSize: 150,
        bodyStyle: 'background:#FFF;',
        items: [
          thesaurus.getQueryBuilder(), 
          thesaurus.getPanel(), 
          thesaurus.getFormPanel()],
        listeners : {
          resize : function(th, adjWidth, adjHeight, rawWidth, rawHeight ) {
            saveState('thesaurus-port-width', adjWidth);
          }
        }
      });
      
      // Fill the screen with the two panels
      viewport = new Ext.Viewport({
        layout : 'border',
        border: false,
        items : [
          {
            layout: 'border',
            id: 'search-port',
            region:'center',
            border: false,
            split:true,
            //width: 400,
            //minSize: 150,
            //maxSize: 600,
            bodyStyle: 'background:#FFF;',
            items: [searchpanel]
          }, 
          rightview
        ],
        listeners : {
          render : function(th) {
            //ds.load();
            //th.add(repositorysearchpanel);
          }
        }
      });
      
      thesaurus.getQueryBuilder().render();
      thesaurus.getQueryBuilder().expand(false, /*no anim*/ false);
      
      // Render the tree
      if (thesaurus.getBrowser().root) {
	if((typeof thesaurus.getBrowser().showRoot) != 'undefined')
        	thesaurus.getBrowser().showRoot();
      }
      
      Ext.get('loading').remove();
      Ext.get('loading-mask').fadeOut({
        remove : true
      }); 
      
      // Fix: 
      // http://extjs.com/forum/showthread.php?t=16650
      Ext.getCmp('advsearchpanel').dropZone.setPadding(0,0,Ext.getCmp('advsearchpanel').getInnerHeight()-Ext.getCmp('advsearchpanel').getFrameHeight(),0);
      
    }
  }, 1000);

});
