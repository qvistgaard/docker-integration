package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ${ContainerName}ContainerApplication {

	public static void main(String[] args) throws Exception {
		final ConfigurableApplicationContext run = SpringApplication.run(${ContainerName}ContainerApplication.class, args);
		run.getBean(${ContainerName}Container.class).waitFor();
	}
}