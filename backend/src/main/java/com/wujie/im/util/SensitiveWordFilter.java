package com.wujie.im.util;

import com.wujie.im.entity.SensitiveWord;
import com.wujie.im.mapper.SensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 基于 DFA 算法的敏感词过滤器
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    private Map<Character, Object> wordMap = new HashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @SuppressWarnings("unchecked")
    public void refresh() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
        Map<Character, Object> newWordMap = new HashMap<>();
        for (SensitiveWord sw : words) {
            String word = sw.getWord();
            if (word == null || word.isBlank()) continue;
            
            Map<Character, Object> currentMap = newWordMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Map<Character, Object> subMap = (Map<Character, Object>) currentMap.get(c);
                if (subMap == null) {
                    subMap = new HashMap<>();
                    currentMap.put(c, subMap);
                }
                currentMap = subMap;
                if (i == word.length() - 1) {
                    currentMap.put('\0', true); // 结束标志
                }
            }
        }
        this.wordMap = newWordMap;
        log.info("敏感词库加载完成，共 {} 个词", words.size());
    }

    public String filter(String text) {
        if (text == null || text.isBlank()) return text;
        
        StringBuilder result = new StringBuilder(text);
        int i = 0;
        while (i < text.length()) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                for (int j = 0; j < length; j++) {
                    result.setCharAt(i + j, '*');
                }
                i += length;
            } else {
                i++;
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int startIndex) {
        Map<Character, Object> currentMap = wordMap;
        int foundLength = 0;
        
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            Object next = currentMap.get(c);
            
            if (next instanceof Map) {
                currentMap = (Map<Character, Object>) next;
                if (currentMap.containsKey('\0')) {
                    foundLength = (i - startIndex) + 1;
                }
            } else {
                break;
            }
        }
        return foundLength;
    }
}
