package com.revolut.bank.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor(staticName = "create")
public class AccountNotFoundException extends RuntimeException {
    private final String accountId;

}
