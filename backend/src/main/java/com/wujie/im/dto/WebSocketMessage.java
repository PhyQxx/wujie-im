package com.wujie.im.dto;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;
    private Object data;
}
