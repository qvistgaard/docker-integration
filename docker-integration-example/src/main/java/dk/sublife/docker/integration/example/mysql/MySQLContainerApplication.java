package dk.sublife.docker.integration.example.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MySQLContainerApplication {

	public static void main(String[] args) throws Exception {
		final ConfigurableApplicationContext run = SpringApplication.run(MySQLContainerApplication.class, args);
		run.getBean(MySQLContainer.class).waitFor();
		run.stop();
	}
}