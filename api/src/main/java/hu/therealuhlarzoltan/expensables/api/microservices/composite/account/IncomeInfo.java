package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeInfo {
    private String recordId;
}
