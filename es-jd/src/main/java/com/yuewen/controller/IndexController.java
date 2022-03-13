package com.yuewen.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/

@RestController
@RequestMapping("Jd")
public class IndexController {

    @GetMapping("/index")
    public String index(){
        return "index";
    }
}
