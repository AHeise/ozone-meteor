package eu.stratosphere.sopremo.base;

import static eu.stratosphere.sopremo.type.JsonUtil.createPath;
import static eu.stratosphere.sopremo.type.JsonUtil.createObjectNode;

import org.junit.Test;

import eu.stratosphere.sopremo.expressions.BinaryBooleanExpression;
import eu.stratosphere.sopremo.expressions.ComparativeExpression;
import eu.stratosphere.sopremo.expressions.ObjectAccess;
import eu.stratosphere.sopremo.expressions.ObjectCreation;
import eu.stratosphere.sopremo.expressions.ComparativeExpression.BinaryOperator;
import eu.stratosphere.sopremo.testing.SopremoOperatorTestBase;
import eu.stratosphere.sopremo.testing.SopremoTestPlan;

public class UnionAllTest extends SopremoOperatorTestBase<UnionAll> {
	@Test
	public void shouldPerformThreeWayBagUnion() {
		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(3, 1);

		final UnionAll union = new UnionAll();
		union.setInputs(sopremoPlan.getInputOperators(0, 3));
		sopremoPlan.getOutputOperator(0).setInputs(union);

		sopremoPlan.getInput(0).
			addValue(1).
			addValue(2);
		sopremoPlan.getInput(1).
			addValue(3).
			addValue(4).
			addValue(5);
		sopremoPlan.getInput(2).
			addValue(6).
			addValue(7).
			addValue(8);
		sopremoPlan.getExpectedOutput(0).
			addValue(1).
			addValue(2).
			addValue(3).
			addValue(4).
			addValue(5).
			addValue(6).
			addValue(7).
			addValue(8);

		sopremoPlan.run();
	}

	@Test
	public void shouldPerformTrivialBagUnion() {
		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(1, 1);

		final UnionAll union = new UnionAll();
		union.setInputs(sopremoPlan.getInputOperator(0));
		sopremoPlan.getOutputOperator(0).setInputs(union);

		sopremoPlan.getInput(0).
			addValue(1).
			addValue(2);
		sopremoPlan.getExpectedOutput(0).
			addValue(1).
			addValue(2);

		sopremoPlan.run();
	}

	@Test
	public void shouldPerformTwoWayBagUnion() {
		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);

		final UnionAll union = new UnionAll();
		union.setInputs(sopremoPlan.getInputOperators(0, 2));
		sopremoPlan.getOutputOperator(0).setInputs(union);

		sopremoPlan.getInput(0).
			addValue(1).
			addValue(2);
		sopremoPlan.getInput(1).
			addValue(3).
			addValue(4).
			addValue(5);
		sopremoPlan.getExpectedOutput(0).
			addValue(1).
			addValue(2).
			addValue(3).
			addValue(4).
			addValue(5);

		sopremoPlan.run();
	}

	@Test
	public void shouldPerformTwoWayBagUnionWithBagSemanticsPerDefault() {
		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);

		final UnionAll union = new UnionAll();
		union.setInputs(sopremoPlan.getInputOperators(0, 2));
		sopremoPlan.getOutputOperator(0).setInputs(union);

		sopremoPlan.getInput(0).
			addValue(1).
			addValue(2);
		sopremoPlan.getInput(1).
			addValue(1).
			addValue(2).
			addValue(3);
		sopremoPlan.getExpectedOutput(0).
			addValue(1).
			addValue(2).
			addValue(3).
			addValue(1).
			addValue(2);

		sopremoPlan.run();
	}

	@Test
	public void shouldPerformTwoWayBagUnionWithJoin() {
		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 2);

		final UnionAll union = new UnionAll().withInputs(sopremoPlan.getInputOperators(0, 2));
		final Grouping grouping1 = new Grouping().withGroupingKey(new ObjectAccess("key1")).withInputs(union);
		final Grouping grouping2 = new Grouping().withGroupingKey(new ObjectAccess("key2")).withInputs(union);
		union.setInputs(sopremoPlan.getInputOperators(0, 2));

		sopremoPlan.getOutputOperator(0).setInputs(grouping1);
		sopremoPlan.getOutputOperator(1).setInputs(grouping2);

		sopremoPlan.getInput(0).
			addObject("key1", "A", "key2", "2").
			addObject("key1", "B", "key2", "1");
		sopremoPlan.getInput(1).
			addObject("key1", "A", "key2", "2").
			addObject("key1", "B", "key2", "1");
		sopremoPlan.getExpectedOutput(0).
			addArray(createObjectNode("key1", "A", "key2", "2"), createObjectNode("key1", "A", "key2", "1")).
			addArray(createObjectNode("key1", "B", "key2", "2"), createObjectNode("key1", "B", "key2", "1"));
		sopremoPlan.getExpectedOutput(1).
			addArray(createObjectNode("key1", "A", "key2", "1"), createObjectNode("key1", "B", "key2", "1")).
			addArray(createObjectNode("key1", "A", "key2", "2"), createObjectNode("key1", "B", "key2", "2"));
		sopremoPlan.run();
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.EqualVerifyTest#createDefaultInstance(int)
	 */
	@Override
	protected UnionAll createDefaultInstance(final int index) {
		return new UnionAll();
	}

}
