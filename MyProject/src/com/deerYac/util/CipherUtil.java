package com.deerYac.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CipherUtil
{
  private static final Log log = LogFactory.getLog(CipherUtil.class);
  public static final String ENCODE_TYPE = "UTF8";
  public static final String KEY_TYPE_AES = "AES";
  public static final String DEFAULT_GLOBAL_AES_KEY = getDefaultAESKeyAsString();
  static Cipher DEFAULT_ENCRYPT_CIPHER;
  static Cipher DEFAULT_DECRYPT_CIPHER;
  static SecretKeySpec secretKeySpec;

  static
  {
    try
    {
      synchronized (CipherUtil.class) {
        byte[] raw = decryptBASE64(DEFAULT_GLOBAL_AES_KEY);
        secretKeySpec = new SecretKeySpec(raw, "AES");

        DEFAULT_ENCRYPT_CIPHER = Cipher.getInstance("AES");
        DEFAULT_ENCRYPT_CIPHER.init(1, secretKeySpec);

        DEFAULT_DECRYPT_CIPHER = Cipher.getInstance("AES");
        DEFAULT_DECRYPT_CIPHER.init(2, secretKeySpec);
      }
    }
    catch (NoSuchAlgorithmException e) {
      log.error(e);
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      log.error(e);
      throw new RuntimeException(e);
    } catch (InvalidKeyException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public static String getDefaultAESKeyAsString()
  {
    return getAESKeyAsString("yjl");
  }
  public static String getAESKeyAsString(String keystr) {
    SecretKey secretKey = getSecretKey(keystr);
    return encryptBASE64(secretKey.getEncoded());
  }

  public static SecretKey getSecretKey(String strKey) {
    try {
      KeyGenerator _generator = KeyGenerator.getInstance("AES");
      SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
      secureRandom.setSeed(strKey.getBytes("UTF8"));
      _generator.init(128, secureRandom);

      return _generator.generateKey();
    } catch (Exception e) {
      log.error(e);
    }throw new RuntimeException(" 初始化密钥出现异常 ");
  }

  public static String encryptByAES(String src_msg)
  {
    if (StringUtils.isBlank(src_msg)) {
      return src_msg;
    }
    byte[] encrypted = (byte[])null;
    try {
      encrypted = DEFAULT_ENCRYPT_CIPHER.doFinal(src_msg.getBytes("UTF8"));
    }
    catch (IllegalBlockSizeException e) {
      log.error(e);
    } catch (BadPaddingException e) {
      log.error(e);
    } catch (UnsupportedEncodingException e) {
      log.error(e);
    }

    return asHex(encrypted);
  }

  public static String encryptByAES(String src_msg, String key)
  {
    if (StringUtils.isBlank(src_msg)) {
      return src_msg;
    }

    byte[] encrypted = (byte[])null;
    try {
      Cipher cipher = Cipher.getInstance("AES");

      byte[] raw = decryptBASE64(key);
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      cipher.init(1, skeySpec);

      encrypted = cipher.doFinal(src_msg.getBytes("UTF8"));
    }
    catch (NoSuchAlgorithmException e) {
      log.error(e);
    } catch (NoSuchPaddingException e) {
      log.error(e);
    } catch (InvalidKeyException e) {
      log.error(e);
    } catch (IllegalBlockSizeException e) {
      log.error(e);
    } catch (BadPaddingException e) {
      log.error(e);
    } catch (UnsupportedEncodingException e) {
      log.error(e);
    }

    String encryptedstr = asHex(encrypted);
    return encryptedstr;
  }

  public static String decryptByAES(String crypt)
  {
    if (StringUtils.isBlank(crypt)) {
      return crypt;
    }
    byte[] original = (byte[])null;
    try {
      byte[] bs = asBin(crypt);
      original = DEFAULT_DECRYPT_CIPHER.doFinal(bs);
    }
    catch (IllegalBlockSizeException e) {
      log.error(e);
    } catch (BadPaddingException e) {
      log.error(e);
    }

    if (original == null)
      return null;
    try
    {
      crypt = new String(original, "UTF8");
    } catch (UnsupportedEncodingException e) {
      log.error(e);
    }
    return crypt;
  }

  public static String decryptByAES(String crypt, String key)
  {
    if (StringUtils.isBlank(crypt)) {
      return crypt;
    }
    byte[] original = (byte[])null;
    try {
      Cipher DECRYPT_CIPHER = Cipher.getInstance("AES");
      byte[] raw = decryptBASE64(key);
      SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");

      DECRYPT_CIPHER.init(2, secretKeySpec);

      byte[] bs = asBin(crypt);
      original = DECRYPT_CIPHER.doFinal(bs);
    } catch (IllegalBlockSizeException e) {
      log.error(e);
    } catch (BadPaddingException e) {
      log.error(e);
    } catch (InvalidKeyException e) {
      log.error(e);
    } catch (NoSuchAlgorithmException e) {
      log.error(e);
    } catch (NoSuchPaddingException e) {
      log.error(e);
    }

    if (original == null)
      return null;
    try
    {
      crypt = new String(original, "UTF8");
    } catch (UnsupportedEncodingException e) {
      log.error(e);
    }
    return crypt;
  }

  public static byte[] decryptBASE64(String key)
  {
    try
    {
      return new BASE64Decoder().decodeBuffer(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String encryptBASE64(byte[] key)
  {
    return new BASE64Encoder().encodeBuffer(key);
  }

  public static String asHex(byte[] bts)
  {
    if (bts == null) {
      return null;
    }
    StringBuffer strbuf = new StringBuffer(bts.length * 2);

    for (int i = 0; i < bts.length; i++) {
      if ((bts[i] & 0xFF) < 16) {
        strbuf.append("0");
      }
      strbuf.append(Long.toString(bts[i] & 0xFF, 16));
    }

    return strbuf.toString();
  }

  public static final byte[] asBin(String src)
  {
    if ((src == null) || (src.length() < 1))
      return null;
    byte[] encrypted = new byte[src.length() / 2];
    for (int i = 0; i < src.length() / 2; i++) {
      int high = Integer.parseInt(src.substring(i * 2, i * 2 + 1), 16);
      int low = Integer.parseInt(src.substring(i * 2 + 1, i * 2 + 2), 16);

      encrypted[i] = ((byte)(high * 16 + low));
    }
    return encrypted;
  }

  public static void main(String[] args) {
    String yuan = "测试被加密的字符串";
    log.info(yuan);
    String key = getAESKeyAsString("yjl");

    log.info("key: " + key);
    log.info("key: " + DEFAULT_GLOBAL_AES_KEY);
    String mi = encryptByAES(yuan);

    log.info("加密: " + mi);

    String ming = decryptByAES(mi);
    log.info("解密: " + ming);
  }
}