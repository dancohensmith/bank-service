package com.revolut.bank.model.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@Builder
@JsonDeserialize(builder = TransferInstruction.TransferInstructionBuilder.class)
public class TransferInstruction {

    @NonNull
    private final String destinationAccountId;
    @NonNull
    private final BigDecimal amountToTransfer;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TransferInstructionBuilder {
    }



}
