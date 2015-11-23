package com.deerYac.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SysPropertiesUtil {
	private static final Log LOG = LogFactory.getLog(SysPropertiesUtil.class);

	private static final Map<String, String> sysProps = new HashMap();

	public static String get(String csmc) {
		if (sysProps.isEmpty()) {
			loadSysProps();
		}
		return (String) sysProps.get(csmc);
	}

	public static boolean getBoolean(String csmc) {
		String cs = get(csmc);
		return "true".equalsIgnoreCase(cs);
	}

	public static int getInteger(String csmc) {
		String cs = get(csmc);
		int intVal = 0;
		if (StringUtils.isNotBlank(cs)) {
			try {
				intVal = Integer.parseInt(cs);
			} catch (NumberFormatException e) {
				LOG.error(e);
			}
		}
		return intVal;
	}

	public static void loadSysProps() {
		ResultSet rs;
		String sql = "select csmc, cs from t_sys_propertity ";
		try {
			rs = DBUtil.queryRowSet(sql, new Object[0]);
		} catch (RuntimeException e) {

			throw new RuntimeException("PropConfigUtil:查询系统参数数据失败！"
					+ e.getMessage());
		}
		if (!sysProps.isEmpty())
			sysProps.clear();
		try {
			while (rs.next())
				sysProps.put(rs.getString(1), rs.getString(2));
		} catch (SQLException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				LOG.error(e);
			}
		}
	}
}