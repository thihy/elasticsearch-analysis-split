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

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeSource;

public class SplitTokenizer extends Tokenizer {

	public static final int DEFAULT_SPLIT_LENGTH = 2;
	private static final int IO_BUFFER_SIZE = 1024;

	public SplitTokenizer(Reader input, int length) {
		super(input);
		init(length);
	}

	public SplitTokenizer(AttributeSource source, Reader input, int length) {
		super(source, input);
		init(length);
	}

	public SplitTokenizer(AttributeFactory factory, Reader input, int length) {
		super(factory, input);
		init(length);
	}

	public SplitTokenizer(Reader input) {
		this(input, DEFAULT_SPLIT_LENGTH);
	}

	private void init(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length must be greater than zero");
		}
		this.splitLength = length;
		termAtt.resizeBuffer(this.splitLength);
	}

	private int splitLength = 0;

	private int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);;
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		int start = offset + bufferIndex;
		while (true) {
			if (bufferIndex >= dataLen) { // need read data from buffer
				offset += dataLen;
				dataLen = input.read(ioBuffer);
				if (dataLen < 0) {// read supplementary  char aware with CharacterUtils
					dataLen = 0; // so next offset += dataLen won't decrement offset
					if (termAtt.length() > 0) {// have read data
						break;
					} else { // end
						finalOffset = correctOffset(offset);
						return false;
					}
				}
				bufferIndex = 0;
			}

			char c = ioBuffer[bufferIndex++];
			termAtt.append(c);
			if (termAtt.length() >= splitLength) {
				break;
			}
		}

		offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(start + termAtt.length()));
		return true;

	}

	@Override
	public final void end() {
		// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset(Reader input) throws IOException {
		super.reset(input);
		bufferIndex = 0;
		offset = 0;
		dataLen = 0;
		finalOffset = 0;
	}
}
