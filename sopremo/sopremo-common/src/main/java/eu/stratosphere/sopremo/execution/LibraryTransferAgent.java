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
package eu.stratosphere.sopremo.execution;

import java.io.IOException;

import org.apache.flink.runtime.execution.librarycache.LibraryCacheManager;
import org.apache.flink.runtime.execution.librarycache.LibraryCacheProfileRequest;
import org.apache.flink.runtime.execution.librarycache.LibraryCacheProfileResponse;
import org.apache.flink.runtime.execution.librarycache.LibraryCacheUpdate;

/**
 */
public class LibraryTransferAgent implements LibraryTransferProtocol {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LibraryCacheProfileResponse getLibraryCacheProfile(final LibraryCacheProfileRequest request)
			throws IOException {

		final LibraryCacheProfileResponse response = new LibraryCacheProfileResponse(request);
		final String[] requiredLibraries = request.getRequiredLibraries();

		for (int i = 0; i < requiredLibraries.length; i++)
			if (LibraryCacheManager.contains(requiredLibraries[i]) == null)
				response.setCached(i, false);
			else
				response.setCached(i, true);

		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateLibraryCache(final LibraryCacheUpdate update) throws IOException {

		// Nothing to to here
	}
}
