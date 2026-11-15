package org.openlap.examples.svg;

public class LineChart extends SvgChart {
    @Override public String getName() { return "Bundled SVG Line Chart"; }
    @Override protected String chartKind() { return "line"; }
}
