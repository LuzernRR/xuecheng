package com.lxz.content.service;

import com.lxz.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
