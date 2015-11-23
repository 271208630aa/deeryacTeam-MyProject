package com.deerYac.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * TSysUser entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "t_sys_user")
public class TSysUser implements java.io.Serializable {

	// Fields

	private String id;
	private String name;
	private Integer age;
	private Integer sex;
	private String email;
	private String password;
	private Integer usertype;
	private String departid;

	// Constructors

	/** default constructor */
	public TSysUser() {
	}

	/** minimal constructor */
	public TSysUser(String id) {
		this.id = id;
	}

	/** full constructor */
	public TSysUser(String id, String name, Integer age, Integer sex,
			String email, String password, Integer usertype, String departid) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.sex = sex;
		this.email = email;
		this.password = password;
		this.usertype = usertype;
		this.departid = departid;
	}

	// Property accessors
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 50)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "name", length = 300)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "age")
	public Integer getAge() {
		return this.age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Column(name = "sex")
	public Integer getSex() {
		return this.sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	@Column(name = "email", length = 100)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "password", length = 50)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "usertype")
	public Integer getUsertype() {
		return this.usertype;
	}

	public void setUsertype(Integer usertype) {
		this.usertype = usertype;
	}

	@Column(name = "departid", length = 50)
	public String getDepartid() {
		return this.departid;
	}

	public void setDepartid(String departid) {
		this.departid = departid;
	}

	@Override
	public String toString() {
		return "TSysUser [id=" + id + ", name=" + name + ", age=" + age + ", sex=" + sex + ", email=" + email + ", password=" + password + ", usertype=" + usertype + ", departid=" + departid + "]";
	}

}