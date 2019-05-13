package com.revolut.bank.domain;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.revolut.bank.exception.AccountNotFoundException;
import com.revolut.bank.exception.TransferFailedException;
import lombok.NonNull;


import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class SerialProcessingBank implements Bank {


    //Using this means no changes to the balances will take place concurrently they will take place in single thread Similar to the actor model.
    //This means that you wont race between transfers.
    private final Executor executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Bank-%s").build());


    @NonNull
    private final Map<String, AccountBalance> balances;

    private SerialProcessingBank(@NonNull Map<String, AccountBalance> balances) {
        this.balances = balances;
    }

    /**
     * This is used for loading pre existing balances states on startup.
     **/
    public static SerialProcessingBank createWithAccounts(@NonNull Map<String, AccountBalance> balances) {
        return new SerialProcessingBank(Maps.newHashMap(balances));
    }

    public static SerialProcessingBank create() {
        return new SerialProcessingBank(Maps.newHashMap());
    }

    @Override
    public CompletableFuture<AccountBalance> balance(String accountId) {
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(balances.get(accountId)).orElseThrow(() -> AccountNotFoundException.create(accountId)), executor);
    }

    @Override
    public CompletableFuture<Collection<AccountBalance>> transfer(String accountSource, String accountDestination, BigDecimal amountToTransfer) {
        return CompletableFuture.supplyAsync(() -> doTransfer(accountSource, accountDestination, amountToTransfer), executor);
    }


    private ImmutableSet<AccountBalance> doTransfer(String accountSource, String accountDestination, BigDecimal amountToTransfer) {

        AccountBalance source = Optional.ofNullable(balances.get(accountSource))
                .orElseThrow(() -> TransferFailedException.create(accountSource, accountDestination, TransferFailedException.Reason.SOURCE_ACCOUNT_INVALID));
        AccountBalance destination = Optional.ofNullable(balances.get(accountDestination))
                .orElseThrow(() -> TransferFailedException.create(accountSource, accountDestination, TransferFailedException.Reason.DESTINATION_ACCOUNT_INVALID));

        if(amountToTransfer.signum() <= 0){
            throw TransferFailedException.create(accountSource, accountDestination, TransferFailedException.Reason.NEGATIVE_TRANSFER_AMMOUNT);
        }

        AccountBalance newSourceBalance = source.withdraw(amountToTransfer);
        validateNewBalance(accountSource, accountDestination, newSourceBalance);
        AccountBalance newDestinationBalance = destination.deposit(amountToTransfer);
        updateBalances(newSourceBalance, newDestinationBalance);

        return ImmutableSet.of(newSourceBalance, newDestinationBalance);
    }

    private void updateBalances(AccountBalance newSourceBalance, AccountBalance newDestinationBalance) {
        balances.put(newSourceBalance.getAccountId(), newSourceBalance);
        balances.put(newDestinationBalance.getAccountId(), newDestinationBalance);
    }

    private void validateNewBalance(String accountSource, String accountDestination, AccountBalance newSourceBalance) {
        if (newSourceBalance.getBalance().signum() < 0){
            throw TransferFailedException.create(accountSource, accountDestination, TransferFailedException.Reason.INSUFFICIENT_FUNDS);
        }
    }

}
