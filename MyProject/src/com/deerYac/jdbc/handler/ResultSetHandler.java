package com.deerYac.jdbc.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.rowset.CachedRowSet;

import com.deerYac.jdbc.DBConn;
import com.sun.rowset.CachedRowSetImpl;

public class ResultSetHandler
{
  public static Class[] getUniPageRowColumnsTypes(ResultSet rs, ResultSetMetaData rsmd, int columnNum)
    throws SQLException
  {
    if ("oracle".equals(DBConn.DATABASE_TYPE))
    {
      return getOraclePageRowColumnsTypes(rs, rsmd, columnNum);
    }if ("mysql".equals(DBConn.DATABASE_TYPE))
    {
      return getMysqlPageRowColumnsTypes(rs, rsmd, columnNum);
    }if ("mssql".equals(DBConn.DATABASE_TYPE))
    {
      return new Class[0];
    }

    return new Class[0];
  }

  public static Object[] getUniPageRowColumnsValues(ResultSet rs, int columnNum)
    throws SQLException
  {
    if ("oracle".equals(DBConn.DATABASE_TYPE))
    {
      return getOraclePageRowColumnsValues(rs, columnNum);
    }if ("mysql".equals(DBConn.DATABASE_TYPE))
    {
      return getMysqlPageRowColumnsValues(rs, columnNum);
    }if ("mssql".equals(DBConn.DATABASE_TYPE))
    {
      return new Object[0];
    }

    return new Object[0];
  }

  public static Class[] getRowColumnsTypes(ResultSet rs, ResultSetMetaData rsmd, int columnNum)
    throws SQLException
  {
    Class[] columnsTypes = new Class[columnNum];

    for (int i = 0; i < columnNum; i++)
    {
      matchJavaType(rsmd, columnsTypes, i);
    }
    return columnsTypes;
  }

  public static Object[] getRowColumnsValues(ResultSet rs, int columnNum)
    throws SQLException
  {
    Object[] columns = new Object[columnNum];

    for (int i = 0; i < columnNum; i++) {
      columns[i] = rs.getObject(i + 1);
    }
    return columns;
  }

  protected static Class[] getMysqlPageRowColumnsTypes(ResultSet rs, ResultSetMetaData rsmd, int columnNum)
    throws SQLException
  {
    return getRowColumnsTypes(rs, rsmd, columnNum);
  }

  protected static Object[] getMysqlPageRowColumnsValues(ResultSet rs, int columnNum)
    throws SQLException
  {
    return getRowColumnsValues(rs, columnNum);
  }

  protected static Class[] getOraclePageRowColumnsTypes(ResultSet rs, ResultSetMetaData rsmd, int columnNum)
    throws SQLException
  {
    Class[] columnsTypes = new Class[columnNum - 1];

    int i = 0; for (int j = columnNum - 1; i < j; i++)
    {
      matchJavaType(rsmd, columnsTypes, i);
    }
    return columnsTypes;
  }

  protected static Object[] getOraclePageRowColumnsValues(ResultSet rs, int columnNum)
    throws SQLException
  {
    Object[] columns = new Object[columnNum - 1];
    int i = 0; for (int j = columnNum - 1; i < j; i++)
    {
      columns[i] = rs.getObject(i + 1);
    }
    return columns;
  }

  protected static Class[] getDb2PageRowColumnsTypes(ResultSet rs, ResultSetMetaData rsmd, int columnNum)
    throws SQLException
  {
    Class[] columnsTypes = new Class[columnNum - 1];

    int i = 1; for (int j = columnNum; i < j; i++)
    {
      matchJavaType(rsmd, columnsTypes, i);
    }
    return columnsTypes;
  }

  protected static Object[] getDb2PageRowColumnsValues(ResultSet rs, int columnNum)
    throws SQLException
  {
    Object[] columns = new Object[columnNum - 1];
    int i = 1; for (int j = columnNum; i < j; i++)
    {
      columns[i] = rs.getObject(i + 1);
    }
    return columns;
  }

  public static CachedRowSet getCachedRowSetInstance()
    throws SQLException
  {
    if ("oracle".equals(DBConn.DATABASE_TYPE))
    {
      return OracleCachedRowSetImpl.getNewInstance();
    }if ("mysql".equals(DBConn.DATABASE_TYPE))
    {
      return new CachedRowSetImpl();
    }if ("mssql".equals(DBConn.DATABASE_TYPE))
    {
      return new CachedRowSetImpl();
    }

    return new CachedRowSetImpl();
  }

  protected static void matchJavaType(ResultSetMetaData rsmd, Class[] columnsTypes, int currentIndex)
    throws SQLException
  {
    int currColumnIndex = currentIndex + 1;
    switch (rsmd.getColumnType(currColumnIndex)) {
    case 12:
      columnsTypes[currentIndex] = String.class;
      break;
    case 2:
      int scale = rsmd.getScale(currColumnIndex);
      if (scale == 0) {
        int precision = rsmd.getPrecision(currColumnIndex);
        if (precision > 9)
          columnsTypes[currentIndex] = Long.class;
        else if (precision > 4)
          columnsTypes[currentIndex] = Integer.class;
        else if (precision > 2)
          columnsTypes[currentIndex] = Short.class;
        else {
          columnsTypes[currentIndex] = Byte.class;
        }
      }
      else if (scale > 0) {
        columnsTypes[currentIndex] = Double.class;
      }
      else if (scale == -127) {
        columnsTypes[currentIndex] = Float.class;
      }
      else {
        columnsTypes[currentIndex] = Number.class;
      }
      break;
    case 91:
      columnsTypes[currentIndex] = Date.class;
      break;
    case 93:
      columnsTypes[currentIndex] = Timestamp.class;
      break;
    case -7:
      columnsTypes[currentIndex] = Short.class;
      break;
    case 4:
      columnsTypes[currentIndex] = Integer.class;
      break;
    case -5:
      columnsTypes[currentIndex] = Long.class;
      break;
    case 3:
      columnsTypes[currentIndex] = Double.class;
      break;
    case 8:
      columnsTypes[currentIndex] = Double.class;
      break;
    case 5:
      columnsTypes[currentIndex] = Short.class;
      break;
    case 6:
      columnsTypes[currentIndex] = Float.class;
      break;
    case 2004:
      columnsTypes[currentIndex] = Byte.class;
      break;
    case 16:
      columnsTypes[currentIndex] = Boolean.class;
      break;
    default:
      columnsTypes[currentIndex] = String.class;
    }
  }
}