<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- een stylesheet om de xmlsource van de query en resulthandlers om te toveren in een bruikbare interface -->

<xsl:template match="/">
<!-- start webpagina-->
<html>
<head>
<title>Query Construction Kit</title>
<link rel="stylesheet" type="text/css" href="qckcss.css"/>
<script language='JavaScript'>
function toggleLayer(whichLayer)
{
if (document.getElementById)
{
// this is the way the standards work
var style2 = document.getElementById(whichLayer).style;
style2.display = style2.display? "":"block";
}
else if (document.all)
{
// this is the way old msie versions work
var style2 = document.all[whichLayer].style;
style2.display = style2.display? "":"block";
}
else if (document.layers)
{
// this is the way nn4 works
var style2 = document.layers[whichLayer].style;
style2.display = style2.display? "":"block";
}
}

function _body_onload()
{
	loff();
}

function _body_onunload()
{
	lon();
}
</script>
<script type="text/javascript" src="lon.js">
<!-- IE vindt het prettig hier wat te hebben staan -->
</script>
</head>
<body onLoad="_body_onload();" onUnload="_body_onunload();">

<!-- layer die wordt weergegeven tijdens het laden van de results -->

<table style="display: none;" id="loaderContainer" onclick="return false;" border="0" cellpadding="0" cellspacing="0" width='100%' height='100%'>
  <tbody>
    <tr>
      <td id="loaderContainerH" height="455"><div id="loader">
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody>
              <tr>
                <td><p><img src="loading.gif" alt="Larry the B.S.E. Cow"/><strong>Please wait... Searching ...</strong></p></td>
              </tr>
            </tbody>
          </table>
        </div></td>
    </tr>
  </tbody>
</table>

<!-- start van de normale output -->

 <div id="header">
  <img src="logo.gif" alt="Logo" class="logo"/><span class="strapline">Query Construction Kit</span>
 </div>
 <div align="center" id="searcharea">
  <form name="queryform" method="post" action="{//form/action}">
   <table border='0' id='searchtext'>
    <tr>
     <td>Enter your query:<br/></td><td>Max. results:</td>
    </tr>
    <tr>
     <xsl:for-each select="//fields/field">
     <td>
       <xsl:choose>
         <xsl:when test='name="nrResults"'>
           <select name='{name}'>
             <option value='10'>10</option>
             <option value='50'>50</option>
             <option value='100'>100</option>
             <option value='500'>500</option>
             <option value='1000'>1000</option>
           </select>
         </xsl:when>
         <xsl:otherwise>
           <input type="text" name="{name}" size="50" value="{value}"/>
         </xsl:otherwise>
       </xsl:choose>
     </td>
    </xsl:for-each>
    </tr>
    <tr>
     <td align='center'>
       <xsl:attribute name='colspan'>
         <xsl:value-of select="count(//field)"/>
       </xsl:attribute>
    <xsl:for-each select="//buttons/button">
     <input id="button" type="submit" name="{name}" size="50" value="{value}" onclick="lon();go_to({//form/action}); return false;"/>
    </xsl:for-each>
     </td>
    </tr>
    </table>
    <div id='advancedsearch' style='cursor: pointer;'>
      <a onClick="toggleLayer('advancedsearchoptions');">Click for advanced search options:</a>
    </div>
    <div id='advancedsearchoptions'>
    <hr/>
     <!-- switch naar template voor de searchmethods -->
     <xsl:apply-templates select="//searchmethods"/>
    </div>
    <hr/>
     <!-- switch naar template voor de spellcheck -->
     <xsl:apply-templates select="//searchterms"/>
     
     <!-- switch naar de template voor de categorieen -->
  <!-- <xsl:apply-templates select="//inputcategories"/> -->
   </form>
  </div>
    <!-- legenda voor de kleurtjes -->
    <xsl:if test='//results/doc/title!=""'>
      <div id='legend'><img src='red.jpg' alt='most relevant'/> = most relevant | <img src='orange.jpg' alt='relevant'/> = relevant | <img src='blue.jpg' alt='less relevant'/> = less relevant | <img src='green.jpg' alt='least relevant'/> = least relevant</div>
    </xsl:if>
    <!-- laat de gebruikte query zien -->
    <xsl:if test="//results/query!=''">
      <div id='fullquery'><p>B.S.E. constructed the following query:</p><xsl:value-of select="//results/query"/></div>
    </xsl:if>
  <xsl:apply-templates select="//results/doc"/>
  <!-- test of de zoekmachine al een aantal seconden heeft teruggegeven -->
  <xsl:if test='page/duration!=""'>
    <div align="center">Found <xsl:value-of select="page/hits"/> total results in <xsl:value-of select="page/duration"/> seconds.</div>
  </xsl:if>
 </body>
</html>
</xsl:template>


<!-- template voor de searchmethods -->
<xsl:template match="//searchmethods">
       <div id='searchmethodtext'>Optionally, uncheck certain search methods to change your result set:</div>
     <!-- for-each loop voor checkboxjes van de zoekmethodes -->
     <table id='searchmethodgroups'>
     <xsl:for-each select="//searchmethods/searchmethodgroup">
      <tr>
      <td>
       <input type="checkbox" value="{value}" name="{name}">
        <xsl:if test='state="true"'>
         <xsl:attribute name="checked">checked</xsl:attribute>
        </xsl:if>
       </input> <div id='searchmethoddescription'><xsl:value-of select="description"/>:</div>
       </td>
       <td>
        <xsl:for-each select="searchmethodoption/option">
          <input type="radio" name="{name}" value="{value}">
           <xsl:if test="state='true'">
            <xsl:attribute name='checked'>checked</xsl:attribute>
           </xsl:if>
           <xsl:if test="enabled='false'">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
           </xsl:if>
          </input>
          <xsl:choose>
           <xsl:when test="enabled='false'">
           <font color='grey'><xsl:value-of select="description"/></font>
           </xsl:when>
           <xsl:otherwise>
            <xsl:value-of select="description"/>
           </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
       </td>
       </tr>
      </xsl:for-each>
     </table>
     
     <!-- knoppen voor files -->
     Download files: 
     <xsl:for-each select="//files/file">
       <xsl:if test='enabled="true"'>
         <input id='button' type='submit' name='{value}' value='{name}'/>
       </xsl:if>
     </xsl:for-each>
     
</xsl:template>

<!-- template voor spellcheck -->
	<!--GEWIJZIGD Rene-->
<xsl:template match="//searchterms">
  <xsl:if test="searchterm/spelling/term/value!=''">
   <xsl:for-each select="searchterm">
   <xsl:if test="spelling/term/name!=''">
	<div id="spellcorrect">
   You typed: <div id='spellcorrectterms'><xsl:value-of select="value"/></div>. Did you mean:
    <xsl:for-each select="spelling/term">
     <input type="radio" name="{name}" value="{value}">
		<xsl:if test="position()=last()">
			<xsl:attribute name="checked">checked</xsl:attribute>
		</xsl:if>
	 </input>	 	 
		<div id='spellcorrectterms'><xsl:value-of select="value"/></div>
    </xsl:for-each>
   </div>
	   </xsl:if>
   </xsl:for-each>
	   
     <div id='spellcheckbutton'><input id="button" type='submit' name='spellchecksubmit' value='Change Query'/></div>
   <hr/>
  </xsl:if>
</xsl:template>

<!-- template voor de categorieen
<xsl:template match="//inputcategories">
 check of er categorieen zijn gedefinieerd 
<xsl:if test="//inputcategories">
<table border="1">
  <tbody>
    <tr>
     loop door de categorieen en geef ze weer indien ze bestaan in het xml bestand
    <xsl:for-each select="//inputcategory">
     <td>
      <input type="checkbox" value="1" name="{cui}">
       <xsl:if test='selected="TRUE"'>
        <xsl:attribute name="checked">checked</xsl:attribute>
       </xsl:if>
      </input> <xsl:value-of select="value"/> (<xsl:value-of select="score"/>)
     </td>
    </xsl:for-each>
    </tr>
  </tbody>
</table>
</xsl:if>
</xsl:template>

-->

<!-- template voor de results -->
<xsl:template match="//results/doc">
<!-- check of er results in de xml staan -->
<xsl:if test='title!=""'>
<p id="results">
 <!-- standaard output -->
 <span>
   <a href="{link}" target="_blank">
   <!-- bepaal kleur van de titel -->
   <xsl:if test='score&gt;=75'>
     <xsl:attribute name='class'>titlered</xsl:attribute>
   </xsl:if>
   <xsl:if test='(score&lt;75) and (score&gt;=50)'>
     <xsl:attribute name='class'>titleorange</xsl:attribute>
   </xsl:if>
   <xsl:if test='(score&lt;50) and (score&gt;25)'>
     <xsl:attribute name='class'>titleblue</xsl:attribute>
   </xsl:if>
   <xsl:if test='score&lt;25'>
     <xsl:attribute name='class'>titlegreen</xsl:attribute>
   </xsl:if>
   <xsl:value-of select="title"/></a></span>
 <span class="abstract"><a class='abstracthover'><i>Abstract: </i> <xsl:value-of select="abstract"/></a></span>
 <span class="score"><i>Score: </i> <xsl:value-of select="score"/> </span>
 <span class="type"><i>Type: </i> <xsl:value-of select="type"/> </span>
 <span class="date"><i>Date: </i> <xsl:value-of select="date"/> </span>
 <!-- check of er het result in een categorie valt te plaatsen -->
 <xsl:if test="resultgroups/resultmaincategory">
   <span class="foundin"><i>Found in: </i>
   <xsl:for-each select="resultgroups/resultkeyword">
    <span class='keywords'><xsl:value-of select="value"/></span>
   </xsl:for-each> <i>Main category: </i> <xsl:value-of select='resultgroups/resultmaincategory/value'/>
   </span>
 </xsl:if>
 
 <!-- output per categorie
 <xsl:for-each select="resultgroups/resultmaincategory">
  <div id="{value}" style="display:none;">
   <span class="title"><xsl:value-of select="../../title"/></span>
   <span class="abstract"><i>Abstract: </i> <xsl:value-of select="../../abstract"/></span>
   <span class="score"><i>Score: </i> <xsl:value-of select="../../score"/> </span>
   <span class="type"><i>Type: </i> <xsl:value-of select="../../type"/> </span>
   <span class="date"><i>Date: </i> <xsl:value-of select="../../date"/> </span>
   <xsl:if test="resultgroups/resultmaincategory">
     <span class="foundin"><i>Found in: </i>
     <xsl:for-each select="../../resultgroups/resultkeyword">
      <span class='keywords'><xsl:value-of select="value"/></span>
     </xsl:for-each> <i>Main category: </i> <xsl:value-of select='../../resultgroups/resultmaincategory/value'/>
     </span>
   </xsl:if>
  </div>
 </xsl:for-each>
 -->
</p>
</xsl:if>
</xsl:template>


</xsl:stylesheet>
