package com.revolut.bank.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Maps;
import com.revolut.bank.exception.TransferFailedException;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;


@Data
@Builder
@JsonDeserialize(builder = Failure.FailureBuilder.class)
public class Failure {

    private final String reason;

    @Singular
    @NonNull
    private final Map<String, Object> details;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class FailureBuilder {
    }

}
