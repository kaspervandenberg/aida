/**
 * @author edgar
 */

function DocCache() {
	var ds = new Ext.data.Store();	
	var raw_json;

  this.setDS = function(ds) {
	  this.ds = ds;		
		//console.log('updated ds, new count: ' + ds.getCount());
	};
	
  this.getDS = function() {
	  return this.ds;
	};
	
	this.setJSON = function(json) {
		this.raw_json = json;
	};
	
	this.count = function() {
		return this.ds.getCount();
	};
	
	this.addDoc = function (record) {
		this.ds.add(record);
	};
	
	this.showHTML = function (url) {
		var panel = new Ext.Panel({
			region: 'center',
			margins:'3 0 3 3',
			cmargins:'3 3 3 3',
			activeTab: 0,
			//autoScroll: true,
			html: '<iframe width="100%" height="100%" src="'+url+'">'
		});

		var win = new Ext.Window({
			title: 'Source document viewer',
			closable:true,
			width: 650,
			height: 450,
			//plain: true,
			layout: 'fit',
			items: [panel]
		});
		
		win.show();
	};

	this.showDoc = function(id) {
		
		var record;
		var txt = '<p>';
		
		for (var k in this.raw_json.items[id]) {
			if (k == 'undefinedtype') {
			  continue;
      }
			txt += '<p><h1>' + k + "</h1>  " + this.raw_json.items[id][k] + '</p>';
		}
		
		txt += '</p>';
    
    showWindow('Cached document viewer', txt);
	
	};

}