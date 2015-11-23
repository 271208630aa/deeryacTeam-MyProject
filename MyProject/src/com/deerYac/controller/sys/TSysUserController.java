package com.deerYac.controller.sys;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.deerYac.bean.TSysUser;
import com.deerYac.service.sys.UserService;

@Controller
@RequestMapping(value="/user")
public class TSysUserController {
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/list")
	public String list(Model model){
		List<TSysUser> users =  userService.ss2();
		model.addAttribute("data", users);
		return "user/list.jsp";
	}
	
	/**
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/load")
	public String load(Model model){
		TSysUser  sysUser = userService.findById("1");
		model.addAttribute("obj", sysUser);
		return "user/entity.jsp";
		
	}
	
}
