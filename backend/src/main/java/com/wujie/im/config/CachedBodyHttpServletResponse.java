package com.wujie.im.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 捕获响应内容的 HttpServletResponse 包装类
 * 允许在处理完成后读取并修改响应内容
 */
public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream capture;
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private boolean committed = false;

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
        this.capture = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (writer != null) {
            throw new IllegalStateException("getWriter() already called");
        }
        if (outputStream == null) {
            outputStream = new CaptureServletOutputStream(capture);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() already called");
        }
        if (writer == null) {
            writer = new PrintWriter(capture);
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            outputStream.flush();
        }
    }

    /**
     * 获取捕获的响应字节数组
     */
    public byte[] getCapture() {
        if (writer != null) {
            writer.flush();
        }
        return capture.toByteArray();
    }

    /**
     * 获取捕获的响应字符串
     */
    public String getCaptureString() {
        return new String(getCapture(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 将响应内容写回客户端（通常在加密后调用）
     */
    public void writeToClient() throws IOException {
        byte[] data = getCapture();
        HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
        rawResponse.setContentLength(data.length);
        rawResponse.getOutputStream().write(data);
        rawResponse.getOutputStream().flush();
        committed = true;
    }

    /**
     * 将响应内容写回客户端，并设置指定的 Content-Length
     */
    public void writeToClient(byte[] data) throws IOException {
        HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
        rawResponse.setContentLength(data.length);
        rawResponse.getOutputStream().write(data);
        rawResponse.getOutputStream().flush();
        committed = true;
    }

    @Override
    public void reset() {
        super.reset();
        capture.reset();
    }

    @Override
    public void resetBuffer() {
        super.resetBuffer();
        capture.reset();
    }

    private static class CaptureServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream os;

        public CaptureServletOutputStream(ByteArrayOutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            os.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
        }
    }
}
