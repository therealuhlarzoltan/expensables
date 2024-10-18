package hu.therealuhlarzoltan.expensables.microservices.accountclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(scanBasePackages = "hu.therealuhlarzoltan", exclude = {MongoAutoConfiguration.class})
public class AccountClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountClientApplication.class, args);
	}

}
