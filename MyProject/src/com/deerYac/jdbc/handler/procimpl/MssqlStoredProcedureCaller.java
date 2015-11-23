package com.deerYac.jdbc.handler.procimpl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.deerYac.jdbc.DBConn;
import com.deerYac.jdbc.handler.ResultSetHandler;
import com.deerYac.util.DBUtil;

public class MssqlStoredProcedureCaller
{
  private static final Log LOG = LogFactory.getLog(MssqlStoredProcedureCaller.class);

  public static <T> List<T> execProcQueryGetBeans(String proceduceName, Class<T> beanClass, Object[] in_params)
  {
    return null;
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
    CachedRowSet crs = ResultSetHandler.getCachedRowSetInstance();
    Object result = execProcQueryObject(proceduceName, in_params);
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

  protected static List<Object[]> execMysqlProcQueryGetList(String proceduceName, Object[] in_params)
  {
    ResultSet rs = null;
    ArrayList list = new ArrayList();
    try
    {
      rs = execUniProcQueryGetLastRs(proceduceName, new Object[] { Boolean.valueOf(false), in_params });
      ResultSetMetaData rsmd = rs.getMetaData();
      int columncount = rsmd.getColumnCount();
      while (rs.next()) {
        Object[] bean = ResultSetHandler.getRowColumnsValues(rs, columncount);
        list.add(bean);
      }
    } catch (SQLException e) {
      LOG.error(e);
    } finally {
      DBUtil.close(LOG, rs, null, null);
    }

    return list;
  }

  protected static ResultSet execUniProcQueryGetLastRs(String proceduceName, Object[] in_params)
    throws SQLException
  {
    Connection conn = null;
    CallableStatement cstmt = null;
    CachedRowSet crs = null;
    ResultSet rs = null;
    int updateCount = -1;

    String procStr = "{call " + proceduceName + " }";
    try
    {
      conn = DBConn.getConnection();
      cstmt = conn.prepareCall(procStr);
      setProcParams(cstmt, in_params);

      cstmt.execute();
      do {
        updateCount = cstmt.getUpdateCount();
        if (updateCount != -1) {
          cstmt.getMoreResults();
        }
        else
        {
          while (cstmt.getMoreResults()) {
            rs = cstmt.getResultSet();
            if (rs != null) {
              crs = ResultSetHandler.getCachedRowSetInstance();
              crs.populate(rs);
            }
          }
        }

      }

      while ((updateCount != -1) || (rs != null));
    }
    catch (SQLException e) {
      LOG.error(e);
      throw new SQLException(e.getMessage());
    } finally {
      DBUtil.close(LOG, rs, cstmt, conn);
    }
    return crs;
  }

  protected static ResultSet execUniProcQueryGetFirstRS(String proceduceName, boolean getLastRs, Object[] in_params)
    throws SQLException
  {
    Connection conn = null;
    CallableStatement cstmt = null;
    CachedRowSet crs = null;
    ResultSet rs = null;

    String procStr = "{call " + proceduceName + " }";

    label204: 
    try { conn = DBConn.getConnection();
      cstmt = conn.prepareCall(procStr);
      setProcParams(cstmt, in_params);

      cstmt.execute();
      if (cstmt.getUpdateCount() == -1) {
        rs = cstmt.getResultSet();
        if (rs != null) {
          crs = ResultSetHandler.getCachedRowSetInstance();
          crs.populate(rs); break label204;
        }
      } else {
        do {
          rs = cstmt.getResultSet();
          if (rs != null) {
            crs = ResultSetHandler.getCachedRowSetInstance();
            crs.populate(rs);
            if (!getLastRs)
              break label204;
          }
          if (!cstmt.getMoreResults()) break;  } while (cstmt.getUpdateCount() == -1);
      }

    }
    catch (SQLException e)
    {
      LOG.error(e);
      throw new SQLException(e.getMessage());
    } finally {
      DBUtil.close(LOG, rs, cstmt, conn);
    }
    return crs;
  }

  private static void setProcParams(CallableStatement cstmt, Object[] params)
    throws SQLException
  {
    if (params != null)
      for (int i = 0; i < params.length; i++)
        if (params[i] != null)
          cstmt.setString(i + 1, params[i].toString());
  }
}