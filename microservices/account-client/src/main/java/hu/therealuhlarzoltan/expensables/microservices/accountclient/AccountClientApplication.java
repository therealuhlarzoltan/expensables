package hu.therealuhlarzoltan.expensables.microservices.accountclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "hu.therealuhlarzoltan")
public class AccountClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountClientApplication.class, args);
	}

}
