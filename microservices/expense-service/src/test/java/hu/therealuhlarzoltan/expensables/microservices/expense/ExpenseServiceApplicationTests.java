package hu.therealuhlarzoltan.expensables.microservices.expense;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "spring.profiles.active=default",
        "spring.data.mongodb.host=localhost"
})
class ExpenseServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
