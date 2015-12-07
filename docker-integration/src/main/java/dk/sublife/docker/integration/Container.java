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
import com.spotify.docker.client.DockerRequestException;
import com.spotify.docker.client.ImageNotFoundException;
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
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static com.spotify.docker.client.DockerClient.LogsParam.stderr;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;

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

	@Value("${dk.sublife.dk.docker.integration.waitForTimeout:60}")
	private Integer waitForTimeout;

	/**
	 * Create docker container config.
	 *
	 * @return docker container config
	 */
	abstract protected ContainerConfig createContainerConfig() throws Exception;

	/**
	 * Check if service is up and running.
	 * <p/>
	 * Retry policies is enforced by the waitFor method.
	 *
	 * @return true if service is up and running
	 */
	abstract public boolean isUp();

	/**
	 * Post create actions.
	 *
	 * Implement this method to perform post startup actions.
	 * This method is executed after docker container is created. This is the
	 * first time it is possible to inspect the container. This allows for
	 * retrieval of the ip address and for example copy files into the container
	 * before the is actually started.
	 *
	 * @return boolean true if everything went according to plan
	 */
	protected boolean postCreateContainer() { return true; }

	/**
	 * Post container start actions.
	 *
	 * Implement this method to perform post container startup actions.
	 * This method is executed after the container is started but before isUp has been run to verify that the container
	 * is fully started.
	 *
	 * @return boolean true if everything went according to plan
	 */
	protected boolean postStartContainer() { return true; }

	/**
	 * Post startup actions.
	 *
	 * Implement this method to perform post startup actions.
	 * This method is executed after isUp returns true but before waitFor returns.
	 *
	 * @return boolean postStartup actions.
	 */
	protected boolean postStartup(){
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
		return waitFor(waitForTimeout);
	}

	/**
	 * Wait for isUp method call is satisfied.
	 *
	 * @param timoutSeconds seconds before failing
	 * @return true when service is available
	 * @throws InterruptedException
	 */
	protected boolean waitFor(long timoutSeconds) throws Exception {
		final Instant start = Instant.now();
		final ContainerInfo inspect = inspect();
		final String name = inspect.name();
		final String image = inspect.config().image();

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
						if (!postStartup()) {
							throw new RuntimeException("Post startup failed!");
						}
					}
				} else {
					LOGGER.error("Container is not up: {}{}", image, name);
					logFromContainer();
					throw new RuntimeException("Container died.");
				}
			} catch (RuntimeException e){
				throw e;
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
			final long seconds = Duration.between(start, Instant.now()).getSeconds();
			if(seconds > timoutSeconds){
				try {
					if (inspect().state().running()) {
						LOGGER.error("Container is running but not up: {}{}", image, name);
						logFromContainer();
					}
				} catch (final Exception e) {
					LOGGER.error("Error inspecting container!", e);
				}
				throw new RuntimeException("Wait time exceeded.");
			}
			Thread.sleep(5000);
		}
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("container is up {}{}", image, name);
		}
		return true;
	}

	protected void logFromContainer() throws DockerException, InterruptedException {
		final ContainerInfo inspect = inspect();
		final String id = inspect.id();
		final String name = inspect.name();
		final String image = inspect.config().image();
		try (final LogStream logs = dockerClient.logs(id, stderr(), stdout())) {
			final String fullLog = logs.readFully();
			LOGGER.error("Container logs from {}{}:\n {}", image, name, fullLog);
		}
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
	 * Get docker container name
	 *
	 * @return container name
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws UnknownHostException
	 */
	public String name() throws DockerException, InterruptedException, UnknownHostException {
		return inspect().name();
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
		final ContainerConfig containerConfig = createContainerConfig();
		this.container = createContainer(containerConfig);
		try {
			if (!postCreateContainer()) {
				throw new RuntimeException("Post create container failed!");
			}
			LOGGER.info("Starting container: image: {}, name: {}, address: {}", containerConfig.image(), name(), address());
			startContainer();
			try {
				if (!postStartContainer()) {
					throw new RuntimeException("Post start container failed!");
				}
			} catch (final Exception postStartContainerException) {
				killContainer();
				throw postStartContainerException;
			}
		} catch (final Exception postCreateContainerException) {
			removeContainer();
			throw new RuntimeException(postCreateContainerException);
		}
	}

	protected ContainerCreation createContainer(final ContainerConfig containerConfig) throws DockerException, InterruptedException {
		try {
			dockerClient.inspectImage(containerConfig.image());
		} catch (ImageNotFoundException e){
			LOGGER.warn("Image not found: {}. Reason {}", containerConfig.image(), e.getMessage());
			pull(containerConfig.image());
		}
		return dockerClient.createContainer(containerConfig);
	}

	protected void startContainer() throws DockerException, InterruptedException, UnknownHostException {
		final String id = container.id();
		try {
			inspect().config().env().forEach(s -> LOGGER.info("Environment Variable: {}", s));
		} catch (NullPointerException e){
			LOGGER.info("No environment variables set for container");
		}
		dockerClient.startContainer(id);
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
		try {
			killContainer();
		} finally {
			removeContainer();
		}
	}

	protected void killContainer() throws DockerException, InterruptedException {
		final String id = container.id();
		final String name = dockerClient.inspectContainer(id).name();
		LOGGER.info("Stopping container: {}", name);
		try {
			dockerClient.killContainer(container.id());
		} catch (final DockerRequestException e) {
			LOGGER.warn("Docker request error during kill!", e);
		}
		LOGGER.info("Container stopped: {}", name);
	}

	protected void removeContainer() throws DockerException, InterruptedException {
		final String id = container.id();
		final String name = dockerClient.inspectContainer(id).name();
		LOGGER.info("Removing container: {}", name);
		dockerClient.removeContainer(id, true);
		LOGGER.info("Container removed: {}", name);
	}

	/**
	 /**
	 * Copies a local directory to the container.
	 *
	 * @param localDirectory The local directory to send to the container.
	 * @param containerDirectory The directory inside the container where the files are copied to.
	 */
	public void copyToContainer(final Path localDirectory, final Path containerDirectory) throws InterruptedException, DockerException, IOException {
		final String id = container.id();
		dockerClient.copyToContainer(localDirectory, id, containerDirectory.toString());
	}

}
