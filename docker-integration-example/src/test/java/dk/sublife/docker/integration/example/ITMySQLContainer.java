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
package dk.sublife.docker.integration.example;

import dk.sublife.docker.integration.example.mysql.MySQLContainer;
import dk.sublife.docker.integration.example.mysql.MySQLContainerApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MySQLContainerApplication.class })
@DirtiesContext
abstract public class ITMySQLContainer {

	@SuppressWarnings("SpringJavaAutowiredMembersInspection")
	@Autowired
	protected ApplicationContext context;

	@Autowired
	protected MySQLContainer container;

	protected Connection connection;

	@Before
	public void setUp() throws Exception {
		container.waitFor();
		connection = DriverManager.getConnection("jdbc:mysql://" + container.address() + "/mysql?user=root");
	}

	protected String version() throws SQLException {
		return connection.getMetaData().getDatabaseProductVersion();
	}

	@Test
	public void testThatMySQLWasStartedSuccesfully() throws Exception {
		assertTrue(context.getBean(MySQLContainer.class).waitFor());
	}

}
