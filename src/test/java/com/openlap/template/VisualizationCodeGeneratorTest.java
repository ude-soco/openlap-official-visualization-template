package com.openlap.template;

import com.openlap.dataset.OpenLAPColumnConfigData;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumn;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.dataset.OpenLAPPortMapping;
import com.openlap.exceptions.DataSetValidationException;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.UnTransformableData;
import com.openlap.exceptions.VisualizationCodeGenerationException;
import com.openlap.template.model.ChartConfiguration;
import com.openlap.template.model.TransformedData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VisualizationCodeGeneratorTest {

    @Before
    public void resetRecordingTransformer() {
        RecordingDataTransformer.wasInvoked = false;
        RecordingDataTransformer.lastInput = null;
    }

    @Test
    public void generateVisualizationCodeInvokesTransformerAndReturnsGeneratedCode() throws Exception {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();
        OpenLAPDataSet source = sourceDataSet();
        Map<String, Object> additionalParams = new HashMap<String, Object>();
        additionalParams.put("suffix", "done");

        String generatedCode =
                generator.generateVisualizationCode(source, validPortConfig(), additionalParams);

        Assert.assertTrue(RecordingDataTransformer.wasInvoked);
        Assert.assertNotNull(RecordingDataTransformer.lastInput);
        Assert.assertSame(generator.visualizationTransformer, generator.lastTransformer);
        Assert.assertSame(additionalParams, generator.lastAdditionalParams);
        Assert.assertEquals("generated:Alpha:42.5:done", generatedCode);
    }

    @Test
    public void getInputAsJsonStringSerializesInitializedInput() {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();

        String json = generator.getInputAsJsonString();

        Assert.assertTrue(json.contains("inputText"));
        Assert.assertTrue(json.contains("inputNumber"));
    }

    @Test
    public void getOutputAsJsonStringSerializesInitializedOutput() {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();
        generator.initializeDataSetConfiguration();

        String json = generator.getOutputAsJsonString();

        Assert.assertTrue(json.contains("outputText"));
    }

    @Test
    public void jsonHelpersSerializeNullConfigurationsAsNullString() {
        NullConfigurationGenerator generator = new NullConfigurationGenerator();

        Assert.assertEquals("null", generator.getInputAsJsonString());
        Assert.assertEquals("null", generator.getOutputAsJsonString());
    }

    @Test
    public void jsonHelpersReturnEmptyStringWhenSerializationFails() {
        BrokenConfigurationGenerator generator = new BrokenConfigurationGenerator();

        Assert.assertEquals("", generator.getInputAsJsonString());
        Assert.assertEquals("", generator.getOutputAsJsonString());
    }

    @Test
    public void isDataProcessableAcceptsValidConfiguration() {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();

        Assert.assertTrue(generator.isDataProcessable(validPortConfig()));
    }

    @Test
    public void isDataProcessableRejectsMissingRequiredConfiguration() {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();

        DataSetValidationException exception =
                Assert.assertThrows(
                        DataSetValidationException.class,
                        () -> generator.isDataProcessable(new OpenLAPPortConfig()));

        Assert.assertTrue(exception.getMessage().contains("Required columns not found"));
        Assert.assertTrue(exception.getMessage().contains("inputText"));
        Assert.assertTrue(exception.getMessage().contains("inputNumber"));
    }

    @Test
    public void isDataProcessableRejectsWrongTypeMapping() {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();

        DataSetValidationException exception =
                Assert.assertThrows(
                        DataSetValidationException.class,
                        () -> generator.isDataProcessable(wrongTypePortConfig()));

        Assert.assertTrue(exception.getMessage().contains("inputText"));
        Assert.assertTrue(exception.getMessage().contains("expected"));
    }

    @Test
    public void isDataProcessableRejectsNullConfigurationWithNullPointerException() {
        TestVisualizationCodeGenerator generator = new TestVisualizationCodeGenerator();

        Assert.assertThrows(NullPointerException.class, () -> generator.isDataProcessable(null));
    }

    private static OpenLAPDataSet sourceDataSet() throws OpenLAPDataColumnException {
        OpenLAPDataSet dataSet = new OpenLAPDataSet();
        dataSet.addOpenLAPDataColumn(column("sourceText", OpenLAPColumnDataType.Text, true));
        dataSet.addOpenLAPDataColumn(column("sourceNumber", OpenLAPColumnDataType.Numeric, true));
        dataSet.getColumns().get("sourceText").getData().add("Alpha");
        dataSet.getColumns().get("sourceNumber").getData().add(42.5f);
        return dataSet;
    }

    private static OpenLAPPortConfig validPortConfig() {
        OpenLAPPortConfig config = new OpenLAPPortConfig();
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("sourceText", OpenLAPColumnDataType.Text, true),
                        configData("inputText", OpenLAPColumnDataType.Text, true)));
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("sourceNumber", OpenLAPColumnDataType.Numeric, true),
                        configData("inputNumber", OpenLAPColumnDataType.Numeric, true)));
        return config;
    }

    private static OpenLAPPortConfig wrongTypePortConfig() {
        OpenLAPPortConfig config = new OpenLAPPortConfig();
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("sourceNumber", OpenLAPColumnDataType.Numeric, true),
                        configData("inputText", OpenLAPColumnDataType.Text, true)));
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("sourceText", OpenLAPColumnDataType.Text, true),
                        configData("inputNumber", OpenLAPColumnDataType.Numeric, true)));
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

    public static class TestVisualizationCodeGenerator extends VisualizationCodeGenerator {
        private DataTransformer lastTransformer;
        private DataTransformer visualizationTransformer;
        private Map<String, Object> lastAdditionalParams;

        @Override
        public String getName() {
            return "Test Visualization";
        }

        @Override
        public ChartConfiguration getConfiguration() {
            return null;
        }

        @Override
        public void initializeDataSetConfiguration() {
            try {
                OpenLAPDataSet input = new OpenLAPDataSet();
                input.addOpenLAPDataColumn(column("inputText", OpenLAPColumnDataType.Text, true));
                input.addOpenLAPDataColumn(column("inputNumber", OpenLAPColumnDataType.Numeric, true));
                setInput(input);

                OpenLAPDataSet output = new OpenLAPDataSet();
                output.addOpenLAPDataColumn(column("outputText", OpenLAPColumnDataType.Text, false));
                setOutput(output);
            } catch (OpenLAPDataColumnException exception) {
                throw new IllegalStateException(exception);
            }
        }

        @Override
        public Class getDataTransformer() {
            return RecordingDataTransformer.class;
        }

        @Override
        public String visualizationLibraryScript() {
            return "<script></script>";
        }

        @Override
        public String visualizationCode(DataTransformer dataTransformer, Map<String, Object> additionalParams)
                throws VisualizationCodeGenerationException {
            lastTransformer = dataTransformer;
            lastAdditionalParams = additionalParams;
            RecordingDataTransformer recordingTransformer = (RecordingDataTransformer) dataTransformer;
            visualizationTransformer = recordingTransformer;
            return "generated:"
                    + recordingTransformer.getTransformedData().getData()
                    + ":"
                    + additionalParams.get("suffix");
        }
    }

    public static class RecordingDataTransformer implements DataTransformer {
        private static boolean wasInvoked;
        private static OpenLAPDataSet lastInput;

        private final TransformedData<String> transformedData = new TransformedData<String>();

        @Override
        public TransformedData<?> transformData(OpenLAPDataSet openLAPDataSet) throws UnTransformableData {
            wasInvoked = true;
            lastInput = openLAPDataSet;
            Object textValue = openLAPDataSet.getColumns().get("inputText").getData().get(0);
            Object numericValue = openLAPDataSet.getColumns().get("inputNumber").getData().get(0);
            transformedData.setData(textValue + ":" + numericValue);
            return transformedData;
        }

        public TransformedData<String> getTransformedData() {
            return transformedData;
        }
    }

    public static class NullConfigurationGenerator extends TestVisualizationCodeGenerator {
        @Override
        public void initializeDataSetConfiguration() {
            setInput(null);
            setOutput(null);
        }
    }

    public static class BrokenConfigurationGenerator extends TestVisualizationCodeGenerator {
        @Override
        public void initializeDataSetConfiguration() {
            setInput(new BrokenDataSet());
            setOutput(new BrokenDataSet());
        }
    }

    public static class BrokenDataSet extends OpenLAPDataSet {
        @Override
        public HashMap<String, OpenLAPDataColumn> getColumns() {
            throw new IllegalStateException("Cannot serialize this dataset");
        }
    }
}
