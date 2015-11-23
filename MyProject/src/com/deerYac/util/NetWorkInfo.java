package com.deerYac.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class NetWorkInfo
{
  private static final Log log = LogFactory.getLog(NetWorkInfo.class);

  protected static final String getMacAddress() throws IOException
  {
    String os = System.getProperty("os.name");
    try {
      if (os.startsWith("Linux"))
        return linuxParseMacAddress(linuxRunIfConfigCommand());
      if (os.startsWith("Windows")) {
        return windowsParseMacAddress(windowsRunIpConfigCommand());
      }
      throw new IOException("unknown operating system: " + os);
    }
    catch (ParseException ex) {
      log.error(ex);
      throw new IOException(ex.getMessage());
    }
  }

  private static final String linuxParseMacAddress(String ipConfigResponse)
    throws ParseException
  {
    String localHost = null;
    try {
      localHost = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
      log.error(ex);
      throw new ParseException(ex.getMessage(), 0);
    }

    StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
    String lastMacAddress = null;

    while (tokenizer.hasMoreTokens()) {
      String line = tokenizer.nextToken().trim();
      boolean containsLocalHost = line.indexOf(localHost) >= 0;

      if ((containsLocalHost) && (lastMacAddress != null)) {
        return lastMacAddress;
      }

      int macAddressPosition = line.indexOf("HWaddr");
      if (macAddressPosition > 0)
      {
        String macAddressCandidate = line.substring(macAddressPosition + 6)
          .trim();
        if (linuxIsMacAddress(macAddressCandidate)) {
          lastMacAddress = macAddressCandidate;
        }
      }
    }

    ParseException ex = new ParseException("cannot read MAC address for " + 
      localHost + " from [" + ipConfigResponse + "]", 0);

    log.info(ex);
    throw ex;
  }

  private static final boolean linuxIsMacAddress(String macAddressCandidate)
  {
    if (macAddressCandidate.length() != 17)
      return false;
    return true;
  }

  private static final String linuxRunIfConfigCommand() throws IOException {
    Process p = Runtime.getRuntime().exec("ifconfig");
    InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

    StringBuffer buffer = new StringBuffer();
    while (true) {
      int c = stdoutStream.read();
      if (c == -1)
        break;
      buffer.append((char)c);
    }
    String outputText = buffer.toString();

    stdoutStream.close();

    return outputText;
  }

  private static final String windowsParseMacAddress(String ipConfigResponse)
    throws ParseException
  {
    String localHost = null;
    try {
      localHost = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
      log.error(ex);
      throw new ParseException(ex.getMessage(), 0);
    }

    StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
    String lastMacAddress = null;

    while (tokenizer.hasMoreTokens()) {
      String line = tokenizer.nextToken().trim();

      if ((line.endsWith(localHost)) && (lastMacAddress != null)) {
        return lastMacAddress;
      }

      int macAddressPosition = line.indexOf(":");
      if (macAddressPosition > 0)
      {
        String macAddressCandidate = line.substring(macAddressPosition + 1)
          .trim();
        if (windowsIsMacAddress(macAddressCandidate)) {
          lastMacAddress = macAddressCandidate;
        }
      }
    }

    ParseException ex = new ParseException("cannot read MAC address from [" + 
      ipConfigResponse + "]", 0);

    log.info(ex);
    throw ex;
  }

  private static final boolean windowsIsMacAddress(String macAddressCandidate)
  {
    if (macAddressCandidate.length() != 17) {
      return false;
    }
    return true;
  }

  private static final String windowsRunIpConfigCommand() throws IOException {
    Process p = Runtime.getRuntime().exec("ipconfig /all");
    InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

    StringBuffer buffer = new StringBuffer();
    while (true) {
      int c = stdoutStream.read();
      if (c == -1)
        break;
      buffer.append((char)c);
    }
    String outputText = buffer.toString();

    stdoutStream.close();

    return outputText;
  }

  protected static String[] getAllWinMacAddress()
  {
    List mac = new ArrayList();

    String os = System.getProperty("os.name");

    if ((os != null) && (os.startsWith("Windows"))) {
      String line = "";
      try {
        String command = "cmd.exe /c ipconfig /all";
        Process p = Runtime.getRuntime().exec(command);

        BufferedReader br = new BufferedReader(new InputStreamReader(p
          .getInputStream(), "gbk"));

        while ((line = br.readLine()) != null) {
          if ((line.indexOf("Physical Address") > 0) || (line.indexOf("物理地址") > 0)) {
            int index = line.indexOf(":") + 2;
            String mac_ = line.substring(index);

            if (windowsIsMacAddress(mac_)) {
              mac.add(mac_);
            }
          }

        }

        br.close();
      }
      catch (IOException e) {
        log.error(e);
      }
    }

    String[] macs = new String[mac.size()];
    for (int i = 0; i < mac.size(); i++) {
      Object o = mac.get(i);
      macs[i] = String.valueOf(o);
    }

    return macs;
  }

  public static final void main(String[] args)
  {
    try
    {
      String[] macs = getAllWinMacAddress();
      for (String mac : macs)
        System.out.println(" MAC Address: " + mac);
    }
    catch (Throwable t) {
      log.error(t);
    }
  }
}