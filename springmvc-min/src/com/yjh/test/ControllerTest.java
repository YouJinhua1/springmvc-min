package com.yjh.test;

import com.yjh.annotation.Controller;
import com.yjh.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class ControllerTest {
	
	@RequestMapping("/hello")
	public String Hello(){
		System.out.println("�M��Controller�Č��������У�");
		return "hello";
	}
}
