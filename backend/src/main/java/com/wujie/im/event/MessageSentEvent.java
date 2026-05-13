package com.wujie.im.event;

import com.wujie.im.entity.Conversation;
import com.wujie.im.entity.Message;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageSentEvent extends ApplicationEvent {
    private final Message message;
    private final Conversation conversation;
    private final Long excludeUserId;

    public MessageSentEvent(Object source, Message message, Conversation conversation, Long excludeUserId) {
        super(source);
        this.message = message;
        this.conversation = conversation;
        this.excludeUserId = excludeUserId;
    }
}
