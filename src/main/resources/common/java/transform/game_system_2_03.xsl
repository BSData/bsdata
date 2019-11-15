<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bsg="http://www.battlescribe.net/schema/gameSystemSchema"
                xmlns="http://www.battlescribe.net/schema/gameSystemSchema"
                exclude-result-prefixes="bsg">

    <xsl:output method="xml" indent="yes"/>
    
    
    <!-- GameSystem -->
    <xsl:template match="/bsg:gameSystem">
        <gameSystem>
            <!-- Attributes -->
            <xsl:attribute name="battleScribeVersion">2.03</xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'battleScribeVersion']"/>
            
            
            <!-- Nodes -->
            <xsl:apply-templates select="node()"/>
        </gameSystem>
    </xsl:template>
        
    
    <!-- Copy -->
    <xsl:template match="* | bsg:*">
        <xsl:element name="{local-name(.)}">
            <xsl:apply-templates select="node() | @*"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="@*">
        <xsl:copy/>
    </xsl:template>
    
</xsl:stylesheet>
