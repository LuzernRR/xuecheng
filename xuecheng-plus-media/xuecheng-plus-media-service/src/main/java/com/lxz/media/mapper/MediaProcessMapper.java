package com.lxz.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxz.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    // 根据sharedIndex查询媒资文件处理列表
    @Select("select * from xczx_media.media_process t " +
            "where t.id % #{sharedTotal} = #{sharedIndex} " +
            "and (t.status=1 or t.status=3) and t.fail_count<3 limit #{count}")
    List<MediaProcess> selectListBySharedIndex(@Param("sharedTotal") int sharedTotal,
                                               @Param("sharedIndex") int sharedIndex,
                                               @Param("count") int count);


    // 乐观锁开启任务，谁抢到谁处理，拿到的返回1
    @Update("update xczx_media.media_process m set m.status='4' " +
            "where (m.status='1' or m.status='3') " +
            "and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);
}
