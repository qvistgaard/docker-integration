package dk.sublife.docker.integration;

import com.spotify.docker.client.messages.ContainerConfig;
import org.junit.Test;

public class ContainerTest {

	@Test(expected = RuntimeException.class)
	public void testThatWaitForWillFailAfterTimeout() throws Exception {
		final Container container = new Container() {
			@Override
			protected ContainerConfig createContainer() {
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
