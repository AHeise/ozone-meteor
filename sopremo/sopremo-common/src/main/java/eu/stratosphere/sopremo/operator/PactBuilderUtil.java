/***********************************************************************************************************************
 *
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
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
package eu.stratosphere.sopremo.operator;

import com.google.common.base.Preconditions;

import eu.stratosphere.api.java.record.operators.CoGroupOperator;
import eu.stratosphere.api.java.record.operators.JoinOperator;
import eu.stratosphere.api.java.record.operators.ReduceOperator;
import eu.stratosphere.types.Key;

/**
 * Some convenience methods for dealing with the PACT-Builder pattern.
 */
public class PactBuilderUtil
{
	public static void addKeys(final CoGroupOperator.Builder builder, final Class<? extends Key<?>>[] keyClasses,
			final int[] keyIndices1,
			final int[] keyIndices2) {
		Preconditions.checkArgument(keyClasses.length == keyIndices1.length && keyClasses.length == keyIndices2.length,
			"Lenght of keyClasses and keyIndices must match.");
		for (int i = 0; i < keyClasses.length; ++i)
			builder.keyField(keyClasses[i], keyIndices1[i], keyIndices2[i]);
	}

	public static void addKeys(final JoinOperator.Builder builder, final Class<? extends Key<?>>[] keyClasses,
			final int[] keyIndices1,
			final int[] keyIndices2) {
		Preconditions.checkArgument(keyClasses.length == keyIndices1.length && keyClasses.length == keyIndices2.length,
			"Lenght of keyClasses and keyIndices must match.");
		for (int i = 0; i < keyClasses.length; ++i)
			builder.keyField(keyClasses[i], keyIndices1[i], keyIndices2[i]);
	}

	public static void addKeys(final ReduceOperator.Builder builder, final Class<? extends Key<?>>[] keyClasses,
			final int[] keyIndices) {
		Preconditions.checkArgument(keyClasses.length == keyIndices.length,
			"Lenght of keyClasses and keyIndices must match.");
		for (int i = 0; i < keyClasses.length; ++i)
			builder.keyField(keyClasses[i], keyIndices[i]);
	}

	public static void addKeysExceptFirst(final CoGroupOperator.Builder builder,
			final Class<? extends Key<?>>[] keyClasses,
			final int[] keyIndices1,
			final int[] keyIndices2) {
		Preconditions.checkArgument(keyClasses.length == keyIndices1.length && keyClasses.length == keyIndices2.length,
			"Lenght of keyClasses and keyIndices must match.");
		for (int i = 1; i < keyClasses.length; ++i)
			builder.keyField(keyClasses[i], keyIndices1[i], keyIndices2[i]);
	}

	public static void addKeysExceptFirst(final JoinOperator.Builder builder, final Class<? extends Key<?>>[] keyClasses,
			final int[] keyIndices1,
			final int[] keyIndices2) {
		Preconditions.checkArgument(keyClasses.length == keyIndices1.length && keyClasses.length == keyIndices2.length,
			"Lenght of keyClasses and keyIndices must match.");
		for (int i = 1; i < keyClasses.length; ++i)
			builder.keyField(keyClasses[i], keyIndices1[i], keyIndices2[i]);
	}
}
