package org.openlap.examples.svg;

import com.openlap.template.VisualizationLibraryInfo;

public class SvgLibrary extends VisualizationLibraryInfo {
    @Override public String getName() { return "OpenLAP bundled SVG examples"; }
    @Override public String getDescription() {
        return "Two self-contained charts sharing a bundled browser runtime and reusable customization metadata.";
    }
    @Override public String getDeveloperName() { return "OpenLAP contributors"; }
}
