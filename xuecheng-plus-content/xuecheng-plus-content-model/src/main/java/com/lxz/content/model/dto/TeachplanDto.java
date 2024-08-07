package com.lxz.content.model.dto;

import com.lxz.content.model.po.Teachplan;
import com.lxz.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/6 下午3:20
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {
    // 与媒资关联的信息
    private TeachplanMedia  teachplanMedia;
    // 小章节list
    private List<TeachplanDto> teachPlanTreeNodes;
}

