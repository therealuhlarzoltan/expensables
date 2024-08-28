package hu.therealuhlarzoltan.expensables.microservices.expense;

import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@ComponentScan(basePackages = {"hu.therealuhlarzoltan"})
public class ExpenseServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ExpenseServiceApplication.class);

    @Autowired
    ReactiveMongoOperations mongoTemplate;


    public static void main(String[] args) {
        var ctx = SpringApplication.run(ExpenseServiceApplication.class, args);


        String mongodDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongodDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
        String applicationName = ctx.getEnvironment().getProperty("spring.application.name");
        LOG.info("Application with name of {} started", applicationName);
        LOG.info("Connected to MongoDb on URL {}:{}", mongodDbHost, mongodDbPort);
    }


    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {

        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(ExpenseRecordEntity.class);
        resolver.resolveIndexFor(ExpenseRecordEntity.class).forEach(e -> indexOps.ensureIndex(e).block());
    }

}