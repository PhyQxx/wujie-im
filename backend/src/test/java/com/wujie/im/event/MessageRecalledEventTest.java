package com.wujie.im.event;

import com.wujie.im.entity.Conversation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageRecalledEventTest {

    @Test
    void constructor_shouldSetAllFields() {
        Conversation conv = new Conversation();
        conv.setId(10L);
        conv.setType("GROUP");

        MessageRecalledEvent event = new MessageRecalledEvent(this, 1L, 2L, conv);

        assertEquals(this, event.getSource());
        assertEquals(1L, event.getUserId());
        assertEquals(2L, event.getMessageId());
        assertEquals(conv, event.getConversation());
    }
}
