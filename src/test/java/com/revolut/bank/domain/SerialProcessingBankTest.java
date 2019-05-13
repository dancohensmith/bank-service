package com.revolut.bank.domain;

import com.revolut.bank.BankMother;
import com.revolut.bank.exception.TransferFailedException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class SerialProcessingBankTest {


    private static final int TIMEOUT = 30;
    private static final String MISSING_ACCOUNT = "missing";
    private Bank bank;

    @Before
    public void beforeEveryTest() {
        bank = BankMother.serialProcessingBankWithTestData();
    }


    @Test
    public void canTransferBetweenAccounts() throws Exception {
        Collection<AccountBalance> modifiedAccounts = bank.transfer(BankMother.FIRST_ACCOUNT_ID, BankMother.SECOND_ACCOUNT_ID, BigDecimal.valueOf(25))
                .get(TIMEOUT, TimeUnit.MILLISECONDS);

        assertThat(modifiedAccounts).usingRecursiveFieldByFieldElementComparator().contains(BankMother.FIRST_ACCOUNT.toBuilder().balance(BigDecimal.valueOf(125)).build(),
                BankMother.SECOND_ACCOUNT.toBuilder().balance(BigDecimal.valueOf(25)).build());


    }

    @Test
    public void failsWhenTransferingNegativeBalance() {
        assertThatThrownBy(() -> bank.transfer(BankMother.FIRST_ACCOUNT_ID, BankMother.SECOND_ACCOUNT_ID, BigDecimal.valueOf(-10)).get(TIMEOUT, TimeUnit.MILLISECONDS)).hasCauseInstanceOf(TransferFailedException.class)
                .satisfies(exception -> assertThat(exception.getCause())
                .hasFieldOrPropertyWithValue("failureReason", TransferFailedException.Reason.NEGATIVE_TRANSFER_AMMOUNT)
                .hasFieldOrPropertyWithValue("destinationAccount", BankMother.SECOND_ACCOUNT_ID)
                .hasFieldOrPropertyWithValue("sourceAccount", BankMother.FIRST_ACCOUNT_ID));

    }

    @Test
    public void failsWhenInvalidSourceAccount() {
        assertThatThrownBy(() -> bank.transfer(MISSING_ACCOUNT, BankMother.SECOND_ACCOUNT_ID, BigDecimal.valueOf(25)).get(TIMEOUT, TimeUnit.MILLISECONDS)).hasCauseInstanceOf(TransferFailedException.class)
                .satisfies(exception -> assertThat(exception.getCause())
                        .hasFieldOrPropertyWithValue("failureReason", TransferFailedException.Reason.SOURCE_ACCOUNT_INVALID)
                        .hasFieldOrPropertyWithValue("destinationAccount", BankMother.SECOND_ACCOUNT_ID)
                        .hasFieldOrPropertyWithValue("sourceAccount", MISSING_ACCOUNT));

    }

    @Test
    public void failsWhenInvalidDestinationAccount() {
        assertThatThrownBy(() -> bank.transfer(BankMother.FIRST_ACCOUNT_ID, MISSING_ACCOUNT, BigDecimal.valueOf(25)).get(TIMEOUT, TimeUnit.MILLISECONDS)).hasCauseInstanceOf(TransferFailedException.class)
                .satisfies(exception -> assertThat(exception.getCause())
                        .hasFieldOrPropertyWithValue("failureReason", TransferFailedException.Reason.DESTINATION_ACCOUNT_INVALID)
                        .hasFieldOrPropertyWithValue("destinationAccount", MISSING_ACCOUNT)
                        .hasFieldOrPropertyWithValue("sourceAccount", BankMother.FIRST_ACCOUNT_ID));

    }

    @Test
    public void canGetAccountBalance() throws Exception {
        AccountBalance accountBalance = bank.balance(BankMother.FIRST_ACCOUNT_ID).get(TIMEOUT, TimeUnit.MILLISECONDS);
        assertThat(accountBalance).isEqualToComparingFieldByFieldRecursively(BankMother.FIRST_ACCOUNT);

    }

}