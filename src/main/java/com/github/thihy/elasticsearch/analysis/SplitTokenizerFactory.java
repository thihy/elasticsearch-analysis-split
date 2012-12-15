/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.thihy.elasticsearch.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class SplitTokenizerFactory extends AbstractTokenizerFactory {

	private final int length;

	public SplitTokenizerFactory(Index index, Settings indexSettings, String name, Settings settings) {
		super(index, indexSettings, name, settings);
		this.length = settings.getAsInt("length", SplitTokenizer.DEFAULT_SPLIT_LENGTH);
	}

	public Tokenizer create(Reader reader) {
		return new SplitTokenizer(reader, length);
	}

}
