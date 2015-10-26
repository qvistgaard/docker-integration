package dk.sublife.docker.integration;

import org.springframework.web.client.RestTemplate;

import java.net.URL;

/**
 * Docker container class with rest support.
 * <p/>
 * Implement this class to create a docker container designed for use with
 * integration testing and isUp support for rest a rest service.
 */
abstract public class ContainerRestAdapter extends Container {

	/**
	 * Check if rest service is up and return response.
	 *
	 * @param url of the service to call
	 * @return response
	 * @throws Exception
	 */
	protected String isUp(URL url) throws Exception {
		final RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(url.toString(), String.class);
	}

	/**
	 *
	 * @param port service listening port
	 * @param path service path
	 * @return response
	 * @throws Exception
	 */
	protected String isUp(final int port, final String path) throws Exception {
		return isUp(address(), port, path);
	}

	/**
	 *
	 * @param port service listening port
	 * @param path service path
	 * @return response
	 * @throws Exception
	 */
	protected String isUp(final String host, final int port, final String path) throws Exception {
		final String url = String.format("http://%s:%d%s", host, port, path);
		return isUp(new URL(url));
	}

}
