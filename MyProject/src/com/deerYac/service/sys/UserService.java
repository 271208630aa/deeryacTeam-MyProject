package com.deerYac.service.sys;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deerYac.bean.TSysUser;
import com.deerYac.dao.BaseDao;
import com.deerYac.util.DBUtil;

@Service
public class UserService {
	@Autowired
	private BaseDao baseService;

	public List<TSysUser> ss() {
		String hql = " from TSysUser";
		List<TSysUser> users = baseService.findByHql(hql);
		return users;
	}

	public List<TSysUser> ss2() {
		String sql = "select * from t_sys_user";
		return DBUtil.queryAllBeanList(sql, TSysUser.class);

	}
	
	public TSysUser findById(String id){
		return baseService.findById(TSysUser.class, id);
	}
}
