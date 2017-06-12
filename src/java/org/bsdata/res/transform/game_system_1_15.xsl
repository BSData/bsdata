<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bsg="http://www.battlescribe.net/schema/gameSystemSchema"
                xmlns="http://www.battlescribe.net/schema/gameSystemSchema"
                exclude-result-prefixes="bsg">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/bsg:gameSystem/@battleScribeVersion">
        <xsl:attribute name="battleScribeVersion">1.15</xsl:attribute>
    </xsl:template>
    
</xsl:stylesheet>
