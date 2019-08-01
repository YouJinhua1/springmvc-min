package com.yjh.test;

import com.yjh.annotation.Controller;
import com.yjh.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class ControllerTest {
	
	@RequestMapping("/hello")
	public String Hello(){
		System.out.println("進入Controller的對應方法中！");
		return "hello";
	}
}
