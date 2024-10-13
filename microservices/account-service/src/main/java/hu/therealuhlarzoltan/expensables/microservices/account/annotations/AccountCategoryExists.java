package hu.therealuhlarzoltan.expensables.microservices.account.annotations;

import hu.therealuhlarzoltan.expensables.microservices.account.components.validators.AccountCategoryExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = AccountCategoryExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountCategoryExists {
    String message() default "Account Category not found";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
