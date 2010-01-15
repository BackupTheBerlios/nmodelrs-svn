<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:my="xxx">

    <xsl:function name="my:isType" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'T')"></xsl:value-of>
    </xsl:function>

    <xsl:function name="my:isConstructor" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'M') and contains($node/@name,'#ctor')"></xsl:value-of>
    </xsl:function>
            
    <xsl:function name="my:isMethod" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'M') and not(contains($node/@name,'#ctor'))"></xsl:value-of>
    </xsl:function>
    
    <xsl:function name="my:isProperty" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'P') and not(contains($node/@name,'('))"></xsl:value-of>
    </xsl:function>
    
    <xsl:function name="my:isIndexer" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'P') and contains($node/@name,'(')"></xsl:value-of>
    </xsl:function>
    
    <xsl:function name="my:isField" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'F')"></xsl:value-of>
    </xsl:function>
    
    <xsl:function name="my:isEvent" as="xs:boolean">
        <xsl:param name="node"/>
        <xsl:value-of select="starts-with($node/@name,'E')"></xsl:value-of>
    </xsl:function>
    
    <xsl:function name="my:fullName" as="xs:string">
        <xsl:param name="node"/>
        <xsl:value-of select="substring($node/@name,3)"></xsl:value-of>
    </xsl:function>
    
    <!--    
        function memberName(node, attr) {
        var cref = node.getAttribute(attr);
        var name = cref.substr(2);
        var p = name.indexOf("(");
        if (p == -1) {
        s = shortName(name);
        if (s == "#ctor") s = shortName(name.substr(0, name.length - 6));
        if (cref.charAt(0) == 'M') s = s + "()";
        return s;
        }
        else {
        s = shortName(name.substr(0, p));
        if (s == "#ctor") s = shortName(name.substr(0, p - 6));
        params = name.substr(p + 1, name.indexOf(")") - p - 1).split(",");
        for (i = 0; i < params.length; i++) params[i] = shortName(params[i]);
        if (cref.charAt(0) == 'P') return "this[" + params.join(",") + "]";
        return s + "(" + params.join(",") + ")";
        }
        }
    -->
    <xsl:function name="my:memberName" as="xs:string">
        <xsl:param name="node"/>
        <xsl:param name="attr"/>
        <xsl:variable name="cref" select="$node/@*[name()=$attr]"/>
        <xsl:variable name="name" select="substring($cref,2)"/>
        <xsl:choose>
            <xsl:when test="contains($name,'(')">
                <xsl:variable name="p1" select="tokenize(substring-before(substring-after($name,'('),')'),',')"/>
                <xsl:variable name="p2" select="string-join(for $p in $p1 return my:shortName($p),',')"/>
                <xsl:choose>
                    <xsl:when test="starts-with($cref,'P')">
                        <xsl:value-of select="concat('this[',$p2,']')"></xsl:value-of>                        
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="s" select="my:shortName(substring-before($name,'('))"/>
                        <xsl:variable name="s2" select="if ($s='#ctor') then my:shortName(substring-before($name,'.#ctor')) else $s"/>
                        <xsl:value-of select="concat($s2,'(',$p2,')')"></xsl:value-of>                        
                    </xsl:otherwise>                    
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="s" select="my:shortName($name)"/>
                <xsl:variable name="s2" select="if ($s='#ctor') then my:shortName(substring-before($name,'.#ctor')) else $s"/>
                <xsl:value-of select="if (starts-with($cref,'M')) then string-join($s2,'()') else $s2"></xsl:value-of>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="my:shortName" as="xs:string">
        <xsl:param name="name"/>
        <xsl:value-of select="tokenize($name,'\.')[last()]"/>
    </xsl:function>
    
<xsl:template match="/">
<HTML>
<HEAD>
<TITLE><xsl:value-of select="doc/assembly/name"/></TITLE>
<LINK rel="stylesheet" type="text/css" href="doc.css"/>
</HEAD>
<BODY>
    <xsl:apply-templates select="doc/members/member"/>
</BODY>
</HTML>
</xsl:template>

<xsl:template match="member">
    <xsl:choose>
        <xsl:when test="my:isType(.)">
            <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h1><xsl:value-of select="my:fullName(.)"/></h1></a>
            <!--xsl:apply-templates/-->
            <xsl:apply-templates select="summary"/>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
		</xsl:when>
		<xsl:when test="my:isMethod(.)">
<!--
		    <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h2><xsl:value-of select="my:memberName(., 'name')"/> method</h2></a>
			<xsl:apply-templates select="summary"/>
			<xsl:if test="param">
				<h4>Parameters</h4>
				<dl><xsl:apply-templates select="param"/></dl>
			</xsl:if>
			<xsl:apply-templates select="returns"/>
			<xsl:if test="exception">
				<h4>Exceptions</h4>
				<dl><xsl:apply-templates select="exception"/></dl>
			</xsl:if>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
-->
		</xsl:when>
		<xsl:when test="my:isConstructor(.)">
		    <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h2><xsl:value-of select="my:memberName(this, 'name')"/> constructor</h2></a>
			<xsl:apply-templates select="summary"/>
			<xsl:if test="param">
				<h4>Parameters</h4>
				<dl><xsl:apply-templates select="param"/></dl>
			</xsl:if>
			<xsl:if test="exception">
				<h4>Exceptions</h4>
				<dl><xsl:apply-templates select="exception"/></dl>
			</xsl:if>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
		</xsl:when>
        <xsl:when test="my:isProperty(.)">
            <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h2><xsl:value-of select="my:memberName(this, 'name')"/> property</h2></a>
			<xsl:apply-templates select="summary"/>
			<xsl:apply-templates select="value"/>
			<xsl:if test="exception">
				<h4>Exceptions</h4>
				<dl><xsl:apply-templates select="exception"/></dl>
			</xsl:if>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
		</xsl:when>
        <xsl:when test="my:isIndexer(.)">
            <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h2><xsl:value-of select="my:memberName(this, 'name')"/> indexer</h2></a>
			<xsl:apply-templates select="summary"/>
			<xsl:if test="param">
				<h4>Parameters</h4>
				<dl><xsl:apply-templates select="param"/></dl>
			</xsl:if>
			<xsl:apply-templates select="value"/>
			<xsl:if test="exception">
				<h4>Exceptions</h4>
				<dl><xsl:apply-templates select="exception"/></dl>
			</xsl:if>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
		</xsl:when>
        <xsl:when test="my:isField(.)">
            <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h2><xsl:value-of select="my:memberName(this, 'name')"/> field</h2></a>
			<xsl:apply-templates select="summary"/>
			<xsl:apply-templates select="value"/>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
		</xsl:when>
        <xsl:when test="my:isEvent(.)">
            <a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute><h2><xsl:value-of select="my:memberName(this, 'name')"/> event</h2></a>
			<xsl:apply-templates select="summary"/>
			<xsl:apply-templates select="remarks"/>
			<xsl:apply-templates select="example"/>
			<xsl:if test="seealso">
				<h4>See Also</h4>
				<xsl:apply-templates select="seealso"/>
			</xsl:if>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template match="summary"><p><xsl:apply-templates/></p></xsl:template>

<xsl:template match="param">
    <dt><i><xsl:value-of select="@name"/></i></dt>
	<dd><xsl:apply-templates/></dd>
</xsl:template>

<xsl:template match="value">
    <h4>Value</h4>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="returns">
    <h4>Returns</h4>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="exception">
    <dt><i><xsl:value-of select="my:memberName(this, 'cref')"/></i></dt>
	<dd><xsl:apply-templates/></dd>
</xsl:template>

<xsl:template match="remarks">
    <h4>Remarks</h4>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="example">
    <h4>Example</h4>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="seealso">
    <xsl:if test="./@cref"><a><xsl:attribute name="href">#<xsl:value-of select="@cref"/></xsl:attribute><xsl:value-of select="my:memberName(., 'cref')"/></a>&#160;</xsl:if>
</xsl:template>

<xsl:template match="para"><p><xsl:apply-templates/></p></xsl:template>

<xsl:template match="code"><pre><xsl:apply-templates/></pre></xsl:template>

<xsl:template match="see">
    <xsl:choose>
        <xsl:when test="./@langword"><code><xsl:value-of select="@langword"/></code></xsl:when>
        <xsl:when test="./@cref"><a><xsl:attribute name="href">#<xsl:value-of select="@cref"/></xsl:attribute><xsl:value-of select="my:memberName(., 'cref')"/></a></xsl:when>
    </xsl:choose>
</xsl:template>

<xsl:template match="list"><ul><xsl:apply-templates/></ul></xsl:template>

<xsl:template match="listheader"><tr><xsl:apply-templates/></tr></xsl:template>

<xsl:template match="item"><li><xsl:apply-templates/></li></xsl:template>

<xsl:template match="term"><td><xsl:apply-templates/></td></xsl:template>

<xsl:template match="description"><li><xsl:apply-templates/></li></xsl:template>

<xsl:template match="c"><code><xsl:apply-templates/></code></xsl:template>

<xsl:template match="paramref"><i><xsl:value-of select="@name"/></i></xsl:template>

<xsl:template match="*"><xsl:copy-of select="."/></xsl:template>
    
</xsl:stylesheet>