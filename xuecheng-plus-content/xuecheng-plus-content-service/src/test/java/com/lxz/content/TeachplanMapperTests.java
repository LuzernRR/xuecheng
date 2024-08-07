package com.lxz.content;

import com.lxz.content.mapper.TeachplanMapper;
import com.lxz.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @description:课程计划mapper测试
 * @author: 12860
 * @time: 2024/8/6 下午4:14
 */
@SpringBootTest
public class TeachplanMapperTests {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    public void testSelectTreeNodes(){
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachplanDtos);
    }
}
