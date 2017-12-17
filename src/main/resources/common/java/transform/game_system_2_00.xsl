<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bsg="http://www.battlescribe.net/schema/gameSystemSchema"
                xmlns="http://www.battlescribe.net/schema/gameSystemSchema"
                exclude-result-prefixes="bsg">

    <xsl:output method="xml" indent="yes"/>

    
    <!-- GameSystem -->
    <xsl:template match="/bsg:gameSystem">
        <gameSystem>
            <!-- Attributes -->
            <xsl:attribute name="battleScribeVersion">2.00</xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'books' and name(.) != 'battleScribeVersion']"/>
            <xsl:if test="@books">
                <xsl:attribute name="book"><xsl:value-of select="@books"/></xsl:attribute>
            </xsl:if>
            
            
            <!-- Nodes -->
            <costTypes>
                <costType id="points" name="pts" />
            </costTypes>
            
            <profileTypes>
                <xsl:apply-templates select="bsg:profileTypes/*"/>
            </profileTypes>
            
            <forceEntries>
                <xsl:apply-templates select="bsg:forceTypes/*"/>
            </forceEntries>
        </gameSystem>
    </xsl:template>
    
    
    <!-- ForceEntry -->
    <xsl:template match="bsg:forceType">
        <forceEntry>
            <!-- Attributes -->
            <xsl:apply-templates select="@id | @name"/>
            
            
            <!-- Nodes -->
            <constraints>
                <xsl:apply-templates select="@minSelections
                                            | @maxSelections
                                            | @minPoints
                                            | @maxPoints
                                            | @minPercentage
                                            | @maxPercentage"/>
            </constraints>
            
            <forceEntries>
                <xsl:apply-templates select="bsg:forceTypes/*"/>
            </forceEntries>
            
            <categoryEntries>
                <xsl:apply-templates select="bsg:categories/*"/>
            </categoryEntries>
        </forceEntry>
    </xsl:template>
    
    
    <!-- CategoryEntry -->
    <xsl:template match="bsg:category">
        <categoryEntry>
            <!-- Attributes -->
            <xsl:apply-templates select="@id | @name"/>
            
            
            <!-- Nodes -->
            <constraints>
                <xsl:apply-templates select="@minSelections
                                            | @maxSelections
                                            | @minPoints
                                            | @maxPoints
                                            | @minPercentage
                                            | @maxPercentage"/>
            </constraints>
            
            <modifiers>
                <xsl:apply-templates select="bsg:modifiers/*"/>
            </modifiers>
        </categoryEntry>
    </xsl:template>
    
    
    <!-- ProfileType -->
    <xsl:template match="bsg:profileType">
        <profileType>
            <!-- Attributes -->
            <xsl:apply-templates select="@*"/>
            
            
            <!-- Nodes -->
            <characteristicTypes>
                <xsl:apply-templates select="bsg:characteristics/*"/>
            </characteristicTypes>
        </profileType>
    </xsl:template>
    
    
    <!-- CharacteristicType -->
    <xsl:template match="bsg:characteristic">
        <characteristicType>
            <xsl:apply-templates select="@* | node()"/>
        </characteristicType>
    </xsl:template>
    
    
    <!-- Modifier -->
    <xsl:template match="bsg:modifier">
        <modifier>
            <!-- Attributes -->
            <xsl:apply-templates select="@type | @field | @value"/>
            
            <!-- Nodes -->
            <conditions>
                <xsl:apply-templates select="bsg:conditions/*"/>
            </conditions>
            
            <conditionGroups>
                <xsl:apply-templates select="bsg:conditionGroups/*"/>
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
                            <xsl:when test="@incrementField = 'percent'">
                                <xsl:attribute name="field">points</xsl:attribute>
                                <xsl:attribute name="percentValue">true</xsl:attribute>
                                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="field"><xsl:value-of select="@incrementField"/></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                        
                        <xsl:attribute name="scope">force</xsl:attribute>
                        <xsl:attribute name="childId"><xsl:value-of select="@incrementParentId"/></xsl:attribute>
                    </repeat>
                </repeats>
            </xsl:if>
        </modifier>
    </xsl:template>
    
    
    <!-- Condition -->
    <xsl:template match="bsg:condition">
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
                <xsl:when test="@field = 'percent'">
                    <xsl:attribute name="field">limit::points</xsl:attribute>
                    <xsl:attribute name="percentValue">true</xsl:attribute>
                    <xsl:attribute name="includeChildSelections">true</xsl:attribute>
                    <xsl:attribute name="scope">parent</xsl:attribute>
                    <xsl:attribute name="childId"><xsl:value-of select="@parentId"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="field"><xsl:value-of select="@field"/></xsl:attribute>
                    <xsl:attribute name="scope">parent</xsl:attribute>
                    <xsl:attribute name="childId"><xsl:value-of select="@parentId"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </condition>
    </xsl:template>
    
    
    <!-- Constraints -->
    <xsl:template match="@minSelections">
        <xsl:if test=". != 0 or ../bsg:modifiers/bsg:modifier[@field = 'minSelections']">
            <constraint>
                <xsl:attribute name="id">minSelections</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxSelections">
        <xsl:if test=". != -1 or ../bsg:modifiers/bsg:modifier[@field = 'maxSelections']">
            <constraint>
                <xsl:attribute name="id">maxSelections</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="field">selections</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="includeChildForces">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@minPoints">
        <xsl:if test=". != 0 or ../bsg:modifiers/bsg:modifier[@field = 'minPoints']">
            <constraint>
                <xsl:attribute name="id">minPoints</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="field">points</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxPoints">
        <xsl:if test=". != -1 or ../bsg:modifiers/bsg:modifier[@field = 'maxPoints']">
            <constraint>
                <xsl:attribute name="id">maxPoints</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="field">points</xsl:attribute>
                <xsl:attribute name="scope">parent</xsl:attribute>
                <xsl:attribute name="includeChildForces">true</xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@minPercentage">
        <xsl:if test=". != 0 or ../bsg:modifiers/bsg:modifier[@field = 'minPercentage']">
            <constraint>
                <xsl:attribute name="id">minPercentage</xsl:attribute>
                <xsl:attribute name="type">min</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="percentValue">true</xsl:attribute>
                <xsl:attribute name="field">limit::points</xsl:attribute>
                <xsl:attribute name="scope">roster</xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@maxPercentage">
        <xsl:if test=". != -1 or ../bsg:modifiers/bsg:modifier[@field = 'maxPercentage']">
            <constraint>
                <xsl:attribute name="id">maxPercentage</xsl:attribute>
                <xsl:attribute name="type">max</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                <xsl:attribute name="percentValue">true</xsl:attribute>
                <xsl:attribute name="field">limit::points</xsl:attribute>
                <xsl:attribute name="scope">roster</xsl:attribute>
                <xsl:attribute name="includeChildForces">true</xsl:attribute>
                <xsl:attribute name="includeChildSelections">true</xsl:attribute>
            </constraint>
        </xsl:if>
    </xsl:template>
    
    
    <!-- Defaults -->
    <xsl:template match="* | bsg:*">
        <xsl:element name="{local-name(.)}">
            <xsl:apply-templates select="node()|@*"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:copy/>
    </xsl:template>
    
</xsl:stylesheet>
