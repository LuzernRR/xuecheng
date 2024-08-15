package com.lxz.content.api;

import com.lxz.content.model.dto.SaveTeachplanDto;
import com.lxz.content.model.dto.TeachplanDto;
import com.lxz.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lxz.media.model.dto.BindTeachplanMediaDto;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/6 下午3:22
 */
@Api(value = "课程计划编辑接口", tags = "课程计划管理")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;
    @ApiOperation("查询课程计划树形结构")
    // 查询课程计划 GET /teachplan/22/tree_nodes
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        return teachplanTree;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }
}
