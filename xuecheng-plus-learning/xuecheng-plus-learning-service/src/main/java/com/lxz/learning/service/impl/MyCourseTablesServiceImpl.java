package com.lxz.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.base.model.PageResult;
import com.lxz.content.model.po.CoursePublish;
import com.lxz.learning.feignclient.ContentServiceClient;
import com.lxz.learning.mapper.XcChooseCourseMapper;
import com.lxz.learning.mapper.XcCourseTablesMapper;
import com.lxz.learning.model.dto.MyCourseTableParams;
import com.lxz.learning.model.dto.XcChooseCourseDto;
import com.lxz.learning.model.dto.XcCourseTablesDto;
import com.lxz.learning.model.po.XcChooseCourse;
import com.lxz.learning.model.po.XcCourseTables;
import com.lxz.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/18 下午3:23
 */
// 选课接口实现
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper chooseCourseMapper;

    @Autowired
    XcCourseTablesMapper courseTablesMapper;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // ==========远程调用内容管理服务获取课程的收费规则==============
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("课程不存在");
        }
        XcChooseCourse xcChooseCourse = null;
        // ================判断课程是否免费================
        if (coursepublish.getCharge().equals("201000")) {
            // ===========免费,向选课表、课程表添加记录=========
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
        } else {
            // ==========收费,向选课表中添加记录=========
            xcChooseCourse = addChargeCourse(userId, coursepublish);
        }
        // ==========判断学习资格(是否过期)并返回============
        XcCourseTablesDto learnStatus = getLearnStatus(userId, courseId);
        // 构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learnStatus.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearnStatus(String userId, Long courseId) {
        // ========查询课程表，查不到直接返回无学习资格========
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if (xcCourseTables == null) {
            xcCourseTablesDto.setLearnStatus("702002"); // 无学习资格
            return xcCourseTablesDto;
        }
        // ========查到了判断是否过期，过期返回无学习资格========
        LocalDateTime validtimeEnd = xcCourseTables.getValidtimeEnd();
        if (LocalDateTime.now().isAfter(validtimeEnd)) {
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702003"); // 学习资格已过期
            return xcCourseTablesDto;
        }
        // ========返回有学习资格========
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        xcCourseTablesDto.setLearnStatus("702001"); // 有学习资格
        return xcCourseTablesDto;
    }

    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {
        return false;
    }

    @Override
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params) {
        return null;
    }

    // 添加免费课程，向选课表、课程表添加记录
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish) {
        // ========幂等性校验，判断是否已经添加过此课程============
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001")  // 免费课程
                .eq(XcChooseCourse::getStatus, "201001");// 选课成功
        // 添加选课记录
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && !xcChooseCourses.isEmpty()) {
            // 如果已经添加过此课程，直接返回
            return xcChooseCourses.get(0);
        }
        // ===============向选课记录表中添加记录================
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setOrderType("700001"); // 免费课程
        xcChooseCourse.setStatus("701001"); // 选课成功
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if (insert <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;

    }
    // 添加收费课程，向选课表中添加记录
    public XcChooseCourse addChargeCourse(String userId, CoursePublish coursepublish) {
        // ========如果存在收费的选课记录且状态为待支付，直接返回============
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")  // 收费课程
                .eq(XcChooseCourse::getStatus, "701002");// 待支付
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && !xcChooseCourses.isEmpty()) {
            // 如果已经添加过此课程，直接返回
            return xcChooseCourses.get(0);
        }
        // ===============向选课记录表中添加记录================
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setOrderType("700002"); // 收费课程
        xcChooseCourse.setStatus("701002"); // 待支付
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if (insert <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }

    // 添加到课程表
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse) {
        // ========判断选课是否成功============
        String status = xcChooseCourse.getStatus();
        if (!status.equals("701001")) {
            XueChengPlusException.cast("选课失败，无法添加到课程表");
        }
        // =======查询课程表中是否已经存在此课程==========
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTables != null) {
            // 如果已经存在，直接返回
            return xcCourseTables;
        }
        // ===============向课程表中添加记录================
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId()); // 选课记录id
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType()); // 课程类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert = courseTablesMapper.insert(xcCourseTables);
        if (insert <= 0) {
            XueChengPlusException.cast("添加课程表记录失败");
        }
        return xcCourseTables;
    }
    // 根据课程和用户查询课程表中的课程
    public XcCourseTables getXcCourseTables(String userId, Long courseId) {
        XcCourseTables xcCourseTables = courseTablesMapper
                .selectOne(new LambdaQueryWrapper<XcCourseTables>()
                        .eq(XcCourseTables::getUserId, userId)
                        .eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }
}
