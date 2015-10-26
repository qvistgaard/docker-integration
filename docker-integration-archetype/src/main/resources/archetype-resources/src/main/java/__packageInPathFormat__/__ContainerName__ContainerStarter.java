package ${package};

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ${ContainerName}ContainerStarter {

	@Bean
	${ContainerName}Container ${ContainerName}ContainerBean(){
		return new ${ContainerName}Container();
	}


}

