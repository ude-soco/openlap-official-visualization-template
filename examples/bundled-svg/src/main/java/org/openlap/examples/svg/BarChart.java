package org.openlap.examples.svg;

public class BarChart extends SvgChart {
    @Override public String getName() { return "Bundled SVG Bar Chart"; }
    @Override protected String chartKind() { return "bar"; }
}
