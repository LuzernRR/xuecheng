package com.lxz.messagesdk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxz.messagesdk.model.po.MqMessageHistory;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface MqMessageHistoryMapper extends BaseMapper<MqMessageHistory> {

}
