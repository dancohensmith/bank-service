package com.revolut.bank;

import com.revolut.bank.util.AccountBalanceLoader;
import com.revolut.bank.domain.Bank;
import com.revolut.bank.domain.SerialProcessingBank;
import com.revolut.bank.web.BankServer;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankServiceApplication {

    public static void main(String[] args) {
        Bank bank = SerialProcessingBank.createWithAccounts(AccountBalanceLoader.initialBankData());
        BankServer.createUsing( bank, Javalin.create()).start(7000);
    }



}
