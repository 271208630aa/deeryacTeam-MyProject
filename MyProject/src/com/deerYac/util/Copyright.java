package com.deerYac.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Copyright
{
  private static final Log LOG = LogFactory.getLog(Copyright.class);
  private static final String L = "Decompiling this copyrighted software is a violation of both your license agreement and the Digital Millenium Copyright jundu of 2009. Under section 1204 of the DMCA, penalties range up to a $200,000 fine or up to five years imprisonment for a first offense. Think about it; pay for a license, avoid prosecution, and feel better about yourself.";
  private static final String YS_LICENSE_FILE_NAME = "dele_sys_filse";
  private static final String ALLOW_LICENSE = getAllowLicenseNotMac();

  private static final ResourceBundle license_cfg = ResourceBundle.getBundle("dele_sys_filse");

  private static String licStr_mac = null;

  private static final Date validDate = DateUtils.addDays(new Date(), 45);
  private static String ys_license = license_cfg.getString("signature");
  private static final String digest = license_cfg.getString("digest");
  private static final String user = Cn2Spell.getPYString(license_cfg.getString("user"));
  private static final String message = "你所使用的系统存在垃圾数据或数据丢失的危险，请尽快联系开发商解决这个问题！";

  static
  {
    try
    {
      licStr_mac = NetWorkInfo.getMacAddress();
    } catch (IOException e) {
      LOG.error(e);
    }
    if (StringUtils.isNotBlank(ys_license)) {
      String[] license = ys_license.split("==");
      if ((license.length == 2) && (StringUtils.isNotBlank(license[1])))
        ys_license = license[1];
      else
        ys_license = null;
    }
  }

  public static void validate()
    throws Exception
  {
  }

  private static String getAllowLicenseByMac()
  {
    String licenseKey = null;
    if ((StringUtils.isNotBlank(user)) && (StringUtils.isNotBlank(licStr_mac))) {
      String need = user.substring(0, 1) + licStr_mac;
      String dx = need + "Decompiling this copyrighted software is a violation of both your license agreement and the Digital Millenium Copyright jundu of 2009. Under section 1204 of the DMCA, penalties range up to a $200,000 fine or up to five years imprisonment for a first offense. Think about it; pay for a license, avoid prosecution, and feel better about yourself." + user;
      int suf = A.decode(dx);
      String code = need + suf;
      licenseKey = "SN" + C.change(code);
    }
    return licenseKey;
  }

  private static String getAllowLicenseNotMac()
  {
    String licenseKey = null;
    if (StringUtils.isNotBlank(user)) {
      String licStr = "FOIDN-COM-0912310";
      String need = user.substring(0, 1) + licStr;
      String dx = need + "Decompiling this copyrighted software is a violation of both your license agreement and the Digital Millenium Copyright jundu of 2009. Under section 1204 of the DMCA, penalties range up to a $200,000 fine or up to five years imprisonment for a first offense. Think about it; pay for a license, avoid prosecution, and feel better about yourself." + user;
      int suf = A.decode(dx);
      String code = need + suf;
      licenseKey = "SN" + C.change(code);
    }
    return licenseKey;
  }

  public static String getDigest()
  {
    return digest;
  }

  public static String getUser() {
    return user;
  }

  public static String getMessage() {
    return "你所使用的系统存在垃圾数据或数据丢失的危险，请尽快联系开发商解决这个问题！";
  }

  public static void main(String[] args)
  {
  }

  static class A
  {
    static int decode(String s)
    {
      int i = 0;
      char[] ac = s.toCharArray();
      int j = 0;
      for (int k = ac.length; j < k; j++) {
        i = 31 * i + ac[j];
      }
      return Math.abs(i);
    }
  }

  static class C {
    static String change(String s) {
      byte[] abyte0 = s.getBytes(Charset.forName("UTF-8"));
      char[] ac = new char[s.length()];
      int i = 0;
      for (int k = abyte0.length; i < k; i++) {
        int j = abyte0[i];
        if ((j >= 48) && (j <= 57))
          j = (j - 48 + 5) % 10 + 48;
        else if ((j >= 65) && (j <= 90))
          j = (j - 65 + 13) % 26 + 65;
        else if ((j >= 97) && (j <= 122))
          j = (j - 97 + 13) % 26 + 97;
        ac[i] = ((char)j);
      }
      return String.valueOf(ac);
    }
  }
}