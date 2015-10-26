package dk.sublife.docker.integration.example.mysql;

import com.spotify.docker.client.messages.ContainerConfig;
import dk.sublife.docker.integration.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

public class MySQLContainer extends Container {

	@Value("${docker.images.mysql:mysql:5.7}")
	private String image;

	/**
	 * slf4j logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MySQLContainer.class);

	@Override
	protected ContainerConfig createContainer() throws Exception {
		return image(image)
				.env("MYSQL_ALLOW_EMPTY_PASSWORD=yes")
				.build();
	}

	@Override
	public boolean isUp() {
		try {
			final Connection connection = DriverManager.getConnection("jdbc:mysql://" + address() + "/mysql?user=root");
			final DatabaseMetaData metaData = connection.getMetaData();
			assert metaData.getDatabaseProductName().equals("MySQL");
			LOGGER.info("Connected to {} v{}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
			connection.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
