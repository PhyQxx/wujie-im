package com.wujie.im.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_shouldReturn200WithNullData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void successWithData_shouldReturn200WithGivenData() {
        String data = "test data";

        Result<String> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals(data, result.getData());
    }

    @Test
    void successWithMsgAndData_shouldReturn200WithGivenMsgAndData() {
        String data = "test data";

        Result<String> result = Result.success("自定义消息", data);

        assertEquals(200, result.getCode());
        assertEquals("自定义消息", result.getMsg());
        assertEquals(data, result.getData());
    }

    @Test
    void error_shouldReturnGivenCodeAndMsgWithNullData() {
        Result<Void> result = Result.error(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void errorWithMsg_shouldReturn500WithGivenMsgAndNullData() {
        Result<Void> result = Result.error("服务器错误");

        assertEquals(500, result.getCode());
        assertEquals("服务器错误", result.getMsg());
        assertNull(result.getData());
    }
}
