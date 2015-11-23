package com.deerYac.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

import com.deerYac.dao.BaseDao;

public class SpringUtils
  implements ApplicationContextAware
{
  private static final Log LOG = LogFactory.getLog(SpringUtils.class);

  private static ApplicationContext applicationContext = null;
  private static BaseDao baseDao = null;
  private static JdbcTemplate jdbcTemplate = null;
  private static CacheManager cacheManager = null;

  public void setApplicationContext(ApplicationContext arg0)
    throws BeansException
  {
    applicationContext = arg0;
  }

  public static Object getBean(String beanid) {
    if (applicationContext == null) {
      return null;
    }
    return applicationContext.getBean(beanid);
  }

  public static BaseDao getBaseDao() {
    if (baseDao == null) {
      Object o = getBean("baseDao");
      if (o != null) baseDao = (BaseDao)o;
    }
    return baseDao;
  }

  public static JdbcTemplate getJdbcTemplate() {
    if (jdbcTemplate == null) {
      try {
        Object o = getBean("jdbcTemplate");
        jdbcTemplate = (JdbcTemplate)o;
      } catch (NoSuchBeanDefinitionException e) {
        LOG.warn("Spring 环境中未配置 JdbcTemplate，推荐配置 JdbcTemplate 和 hibernateTemplate ：：：" + e);
      }
    }
    return jdbcTemplate;
  }

  public static CacheManager getCacheManager()
  {
    if (cacheManager == null) {
      Object o = getBean("ehCacheManager");
      if (o != null) cacheManager = (CacheManager)o;
    }
    return cacheManager;
  }
}