package com.revolut.bank.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.math.BigDecimal;

@Getter
@ToString
@Builder(toBuilder = true)
@JsonDeserialize(builder = AccountBalance.AccountBalanceBuilder.class)
public class AccountBalance {

    @Builder.Default
    private BigDecimal balance = new BigDecimal(0);
    @NonNull
    private String accountId;

    AccountBalance withdraw(BigDecimal amountToWithdraw){
        BigDecimal newBalance = balance.subtract(amountToWithdraw);
        return this.toBuilder().balance(newBalance).build();
    }

     AccountBalance deposit(BigDecimal toDeposit){
         BigDecimal newBalance = balance.add(toDeposit);
         return this.toBuilder().balance(newBalance).build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AccountBalanceBuilder {
    }

}
