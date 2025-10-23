package com.cleverse.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cleverse.ai.service.OllamaService;
import com.cleverse.ai.service.TempService;

@Controller
@RequestMapping("/temp")
public class TempController {
	 @Autowired
	 private  TempService tempService;
	 
	 
    @RequestMapping("/index")
    public String index() {
        return "index"; // JSP 파일 사용
    }

    @RequestMapping("/user")
    public void createUser(@RequestParam Object u) {
    	tempService.saveUser(u);
    }

    @RequestMapping("/user/{id}")
    public void getUser(@RequestParam Long id) {
       tempService.getUser(id);
    }

}
