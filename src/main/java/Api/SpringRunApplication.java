package Api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringRunApplication.class, args);
    }
}
