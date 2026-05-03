package group_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class GroupManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroupManagerApplication.class, args);
	}

}
