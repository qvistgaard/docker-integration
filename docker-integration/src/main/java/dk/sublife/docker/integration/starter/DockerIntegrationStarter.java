package dk.sublife.docker.integration.starter;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.HostConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerIntegrationStarter {

	@Bean
	@ConditionalOnMissingBean(HostConfig.class)
	HostConfig hostConfig(){
		return HostConfig.builder().publishAllPorts(true).build();
	}

	@Bean
	@ConditionalOnMissingBean(DockerClient.class)
	DockerClient dockerClient() throws DockerCertificateException {
		return DefaultDockerClient.fromEnv().build();
	}
}
