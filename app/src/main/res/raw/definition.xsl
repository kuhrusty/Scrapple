<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
  <body>
  <xsl:for-each select="entry_list/entry">
    <div class="word-and-pronunciation">
<!--      <span class="def-num">1</span> -->
      <h1><xsl:value-of select="ew"/></h1>
<!-- we don't care about subj -->
<!-- or art -->
<!-- or sound -->
      <div class="word-attributes">
        <span class="main-attr"><em><xsl:value-of select="fl"/></em></span>
        <xsl:text> </xsl:text>
        <span class="word-syllables"><xsl:value-of select="hw"/></span>
        <xsl:text> </xsl:text>
        <span class="pr"><xsl:value-of select="pr"/></span>
      </div>

<!--      <xsl:value-of select="in"/> -->

      <xsl:for-each select="dt">
        <xsl:for-each select="/">
<!--
          <xsl:if test="">

          </xsl:if>
-->
        </xsl:for-each>
      </xsl:for-each>

    </div>
  </xsl:for-each>


<!--
  <h2>My CD Collection</h2>
    <table border="1">
      <tr bgcolor="#9acd32">
        <th>Title</th>
        <th>Artist</th>
      </tr>
      <xsl:for-each select="catalog/cd">
      <tr>
        <td><xsl:value-of select="title"/></td>
        <td><xsl:value-of select="artist"/></td>
      </tr>
      </xsl:for-each>
    </table>
-->
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>