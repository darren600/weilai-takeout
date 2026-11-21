package com.sky.mapper.mp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus 专用 Mapper（窄口接入，隔离设计）。
 *
 * <p>为什么不改现有的 UserMapper：现有 8 个 Mapper 都声明了 void insert(Entity)，
 * 与 BaseMapper.insert(T):int 的返回值冲突，直接 extends BaseMapper 会编译失败（坑 A）。
 * 因此新建一个空的隔离接口，只给 mockLogin 的 LambdaQueryWrapper 按 phone 查询用，
 * 既有 11 个 Mapper 与 11 个 XML 一行不动。</p>
 */
@Mapper
public interface UserMpMapper extends BaseMapper<User> {
}
