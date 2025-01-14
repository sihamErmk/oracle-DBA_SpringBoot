package ma.fstt.springoracle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ma.fstt.springoracle")
public class SpringOracleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringOracleApplication.class, args);
    }

}
