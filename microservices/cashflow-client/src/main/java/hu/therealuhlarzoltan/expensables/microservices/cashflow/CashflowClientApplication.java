package hu.therealuhlarzoltan.expensables.microservices.cashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "hu.therealuhlarzoltan")
public class CashflowClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashflowClientApplication.class, args);
	}

}
