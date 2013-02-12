// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An extended GridPanel which displays the results of a SPARQL query
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      17 April 2009
* @version   $Id$
*
* @license 
*/

// namespaces
//Ext.ns('Ext.app');

Ext.data.DynamicJsonReader = function(config){
    Ext.data.DynamicJsonReader.superclass.constructor.call(this, config, []);
};
Ext.extend(Ext.data.DynamicJsonReader, Ext.data.JsonReader, {
    getRecordType : function(data) {
        var i = 0, arr = [];
        for (var name in data[0]) { arr[i++] = name; } // is there a built-in to do this?
        
        this.recordType = Ext.data.Record.create(arr);
        return this.recordType;
        },
        
    readRecords : function(o){ // this is just the same as base class, with call to getRecordType injected
      this.jsonData = o;
      var s = this.meta;
    	var sid = s.id;
    	
    	var totalRecords = 0;
    	if(s.totalProperty){
        var v = parseInt(eval("o." + s.totalProperty), 10);
          if(!isNaN(v)){
            totalRecords = v;
          }
      }
    	var root = s.root ? eval("o." + s.root) : o;
    	
    	var recordType = this.getRecordType(root);
    	var fields = recordType.prototype.fields;
    	
        var records = [];
	    for(var i = 0; i < root.length; i++){
		    var n = root[i];
	        var values = {};
	        var id = (n[sid] !== undefined && n[sid] !== "" ? n[sid] : null);
	        for(var j = 0, jlen = fields.length; j < jlen; j++){
	            var f = fields.items[j];
	            var map = f.mapping || f.name;
	            var v = n[map] !== undefined ? n[map].value : f.defaultValue;
	            v = f.convert(v);
	            values[f.name] = v;
	        }
	        var record = new recordType(values, id);
	        record.json = n;
	        records[records.length] = record;
	    }
	    return {
	        records : records,
	        totalRecords : totalRecords || records.length
	    };
    }
});

Ext.grid.DynamicColumnModel = function(store){
    var cols = [];
    var recordType = store.recordType;
    var fields = recordType.prototype.fields;
    
    for (var i = 0; i < fields.keys.length; i++)
    {
        var fieldName = fields.keys[i];
        var field = recordType.getField(fieldName);
        cols[i] = {header: field.name, dataIndex: field.name, width:300, sortable:true};
    }
    Ext.grid.DynamicColumnModel.superclass.constructor.call(this, cols);
};
Ext.extend(Ext.grid.DynamicColumnModel, Ext.grid.ColumnModel, {});

