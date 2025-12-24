package com.ksj.plomi.global.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSuccessResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("성공 응답 DTO 생성 - 모든 파라미터 포함 (SuccessStatus, 메시지, 데이터)")
    void testSuccessFullParams() throws Exception {
        Map<String, String> testData = Map.of("key1", "value1", "key2", "value2");
        String testMessage = "SuccessStatus, message, data가 포함된 성공 응답입니다.";

        ApiSuccessResponseDto<Map<String, String>> responseDto =
                ApiSuccessResponseDto.success(SuccessStatus.SUCCESS, testMessage, testData);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseDto.isSuccess()).isTrue();
        assertThat(responseDto.getCode()).isEqualTo(SuccessStatus.SUCCESS.getCode());
        assertThat(responseDto.getMessage()).isEqualTo(testMessage);
        assertThat(responseDto.getData()).isEqualTo(testData);

        String json = objectMapper.writeValueAsString(responseDto);
        assertThat(json).contains("\"status\":200")
                .contains("\"success\":true")
                .contains("\"code\":\"SUCCESS\"")
                .contains("\"message\":\"SuccessStatus, message, data가 포함된 성공 응답입니다.\"")
                .contains("\"data\":{\"key1\":\"value1\",\"key2\":\"value2\"}");
    }

    @Test
    @DisplayName("성공 응답 DTO 생성 - SuccessStatus와 데이터만 포함 (메시지 필드 X)")
    void testSuccessWithDataOnly() throws Exception {
        List<String> testData = List.of("아이템1", "아이템2");

        ApiSuccessResponseDto<List<String>> responseDto =
                ApiSuccessResponseDto.success(SuccessStatus.CREATED, testData);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(responseDto.isSuccess()).isTrue();
        assertThat(responseDto.getCode()).isEqualTo(SuccessStatus.CREATED.getCode());
        assertThat(responseDto.getMessage()).isNull();
        assertThat(responseDto.getData()).isEqualTo(testData);

        String json = objectMapper.writeValueAsString(responseDto);
        assertThat(json).contains("\"status\":201")
                .contains("\"success\":true")
                .contains("\"code\":\"CREATED\"")
                .doesNotContain("\"message\"")
                .contains("\"data\":[\"아이템1\",\"아이템2\"]");
    }
}
