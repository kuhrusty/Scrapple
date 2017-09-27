<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:template match="/">
  <html>
  <body>
    <xsl:apply-templates select="entry_list/entry"/>
  </body>
  </html>
</xsl:template>


<xsl:template match="entry_list/entry">
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
  </div>

  <dl>
    <xsl:apply-templates select="def/sn | def/dt"/>
  </dl>
</xsl:template>


<xsl:template match="def/sn">
  <dt><xsl:value-of select="."/></dt>
</xsl:template>


<xsl:template match="def/dt">
  <!--  how to replace <it> with <em> ? -->
<!--  <xsl:apply-templates select="@*|node()"/> -->
  <dd><xsl:value-of select="."/></dd>
</xsl:template>

<!--
<xsl:template match="def/it">
  <em><xsl:apply-templates select="@*|node()" /></em>
</xsl:template>
-->
<!--
<xsl:template match="it">
  <em><xsl:apply-templates select="@*|node()"/></em>
</xsl:template>
-->


</xsl:stylesheet>