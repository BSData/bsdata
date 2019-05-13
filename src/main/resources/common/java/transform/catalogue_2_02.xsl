<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bsc="http://www.battlescribe.net/schema/catalogueSchema"
                xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                exclude-result-prefixes="bsc">

    <xsl:output method="xml" indent="yes"/>
    
    <!-- Catalogue -->
    <xsl:template match="/bsc:catalogue">
        <catalogue>
            <!-- Attributes -->
            <xsl:attribute name="battleScribeVersion">2.02</xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'battleScribeVersion' and name(.) != 'book' and name(.) != 'page']"/>
            
            
            <!-- Nodes -->
            <publications>
                <xsl:apply-templates select="//*[@book]" mode="entry"/>
            </publications>
            
            <xsl:apply-templates select="node()"/>
        </catalogue>
    </xsl:template>
    
    
    <!-- Publication -->
    <xsl:key name="bookId" match="//*[@book]" use="@book"/>
    <xsl:template match="*" mode="entry">
        <xsl:if test="not(@book = '') and generate-id() = generate-id(key('bookId', @book)[1])">
            <publication>
                <!-- Attributes -->
                <xsl:attribute name="id"><xsl:value-of select="substring(/bsc:catalogue/@id, 1, 9)"/>-pub<xsl:value-of select="generate-id(key('bookId', @book)[1])"/></xsl:attribute>
                <xsl:attribute name="name"><xsl:value-of select="@book"/></xsl:attribute>
            </publication>
        </xsl:if>
    </xsl:template>
    
    
    <!-- PublicationLink -->
    <xsl:template match="@book">
        <xsl:if test="not(. = '')">
            <xsl:attribute name="publicationId"><xsl:value-of select="substring(/bsc:catalogue/@id, 1, 9)"/>-pub<xsl:value-of select="generate-id(key('bookId', .)[1])"/></xsl:attribute>
        </xsl:if>
    </xsl:template>
    
    
    <!-- Cost -->
    <xsl:template match="bsc:cost">
        <cost>
            <!-- Attributes -->
            <xsl:attribute name="typeId"><xsl:value-of select="@costTypeId"/></xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'costTypeId']"/>
        </cost>
    </xsl:template>
    
    
    <!-- Profile -->
    <xsl:template match="bsc:profile">
        <profile>
            <!-- Attributes -->
            <xsl:attribute name="typeId"><xsl:value-of select="@profileTypeId"/></xsl:attribute>
            <xsl:attribute name="typeName"><xsl:value-of select="@profileTypeName"/></xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'profileTypeId' and name(.) != 'profileTypeName']"/>
            
            
            <!-- Nodes -->
            <xsl:apply-templates select="node()"/>
        </profile>
    </xsl:template>
    
    
    <!-- Characteristic -->
    <xsl:template match="bsc:characteristic">
        <characteristic>
            <!-- Attributes -->
            <xsl:attribute name="typeId"><xsl:value-of select="@characteristicTypeId"/></xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'characteristicTypeId' and name(.) != 'value']"/>
            
            
            <!-- Value -->
            <xsl:value-of select="@value"/>
        </characteristic>
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
