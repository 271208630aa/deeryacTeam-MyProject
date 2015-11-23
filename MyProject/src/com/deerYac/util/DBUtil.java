package com.deerYac.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import com.deerYac.jdbc.DBConn;
import com.deerYac.jdbc.handler.PageHandler;
import com.deerYac.jdbc.handler.ResultSetHandler;
import com.deerYac.jdbc.handler.StoredProcedureHandler;

public final class DBUtil {
	private static final Log LOG = LogFactory.getLog(DBUtil.class);

	public static int executeOneoffSQL(String sql) {
		int updatedRows = -1;
		if (SpringUtils.getJdbcTemplate() != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("SpringUtils.getJdbcTemplate() execute >>> " + sql);
			}
			updatedRows = SpringUtils.getJdbcTemplate().update(sql);
		} else {
			LOG.info(sql);
			Connection con = DBConn.getConnection();
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				updatedRows = stmt.executeUpdate(sql);
			} catch (SQLException e) {
				LOG.error(sql, e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, null, stmt, con);
			}
		}
		return updatedRows;
	}

	public static int executeSQL(String sql, Object[] in_params) {
		int updatedRows = -1;
		if (SpringUtils.getJdbcTemplate() != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("SpringUtils.getJdbcTemplate() execute >>> " + sql);
			}
			in_params = deleteNull(in_params);
			updatedRows = SpringUtils.getJdbcTemplate().update(sql, in_params);
		} else {
			LOG.info(sql);
			Connection con = DBConn.getConnection();
			PreparedStatement pstmt = null;
			try {
				pstmt = con.prepareStatement(sql);
				if (in_params != null) {
					int i = 0;
					for (int j = in_params.length; i < j; i++) {
						if (in_params[i] != null) {
							pstmt.setObject(i + 1, in_params[i]);
						}
					}
				}
				updatedRows = pstmt.executeUpdate();
			} catch (SQLException e) {
				LOG.error(sql, e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, null, pstmt, con);
			}
		}
		return updatedRows;
	}

	public static int[] executeBatch(String sql, List<Object[]> values) {
		int[] updatedRows = (int[]) null;
		if (SpringUtils.getJdbcTemplate() != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("SpringUtils.getJdbcTemplate() execute >>> " + sql);
			}
			updatedRows = SpringUtils.getJdbcTemplate()
					.batchUpdate(sql, values);
		} else {
			LOG.info(sql);
			Connection con = DBConn.getConnection();
			boolean autoCommit = false;
			PreparedStatement pstmt = null;
			try {
				autoCommit = con.getAutoCommit();
				con.setAutoCommit(false);
				pstmt = con.prepareStatement(sql);
				int i = 0;
				for (int j = values.size(); i < j; i++) {
					Object[] row = (Object[]) values.get(i);
					for (int k = 0; k < row.length; k++) {
						pstmt.setObject(k + 1, row[k] == null ? "" : row[k]);
					}
					pstmt.addBatch();
				}
				updatedRows = pstmt.executeBatch();
				con.commit();
			} catch (SQLException e) {
				LOG.error("DBUtil error", e);
				try {
					con.rollback();
				} catch (SQLException e1) {
					LOG.error("事务回滚失败！", e);
				}
				throw new RuntimeException(e);
			} finally {
				try {
					con.setAutoCommit(autoCommit);
				} catch (SQLException e) {
					LOG.error("恢复AutoCommit值失败！", e);
				}
				close(LOG, null, pstmt, con);
			}
		}
		return updatedRows;
	}

	public static int[] executeBatch(List<String> statements) {
		if ((statements == null) || (statements.isEmpty())) {
			return null;
		}
		int[] updatedRows = (int[]) null;
		if (SpringUtils.getJdbcTemplate() != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("SpringUtils.getJdbcTemplate() execute >>> "
						+ (String) statements.get(0) + " <<< ...");
			}
			updatedRows = SpringUtils.getJdbcTemplate().batchUpdate(
					(String[]) statements.toArray(new String[0]));
		} else {
			Connection con = DBConn.getConnection();
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				for (int i = 0; i < statements.size(); i++) {
					stmt.addBatch((String) statements.get(i));
				}
				updatedRows = stmt.executeBatch();
			} catch (SQLException e) {
				LOG.error(e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, null, stmt, con);
			}
		}
		return updatedRows;
	}

	private static ResultSet queryData(PreparedStatement pstmt, Object... params)
			throws SQLException {
		if ((params != null) && (params.length > 0)) {
			int i = 0;
			for (int j = params.length; i < j; i++) {
				if (params[i] != null) {
					pstmt.setObject(i + 1, params[i]);
				}
			}
		}

		return pstmt.executeQuery();
	}

	public static ResultSet queryRowSet(String sql, Object... params) {
		if (LOG.isInfoEnabled()) {
			LOG.info(sql);
		}

		CachedRowSet crs = null;
		if (SpringUtils.getJdbcTemplate() == null) {
			Connection conn = DBConn.getConnection();
			ResultSet rs = null;
			PreparedStatement pstmt = null;
			try {
				crs = ResultSetHandler.getCachedRowSetInstance();
				pstmt = conn.prepareStatement(sql);
				rs = queryData(pstmt, params);
				crs.populate(rs);
			} catch (SQLException e) {
				LOG.error(sql + "\n" + e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, rs, pstmt, conn);
			}
		} else {
			params = deleteNull(params);
			crs = (CachedRowSet) SpringUtils.getJdbcTemplate().query(sql,
					new ResultSetExtractor() {
						public CachedRowSet extractData(ResultSet rs)
								throws SQLException, DataAccessException {
							CachedRowSet crs = ResultSetHandler
									.getCachedRowSetInstance();
							crs.populate(rs);
							return crs;
						}
					}, params);
		}

		return crs;
	}

	public static List<Object[]> queryAllList(String sql, Object... params) {
		return queryPageList(null, sql, params);
	}

	public static List<Object[]> queryPageList(Pager pager, String sql,
			Object... params) {
		if (pager != null) {
			sql = PageHandler.convert2PagedSQL(pager, sql);
		}

		List list = null;
		ResultSet rs = queryRowSet(sql, params);
		try {
			rs.last();
			int size = rs.getRow();

			if (size < 1) {
				close(LOG, rs, null, null);
				return new ArrayList(0);
			}
			list = new ArrayList(size);
			rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnNum = rsmd.getColumnCount();

			while (rs.next()) {
				Object[] bean = (Object[]) null;
				if (pager != null)
					bean = ResultSetHandler.getUniPageRowColumnsValues(rs,
							columnNum);
				else {
					bean = ResultSetHandler.getRowColumnsValues(rs, columnNum);
				}
				list.add(bean);
			}
		} catch (SQLException e) {
			LOG.error(sql + "\n" + e);
			throw new RuntimeException(e);
		} finally {
			close(LOG, rs, null, null);
		}
		close(LOG, rs, null, null);

		return list;
	}

	public static List<Map<String, String>> queryForList(String sql,
			Object... params) {
		return queryForList(null, sql, params);
	}

	public static List<Map<String, String>> queryForList(Pager pager,
			String sql, Object... params) {
		if (pager != null) {
			sql = PageHandler.convert2PagedSQL(pager, sql);
		}
		List list = new ArrayList();
		ResultSet rs = queryRowSet(sql, params);
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] name = new String[count];
			for (int i = 0; i < count; i++) {
				name[i] = rsmd.getColumnLabel(i + 1);
				if (StringUtils.isBlank(name[i])) {
					name[i] = rsmd.getColumnName(i + 1);
				}
				name[i] = name[i].toUpperCase();
			}
			while (rs.next()) {
				Map tempmap = new HashMap();
				for (int i = 0; i < name.length; i++) {
					String tempstr = name[i];
					tempmap.put(tempstr.toLowerCase(), rs.getString(tempstr));
				}
				list.add(tempmap);
			}
		} catch (SQLException e) {
			LOG.error(sql + "\n" + e);
			throw new RuntimeException(e);
		} finally {
			close(LOG, rs, null, null);
		}
		return list;
	}

	public static Object[] queryRowColumns(String sql, Object... params) {
		Object[] columns = (Object[]) null;
		ResultSet rs = queryRowSet(sql, params);
		try {
			rs.last();
			int size = rs.getRow();

			if (size < 1) {
				close(LOG, rs, null, null);
				return null;
			}
			if (size > 1) {
				throw new RuntimeException("超出方法能力，查询的结果集大于一行记录！");
			}

			rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnNum = rsmd.getColumnCount();

			if (rs.next())
				columns = ResultSetHandler.getRowColumnsValues(rs, columnNum);
		} catch (SQLException e) {
			LOG.error(sql);
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			close(LOG, rs, null, null);
		}
		close(LOG, rs, null, null);

		return columns;
	}

	public static <T> List<T> queryAllBeanList(String sql, Class<T> beanClass,
			Object... params) throws RuntimeException {
		return queryPageBeanList(null, sql, beanClass, params);
	}

	public static <T> List<T> queryPageBeanList(Pager pager, String sql,
			Class<T> beanClass, Object[] params) {
		if (LOG.isInfoEnabled()) {
			LOG.info(sql);
		}
		List results;
		if (pager != null)
			sql = PageHandler.convert2PagedSQL(pager, sql);
		if (SpringUtils.getJdbcTemplate() == null) {
			Connection conn = DBConn.getConnection();
			QueryRunner qr = new QueryRunner();
			try {
				results = (List) qr.query(conn, sql, new BeanListHandler(
						beanClass), params);
			} catch (SQLException e) {
				LOG.error(sql);
				LOG.error(e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, null, null, conn);
			}
		} else {
			params = deleteNull(params);
			results = SpringUtils.getJdbcTemplate().query(sql,
					ParameterizedBeanPropertyRowMapper.newInstance(beanClass),
					params);
		}

		return results;
	}

	public static <T> T queryBean(String sql, Class<T> beanClass,
			Object[] params) {
		if (LOG.isInfoEnabled()) {
			LOG.info(sql);
		}

		Object bean = null;

		if (SpringUtils.getJdbcTemplate() == null) {
			Connection conn = DBConn.getConnection();
			QueryRunner qr = new QueryRunner();
			try {
				bean = qr.query(conn, sql, new BeanHandler(beanClass), params);
			} catch (SQLException e) {
				LOG.error(sql);
				LOG.error(e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, null, null, conn);
			}
		} else {
			params = deleteNull(params);
			try {
				bean = SpringUtils.getJdbcTemplate().queryForObject(
						sql,
						ParameterizedBeanPropertyRowMapper
								.newInstance(beanClass), params);
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
		}

		return (T) bean;
	}

	public static Object queryFieldValue(String sql, Object... params) {
		if (LOG.isInfoEnabled()) {
			LOG.info(sql);
		}

		Object object = null;
		if (SpringUtils.getJdbcTemplate() == null) {
			Connection conn = DBConn.getConnection();
			ResultSet rs = null;
			PreparedStatement pstmt = null;
			try {
				pstmt = conn.prepareStatement(sql);
				rs = queryData(pstmt, params);
				if (rs.next())
					object = rs.getObject(1);
			} catch (SQLException e) {
				LOG.error(sql + "\n" + e);
				throw new RuntimeException(e);
			} finally {
				close(LOG, rs, pstmt, conn);
			}
		} else {
			params = deleteNull(params);
			try {
				object = SpringUtils.getJdbcTemplate().queryForObject(sql,
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								return rs.getObject(1);
							}
						}, params);
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
		}

		return object;
	}

	public static int count(String sql, Object... params) {
		if ((sql == null) || (sql.trim().equals(""))) {
			throw new RuntimeException(
					"期望是select count(*) from ...查询语句，实际传入了空字符串！");
		}

		if (sql.trim().toUpperCase().startsWith("FROM")) {
			sql = "SELECT COUNT(*) " + sql;
		}
		if (!sql.toUpperCase().contains("COUNT(")) {
			throw new RuntimeException(sql
					+ " 不是有效的select count(*) from ...查询语句！");
		}
		int rowsCount = 0;
		if (SpringUtils.getJdbcTemplate() == null) {
			Object val = queryFieldValue(sql, params);
			String s_val = String.valueOf(val);
			if (s_val != null)
				rowsCount = Integer.parseInt(s_val);
		} else {
			params = deleteNull(params);
			rowsCount = SpringUtils.getJdbcTemplate().queryForInt(sql, params);
		}
		return rowsCount;
	}

	public static long countForLong(String sql, Object... params) {
		Integer ct = Integer.valueOf(count(sql, params));
		return ct.longValue();
	}

	public static void close(Log targetlog, ResultSet rs, Statement stmt,
			Connection conn) {
		if (targetlog == null)
			targetlog = LOG;
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			targetlog.error(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				targetlog.error(e);
			} finally {
				DBConn.close(conn);
			}
		}
	}

	public static String spellSqlWhere(String columnName, String sqlconnector,
			Object[] values) {
		StringBuffer where = new StringBuffer(" (");
		for (Object value : values) {
			if ("=".equals(sqlconnector)) {
				if ((value instanceof String))
					where.append(columnName).append(" ").append(sqlconnector)
							.append(" '").append(value).append("' or ");
				else
					where.append(columnName).append(" ").append(sqlconnector)
							.append(" ").append(value).append(" or ");
			} else if ("like".equalsIgnoreCase(sqlconnector)) {
				where.append(columnName).append(" ").append(sqlconnector)
						.append(" '").append(value).append("%' or ");
			}
		}
		where.delete(where.lastIndexOf(" or"), where.length());
		return ") ";
	}

	public static <T> List<T> callProcQueryGetBeans(String proceduceName,
			Class<T> beanClass, Object... in_params) {
		return StoredProcedureHandler.execProcQueryGetBeans(proceduceName,
				beanClass, in_params);
	}

	public static List<Object[]> callProcQueryGetList(String proceduceName,
			Object... in_params) {
		return StoredProcedureHandler.execProcQueryGetList(proceduceName,
				in_params);
	}

	public static String callProcQueryString(String proceduceName,
			Object... in_params) throws SQLException {
		return StoredProcedureHandler.execProcQueryString(proceduceName,
				in_params);
	}

	@Deprecated
	public static Object callProcQueryObject(String proceduceName,
			Object... in_params) throws SQLException {
		return StoredProcedureHandler.execProcQueryObject(proceduceName,
				in_params);
	}

	public static void callProcNoneQuery(String proceduceName, Object... params)
			throws SQLException {
		StoredProcedureHandler.execProcNoneQuery(proceduceName, params);
	}

	protected static Object[] deleteNull(Object[] params) {
		ArrayList args = new ArrayList();
		Object[] arrayOfObject = params;
		int j = params.length;
		for (int i = 0; i < j; i++) {
			Object o = arrayOfObject[i];
			if (o != null) {
				args.add(o);
			}
		}
		return args.toArray();
	}
}