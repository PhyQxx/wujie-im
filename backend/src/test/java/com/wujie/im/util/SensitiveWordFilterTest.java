package com.wujie.im.util;

import com.wujie.im.entity.SensitiveWord;
import com.wujie.im.mapper.SensitiveWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class SensitiveWordFilterTest {

    @Mock
    private SensitiveWordMapper sensitiveWordMapper;

    @InjectMocks
    private SensitiveWordFilter sensitiveWordFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        List<SensitiveWord> words = new ArrayList<>();
        words.add(createWord("敏感词"));
        words.add(createWord("测试"));
        
        when(sensitiveWordMapper.selectList(null)).thenReturn(words);
        sensitiveWordFilter.init();
    }
    
    private SensitiveWord createWord(String word) {
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        return sw;
    }

    @Test
    public void testFilter() {
        // "这是一个敏感词测试"
        String text = "\u8fd9\u662f\u4e00\u4e2a\u654f\u611f\u8bcd\u6d4b\u8bd5";
        String filtered = sensitiveWordFilter.filter(text);
        // "这是一个*****"
        assertEquals("\u8fd9\u662f\u4e00\u4e2a\u002a\u002a\u002a\u002a\u002a", filtered); 
        
        // "测试一下敏感词"
        text = "\u6d4b\u8bd5\u4e00\u4e0b\u654f\u611f\u8bcd";
        filtered = sensitiveWordFilter.filter(text);
        // "**一下***"
        assertEquals("\u002a\u002a\u4e00\u4e0b\u002a\u002a\u002a", filtered);
    }
    
    @Test
    public void testNoMatch() {
        String text = "正常内容";
        String filtered = sensitiveWordFilter.filter(text);
        assertEquals("正常内容", filtered);
    }
}
