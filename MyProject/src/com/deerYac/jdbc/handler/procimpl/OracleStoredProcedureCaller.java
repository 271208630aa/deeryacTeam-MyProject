package com.deerYac.jdbc.handler.procimpl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.deerYac.jdbc.DBConn;
import com.deerYac.jdbc.handler.ResultSetHandler;
import com.deerYac.util.DBUtil;

public class OracleStoredProcedureCaller
{
  private static final Log LOG = LogFactory.getLog(OracleStoredProcedureCaller.class);

  public static <T> List<T> execProcQueryGetBeans(String proceduceName, Class<T> beanClass, Object[] in_params)
  {
    ResultSet rs = null;
    List list = null;
    try {
      rs = execProcQueryResultSet(proceduceName, in_params);
      list = new BeanListHandler(beanClass).handle(rs);
    } catch (SQLException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    } finally {
      DBUtil.close(LOG, rs, null, null);
    }
    if (list == null) {
      list = Collections.emptyList();
    }
    return list;
  }

  public static List<Object[]> execProcQueryGetList(String proceduceName, Object[] in_params)
  {
    ResultSet rs = null;
    ArrayList list = new ArrayList();
    ResultSetMetaData rsmd;
    try
    {
      rs = execProcQueryResultSet(proceduceName, in_params);
      rsmd = rs.getMetaData();
      int columncount = rsmd.getColumnCount();
      while (rs.next()) {
        Object[] bean = ResultSetHandler.getRowColumnsValues(rs, columncount);
        list.add(bean);
      }
    } catch (SQLException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    } finally {
      DBUtil.close(LOG, rs, null, null);
    }

    return list;
  }

  public static String execProcQueryString(String proceduceName, Object[] in_params)
    throws SQLException
  {
    Object result = execProcQueryObject(proceduceName, in_params);

    return result.toString();
  }

  public static ResultSet execProcQueryResultSet(String proceduceName, Object[] in_params)
    throws SQLException
  {
    Object result = execProcQueryObject(proceduceName, in_params);
    CachedRowSet crs = ResultSetHandler.getCachedRowSetInstance();
    if ((result instanceof ResultSet)) {
      ResultSet rs = (ResultSet)result;
      crs.populate(rs);
    }

    return crs;
  }

  public static Object execProcQueryObject(String proceduceName, Object[] in_params)
    throws SQLException
  {
    Connection conn = null;
    CallableStatement proc = null;
    Object result = null;

    String procStr = "{call " + proceduceName + " }";
    try
    {
      conn = DBConn.getConnection();
      proc = conn.prepareCall(procStr);
      if (in_params != null) {
        for (int i = 0; i < in_params.length; i++) {
          if (in_params[i] != null) {
            proc.setString(i + 1, in_params[i].toString());
          }
        }
        proc.registerOutParameter(in_params.length + 1, 12);
      }

      synchronized (proc) {
        proc.execute();
      }
      if (in_params != null)
        result = proc.getObject(in_params.length + 1);
    }
    catch (SQLException e)
    {
      LOG.error(e);
      throw new SQLException(e.getMessage());
    } finally {
      DBUtil.close(LOG, null, proc, conn);
    }
    return result;
  }
}