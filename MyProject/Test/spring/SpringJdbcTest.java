package spring;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.deerYac.bean.TSysUser;
import com.deerYac.util.DBUtil;
import com.deerYac.util.MD5;


public class SpringJdbcTest {

	private ApplicationContext ctx = null;
	private JdbcTemplate jdbcTemplate;
//	private SysUserDao userDao;
	//private SysTestDao testDao;
	
	{
		ctx = new ClassPathXmlApplicationContext("spring-db.xml");
		jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbc");
	//	testDao = ctx.getBean(SysTestDao.class);
	}
	
	@Test
	public void testDataSource() throws SQLException{
		DataSource dataSource= ctx.getBean(DataSource.class);
		System.out.println(dataSource.getConnection());
	}
	
	@Test
	public void testUserDao(){
	//	TSysUser user  =  userDao.findById("1");
	//	System.out.println(user);
	}
	
	/**
	 * 批量插入
	 * @throws SQLException
	 */
	@Test
	public void insertInto() throws SQLException{
		String sql = "insert into test.t_sys_user (id,name, age, sex, email, password, usertype, departid) values (?,?,?,?,?,?,?,?)";
		List<Object[]> args = new ArrayList<Object[]>();
		args.add(new Object[]{"1","张三",11,1,"111@gmail.com",MD5.ecodeByMD5("123456"),"1","320001"});
		args.add(new Object[]{"2","李四",21,1,"121@gmail.com",MD5.ecodeByMD5("123456"),"1","320001"});
		args.add(new Object[]{"3","王五",12,1,"131@gmail.com",MD5.ecodeByMD5("123456"),"2","320001"});
		args.add(new Object[]{"4","露露",13,1,"141@gmail.com",MD5.ecodeByMD5("123456"),"2","320001"});
		args.add(new Object[]{"5","周萌",44,2,"151@gmail.com",MD5.ecodeByMD5("123456"),"1","320001"});
		args.add(new Object[]{"6","惠立峰",32,2,"161@gmail.com",MD5.ecodeByMD5("123456"),"1","320001"});
		args.add(new Object[]{"7","唐嫣",29,2,"171@gmail.com",MD5.ecodeByMD5("123456"),"2","320001"});
		args.add(new Object[]{"8","李易峰",20,2,"181@gmail.com",MD5.ecodeByMD5("123456"),"2","320001"});
		
		jdbcTemplate.batchUpdate(sql, args);
	}
	
	/**
	 * 查询单个对象
	 */
	@Test
	public void queryForObject(){
		String sql = "select id,name, age, sex, email, password, usertype, departid from t_sys_user where id = ? ";
		RowMapper<TSysUser> mapper = new BeanPropertyRowMapper<TSysUser>(TSysUser.class);
		TSysUser user =  jdbcTemplate.queryForObject(sql, mapper,1);
		System.out.println(user);
		
	}
	
	/**
	 * 查询对象集合
	 */
	@Test
	public void queryForList(){
		String sql = "select id,name, age, sex, email, password, usertype, departid from t_sys_user where id > ? ";
		RowMapper<TSysUser> mapper = new BeanPropertyRowMapper<TSysUser>(TSysUser.class);
		List<TSysUser> users =  jdbcTemplate.query(sql, mapper,1);
		System.out.println(users);
	}
	
	@Test
	public void queryForListByDButil(){
		String sql = "select id,name, age, sex, email, password, usertype, departid from t_sys_user where id > ? ";
		List<TSysUser> users = DBUtil.queryAllBeanList(sql, TSysUser.class, 1);
		System.out.println(users);
	}
	
	@Test
	public void queryForInt(){
		String sql = "select count(1) c from t_sys_user where id > ? ";
		long c =  jdbcTemplate.queryForObject(sql, Long.class,1);
		System.out.println(c);
	}
	
	@Test
	public void dbTest(){
		String sql = "select count(1) c from t_sys_user where id > ? ";
		long c = DBUtil.count(sql, 1);
		System.out.println(c);
	}
	
	@Test
	public void fff(){
		System.out.println(MD5.ecodeByMD5("123"));
	}

	/*@Test
	public void ss(){
		String hql = " from TSysUser";
		//Query query =  getSession().createQuery(hql);
		List<TSysUser> lsit = query.list();
		//List<TSysUser> users =  bd.findByHql(hql, new Object[]{});
		System.out.println(lsit);
	}*/
	
	@Test
	public void testUserDao2(){
	//	List<TSysUser> users = testDao.ss();
	//	System.out.println(users);
	}
}
