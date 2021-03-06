/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.sopremo.aggregation;

import java.io.IOException;

import javolution.text.TypeFormat;
import eu.stratosphere.sopremo.type.CachingArrayNode;
import eu.stratosphere.sopremo.type.IJsonNode;

/**
 * Replaces simple {@link ArrayAccess}es in {@link SopremoReduce} and {@link SopremoCoGroup}.<br/>
 * A general {@link ArrayAccess} does not interact well with {@link IStreamNode}; this implementation can be used in
 * {@link BatchAggregationExpression} and is thus more versatile.
 */
public class ArrayAccessAsAggregation extends Aggregation {
	private final int startIndex, endIndex;

	private transient int elementsToSkip, remainingElements;

	private final boolean range;

	private transient final CachingArrayNode<IJsonNode> arrayResult = new CachingArrayNode<IJsonNode>();

	public ArrayAccessAsAggregation(final int index) {
		this(index, index, false);
	}

	/**
	 * Initializes ArrayAccessAsAggregation.
	 */
	public ArrayAccessAsAggregation(final int startIndex, final int endIndex, final boolean range) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.range = range;
	}

	/**
	 * Initializes ArrayAccessAsAggregation.
	 */
	ArrayAccessAsAggregation() {
		this(0, 0, false);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.aggregation.Aggregation#aggregate(eu.stratosphere.sopremo.type.IJsonNode)
	 */
	@Override
	public void aggregate(final IJsonNode element) {
		if (this.elementsToSkip > 0)
			this.elementsToSkip--;
		else if (this.remainingElements > 0) {
			this.arrayResult.addClone(element);
			this.remainingElements--;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.aggregation.Aggregation#toString(java.lang.StringBuilder)
	 */
	@Override
	public void appendAsString(final Appendable appendable) throws IOException {
		// super.appendAsString(appendable);
		appendable.append("@[");
		TypeFormat.format(this.startIndex, appendable);
		if (this.startIndex != this.endIndex) {
			appendable.append(':');
			TypeFormat.format(this.endIndex, appendable);
		}
		appendable.append(']');
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.aggregation.Aggregation#clone()
	 */
	@Override
	public Aggregation clone() {
		return new ArrayAccessAsAggregation(this.startIndex, this.endIndex, this.range);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.aggregation.Aggregation#getFinalAggregate()
	 */
	@Override
	public IJsonNode getFinalAggregate() {
		if (this.range)
			return this.arrayResult;
		return this.arrayResult.get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.aggregation.Aggregation#initialize()
	 */
	@Override
	public void initialize() {
		this.elementsToSkip = this.startIndex;
		this.remainingElements = this.endIndex - this.startIndex + 1;
		this.arrayResult.clear();
	}
}
