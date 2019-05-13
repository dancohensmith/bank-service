package com.revolut.bank.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.revolut.bank.domain.AccountBalance;
import io.javalin.plugin.json.JavalinJackson;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class AccountBalanceLoader {

    public static Map<@NonNull String, AccountBalance> initialBankData() {
        try {
            URL inStream = ClassLoader.getSystemResource("bank_data.json");
            AccountBalance[] initialBankData =  JavalinJackson.getObjectMapper()
                    .readValue(inStream, new TypeReference<AccountBalance[]>() {
                    });
            return Arrays.stream(initialBankData).collect(Collectors.toMap(AccountBalance::getAccountId, Function.identity()));
        } catch (IOException e) {
            log.error("Failed to load initial data", e);
            throw new RuntimeException("Failed to load initial data");
        }
    }
}
