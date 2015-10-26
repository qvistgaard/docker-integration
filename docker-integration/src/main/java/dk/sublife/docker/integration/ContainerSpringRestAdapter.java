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
