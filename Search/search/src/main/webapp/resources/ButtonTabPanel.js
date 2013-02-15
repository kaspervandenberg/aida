// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An Application
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      20 March 2009
* @version   $Id$
* 
* @license  
*/

Ext.ns('Ext.app');

Ext.app.ButtonTabPanel = Ext.extend(Ext.TabPanel, {

  //initComponent : function() {
  //  Ext.app.ButtonTabPanel.superclass.initComponent.call(this, arguments);
  //}

    onRender : function(ct, position) {
        Ext.app.ButtonTabPanel.superclass.onRender.apply(this, arguments);
        var butEl = this.edge.insertSibling({
            tag: "li"
        });
        new Ext.Button(Ext.apply({
            renderTo: butEl,
            iconCls : 'btn-browse',
            text : 'Refresh'
        }, this.button));
    },

    findTargets : function(e) {
        var item = null;
        var itemEl = e.getTarget("li", this.strip);
        if (itemEl) {
            item = this.getComponent(itemEl.id.split(this.idDelimiter)[1]);
            if (item && item.disabled) {
                return {
                    close : null,
                    item : null,
                    el : null
                };
            }
        }
        return {
            close : e.getTarget(".x-tab-strip-close", this.strip),
            item : item,
            el : itemEl
        }
    }
});

Ext.reg("buttontabpanel", Ext.app.ButtonTabPanel); 



