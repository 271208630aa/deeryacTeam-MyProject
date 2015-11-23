package com.deerYac.jdbc;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.deerYac.util.ConfigUtil;
import com.deerYac.util.SpringUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public final class DBConn {
	private static final Log log = LogFactory.getLog(DBConn.class);

	private static final ResourceBundle DB_CONFIG = ConfigUtil
			.getConfigResourceBundle();

	public static final String DATABASE_TYPE = DB_CONFIG
			.getString("database_type");

	private static final String DATA_SOURCE_TYPE = DB_CONFIG
			.getString("dataSource_type");
	private static final String DATASOURCE_TYPE_PROXOOL = "proxool";
	private static final String DATASOURCE_TYPE_C3P0 = "c3p0";
	private static final String DATASOURCE_TYPE_JNDI = "jndi";
	private static final String DATASOURCE_TYPE_JDBC = "jdbc";
	private static final String DATASOURCE_TYPE_DRUID = "druid";
	private static Context context = null;
	private static DataSource dataSource = null;

	private static String DEFAULT_POOL_NAME = "DataSource";
	private static String DEFAULT_JNDI_NAME = null;

	static {
		if (DATASOURCE_TYPE_JNDI.equals(DATA_SOURCE_TYPE)) {
			String jndi_name = DB_CONFIG.getString("jndi_name");
			if ((jndi_name != null) && (!"".equals(jndi_name.trim()))) {
				if (!jndi_name.startsWith("java:comp/env/")) {
					jndi_name = "java:comp/env/" + jndi_name;
				}
				DEFAULT_JNDI_NAME = jndi_name;
				try {
					context = new InitialContext();
					dataSource = (DataSource) context.lookup(DEFAULT_JNDI_NAME);
				} catch (NamingException e) {
					log.error("数据库连接异常：" + e.toString());
				}
			} else {
				log.error("数据库连接异常：请在数据库配置文件dbconfig中指定jndi_name！");
			}
		} else if (DATASOURCE_TYPE_C3P0.equals(DATA_SOURCE_TYPE)) {
			DEFAULT_POOL_NAME = DB_CONFIG.getString("c3p0_alias");
			ComboPooledDataSource c3p0DataSource;
			if ((StringUtils.isBlank(DEFAULT_POOL_NAME))
					|| ("Default".equalsIgnoreCase(DEFAULT_POOL_NAME)))
				c3p0DataSource = new ComboPooledDataSource();
			else
				c3p0DataSource = new ComboPooledDataSource(DEFAULT_POOL_NAME);
			try {
				c3p0DataSource.setDriverClass(DB_CONFIG.getString("db.driver"));
			} catch (PropertyVetoException e) {
				log.error(e);
			}
			c3p0DataSource.setJdbcUrl(DB_CONFIG.getString("db.url"));
			c3p0DataSource.setUser(DB_CONFIG.getString("db.username"));
			c3p0DataSource.setPassword(DB_CONFIG.getString("db.password"));
			c3p0DataSource.setMinPoolSize(Integer.valueOf(DB_CONFIG.getString("db.c3p0.minPoolSize")).intValue());
			c3p0DataSource.setAcquireIncrement(Integer.valueOf(DB_CONFIG.getString("db.c3p0.acquireIncrement")).intValue());
			c3p0DataSource.setMaxPoolSize(Integer.valueOf(DB_CONFIG.getString("db.c3p0.maxPoolSize")).intValue());
			c3p0DataSource.setInitialPoolSize(Integer.valueOf(DB_CONFIG.getString("db.c3p0.initialPoolSize")).intValue());
			c3p0DataSource.setTestConnectionOnCheckin(Boolean.valueOf(DB_CONFIG.getString("db.c3p0.testConnectionOnCheckin")).booleanValue());
			c3p0DataSource.setPreferredTestQuery(DB_CONFIG.getString("db.c3p0.preferredTestQuery"));
			c3p0DataSource.setMaxIdleTime(Integer.valueOf(DB_CONFIG.getString("db.c3p0.maxIdleTime")).intValue());
			c3p0DataSource.setIdleConnectionTestPeriod(Integer.valueOf(DB_CONFIG.getString("db.c3p0.idleConnectionTestPeriod")).intValue());
			c3p0DataSource.setCheckoutTimeout(Integer.valueOf(DB_CONFIG.getString("db.c3p0.checkoutTimeout")).intValue());
			c3p0DataSource.setNumHelperThreads(Integer.valueOf(DB_CONFIG.getString("db.c3p0.numHelperThreads")).intValue());
			dataSource = c3p0DataSource;
		} else if (DATASOURCE_TYPE_PROXOOL.equals(DATA_SOURCE_TYPE)) {
			DEFAULT_POOL_NAME = "proxool." + DB_CONFIG.getString("proxool_alias");
			try {
				Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
			} catch (ClassNotFoundException e) {
				log.error(e);
			}
		} else if (DATASOURCE_TYPE_DRUID.equals(DATA_SOURCE_TYPE)) {
			DEFAULT_POOL_NAME = DB_CONFIG.getString("druid_alias");
			DruidDataSource druidDataSource = new DruidDataSource();
			druidDataSource.setDriverClassName(DB_CONFIG.getString("db.driver"));
			druidDataSource.setUrl(DB_CONFIG.getString("db.url"));
			druidDataSource.setUsername(DB_CONFIG.getString("db.username"));
			druidDataSource.setPassword(DB_CONFIG.getString("db.password"));
			try {
				druidDataSource.setFilters(DB_CONFIG.getString("druid.filters"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			druidDataSource.setValidationQuery(DB_CONFIG.getString("druid.validationQuery"));

			try {
				boolean logAbandoned = convertBoolean(DB_CONFIG.getString("druid.logAbandoned"));
				druidDataSource.setLogAbandoned(logAbandoned);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				boolean removeAbandoned = convertBoolean(DB_CONFIG.getString("druid.removeAbandoned"));
				druidDataSource.setRemoveAbandoned(removeAbandoned);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				boolean testWhileIdle = convertBoolean(DB_CONFIG.getString("druid.testWhileIdle"));
				druidDataSource.setTestWhileIdle(testWhileIdle);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				boolean testOnReturn = convertBoolean(DB_CONFIG.getString("druid.testOnReturn"));
				druidDataSource.setTestOnReturn(testOnReturn);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				long timeBetweenEvictionRunsMillis = convertLong(DB_CONFIG.getString("druid.timeBetweenEvictionRunsMillis"));
				druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				long minEvictableIdleTimeMillis = convertLong(DB_CONFIG.getString("druid.minEvictableIdleTimeMillis"));
				druidDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				long maxWait = convertLong(DB_CONFIG.getString("druid.maxWait"));
				druidDataSource.setMaxWait(maxWait);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				int removeAbandonedTimeout = convertInt(DB_CONFIG.getString("druid.removeAbandonedTimeout"));
				druidDataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				int maxOpenPreparedStatements = convertInt(DB_CONFIG.getString("druid.maxOpenPreparedStatements"));
				druidDataSource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				int minIdle = convertInt(DB_CONFIG.getString("druid.minIdle"));
				druidDataSource.setMinIdle(minIdle);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				int initialSize = convertInt(DB_CONFIG.getString("druid.initialSize"));
				druidDataSource.setInitialSize(initialSize);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				int maxActive = convertInt(DB_CONFIG.getString("druid.maxActive"));
				druidDataSource.setMaxActive(maxActive);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			dataSource = druidDataSource;
		}
	}

	/**
	 * String 转换 boolean
	 * 
	 * @param bool
	 * @return
	 * @throws Exception
	 */
	public static boolean convertBoolean(String bool) throws Exception {
		if (StringUtils.isNotBlank(bool)) {
			return Boolean.parseBoolean(bool);
		}
		throw new Exception("boolean值转换错误（数据库配置）");
	}

	public static long convertLong(String l) throws NumberFormatException {
		if (StringUtils.isNotBlank(l)) {
			return Long.parseLong(l);
		}
		throw new NumberFormatException("long值转换错误（数据库配置）");
	}

	public static int convertInt(String l) throws NumberFormatException {
		if (StringUtils.isNotBlank(l)) {
			return Integer.parseInt(l);
		}
		throw new NumberFormatException("int值转换错误（数据库配置）");
	}

	public static Connection getConnection() {
		Connection conn = null;
		if (DATASOURCE_TYPE_JNDI.equals(DATA_SOURCE_TYPE)) {
			conn = getConnByJNDI();
		} else if (DATASOURCE_TYPE_C3P0.equals(DATA_SOURCE_TYPE)) {
			conn = getConnByC3p0();
		} else if (DATASOURCE_TYPE_PROXOOL.equals(DATA_SOURCE_TYPE)) {
			conn = getConnByProxool();
		} else if (DATASOURCE_TYPE_JDBC.equals(DATA_SOURCE_TYPE)) {
			String driver = DB_CONFIG.getString("db.driver");
			String url = DB_CONFIG.getString("db.url");
			String user = DB_CONFIG.getString("db.username");
			String password = DB_CONFIG.getString("db.password");
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(url, user, password);
			} catch (ClassNotFoundException e) {
				log.error("数据库连接异常：" + e);
			} catch (SQLException e) {
				log.error("数据库连接异常：" + e);
			}
		}else if(DATASOURCE_TYPE_DRUID.equals(DATA_SOURCE_TYPE)){
			conn = getConnByDruid();
		}
		if (conn == null) {
			log.fatal("Get db connection failed!");
			throw new RuntimeException("Get db connection failed!");
		}
		return conn;
	}

	public static Connection getConnByC3p0() {
		Connection conn;
		try {
			if (SpringUtils.getJdbcTemplate() != null) {
				conn = SpringUtils.getJdbcTemplate().getDataSource()
						.getConnection();
			} else if (dataSource != null) {
				try {
					conn = dataSource.getConnection();
				} catch (SQLException e) {
					log.error("数据库连接异常：" + e);
					throw new RuntimeException("数据库连接异常：" + e);
				}
			} else {
				log.error("数据库连接异常：dataSource为null，c3p0没有被正确实例化！");
				throw new RuntimeException(
						"数据库连接异常：dataSource为null，c3p0没有被正确实例化！");
			}
		} catch (SQLException e) {
			log.error("数据库连接异常：" + e);
			throw new RuntimeException("数据库连接异常：" + e);
		}
		return conn;
	}
	
	public static Connection getConnByDruid() {
		Connection conn;
		try {
			if (SpringUtils.getJdbcTemplate() != null) {
				conn = SpringUtils.getJdbcTemplate().getDataSource()
						.getConnection();
			} else if (dataSource != null) {
				try {
					conn = dataSource.getConnection();
				} catch (SQLException e) {
					log.error("数据库连接异常：" + e);
					throw new RuntimeException("数据库连接异常：" + e);
				}
			} else {
				log.error("数据库连接异常：dataSource为null，druid没有被正确实例化！");
				throw new RuntimeException(
						"数据库连接异常：dataSource为null，druid没有被正确实例化！");
			}
		} catch (SQLException e) {
			log.error("数据库连接异常：" + e);
			throw new RuntimeException("数据库连接异常：" + e);
		}
		return conn;
	}

	public static Connection getConnByProxoolAlias(String poolAlias) {
		Connection conn;
		try {
			if (SpringUtils.getJdbcTemplate() != null)
				conn = SpringUtils.getJdbcTemplate().getDataSource()
						.getConnection();
			else
				conn = DriverManager.getConnection(poolAlias);
		} catch (SQLException e) {
			log.error("数据库连接异常：" + e);
			throw new RuntimeException("数据库连接异常：" + e);
		}
		return conn;
	}

	protected static Connection getConnByProxool() {
		return getConnByProxoolAlias(DEFAULT_POOL_NAME);
	}

	protected static Connection getConnByJNDI() {
		Connection conn;
		try {
			if (SpringUtils.getJdbcTemplate() != null) {
				conn = SpringUtils.getJdbcTemplate().getDataSource()
						.getConnection();
			} else if (dataSource != null) {
				try {
					conn = dataSource.getConnection();
				} catch (SQLException e) {
					log.error("数据库连接异常：" + e);
					throw new RuntimeException("数据库连接异常：" + e);
				}
			} else {
				log.error("数据库连接异常：不存在JNDI名称为" + DEFAULT_JNDI_NAME
						+ "的数据库连接上下文变量ds！");
				throw new RuntimeException("数据库连接异常：不存在JNDI名称为"
						+ DEFAULT_JNDI_NAME + "的数据库连接上下文变量ds！");
			}
		} catch (SQLException e) {
			log.error("数据库连接异常：" + e);
			throw new RuntimeException("数据库连接异常：" + e);
		}
		return conn;
	}

	public static void close(Connection conn) {
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("=============关闭数据库连接出错，错误原因：" + e);
				try {
					if (!conn.isClosed())
						conn.close();
				} catch (SQLException e2) {
					log.error("=============尝试再次关闭数据库连接出错，错误原因：" + e2);
				}
			}
	}

	public static String getDEFAULT_POOL_NAME() {
		return DEFAULT_POOL_NAME;
	}

	public static void setDEFAULT_POOL_NAME(String default_pool_name) {
		DEFAULT_POOL_NAME = default_pool_name;
	}
}