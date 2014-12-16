/*
 * Copyright (c) 2014 Villu Ruusmann
 *
 * This file is part of JPMML-Storm
 *
 * JPMML-Storm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Storm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Storm.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.storm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.manager.PMMLManager;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.SourceLocationTransformer;
import org.xml.sax.InputSource;

public class Main {

	static
	public void main(String... args) throws Exception {

		if(args.length != 3){
			System.err.println("Usage: java " + Main.class.getName() + " <PMML file> <Input CSV file> <Output CSV file>");

			System.exit(-1);
		}

		PMML pmml;

		InputStream is = new FileInputStream(args[0]);

		try {
			Source source = ImportFilter.apply(new InputSource(is));

			pmml = JAXBUtil.unmarshalPMML(source);
		} finally {
			is.close();
		}

		pmml.accept(new SourceLocationTransformer());

		PMMLManager pmmlManager = new PMMLManager(pmml);

		ModelEvaluator<?> modelEvaluator = (ModelEvaluator<?>)pmmlManager.getModelManager(ModelEvaluatorFactory.getInstance());

		PMMLBolt pmmlEvaluator = new PMMLBolt(modelEvaluator);

		List<FieldName> inputFields = new ArrayList<FieldName>();
		inputFields.addAll(modelEvaluator.getActiveFields());

		CsvReaderSpout csvReader = new CsvReaderSpout(new File(args[1]), inputFields);

		List<FieldName> outputFields = new ArrayList<FieldName>();
		outputFields.addAll(modelEvaluator.getTargetFields());
		outputFields.addAll(modelEvaluator.getOutputFields());
		CsvWriterBolt csvWriter = new CsvWriterBolt(new File(args[2]), outputFields);

		TopologyBuilder topologyBuilder = new TopologyBuilder();
		topologyBuilder.setSpout("input", csvReader);
		topologyBuilder.setBolt("pmml", pmmlEvaluator)
			.shuffleGrouping("input");
		topologyBuilder.setBolt("output", csvWriter)
			.shuffleGrouping("pmml");

		Config config = new Config();
		config.setDebug(false);

		StormTopology topology = topologyBuilder.createTopology();

		LocalCluster localCluster = new LocalCluster();
		localCluster.submitTopology("example", config, topology);

		Utils.sleep(30L * 1000L);

		localCluster.killTopology("example");
		localCluster.shutdown();
	}
}