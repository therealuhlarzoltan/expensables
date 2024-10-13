package hu.therealuhlarzoltan.expensables.microservices.transactionclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "hu.therealuhlarzoltan")
public class TransactionClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionClientApplication.class, args);
    }

}
