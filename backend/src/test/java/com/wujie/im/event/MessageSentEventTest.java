package com.wujie.im.event;

import com.wujie.im.entity.Conversation;
import com.wujie.im.entity.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageSentEventTest {

    @Test
    void constructor_shouldSetAllFields() {
        Message msg = new Message();
        msg.setId(1L);
        msg.setContent("test content");

        Conversation conv = new Conversation();
        conv.setId(10L);
        conv.setType("SINGLE");

        MessageSentEvent event = new MessageSentEvent(this, msg, conv, 1L);

        assertEquals(this, event.getSource());
        assertEquals(msg, event.getMessage());
        assertEquals(conv, event.getConversation());
        assertEquals(1L, event.getExcludeUserId());
    }

    @Test
    void constructor_shouldAllowNullExcludeUserId() {
        Message msg = new Message();
        Conversation conv = new Conversation();

        MessageSentEvent event = new MessageSentEvent(this, msg, conv, null);

        assertNull(event.getExcludeUserId());
    }
}
