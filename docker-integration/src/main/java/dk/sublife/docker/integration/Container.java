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

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ImageNotFoundException;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

/**
 * Docker container class.
 * <p/>
 * Implement this class to create a docker container designed for use with
 * integration testing.
 */
abstract public class Container implements InitializingBean, DisposableBean {

	/**
	 * slf4j logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Container.class);


	/**
	 * Docker container instance.
	 */
	private ContainerCreation container;

	/**
	 * Docker client.
	 */
	@Autowired
	private DockerClient dockerClient;

	/**
	 * Docker host config
	 */
	@Autowired
	private HostConfig hostConfig;
	private boolean isUp = false;

	/**
	 * Create docker container config.
	 *
	 * @return docker container config
	 */
	abstract protected ContainerConfig createContainer() throws Exception;

	/**
	 * Check if service is up and running.
	 * <p/>
	 * Retry policies is enforced by the waitFor method.
	 *
	 * @return true if service is up and running
	 */
	abstract public boolean isUp();


	/**
	 * Post startup actions.
	 *
	 * Implement this method to perform post startup actions.
	 * this method is executed after isUp returns true but before waitFor returns.
	 *
	 * @return boolean postStartup actions.
	 */
	public boolean postStartup(){
		return true;
	}

	/**
	 * Create default docker host configuration.
	 * <p/>
	 * Default host configuration can be overwritten by creating a bean which
	 * exposes a {@link HostConfig} instance.
	 * <p/>
	 * To create a custom host config for a container simply overwrite this method.
	 *
	 * @return host config
	 */
	protected HostConfig createHostConfig(){
		return hostConfig;
	}

	/**
	 * Create Container config builder from image.
	 *
	 * @param image docker image
	 * @param env Environment variables
	 * @return Container config builder
	 */
	protected ContainerConfig.Builder image(final String image, String... env){
		return ContainerConfig.builder()
				.hostConfig(createHostConfig())
				.env(ImmutableList.copyOf(env))
				.image(image);
	}

	/**
	 * Wait for isUp method call is satisfied.
	 * <p/>
	 * Overwrite this method to change the default wait timeout, before returning
	 * errors. the default timout is 60 seconds.
	 *
	 * @return true if images is running
	 * @throws InterruptedException
	 */
	public boolean waitFor() throws Exception {
		return waitFor(60);
	}

	/**
	 * Wait for isUp method call is satisfied.
	 *
	 * @param timoutSeconds seconds before failing
	 * @return true when service is available
	 * @throws InterruptedException
	 */
	protected boolean waitFor(long timoutSeconds) throws Exception {
		Instant start = Instant.now();
		final ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
		final String name = containerInfo.name();
		final String image = containerInfo.config().image();


		if(LOGGER.isInfoEnabled() && !isUp){
			LOGGER.info("Waiting for container is up: {}{}", image, name);
		}
		while(!isUp){
			try {
				if (inspect().state().running()) {
					isUp = isUp();
					if(isUp){
						if(LOGGER.isInfoEnabled()){
							LOGGER.info("Running post startup actions...");
						}
						postStartup();
					}
				} else {
					LOGGER.error("Container is not up: {}{}", image, name);
					final LogStream logs = dockerClient.logs(
							container.id(),
							DockerClient.LogsParameter.STDERR,
							DockerClient.LogsParameter.STDOUT);
					while (logs.hasNext()) {
						final LogMessage next = logs.next();
						final CharBuffer decode = Charset.forName("UTF-8").decode(next.content());
						LOGGER.error(String.valueOf(decode).trim());
					}
					throw new RuntimeException("Container died.");
				}
			} catch (RuntimeException e){
				throw e;
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
			final long seconds = Duration.between(start, Instant.now()).getSeconds();
			if(seconds > timoutSeconds){
				throw new RuntimeException("Wait time exceeded.");
			}
			Thread.sleep(5000);
		}
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("container is up {}{}", image, name);
		}
		return true;
	}

	/**
	 * Get docker container ip address
	 *
	 * @return IP Address
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws UnknownHostException
	 */
	public String address() throws DockerException, InterruptedException, UnknownHostException {
		return inspect().networkSettings().ipAddress();
	}

	/**
	 * Inspect docker container.
	 *
	 * @return
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	public ContainerInfo inspect() throws DockerException, InterruptedException {
		return dockerClient.inspectContainer(container.id());
	}

	/**
	 * Pull docker image.
	 *
	 * @param images
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	protected void pull(final String images) throws DockerException, InterruptedException {
		dockerClient.pull(images);
	}

	/**
	 * Invoked by a BeanFactory after it has set all bean properties supplied
	 * (and satisfied BeanFactoryAware and ApplicationContextAware).
	 * <p>This method allows the bean instance to perform initialization only
	 * possible when all bean properties have been set and to throw an
	 * exception in the event of misconfiguration.
	 *
	 * @throws Exception in the event of misconfiguration (such
	 *                   as failure to set an essential property) or if initialization fails.
	 */
	@Override
	synchronized public void afterPropertiesSet() throws Exception {
		final ContainerConfig containerConfig = createContainer();
		try {
			dockerClient.inspectImage(containerConfig.image());
		} catch (ImageNotFoundException e){
			LOGGER.warn("Image not found: {}. Reason {}", containerConfig.image(), e.getMessage());
			pull(containerConfig.image());
		}
		this.container = dockerClient.createContainer(containerConfig);

		try {
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("Starting container: image: {}, name: {}, adress: {}", containerConfig.image(), dockerClient.inspectContainer(this.container.id()).name(), address());
				inspect().config().env().forEach(s -> LOGGER.info("Environment Variable: {}", s));
			}
			dockerClient.startContainer(this.container.id());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invoked by a BeanFactory on destruction of a singleton.
	 *
	 * @throws Exception in case of shutdown errors.
	 *                   Exceptions will get logged but not rethrown to allow
	 *                   other beans to release their resources too.
	 */
	@Override
	public void destroy() throws Exception {
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("Stopping container: {}", dockerClient.inspectContainer(container.id()).name());
		}
		dockerClient.killContainer(container.id());

		if(LOGGER.isInfoEnabled()){
			LOGGER.info("Removing container: {}", dockerClient.inspectContainer(container.id()).name());
		}
		dockerClient.removeContainer(container.id(), true);
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("Container killed and removed: {}", dockerClient.inspectContainer(container.id()).name());
		}
	}
}
