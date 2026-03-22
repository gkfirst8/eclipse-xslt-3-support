<xsl:stylesheet version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="/">
        <out>
            <xsl:value-of select="'hello'"/>
        </out>
    </xsl:template>

    <xsl:function name="demo:greeting" as="xs:string"
        xmlns:demo="urn:demo"
        xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <xsl:param name="name" as="xs:string"/>
        <xsl:sequence select="'Hello ' || $name"/>
    </xsl:function>
</xsl:stylesheet>
