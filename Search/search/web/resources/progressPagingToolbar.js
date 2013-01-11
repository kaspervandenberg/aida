
/*
	Author       : Jay Garcia
	Site         : http://tdg-i.com
	Blog post    : http://tdg-i.com/63/how-to-integrate-the-progress-bar-with-the-paging-toolbar
	Contact Info : jgarcia@tdg-i.com
	Purpose      : An integrated progress bar for the Paging toolbar 
	Warranty     : none
	Price        : free
	Version      : b1
	Date         : 01/29/2009
	
*/
Ext.ns('Ext.ux', 'Ext.ux.plugins');
Ext.ux.plugins.ProgressPagingToolbar = Ext.extend(Ext.PagingToolbar, {
	defaultAnimCfg : {
		duration : 1,
		easing   : 'bounceOut'	
	},												  
	onRender : function(ct, position){
		Ext.PagingToolbar.superclass.onRender.call(this, ct, position);
		this.first = this.addButton({
			tooltip: this.firstText,
			iconCls: "x-tbar-page-first",
			disabled: true,
			handler: this.onClick.createDelegate(this, ["first"])
		});
		this.prev = this.addButton({
			tooltip: this.prevText,
			iconCls: "x-tbar-page-prev",
			disabled: true,
			handler: this.onClick.createDelegate(this, ["prev"])
		});
		this.addSeparator();
		this.add(this.beforePageText);
		this.field = Ext.get(this.addDom({
		   tag: "input",
		   type: "text",
		   size: "3",
		   value: "1",
		   cls: "x-tbar-page-number"
		}).el);
		this.field.on("keydown", this.onPagingKeydown, this);
		this.field.on("focus", function(){this.dom.select();});
		this.field.on("blur", this.onPagingBlur, this);
		this.afterTextEl = this.addText(String.format(this.afterPageText, 1));
		this.field.setHeight(18);
		this.addSeparator();
		this.next = this.addButton({
			tooltip: this.nextText,
			iconCls: "x-tbar-page-next",
			disabled: true,
			handler: this.onClick.createDelegate(this, ["next"])
		});
		this.last = this.addButton({
			tooltip: this.lastText,
			iconCls: "x-tbar-page-last",
			disabled: true,
			handler: this.onClick.createDelegate(this, ["last"])
		});
		this.addSeparator();
		this.loading = this.addButton({
			tooltip: this.refreshText,
			iconCls: "x-tbar-loading",
			handler: this.onClick.createDelegate(this, ["refresh"])
		});
		
		
		/*
			Begin Extension to onRender
		*/
		if(this.displayInfo){
			this.addSpacer();
			this.displayEl = Ext.fly(this.el.dom).createChild({cls:'x-paging-info', style:'top:2px'});	

			//this.add('->');
			this.displayItem = new Ext.ProgressBar({
				renderTo : this.displayEl,
				text     : this.defaultText,
				width    : this.progBarWidth || 225,
				animate  :  this.defaultAnimCfg
			})

			this.on('destroy', this.displayItem.destroy, this.displayItem);
		}
		/*
			End extension
		*/
		if(this.dsLoaded){
			this.onLoad.apply(this, this.dsLoaded);
		}
	},
	updateInfo : function(){
		if(this.displayItem){
			var count   = this.store.getCount();
			var pgData  = this.getPageData();
			var pageNum = this.readPage(pgData);
			
			var msg    = count == 0 ?
				this.emptyMsg :
				String.format(
					this.displayMsg,
					this.cursor+1, this.cursor+count, this.store.getTotalCount()
				);
				
			pageNum = pgData.activePage; ;	
			
			var pct	= pageNum / pgData.pages;	
			
			this.displayItem.updateProgress(pct, msg);
		}
	}	

});