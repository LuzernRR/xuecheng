package com.lxz.content.service;

import com.lxz.content.model.dto.SaveTeachplanDto;
import com.lxz.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {
    // 根据课程id查询课程计划
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    public void saveTeachplan(SaveTeachplanDto teachplanDto);
}
