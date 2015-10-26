package dk.sublife.docker.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Docker container class with rest support.
 * <p/>
 * Implement this class to create a docker container designed for use with
 * integration testing and isUp support for rest a rest service.
 */
abstract public class ContainerSpringRestAdapter extends ContainerRestAdapter {

	/**
	 * slf4j logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerSpringRestAdapter.class);


	public boolean isUp(){
		return isUp(8080);
	}

	public boolean isUp(final int port) {
		try {
			LOGGER.info(isUp(port, "/health"));
			return true;
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		return false;
	}

}
