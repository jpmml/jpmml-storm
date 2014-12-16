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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;

public class PMMLBolt extends BaseRichBolt {

	private Evaluator evaluator = null;

	private OutputCollector collector = null;


	public PMMLBolt(Evaluator evaluator){
		setEvaluator(evaluator);
	}

	@Override
	public void prepare(Map configuration, TopologyContext context, OutputCollector collector){
		setCollector(collector);
	}

	@Override
	public void execute(Tuple tuple){
		Evaluator evaluator = getEvaluator();

		Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();

		List<FieldName> activeFields = evaluator.getActiveFields();
		for(FieldName activeField : activeFields){
			FieldValue value = EvaluatorUtil.prepare(evaluator, activeField, tuple.getValueByField(activeField.getValue()));

			arguments.put(activeField, value);
		}

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		Values values = new Values();

		List<FieldName> targetFields = evaluator.getTargetFields();
		for(FieldName targetField : targetFields){
			values.add(EvaluatorUtil.decode(result.get(targetField)));
		}

		List<FieldName> outputFields = evaluator.getOutputFields();
		for(FieldName outputField : outputFields){
			values.add(result.get(outputField));
		}

		OutputCollector collector = getCollector();

		collector.emit(tuple, values);
		collector.ack(tuple);
	}

	@Override
	public void cleanup(){
		super.cleanup();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer){
		Evaluator evaluator = getEvaluator();

		List<String> fields = new ArrayList<String>();

		List<FieldName> targetFields = evaluator.getTargetFields();
		for(FieldName targetField : targetFields){
			fields.add(targetField.getValue());
		}

		List<FieldName> outputFields = evaluator.getOutputFields();
		for(FieldName outputField : outputFields){
			fields.add(outputField.getValue());
		}

		declarer.declare(new Fields(fields));
	}

	public Evaluator getEvaluator(){
		return this.evaluator;
	}

	private void setEvaluator(Evaluator evaluator){
		this.evaluator = evaluator;
	}

	public OutputCollector getCollector(){
		return this.collector;
	}

	private void setCollector(OutputCollector collector){
		this.collector = collector;
	}
}