package eu.stratosphere.sopremo.base;

import org.apache.flink.api.common.operators.base.CrossOperatorBase.CrossWithSmall;

import eu.stratosphere.sopremo.expressions.ObjectAccess;
import eu.stratosphere.sopremo.expressions.PathSegmentExpression;
import eu.stratosphere.sopremo.operator.ElementaryOperator;
import eu.stratosphere.sopremo.operator.InputCardinality;
import eu.stratosphere.sopremo.operator.Internal;
import eu.stratosphere.sopremo.operator.Property;
import eu.stratosphere.sopremo.pact.SopremoCross;
import eu.stratosphere.sopremo.type.IJsonNode;

/**
 * Implements a projection which gets contextual information through a logical side channel.<br>
 * The side channel is given by the second input source.<br>
 * This operator is useful, when a projection needs the result of a prior global aggregation.
 */
@InputCardinality(2)
@Internal
public class ContextualProjection extends ElementaryOperator<ContextualProjection> {
	private PathSegmentExpression contextPath = new ObjectAccess("context");

	public PathSegmentExpression getContextPath() {
		return this.contextPath;
	}

	@Property
	public void setContextPath(final PathSegmentExpression contextPath) {
		if (contextPath == null)
			throw new NullPointerException("contextPath must not be null");

		this.contextPath = contextPath;
	}

	public ContextualProjection withContextPath(final PathSegmentExpression contextPath) {
		this.setContextPath(contextPath);
		return this;
	}

	public static class Implementation extends SopremoCross implements CrossWithSmall {
		private PathSegmentExpression contextPath;

		@Override
		protected IJsonNode cross(final IJsonNode value, final IJsonNode context) {
			this.contextPath.set(value, context);
			return value;
		}
	}
}
