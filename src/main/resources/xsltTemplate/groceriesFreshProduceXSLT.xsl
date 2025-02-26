<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:template match="/">
        {
            "order": {
                "id": "<xsl:value-of select="/order/id"/>",
                "customer": "<xsl:value-of select="/order/customer"/>",
                "amount": "<xsl:value-of select="/order/amount"/>",
                "country": "<xsl:value-of select="/order/country"/>",
                "category": {
                    "name": "<xsl:value-of select="/order/category/name"/>",
                    "subcategories": [
                        <xsl:for-each select="/order/category/subcategories/subcategory">
                            {
                                "name": "<xsl:value-of select="name"/>",
                                "items": [
                                    <xsl:for-each select="items/item">
                                        {
                                            "name": "<xsl:value-of select="name"/>",
                                            "price": "<xsl:value-of select="price"/>",
					    "loyaltyPoints": "<xsl:value-of select="loyaltyPoints"/>"
                                        }<xsl:if test="position() != last()">,</xsl:if>
                                    </xsl:for-each>
                                ]
                            }<xsl:if test="position() != last()">,</xsl:if>
                        </xsl:for-each>
                    ]
                }
            }
        }
    </xsl:template>
</xsl:stylesheet>