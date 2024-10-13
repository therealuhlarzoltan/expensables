package hu.therealuhlarzoltan.expensables.microservices.expense.annotations;

import hu.therealuhlarzoltan.expensables.microservices.expense.components.validators.ExpenseCategoryExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExpenseCategoryExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpenseCategoryExists {
    String message() default "Expense Category not found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
