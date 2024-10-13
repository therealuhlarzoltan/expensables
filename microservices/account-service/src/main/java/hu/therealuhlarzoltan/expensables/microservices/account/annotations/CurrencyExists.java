package hu.therealuhlarzoltan.expensables.microservices.account.annotations;

import hu.therealuhlarzoltan.expensables.microservices.account.components.validators.CurrencyExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CurrencyExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrencyExists {
    String message() default "Currency not found";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
