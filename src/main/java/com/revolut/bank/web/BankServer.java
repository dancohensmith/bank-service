package com.revolut.bank.web;

import com.revolut.bank.domain.AccountBalance;
import com.revolut.bank.domain.Bank;
import com.revolut.bank.exception.AccountNotFoundException;
import com.revolut.bank.exception.TransferFailedException;
import com.revolut.bank.model.dto.Failure;
import com.revolut.bank.model.dto.TransferInstruction;

import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static io.javalin.apibuilder.ApiBuilder.*;


@RequiredArgsConstructor(staticName = "createUsing")
public class BankServer implements AutoCloseable {

    private static final String ACCOUNTID_PATH_VAR = "accountid";
    private static final String BALANCE_FOR_ACCOUNT_PATH = "/accounts/:" + ACCOUNTID_PATH_VAR + "/balance";
    private static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
    private static final int NOT_FOUND_STATUS_CODE = 404;
    private static final int BAD_REQUEST_STATUS_CODE = 400;
    private final Bank bank;
    private final Javalin javalin;

    public void start(int port) {

        javalin.routes(() ->path(BALANCE_FOR_ACCOUNT_PATH, this::accountBalanceHandlers))
                .exception(TransferFailedException.class, this::handleTransferFailed)
                .exception(AccountNotFoundException.class, this::handleAccountNotFound)
                .exception(Exception.class, this::handleUnknownFailure)
                .start(port);

    }

    private void accountBalanceHandlers() {
        get(this::retrieveBalance);
        post("/transfer", this::transfer);
    }

    private void handleUnknownFailure(Exception exception, Context context) {
        context.json(Failure.builder().reason("Internal Server Error").detail("type", exception.getClass().getSimpleName()).build());
        context.status(INTERNAL_SERVER_ERROR_STATUS_CODE);
    }

    private void handleAccountNotFound(AccountNotFoundException cause, Context context) {
        context.json(transform(cause));
        context.status(NOT_FOUND_STATUS_CODE);
    }


    private void handleTransferFailed(TransferFailedException cause, Context ctx) {
        if (cause.failureReason() == TransferFailedException.Reason.SOURCE_ACCOUNT_INVALID) {
            ctx.json(transform(cause));
            ctx.status(NOT_FOUND_STATUS_CODE);
        } else {
            ctx.json(transform(cause));
            ctx.status(BAD_REQUEST_STATUS_CODE);
        }
    }

    private Failure transform(AccountNotFoundException failure) {
        return Failure.builder()
                .reason("Invalid Account")
                .detail("accountId", failure.accountId())
                .build();
    }

    private Failure transform(TransferFailedException failure) {
        return Failure.builder()
                .reason("Transfer Failed")
                .detail("reason", failure.failureReason())
                .detail("sourceAccount", failure.sourceAccount())
                .detail("destinationAccount", failure.destinationAccount())
                .build();
    }

    private void retrieveBalance(Context context) {
        context.json(bank.balance(accountId(context)));
    }

    private void transfer(Context context) {
        CompletableFuture<Collection<AccountBalance>> transferFuture = requestTransfer(context, context.bodyAsClass(TransferInstruction.class));
        context.json(transferFuture);
    }

    private CompletableFuture<Collection<AccountBalance>> requestTransfer(Context context, TransferInstruction instruction) {
        return bank.transfer(accountId(context), instruction.getDestinationAccountId(), instruction.getAmountToTransfer());
    }

    @NotNull
    private String accountId(Context ctx) {
        return ctx.pathParam(ACCOUNTID_PATH_VAR);
    }


    @Override
    public void close() {
        javalin.stop();
    }
}
