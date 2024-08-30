package hu.therealuhlarzoltan.expensables.microservices.income.components.mappers;

import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface IncomeCategoryMapper {
    @Named("mapIncomeCategoryToString")
    default String mapIncomeCategoryToString(String incomeCategory) {
        return IncomeCategory.valueOf(incomeCategory).getName();
    }

    @Named("mapStringToIncomeCategory")
    default String mapStringToIncomeCategory(String incomeCategory) {
        String converted;
        try {
            converted = IncomeCategory.fromString(incomeCategory).toString();
        } catch (IllegalArgumentException e) {
            converted = incomeCategory;
        }
        return converted;
    }
}
