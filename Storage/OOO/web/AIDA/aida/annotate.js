/*
 * Copyright (C) 2006 Willem Robert van Hage wrvhage@few.vu.nl
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

var ELEMENT_NODE = 1;
var TEXT_NODE = 3;
var CDATA_SECTION_NODE = 4;
var DOCUMENT_NODE = 9;

var mainloc = top.opener;

function createNiceUri(uri,type) {
    if (uri.match(/(http|https|file|srb):\/\//)) return uri;
    else if (type == "property") {
        return property_default_ns + escape(uri);
    } else if (type == "concept") {
        return concept_default_ns + escape(uri);
    }
    return uri;
}

function storeAnnotation() {
    var selection = createSelectionAnnotation();
    var property;
    if (document.annotate.property_hidden) property = document.annotate.property_hidden.value;
    if (property == null || property == "") property = document.annotate.property.value;
    property = createNiceUri(property,"property");
    var concept;
    if (document.annotate.concept_hidden) concept = document.annotate.concept_hidden.value;
    if (concept == null || concept == "") concept = document.annotate.concept.value;
    concept = createNiceUri(concept,"concept");
    storeStatement(selection,property,concept);
}

function getUrl() {
    try {
        netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
    } catch (err) {
        alert(err);
        return null;
    }
    return "" + mainloc.location;
}

function loadUrl(url) {
    try {
        netscape.security.PrivilegeManager.enablePrivilege('UniversalFileRead');
    } catch (err) {
        alert(err);
        return;
    }
    if (!url.match(/^(http|https|file|srb):\/\//)) {
        mainloc.location.href = "file://" + url;
    } else {
        mainloc.location.href = "" + url;
    }
}

function getRoot(node) {
    if (node.nodeType == DOCUMENT_NODE) return node;
    if (node.parentNode == null) return node;
    return getRoot(node.parentNode);
}

function getBody(node) {
    return getRoot(node).getElementsByTagName("body").item(0);
}

function createSelectionAnnotation() {
    var range;
    try {
        netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserRead');
    } catch (err) {
        alert(err);
        return null;
    }
    var frame = mainloc;
    var selection = frame.getSelection();
    if (selection) {
        if (selection.rangeCount == null || selection.rangeCount == 0) {
            return getUrl();
        }
        range = selection.getRangeAt(0);
        var startNode = range.startContainer;
        var endNode = range.endContainer;
        var reference = getBody(startNode);
        var startOffset = getContentOffset(reference,startNode,range.startOffset,null);
        var endOffset = getContentOffset(reference,endNode,range.endOffset,null);
        var url = "" + frame.location;
        var refpath;
        // special case for HTML to circumvent xpointer substring whitespace issues with outside the body element
        if (url.match(/html?$/)) {
            refpath = "//body";
        } else {
            refpath = "/";
        }
        if (endOffset - startOffset == 0) {
            // assume that the whole document is selected
            return url;
        }
        var xpath = "#xpointer(" + refpath + "," + (startOffset) + "," + (endOffset - startOffset) + ")";
        return url + xpath;
    }
}


/*
 * The following code was taken from the Web Annotation project
 * by Geoffrey Glass
 *
 * Copyright (C) 2005 Geoffrey Glass www.geof.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */


/*
 * Used for converting a (container,offset) pair as used by the W3C Range object
 * to a character offset relative to a specific element.
 */
function getContentOffset( rel, container, offset, fskip ) {
    var sofar = 0;
    
    // Start with rel and walk forward until we hit the range reference
    var node = rel;
    while ( node != container && node != null) {
        if ( TEXT_NODE == node.nodeType || CDATA_SECTION_NODE == node.nodeType )
            sofar += node.length;
        node = walkNextNode( node, fskip );
    }
    if ( null == node )
        return 0;
    
    // First case:  a character offset in a text node (most common case for selection ranges)
    if ( TEXT_NODE == node.nodeType || CDATA_SECTION_NODE == node.nodeType ) {
        //trace( 'getContentOffset ' + container + ',' + offset + ' -> ' + (sofar+offset) );
        return sofar + offset;
    }
    // Second case:  a child element offset within a non-text node
    else {
        // Walk forward through child nodes until we hit the specified offset
        node = node.firstChild;
        for ( var i = 0;  i < offset;  ++i ) {
            if ( null == node )
                debug( 'Error in getContentOffset:  invalid element offset' );
            sofar += nodeTextLength( node );
            node = node.nextSibling;
        }
        return sofar;
    }
}

function walkNextNode( node, fskip ) {
    var next = node;
    do {
        if ( ELEMENT_NODE == next.nodeType && next.firstChild && ( null == fskip || ! fskip( next ) ) ) {
            next = next.firstChild;
        }
        else if ( next.nextSibling ) {
            next = next.nextSibling;
        }
        else {
            next = next.parentNode;
            while ( null != next && null == next.nextSibling )
                next = next.parentNode;
            if ( null != next )
                next = next.nextSibling;
        }
    }
    while ( null != next && fskip && fskip( next ) );
    return next;
}

/*
 * Calculate the number of characters of text in a node
 * Does this work correctly with variable-length unicode characters?
 * Any elements with a class of skipClass are ignored
 */
function nodeTextLength( node, skipClass ) {
    // Base case
    if ( TEXT_NODE == node.nodeType || CDATA_SECTION_NODE == node.nodeType )
        return node.length;
    // Recurse
    else if ( ELEMENT_NODE == node.nodeType && ( null == skipClass || ! hasClass( node, skipClass ) ) ) {
        var n = 0;
        for ( var i = 0;  i < node.childNodes.length;  ++i )
            n += nodeTextLength( node.childNodes[ i ] );
        return n;
    }
    else
        return 0;
}


