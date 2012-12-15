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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class SplitTokenFilter extends TokenFilter {
	public static final int DEFAULT_SPLIT_LENGTH = 2;

	private int length;

	private int curPos;
	private char[] curTermBuffer;
	private int curTermLength;
	private int tokOffsetStart;
	private int tokOffsetEnd;
	private boolean hasIllegalOffsets;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	public SplitTokenFilter(TokenStream input, int length) {
		super(input);
		if (length < 1) {
			throw new IllegalArgumentException("length must be greater than zero");
		}
		this.length = length;
	}

	public SplitTokenFilter(TokenStream input) {
		this(input, DEFAULT_SPLIT_LENGTH);
	}

	/** Returns the next token in the stream, or null at EOS. */
	@Override
	public final boolean incrementToken() throws IOException {
		while (true) {
			if (curTermBuffer == null) {
				if (!input.incrementToken()) {
					return false;
				}
				curTermBuffer = termAtt.buffer().clone();
				curTermLength = termAtt.length();
				curPos = 0;
				tokOffsetStart = offsetAtt.startOffset();
				tokOffsetEnd = offsetAtt.endOffset();
				hasIllegalOffsets = (tokOffsetStart + curTermLength) != tokOffsetEnd;
			}
			if (curPos < curTermLength) {
				int endPos = curPos + length;
				if (endPos > curTermLength) {
					endPos = curTermLength;
				}
				clearAttributes();
				termAtt.copyBuffer(curTermBuffer, curPos, endPos - curPos);
				if (hasIllegalOffsets) {
					offsetAtt.setOffset(tokOffsetStart, tokOffsetEnd);
				} else {
					offsetAtt.setOffset(tokOffsetStart + curPos, tokOffsetStart + endPos);
				}
				curPos = endPos;
				return true;
			}
			curTermBuffer = null;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		curTermBuffer = null;
	}
}
