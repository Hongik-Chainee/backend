package com.chaineeproject.chainee.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record KycPhoneRequestDto(
        @Schema(description = "휴대폰 번호(숫자만 10~11자리)", example = "01012345678")
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,11}$")
        String phone
) {}
