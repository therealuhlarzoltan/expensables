package hu.therealuhlarzoltan.expensables.microservices.expense.annotations;

import hu.therealuhlarzoltan.expensables.microservices.expense.components.validators.ExpenseSubCategoryExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExpenseSubCategoryExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpenseSubCategoryExists {
    String message() default "Expense Subcategory not found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}