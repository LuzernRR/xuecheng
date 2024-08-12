package com.lxz.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxz.media.model.po.MediaProcess;
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
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    @Select("select * from xczx_media.media_process t " +
            "where t.id % #{sharedTotal} = #{sharedIndex} " +
            "and (t.status=1 or t.status=3) and t.fail_count<3 limit #{count}")
    List<MediaProcess> selectListBySharedIndex(@Param("sharedIndex") int sharedTotal,
                                               @Param("sharedIndex") int sharedIndex,
                                               @Param("count") int count);
}
