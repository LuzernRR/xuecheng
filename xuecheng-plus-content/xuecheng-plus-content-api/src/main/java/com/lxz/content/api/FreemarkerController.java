package com.lxz.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/13 下午8:56
 */
@Controller  // 不使用@RestController，因为不需要返回json数据
public class FreemarkerController {
    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        // 指定数据模型
        modelAndView.addObject("name", "freemarker");
        // 指定模板文件名
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
