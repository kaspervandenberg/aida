<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:aid="http://www.mgj.org/aid">

<xsl:template match="aid:result">
    <!-- start webpagina-->
    <html>
        <head>
            <title>AID: Synonym results</title>
            <link rel="stylesheet" type="text/css" href="qckcss.css"/>
        </head>
        <body>
        <h2>Stored synonyms</h2>
        <h3>Index: <xsl:value-of select="@index"/></h3>
        <h3>Date generated: <xsl:value-of select="@date"/></h3>
    <table border="0">
    <tr bgcolor="white">
      <th align="left">Term</th>
      <th align="left">Synonym</th>
    </tr>
<xsl:for-each select="aid:synonym">
<xsl:sort select="."/>
   <tr>
      <td><b><xsl:value-of select="."/></b></td>
      <td><xsl:value-of select="./@term"/></td>
   </tr>
</xsl:for-each>
    </table>                 
</body>
    </html>
</xsl:template>

</xsl:stylesheet> 
