package com.openlap.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class VisualizationManifestTest {
    @Test
    public void publishedSchemaResourceMatchesTheOptionalContract() throws Exception {
        Assert.assertEquals("META-INF/openlap/visualization.json", VisualizationManifest.RESOURCE_PATH);
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(VisualizationManifest.SCHEMA_RESOURCE_PATH)) {
            Assert.assertNotNull("The schema must be packaged with the SDK", input);
            JsonNode schema = new ObjectMapper().readTree(input);
            Assert.assertEquals(VisualizationManifest.SCHEMA_VERSION,
                    schema.path("properties").path("schemaVersion").path("const").intValue());
            Assert.assertFalse(schema.path("additionalProperties").booleanValue());
            Assert.assertTrue(schema.path("$defs").has("asset"));
            Assert.assertTrue(schema.path("$defs").has("customization"));
            Assert.assertEquals(64, schema.path("properties").path("assets").path("maxItems").intValue());
            Assert.assertEquals(16, schema.path("properties").path("runtimes").path("maxItems").intValue());
            Assert.assertEquals(64, schema.path("$defs").path("runtime").path("properties").path("assets").path("maxItems").intValue());
            Assert.assertEquals(240, schema.path("$defs").path("asset").path("properties").path("path").path("maxLength").intValue());
            Assert.assertEquals(100, schema.path("$defs").path("id").path("maxLength").intValue());
            JsonNode blocked = schema.path("$defs").path("id").path("not").path("enum");
            Assert.assertEquals(3, blocked.size());
            Assert.assertEquals("constructor", blocked.get(0).textValue());
            Assert.assertEquals("prototype", blocked.get(1).textValue());
            Assert.assertEquals("__proto__", blocked.get(2).textValue());
        }
    }
}
