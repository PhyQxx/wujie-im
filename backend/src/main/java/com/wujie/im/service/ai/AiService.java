package com.wujie.im.service.ai;

import com.wujie.im.entity.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class AiService {
    @Autowired
    private MiniMaxService miniMaxService;
    @Autowired
    private GlmService glmService;
    @Autowired
    private DeepSeekService deepSeekService;

    public String chat(AiConfig config, List<String> history, String userMessage) {
        String provider = config.getProvider();
        switch (provider) {
            case "MINIMAX":
                return miniMaxService.chat(config, history, userMessage);
            case "GLM":
                return glmService.chat(config, history, userMessage);
            case "DEEPSEEK":
                return deepSeekService.chat(config, history, userMessage);
            default:
                throw new RuntimeException("不支持的AI提供商: " + provider);
        }
    }
}
