package eu.stratosphere.sopremo.pact;

import com.google.common.reflect.TypeToken;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.generic.stub.AbstractStub;
import eu.stratosphere.pact.generic.stub.GenericCrosser;
import eu.stratosphere.sopremo.EvaluationContext;
import eu.stratosphere.sopremo.SopremoEnvironment;
import eu.stratosphere.sopremo.serialization.SopremoRecord;
import eu.stratosphere.sopremo.type.IJsonNode;
import eu.stratosphere.sopremo.type.IObjectNode;
import eu.stratosphere.sopremo.type.typed.TypedObjectNode;

/**
 * An abstract implementation of the {@link GenericCrosser}. SopremoCross provides the functionality to convert the
 * standard input of the GenericCrosser to a more manageable representation (both inputs are converted to a subclass of
 * {@link IJsonNode}).
 */
public abstract class GenericSopremoCross<Left extends IJsonNode, Right extends IJsonNode, Out extends IJsonNode>
		extends AbstractStub
		implements GenericCrosser<SopremoRecord, SopremoRecord, SopremoRecord>, SopremoStub {
	private EvaluationContext context;

	private JsonCollector<Out> collector;

	private TypedObjectNode typedInputNode1, typedInputNode2;

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.pact.common.stubs.Stub#open(eu.stratosphere.nephele.configuration.Configuration)
	 */
	@Override
	public void open(final Configuration parameters) throws Exception {
		SopremoEnvironment.getInstance().setConfiguration(parameters);
		// SopremoEnvironment.getInstance().setConfigurationAndContext(parameters, getRuntimeContext());
		this.context = SopremoEnvironment.getInstance().getEvaluationContext();
		this.collector = new JsonCollector<>(this.context);
		final TypedObjectNode[] typedNodes =
			SopremoUtil.getTypedNodes(TypeToken.of(this.getClass()).getSupertype(GenericSopremoCross.class));
		this.typedInputNode1 = typedNodes[0];
		this.typedInputNode2 = typedNodes[1];
		SopremoUtil.configureWithTransferredState(this, GenericSopremoCross.class, parameters);
	}

	/**
	 * This method must be implemented to provide a user implementation of a cross.
	 * 
	 * @param values1
	 *        an {@link IJsonNode} from the first input
	 * @param values2
	 *        an {@link IJsonNode} from the second input
	 * @param out
	 *        a collector that collects all output pairs
	 */
	protected abstract void cross(Left value1, Right value2, JsonCollector<Out> out);

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.pact.common.stubs.CrossStub#cross(eu.stratosphere.pact.common.type.PactRecord,
	 * eu.stratosphere.pact.common.type.PactRecord, eu.stratosphere.pact.common.stubs.Collector)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void cross(final SopremoRecord record1, final SopremoRecord record2, final Collector<SopremoRecord> out) {
		this.collector.configure(out);
		final IJsonNode input1 = record1.getNode();
		final IJsonNode input2 = record2.getNode();

		if (SopremoUtil.LOG.isTraceEnabled())
			SopremoUtil.LOG.trace(String.format("%s %s/%s", this.getContext().getOperatorDescription(), input1, input2));
		try {
			this.cross(
				(Left) (this.typedInputNode1 == null ? input1
					: this.typedInputNode1.withBackingNode((IObjectNode) input1)),
				(Right) (this.typedInputNode2 == null ? input2
					: this.typedInputNode2.withBackingNode((IObjectNode) input2)),
				this.collector);
		} catch (final RuntimeException e) {
			SopremoUtil.LOG.error(String.format("Error occurred @ %s with v1 %s/%s v2: %s", this.getContext()
				.getOperatorDescription(), input1, input2, e));
			throw e;
		}
	}

	@Override
	public final EvaluationContext getContext() {
		return this.context;
	}
}
