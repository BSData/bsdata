<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bsc="http://www.battlescribe.net/schema/catalogueSchema"
                xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                exclude-result-prefixes="bsc">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/bsc:catalogue/@battleScribeVersion">
        <xsl:attribute name="battleScribeVersion">1.15</xsl:attribute>
    </xsl:template>
    
</xsl:stylesheet>
