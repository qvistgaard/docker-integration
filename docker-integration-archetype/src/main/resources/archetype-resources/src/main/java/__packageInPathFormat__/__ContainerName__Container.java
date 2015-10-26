#set( $container = '${rename.this.to.container.name.property:my/container}' )

package ${package};

import com.google.common.io.Files;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import dk.sublife.docker.integration.ContainerRestAdapter;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class ${ContainerName}Container extends ContainerRestAdapter {

	@Value("${container}")
	private String image;

	@Override
	protected ContainerConfig createContainer() {
		return ContainerConfig.builder()
				.hostConfig(createHostConfig())
				.image(image)
				.build();
	}

	@Override
	protected HostConfig createHostConfig() {
		return HostConfig.builder()
				.publishAllPorts(true)
				.build();
	}

	@Override
	public boolean isUp() {
		try {
			System.out.println(isUp(8888, "url"));
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
}

