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
package eu.stratosphere.sopremo.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.flink.api.common.io.FormatUtil;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.configuration.Configuration;
import org.junit.Ignore;

import eu.stratosphere.core.testing.AssertUtil;
import eu.stratosphere.sopremo.SopremoEnvironment;
import eu.stratosphere.sopremo.io.SopremoFormat.SopremoFileOutputFormat;
import eu.stratosphere.sopremo.pact.SopremoUtil;
import eu.stratosphere.sopremo.serialization.SopremoRecord;
import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
import eu.stratosphere.sopremo.type.IJsonNode;

/**
 * Base class for testing output formats.
 */
@Ignore
public class OutputFormatTest {
	public static final SopremoRecordLayout NULL_LAYOUT = SopremoRecordLayout.create();

	/**
	 * Writes the given values using the format, reads them back in, and compares the original values with the read
	 * values.
	 */
	public static void writeAndRead(final SopremoFormat format, final IJsonNode... values) throws IOException {

		final File file = File.createTempFile("csvTest.csv", null);
		file.delete();

		writeToFile(file, format, values);
		final Collection<IJsonNode> readValues = InputFormatTest.readFromFile(file, format);

		file.delete();

		AssertUtil.assertIteratorEquals(Arrays.asList(values).iterator(), readValues.iterator());
	}

	public static void writeToFile(final File file, final SopremoFormat format, final IJsonNode... values)
			throws IOException {
		final Configuration config = new Configuration();
		SopremoEnvironment.getInstance().save(config);
		SopremoUtil.transferFieldsToConfiguration(format, SopremoFormat.class, config,
			format.getOutputFormat(), OutputFormat.class);
		@SuppressWarnings("unchecked")
		final SopremoFileOutputFormat outputFormat =
			FormatUtil.openOutput((Class<? extends SopremoFileOutputFormat>) format.getOutputFormat(),
				file.toURI().toString(), config);

		for (final IJsonNode value : values) {
			final SopremoRecord record = new SopremoRecord();
			record.setNode(value);
			outputFormat.writeRecord(record);
		}
		outputFormat.close();
	}
}
