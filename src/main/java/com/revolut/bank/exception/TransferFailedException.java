package com.revolut.bank.exception;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor(staticName = "create")
public class TransferFailedException extends RuntimeException{

    private final String sourceAccount;
    private final String destinationAccount;
    private final Reason failureReason;

    public enum Reason {
        SOURCE_ACCOUNT_INVALID,
        DESTINATION_ACCOUNT_INVALID,
        NEGATIVE_TRANSFER_AMMOUNT,
        INSUFFICIENT_FUNDS
    }
}
