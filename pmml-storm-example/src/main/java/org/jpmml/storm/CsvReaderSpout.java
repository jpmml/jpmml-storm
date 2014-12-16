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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.dmg.pmml.FieldName;

public class CsvReaderSpout extends BaseRichSpout {

	private File file = null;

	private List<FieldName> columns = null;

	private BufferedReader reader = null;

	private List<String> header = null;

	private SpoutOutputCollector collector = null;


	public CsvReaderSpout(File file, List<FieldName> columns){
		this.file = file;
		this.columns = columns;
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){

		try {
			this.reader = new BufferedReader(new FileReader(this.file));

			this.header = readLine();
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		this.collector = collector;
	}

	@Override
	public void nextTuple(){
		List<String> line;

		try {
			line = readLine();
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		if(line == null){
			return;
		}

		Values values = new Values();

		List<FieldName> columns = this.columns;
		for(FieldName column : columns){
			int index = this.header.indexOf(column.getValue());

			if(index < 0){
				values.add(null);
			} else

			{
				values.add(line.get(index));
			}
		}

		this.collector.emit(values);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer){
		List<String> fields = new ArrayList<String>();

		List<FieldName> columns = this.columns;
		for(FieldName column : columns){
			fields.add(column.getValue());
		}

		declarer.declare(new Fields(fields));
	}

	@Override
	public void close(){

		try {
			this.reader.close();
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
	}

	private List<String> readLine() throws IOException {
		String line = this.reader.readLine();
		if(line == null){
			return null;
		}

		List<String> result = new ArrayList<String>();

		String[] cells = line.split(",");
		for(String cell : cells){

			if(("N/A").equals(cell) || ("N/A").equals(cell)){
				cell = null;
			}

			result.add(cell);
		}

		return result;
	}
}