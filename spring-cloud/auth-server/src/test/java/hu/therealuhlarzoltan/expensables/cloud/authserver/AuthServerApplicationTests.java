package hu.therealuhlarzoltan.expensables.cloud.authserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
		"eureka.client.enabled=false",
		"spring.profiles.active=default",
		"spring.flyway.enabled=false",
})
class AuthServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
