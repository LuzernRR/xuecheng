package com.lxz.content.model.dto;

import lombok.Data;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/6 下午7:49
 */
@Data
public class SaveTeachplanDto {
    private Long id;
    private String pname;
    private Long parentid;
    private Integer grade;
    private String madiaType;
    private Long courseId;
    private Long coursePubId;
    private String isPreview;
}
