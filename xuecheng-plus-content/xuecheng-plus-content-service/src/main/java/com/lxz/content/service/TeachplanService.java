package com.lxz.content.service;

import com.lxz.content.model.dto.SaveTeachplanDto;
import com.lxz.content.model.dto.TeachplanDto;
import com.lxz.media.model.dto.BindTeachplanMediaDto;
import java.util.List;

public interface TeachplanService {
    // 根据课程id查询课程计划
    public List<TeachplanDto> findTeachplanTree(Long courseId);
    // 保存课程计划
    public void saveTeachplan(SaveTeachplanDto teachplanDto);
    // 课程计划和媒资信息绑定
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
