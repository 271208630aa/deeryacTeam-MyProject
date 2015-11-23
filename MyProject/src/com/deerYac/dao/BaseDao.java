package com.deerYac.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

public abstract interface BaseDao
{
	public abstract Session getSession();

	public abstract void delete(String hql, Object ... values);

	public abstract void deleteAll(Collection<?> paramCollection);

	public abstract boolean deleteAll(String paramString1, String paramString2, String paramString3, String paramString4);

	public abstract boolean deleteAll(String objectName, String paramName, String sqlconnector, Object ... values);

	public abstract Serializable save(Object paramObject);

	public abstract boolean saveAll(List<?> paramList);

	public abstract boolean saveOrUpdateAll(List<?> paramList);

	public abstract void update(Object paramObject);

	public abstract void updateNotNull(Object paramObject);

	public abstract void updateNotNullAll(List<?> paramList);

	public abstract void saveOrUpdate(Object paramObject);

	public abstract <T> T findById(Class<?> objClass, Object id);

	public abstract <T> List<T> findByHql(String hql, Object... objects);

}