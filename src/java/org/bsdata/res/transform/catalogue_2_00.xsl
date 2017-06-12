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
            <xsl:attribute name="battleScribeVersion">2.00</xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'books' and name(.) != 'battleScribeVersion']"/>
            <xsl:if test="@books">
                <xsl:attribute name="book"><xsl:value-of select="@books"/></xsl:attribute>
            </xsl:if>
            
            <!-- Nodes -->
            <selectionEntries>
                <xsl:apply-templates select="bsc:entries/*"/>
            </selectionEntries>
            
            <rules>
                <xsl:apply-templates select="bsc:rules/*"/>
            </rules>
            
            <entryLinks>
                <xsl:apply-templates select="bsc:links/*[@linkType = 'entry' or @linkType = 'entry group']"/>
            </entryLinks>
            
            <infoLinks>
                <xsl:apply-templates select="bsc:links/*[@linkType = 'rule' or @linkType = 'profile']"/>
            </infoLinks>
            
            <sharedSelectionEntries>
                <xsl:apply-templates select="bsc:sharedEntries/*"/>
            </sharedSelectionEntries>
            
            <sharedSelectionEntryGroups>
                <xsl:apply-templates select="bsc:sharedEntryGroups/*"/>
            </sharedSelectionEntryGroups>
            
            <sharedRules>
                <xsl:apply-templates select="bsc:sharedRules/*"/>
            </sharedRules>
            
            <sharedProfiles>
                <xsl:apply-templates select="bsc:sharedProfiles/*"/>
            </sharedProfiles>
        </catalogue>
    </xsl:template>


    <!-- SelectionEntry -->
    <xsl:template match="bsc:entry">
        <selectionEntry>
            <!-- Attributes -->
            <xsl:apply-templates select="@id
                                        | @name
                                        | @type
                                        | @collective
                                        | @hidden
                                        | @book
                                        | @page
                                        | @description"/>
            <xsl:if test="@categoryId">
                <xsl:attribute name="categoryEntryId"><xsl:value-of select="@categoryId"/></xsl:attribute>
            </xsl:if>
        
            
            <!-- Nodes -->
            <costs>
                <cost costTypeId="points" name="pts">
                    <xsl:attribute name="value"><xsl:value-of select="@points"/></xsl:attribute>
                </cost>
            </costs>
            
            <constraints>
                <xsl:apply-templates select="@minSelections
                                            | @maxSelections
                                            | @minPoints
                                            | @maxPoints
                                            | @minInForce
                                            | @maxInForce
                                            | @minInRoster
                                            | @maxInRoster"/>
            </constraints>
            
            <modifiers>
                <xsl:apply-templates select="bsc:modifiers/*"/>
            </modifiers>
            
            <selectionEntries>
                <xsl:apply-templates select="bsc:entries/*"/>
            </selectionEntries>
            
            <selectionEntryGroups>
                <xsl:apply-templates select="bsc:entryGroups/*"/>
            </selectionEntryGroups>
            
            <rules>
                <xsl:apply-templates select="bsc:rules/*"/>
            </rules>
            
            <profiles>
                <xsl:apply-templates select="bsc:profiles/*"/>
            </profiles>
            
            <entryLinks>
                <xsl:apply-templates select="bsc:links/*[@linkType = 'entry' or @linkType = 'entry group']"/>
            </entryLinks>
            
            <infoLinks>
                <xsl:apply-templates select="bsc:links/*[@linkType = 'rule' or @linkType = 'profile']"/>
            </infoLinks>
        </selectionEntry>
    </xsl:template>


    <!-- SelectionEntryGroup -->
    <xsl:template match="bsc:entryGroup">
        <selectionEntryGroup>
            <!-- Attributes -->
            <xsl:apply-templates select="@id
                                        | @name
                                        | @collective
                                        | @hidden"/>
            <xsl:if test="@defaultEntryId">
                <xsl:attribute name="defaultSelectionEntryId"><xsl:value-of select="@defaultEntryId"/></xsl:attribute>
            </xsl:if>
                                        
                                        
            <!-- Nodes -->
            <constraints>
                <xsl:apply-templates select="@minSelections
                                            | @maxSelections
                                            | @minPoints
                                            | @maxPoints
                                            | @minInForce
                                            | @maxInForce
                                            | @minInRoster
                                            | @maxInRoster"/>
            </constraints>
            
            <modifiers>
                <xsl:apply-templates select="bsc:modifiers/*"/>
            </modifiers>
            
            <selectionEntries>
                <xsl:apply-templates select="bsc:entries/*"/>
            </selectionEntries>
            
            <selectionEntryGroups>
                <xsl:apply-templates select="bsc:entryGroups/*"/>
            </selectionEntryGroups>
            
            <entryLinks>
                <xsl:apply-templates select="bsc:links/*[@linkType = 'entry' or @linkType = 'entry group']"/>
            </entryLinks>
        </selectionEntryGroup>
    </xsl:template>
    
    
    <!-- EntryLink -->
    <xsl:template match="bsc:link[@linkType = 'entry' or @linkType = 'entry group']">
        <entryLink>
            <!-- Attributes -->
            <xsl:apply-templates select="@id | @targetId"/>
            <xsl:if test="@categoryId">
                <xsl:attribute name="categoryEntryId"><xsl:value-of select="@categoryId"/></xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="@linkType = 'entry'">
                    <xsl:attribute name="type">selectionEntry</xsl:attribute>
                </xsl:when>
                <xsl:when test="@linkType = 'entry group'">
                    <xsl:attribute name="type">selectionEntryGroup</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="type"><xsl:value-of select="@linkType"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            
            
            <!-- Nodes -->
            <modifiers>
                <xsl:apply-templates select="bsc:modifiers/*"/>
            </modifiers>
        </entryLink>
    </xsl:template>
    
    
    <!-- InfoLink -->
    <xsl:template match="bsc:link[@linkType = 'rule' or @linkType = 'profile']">
        <infoLink>
            <!-- Attributes -->
            <xsl:apply-templates select="@id | @targetId"/>
            <xsl:attribute name="type"><xsl:value-of select="@linkType"/></xsl:attribute>
            
            
            <!-- Nodes -->
            <modifiers>
                <xsl:apply-templates select="bsc:modifiers/*"/>
            </modifiers>
        </infoLink>
    </xsl:template>


    <!-- Characteristic -->
    <xsl:template match="bsc:characteristic">
        <characteristic>
            <!-- Attributes -->
            <xsl:attribute name="characteristicTypeId"><xsl:value-of select="@characteristicId"/></xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'characteristicId']"/>
        </characteristic>
    </xsl:template>
    
    
    <!-- Modifier -->
    <xsl:template match="bsc:modifier">
        <modifier>
            <!-- Attributes -->
            <xsl:choose>
                <xsl:when test="@type = 'show' or @type = 'hide'">
                    <xsl:attribute name="type">set</xsl:attribute>
                    <xsl:attribute name="field">hidden</xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="@type = 'show'">
                            <xsl:attribute name="value">false</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="@type = 'hide'">
                            <xsl:attribute name="value">true</xsl:attribute>
                        </xsl:when>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="@type | @field | @value"/>
                </xsl:otherwise>
            </xsl:choose>
            
            
            <!-- Nodes -->
            <conditions>
                <xsl:apply-templates select="bsc:conditions/*"/>
            </conditions>
            
            <conditionGroups>
                <xsl:apply-templates select="bsc:conditionGroups/*"/>
            </conditionGroups>
            
            <xsl:if test="@repeat = 'true'">
                <repeats>
                    <repeat>
                        <xsl:attribute name="repeats"><xsl:value-of select="@numRepeats"/></xsl:attribute>
                        <xsl:attribute name="value"><xsl:value-of select="@incrementValue"/></xsl:attribute>
                        
                        <xsl:choose>
                            <xsl:when test="@incrementField = 'points limit'">
                                <xsl:attribute name="field">limit::points</xsl:attribute>
                            </xsl:when>
                            <xsl:when test="@incrementField = 'total selections'">
                                <xsl:attribute name="field">selections</xsl:attribute>
                            </xsl:when>
                            <xsl:when test="@incrementField = 'points'">
                                <xsl:attribute name="field">points</xsl:attribute>
                                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="field"><xsl:value-of select="@incrementField"/></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                        
                        <xsl:choose>
                            <xsl:when test="@incrementParentId = 'direct parent'">
                                <xsl:attribute name="scope">parent</xsl:attribute>
                            </xsl:when>
                            <xsl:when test="@incrementParentId = 'force type'">
                                <xsl:attribute name="scope">force</xsl:attribute>
                                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                            </xsl:when>
                            <xsl:when test="@incrementParentId = 'roster' or @incrementParentId = 'category'">
                                <xsl:attribute name="scope"><xsl:value-of select="@incrementParentId"/></xsl:attribute>
                                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="scope"><xsl:value-of select="@incrementParentId"/></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                        
                        <xsl:attribute name="childId"><xsl:value-of select="@incrementChildId"/></xsl:attribute>
                        <xsl:attribute name="shared">true</xsl:attribute>
                    </repeat>
                </repeats>
            </xsl:if>
        </modifier>
    </xsl:template>
    
    
    <!-- Condition -->
    <xsl:template match="bsc:condition">
        <condition>
            <!-- Attributes -->
            <xsl:choose>
                <xsl:when test="@type = 'less than'">
                    <xsl:attribute name="type">lessThan</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = 'greater than'">
                    <xsl:attribute name="type">greaterThan</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = 'equal to'">
                    <xsl:attribute name="type">equalTo</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = 'not equal to'">
                    <xsl:attribute name="type">notEqualTo</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = 'at least'">
                    <xsl:attribute name="type">atLeast</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = 'at most'">
                    <xsl:attribute name="type">atMost</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = 'instance of'">
                    <xsl:attribute name="type">instanceOf</xsl:attribute>
                </xsl:when>
            </xsl:choose>
            
            <xsl:apply-templates select="@value"/>
            
            <xsl:choose>
                <xsl:when test="@field = 'points limit'">
                    <xsl:attribute name="field">limit::points</xsl:attribute>
                    <xsl:attribute name="scope">roster</xsl:attribute>
                    <xsl:attribute name="childId">any</xsl:attribute>
                </xsl:when>
                <xsl:when test="@field = 'total selections'">
                    <xsl:attribute name="field">selections</xsl:attribute>
                    <xsl:attribute name="scope">roster</xsl:attribute>
                    <xsl:attribute name="childId">any</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="@field = 'points'">
                            <xsl:attribute name="field">points</xsl:attribute>
                            <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="field"><xsl:value-of select="@field"/></xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:choose>
                        <xsl:when test="@parentId = 'direct parent'">
                            <xsl:attribute name="scope">parent</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="@parentId = 'force type'">
                            <xsl:attribute name="scope">force</xsl:attribute>
                            <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="@parentId = 'roster' or @parentId = 'category'">
                            <xsl:attribute name="scope"><xsl:value-of select="@parentId"/></xsl:attribute>
                            <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="scope"><xsl:value-of select="@parentId"/></xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:attribute name="childId"><xsl:value-of select="@childId"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:attribute name="shared">true</xsl:attribute>
        </condition>
    </xsl:template>


    <!-- Constraints -->
    <xsl:template match="@minSelections">
        <xsl:if test=". != 0 or ../bsc:modifiers/bsc:modifier[@field = 'minSelections'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">minSelections</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxSelections">
        <xsl:if test=". != -1 or ../bsc:modifiers/bsc:modifier[@field = 'maxSelections'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">maxSelections</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@minPoints">
        <xsl:if test=". != 0 or ../bsc:modifiers/bsc:modifier[@field = 'minPoints'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">minPoints</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="field">points</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxPoints">
        <xsl:if test=". != -1 or ../bsc:modifiers/bsc:modifier[@field = 'maxPoints'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">maxPoints</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="field">points</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@minInForce">
        <xsl:if test=". != 0 or ../bsc:modifiers/bsc:modifier[@field = 'minInForce'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">minInForce</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">force</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                <xsl:if test="ancestor::bsc:sharedEntries or ancestor::bsc:sharedEntryGroups">
                    <xsl:attribute name="shared">true</xsl:attribute>
                </xsl:if>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxInForce">
        <xsl:if test=". != -1 or ../bsc:modifiers/bsc:modifier[@field = 'maxInForce'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">maxInForce</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">force</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                <xsl:if test="ancestor::bsc:sharedEntries or ancestor::bsc:sharedEntryGroups">
                    <xsl:attribute name="shared">true</xsl:attribute>
                </xsl:if>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@minInRoster">
        <xsl:if test=". != 0 or ../bsc:modifiers/bsc:modifier[@field = 'minInRoster'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">minInRoster</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">roster</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                <xsl:if test="ancestor::bsc:sharedEntries or ancestor::bsc:sharedEntryGroups">
                    <xsl:attribute name="shared">true</xsl:attribute>
                </xsl:if>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxInRoster">
        <xsl:if test=". != -1 or ../bsc:modifiers/bsc:modifier[@field = 'maxInRoster'] 
                or local-name(../..) = 'sharedEntries' or local-name(../..) = 'sharedEntryGroups'">
            <constraint>
                <xsl:attribute name="id">maxInRoster</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">roster</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                <xsl:if test="ancestor::bsc:sharedEntries or ancestor::bsc:sharedEntryGroups">
                    <xsl:attribute name="shared">true</xsl:attribute>
                </xsl:if>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template match="* | bsc:*">
        <xsl:element name="{local-name(.)}">
            <xsl:apply-templates select="node() | @*"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="@*">
        <xsl:copy/>
    </xsl:template>
    
</xsl:stylesheet>
