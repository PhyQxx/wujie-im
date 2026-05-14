package com.wujie.im.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageReadEventTest {

    @Test
    void constructor_shouldSetAllFields() {
        MessageReadEvent event = new MessageReadEvent(this, 1L, 2L, 3L, 4L);

        assertEquals(this, event.getSource());
        assertEquals(1L, event.getUserId());
        assertEquals(2L, event.getConversationId());
        assertEquals(3L, event.getMessageId());
        assertEquals(4L, event.getMessageSenderId());
    }
}
