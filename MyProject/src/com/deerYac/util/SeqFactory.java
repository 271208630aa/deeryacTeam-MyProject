package com.deerYac.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;

public class SeqFactory
{
  private static final int maxRandomNum = 99999;
  private static final int mixRandomNum = 10000;
  private static AtomicInteger randomAppeadNum = new AtomicInteger(10000);
  private static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");

  public static final synchronized String getNewSequenceAlone()
  {
    randomAppeadNum.compareAndSet(99999, 10000);
    int rand = randomAppeadNum.incrementAndGet();
    String mDateTime = DateFormat.format(Calendar.getInstance().getTime());
    String seq = mDateTime + rand;
    return seq;
  }

  public static final String getNewTreeIdByParentId(String tableName, String nodeColumnName, String parentColumnName, String parentid)
    throws Exception
  {
    String sql = "select max(" + nodeColumnName + ") from " + tableName + " where " + parentColumnName + " = ? ";
    String maxNodeid = null; String nodeid = null;
    maxNodeid = (String)DBUtil.queryFieldValue(sql, new Object[] { parentid });
    if (StringUtils.isBlank(maxNodeid))
    {
      nodeid = parentid + "001";
    }
    else {
      String kk = maxNodeid.substring(maxNodeid.length() - 3);
      int c = Integer.parseInt(kk) + 1;
      if (c <= 999) {
        if (c < 10)
          kk = "00" + c;
        else if (c < 100)
          kk = "0" + c;
        else
          kk = String.valueOf(c);
      }
      else {
        throw new Exception("There are no id resource !");
      }
      nodeid = maxNodeid.substring(0, maxNodeid.length() - 3) + kk;
    }
    return nodeid;
  }
}