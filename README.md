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
* `pmml-storm/target/pmml-storm-1.0-SNAPSHOT.jar` - Library JAR file.
* `pmml-storm-example/target/example-1.0-SNAPSHOT.jar` - Example topology JAR file.

# Usage #

## Library ##

Constructing an instance of Apache Storm bolt class `org.jpmml.storm.PMMLBolt` based on a PMML document in local filesystem:
```java
File pmmlFile = ...;
Evaluator evaluator = PMMLBoltUtil.createEvaluator(pmmlFile);
PMMLBolt pmmlBolt = new PMMLBolt(evaluator);
```

Building a simple topology for scoring data:
```java
TopologyBuilder topologyBuilder = ...;

topologyBuilder.setSpout("input", ...);
topologyBuilder.setBolt("pmml", pmmlBolt)
	.shuffleGrouping("input");
topologyBuilder.setBolt("output", ...)
	.shuffleGrouping("pmml");
```

## Example topology ##

The example topology JAR file contains a single executable class `org.jpmml.storm.Main`.

This class expects three command-line arguments:

1. The path of the **model** PMML file in local filesystem.
2. The path of the **input** CSV file in local filesystem.
3. The path of the **output** CSV file in local filesystem.

For example:
```
storm jar example-1.0-SNAPSHOT.jar org.jpmml.storm.Main DecisionTreeIris.pmml Iris.csv /tmp/DecisionTreeIris.csv
```

Please note that example topology is operational for 30 seconds. If you are testing with larger files please raise this limit as needed.

# License #

JPMML-Storm is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)
