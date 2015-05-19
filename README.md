JPMML-Storm [![Build Status](https://travis-ci.org/jpmml/jpmml-storm.png?branch=master)](https://travis-ci.org/jpmml/jpmml-storm)
===========

PMML evaluator library for the Apache Storm distributed realtime computation system (https://storm.apache.org/).

# Features #

* Full support for PMML specification versions 3.0 through 4.2. The evaluation is handled by the [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) library.

# Prerequisites #

* Apache Storm version 0.7.0 or newer.

# Installation #

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:
* `pmml-storm/target/pmml-storm-runtime-1.0-SNAPSHOT.jar` - Library uber-JAR file. It contains the classes of the library JAR file `pmml-storm/target/pmml-storm-1.0-SNAPSHOT.jar`, plus all the classes of its transitive dependencies.
* `pmml-storm-example/target/example-1.0-SNAPSHOT.jar` - Example Apache Storm topology JAR file.

# Usage #

## Library ##

The [JPMML-Model] (https://github.com/jpmml/jpmml-model) library provides facilities for loading PMML schema version 3.X and 4.X documents into an instance of `org.dmg.pmml.PMML`:
```java
// Use SAX filtering to transform PMML schema version 3.X and 4.X documents to PMML schema version 4.2 document
Source source = ImportFilter.apply(...);

PMML pmml = JAXBUtil.unmarshalPMML(source);

// Transform default SAX Locator information to java.io.Serializable form
pmml.accept(new LocatorTransformer());
```

The [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) library provides facilities for obtaining a proper instance of `org.jpmml.evaluator.ModelEvaluator`:
```java
PMML pmml = ...;

PMMLManager pmmlManager = new PMMLManager(pmml);

ModelEvaluator<?> modelEvaluator = (ModelEvaluator<?>)pmmlManager.getModelManager(ModelEvaluatorFactory.getInstance());
```

The JPMML-Storm library itself provides Apache Storm bolt class `org.jpmml.storm.PMMLBolt`, which integrates the specified `org.jpmml.evaluator.ModelEvaluator` instance into the specified Apache Storm topology instance. The output fields of the bolt match the target fields in the [MiningSchema element] (http://www.dmg.org/v4-2-1/MiningSchema.html), plus all the output fields in the [Output element] (http://www.dmg.org/v4-2-1/Output.html).
```java
ModelEvaluator<?> modelEvaluator = ...;

PMMLBolt pmmlEvaluator = new PMMLBolt(modelEvaluator);
```

Please see [the example application] (https://github.com/jpmml/jpmml-storm/blob/master/pmml-storm-example/src/main/java/org/jpmml/storm/Main.java) for full picture.

## Example Apache Storm topology ##

The example Apache Storm topology JAR file contains a single executable class `org.jpmml.storm.Main`.

This class expects three command-line arguments:

1. The path of the **model** PMML file in local filesystem.
2. The path of the **input** CSV file in local filesystem.
3. The path of the **output** CSV file in local filesystem.

For example:
```
storm jar example-1.0-SNAPSHOT.jar org.jpmml.storm.Main model.pmml input.csv output.csv
```

Please note that example topology is operational for 30 seconds. If you are testing with larger files please raise this limit as needed.

# License #

JPMML-Storm is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)
