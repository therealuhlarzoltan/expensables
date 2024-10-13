package hu.therealuhlarzoltan.expensables.microservices.accountclient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
		"eureka.client.enabled=false"
})
class AccountClientApplicationTests {

	@Test
	void contextLoads() {
	}

}
