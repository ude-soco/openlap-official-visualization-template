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
        }
    }
}
