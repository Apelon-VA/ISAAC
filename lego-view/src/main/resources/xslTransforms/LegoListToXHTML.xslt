<?xml version="1.0" encoding="UTF-8"?>
<!-- A XSL transform which turns (valid) Lego XML files into XHTML 1.1 HTML files.  Internal formatting of the output is poor - 
		suggest using something like JTidy in the pipeline to format the output XHTML.
		Author - Dan Armbrust
 -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:text>&#10;</xsl:text>
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"></xsl:text>
		<html version="-//W3C//DTD XHTML 1.1//EN" xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml11.xsd">
			<head>
				<style type="text/css">
	body
	{
		font-family:"Arial", Sans-serif;
		font-size: 15px;
		margin: 5px 15px;
		background: #fff;
		border-style:double;
		border-width:2px; 
	}
	.title
	{
		background: #ccc;
		padding: 10px 5px;
		font-size: 18px; 
		font-weight: bold;
	}
	.sectionTitle
	{
		background: #ccc;
		padding: 10px 5px;
		font-size: 16px; 
		font-weight: bold;
	}
	.indent
	{
		padding: 5px 0 0px 5px;
		margin: 0 0 0 10px;
		border-left-style:solid;
		border-width:1px;
	}

				</style>
				<title>
					<xsl:value-of select="legoList/groupName" />
				</title>
			</head>
			<body>
				<div class="title">Lego List '<xsl:value-of select="legoList/groupName" />'</div>
				<div class="indent">
					Description: <xsl:value-of select="legoList/groupDescription" /><br/>
					UUID: <xsl:value-of select="legoList/legoListUUID" /><br/>
					Description: <xsl:value-of select="legoList/comment" />
					
					<xsl:for-each select="legoList/lego">
					<div class="sectionTitle">Lego <xsl:value-of select="legoUUID"/></div>
					<div class="indent">
							<xsl:value-of select="pncs/@name"/><br/> 
						<table>
							<tr>
								<td style="min-width: 200px;">Value: <xsl:value-of select="pncs/@value"/></td>
								<td style="min-width: 200px;">ID: <xsl:value-of select="pncs/@id"/></td>
							</tr>
							<tr>
								<td>Author: <xsl:value-of select="stamp/@author"/></td>
								<td>Time: <xsl:value-of select="stamp/@time"/></td>
							</tr>
							<tr>
								<td>Status: <xsl:value-of select="stamp/@status"/></td>
								<td>Module: <xsl:value-of select="stamp/@module"/></td>
							</tr>
							<tr>
								<td>Path: <xsl:value-of select="stamp/@path"/></td>
							</tr>
						</table>
						<xsl:for-each select="assertion">
							<div class="sectionTitle">Assertion <xsl:value-of select="assertionUUID" /></div>
							<div class="indent">
								<div class="sectionTitle">Discernible</div>
								<div class="indent">
									<xsl:apply-templates select="discernible/expression"/>
								</div>
								<div class="sectionTitle">Qualifier</div>
								<div class="indent">
									<xsl:apply-templates select="qualifier/expression"/>
								</div>
								<xsl:if test="timing">
									<div class="sectionTitle">Timing</div>
									<div class="indent">
										<xsl:apply-templates select="timing"/>
									</div>
								</xsl:if>
								<div class="sectionTitle">Value</div>
								<div class="indent">
									<xsl:apply-templates select="value/expression"/>
									<xsl:value-of select="value/text"/>
									<xsl:value-of select="value/boolean"/>
									<xsl:apply-templates select="value/measurement"/>
								</div>
								<xsl:if test="assertionComponent">
									<div class="sectionTitle">Assertion Components</div>
									<div class="indent">
										<xsl:for-each select="assertionComponent">
											<xsl:apply-templates select="type/concept"/> -&gt; Assertion <xsl:value-of select="assertionUUID" /><br/>
										</xsl:for-each>
									</div>
								</xsl:if>
							</div>
						</xsl:for-each>
						</div>
					</xsl:for-each>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="expression">
		<xsl:if test="concept">
			<xsl:apply-templates select="concept"/><br/>
		</xsl:if>
		<xsl:if test="expression">
			<xsl:apply-templates select="expression"/><br/>
		</xsl:if>
		<xsl:if test="relation">
			<xsl:apply-templates select="relation"/>
		</xsl:if>
		<xsl:if test="relationGroup/relation">
			<div class="indent">
				<xsl:apply-templates select="relationGroup/relation"/>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="relation">
		<div class="indent">
			<xsl:apply-templates select="type/concept"/> - &gt;
			<xsl:apply-templates select="destination/expression"/>
			<xsl:apply-templates select="destination/text"/>
			<xsl:apply-templates select="destination/boolean"/>
			<xsl:apply-templates select="destination/measurement"/>
		</div>
	</xsl:template>
	
	<xsl:template match="bound | lowerBound | upperBound">
		<xsl:choose>
			<xsl:when test="@lowerPointInclusive = 'false'">
				(
			</xsl:when>
			<xsl:otherwise>
				[
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="lowerPoint"/>, <xsl:value-of select="upperPoint"/> 
		<xsl:choose>
			<xsl:when test="@upperPointInclusive = 'false'">
				)
			</xsl:when>
			<xsl:otherwise>
				]
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="interval">
		<xsl:apply-templates match="lowerBound" /> X <xsl:apply-templates match="upperBound" /> 
	</xsl:template>
	
	
	<xsl:template match="measurement | timing">
		<xsl:if test="point">
			<xsl:value-of select="point/value"/>
		</xsl:if>
		<xsl:if test="bound">
			<xsl:apply-templates match="bound" />
		</xsl:if>
		<xsl:if test="interval">
			<xsl:apply-templates match="interval" />
		</xsl:if>
		<xsl:if test="units/concept">
			<br/><xsl:apply-templates select="units/concept"/>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template match="concept">
		<span>
			<xsl:attribute name="title">
				<xsl:value-of select="@sctid"/> : <xsl:value-of select="@uuid"/>
			</xsl:attribute>
			<xsl:value-of select="@desc" />
		</span>
	</xsl:template>
</xsl:stylesheet>