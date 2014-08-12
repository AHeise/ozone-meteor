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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flink.api.common.io.FormatUtil;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.configuration.Configuration;
import org.junit.Ignore;

import eu.stratosphere.sopremo.SopremoEnvironment;
import eu.stratosphere.sopremo.io.SopremoFormat.SopremoFileInputFormat;
import eu.stratosphere.sopremo.pact.SopremoUtil;
import eu.stratosphere.sopremo.serialization.SopremoRecord;
import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
import eu.stratosphere.sopremo.type.IJsonNode;

/**
 * 
 */
@Ignore
public class InputFormatTest {
	public static final SopremoRecordLayout NULL_LAYOUT = SopremoRecordLayout.create();

	public static Collection<IJsonNode> readFromFile(final File file, final SopremoFormat format) throws IOException {

		final Configuration config = new Configuration();
		SopremoEnvironment.getInstance().save(config);
		SopremoUtil.transferFieldsToConfiguration(format, SopremoFormat.class, config,
			format.getInputFormat(), InputFormat.class);
		@SuppressWarnings("unchecked")
		final SopremoFileInputFormat inputFormat =
			FormatUtil.openInput((Class<? extends SopremoFileInputFormat>) format.getInputFormat(),
				file.toURI().toString(), config);

		final List<IJsonNode> values = new ArrayList<IJsonNode>();
		while (!inputFormat.reachedEnd()) {
			final SopremoRecord record = new SopremoRecord();
			if (inputFormat.nextRecord(record) != null)
				values.add(record.getNode().clone());
		}
		inputFormat.close();
		return values;
	}
}
