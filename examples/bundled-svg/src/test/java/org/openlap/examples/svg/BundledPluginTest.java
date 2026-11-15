package org.openlap.examples.svg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openlap.dataset.OpenLAPColumnConfigData;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.dataset.OpenLAPPortMapping;
import com.openlap.exceptions.UnTransformableData;
import com.openlap.exceptions.VisualizationCodeGenerationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BundledPluginTest {
    @Test
    public void bothInstalledChartClassesUseOneCompleteHashedRuntime() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(resource("META-INF/openlap/visualization.json"));
        Assert.assertEquals(1, manifest.path("schemaVersion").intValue());
        Assert.assertEquals(2, manifest.path("charts").size());
        Assert.assertEquals(1, manifest.path("runtimes").size());
        Set<String> assets = new HashSet<String>();
        for (JsonNode asset : manifest.path("assets")) {
            String path = asset.path("path").textValue();
            Assert.assertTrue(path.startsWith("web/"));
            Assert.assertFalse(path.contains(".."));
            Assert.assertTrue(assets.add(asset.path("id").textValue()));
            byte[] bytes = resource(path);
            StringBuilder hash = new StringBuilder();
            for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes)) hash.append(String.format("%02x", item & 255));
            Assert.assertEquals("Resource changed; run scripts/update-example-manifest.py --write", asset.path("sha256").textValue(), hash.toString());
        }
        for (JsonNode assetId : manifest.path("runtimes").get(0).path("assets")) Assert.assertTrue(assets.contains(assetId.textValue()));
        for (JsonNode chart : manifest.path("charts")) {
            Assert.assertEquals("svg-example", chart.path("runtimeId").textValue());
            Assert.assertTrue(assets.contains(chart.path("iconAssetId").textValue()));
            Assert.assertTrue(SvgChart.class.isAssignableFrom(Class.forName(chart.path("implementingClass").textValue())));
            Assert.assertEquals(6, chart.path("customizations").size());
        }
    }

    @Test
    public void bothGeneratorsCompileAgainstThePublishedBackendAbiAndEscapeInlineData() throws Exception {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("chartTitle", "</script><script>alert('title')</script>\u2028");
        options.put("seriesColor", "#ef6c00");
        for (SvgChart chart : new SvgChart[] {new BarChart(), new LineChart()}) {
            String html = chart.generateVisualizationCode(dataset("</script><img src=x onerror=alert(1)>", 3.5), mapping(), options);
            Assert.assertTrue(html.contains("window.OpenLAPExample.render(document.getElementById("));
            Assert.assertTrue(html.contains("\\u003c/script\\u003e"));
            Assert.assertTrue(html.contains("\\u2028"));
            Assert.assertFalse(html.contains("<img"));
            Assert.assertFalse(html.contains("</script><script>"));
            Assert.assertFalse(html.contains("https://"));
            Assert.assertTrue(html.contains("\"value\":3.5"));
            Assert.assertTrue(html.contains("\"seriesColor\":\"#ef6c00\""));
            Assert.assertEquals("", chart.visualizationLibraryScript());
        }
    }

    @Test
    public void repeatedChartsHaveDistinctContainerIdentifiers() throws Exception {
        BarChart chart = new BarChart();
        String first = chart.generateVisualizationCode(dataset("First", 1), mapping(), new HashMap<String, Object>());
        String second = chart.generateVisualizationCode(dataset("First", 1), mapping(), new HashMap<String, Object>());
        Assert.assertNotEquals(first, second);
    }

    @Test(expected = UnTransformableData.class)
    public void nonFiniteDataNeverBecomesJavaScript() throws Exception {
        new BarChart().generateVisualizationCode(dataset("Invalid", Double.NaN), mapping(), new HashMap<String, Object>());
    }

    @Test(expected = VisualizationCodeGenerationException.class)
    public void executableColorParametersAreRejected() throws Exception {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("seriesColor", "red; alert(1)");
        new BarChart().generateVisualizationCode(dataset("Valid", -4), mapping(), options);
    }

    @Test(expected = VisualizationCodeGenerationException.class)
    public void valuesOutsideAdvertisedChoicesAreRejected() throws Exception {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("valueLabelPosition", "left");
        new BarChart().generateVisualizationCode(dataset("Valid", -4), mapping(), options);
    }

    @SuppressWarnings("unchecked")
    private static OpenLAPDataSet dataset(String label, double value) throws Exception {
        OpenLAPDataSet data = new OpenLAPDataSet();
        data.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("sourceCategory", OpenLAPColumnDataType.Text, true, "Category", ""));
        data.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("sourceValue", OpenLAPColumnDataType.Numeric, true, "Value", ""));
        data.getColumns().get("sourceCategory").getData().add(label);
        data.getColumns().get("sourceValue").getData().add(value);
        return data;
    }

    private static OpenLAPPortConfig mapping() {
        OpenLAPPortConfig mapping = new OpenLAPPortConfig();
        mapping.getMapping().add(new OpenLAPPortMapping(
                port("sourceCategory", OpenLAPColumnDataType.Text), port("category", OpenLAPColumnDataType.Text)));
        mapping.getMapping().add(new OpenLAPPortMapping(
                port("sourceValue", OpenLAPColumnDataType.Numeric), port("value", OpenLAPColumnDataType.Numeric)));
        return mapping;
    }

    private static OpenLAPColumnConfigData port(String id, OpenLAPColumnDataType type) {
        return new OpenLAPColumnConfigData(id, type, true, id, "");
    }

    private static byte[] resource(String path) throws Exception {
        try (InputStream input = BundledPluginTest.class.getClassLoader().getResourceAsStream(path)) {
            Assert.assertNotNull("Required packaged resource: " + path, input);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) != -1;) bytes.write(buffer, 0, read);
            return bytes.toByteArray();
        }
    }
}
