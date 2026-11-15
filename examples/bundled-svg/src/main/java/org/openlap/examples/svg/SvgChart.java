package org.openlap.examples.svg;

import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.VisualizationCodeGenerationException;
import com.openlap.template.DataTransformer;
import com.openlap.template.VisualizationCodeGenerator;
import com.openlap.template.model.ChartConfiguration;

import java.util.Map;
import java.util.UUID;

public abstract class SvgChart extends VisualizationCodeGenerator {
    protected abstract String chartKind();

    @Override public ChartConfiguration getConfiguration() {
        // New controls are described by the manifest; legacy capabilities stay ABI-compatible.
        return new ChartConfiguration(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false, false);
    }

    @Override public void initializeDataSetConfiguration() {
        OpenLAPDataSet input = new OpenLAPDataSet();
        try {
            input.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(
                    "category", OpenLAPColumnDataType.Text, true, "Categories", "Labels for the horizontal axis."));
            input.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(
                    "value", OpenLAPColumnDataType.Numeric, true, "Values", "One finite numerical value per category."));
        } catch (OpenLAPDataColumnException error) {
            throw new IllegalStateException("Could not initialize SVG chart ports.", error);
        }
        setInput(input);
        setOutput(new OpenLAPDataSet());
    }

    @Override public Class getDataTransformer() { return SvgDataTransformer.class; }

    @Override public String visualizationLibraryScript() {
        // Dependencies are declared in META-INF/openlap/visualization.json.
        return "";
    }

    @Override public String visualizationCode(DataTransformer transformer, Map<String, Object> options) {
        String id = "openlap-svg-" + UUID.randomUUID().toString();
        StringBuilder points = new StringBuilder("[");
        for (SvgDataTransformer.Point point : ((SvgDataTransformer) transformer).getPoints()) {
            if (points.length() > 1) points.append(',');
            points.append("{\"label\":").append(json(point.label))
                    .append(",\"value\":").append(Double.toString(point.value)).append('}');
        }
        points.append(']');
        String color = text(options, "seriesColor", "#1976d2");
        if (!color.matches("#[A-Fa-f0-9]{6}")) throw new VisualizationCodeGenerationException("Choose a six-digit hexadecimal color.");
        double height = number(options, "height", 420);
        if (height < 260 || height > 800) throw new VisualizationCodeGenerationException("Chart height must be between 260 and 800 pixels.");
        String labelPosition = text(options, "valueLabelPosition", "above");
        if (!"above".equals(labelPosition) && !"below".equals(labelPosition)) {
            throw new VisualizationCodeGenerationException("Choose an advertised value-label position.");
        }
        StringBuilder parameters = new StringBuilder("{")
                .append("\"chartTitle\":").append(json(text(options, "chartTitle", "")))
                .append(",\"seriesColor\":").append(json(color))
                .append(",\"height\":").append(height)
                .append(",\"showValues\":").append(bool(options, "showValues", true))
                .append(",\"showGrid\":").append(bool(options, "showGrid", true))
                .append(",\"valueLabelPosition\":").append(json(labelPosition))
                .append('}');
        return "<div class=\"openlap-svg-chart\" id=\"" + id + "\"></div><script>"
                + "window.OpenLAPExample.render(document.getElementById(" + json(id) + "),"
                + points + "," + parameters + "," + json(chartKind()) + ");</script>";
    }

    private static String text(Map<String, Object> options, String key, String fallback) {
        Object value = options == null ? null : options.get(key);
        if (value == null) return fallback;
        if (!(value instanceof String)) throw new VisualizationCodeGenerationException(key + " must be text.");
        return (String) value;
    }

    private static boolean bool(Map<String, Object> options, String key, boolean fallback) {
        Object value = options == null ? null : options.get(key);
        if (value == null) return fallback;
        if (!(value instanceof Boolean)) throw new VisualizationCodeGenerationException(key + " must be true or false.");
        return (Boolean) value;
    }

    private static double number(Map<String, Object> options, String key, double fallback) {
        Object value = options == null ? null : options.get(key);
        if (value == null) return fallback;
        if (!(value instanceof Number)) throw new VisualizationCodeGenerationException(key + " must be a number.");
        double number = ((Number) value).doubleValue();
        if (Double.isNaN(number) || Double.isInfinite(number)) throw new VisualizationCodeGenerationException(key + " must be finite.");
        return number;
    }

    // JSON embedded in an inline script must escape HTML delimiters too.
    static String json(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '"' || character == '\\') escaped.append('\\').append(character);
            else if (character < 32 || character == '<' || character == '>' || character == '&'
                    || character == '\u2028' || character == '\u2029') {
                escaped.append(String.format("\\u%04x", (int) character));
            } else escaped.append(character);
        }
        return escaped.append('"').toString();
    }
}
