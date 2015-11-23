package com.deerYac.jdbc.handler;

import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;

import oracle.jdbc.rowset.OracleCachedRowSet;

public class OracleCachedRowSetImpl
{
  public static final CachedRowSet getNewInstance()
    throws SQLException
  {
    return new OracleCachedRowSet();
  }
}