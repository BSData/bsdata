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
            <xsl:attribute name="battleScribeVersion">2.01</xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'battleScribeVersion']"/>
            
            
            <!-- Nodes -->
            <categoryEntries>
                <xsl:apply-templates select="//bsc:forceEntries/bsc:forceEntry/bsc:categoryEntries/*" mode="entry"/>
            </categoryEntries>
            
            <xsl:apply-templates select="node()"/>
        </catalogue>
    </xsl:template>
    
    
    <!-- CategoryEntry -->
    <xsl:key name="categoryEntryId" match="bsc:categoryEntry" use="@id"/>
    <xsl:template match="bsc:categoryEntry" mode="entry">
    	<xsl:if test="generate-id()=generate-id(key('categoryEntryId', @id)[1])">
            <categoryEntry>
                <!-- Attributes -->
                <xsl:apply-templates select="@*"/>


                <!-- Nodes -->
                <xsl:apply-templates select="node()[name(.) != 'constraints' and name(.) != 'modifiers']"/>
            </categoryEntry>
        </xsl:if>
    </xsl:template>
    
    
    <!-- ForceEntry -->
    <xsl:template match="bsc:forceEntry">
        <forceEntry>
            <!-- Attributes -->
            <xsl:apply-templates select="@*"/>
            
            
            <!-- Nodes -->
            <categoryLinks>
                <xsl:apply-templates select="bsc:categoryEntries/*" mode="link"/>
            </categoryLinks>
            
            <xsl:apply-templates select="node()[name(.) != 'categoryEntries']"/>
        </forceEntry>
    </xsl:template>
    
    
    <!-- CategoryLink -->
    <xsl:template match="bsc:categoryEntry" mode="link">
        <categoryLink>
            <!-- Attributes -->
            <xsl:attribute name="id"><xsl:value-of select="../../@id"/>-<xsl:value-of select="@id"/></xsl:attribute>
            <xsl:attribute name="targetId"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'id']"/>
            
            
            <!-- Nodes -->
            <xsl:apply-templates select="node()"/>
        </categoryLink>
    </xsl:template>


    <!-- SelectionEntry -->
    <xsl:template match="bsc:selectionEntry">
        <selectionEntry>
            <!-- Attributes -->
            <xsl:apply-templates select="@*[name(.) != 'categoryEntryId']"/>
            
            
            <!-- Nodes -->
            <xsl:if test="@categoryEntryId != '' and @categoryEntryId != '(No Category)'">
                <categoryLinks>
                    <categoryLink>
                        <xsl:attribute name="id"><xsl:value-of select="@id"/>-<xsl:value-of select="@categoryEntryId"/></xsl:attribute>
                        <xsl:attribute name="targetId"><xsl:value-of select="@categoryEntryId"/></xsl:attribute>
                        <xsl:attribute name="primary">true</xsl:attribute>
                    </categoryLink>
                </categoryLinks>
            </xsl:if>
            
            <xsl:apply-templates select="node()"/>
        </selectionEntry>
    </xsl:template>
    
    
    <!-- EntryLink -->
    <xsl:template match="bsc:entryLink">
        <entryLink>
            <!-- Attributes -->
            <xsl:apply-templates select="@*[name(.) != 'categoryEntryId']"/>
            
            
            <!-- Nodes -->
            <xsl:if test="@categoryEntryId != '' and @categoryEntryId != '(No Category)'">
                <categoryLinks>
                    <categoryLink>
                        <xsl:attribute name="id"><xsl:value-of select="@id"/>-<xsl:value-of select="@categoryEntryId"/></xsl:attribute>
                        <xsl:attribute name="targetId"><xsl:value-of select="@categoryEntryId"/></xsl:attribute>
                        <xsl:attribute name="primary">true</xsl:attribute>
                    </categoryLink>
                </categoryLinks>
            </xsl:if>
            
            <xsl:apply-templates select="node()"/>
        </entryLink>
    </xsl:template>
    
    
    <!-- Query Scope -->
    <xsl:template match="@scope">
        <xsl:choose>
            <xsl:when test=". = 'category'">
                <xsl:attribute name="scope">primary-category</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="scope"><xsl:value-of select="."/></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
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
