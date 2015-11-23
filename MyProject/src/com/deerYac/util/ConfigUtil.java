package com.deerYac.util;

import java.util.ResourceBundle;

public class ConfigUtil
{
  private static final ResourceBundle CONFIG = ResourceBundle.getBundle("db");

  public static String getString(String key)
  {
    return CONFIG.getString(key);
  }

  public static String[] getStringArray(String key)
  {
    return CONFIG.getStringArray(key);
  }

  public static ResourceBundle getConfigResourceBundle() {
    return CONFIG;
  }
}