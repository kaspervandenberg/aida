/**********
*
* http://TDG-i.com TDG innovations, LLC
* 03/15/2008, Jay Garcia, jgarcia@tdg-i.com
* borderLayout extension example displaying 'collapsed titles' 
* Blog Link: http://tdg-i.com/30/how-to-get-titles-in-collapsed-panels-for-border-layout
* Purpose : Adds ability to insert title in collapsed layout panels.  
* Note: You must use layout 'tdgi_border' to use this extension.
* Works with EXT 2.0+
*
* Parameters: collapsedTitle, object || string || boolean
* 
* Example Object: 
	collapsedTitle: {
		//Just like an Ext.DomHelper config object
		element : {
			// Required0
			tag  : 'div',
			// Required
			html : 'NorthPanel Collapsed',
			// Set this if you need to.
			style  : "margin-left:3px; color: #FF0000; font-weight: bold;"
		}
	}

*
* Example String: (Html fragment)
	collapsedTitle: '<div style="myStyle">My Collapsed Title</div>'
*
* Example Boolean: (This allows you to use the original title)
	collapsedTitle: true

*
* License : Free to use, just please don't sell without permission.
* This was made for myself, my customers, and the EXT Community.  
* 
* Waranty : None. I can answer questions via email: jgarcia@tdg-i.com
*
**********/

Ext.namespace('Ext.ux', 'Ext.ux.TDGi');

Ext.ux.TDGi.BorderLayout = function(config) {

	Ext.ux.TDGi.BorderLayout.superclass.constructor.call(this, config, this);
	
	
};

Ext.extend(Ext.ux.TDGi.BorderLayout, Ext.layout.BorderLayout, {
	northTitleAdded  : false,
	southTitleAdded  : false,
	eastTitleAdded  : false,
	westTitleAdded  : false,

	doCollapsedTitle : function (ct) {
		function doHtmlInsert(ct, element) {
			if (ct.region == 'east' || ct.region =='west') {
				if (Ext.isIE6 || Ext.isIE7) {
					if (ct.region == 'east') {
						Ext.get(ct.collapsedEl.dom.firstChild).applyStyles({margin:'3px 3px 5px 3px'});
					}
					else {
						Ext.get(ct.collapsedEl.dom.firstChild).applyStyles({margin:'3px auto 5px 3px'});
					}
				}
				return(Ext.DomHelper.append(ct.collapsedEl, element));
			}
			else {
				return(Ext.DomHelper.insertFirst(ct.collapsedEl, element));
			}			
			
		}
		
		if (ct.collapsedTitle) {
			if (typeof ct.collapsedTitle == 'object') {
				if (typeof ct.collapsedTitle.element == 'object') {
					var element = ct.collapsedTitle.element;
					if (element.style) {
						element.style += 'float: left;';	
					}
					else {
						element.style = 'float: left;';	
					}
					doHtmlInsert(ct, element);
					return(true);
				}
			}
			else if (typeof ct.collapsedTitle == 'string') {
				var element = ct.collapsedTitle;
				doHtmlInsert(ct, element);
				return(true)	
			}
			else if (typeof ct.collapsedTitle == 'boolean' && ct.collapsedTitle == true) {
				if (ct.region == 'east' || ct.region =='west') {
					if (Ext.isIE6 || Ext.isIE7) {
						var element = {
							tag : 'div',
							style : "writing-mode: tb-rl; ",
							html : ct.title 
						}
					}
					else {
						var y = 0, txt = '';
						for (i = 0; y < ct.title.length ; i ++ ) {
							txt += ct.title.substr(y, 1) + '<br />';
							y++;	
						}
						var element = {
							tag : 'div',
							style : "text-align: center;",
							html : txt 
						}
					}
				}
				else {
					var element = {
						tag : 'div',
						html : ct.title ,
						style : 'float: left;'
					}
				
				}
				doHtmlInsert(ct, element);
				return(true);				
			}
			
		}

	},
	// private
	onLayout : function(ct, target){

		var collapsed;
		if(!this.rendered){
			
			target.position();
			target.addClass('x-border-layout-ct');
			var items = ct.items.items;
			collapsed = [];
			for(var i = 0, len = items.length; i < len; i++) {
				var c = items[i];
				var pos = c.region;
				if(c.collapsed){
					collapsed.push(c);
				}
				c.collapsed = false;
				if(!c.rendered){
					c.cls = c.cls ? c.cls +' x-border-panel' : 'x-border-panel';
					c.render(target, i);
				}
				this[pos] = pos != 'center' && c.split ?
					new Ext.layout.BorderLayout.SplitRegion(this, c.initialConfig, pos) :
					new Ext.layout.BorderLayout.Region(this, c.initialConfig, pos);
				this[pos].render(target, c);
			}
			this.rendered = true;
		}

		var size = target.getViewSize();
		if(size.width < 20 || size.height < 20){ // display none?
			if(collapsed){
				this.restoreCollapsed = collapsed;
			}
			return;
		}else if(this.restoreCollapsed){
			collapsed = this.restoreCollapsed;
			delete this.restoreCollapsed;
		}

		var w = size.width, h = size.height;
		var centerW = w, centerH = h, centerY = 0, centerX = 0;

		var n = this.north, s = this.south, west = this.west, e = this.east, c = this.center;
		if(!c){
			throw 'No center region defined in BorderLayout ' + ct.id;
		}

		if(n && n.isVisible()){
			var b = n.getSize();
			var m = n.getMargins();
			b.width = w - (m.left+m.right);
			b.x = m.left;
			b.y = m.top;
			centerY = b.height + b.y + m.bottom;
			centerH -= centerY;
			n.applyLayout(b);
					
			/**********
			*
			* http://TDG-i.com TDG innovations, LLC
			* 03/15/2008, Jay Garcia, jgarcia@tdg-i.com
			* borderLayout extension example displaying 'collapsed titles' 
			* Blog Link: http://tdg-i.com/30/how-to-get-titles-in-collapsed-panels-for-border-layout
			* Purpose : Adds ability to insert title in collapsed layout panels.
			*
			************/
			if (typeof n.collapsedEl != 'undefined' && n.collapsedTitle && this.northTitleAdded == false) {
				if (this.doCollapsedTitle(n)) {
					this.northTitleAdded = true;
				}
			}                    
		}
		if(s && s.isVisible()){
			var b = s.getSize();
			var m = s.getMargins();
			b.width = w - (m.left+m.right);
			b.x = m.left;
			var totalHeight = (b.height + m.top + m.bottom);
			b.y = h - totalHeight + m.top;
			centerH -= totalHeight;
			s.applyLayout(b);

			/**********
			*
			* http://TDG-i.com TDG innovations, LLC
			* 03/15/2008, Jay Garcia, jgarcia@tdg-i.com
			* borderLayout extension example displaying 'collapsed titles' 
			* Blog Link: http://tdg-i.com/30/how-to-get-titles-in-collapsed-panels-for-border-layout
			* Purpose : Adds ability to insert title in collapsed layout panels.
			*
			************/

			if (typeof s.collapsedEl != 'undefined' && s.collapsedTitle && this.southTitleAdded == false) {
				if (this.doCollapsedTitle(s)) {
					this.southTitleAdded = true;
				}
			}                
		}
		if(west && west.isVisible()){
			var b = west.getSize();
			var m = west.getMargins();
			b.height = centerH - (m.top+m.bottom);
			b.x = m.left;
			b.y = centerY + m.top;
			var totalWidth = (b.width + m.left + m.right);
			centerX += totalWidth;
			centerW -= totalWidth;
			west.applyLayout(b);

			/**********
			*
			* http://TDG-i.com TDG innovations, LLC
			* 03/15/2008, Jay Garcia, jgarcia@tdg-i.com
			* borderLayout extension example displaying 'collapsed titles' 
			* Blog Link: http://tdg-i.com/30/how-to-get-titles-in-collapsed-panels-for-border-layout
			* Purpose : Adds ability to insert title in collapsed layout panels.
			*
			************/
			if (typeof west.collapsedEl != 'undefined' && west.collapsedTitle && this.westTitleAdded == false) {
				if (this.doCollapsedTitle(west)) {
					this.westTitleAdded = true;
				}
			
			}                
			
		}
		if(e && e.isVisible()){
			var b = e.getSize();
			var m = e.getMargins();
			b.height = centerH - (m.top+m.bottom);
			var totalWidth = (b.width + m.left + m.right);
			b.x = w - totalWidth + m.left;
			b.y = centerY + m.top;
			centerW -= totalWidth;
			e.applyLayout(b);
	
			/**********
			*
			* http://TDG-i.com TDG innovations, LLC
			* 03/15/2008, Jay Garcia, jgarcia@tdg-i.com
			* borderLayout extension example displaying 'collapsed titles' 
			* Blog Link: http://tdg-i.com/30/how-to-get-titles-in-collapsed-panels-for-border-layout
			* Purpose : Adds ability to insert title in collapsed layout panels.
			*
			************/
			if (typeof e.collapsedEl != 'undefined' && e.collapsedTitle && this.eastTitleAdded == false) {
				if (this.doCollapsedTitle(e)) {
					this.eastTitleAdded = true;
				}
			}                
			
		}

		var m = c.getMargins();
		var centerBox = {
			x: centerX + m.left,
			y: centerY + m.top,
			width: centerW - (m.left+m.right),
			height: centerH - (m.top+m.bottom)
		};
		c.applyLayout(centerBox);

		if(collapsed){
			for(var i = 0, len = collapsed.length; i < len; i++){
				collapsed[i].collapse(false);
			}
		}

		if(Ext.isIE && Ext.isStrict){ // workaround IE strict repainting issue
			target.repaint();
		}
	}
});

Ext.Container.LAYOUTS['tdgi_border'] = Ext.ux.TDGi.BorderLayout;