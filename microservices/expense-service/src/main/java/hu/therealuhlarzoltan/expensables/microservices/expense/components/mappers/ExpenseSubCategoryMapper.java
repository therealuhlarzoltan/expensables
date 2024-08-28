package hu.therealuhlarzoltan.expensables.microservices.expense.components.mappers;

import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ExpenseSubCategoryMapper {
    Function<String, String> DISPLAY_NAME_RESOLVER = new Function<String, String>() {
        @Override
        public String apply(String s) {
            if (!s.contains("_AND_")) {
                var split = s.split("_");
                for (int i = 0; i < split.length; ++i)
                    split[i] = split[i].toLowerCase();
                split[0] = split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                return String.join(" ", split);
            } else {
                var split = s.split("_AND_");
                for (int i = 0; i < split.length; ++i) {
                    if (split[i].contains("_")) {
                        var subSplit = split[i].toLowerCase();
                        subSplit = subSplit.replace("_", " ");
                        split[i] = subSplit;
                    } else {
                        split[i] = split[i].toLowerCase();
                    }
                }
                split[0] = split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                return String.join(", ", split);
            }
        }
    };

    @Named("mapSubCategoryToString")
    default String mapSubCategoryToString(String subCategory) {
        return DISPLAY_NAME_RESOLVER.apply(subCategory);
    }

    @Named("mapStringToSubCategory")
    default String mapStringToSubCategory(String subCategory) {
        List<String> subCategoryEnums = ExpenseCategory.getAllSubCategories();
        List<String> subCategoryDisplayNames = subCategoryEnums.stream().map(DISPLAY_NAME_RESOLVER).toList();
        var match = subCategoryDisplayNames.stream().filter(subCategory::equalsIgnoreCase).findFirst();
        if (match.isPresent()) {
            return subCategoryEnums.get(subCategoryDisplayNames.indexOf(match.get()));
        }
        return subCategory;
    }
}
