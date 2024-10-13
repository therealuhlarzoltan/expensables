package hu.therealuhlarzoltan.expensables.microservices.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
		"eureka.client.enabled=false",
		"spring.profiles.active=default",
		"spring.data.mongodb.host=localhost"
})
class AccountServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
