package com.cleverse.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

    @RequestMapping("/")
    public String home() {
        return "redirect:/main";
    }

    @RequestMapping("/main")
    public String main() {
        return "index";
    }
}
