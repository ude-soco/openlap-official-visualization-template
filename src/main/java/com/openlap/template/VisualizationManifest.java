package com.openlap.template;

/**
 * Resource names for the optional browser-runtime contract. Existing generator
 * subclasses do not need to implement a new Java method to use this contract.
 */
public final class VisualizationManifest {
    public static final int SCHEMA_VERSION = 1;
    public static final String RESOURCE_PATH = "META-INF/openlap/visualization.json";
    public static final String SCHEMA_RESOURCE_PATH =
            "com/openlap/template/visualization-manifest-v1.schema.json";

    private VisualizationManifest() {}
}
