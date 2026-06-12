package com.openlap.template;

import org.junit.Assert;
import org.junit.Test;

public class VisualizationEscaperTest {

    @Test
    public void escapeHtmlTextHandlesCommonTextValues() {
        Assert.assertEquals("", VisualizationEscaper.escapeHtmlText(null));
        Assert.assertEquals("normal text", VisualizationEscaper.escapeHtmlText("normal text"));
        Assert.assertEquals("Tom &amp; Jerry", VisualizationEscaper.escapeHtmlText("Tom & Jerry"));
        Assert.assertEquals("&quot;double quotes&quot;", VisualizationEscaper.escapeHtmlText("\"double quotes\""));
        Assert.assertEquals("&#39;single quotes&#39;", VisualizationEscaper.escapeHtmlText("'single quotes'"));
        Assert.assertEquals(
                "&lt;script&gt;alert(1)&lt;/script&gt;",
                VisualizationEscaper.escapeHtmlText("<script>alert(1)</script>"));
        Assert.assertEquals(
                "&lt;/script&gt;&lt;script&gt;alert(1)&lt;/script&gt;",
                VisualizationEscaper.escapeHtmlText("</script><script>alert(1)</script>"));
        Assert.assertEquals("C:\\temp\\file", VisualizationEscaper.escapeHtmlText("C:\\temp\\file"));
        Assert.assertEquals("line\nbreak", VisualizationEscaper.escapeHtmlText("line\nbreak"));
        Assert.assertEquals("M\u00fcnchen \u6771\u4eac", VisualizationEscaper.escapeHtmlText("M\u00fcnchen \u6771\u4eac"));
        Assert.assertEquals(
                "line&#8232;separator&#8233;end",
                VisualizationEscaper.escapeHtmlText("line\u2028separator\u2029end"));
    }

    @Test
    public void escapeHtmlAttributeHandlesAttributeValues() {
        Assert.assertEquals("", VisualizationEscaper.escapeHtmlAttribute(null));
        Assert.assertEquals("normal text", VisualizationEscaper.escapeHtmlAttribute("normal text"));
        Assert.assertEquals("Tom &amp; Jerry", VisualizationEscaper.escapeHtmlAttribute("Tom & Jerry"));
        Assert.assertEquals("&quot;double quotes&quot;", VisualizationEscaper.escapeHtmlAttribute("\"double quotes\""));
        Assert.assertEquals("&#39;single quotes&#39;", VisualizationEscaper.escapeHtmlAttribute("'single quotes'"));
        Assert.assertEquals(
                "&lt;script&gt;alert(1)&lt;/script&gt;",
                VisualizationEscaper.escapeHtmlAttribute("<script>alert(1)</script>"));
        Assert.assertEquals("C:\\temp\\file", VisualizationEscaper.escapeHtmlAttribute("C:\\temp\\file"));
        Assert.assertEquals("line&#10;break", VisualizationEscaper.escapeHtmlAttribute("line\nbreak"));
        Assert.assertEquals("M\u00fcnchen \u6771\u4eac", VisualizationEscaper.escapeHtmlAttribute("M\u00fcnchen \u6771\u4eac"));
        Assert.assertEquals(
                "line&#8232;separator&#8233;end",
                VisualizationEscaper.escapeHtmlAttribute("line\u2028separator\u2029end"));
    }

    @Test
    public void escapeJavaScriptStringHandlesStringLiteralValues() {
        Assert.assertEquals("", VisualizationEscaper.escapeJavaScriptString(null));
        Assert.assertEquals("normal text", VisualizationEscaper.escapeJavaScriptString("normal text"));
        Assert.assertEquals("Tom \\u0026 Jerry", VisualizationEscaper.escapeJavaScriptString("Tom & Jerry"));
        Assert.assertEquals("\\\"double quotes\\\"", VisualizationEscaper.escapeJavaScriptString("\"double quotes\""));
        Assert.assertEquals("\\'single quotes\\'", VisualizationEscaper.escapeJavaScriptString("'single quotes'"));
        Assert.assertEquals(
                "\\u003Cscript\\u003Ealert(1)\\u003C/script\\u003E",
                VisualizationEscaper.escapeJavaScriptString("<script>alert(1)</script>"));
        Assert.assertEquals(
                "\\u003C/script\\u003E\\u003Cscript\\u003Ealert(1)\\u003C/script\\u003E",
                VisualizationEscaper.escapeJavaScriptString("</script><script>alert(1)</script>"));
        Assert.assertEquals("C:\\\\temp\\\\file", VisualizationEscaper.escapeJavaScriptString("C:\\temp\\file"));
        Assert.assertEquals("line\\nbreak", VisualizationEscaper.escapeJavaScriptString("line\nbreak"));
        Assert.assertEquals(
                "M\u00fcnchen \u6771\u4eac",
                VisualizationEscaper.escapeJavaScriptString("M\u00fcnchen \u6771\u4eac"));
        Assert.assertEquals(
                "line\\u2028separator\\u2029end",
                VisualizationEscaper.escapeJavaScriptString("line\u2028separator\u2029end"));
    }

    @Test
    public void escapeJavaScriptStringProtectsGeneratedLabelsValuesAndParameters() {
        String label = "</script><script>alert(1)</script>";
        String value = "value \"quoted\" \\ path\nnext";
        String parameter = "Title 'quoted' \u2028 next";
        String escapedValue = VisualizationEscaper.escapeJavaScriptString(value);
        String escapedParameter = VisualizationEscaper.escapeJavaScriptString(parameter);

        String generated =
                "<script>"
                        + "var label='"
                        + VisualizationEscaper.escapeJavaScriptString(label)
                        + "';"
                        + "var value=\""
                        + escapedValue
                        + "\";"
                        + "var title='"
                        + escapedParameter
                        + "';"
                        + "</script>";

        Assert.assertFalse(generated.contains("</script><script>"));
        Assert.assertTrue(generated.contains("\\u003C/script\\u003E"));
        Assert.assertEquals("value \\\"quoted\\\" \\\\ path\\nnext", escapedValue);
        Assert.assertEquals("Title \\'quoted\\' \\u2028 next", escapedParameter);
        Assert.assertTrue(generated.contains("var value=\"" + escapedValue + "\";"));
        Assert.assertTrue(generated.contains("var title='" + escapedParameter + "';"));
    }
}
