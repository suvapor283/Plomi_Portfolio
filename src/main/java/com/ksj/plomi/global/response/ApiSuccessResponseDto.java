package com.ksj.plomi.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiSuccessResponseDto<T> {

    private final int status;
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiSuccessResponseDto<T> success(SuccessStatus status, String message, T data) {
        return new ApiSuccessResponseDto<>(
                status.getHttpStatus().value(),
                true,
                status.getCode(),
                message,
                data
        );
    }

    public static <T> ApiSuccessResponseDto<T> success(SuccessStatus status, T data) {
        return success(status, null, data);
    }
}
