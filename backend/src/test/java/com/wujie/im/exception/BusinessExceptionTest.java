package com.wujie.im.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void defaultConstructor_shouldReturn500Code() {
        BusinessException ex = new BusinessException("test error");

        assertEquals(500, ex.getCode());
        assertEquals("test error", ex.getMessage());
    }

    @Test
    void constructorWithCode_shouldReturnGivenCode() {
        BusinessException ex = new BusinessException(404, "not found");

        assertEquals(404, ex.getCode());
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void forbidden_shouldReturn403Code() {
        BusinessException ex = BusinessException.forbidden("no permission");

        assertEquals(403, ex.getCode());
        assertEquals("no permission", ex.getMessage());
    }

    @Test
    void notFound_shouldReturn404Code() {
        BusinessException ex = BusinessException.notFound("resource missing");

        assertEquals(404, ex.getCode());
        assertEquals("resource missing", ex.getMessage());
    }

    @Test
    void badRequest_shouldReturn400Code() {
        BusinessException ex = BusinessException.badRequest("invalid param");

        assertEquals(400, ex.getCode());
        assertEquals("invalid param", ex.getMessage());
    }

    @Test
    void unauthorized_shouldReturn401Code() {
        BusinessException ex = BusinessException.unauthorized("token expired");

        assertEquals(401, ex.getCode());
        assertEquals("token expired", ex.getMessage());
    }
}
