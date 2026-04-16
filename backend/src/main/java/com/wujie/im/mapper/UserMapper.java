package com.wujie.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wujie.im.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
