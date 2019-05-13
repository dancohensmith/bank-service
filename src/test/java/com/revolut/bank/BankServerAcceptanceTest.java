package com.revolut.bank;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.revolut.bank.domain.AccountBalance;
import com.revolut.bank.exception.TransferFailedException;
import com.revolut.bank.model.dto.Failure;
import com.revolut.bank.web.BankServer;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

import static com.revolut.bank.BankMother.*;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public class BankServerAcceptanceTest {


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Javalin javalin;

    private BankServerClient bankServerClient;

    @Before
    public void beforeEveryTest(){
        javalin = Javalin.create();
        BankServer server = BankServer.createUsing(BankMother.serialProcessingBankWithTestData(), javalin);
        server.start(0);
        bankServerClient = BankServerClient.createWithPort(javalin.port(), OBJECT_MAPPER);
    }

    @After
    public void afterEveryTest(){
        javalin.stop();
    }

    @Test
    public void canTransferBetweenAccounts() throws Exception {

        HttpResponse<String> transferResponse = bankServerClient.requestTransfer(SECOND_ACCOUNT_ID, FIRST_ACCOUNT_ID, BigDecimal.valueOf(25));
        assertThat(transferResponse.getStatus()).isEqualTo(200);

        Collection<AccountBalance> balances = fromJson(transferResponse, new TypeReference<Collection<AccountBalance>>() {});

        assertThat(balances).usingRecursiveFieldByFieldElementComparator().contains(FIRST_ACCOUNT.toBuilder().balance(BigDecimal.valueOf(125)).build(),
                SECOND_ACCOUNT.toBuilder().balance(BigDecimal.valueOf(25)).build());

        checkBalance(FIRST_ACCOUNT_ID, FIRST_ACCOUNT.toBuilder().balance(new BigDecimal(125)));
        checkBalance(SECOND_ACCOUNT_ID, SECOND_ACCOUNT.toBuilder().balance(new BigDecimal(25)));

    }

    @Test
    public void failsWhenSourceAccountIsInvalid() throws Exception{

        HttpResponse<String> transferResponse = bankServerClient.requestTransfer(SECOND_ACCOUNT_ID, "missingSource", BigDecimal.valueOf(25));
        assertThat(transferResponse.getStatus()).isEqualTo(404);
        Failure failure = fromJson(transferResponse, new TypeReference<Failure>() {});
        Failure expectedFailure = transferFailed("missingSource", SECOND_ACCOUNT_ID, TransferFailedException.Reason.SOURCE_ACCOUNT_INVALID);
        assertThat(failure).isEqualToComparingFieldByFieldRecursively(expectedFailure);

    }


    @Test
    public void failsWhenDestinationAccountIsInvalid() throws Exception{

        HttpResponse<String> transferResponse = bankServerClient.requestTransfer("missing", FIRST_ACCOUNT_ID, BigDecimal.valueOf(25));
        assertThat(transferResponse.getStatus()).isEqualTo(400);
        Failure failure = fromJson(transferResponse, new TypeReference<Failure>() {});
        Failure expectedFailure = transferFailed(FIRST_ACCOUNT_ID, "missing", TransferFailedException.Reason.DESTINATION_ACCOUNT_INVALID);
        assertThat(failure).isEqualToComparingFieldByFieldRecursively(expectedFailure);

    }

    @Test
    public void failsWhenInsufficientFunds() throws Exception{

        HttpResponse<String> transferResponse = bankServerClient.requestTransfer(SECOND_ACCOUNT_ID, FIRST_ACCOUNT_ID, BigDecimal.valueOf(200));
        assertThat(transferResponse.getStatus()).isEqualTo(400);
        Failure failure = fromJson(transferResponse, new TypeReference<Failure>() {});
        Failure expectedFailure = transferFailed(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, TransferFailedException.Reason.INSUFFICIENT_FUNDS);
        assertThat(failure).isEqualToComparingFieldByFieldRecursively(expectedFailure);

    }


    @Test
    public void failsWhenNegativeTransfer() throws Exception{

        HttpResponse<String> transferResponse = bankServerClient.requestTransfer(SECOND_ACCOUNT_ID, FIRST_ACCOUNT_ID, BigDecimal.valueOf(-200));
        assertThat(transferResponse.getStatus()).isEqualTo(400);
        Failure failure = fromJson(transferResponse, new TypeReference<Failure>() {});
        Failure expectedFailure = transferFailed(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, TransferFailedException.Reason.NEGATIVE_TRANSFER_AMMOUNT);
        assertThat(failure).isEqualToComparingFieldByFieldRecursively(expectedFailure);

    }

    private Failure transferFailed(String sourceAccount, String destinationAccount, TransferFailedException.Reason reason) {
        return Failure.builder().reason("Transfer Failed")
                .detail("reason", reason.toString())
                .detail("sourceAccount", sourceAccount)
                .detail("destinationAccount", destinationAccount)
                .build();
    }

    private void checkBalance(String accountId, AccountBalance.AccountBalanceBuilder expectedBalance) throws UnirestException, IOException {
        HttpResponse<String> balanceResponse = bankServerClient.requestBalance(accountId);
        assertThat(balanceResponse.getStatus()).isEqualTo(200);
        AccountBalance accountBalance = fromJson(balanceResponse, new TypeReference<AccountBalance>() {});
        assertThat(accountBalance).isEqualToComparingFieldByFieldRecursively(expectedBalance);
    }

    private <T> T fromJson(HttpResponse<String> response, TypeReference<T> typeRef) throws IOException {
        return OBJECT_MAPPER.readValue(response.getBody(), typeRef);
    }


}