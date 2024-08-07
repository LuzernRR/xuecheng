package com.lxz.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxz.content.mapper.TeachplanMapper;
import com.lxz.content.model.dto.SaveTeachplanDto;
import com.lxz.content.model.dto.TeachplanDto;
import com.lxz.content.model.po.Teachplan;
import com.lxz.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/6 下午4:30
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        if (teachplanDtos == null) {
            return Collections.emptyList();
        } else {
            return teachplanDtos; // 返回课程计划树形结构
    }
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        // 通过课程计划id判断是新增还是修改
        // teachplanId为空则新增，不为空则修改
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null) {
            // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            // 确定排序字段，找到它的同级节点，然后将新节点插入到同级节点的最后
            // select count(1) from teachplan where courseid=#{courseid} and parentid=#{parentid}
            Long parentId = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            Integer count = getTeachplanCount(courseId, parentId);
            teachplan.setOrderby(count + 1);
            // 保存课程计划
            teachplanMapper.insert(teachplan);
        } else {
            // 修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            // 将参数复制到teachplan中
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }

    private Integer getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count+1;
    }
}
