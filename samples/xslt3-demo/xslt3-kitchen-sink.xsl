<xsl:stylesheet version="3.0"
    expand-text="yes"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:array="http://www.w3.org/2005/xpath-functions/array"
    xmlns:demo="urn:demo"
    xmlns:err="http://www.w3.org/2005/xqt-errors"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="array demo err map math xs">

    <xsl:import href="common.xsl"/>

    <xsl:output method="xml" indent="yes" use-character-maps="demo:ascii-map"/>

    <xsl:character-map name="demo:ascii-map">
        <xsl:output-character character="é" string="e"/>
    </xsl:character-map>

    <xsl:mode name="demo:main" on-no-match="shallow-copy" use-accumulators="demo:running-total"/>
    <xsl:mode name="demo:summary" on-no-match="shallow-skip"/>

    <xsl:decimal-format name="demo:euro" decimal-separator="," grouping-separator="."/>

    <xsl:key name="demo:by-kind" match="demo:item" use="@kind"/>

    <xsl:attribute-set name="demo:common-attrs">
        <xsl:attribute name="data-generated">yes</xsl:attribute>
        <xsl:attribute name="data-xslt-version">3.0</xsl:attribute>
    </xsl:attribute-set>

    <xsl:param name="debug" as="xs:boolean" select="false()"/>

    <xsl:variable name="demo:lookup" as="map(xs:string, xs:integer)"
        select="map{ 'alpha': 1, 'beta': 2, 'gamma': 3 }"/>

    <xsl:variable name="demo:numbers" as="xs:integer*" select="1 to 6"/>

    <xsl:variable name="demo:constructed-map" as="map(xs:string, item()*)">
        <xsl:map>
            <xsl:map-entry key="'mode'" select="'demo:main'"/>
            <xsl:map-entry key="'formats'" select="('xml', 'json', 'text')"/>
            <xsl:map-entry key="'today'" select="current-date()"/>
        </xsl:map>
    </xsl:variable>

    <xsl:variable name="demo:constructed-doc" as="document-node()">
        <xsl:document>
            <demo:root created="{current-date()}">
                <demo:item kind="alpha" price="12.50">one</demo:item>
                <demo:item kind="beta" price="9.10">two</demo:item>
                <demo:item kind="alpha" price="7.40">three</demo:item>
                <demo:item kind="gamma" price="18.95">four</demo:item>
                <number>10</number>
                <number>20</number>
                <number>30</number>
            </demo:root>
        </xsl:document>
    </xsl:variable>

    <xsl:accumulator name="demo:running-total" as="xs:integer" initial-value="0">
        <xsl:accumulator-rule match="number" select="$value + xs:integer(.)"/>
    </xsl:accumulator>

    <xsl:function name="demo:showcase-functions" as="map(xs:string, item()*)">
        <xsl:param name="items" as="element(demo:item)*"/>

        <xsl:sequence select="map{
            'abs': abs(-5),
            'avg': avg($demo:numbers),
            'distinct-values': distinct-values($items/@kind ! string()),
            'empty': empty($items[@kind = 'missing']),
            'exists': exists($items),
            'filter': filter($demo:numbers, function($n) { $n mod 2 = 0 }),
            'fold-left': fold-left($demo:numbers, 0, function($sum, $n) { $sum + $n }),
            'for-each': for-each($demo:numbers, function($n) { $n * $n }),
            'format-date': format-date(xs:date('2026-03-22'), '[Y0001]-[M01]-[D01]'),
            'format-dateTime': format-dateTime(xs:dateTime('2026-03-22T12:34:56'), '[H01]:[m01]:[s01]'),
            'format-number': format-number(12345.678, '#,##0.00'),
            'head': head($demo:numbers),
            'innermost': innermost(($items, $items[1]/text())),
            'json-roundtrip': xml-to-json(json-to-xml('{&quot;alpha&quot;:1}')),
            'lower-case': lower-case('MIXED'),
            'map-contains': map:contains($demo:lookup, 'gamma'),
            'map-get': map:get($demo:lookup, 'beta'),
            'map-keys': map:keys($demo:lookup),
            'matches': matches('abc123', '\d+'),
            'math-pi': math:pi(),
            'math-sqrt': math:sqrt(81),
            'normalize-space': normalize-space('  too   much  space  '),
            'outermost': outermost(($items, $items[1]/text())),
            'parse-json': parse-json('{&quot;alpha&quot;:1}'),
            'path': path($items[1]),
            'QName': QName('urn:demo', 'demo:node'),
            'replace': replace('abracadabra', 'a', 'A'),
            'resolve-uri': resolve-uri('common.xsl', static-base-uri()),
            'reverse': reverse($demo:numbers),
            'round-half-to-even': round-half-to-even(2.5),
            'sort': sort($items/@price ! xs:decimal(.)),
            'string-join': string-join($items ! string(.), '|'),
            'subsequence': subsequence($demo:numbers, 2, 3),
            'tail': tail($demo:numbers),
            'tokenize': tokenize('alpha,beta,gamma', ','),
            'upper-case': upper-case('mixed'),
            'array-append': array:append([1, 2], 3),
            'array-head': array:head(['first', 'second']),
            'array-size': array:size(['a', 'b', 'c']),
            'array-tail': array:tail([1, 2, 3])
        }"/>
    </xsl:function>

    <xsl:template name="main">
        <xsl:param name="root" as="element()?" select="()"/>

        <xsl:if test="$debug">
            <xsl:message expand-text="yes">Running {static-base-uri()}</xsl:message>
        </xsl:if>
        <xsl:call-template name="demo:emit-report">
            <xsl:with-param name="root" select="($root, $demo:constructed-doc/demo:root)[1]"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/">
        <xsl:call-template name="main">
            <xsl:with-param name="root" select="/*"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="demo:emit-report">
        <xsl:param name="root" as="element()?"/>

        <xsl:variable name="effective-root" as="element()"
            select="($root[self::demo:root], $demo:constructed-doc/demo:root)[1]"/>
        <xsl:variable name="items" as="element(demo:item)*" select="$effective-root/demo:item"/>
        <xsl:variable name="templating-doc" as="document-node()">
            <xsl:document>
                <xsl:copy-of select="$effective-root"/>
            </xsl:document>
        </xsl:variable>
        <xsl:variable name="function-map" as="map(xs:string, item()*)"
            select="demo:showcase-functions($items)"/>

        <xsl:assert test="exists($items)" select="'The kitchen sink sample expects demo:item elements.'"/>

        <demo:report xsl:use-attribute-sets="demo:common-attrs">
            <xsl:namespace name="extra" select="'urn:extra'"/>
            <xsl:comment>Intentional XSLT 3.0 kitchen sink sample for editor inspection.</xsl:comment>
            <xsl:processing-instruction name="sample">xslt3-kitchen-sink</xsl:processing-instruction>

            <title>XSLT 3.0 kitchen sink</title>

            <constructed-map>
                <xsl:for-each select="sort(map:keys($demo:constructed-map))">
                    <entry key="{.}">
                        <xsl:value-of select="string-join($demo:constructed-map(.) ! string(.), ', ')"/>
                    </entry>
                </xsl:for-each>
            </constructed-map>

            <named-template>
                <xsl:call-template name="demo:hello"/>
            </named-template>

            <dynamic-element>
                <xsl:element name="demo:generated">
                    <xsl:attribute name="status">created</xsl:attribute>
                    <xsl:text>Built with xsl:element and xsl:attribute.</xsl:text>
                </xsl:element>
            </dynamic-element>

            <function-showcase>
                <xsl:for-each select="sort(map:keys($function-map))">
                    <entry name="{.}">
                        <xsl:value-of select="serialize($function-map(.), map { 'method': 'adaptive' })"/>
                    </entry>
                </xsl:for-each>
            </function-showcase>

            <grouped-items>
                <xsl:for-each-group select="$items" group-by="@kind">
                    <group kind="{current-grouping-key()}">
                        <xsl:value-of select="string-join(current-group() ! string(.), ', ')"/>
                    </group>
                </xsl:for-each-group>
            </grouped-items>

            <sorted-items>
                <xsl:perform-sort select="$items">
                    <xsl:sort select="@price ! xs:decimal(.)"/>
                </xsl:perform-sort>
            </sorted-items>

            <iteration>
                <xsl:iterate select="$demo:numbers">
                    <xsl:param name="sum" as="xs:integer" select="0"/>
                    <xsl:on-completion>
                        <total value="{$sum}"/>
                    </xsl:on-completion>
                    <value current="{.}" running-total="{$sum + .}"/>
                    <xsl:choose>
                        <xsl:when test=". ge 4">
                            <xsl:break/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:next-iteration>
                                <xsl:with-param name="sum" select="$sum + ."/>
                            </xsl:next-iteration>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:iterate>
            </iteration>

            <regex-analysis>
                <xsl:analyze-string select="'A12 B34'" regex="[A-Z]+|\d+">
                    <xsl:matching-substring>
                        <token type="{if (matches(., '^\d+$')) then 'number' else 'word'}">{.}</token>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <separator>{normalize-space(.)}</separator>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </regex-analysis>

            <exception-demo>
                <xsl:try>
                    <xsl:sequence select="xs:integer('oops')"/>
                    <xsl:catch errors="*">
                        <caught code="{$err:code}" description="{$err:description}"/>
                    </xsl:catch>
                </xsl:try>
            </exception-demo>

            <copied-items>
                <xsl:copy-of select="$items"/>
            </copied-items>

            <templated-items>
                <xsl:apply-templates select="$templating-doc" mode="demo:main"/>
            </templated-items>
        </demo:report>
    </xsl:template>

    <xsl:template match="/" mode="demo:main">
        <xsl:apply-templates select="demo:root" mode="#current"/>
    </xsl:template>

    <xsl:template match="demo:root" mode="demo:main">
        <xsl:apply-templates select="demo:item | number" mode="#current"/>
    </xsl:template>

    <xsl:template match="demo:item[@kind = 'alpha']" mode="demo:main">
        <featured-item original-kind="{@kind}">
            <xsl:next-match/>
        </featured-item>
    </xsl:template>

    <xsl:template match="demo:item" mode="demo:main">
        <item-summary>
            <xsl:number level="any" count="demo:item"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="upper-case(.)"/>
            <xsl:text> (€</xsl:text>
            <xsl:value-of select="format-number(xs:decimal(@price), '#.##0,00', 'demo:euro')"/>
            <xsl:text>)</xsl:text>
        </item-summary>
    </xsl:template>

    <xsl:template match="number" mode="demo:main">
        <number-summary value="{.}" accumulator-after="{accumulator-after('demo:running-total')}"/>
    </xsl:template>
</xsl:stylesheet>
