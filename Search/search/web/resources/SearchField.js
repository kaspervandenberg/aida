// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The term and concepts search boxes
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      5 April 2009
* @version   $Id$
*
* @license 
*/

////////////////////////////////////////////////////////////////////////////////
// Query search field
////////////////////////////////////////////////////////////////////////////////
Ext.app.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
	initComponent : function() {
		
		Ext.app.SearchField.superclass.initComponent.call(this);
		
		this.on('specialkey', function(f, e) {
			if (e.getKey() == e.ENTER) {
				this.onTrigger2Click();
			}
		}, this);
	},

	trigger1Class : 'x-form-clear-trigger',
	trigger2Class : 'x-form-search-trigger',
	hideTrigger1 : true,
	hasSearch : false,
	paramName : 'query',
	query_value : '',
	emptyText: 'Type in a query...',

	onTrigger1Click : function() { // clear
		
		this.el.dom.value = '';
		var o = {
			start : 0
		};
		this.store.baseParams = this.store.baseParams || {};
		this.store.baseParams[this.paramName] = '';
		this.hasSearch = false;
    this.repaintTriggers();
	},

	onTrigger2Click : function() { // search
		
		var v = this.getRawValue();
    this.query_value = v;
		//if (v.length < 1) {
		//	this.onTrigger1Click();
		//	return;
		//}
		var o = {
			start : 0
		};
		this.store.baseParams = this.store.baseParams || {};
		this.store.baseParams[this.paramName] = v;
		this.store.reload({
			params : o
		});
		this.hasSearch = true;
		
    this.repaintTriggers();
	},
  
  repaintTriggers : function() {
    this.triggers[0].hide();
    this.triggers[1].hide();
    this.triggers[1].show();
    if (this.hasSearch) {
      this.triggers[0].show();
    } else {
      this.triggers[0].hide();
    }
  }
});

////////////////////////////////////////////////////////////////////////////////
// password field
////////////////////////////////////////////////////////////////////////////////
Ext.app.PasswordField = Ext.extend(Ext.form.TriggerField, {
	initComponent : function() {
		Ext.app.PasswordField.superclass.initComponent.call(this);
    
	},
  inputType : 'password',

	validateOnBlur : false,
	//triggerClass : 'x-form-search-trigger',

	onTriggerClick : function() { // toggle
		//this.el.dom.value = '';
    //console.log(this);
    if (this.inputType === 'password') {
      this.inputType = 'text';
    } else {
      this.inputType = 'password';
    }
    //textual = new Ext.app.PasswordField(this);
	} 
});
