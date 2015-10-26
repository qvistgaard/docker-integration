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
