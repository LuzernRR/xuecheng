package com.lxz.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.content.mapper.TeachplanMapper;
import com.lxz.content.mapper.TeachplanMediaMapper;
import com.lxz.content.model.dto.SaveTeachplanDto;
import com.lxz.content.model.dto.TeachplanDto;
import com.lxz.content.model.po.Teachplan;
import com.lxz.content.model.po.TeachplanMedia;
import com.lxz.content.service.TeachplanService;
import com.lxz.media.model.dto.BindTeachplanMediaDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

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
    @Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 课程id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        // 查询课程信息
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengPlusException.cast("教学计划不存在");
        }
        // 判断课程计划是否为2级
        Integer grade = teachplan.getGrade();
        if (grade != 2){
            XueChengPlusException.cast("只能为第二级课程计划绑定媒资信息");
        }
        // 课程id
        Long courseId = teachplan.getCourseId();
        // ===========先删除原有记录===============
        // 根据课程计划id删除绑定的媒资
        // 使用lambda表达式查询TeachplanMedia中与bindTeachplanMediaDto.getTeachplanId()相等的记录
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, bindTeachplanMediaDto.getTeachplanId()));
        // ===========添加新的关联关系============
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    private Integer getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count+1;
    }
}
