package com.deerYac.util;

import java.io.UnsupportedEncodingException;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Cn2Spell
{
  public static final String toFullPinYin(String hanzhis)
  {
    CharSequence s = hanzhis;

    char[] hanzhi = new char[s.length()];
    for (int i = 0; i < s.length(); i++) {
      hanzhi[i] = s.charAt(i);
    }

    char[] t1 = hanzhi;
    String[] t2 = (String[])null;

    HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
    t3.setCaseType(HanyuPinyinCaseType.UPPERCASE);
    t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    t3.setVCharType(HanyuPinyinVCharType.WITH_V);

    int t0 = t1.length;
    StringBuffer py = new StringBuffer();
    try {
      for (int i = 0; i < t0; i++)
        if (String.valueOf(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
          t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
          py.append(t2[0].toString());
        }
    }
    catch (BadHanyuPinyinOutputFormatCombination e)
    {
      e.printStackTrace();
    }

    return py.toString().trim();
  }

  public static final String getPYString(String str)
  {
    if ((str == null) || (str.replaceAll(" ", "").equals(""))) {
      return "";
    }
    str = str.replaceAll(" ", "");
    StringBuffer tempStr = new StringBuffer("");
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if ((c >= '!') && (c <= '~'))
        tempStr.append(String.valueOf(c));
      else {
        tempStr.append(getPYChar(String.valueOf(c)));
      }
    }
    return tempStr.toString();
  }

  public static final String getPYChar(String c)
  {
    byte[] array = (byte[])null;
    try {
      array = String.valueOf(c).getBytes("GBK");
    } catch (UnsupportedEncodingException e) {
      array = new byte[2];
      e.printStackTrace();
    }
    int i = (short)(array[0] - 0 + 256) * 256 + (short)(array[1] - 0 + 256);
    if (i < 45217) return "*";
    if (i < 45253) return "a";
    if (i < 45761) return "b";
    if (i < 46318) return "c";
    if (i < 46826) return "d";
    if (i < 47010) return "e";
    if (i < 47297) return "f";
    if (i < 47614) return "g";
    if (i < 48119) return "h";
    if (i < 49062) return "j";
    if (i < 49324) return "k";
    if (i < 49896) return "l";
    if (i < 50371) return "m";
    if (i < 50614) return "n";
    if (i < 50622) return "o";
    if (i < 50906) return "p";
    if (i < 51387) return "q";
    if (i < 51446) return "r";
    if (i < 52218) return "s";
    if (i < 52698) return "t";
    if (i < 52980) return "w";
    if (i < 53689) return "x";
    if (i < 54481) return "y";
    if (i < 55290) return "z";
    return "*";
  }

  public static void main(String[] args)
  {
    System.out.println(getPYString("军务处"));
  }
}