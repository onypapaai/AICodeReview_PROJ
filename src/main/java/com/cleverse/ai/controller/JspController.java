package com.cleverse.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jsp")
public class JspController {

    @RequestMapping("/")
    public String index() {
        return "index"; // JSP 파일 사용
    }
}
