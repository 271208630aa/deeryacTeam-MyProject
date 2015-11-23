package com.deerYac.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.deerYac.dao.BaseDao;

public class OldSource extends HibernateDaoSupport{
	private static final Log log = LogFactory.getLog(OldSource.class);

	@SuppressWarnings("unchecked")
	public  <T> T findById(Class<?> objClass, Object id) {
		return (T) getHibernateTemplate().get(objClass, (Serializable) id);
	}

	public <T> T findBean(String hql, Object[] para) {
		List list = findByHql(hql, para);
		Object bean = null;
		if (!list.isEmpty()) {
			bean = list.get(0);
		}
		return (T) bean;
	}

	public <T> List<T> findByHqlWithSecondCache(boolean useSecondCache,
			String hql, Object[] paras) {
		return findPageByHqlWithSecondCache(useSecondCache, hql, null, paras);
	}

	public <T> List<T> findByHql(String hql, Object[] para) {
		return findByHqlWithSecondCache(false, hql, para);
	}

	public <T> List<T> findPageByHql(String counthql, String resulthql,
			Pager pager, Object[] para) {
		long totalRows = count(counthql, para);
		pager.setTotalRows(totalRows);
		List list = findPageByHql(resulthql, pager, para);
		return list;
	}

	public <T> List<T> findPageByHql(String hql, Pager pager, Object[] para) {
		return findPageByHqlWithSecondCache(false, hql, pager, para);
	}

	@SuppressWarnings("deprecation")
	private <T> List<T> findPageByHqlWithSecondCache(
			final boolean useSecondCache, final String hql, final Pager pager,
			final Object[] para) {
		List list = getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				List list = null;
				if ((pager == null) || (pager.getTotalRows() > 0L)) {
					Query query = session.createQuery(hql);
					query.setCacheable(useSecondCache);
					query.setReadOnly(true);

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
									Object[] params = (Object[]) map
											.get(paramName);
									query.setParameterList(paramName, params);
								} else {
									query.setParameter(i, para[i]);
								}
							}
						}
					}
					if (pager != null) {
						query.setFirstResult((pager.getCurrentPageno() - 1)
								* pager.getEachPageRows());
						query.setMaxResults(pager.getEachPageRows());
					}
					list = query.list();
				} else {
					list = Collections.emptyList();
				}

				return list;
			}
		});
		return list;
	}

	public long count(String hql, Object[] params) {
		long rowscount = 0L;
		if (StringUtils.isBlank(hql)) {
			throw new RuntimeException(
					"期望是select count(*) from ...查询语句，实际传入了空字符串！");
		}
		if (hql.trim().toUpperCase().startsWith("FROM")) {
			hql = "select count(*) " + hql;
		}

		Object object = findFieldValue(hql, params);
		if (object != null) {
			rowscount = Long.parseLong(object.toString());
		}
		return rowscount;
	}

	public Object findFieldValue(String hql, Object[] params) {
		Object object = null;
		List list = findByHql(hql, params);
		if ((list != null) && (!list.isEmpty())) {
			object = list.get(0);
		}
		return object;
	}

	public void delete(String hql, Object[] para) {
		if (log.isDebugEnabled())
			log.debug(hql);
		try {
			executeHql(hql, para);
		} catch (RuntimeException e) {
			log.error("delete failed : " + hql);
			log.error(e);
			throw e;
		}
	}

	public void deleteAll(Collection<?> objects) {
		getHibernateTemplate().deleteAll(objects);
	}

	public boolean deleteAll(String objectName, String paramName,
			String sqlconnector, String values) {
		if ((paramName == null) || (sqlconnector == null) || (values == null)
				|| (values.length() < 1)) {
			return false;
		}

		return deleteAll(objectName, paramName, sqlconnector, values.split(","));
	}

	public boolean deleteAll(String objectName, String paramName,
			String sqlconnector, Object[] values) {
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
			log.info(hql);
		}

		delete(hql.toString(), new Object[0]);

		return true;
	}

	@SuppressWarnings("unchecked")
	public int executeHql(final String hql, final Object[] para) {
		Object s = getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(hql);
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
		});
		return Integer.parseInt(s.toString());
	}

	public Serializable save(Object object) {
		log.debug("saving Object instance");
		Serializable l;
		try {
			l = getHibernateTemplate().save(object);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
		return l;
	}

	public void update(Object object) {
		log.debug("updating Object instance");
		try {
			getHibernateTemplate().update(object);
			log.debug("update successful");
		} catch (RuntimeException re) {
			log.error("update failed", re);
			throw re;
		}
	}

	public void updateNotNull(Object entity) {
		if (entity == null) {
			throw new RuntimeException("被更新的EntityBean为null！");
		}
		SessionFactory sf = getHibernateTemplate().getSessionFactory();
		ClassMetadata cm = sf.getClassMetadata(entity.getClass());
		String idProperty = cm.getIdentifierPropertyName();
		if (idProperty == null) {
			throw new RuntimeException("此动态更新方法不支持没有主键属性的EntityBean！");
		}

		Object idPropertyValue = getPojoPropertyValue(entity, idProperty);
		String entityName = cm.getEntityName();
		String[] properties = cm.getPropertyNames();

		List values = new LinkedList();

		StringBuffer hql = new StringBuffer("update ").append(entityName);
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

	public static Object getPojoPropertyValue(Object entity, String propertyName) {
		Object value = null;
		try {
			value = BeanUtils.getPropertyDescriptor(entity.getClass(),
					propertyName);
		} catch (BeansException e) {
			e.printStackTrace();
		}
		return value;
	}

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

				if (newTrans)
					tx.commit();
				session.setFlushMode(oldFlushMode);
			} catch (HibernateException e) {
				tx.rollback();
				log.error(e);
			} finally {
				releaseSession(session);
			}

		}

		return true;
	}

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
				if (newTrans)
					tx.commit();
				session.setFlushMode(oldFlushMode);
			} catch (HibernateException e) {
				tx.rollback();
				log.error(e);
			} finally {
				releaseSession(session);
			}
		}

		return true;
	}

	public void saveOrUpdate(Object object) {
		log.debug("saving Object instance");
		try {
			getHibernateTemplate().saveOrUpdate(object);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("saveOrUpdate failed", re);
			throw re;
		}
	}

	public Object merge(Object detachedInstance) {
		log.debug("merging Object instance");
		try {
			Object result = getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachClean(Object instance) {
		log.debug("attaching clean Object instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public Session getCurrentSession() {
		return getSession();
	}
}