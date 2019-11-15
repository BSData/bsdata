<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bsc="http://www.battlescribe.net/schema/catalogueSchema"
                xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                exclude-result-prefixes="bsc">

    <xsl:output method="xml" indent="yes"/>
    
    
    <!-- Catalogue -->
    <xsl:template match="/bsc:catalogue">
        <catalogue>
            <!-- Attributes -->
            <xsl:attribute name="battleScribeVersion">2.03</xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'battleScribeVersion']"/>
            
            
            <!-- Nodes -->
            <xsl:apply-templates select="node()"/>
        </catalogue>
    </xsl:template>
        
    
    <!-- Copy -->
    <xsl:template match="* | bsc:*">
        <xsl:element name="{local-name(.)}">
            <xsl:apply-templates select="node() | @*"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="@*">
        <xsl:copy/>
    </xsl:template>
    
</xsl:stylesheet>
