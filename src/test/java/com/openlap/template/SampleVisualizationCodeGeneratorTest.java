package com.openlap.template;

import com.openlap.dataset.OpenLAPColumnConfigData;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumn;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.dataset.OpenLAPPortMapping;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.UnTransformableData;
import com.openlap.exceptions.VisualizationCodeGenerationException;
import com.openlap.template.model.ChartConfiguration;
import com.openlap.template.model.TransformedData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SampleVisualizationCodeGeneratorTest {

    @Test
    public void sampleVisualizationProvidesRequiredTemplateMethods() {
        SampleVisualization visualization = new SampleVisualization();

        Assert.assertEquals("Sample Bar Chart", visualization.getName());
        Assert.assertNotNull(visualization.getConfiguration());
        Assert.assertEquals(SampleDataTransformer.class, visualization.getDataTransformer());
        Assert.assertTrue(visualization.getVisualizationLibraryScript().contains("sample-library.js"));
    }

    @Test
    public void sampleVisualizationGeneratesExpectedSnippet() throws Exception {
        SampleVisualization visualization = new SampleVisualization();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("title", "Course Activity");

        String generated =
                visualization.generateVisualizationCode(sourceDataSet("Alpha", 42.5f), validPortConfig(), params);

        Assert.assertEquals(
                "<div id=\"sample-chart\"></div>"
                        + "<script>"
                        + "var title='Course Activity';"
                        + "var labels=['Alpha'];"
                        + "var values=[42.5];"
                        + "</script>",
                generated);
    }

    @Test
    public void sampleVisualizationIncludesTransformedData() throws Exception {
        SampleVisualization visualization = new SampleVisualization();

        String generated =
                visualization.generateVisualizationCode(sourceDataSet("Beta", 7.0f), validPortConfig(), emptyParams());

        Assert.assertTrue(generated.contains("var labels=['Beta'];"));
        Assert.assertTrue(generated.contains("var values=[7.0];"));
    }

    @Test
    public void sampleVisualizationEscapesDangerousLabelsAndParameters() throws Exception {
        SampleVisualization visualization = new SampleVisualization();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("title", "Title 'quoted' \u2028 next");

        String generated =
                visualization.generateVisualizationCode(
                        sourceDataSet("</script><script>alert(1)</script>", 13.0f),
                        validPortConfig(),
                        params);

        Assert.assertFalse(generated.contains("</script><script>"));
        Assert.assertTrue(generated.contains("\\u003C/script\\u003E\\u003Cscript\\u003Ealert(1)\\u003C/script\\u003E"));
        Assert.assertTrue(generated.contains("var title='Title \\'quoted\\' \\u2028 next';"));
    }

    private static Map<String, Object> emptyParams() {
        return new HashMap<String, Object>();
    }

    private static OpenLAPDataSet sourceDataSet(String label, Float value) throws OpenLAPDataColumnException {
        OpenLAPDataSet dataSet = new OpenLAPDataSet();
        dataSet.addOpenLAPDataColumn(column("sourceLabel", OpenLAPColumnDataType.Text, true));
        dataSet.addOpenLAPDataColumn(column("sourceValue", OpenLAPColumnDataType.Numeric, true));
        dataSet.getColumns().get("sourceLabel").getData().add(label);
        dataSet.getColumns().get("sourceValue").getData().add(value);
        return dataSet;
    }

    private static OpenLAPPortConfig validPortConfig() {
        OpenLAPPortConfig config = new OpenLAPPortConfig();
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("sourceLabel", OpenLAPColumnDataType.Text, true),
                        configData("label", OpenLAPColumnDataType.Text, true)));
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("sourceValue", OpenLAPColumnDataType.Numeric, true),
                        configData("value", OpenLAPColumnDataType.Numeric, true)));
        return config;
    }

    private static OpenLAPDataColumn column(String id, OpenLAPColumnDataType type, boolean required)
            throws OpenLAPDataColumnException {
        return OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(id, type, required, null, null);
    }

    private static OpenLAPColumnConfigData configData(
            String id, OpenLAPColumnDataType type, boolean required) {
        return new OpenLAPColumnConfigData(id, type, required, null, null);
    }

    public static class SampleVisualization extends VisualizationCodeGenerator {
        @Override
        public String getName() {
            return "Sample Bar Chart";
        }

        @Override
        public ChartConfiguration getConfiguration() {
            return new ChartConfiguration(
                    false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false);
        }

        @Override
        public void initializeDataSetConfiguration() {
            try {
                OpenLAPDataSet input = new OpenLAPDataSet();
                input.addOpenLAPDataColumn(column("label", OpenLAPColumnDataType.Text, true));
                input.addOpenLAPDataColumn(column("value", OpenLAPColumnDataType.Numeric, true));
                setInput(input);
                setOutput(new OpenLAPDataSet());
            } catch (OpenLAPDataColumnException exception) {
                throw new IllegalStateException("Could not initialize sample visualization columns", exception);
            }
        }

        @Override
        public Class getDataTransformer() {
            return SampleDataTransformer.class;
        }

        @Override
        public String visualizationLibraryScript() {
            return "<script src=\"https://example.test/sample-library.js\"></script>";
        }

        @Override
        public String visualizationCode(DataTransformer dataTransformer, Map<String, Object> additionalParams)
                throws VisualizationCodeGenerationException {
            SampleDataTransformer transformer = (SampleDataTransformer) dataTransformer;
            List<SamplePoint> points = transformer.getTransformedData().getData();
            String title = additionalParams.containsKey("title") ? additionalParams.get("title").toString() : "";

            StringBuilder labels = new StringBuilder();
            StringBuilder values = new StringBuilder();
            for (int index = 0; index < points.size(); index++) {
                SamplePoint point = points.get(index);
                labels.append("'")
                        .append(VisualizationEscaper.escapeJavaScriptString(point.getLabel()))
                        .append("'");
                values.append(point.getValue());
                if (index < points.size() - 1) {
                    labels.append(",");
                    values.append(",");
                }
            }

            return "<div id=\"sample-chart\"></div>"
                    + "<script>"
                    + "var title='"
                    + VisualizationEscaper.escapeJavaScriptString(title)
                    + "';"
                    + "var labels=["
                    + labels
                    + "];"
                    + "var values=["
                    + values
                    + "];"
                    + "</script>";
        }
    }

    public static class SampleDataTransformer implements DataTransformer {
        private final TransformedData<List<SamplePoint>> transformedData =
                new TransformedData<List<SamplePoint>>();

        @Override
        public TransformedData<?> transformData(OpenLAPDataSet openLAPDataSet) throws UnTransformableData {
            List<SamplePoint> points = new ArrayList<SamplePoint>();
            List<Object> labels = openLAPDataSet.getColumns().get("label").getData();
            List<Object> values = openLAPDataSet.getColumns().get("value").getData();
            for (int index = 0; index < labels.size(); index++) {
                points.add(new SamplePoint(String.valueOf(labels.get(index)), values.get(index)));
            }
            transformedData.setData(points);
            return transformedData;
        }

        public TransformedData<List<SamplePoint>> getTransformedData() {
            return transformedData;
        }
    }

    public static class SamplePoint {
        private final String label;
        private final Object value;

        public SamplePoint(String label, Object value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public Object getValue() {
            return value;
        }
    }
}
