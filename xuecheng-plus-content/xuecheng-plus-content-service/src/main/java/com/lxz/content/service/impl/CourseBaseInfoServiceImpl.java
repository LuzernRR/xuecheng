package com.lxz.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.content.mapper.CourseBaseMapper;
import com.lxz.content.mapper.CourseCategoryMapper;
import com.lxz.content.mapper.CourseMarketMapper;
import com.lxz.content.model.dto.AddCourseDto;
import com.lxz.content.model.dto.CourseBaseInfoDto;
import com.lxz.content.model.dto.EditCourseDto;
import com.lxz.content.model.dto.QueryCourseParamsDto;
import com.lxz.content.model.po.CourseBase;
import com.lxz.content.model.po.CourseMarket;
import com.lxz.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 根据名称模糊查询， 在sql中拼接courseName like '%java%'
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        // 根据课程审核状态查询， 在sql中拼接audit_status = ？
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        // 按课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus());

        // 创建page分页参数对象，当前页1，每页显示10条
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 执行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 数据列表
        List<CourseBase> items = pageResult.getRecords();
        // 总记录数
        long total = pageResult.getTotal();
        // List<T> items long counts long page long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
        System.out.println(courseBasePageResult);
        return courseBasePageResult;
    }

    @Transactional  // 增删改查需要添加事务注解
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 参数的合法性校验
//        if (StringUtils.isBlank(dto.getName())){
////            throw new RuntimeException("课程名称不能为空");
//            XueChengPlusException.cast("课程名称不能为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())){
//            throw new RuntimeException("课程大分类不能为空");
//        }
//        if (StringUtils.isBlank(dto.getSt())){
//            throw new RuntimeException("课程小分类不能为空");
//        }
//        if (StringUtils.isBlank(dto.getGrade())){
//            throw new RuntimeException("课程等级不能为空");
//        }
//        if (StringUtils.isBlank(dto.getTeachmode())){
//            throw new RuntimeException("学习模式不能为空");
//        }
//        if (StringUtils.isBlank(dto.getUsers())){
//            throw new RuntimeException("适应人群不能为空");
//        }
//        if (StringUtils.isBlank(dto.getCharge())){
//            throw new RuntimeException("课程收费不能为空");
//        }
        // 向课程信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        // 将传入的页面参数传入courseBaseNew对象
//        courseBaseNew.setName(dto.getName());
//        courseBaseNew.setDescription(dto.getDescription());
        // 从原始对象中拷贝属性到新对象，不用上面的，可以使用BeanUtils.copyProperties
        // 只要属性名称一致，就会自动拷贝
        BeanUtils.copyProperties(dto, courseBaseNew);
        // companyId是页面传入的参数，需要设置到courseBaseNew对象中
        courseBaseNew.setCompanyId(companyId);
        // 创建时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 设置默认审核状态为未提交
        courseBaseNew.setAuditStatus("202002");
        // 设置默认发布状态为未发布
        courseBaseNew.setStatus("203001");
        // 课程基本信息表插入数据库
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0){
            throw new RuntimeException("课程信息添加失败");
        }
        // 向课程营销表course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        // 将传入的页面参数拷贝到courseMarketNew对象
        BeanUtils.copyProperties(dto, courseMarketNew);
        // 课程id
        Long courseId = courseBaseNew.getId();
        courseMarketNew.setId(courseId);
        // 保存营销信息
        savaCourseMarket(courseMarketNew);

        // 保存成功后，从数据库查询课程基本信息和课程营销信息，返回
        return getCourseBaseInfo(courseId);
    }

    // 查询课程信息
    @Override
    @Transactional
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        // 从课程信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }
        // 从课程营销信息表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 封装到CourseBaseInfoDto对象中返回
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // mtName和stName没有，需要查询课程分类表
        // 通过courseCategoryMapper查询分类信息，将分类信息设置到courseBaseInfoDto中
        courseBaseInfoDto.setMtName(courseCategoryMapper.selectById(courseBase.getMt()).getName());
        courseBaseInfoDto.setStName(courseCategoryMapper.selectById(courseBase.getSt()).getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        // 数据合法性校验
        // 根据具体的业务逻辑去校验
        // 本机构只能修改本机构的课程
        // 拿到课程id
        Long courseId = editCourseDto.getId();
        // 查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构没有权限修改此课程");
        }
        // 封装数据
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        // 更新数据库
        int update = courseBaseMapper.updateById(courseBase);
        if (update <= 0){
            XueChengPlusException.cast("课程信息更新失败");
        }
        // 更新营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarketNew);
        courseMarketNew.setId(courseId);
        savaCourseMarket(courseMarketNew);
        // 查询课程营销信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    // 单独写一个方法保存营销信息，存在则更新，不存在则添加
    public void savaCourseMarket(CourseMarket courseMarketNew){
        // 参数的合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isEmpty(charge)){
            throw new RuntimeException("收费规则不能为空");
        }
        // 如果课程收费(201001)，价格没有填写也要抛出异常
        if (charge.equals("201001")){
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue() <= 0){
//                throw new RuntimeException("课程价格不能为空，且价格必须大于0");
                XueChengPlusException.cast("课程价格不能为空，且价格必须大于0");
            }

        }
        // 从数据库查询课程营销信息，存在则更新，不存在则添加
        Long id = courseMarketNew.getId();  // 拿到课程id
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null) {
            // 不存在则插入数据库
            int insert = courseMarketMapper.insert(courseMarketNew);
        }else {
            // 存在则更新数据库, 将courseMarketNew的属性拷贝到courseMarket中
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            // 设置id
            courseMarket.setId(courseMarket.getId());
            // 更新数据库
            int update = courseMarketMapper.updateById(courseMarket);
        }
    }

}
