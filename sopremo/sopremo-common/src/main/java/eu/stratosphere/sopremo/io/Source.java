package eu.stratosphere.sopremo.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.common.operators.base.GenericDataSourceBase;

import eu.stratosphere.pact.common.plan.PactModule;
import eu.stratosphere.sopremo.SopremoEnvironment;
import eu.stratosphere.sopremo.expressions.ArrayCreation;
import eu.stratosphere.sopremo.expressions.EvaluationExpression;
import eu.stratosphere.sopremo.operator.ElementaryOperator;
import eu.stratosphere.sopremo.operator.InputCardinality;
import eu.stratosphere.sopremo.operator.Name;
import eu.stratosphere.sopremo.operator.Property;
import eu.stratosphere.sopremo.pact.SopremoUtil;
import eu.stratosphere.sopremo.serialization.SopremoRecord;
import eu.stratosphere.sopremo.type.IJsonNode;
import eu.stratosphere.sopremo.type.NullNode;
import eu.stratosphere.util.Equaler;

/**
 * Represents a data source in a PactPlan.
 */
@InputCardinality(0)
@Name(noun = "source")
public class Source extends ElementaryOperator<Source> {
	private String inputPath;

	private EvaluationExpression adhocExpression;

	private SopremoFormat format;

	/**
	 * Initializes a Source. This Source uses {@link Source#Source(EvaluationExpression)} with an {@link ArrayCreation}.
	 * This means the provided input data of this Source is empty.
	 */
	public Source() {
		this(new ArrayCreation());
	}

	/**
	 * Initializes a Source with the given {@link EvaluationExpression}. This expression serves as the data provider.
	 * 
	 * @param adhocValue
	 *        the expression that should be used
	 */
	public Source(final EvaluationExpression adhocValue) {
		this.adhocExpression = adhocValue;
		this.format = new JsonFormat();
	}

	/**
	 * Initializes a Source with the given {@link SopremoFormat}.
	 * 
	 * @param format
	 *        the SopremoFormat that should be used
	 */
	public Source(final SopremoFormat format) {
		this(format, null);
	}

	/**
	 * Initializes a Source with the given {@link SopremoFormat} and the given path.
	 * 
	 * @param format
	 *        the SopremoFormat that should be used
	 * @param inputPath
	 *        the path to the input file
	 */
	public Source(final SopremoFormat format, final String inputPath) {
		// check and normalize
		this.inputPath = inputPath;
		this.format = format;

		if (format.getInputFormat() == null)
			throw new IllegalArgumentException("given format does not support reading");

		if (this.inputPath != null)
			this.checkPath();
		this.addPropertiesFrom(this.format);
	}

	/**
	 * Initializes a Source with the given path. This Source uses a {@link JsonFormat} to read the data.
	 * 
	 * @param inputPath
	 *        the path to the input file
	 */
	public Source(final String inputPath) {
		this(new JsonFormat(), inputPath);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.operator.ElementaryOperator#appendAsString(java.lang.Appendable)
	 */
	@Override
	public void appendAsString(final Appendable appendable) throws IOException {
		appendable.append(this.getName()).append(" [");
		if (this.isAdhoc())
			this.adhocExpression.appendAsString(appendable);
		else {
			if (this.inputPath != null)
				appendable.append(this.inputPath).append(", ");
			this.format.appendAsString(appendable);
		}
		appendable.append("]");
	}

	@Override
	public PactModule asPactModule() {
		final String name = this.getName();
		GenericDataSourceBase<SopremoRecord, ?> contract;
		if (this.isAdhoc()) {
			contract = new GenericDataSourceBase<SopremoRecord, GeneratorInputFormat>(
				GeneratorInputFormat.class, SopremoOperatorInfoHelper.source(), String.format("Adhoc %s", name));
			SopremoUtil.setObject(contract.getParameters(), GeneratorInputFormat.ADHOC_EXPRESSION_PARAMETER_KEY,
				this.adhocExpression);
		} else {
			contract =
				new GenericDataSourceBase<SopremoRecord, InputFormat<SopremoRecord, ?>>(this.format.getInputFormat(),
					SopremoOperatorInfoHelper.source(), name);
			this.format.configureForInput(contract.getParameters(), contract, this.inputPath);
		}
		final PactModule pactModule = new PactModule(0, 1);
		SopremoEnvironment.getInstance().save(contract.getParameters());
		contract.setDegreeOfParallelism(this.getDegreeOfParallelism());
		pactModule.getOutput(0).setInput(contract);
		// pactModule.setInput(0, contract);
		return pactModule;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final Source other = (Source) obj;
		return Equaler.SafeEquals.equal(this.inputPath, other.inputPath)
			&& Equaler.SafeEquals.equal(this.format, other.format)
			&& Equaler.SafeEquals.equal(this.adhocExpression, other.adhocExpression);
	}

	/**
	 * Returns the adhoc expression of this Source
	 * 
	 * @return the expression
	 */
	public EvaluationExpression getAdhocExpression() {
		return this.adhocExpression;
	}

	/**
	 * If this Source is adhoc ({@link Source#isAdhoc()}) this method evaluates the adhoc expression and returns the
	 * result or throws an exception otherwise.
	 * 
	 * @return the adhoc values
	 */
	public IJsonNode getAdhocValues() {
		if (!this.isAdhoc())
			throw new IllegalStateException();
		return this.getAdhocExpression().evaluate(NullNode.getInstance());
	}

	/**
	 * Returns the format.
	 * 
	 * @return the format
	 */
	public SopremoFormat getFormat() {
		return this.format;
	}

	/**
	 * Returns the inputPath.
	 * 
	 * @return the path
	 */
	public String getInputPath() {
		return this.inputPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.adhocExpression == null ? 0 : this.adhocExpression.hashCode());
		result = prime * result + (this.format == null ? 0 : this.format.hashCode());
		result = prime * result + (this.inputPath == null ? 0 : this.inputPath.hashCode());
		return result;
	}

	/**
	 * Determines if this Source is adhoc (read his data from an {@link EvaluationExpression}) or not (read his data
	 * from a file)
	 * 
	 * @return either this Source is adhoc or not
	 */
	public boolean isAdhoc() {
		return this.adhocExpression != null;
	}

	/**
	 * Sets the adhoc expression of this Source.
	 * 
	 * @param adhocExpression
	 *        the expression that should be used
	 */
	public void setAdhocExpression(final EvaluationExpression adhocExpression) {
		if (adhocExpression == null)
			throw new NullPointerException("adhocExpression must not be null");

		this.inputPath = null;
		this.adhocExpression = adhocExpression;
	}

	/**
	 * Sets the format to the specified value.
	 * 
	 * @param format
	 *        the format to set
	 */
	@Property(preferred = true)
	public void setFormat(final SopremoFormat format) {
		if (format == null)
			throw new NullPointerException("format must not be null");
		if (format.getInputFormat() == null)
			throw new IllegalArgumentException("reading for the given format is not supported");

		this.removePropertiesFrom(this.format);
		this.format = format;
		this.addPropertiesFrom(format);
	}

	/**
	 * Sets the path to the input file.
	 * 
	 * @param inputPath
	 *        the path
	 */
	public void setInputPath(final String inputPath) {
		if (inputPath == null)
			throw new NullPointerException("inputPath must not be null");

		this.adhocExpression = null;
		this.inputPath = inputPath;
		this.checkPath();
	}

	/**
	 * 
	 */
	private void checkPath() {
		try {
			final URI uri = new URI(this.inputPath);
			if (uri.getScheme() == null)
				throw new IllegalStateException(
					"File name of source does not have a valid schema (such as hdfs or file): " + this.inputPath);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException("Invalid path", e);
		}
	}
}
