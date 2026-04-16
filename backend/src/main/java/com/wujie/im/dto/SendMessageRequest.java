package com.wujie.im.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long conversationId;
    private Long senderId;
    private String content;
    private String contentType;
    private String meta;
    private Long replyId;
}
