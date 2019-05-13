package com.revolut.bank;

import com.google.common.collect.ImmutableMap;
import com.revolut.bank.domain.AccountBalance;
import com.revolut.bank.domain.Bank;
import com.revolut.bank.domain.SerialProcessingBank;

import java.math.BigDecimal;

public class BankMother{

    public static final String FIRST_ACCOUNT_ID = "acc1";
    public static final AccountBalance FIRST_ACCOUNT = AccountBalance.builder().accountId(FIRST_ACCOUNT_ID).balance(BigDecimal.valueOf(150)).build();
    public static final String SECOND_ACCOUNT_ID = "acc2";
    public static final AccountBalance SECOND_ACCOUNT = AccountBalance.builder().accountId(SECOND_ACCOUNT_ID).build();

    public static Bank serialProcessingBankWithTestData(){
        return  SerialProcessingBank.createWithAccounts(ImmutableMap.of(FIRST_ACCOUNT.getAccountId(), FIRST_ACCOUNT, SECOND_ACCOUNT.getAccountId(), SECOND_ACCOUNT));

    }

}
