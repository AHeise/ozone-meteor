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
package eu.stratosphere.sopremo.expressions.tree;

import java.util.ListIterator;

import eu.stratosphere.sopremo.expressions.EvaluationExpression;

/**
 */
public abstract class GenericListChildIterator<E extends EvaluationExpression> implements ChildIterator {
	private final ListIterator<E> expressionIterator;

	public GenericListChildIterator(final ListIterator<E> expressionIterator) {
		this.expressionIterator = expressionIterator;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	@Override
	public void add(final EvaluationExpression e) {
		this.expressionIterator.add(this.convert(e));
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.expressions.tree.ChildIterator#isNamed()
	 */
	@Override
	public boolean canChildBeRemoved() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.expressions.tree.ChildIterator#getChildName()
	 */
	@Override
	public String getChildName() {
		return null;
	}

	@Override
	public boolean hasNext() {
		return this.expressionIterator.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return this.expressionIterator.hasPrevious();
	}

	@Override
	public EvaluationExpression next() {
		return this.expressionIterator.next();
	}

	@Override
	public int nextIndex() {
		return this.expressionIterator.nextIndex();
	}

	@Override
	public EvaluationExpression previous() {
		return this.expressionIterator.previous();
	}

	@Override
	public int previousIndex() {
		return this.expressionIterator.previousIndex();
	}

	@Override
	public void remove() {
		this.expressionIterator.remove();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	@Override
	public void set(final EvaluationExpression e) {
		this.expressionIterator.set(this.convert(e));
	}

	protected abstract E convert(EvaluationExpression childExpression);

	/**
	 * Returns the expressionIterator.
	 * 
	 * @return the expressionIterator
	 */
	protected ListIterator<E> getExpressionIterator() {
		return this.expressionIterator;
	}

}
