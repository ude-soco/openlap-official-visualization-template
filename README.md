# OpenLAP Visualization Template

## INTRODUCTION

The following video gives the introduction to the OpenLAP followed by the tutorial to add new visualization technique to
the OpenLAP.

<p align="center">
	<a href="http://www.youtube.com/watch?feature=player_embedded&v=e6dwtO0mneo" target="_blank">
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
<script src="https://cdn.jsdelivr.net/npm
/apexcharts@3.45.1/dist/apexcharts.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm
/apexcharts@3.45.1/dist/apexcharts.min.css" rel="stylesheet">

  <!-- Visualization Generation Script -->
  <div id='chartdiv'></div>
  <script>
    var options = {
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
    var chart = new ApexCharts(document.querySelector("#chart"), options);
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

* The ` isDataProcessable()` method takes an `OLAPPortConfiguration` as parameter and validate if the `OpenLAPDataSet`
  with the specified configuration can be processed or not.

* The `generateVisualizationCode()` method takes an `OpenLAPDataSet`, an `OLAPPortConfiguration` and a `DataTransformer`
  as parameters. The incoming `OpenLAPDataSet` is used as an input data which is transformed using the `DataTransformer`
  if the `OLAPPortConfiguration` is valid. The transformed data is then processed using the `visualizationCode()` method
  to produce the “Visualization Generation Script” (as discussed in the Fundamental Concepts section).

* The `getVisualizationLibraryScript()` method returns the “Visualization Library Script” (as discussed in the
  Fundamental Concepts section) by calling the `visualizationLibraryScript()` method.

* The `getInput()` method allow other classes to obtain the input `OpenLAPDataSet` which can be used to get the columns
  metadata as `OLAPColumnConfigurationData` class.

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

## Step by step guide to implement a new Visualization Method

The following steps must be followed by the developer to implement a new Analytics Method for the OpenLAP:

1. Setting up the development environment.

2. Creating project and importing the dependencies into it.

3. Create a class that extends the VisualizationCodeGenerator and VisualizationLibraryInfo classes.

4. Define the input `OpenLAPDataSet`.

5. Create a class that implements the `DataTransformer` interface.

6. Implement the method of the `DataTransformer` interface.

7. Implement the remaining abstract methods of the `VisualizationCodeGenerator` class.

8. Pack the project into a fat jar file.

9. Upload the JAR file using the OpenLAP administration panel.

These steps are explained in more details with concrete examples in the following sections.

### Step 1. Setting up the development environment

To develop a new analytics method, you need to install the following softwares.

* Java Development Kit (JDK) 7+: Ensure that you have Java Development Kit version 7 or above installed on your system (
  Amazon corretto 11 maximum in current OpenLap version).
* Any Integrated Development Environment (IDE) for Java development, such
  as, [Intellij IDEA](https://www.jetbrains.com/idea/download), [NetBeans](https://netbeans.org/downloads/), [Eclipse](https://eclipse.org/downloads/),
  etc.

In the following steps we are going to use the Intellij IDEA for developing the sample analytics method using maven.

### Step 2. Creating project and importing the dependencies into it.

* Create a new project. `File -> New -> Project`
* Select `Maven` from the left and click `Next`.
* Enter the GroupId, ArtifactId and Version etc. To facilitate the retrieval of a recently implemented visualization
  method from the fat JAR file, it is essential to set its GroupId correctly. Otherwise, identifying the newly added
  visualization method class becomes challenging due to the presence of numerous class files from the dependencies
  within fat the JAR file. The GroupId needs to be set as "openlap.visualizer" without the quotes. Click then create.

* Add JitPack repository and include the dependencies needed to access the abstract class VisualizationCodeGenerator and
  DataTransformer interface into the 'pom.xml' file. Obtain the latest version of the dependency XML from the specified
  source: [visualization technique source](https://jitpack.io/#OpenlapDependencies/open-lap-visualization-technique-template-master)
  and [data exchange source](https://jitpack.io/#OpenlapDependencies/open-lap-data-exchange-formats-master) website and
  take the dependency for the maven section. Afterwards reload the maven project.

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
    <groupId>com.github.OpenlapDependencies</groupId>
    <artifactId>
        open-lap-visualization-technique-template-master
    </artifactId>
    <version>version1</version>
</dependency>

<dependency>
    <groupId>com.github.OpenlapDependencies</groupId>
    <artifactId>open-lap-data-exchange-formats-master</artifactId>
    <version>version1</version>
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

public class BarChart extends VisualizationCodeGenerator {

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
// Declaration of input OpenLAPDataSet by adding OLAPDataColum objects with the OpenLAPDataColumnFactory
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
	} catch (OpenLAPDataColumnException var2) {
		var2.printStackTrace();
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

public class PairList implements DataTransformer {
}
```

### Step 6. Implement the method of the `DataTransformer` interface.

The DataTransformer interface necessitates the implementation of only one method. The provided example offers a
demonstration of a data transformer. Specifically, this data transformer takes the input OpenLAPDataSet defined in the
*initializeDataSetConfiguration* method of the class *BarChart* and converts it into a list of string and integer pairs
*List<Pair<String, Integer>>*. The result is returned as an object of the TransformedData class.

```java
package openlap.visualizer;

import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumn;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.exceptions.UnTransformableData;
import com.openlap.template.DataTransformer;
import com.openlap.template.model.TransformedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.util.Pair;

public class PairList implements DataTransformer {
	TransformedData<List<Pair<String, Float>>> transformedData = new TransformedData();

	public TransformedData<List<Pair<String, Float>>> getTransformedData() {
		return this.transformedData;
	}

	public TransformedData<?> transformData(OpenLAPDataSet openLAPDataSet) throws UnTransformableData {

		this.transformedData.setData(new ArrayList());

		List<OpenLAPDataColumn> columns = openLAPDataSet.getColumnsAsList(true);

		List<String> textualLabels = null;
		List<Float> numericValues = null;

		for (var column : columns) {
			if (column.getConfigurationData()
					.getType()
					.equals(OpenLAPColumnDataType.Numeric))

				numericValues = column.getData();

			else
				textualLabels = column.getData();
		}

		if (textualLabels != null) {

			for (int i = 0; i < textualLabels.size(); ++i) {

				// add the data to transformerData as a pair of string and float values
				((List) this.transformedData.getData())
						.add(new Pair((String) textualLabels.get(i),
								Float.parseFloat(String
										.valueOf(numericValues.get(i)))));
			}
		}

		return this.transformedData;
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

import java.util.List;
import java.util.Map;

import openlap.visualizer.PairList;
import javafx.util.Pair;

public class BarChart extends VisualizationCodeGenerator {

	// Declaration of input OpenLAPDataSet by adding OLAPDataColum objects with the OpenLAPDataColumnFactory
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
		} catch (OpenLAPDataColumnException var2) {
			var2.printStackTrace();
		}
	}


	@Override
	public String getName() {
		return "Bar Chart";
	}

	@Override
	public String visualizationLibraryScript() {
		return
				"<link href=\"https://cdn.jsdelivr.net/npm" +
						"/apexcharts@3.45.1/dist/apexcharts.min.css\" rel=\"stylesheet\"" +
						"<script " +
						"src=\"https://cdn.jsdelivr.net/npm" +
						"/apexcharts@3.45.1/dist/apexcharts.min.js\">" +
						"</script>";
	}

	@Override
	public Class getDataTransformer() {
		return PairList.class;
	}

	@Override
	public String visualizationCode(DataTransformer dataTransformer, Map<String, Object> map) throws VisualizationCodeGenerationException {
		PairList dataInput = (PairList) dataTransformer;

		List<Pair<String, Float>> transformedDataInput = (List) dataInput.getTransformedData().getData();

		String width = map.containsKey("width") ? map.get("width").toString() : "500";
		String height = map.containsKey("height") ? map.get("height").toString() : "350";
		String yLabel = map.containsKey("yLabel") ? map.get("yLabel").toString() : "Value";

		String apexChartBuildingText;

		StringBuilder dataAxisX = new StringBuilder();
		StringBuilder dataAxisY = new StringBuilder();

		for (int i = 0; i < transformedDataInput.size(); i++) {

			dataAxisX.append(transformedDataInput.get(i).getValue());

			dataAxisY.append("'" + transformedDataInput.get(i).getKey() + "'");

			if (i != transformedDataInput.size() - 1) {
				dataAxisX.append(", ");
				dataAxisY.append(", ");
			}
		}

		apexChartBuildingText = "<div id=\"chart\"></div>" +
				"<script> " +
				"var options = {chart: {type: 'bar', height: " + height + ", width: " + width + "}," +
				"series: [{name: '" + yLabel + "',data: [" + dataAxisX + "]}]," +
				"xaxis: {categories: [" + dataAxisY + "]} };" +
				"var chart = new ApexCharts(document.querySelector(\"#chart\"), options);" +
				"chart.render();</script>";

		return apexChartBuildingText;
	}
}
```

#### Step 8. Pack the project into a fat jar file

To create a comprehensive JAR file for the project, it is necessary to package it as a fat jar file. To guide Maven in
producing a fat jar from the project, it is essential to integrate a Fat JAR build configuration into the pom.xml file
of the project. The provided configuration should be added to the pom.xml file as outlined below.

```java
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.1.1</version>

            <configuration>
                <descriptorRefs>
                    <descriptorRef>
jar-with-dependencies
		</descriptorRef>
                </descriptorRefs>
            </configuration>

            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Following this, the following command in the terminal of IntelliJ needs to be run: *mvn clean package*. Maven will then
generate a Fat JAR in the *target* directory. The fat jar file will have the following format
*my-project-name-jar-with-dependencies.jar*.

#### Step 9. Upload the JAR file using the OpenLAP administration panel

Submit the JAR file to OpenLAP by uploading it through the administration panel. 
