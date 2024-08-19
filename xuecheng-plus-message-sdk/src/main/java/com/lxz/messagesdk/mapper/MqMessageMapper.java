package com.lxz.messagesdk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxz.messagesdk.model.po.MqMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {

    @Select("SELECT t.* FROM mq_message t WHERE t.id % #{shardTotal} = #{shardIndex} and t.state='0' and t.message_type=#{messageType} limit #{count}")
    List<MqMessage> selectListByShardIndex(@Param("shardIndex") int shardIndex, @Param("shardTotal") int shardTotal, @Param("messageType") String messageType, @Param("count") int count);

}
