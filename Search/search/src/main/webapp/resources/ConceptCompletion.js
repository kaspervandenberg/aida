// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The concept search box
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      23 April 2009
* @version   $Id$
*
* @license 
*/


////////////////////////////////////////////////////////////////////////////////
// Concept completion combobox
////////////////////////////////////////////////////////////////////////////////
Ext.app.ConceptCompletion = Ext.extend(Ext.form.ComboBox, {
	
  initComponent:function() {
  	
    // store getting items from server
    var store = new Ext.data.JsonStore({
      id : 'URI',
      fields : [
        {name:'URI', type:'string'},
        {name:'text', type:'string'}
      ],
      // may be slow with a large list:
      remoteSort : false,
      sortInfo: {field: 'text', direction: 'ASC'},
      url : baseURL + '/Services/rest/thesaurusbrowser/suggest',
      baseParams : {
        node        : '',
  			ns          : this.baseParams.ns,
	      server_url  : this.baseParams.server_url,
	      repository  : this.baseParams.repository,
	      username    : this.baseParams.username,
	      password    : this.baseParams.password,
        suggest     : 'true',
  			json        : 'true',
  			target      : 'ThesaurusBrowser'
      },
      listeners : {
      	beforeload : function(store, opts) {
      		var query = store.baseParams.query;
      		store.baseParams.node = store.baseParams.query;
      		if (query === null || query.length < 2) {
      		  return false;
      	  } 
      		return true;
      	}
      }
    });
  	
    Ext.apply( this, {
    	triggerClass : 'x-form-search-trigger',
      baseParams : this.baseParams,
      store : store,
      // query all records on trigger click
      triggerAction : 'all',
	    // minimum characters to start the search
	    minChars : 2,
	    // do not allow arbitrary values
	    forceSelection : true,
	    // otherwise we will not receive key events
	    enableKeyEvents : true,
	    // make the drop down list resizable
	    resizable : true,
	    // we need wider list for paging toolbar
	    minListWidth : 300,
	    width : 300,
	    // force user to fill something
	    allowBlank : true,
	    // concatenate 
		  //tpl:'<tpl for="."><div class="x-combo-list-item">{text} &lt;<i>{id}</i>&gt;</div></tpl>'
      tpl:'<tpl for="."><div class="x-combo-list-item">{text}</div></tpl>'
		  // suggest
		  //typeAhead : true
    }); // end apply
  	
    // call parent initComponent
    Ext.app.ConceptCompletion.superclass.initComponent.call(this);
		
		this.on('select', function(combo, record, index) {
			var text = record.get('text');
			var id = record.get('URI');
			this.setRawValue(text);
			
      var suggestion = new Ext.tree.AsyncTreeNode({
        //expanded : true,
        loader : this.browser.getLoader(),
        draggable : true,
        iconCls : 'broader-icon',
        qtip : id,
        //id : id,
        plainText : text,
        URI : id,
        //text : text + ' - <div class="concept-id"> &lt;<i>'+id+'</i>&gt;</div>',
        text : text,
        leaf : false
      });
      
    	this.browser.root.appendChild(suggestion);
    	this.browser.root.collapseChildNodes(false);
    	suggestion.expand(false, false);
			
		}, this);

	  this.on('blur', function() {
      var val = this.getRawValue();
      this.setRawValue.defer(1, this, [val]);	
		}, this);
		
    this.on('render', function() { 
      this.el.set(
          {qtip:'Type at least ' + this.minChars + ' characters to search'}
      );
    }, this);
    
    
    // requery if field is cleared by typing
    this.on('keypress', function(f, e) {
    	//if (e.getKey() == e.ENTER) {
			  //var text = e.target.value;
			  //var id = record.get('id');
  			//this.setRawValue(text);
    	//}
    	
    	if(!this.getRawValue()) {
        this.doQuery('', true);
      }
      
    }, this);
    
    
		
 
  } // end of function initComponent
});
