package com.deerYac.jdbc.handler;

import com.deerYac.jdbc.DBConn;
import com.deerYac.util.Pager;

public class PageHandler
{
  public static String convert2PagedSQL(Pager pager, String sql)
  {
    if ("mysql".equals(DBConn.DATABASE_TYPE))
      return convert2MysqlPagedSQL(pager, sql);
    if ("oracle".equals(DBConn.DATABASE_TYPE))
      return convert2OraclePagedSQL(pager, sql);
    if ("mssql".equals(DBConn.DATABASE_TYPE)) {
      return convert2MssqlPagedSQL(pager, sql);
    }
    return sql;
  }

  public static String convert2MysqlPagedSQL(Pager pager, String sql)
  {
    int startInt = (pager.getCurrentPageno() - 1) * pager.getEachPageRows();
    int pageSize = pager.getEachPageRows();
    sql = sql + " limit " + startInt + "," + pageSize;
    return sql;
  }

  public static String convert2OraclePagedSQL(Pager pager, String sql)
  {
    int startInt = (pager.getCurrentPageno() - 1) * pager.getEachPageRows();
    int endInt = pager.getCurrentPageno() * pager.getEachPageRows();
    sql = "SELECT * FROM (SELECT ROW_.*,ROWNUM RN FROM ( " + 
      sql + 
      ") ROW_ ) WHERE RN > " + startInt + " AND RN <= " + endInt;
    return sql;
  }

  public static String convert2OraclePagedSQLAsOrder(Pager pager, String sql)
  {
    int startInt = (pager.getCurrentPageno() - 1) * pager.getEachPageRows();
    int endInt = pager.getCurrentPageno() * pager.getEachPageRows();
    sql = "SELECT * FROM (SELECT ROW_.*,ROWNUM RN FROM ( " + 
      sql + 
      ") ROW_ WHERE ROWNUM <= " + endInt + ") WHERE RN > " + startInt;
    return sql;
  }

  public static String convert2Db2PagedSQL(Pager pager, String sql, String orderby)
  {
    int startInt = (pager.getCurrentPageno() - 1) * pager.getEachPageRows();
    int endInt = pager.getCurrentPageno() * pager.getEachPageRows();
    String[] sqls = sql.toLowerCase().split("select");
    sql = "SELECT * FROM (SELECT ROWNUMBER() OVER(ORDER BY " + orderby + ") AS ROWNUM, " + 
      sqls[1] + 
      ") WHERE ROWNUM BETWEEN " + startInt + " AND " + endInt;
    return sql;
  }

  public static String convert2MssqlPagedSQL(Pager pager, String sql)
  {
    return sql;
  }
}