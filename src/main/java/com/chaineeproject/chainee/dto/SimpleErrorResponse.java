// src/main/java/com/chaineeproject/chainee/dto/SimpleErrorResponse.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SimpleErrorResponse")
public record SimpleErrorResponse(
        @Schema(example = "false") boolean success,
        @Schema(example = "INVALID_OR_EXPIRED_CODE") String message
) {}
