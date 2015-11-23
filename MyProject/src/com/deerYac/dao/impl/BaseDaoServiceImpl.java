package com.deerYac.dao.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.deerYac.dao.BaseDao;

@Service("baseService")
public final class BaseDaoServiceImpl implements BaseDao {
	private static final Logger log = LoggerFactory.getLogger(BaseDaoServiceImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private SessionFactory sessionFactory;

	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public void delete(String hql, Object... values) {
		executeHql(hql, values);
	}

	/**
	 * 删除实体集合
	 * 
	 * @param objects
	 *            对象集合
	 */
	@Override
	public void deleteAll(Collection<?> objects) {
		if (objects != null && objects.size() > 0) {
			Session session = getSession();
			for (Object entity : objects) {
				session.delete(entity);
			}
		}
	}

	/**
	 * 删除
	 * 
	 * @param 对象名称
	 * @param 参数名称
	 * @param 值连接符
	 * @param 参数使用
	 *            “,”（英文逗号）分隔开
	 */
	@Override
	public boolean deleteAll(String objectName, String paramName, String sqlconnector, String values) {
		if ((paramName == null) || (sqlconnector == null) || (values == null)
				|| (values.length() < 1)) {
			return false;
		}
		return deleteAll(objectName, paramName, sqlconnector, values.split(","));
	}

	/**
	 * 删除
	 * 
	 * @param 对象名称
	 * @param 参数名称
	 * @param 值连接符
	 * @param 参数集合
	 */
	@Override
	public boolean deleteAll(String objectName, String paramName, String sqlconnector, Object... values) {
		if ((paramName == null) || (sqlconnector == null) || (values == null)
				|| (values.length < 1)) {
			return false;
		}
		StringBuilder hql = new StringBuilder("delete ");
		hql.append(objectName).append(" where ");
		for (Object value : values) {
			if ("=".equals(sqlconnector))
				hql.append(paramName).append(" ").append(sqlconnector)
						.append(" '").append(value).append("' or ");
			else if ("like".equalsIgnoreCase(sqlconnector))
				hql.append(paramName).append(" ").append(sqlconnector)
						.append(" '").append(value).append("%' or ");
			else {
				return false;
			}
		}
		hql.delete(hql.lastIndexOf(" or "), hql.length());

		if (log.isInfoEnabled()) {
			log.info(hql.toString());
		}
		delete(hql.toString(), new Object[0]);
		return true;
	}

	/**
	 * 保存实体
	 */
	@Override
	public Serializable save(Object object) {
		log.debug("saving Object instance");
		Serializable l;
		try {
			l = getSession().save(object);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
		return l;
	}

	@Override
	public boolean saveAll(List<?> objects) {
		int batchSize = 50;
		if ((objects != null) && (!objects.isEmpty())) {
			Session session = getSession();
			Transaction tx = session.getTransaction();

			boolean newTrans = false;
			if (tx == null) {
				tx = session.beginTransaction();
				newTrans = true;
			}
			try {
				FlushMode oldFlushMode = session.getFlushMode();
				session.setFlushMode(FlushMode.AUTO);
				int i = 0;
				for (int k = objects.size(); i < k; i++) {
					session.save(objects.get(i));
					if ((i % batchSize == 0) || (i == k - 1)) {
						session.flush();
						session.clear();
					}
				}
				if (newTrans) {
					tx.commit();
				}
				session.setFlushMode(oldFlushMode);
			} catch (HibernateException e) {
				tx.rollback();
				log.error(e.toString());
			}
		}

		return true;
	}

	@Override
	public boolean saveOrUpdateAll(List<?> objects) {
		int batchSize = 50;
		if ((objects != null) && (!objects.isEmpty())) {
			Session session = getSession();
			Transaction tx = session.getTransaction();

			boolean newTrans = false;
			if (tx == null) {
				tx = session.beginTransaction();
				newTrans = true;
			}
			try {
				FlushMode oldFlushMode = session.getFlushMode();
				session.setFlushMode(FlushMode.AUTO);
				int i = 0;
				for (int k = objects.size(); i < k; i++) {
					session.saveOrUpdate(objects.get(i));
					if ((i % batchSize == 0) || (i == k - 1)) {
						session.flush();
						session.clear();
					}
				}
				if (newTrans) {
					tx.commit();
				}
				session.setFlushMode(oldFlushMode);
			} catch (HibernateException e) {
				tx.rollback();
				log.error(e.toString());
			}
		}

		return true;
	}

	@Override
	public void update(Object object) {
		log.debug("updating Object instance");
		try {
			getSession().update(object);
			log.debug("update successful");
		} catch (RuntimeException re) {
			log.error("update failed", re);
			throw re;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void updateNotNull(Object entity) {
		if (entity == null) {
			throw new RuntimeException("被更新的EntityBean为null！");
		}
		SessionFactory sf = getSession().getSessionFactory();
		ClassMetadata cm = sf.getClassMetadata(entity.getClass());
		String idProperty = cm.getIdentifierPropertyName();
		if (idProperty == null) {
			throw new RuntimeException("此动态更新方法不支持没有主键属性的EntityBean！");
		}

		Object idPropertyValue = getPojoPropertyValue(entity, idProperty);
		String entityName = cm.getEntityName();
		String[] properties = cm.getPropertyNames();

		List values = new LinkedList();

		StringBuilder hql = new StringBuilder("update ").append(entityName);
		hql.append(" set ");

		for (String property : properties) {
			Object propertyValue = cm.getPropertyValue(entity, property);
			if (propertyValue != null) {
				hql.append(property).append("=").append("?, ");
				values.add(propertyValue);
			}
		}
		values.add(idPropertyValue);
		hql.deleteCharAt(hql.length() - 2);
		hql.append("where ").append(idProperty).append("=?");
		if (log.isInfoEnabled())
			log.info("update object with not null properties ===>>> " + hql);
		try {
			executeHql(hql.toString(), values.toArray());
		} catch (RuntimeException re) {
			log.error(hql.toString());
			log.error("above hql execute failed", re);
			throw re;
		}
	}

	/**
	 * 更新列表中实体非空属性
	 */
	@Override
	public void updateNotNullAll(List<?> objects) {
		if ((objects == null) || (objects.size() > 50)) {
			throw new RuntimeException("不能执行此操作：集合为空或者待更新的EntityBean数量大于50个！");
		}
		int i = 0;
		for (int k = objects.size(); i < k; i++) {
			Object entity = objects.get(i);
			updateNotNull(entity);
		}
	}

	/**
	 * 保存或更新
	 */
	@Override
	public void saveOrUpdate(Object object) {
		log.debug("saving Object instance");
		try {
			getSession().saveOrUpdate(object);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("saveOrUpdate failed", re);
			throw re;
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T findById(Class<?> objClass, Object id) {
		return (T) getSession().get(objClass, (Serializable) id);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> List<T> findByHql(String sql, Object... para) {
		Query query = getSession().createQuery(sql);
		if (para != null && para.length > 0) {
			int size = para.length;
			for (int i = 0; i < size; i++) {
				if (para[i] != null) {
					if ((para[i] instanceof String)) {
						String new_name = (String) para[i];
						query.setString(i, new_name);
					} else if ((para[i] instanceof Integer)) {
						Integer new_name = (Integer) para[i];
						query.setInteger(i, new_name.intValue());
					} else if ((para[i] instanceof BigDecimal)) {
						BigDecimal new_name = (BigDecimal) para[i];
						query.setBigDecimal(i, new_name);
					} else if ((para[i] instanceof Date)) {
						Date new_name = (Date) para[i];
						query.setTimestamp(i, new_name);
					} else if ((para[i] instanceof Map)) {
						Map map = (Map) para[i];
						String paramName = (String) map.keySet()
								.toArray()[0];
						Object[] params = (Object[]) map.get(paramName);
						query.setParameterList(paramName, params);
					} else {
						query.setParameter(i, para[i]);
					}
				} else {
					query.setString(i, null);
				}
			}
		}
		List<T> list = query.list();
		return list;
	}

	/**
	 * 执行hql语句
	 * 
	 * @param hql
	 * @param 参数集合
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public int executeHql(final String hql, final Object... para) {
		Query query = getSession().createQuery(hql);
		if (para != null) {
			for (int i = 0; i < para.length; i++) {
				if (para[i] != null) {
					if ((para[i] instanceof String)) {
						String new_name = (String) para[i];
						query.setString(i, new_name);
					} else if ((para[i] instanceof Integer)) {
						Integer new_name = (Integer) para[i];
						query.setInteger(i, new_name.intValue());
					} else if ((para[i] instanceof BigDecimal)) {
						BigDecimal new_name = (BigDecimal) para[i];
						query.setBigDecimal(i, new_name);
					} else if ((para[i] instanceof Date)) {
						Date new_name = (Date) para[i];
						query.setTimestamp(i, new_name);
					} else if ((para[i] instanceof Map)) {
						Map map = (Map) para[i];
						String paramName = (String) map.keySet()
								.toArray()[0];
						Object[] params = (Object[]) map.get(paramName);
						query.setParameterList(paramName, params);
					} else {
						query.setParameter(i, para[i]);
					}
				}
			}
		}
		int i = query.executeUpdate();
		return Integer.valueOf(i);
	}

	public static Object getPojoPropertyValue(Object entity, String propertyName) {
		Object value = null;
		try {
			value = BeanUtils.getPropertyDescriptor(entity.getClass(), propertyName);
		} catch (BeansException e) {
			e.printStackTrace();
		}
		return value;
	}
}