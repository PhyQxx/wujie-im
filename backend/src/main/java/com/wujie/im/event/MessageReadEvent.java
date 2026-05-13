package com.wujie.im.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageReadEvent extends ApplicationEvent {
    private final Long userId;
    private final Long conversationId;
    private final Long messageId;
    private final Long messageSenderId;

    public MessageReadEvent(Object source, Long userId, Long conversationId, Long messageId, Long messageSenderId) {
        super(source);
        this.userId = userId;
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.messageSenderId = messageSenderId;
    }
}
