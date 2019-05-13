package com.revolut.bank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.revolut.bank.model.dto.TransferInstruction;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor(staticName = "createWithPort")
public class BankServerClient {

    private final int port;
    private final ObjectMapper objectMapper;

    HttpResponse<String> requestTransfer(String destinationAccount, final String sourceAccountId, BigDecimal amountToTransfer) throws UnirestException, JsonProcessingException {
        return Unirest.post("http://localhost:" + port + "/accounts/" + sourceAccountId + "/balance/transfer")
                .header("Accept", "application/json")
                .body(objectMapper.writeValueAsString(TransferInstruction.builder().amountToTransfer(amountToTransfer).destinationAccountId(destinationAccount).build()))
                .asString();
    }

    HttpResponse<String> requestBalance(final String accountId) throws UnirestException {
        return Unirest.get("http://localhost:" + port + "/accounts/" + accountId+ "/balance")
                .header("Accept", "application/json")
                .asString();
    }
}
