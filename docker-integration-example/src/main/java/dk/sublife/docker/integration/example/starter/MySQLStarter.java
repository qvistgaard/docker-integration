package dk.sublife.docker.integration.example.starter;

import dk.sublife.docker.integration.example.mysql.MySQLContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySQLStarter {

	@Bean
	MySQLContainer mySQLContainer(){
		return new MySQLContainer();
	}


}
