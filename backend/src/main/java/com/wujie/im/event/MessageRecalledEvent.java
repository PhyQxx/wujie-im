package com.wujie.im.event;

import com.wujie.im.entity.Conversation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageRecalledEvent extends ApplicationEvent {
    private final Long userId;
    private final Long messageId;
    private final Conversation conversation;

    public MessageRecalledEvent(Object source, Long userId, Long messageId, Conversation conversation) {
        super(source);
        this.userId = userId;
        this.messageId = messageId;
        this.conversation = conversation;
    }
}
