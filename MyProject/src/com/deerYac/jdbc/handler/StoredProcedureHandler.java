package com.deerYac.jdbc.handler;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.deerYac.jdbc.DBConn;
import com.deerYac.jdbc.handler.procimpl.MssqlStoredProcedureCaller;
import com.deerYac.jdbc.handler.procimpl.MysqlStoredProcedureCaller;
import com.deerYac.jdbc.handler.procimpl.OracleStoredProcedureCaller;

public class StoredProcedureHandler
{
  private static final Log LOG = LogFactory.getLog(StoredProcedureHandler.class);

  public static <T> List<T> execProcQueryGetBeans(String proceduceName, Class<T> beanClass, Object[] in_params)
  {
    if ("oracle".equals(DBConn.DATABASE_TYPE))
    {
      return OracleStoredProcedureCaller.execProcQueryGetBeans(proceduceName, beanClass, in_params);
    }
    if ("mysql".equals(DBConn.DATABASE_TYPE))
    {
      return MysqlStoredProcedureCaller.execProcQueryGetBeans(proceduceName, beanClass, in_params);
    }
    if ("mssql".equals(DBConn.DATABASE_TYPE))
    {
      return MssqlStoredProcedureCaller.execProcQueryGetBeans(proceduceName, beanClass, in_params);
    }

    return Collections.emptyList();
  }

  public static List<Object[]> execProcQueryGetList(String proceduceName, Object[] in_params)
  {
    if ("oracle".equals(DBConn.DATABASE_TYPE))
    {
      return OracleStoredProcedureCaller.execProcQueryGetList(proceduceName, in_params);
    }
    if ("mysql".equals(DBConn.DATABASE_TYPE))
    {
      return MysqlStoredProcedureCaller.execProcQueryGetList(proceduceName, in_params);
    }
    if ("mssql".equals(DBConn.DATABASE_TYPE))
    {
      return MssqlStoredProcedureCaller.execProcQueryGetList(proceduceName, in_params);
    }

    return Collections.emptyList();
  }

  public static String execProcQueryString(String proceduceName, Object[] in_params)
    throws SQLException
  {
    String res = (String)execProcQueryObject(proceduceName, in_params);
    return res;
  }

  public static Object execProcQueryObject(String proceduceName, Object[] in_params)
    throws SQLException
  {
    if ("oracle".equals(DBConn.DATABASE_TYPE))
    {
      return OracleStoredProcedureCaller.execProcQueryObject(proceduceName, in_params);
    }
    if ("mysql".equals(DBConn.DATABASE_TYPE))
    {
      return MysqlStoredProcedureCaller.execProcQueryObject(proceduceName, in_params);
    }
    if ("mssql".equals(DBConn.DATABASE_TYPE))
    {
      return MssqlStoredProcedureCaller.execProcQueryObject(proceduceName, in_params);
    }

    return ""; } 
  // ERROR //
  public static void execProcNoneQuery(String proceduceName, Object[] params) throws SQLException { // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: aconst_null
    //   3: astore_3
    //   4: new 95	java/lang/StringBuilder
    //   7: dup
    //   8: ldc 97
    //   10: invokespecial 99	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   13: aload_0
    //   14: invokevirtual 102	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   17: ldc 106
    //   19: invokevirtual 102	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   22: invokevirtual 108	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   25: astore 4
    //   27: invokestatic 112	com/apexedu/framework/jdbc/DBConn:getConnection	()Ljava/sql/Connection;
    //   30: astore_2
    //   31: aload_2
    //   32: aload 4
    //   34: invokeinterface 116 2 0
    //   39: astore_3
    //   40: aload_3
    //   41: aload_1
    //   42: invokestatic 122	com/apexedu/framework/jdbc/handler/StoredProcedureHandler:setProcParams	(Ljava/sql/CallableStatement;[Ljava/lang/Object;)V
    //   45: aload_3
    //   46: dup
    //   47: astore 5
    //   49: monitorenter
    //   50: aload_3
    //   51: invokeinterface 126 1 0
    //   56: pop
    //   57: aload 5
    //   59: monitorexit
    //   60: goto +46 -> 106
    //   63: aload 5
    //   65: monitorexit
    //   66: athrow
    //   67: astore 5
    //   69: getstatic 16	com/apexedu/framework/jdbc/handler/StoredProcedureHandler:LOG	Lorg/apache/commons/logging/Log;
    //   72: aload 5
    //   74: invokeinterface 132 2 0
    //   79: new 81	java/sql/SQLException
    //   82: dup
    //   83: aload 5
    //   85: invokevirtual 138	java/sql/SQLException:getMessage	()Ljava/lang/String;
    //   88: invokespecial 141	java/sql/SQLException:<init>	(Ljava/lang/String;)V
    //   91: athrow
    //   92: astore 6
    //   94: getstatic 16	com/apexedu/framework/jdbc/handler/StoredProcedureHandler:LOG	Lorg/apache/commons/logging/Log;
    //   97: aconst_null
    //   98: aload_3
    //   99: aload_2
    //   100: invokestatic 142	com/apexedu/framework/util/DBUtil:close	(Lorg/apache/commons/logging/Log;Ljava/sql/ResultSet;Ljava/sql/Statement;Ljava/sql/Connection;)V
    //   103: aload 6
    //   105: athrow
    //   106: getstatic 16	com/apexedu/framework/jdbc/handler/StoredProcedureHandler:LOG	Lorg/apache/commons/logging/Log;
    //   109: aconst_null
    //   110: aload_3
    //   111: aload_2
    //   112: invokestatic 142	com/apexedu/framework/util/DBUtil:close	(Lorg/apache/commons/logging/Log;Ljava/sql/ResultSet;Ljava/sql/Statement;Ljava/sql/Connection;)V
    //   115: return
    //
    // Exception table:
    //   from	to	target	type
    //   50	60	63	finally
    //   63	66	63	finally
    //   27	67	67	java/sql/SQLException
    //   27	92	92	finally
	  } 
  public static void setProcParams(CallableStatement cstmt, Object[] params) throws SQLException { if (params != null)
      for (int i = 0; i < params.length; i++)
        if (params[i] != null)
          cstmt.setString(i + 1, params[i].toString());
  }
}