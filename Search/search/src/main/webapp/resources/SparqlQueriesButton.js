// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An extended Ext.Button which contain template queries
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      1 May 2009
* @version   $Id$
*
* @license 
*/

var Queries = function() {
  
  return {
    
    queries : [
      new Ext.menu.Item({
        text : 'Query GO repository (definitions)',
        icon : 'icons/database_refresh.png',
        handler : function(item) {
          
          var node = item.node;
          var testwin = new Ext.app.RepositorySearch({
            repository : loadState('repository', 'GO'),
            query :
              "prefix go: <http://purl.org/obo/owl/GO#>\n" +
              "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
              "prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
              "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
              "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
              "select  distinct ?name  ?class ?deflabel\n" +
              "where\n" +
              "{\n" +
              "\t?class rdf:type owl:Class.\n" +
              "\t?class rdfs:label ?name.\n" +
              "\toptional {\n" +
              "\t\t?class obo:hasDefinition ?def.\n" +
              "\t\t?def rdfs:label ?deflabel \n" +
              "\t}\n" +
              "\tfilter ((?class = <"+node.id+">))\n" +
              "}\n"
            // not yet used:
            ,conceptid : node.id 
          });
          
          testwin.show();
        }
      }),
      
      new Ext.menu.Item({
        text : 'Query GO repository (superClass)',
        icon : 'icons/database_refresh.png',
        handler : function(item) {
          
          var node = item.node;
          var testwin = new Ext.app.RepositorySearch({
            repository : loadState('repository', 'GO'),
            query :
              "prefix go: <http://purl.org/obo/owl/GO#>\n" +
              "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
              "prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
              "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
              "select distinct ?name  ?class ?definition\n" +
              "where\n" +
              "{   <"+node.id+"> rdfs:subClassOf ?class.\n" +
              "    ?class rdfs:label ?name.\n" +
              "    ?class obo:hasDefinition ?def.\n" +
              "    ?def rdfs:label ?definition \n" +
              "}\n"
            // not yet used:
            ,conceptid : node.id 
          });
          
          testwin.show();
        }
      })
    ],
    
    getButton : function() {
      return new Ext.Button({
        text : 'Repository Queries',
        icon : 'icons/database_refresh.png',
        menu : {
          items : this.queries
        }
        
      });
    },
    getMenu : function() {
      return new Ext.menu.Item({
        text : 'Repository Queries',
        icon : 'icons/database_refresh.png',
        menu : this.queries
      })
    }
  };
}();

// application main entry point
Ext.app.SparqlQueriesButton = Ext.extend(Ext.menu.Item, {
  
  queries : [
    new Ext.menu.Item({
      text : 'Query GO repository (definitions)',
      icon : 'icons/database_refresh.png',
      handler : function(item) {
        
        var node = item.node;
        var testwin = new Ext.app.RepositorySearch({
          repository : loadState('repository', 'GO'),
          query :
            "prefix go: <http://purl.org/obo/owl/GO#>\n" +
            "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
            "prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
            "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "select  distinct ?name  ?class ?deflabel\n" +
            "where\n" +
            "{\n" +
            "\t?class rdf:type owl:Class.\n" +
            "\t?class rdfs:label ?name.\n" +
            "\toptional {\n" +
            "\t\t?class obo:hasDefinition ?def.\n" +
            "\t\t?def rdfs:label ?deflabel \n" +
            "\t}\n" +
            "\tfilter ((?class = <"+node.id+">))\n" +
            "}\n"
          // not yet used:
          ,conceptid : node.id 
        });
        
        testwin.show();
      }
    }),
    
    new Ext.menu.Item({
      text : 'Query GO repository (superClass)',
      icon : 'icons/database_refresh.png',
      handler : function(item) {
        
        var node = item.node;
        var testwin = new Ext.app.RepositorySearch({
          repository : loadState('repository', 'GO'),
          query :
            "prefix go: <http://purl.org/obo/owl/GO#>\n" +
            "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
            "prefix obo: <http://www.geneontology.org/formats/oboInOwl#>\n" +
            "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "select distinct ?name  ?class ?definition\n" +
            "where\n" +
            "{   <"+node.id+"> rdfs:subClassOf ?class.\n" +
            "    ?class rdfs:label ?name.\n" +
            "    ?class obo:hasDefinition ?def.\n" +
            "    ?def rdfs:label ?definition \n" +
            "}\n"
          // not yet used:
          ,conceptid : node.id 
        });
        
        testwin.show();
      }
    })
  ],
  
  initComponent : function() {
    
    this.id = this.id ? this.id : Ext.id();
    
    Ext.apply( this, {
      text : 'Repository Queries',
      icon : 'icons/database_refresh.png',
      menu : this.queries
    }); // end Ext.apply
    
    Ext.app.SparqlQueriesButton.superclass.initComponent.call(this, arguments);

    
  } // end initComponent
  
  
 
}); 

Ext.reg('sparqlbutton', Ext.app.SparqlQueriesButton);