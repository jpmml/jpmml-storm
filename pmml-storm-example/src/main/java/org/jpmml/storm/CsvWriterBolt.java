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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.dmg.pmml.FieldName;

public class CsvWriterBolt extends BaseRichBolt {

	private File file = null;

	private List<FieldName> columns = null;

	private BufferedWriter writer = null;

	private OutputCollector collector = null;


	public CsvWriterBolt(File file, List<FieldName> columns){
		this.file = file;
		this.columns = columns;
	}

	@Override
	public void prepare(Map configuration, TopologyContext context, OutputCollector collector){

		try {
			this.writer = new BufferedWriter(new FileWriter(file));

			writeLine(null);
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		this.collector = collector;
	}

	@Override
	public void execute(Tuple tuple){

		try {
			this.writer.write('\n');

			writeLine(tuple);
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		this.collector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer){
	}

	@Override
	public void cleanup(){

		try {
			this.writer.close();
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
	}

	private void writeLine(Tuple tuple) throws IOException {
		StringBuilder sb = new StringBuilder();

		String sep = "";

		List<FieldName> columns = this.columns;
		for(FieldName column : columns){
			sb.append(sep);

			sb.append(tuple != null ? tuple.getValueByField(column.getValue()) : column.getValue());

			sep = ",";
		}

		this.writer.write(sb.toString());
		this.writer.flush();
	}
}