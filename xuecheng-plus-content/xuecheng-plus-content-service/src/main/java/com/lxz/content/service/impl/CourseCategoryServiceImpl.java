package com.lxz.content.service.impl;

import com.lxz.content.mapper.CourseCategoryMapper;
import com.lxz.content.model.dto.CourseCategoryTreeDto;
import com.lxz.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/2 下午4:35
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService{
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 调用mapper递归查询出分类信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        // 封装成List<CourseCategoryTreeDto>返回
        // 先将list转成map, key就是节点的id, value就是节点对象,目的就是方便从map获取节点对象
        // (key1, key2) -> key2)：如果key1和key2冲突，就取key2
        // filter(item -> !id.equals(item.getId())): 把根节点排除
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        // 定义一个list作为最终返回的list
        List<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        // 然后从头遍历list, 从map中获取父节点id, 如果父节点id为空, 说明是根节点, 直接添加到返回结果中
        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item ->{
            // 向list写入父节点元素
            if (item.getParentid().equals(id)) {
                courseCategoryList.add(item);
            }
            // 从map中获取父节点
            CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
            if(courseCategoryTreeDto != null) {
                if (courseCategoryTreeDto.getChildrenThreeNodes() == null) {
                    // 如果该父节点的childrenThreeNodes属性为空, 则初始化一个ArrayList
                    courseCategoryTreeDto.setChildrenThreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                // 将子节点放在父节点的childrenThreeNodes属性中
                courseCategoryTreeDto.getChildrenThreeNodes().add(item);
            }
        });
        return courseCategoryList;
    }
}
