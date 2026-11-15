# OpenLAP Visualization Template

Build visualization plugins that OpenLAP can install as JAR files. The template
provides typed chart inputs, data transformation and HTML/JavaScript generation.
The optional version 1 manifest adds bundled browser dependencies and reusable
customization controls without changing existing generator methods.

## Build and try the bundled example

Use a JDK supported by your OpenLAP installation (JDK 11 or newer can build this
project; the SDK and example retain Java 8 bytecode compatibility).

```sh
./mvnw clean package
./mvnw -f examples/bundled-svg/pom.xml clean package
```

Upload `examples/bundled-svg/target/openlap-bundled-svg-example-1.0.0.jar` through
OpenLAP's visualization-library administration flow. Review and activate it on a
version of OpenLAP that supports the visualization manifest. It provides Bar and
Line charts with one shared SVG runtime, a packaged icon and six customization
controls. It needs no browser CDN, frontend source changes or scripts on the
embedding platform. See the [example guide](examples/bundled-svg/README.md).

The example compiles against the published template **1.1.0** and dataset
**1.0.0**, matching the existing backend plugin ABI. Both are `provided`
dependencies: they must not be copied or shaded into the uploaded JAR. The SDK
checkout itself uses the newer dataset dependency declared in its own POM; a
plugin's provided dependencies should match the target OpenLAP installation.

## Browser runtime and customization contract

New self-contained plugins include this resource:

```text
META-INF/openlap/visualization.json
web/…                         JavaScript, CSS and optional PNG/SVG icons
```

The SDK packages a [JSON Schema](src/main/resources/com/openlap/template/visualization-manifest-v1.schema.json)
for editor assistance. `VisualizationManifest` exposes the resource path and
schema version without adding methods to existing abstract classes.

| Manifest field | Purpose |
| --- | --- |
| `schemaVersion`, `pluginId`, `pluginVersion` | Identify the contract and plugin build. |
| `assets` | Declare each resource's ID, JAR path, kind and SHA-256 checksum. |
| `runtimes` | Select an exact runtime version and its ordered asset IDs. |
| `charts` | Connect each chart ID and Java implementation to its runtime, input roles, description and icon. |
| `customizations` | Describe controls, defaults, validation, groups and optional visibility conditions. |

A library's charts normally share one runtime. Declare additional runtimes only
when the charts need different browser dependencies. Runtime versions are chosen
and packaged during development; selecting a chart loads the declared assets,
not a floating latest release. Dependencies and their license notices belong in
the plugin distribution. Put redistribution notices under `META-INF/licenses/`
so they remain in the distributed JAR; the manifest lists browser assets rather
than adding a separate license field. The example runtime is original project code and does
not redistribute a third-party chart library.

Customization fields support `text`, `number`, `boolean`, `select` and `color`.
Each has a stable `id`, a `label` and an `elements`, `styles` or `filters` group.
Use `defaultValue`, select `options`, numeric `min`/`max`/`step`, and `help` as
needed. `visibleWhen: {"id": "showValues", "equals": true}` conditionally displays
a dependent control. OpenLAP presents these with its native controls; the selected
values reach the generator through its existing `additionalParams` map. Validate
values in the generator too, and preserve parameter IDs across compatible plugin
versions.

Every chart's `implementingClass` must match a discovered concrete generator.
Asset IDs, runtime IDs, chart IDs and customization IDs must be unique in their
scope. Runtime and icon references must resolve to declared assets. Paths must
stay below `web/` with no traversal, URLs or backslashes. Scripts use `.js`, styles
use `.css`, and icons use `.png` or `.svg`. Every declared byte sequence must match
its checksum. OpenLAP performs semantic and bounded archive validation in addition
to the schema's structural checks; schema validation alone does not establish
trust in plugin code.

Version 1 accepts at most 64 assets, 16 runtimes, 64 asset references per runtime
and 64 chart types. Identifiers are limited to 100 characters and asset paths to
240 characters. Each chart must include a `customizations` array, which may be
empty. The backend also limits the manifest to 128 KiB, each browser asset to
8 MiB and the combined declared assets to 32 MiB. These limits are checked during
upload before the plugin can be activated.

The backend serves only declared assets from the approved immutable plugin
artifact. Browser asset URLs are provided by OpenLAP. Plugins never need storage
credentials or hardcoded deployment URLs. Rendering loads dependencies before
executing chart code; external platforms only paste the generated iframe.

`visualizationLibraryScript()` remains part of the legacy Java API. For a
manifest-based plugin, return an empty string as in the example. Keep dependency
loading separate from generated chart fragments. Older OpenLAP versions do not
understand the manifest and still require their legacy dependency setup.

## Legacy generator guide

The remaining tutorial describes the existing Java generator API. Its historical
CDN examples are for legacy integrations; use the bundled example above for new
self-contained plugins.

## INTRODUCTION

The following video gives the introduction to the OpenLAP followed by the tutorial to add new visualization technique to
the OpenLAP.

## Stabilization Notes

The examples in this README target Java 8 and use explicit Java types with small domain classes.

Generated HTML and JavaScript must escape user-controlled labels, values, and additional parameters. See
the Safe HTML/JS generation section before concatenating transformed data into generated snippets.

<p align="center">
	<a href="https://www.youtube.com/watch?feature=player_embedded&v=e6dwtO0mneo" target="_blank">
		<span><strong>Video Tutorial to add new Visualization techniques to the OpenLAP</strong></span>
		<br>
		<img src="http://lanzarote.informatik.rwth-aachen.de/openlap/img/others/visualizer.png" width=480 alt="OpenLAP Introduction and New Visualizer"/>
	</a>
</p>

## Fundamental Concepts

The main idea behind the Visualizer is to receive the incoming analyzed data in the OpenLAP-DataSet format and convert
it to the format consisting of HTML and JavaScript code which can be visualized on the client side. To clarify the
concept, take a look at the example below which uses Google Charts API to generate a bar chart.

```javascript
<!-- Visualization Library Script -->
<script src="https://cdn.jsdelivr.net/npm/apexcharts@3.45.1/dist/apexcharts.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/apexcharts@3.45.1/dist/apexcharts.min.css" rel="stylesheet">

  <!-- Visualization Generation Script -->
  <div id='chartdiv'></div>
  <script>
    const options = {
    // Visualization data
    series: [{
    data: [400, 430, 448, 470, 540, 580, 690, 1100, 1200, 1380]
  }],

    //Visualization options
    chart: {
    type: 'bar',
    height: 350
  },
    plotOptions: {
    bar: {
    borderRadius: 4,
    horizontal: true,
  }
  },
    dataLabels: {
    enabled: false
  },
    xaxis: {
    categories: ['South Korea', 'Canada', 'United Kingdom', 'Netherlands', 'Italy', 'France', 'Japan',
    'United States', 'China', 'Germany'
    ],
  }
  };

    //Visualization generation
    const chart = new ApexCharts(document.querySelector("#chart"), options);
    chart.render();
  </script>
```

The code is divided into two main sections. Visualization Library Script section is responsible for incorporating the
visualization library scripts on the webpage. Visualization Generation Script section contains the code to generate the
chart. The Visualization Generation Script section is further segmented into three sub-sections:

* Visualization Data: This part consists of the data needed to generate the graph. In real life application, this
  section is dynamically generated based on the input data received as input in OpenLAPDataSet format. It is not hard
  coded as in this example

* Visualization Options: It is possible to customize the chart with the options.

* Visualization Generation: This segment contains the scripts that utilize the data and options to generate the chart.

Before going further into the details, here is a list of terminologies which will be helpful to understand this guide
and how they are interacting with each other:

* <strong>VisualizationFramework</strong>: A web visualization library or framework which can be utilized to create
  interactive visualizations. E.g. D3.js, Google Charts, dygraphs etc.

* <strong>DataTransformer</strong>: A concrete implementation which transforms data received from the client in the form
  of the `OpenLAPDataSet` into a data structure understandable by the VisualizationMethod that uses it.

* <strong>VisualizationMethod</strong>: A concrete interactive visualization type. E.g. bar chart, pie chart etc.

To implement a new visualization technique, the developer must extend the abstract class `VisualizationCodeGenerator`
and implement an interface `DataTransformer` available in the OpenLAP-Visualizer-Framework project.

### Methods of the `VisualizationCodeGenerator` abstract class

The `VisualizationCodeGenerator` abstract class has a series of methods that allows new classes that extend it to be
used by the OpenLAP.

#### Implemented Methods

* The ` isDataProcessable()` method takes an `OpenLAPPortConfig` as parameter and validate if the `OpenLAPDataSet`
  with the specified configuration can be processed or not.

* The `generateVisualizationCode()` method takes an `OpenLAPDataSet`, an `OpenLAPPortConfig` and a `DataTransformer`
  as parameters. The incoming `OpenLAPDataSet` is used as an input data which is transformed using the `DataTransformer`
  if the `OpenLAPPortConfig` is valid. The transformed data is then processed using the `visualizationCode()` method
  to produce the “Visualization Generation Script” (as discussed in the Fundamental Concepts section).

* The `getVisualizationLibraryScript()` method returns the “Visualization Library Script” (as discussed in the
  Fundamental Concepts section) by calling the `visualizationLibraryScript()` method.

* The `getInput()` method allow other classes to obtain the input `OpenLAPDataSet` which can be used to get the columns
  metadata as `OpenLAPColumnConfigData` class.

#### Abstract Methods

* The `initializeDataSetConfiguration()` method is where the developer will define the column configuration of the input
  `OpenLAPDataSet`.

* The `visualizationCode()` is the core method where the developer will implement the logic to convert the data
  transformed using the `DataTransformer` in the “Visualization Generation Script” which is returned as a string. This
  method is called by the `generateVisualizationCode()` method described above.

* The `visualizationLibraryScript()` method is where the developer will provide the “Visualization Library Script”
  section of the implementing visualization technique.

* getName() method returns the name of the visualization method

* getDataTransformer() method returns the DataTransformer used in the visualization method

### Methods of the `DataTransformer` interface class

The `DataTransformer` interface class provides a single method `transformData()` to be implemented which accept the
`OpenLAPDataSet` as input parameter. The developers will implement the logic here to transform the incoming
`OpenLAPDataSet` to any data structure which will be used by the `visualizationCode()` method of the
‘VisualizationCodeGenerator’ abstract class to generate the “Visualization Generation Script”. The return of this method
is a class `TransformedData<T>` which contains a single object `dataContent` of type `<T>` to store the transformed
data.

### Important Note

The concept of the abstract `VisualizationCodeGenerator` class and the `DataTransformer` interface should be clear from
the above description. In the `VisualizationCodeGenerator` class the developer defines the input `OpenLAPDataSet` column
configuration, the core logic to generate the “Visualization Generation Script”, and provide “Visualization Library
Script”. Whereas, in the `DataTransformer` the developer implement the conversion of input `OpenLAPDataSet` to suitable
data structure which can be used to generate “Visualization Generation Script” in the `VisualizationCodeGenerator`
class. So the important point here is that the input `OpenLAPDataSet` is defined in the `VisualizationCodeGenerator`
class but it is not used there. It is used in the `DataTransformer` and the transformed data from this interface class
is then used in the `VisualizationCodeGenerator`.

## Safe HTML/JS generation

Visualization methods return raw HTML and JavaScript snippets, so treat dataset labels, transformed values, and
additional parameters as untrusted whenever they are concatenated into generated code.

Use `VisualizationEscaper.escapeHtmlText(value)` when writing user-controlled text between HTML tags.
Use `VisualizationEscaper.escapeHtmlAttribute(value)` when writing user-controlled text inside an HTML attribute.
Use `VisualizationEscaper.escapeJavaScriptString(value)` when writing user-controlled text inside a JavaScript string
literal.

The helpers return an empty string for `null`. They do not validate JavaScript numbers, booleans, identifiers, URLs, or
whole expressions; parse or allow-list those values before concatenating them into generated code.

## Step by step guide to implement a new Visualization Method

The following steps must be followed by the developer to implement a new Analytics Method for the OpenLAP:

1. Setting up the development environment.

2. Creating project and importing the dependencies into it.

3. Create a class that extends the VisualizationCodeGenerator and VisualizationLibraryInfo classes.

4. Define the input `OpenLAPDataSet`.

5. Create a class that implements the `DataTransformer` interface.

6. Implement the method of the `DataTransformer` interface.

7. Implement the remaining abstract methods of the `VisualizationCodeGenerator` class.

8. Package the plugin JAR with OpenLAP dependencies marked `provided`.

9. Upload the JAR file using the OpenLAP administration panel.

These steps are explained in more details with concrete examples in the following sections.

### Step 1. Setting up the development environment

To develop a new analytics method, you need to install the following softwares.

* A supported Java Development Kit. This SDK emits Java 8 bytecode; use JDK 11 or newer for the current Maven build. Match your target OpenLAP installation when choosing provided plugin dependencies.
* Any Integrated Development Environment (IDE) for Java development, such
  as, [Intellij IDEA](https://www.jetbrains.com/idea/download), [NetBeans](https://netbeans.org/downloads/), [Eclipse](https://eclipse.org/downloads/),
  etc.

In the following steps we are going to use the Intellij IDEA for developing the sample analytics method using maven.

### Step 2. Creating project and importing the dependencies into it.

* Create a new project. `File -> New -> Project`
* Select `Maven` from the left and click `Next`.
* Enter a GroupId, ArtifactId and version for your plugin. Use unique Java package names for its implementation classes; OpenLAP discovers chart classes by the template contract rather than requiring a particular Maven GroupId.

* Add the JitPack repository and provided dependencies for the OpenLAP installation you target. The example below matches the published ABI used by the existing backend. Refer to the [visualization template](https://github.com/ude-soco/openlap-official-visualization-template) and [dataset formats](https://github.com/ude-soco/openlap-official-data-exchange-formats) repositories when qualifying another target version.

Maven:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.ude-soco</groupId>
    <artifactId>openlap-official-visualization-template</artifactId>
    <version>1.1.0</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.github.ude-soco</groupId>
    <artifactId>openlap-official-data-exchange-formats</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
</dependencies>
```

### Step 3. Create a classes that extends the VisualizationCodeGenerator and VisualizationLibraryInfo classes

If the class 'Main' was automatically generated, it can be safely deleted. Within the 'openlap.visualizer' package,
create two additional packages: 'Charts' and 'VisualizationInformation'. In the 'Charts' package, develop classes that
extend VisualizationCodeGenerator, each representing a specific chart type (e.g., Line Chart, Pie Chart). In the '
VisualizationInformation' package, create a class that extends VisualizationLibraryInfo. Only one such class should
exist, symbolizing the library responsible for chart creation (e.g., ApexCharts library).

```java
package openlap.visualizer.Charts;

import com.openlap.template.VisualizationCodeGenerator;

public abstract class BarChart extends VisualizationCodeGenerator {

}
```

```java
package openlap.visualizer.VisualizationInformation;

import com.openlap.template.VisualizationLibraryInfo;

public class VisualizationInformation extends VisualizationLibraryInfo {
	public VisualizationInformation() {
	}

	public String getName() {
		return "apexcharts.js";
	}

	public String getDescription() {
		return "ApexCharts is a modern charting library that helps developers to " +
				"create beautiful and interactive visualizations for web pages";
	}

	public String getDeveloperName() {
		return "Developer Name";
	}
}
```

### Step 4. Define the input `OpenLAPDataSet`.

The input OpenLAPDataSet should be defined in the *initializeDataSetConfiguration()* method of the class BarChart as
shown in the example below. Generally speaking, the input types defined here should correspond to the columns data types
defined as outputs in the constructor of the chosen analytics method. The current example implies that the chosen
analytics method has an output OpenLAPDataSet with two columns that have one column of type *text* and one column of
type *numeric*. Therefore, we have here an input OpenLAPDataSet with two columns that have one column of type *text* and
one column of type *numeric*.

```java
// Declaration of input OpenLAPDataSet by adding OpenLAPDataColumn objects with the OpenLAPDataColumnFactory
@Override
public void initializeDataSetConfiguration() {
	this.setInput(new OpenLAPDataSet());
	this.setOutput(new OpenLAPDataSet());

	try {
		this.getInput().addOpenLAPDataColumn(
				OpenLAPDataColumnFactory
						.createOpenLAPDataColumnOfType(
								"xAxisStrings",
								OpenLAPColumnDataType.Text,
								true,
								"X-Axis",
								"List of items to be displayed on X-Axis of the graph"));

		this.getInput().addOpenLAPDataColumn(
				OpenLAPDataColumnFactory
						.createOpenLAPDataColumnOfType(
								"yAxisValues",
								OpenLAPColumnDataType.Numeric,
								true,
								"Y-Axis",
								"List of items to be displayed on Y-Axis of the graph"));
	} catch (OpenLAPDataColumnException exception) {
		throw new IllegalStateException("Could not initialize BarChart input columns", exception);
	}
}
```

### Step 5. Create a class that implements the `DataTransformer` interface.

Generate a class that implements DataTransformer interface by following the provided example. It is important to note
that this class should not be placed within the *openlap.visualizer.Charts* or
*openlap.visualizer.VisualizationInformation* packages, but should be placed elsewhere.

```java
package openlap.visualizer;

import com.openlap.template.DataTransformer;

public class BarChartDataTransformer implements DataTransformer {
}
```

### Step 6. Implement the method of the `DataTransformer` interface.

The DataTransformer interface necessitates the implementation of only one method. The provided example offers a
demonstration of a data transformer. Specifically, this data transformer takes the input OpenLAPDataSet defined in the
*initializeDataSetConfiguration* method of the class *BarChart* and converts it into a list of chart points. The result
is returned as an object of the TransformedData class.

```java
package openlap.visualizer;

import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumn;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.exceptions.UnTransformableData;
import com.openlap.template.DataTransformer;
import com.openlap.template.model.TransformedData;

import java.util.ArrayList;
import java.util.List;

public class BarChartDataTransformer implements DataTransformer {
	TransformedData<List<ChartPoint>> transformedData = new TransformedData<List<ChartPoint>>();

	public TransformedData<List<ChartPoint>> getTransformedData() {
		return this.transformedData;
	}

	public TransformedData<?> transformData(OpenLAPDataSet openLAPDataSet) throws UnTransformableData {

		this.transformedData.setData(new ArrayList<ChartPoint>());

		List<OpenLAPDataColumn> columns = openLAPDataSet.getColumnsAsList(true);

		List<?> textualLabels = null;
		List<?> numericValues = null;

		for (OpenLAPDataColumn column : columns) {
			if (column.getConfigurationData()
					.getType()
					.equals(OpenLAPColumnDataType.Numeric))

				numericValues = column.getData();

			else
				textualLabels = column.getData();
		}

		if (textualLabels != null) {

			for (int i = 0; i < textualLabels.size(); ++i) {

				this.transformedData.getData()
						.add(new ChartPoint(
								String.valueOf(textualLabels.get(i)),
								Float.parseFloat(String.valueOf(numericValues.get(i)))));
			}
		}

		return this.transformedData;
	}

	public static class ChartPoint {
		private final String label;
		private final Float value;

		public ChartPoint(String label, Float value) {
			this.label = label;
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public Float getValue() {
			return value;
		}
	}
}
```

### Step 7. Implement the remaining abstract methods of the `VisualizationCodeGenerator` class.

Now that we have established the DataTransformer and clarified the format in which data will be accessible in the
*visualizationCode* method, it is imperative to implement the remaining abstract methods within the
VisualizationCodeGenerator class. Below is an illustrative example demonstrating the implementation of the Bar Chart
visualization technique. This example utilizes the ApexChart visualization framework to generate the 'Visualization
Generation Script'.

```java
package openlap.visualizer.Charts;

import com.openlap.template.VisualizationCodeGenerator;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.VisualizationCodeGenerationException;
import com.openlap.template.DataTransformer;
import com.openlap.template.VisualizationEscaper;
import com.openlap.template.model.ChartConfiguration;

import java.util.List;
import java.util.Map;

import openlap.visualizer.BarChartDataTransformer;

public class BarChart extends VisualizationCodeGenerator {

	// Declaration of input OpenLAPDataSet by adding OpenLAPDataColumn objects with the OpenLAPDataColumnFactory
	@Override
	public void initializeDataSetConfiguration() {
		this.setInput(new OpenLAPDataSet());
		this.setOutput(new OpenLAPDataSet());

		try {
			this.getInput().addOpenLAPDataColumn(
					OpenLAPDataColumnFactory
							.createOpenLAPDataColumnOfType(
									"xAxisStrings",
									OpenLAPColumnDataType.Text,
									true,
									"X-Axis",
									"List of items to be displayed on X-Axis of the graph"));

			this.getInput().addOpenLAPDataColumn(
					OpenLAPDataColumnFactory
							.createOpenLAPDataColumnOfType(
									"yAxisValues",
									OpenLAPColumnDataType.Numeric,
									true,
									"Y-Axis",
									"List of items to be displayed on Y-Axis of the graph"));
		} catch (OpenLAPDataColumnException exception) {
			throw new IllegalStateException("Could not initialize BarChart input columns", exception);
		}
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
	public String getName() {
		return "Bar Chart";
	}

	@Override
	public String visualizationLibraryScript() {
		return
				"<link href=\"https://cdn.jsdelivr.net/npm/apexcharts@3.45.1/dist/apexcharts.min.css\" rel=\"stylesheet\">" +
						"<script src=\"https://cdn.jsdelivr.net/npm/apexcharts@3.45.1/dist/apexcharts.min.js\">" +
						"</script>";
	}

	@Override
	public Class getDataTransformer() {
		return BarChartDataTransformer.class;
	}

	@Override
	public String visualizationCode(DataTransformer dataTransformer, Map<String, Object> map) throws VisualizationCodeGenerationException {
		BarChartDataTransformer dataInput = (BarChartDataTransformer) dataTransformer;

		List<BarChartDataTransformer.ChartPoint> transformedDataInput = dataInput.getTransformedData().getData();

		String width = map.containsKey("width") ? map.get("width").toString() : "500";
		String height = map.containsKey("height") ? map.get("height").toString() : "350";
		String yLabel = map.containsKey("yLabel") ? map.get("yLabel").toString() : "Value";
		String escapedYLabel = VisualizationEscaper.escapeJavaScriptString(yLabel);

		String apexChartBuildingText;

		StringBuilder dataAxisX = new StringBuilder();
		StringBuilder dataAxisY = new StringBuilder();

		for (int i = 0; i < transformedDataInput.size(); i++) {

			dataAxisX.append(transformedDataInput.get(i).getValue());

			dataAxisY.append("'")
					.append(VisualizationEscaper.escapeJavaScriptString(transformedDataInput.get(i).getLabel()))
					.append("'");

			if (i != transformedDataInput.size() - 1) {
				dataAxisX.append(", ");
				dataAxisY.append(", ");
			}
		}

		apexChartBuildingText = "<div id=\"chart\"></div>" +
				"<script> " +
				"const options = {chart: {type: 'bar', height: " + height + ", width: " + width + "}," +
				"series: [{name: '" + escapedYLabel + "',data: [" + dataAxisX + "]}]," +
				"xaxis: {categories: [" + dataAxisY + "]} };" +
				"const chart = new ApexCharts(document.querySelector(\"#chart\"), options);" +
				"chart.render();</script>";

		return apexChartBuildingText;
	}
}
```

The example escapes string values before placing them into JavaScript string literals. Values such as `width` and
`height` are JavaScript numeric fragments; validate or parse them as numbers before concatenating them in production
visualization code.

#### Step 8. Package the plugin JAR

Mark OpenLAP template and dataset dependencies as `provided`. Package your own
classes, the visualization manifest and browser assets using the standard Maven
JAR lifecycle:

```sh
mvn clean package
```

The resulting `target/<artifactId>-<version>.jar` is uploadable. Do not assemble
OpenLAP SDK or dataset classes into it; the backend provides those classes and
rejects shadow copies. If your generator needs additional Java libraries, package
only those qualified dependencies without duplicating the provided OpenLAP APIs.
The [bundled example POM](examples/bundled-svg/pom.xml) is a complete working build.

#### Step 9. Upload the JAR file using the OpenLAP administration panel

Submit the JAR file to OpenLAP by uploading it through the administration panel. 
