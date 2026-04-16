package com.wujie.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wujie.im.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {}
