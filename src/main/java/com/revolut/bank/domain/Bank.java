package com.revolut.bank.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface Bank {

    CompletableFuture<Collection<AccountBalance>> transfer(String accountSource, String accountDestination, BigDecimal amountToTransfer);
    CompletableFuture<AccountBalance> balance(String accountId);

}
