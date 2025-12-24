package com.ksj.plomi.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus {

    SUCCESS(HttpStatus.OK, "SUCCESS"),
    CREATED(HttpStatus.CREATED, "CREATED"),
    NO_CONTENT(HttpStatus.NO_CONTENT, "NO_CONTENT");

    private final HttpStatus httpStatus;
    private final String code;
}
