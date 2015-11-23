/*
 * Copyright 2015 Steffen Folman Sørensen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.sublife.docker.integration;

import com.spotify.docker.client.messages.ContainerConfig;
import org.junit.Test;

public class ContainerTest {

	@Test(expected = RuntimeException.class)
	public void testThatWaitForWillFailAfterTimeout() throws Exception {
		final Container container = new Container() {
			@Override
			protected ContainerConfig createContainerConfig() {
				return null;
			}

			@Override
			public boolean isUp() {
				return false;
			}
		};

		container.waitFor(2);
	}
}
