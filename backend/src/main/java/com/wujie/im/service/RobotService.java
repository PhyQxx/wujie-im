package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.Robot;
import com.wujie.im.entity.RobotRule;
import com.wujie.im.entity.AiConfig;
import com.wujie.im.mapper.RobotMapper;
import com.wujie.im.mapper.RobotRuleMapper;
import com.wujie.im.mapper.AiConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class RobotService {
    @Autowired
    private RobotMapper robotMapper;
    @Autowired
    private RobotRuleMapper robotRuleMapper;
    @Autowired
    private AiConfigMapper aiConfigMapper;

    public Robot createRobot(String name, String avatar, String type, Long ownerId) {
        Robot robot = new Robot();
        robot.setName(name);
        robot.setAvatar(avatar);
        robot.setType(type != null ? type : "AI");
        robot.setOwnerId(ownerId);
        robot.setStatus("ACTIVE");
        robot.setResponseMode("MENTION");
        robot.setContextSize(20);
        robotMapper.insert(robot);
        return robot;
    }

    public List<Robot> listRobots(Long ownerId) {
        LambdaQueryWrapper<Robot> q = new LambdaQueryWrapper<Robot>().eq(Robot::getOwnerId, ownerId);
        return robotMapper.selectList(q);
    }

    public AiConfig getAiConfig(Long robotId) {
        return aiConfigMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getRobotId, robotId)
        );
    }

    public void saveAiConfig(AiConfig config) {
        AiConfig exist = aiConfigMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getRobotId, config.getRobotId())
        );
        if (exist != null) {
            config.setId(exist.getId());
            aiConfigMapper.updateById(config);
        } else {
            aiConfigMapper.insert(config);
        }
    }

    public List<RobotRule> getRules(Long robotId) {
        return robotRuleMapper.selectList(
                new LambdaQueryWrapper<RobotRule>()
                        .eq(RobotRule::getRobotId, robotId)
                        .eq(RobotRule::getEnabled, 1)
                        .orderByDesc(RobotRule::getPriority)
        );
    }

    public String matchRule(Long robotId, String content) {
        List<RobotRule> rules = getRules(robotId);
        for (RobotRule rule : rules) {
            if ("KEYWORD".equals(rule.getRuleType()) && rule.getKeyword() != null
                    && content.contains(rule.getKeyword())) {
                return rule.getReplyContent();
            }
            if ("REGEX".equals(rule.getRuleType()) && rule.getPattern() != null
                    && content.matches(rule.getPattern())) {
                return rule.getReplyContent();
            }
        }
        return null;
    }
}
