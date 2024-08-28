package hu.therealuhlarzoltan.expensables.microservices.income.annotations;

import hu.therealuhlarzoltan.expensables.microservices.income.components.validators.IncomeCategoryExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = IncomeCategoryExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface IncomeCategoryExists {
    String message() default "Income Category not found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
