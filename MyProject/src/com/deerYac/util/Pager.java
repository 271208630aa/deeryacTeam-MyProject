package com.deerYac.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class Pager
{
  private static boolean USED_IN_OUR_FRAMEWORK = true;

  static String pageNoParamFlagName = "pager.currentPageno";
  static String pageNoParamFlagID = "pager_currentPageno";
  protected int currentPageno = 1;
  protected long totalRows = 1L;
  protected int eachPageRows = 10;
  protected long totalPages = 1L;
  protected String toolBar = "";

  protected String pageName = "";
  protected String urlparams = "";
  protected String formname = "";
  protected int formIndex = 0;

  static
  {
    try {
      SysPropertiesUtil.loadSysProps();
    }
    catch (RuntimeException e) {
      USED_IN_OUR_FRAMEWORK = false;
    }
  }

  public Pager() { if (USED_IN_OUR_FRAMEWORK)
      try {
        this.eachPageRows = SysPropertiesUtil.getInteger("pager.eachPageRows");
      }
      catch (RuntimeException e) {
        this.eachPageRows = 10;
      }
  }

  public int getCurrentPageno()
  {
    this.totalPages = getTotalPages();
    if (this.currentPageno > this.totalPages) {
      this.currentPageno = Long.signum(this.totalPages);
    }
    return this.currentPageno;
  }

  public int getCurrentPagenoDirect()
  {
    return this.currentPageno;
  }

  public void setCurrentPageno(int currentPageno) {
    if (currentPageno < 1)
      this.currentPageno = 1;
    else
      this.currentPageno = currentPageno;
  }

  public int getEachPageRows()
  {
    return this.eachPageRows;
  }

  public void setEachPageRows(int eachPageRows) {
    if (eachPageRows > 100) {
      eachPageRows = 100;
    }
    this.eachPageRows = eachPageRows;
  }

  public long getTotalRows() {
    return this.totalRows;
  }

  public void setTotalRows(long totalRows) {
    this.totalRows = totalRows;
  }

  public boolean isFirst() {
    return this.currentPageno <= 1;
  }

  public boolean isLast() {
    return this.currentPageno >= this.totalPages;
  }

  public long getTotalPages()
  {
    if (this.eachPageRows < 1)
      this.eachPageRows = 10;
    long _totalpages;
    if (this.totalRows % this.eachPageRows == 0L) {
      _totalpages = this.totalRows / this.eachPageRows;
    }
    else
    {
      if (this.totalRows % this.eachPageRows > 0L)
        _totalpages = this.totalRows / this.eachPageRows + 1L;
      else
        _totalpages = 1L;
    }
    if (_totalpages == 0L) {
      _totalpages = 1L;
    }
    return _totalpages;
  }

  public String getPageName() {
    return this.pageName;
  }

  public void setPageName(String pageName) {
    this.pageName = StringEscapeUtils.escapeHtml(pageName);
  }

  public String getUrlparams() {
    return this.urlparams;
  }

  public void setUrlparams(String urlparams) {
    this.urlparams = urlparams;
  }

  public String getToolBar()
  {
    this.currentPageno = getCurrentPageno();
    String urlparam = null;
    if (StringUtils.isBlank(this.urlparams))
      urlparam = pageNoParamFlagName + "=";
    else {
      urlparam = this.urlparams + "&" + pageNoParamFlagName + "=";
    }

    StringBuffer str = new StringBuffer("");
    str.append("<div align='center'>");
    if (isFirst()) {
      str.append("[首页] [上一页]&nbsp;");
    }
    else {
      str.append("[<a href='" + this.pageName + "?" + urlparam + "1'>首页</a>]&nbsp;");
      str.append("[<a href='" + this.pageName + "?" + urlparam + (this.currentPageno - 1) + "'>上一页</a>]&nbsp;");
    }
    if (isLast()) {
      str.append("[下一页] [尾页]&nbsp;");
    }
    else {
      str.append("[<a href='" + this.pageName + "?" + urlparam + (this.currentPageno + 1) + "'>下一页</a>]&nbsp;");
      str.append("[<a href='" + this.pageName + "?" + urlparam + this.totalPages + "'>尾页</a>]&nbsp;");
    }
    str.append("&nbsp;共<b>" + this.totalPages + "</b>页&nbsp;<b>" + this.totalRows + "</b>条记录&nbsp;");
    str.append("&nbsp;转到<select name='" + pageNoParamFlagName + "' onChange=\"location.href='" + this.pageName + "?" + urlparam + "'+this.options[this.selectedIndex].value\" >");
    for (int i = 1; i <= this.totalPages; i++)
    {
      if (i == this.currentPageno)
        str.append("<option value='" + i + "' selected>第" + i + "页</option>");
      else
        str.append("<option value='" + i + "'>第" + i + "页</option>");
    }
    str.append("</select>");
    str.append("<input id='pageTotalRows' name='pageTotalRows' value='" + this.totalRows + "' type='hidden'/>");
    str.append("</div>");
    this.toolBar = str.toString();
    return this.toolBar;
  }

  public String getCommonPostToolBar()
  {
    this.currentPageno = getCurrentPageno();
    StringBuffer rollpage = new StringBuffer();
    rollpage.append("<script language=\"javascript\">\n");
    rollpage.append("\tfunction trunPage(formObj, reqPageNo){\n");
    rollpage.append("\t\tif( formObj ){\n");
    rollpage.append("\t\t}else{\n");
    if (StringUtils.isBlank(this.formname))
      rollpage.append("\t\t\tformObj = document.forms[").append(this.formIndex).append("];\n");
    else {
      rollpage.append("\t\t\tformObj = document.forms['").append(this.formname).append("'];\n");
    }
    rollpage.append("\t\t}\n");
    rollpage.append("\t\tif( reqPageNo ){\n");
    rollpage.append("\t\t\t\tformObj." + pageNoParamFlagID + ".value = reqPageNo;\n");
    rollpage.append("\t\t}\n");
    rollpage.append("\t\tvar nowPageNo = formObj." + pageNoParamFlagID + ".value;\n");
    rollpage.append("\t\tif( nowPageNo<1 || nowPageNo>").append(this.totalPages).append(" ){\n");
    rollpage.append("\t\t\t\talert('当前的有效页码是小于或等于").append(this.totalPages).append("的正整数，请重新输入！');\n");
    rollpage.append("\t\t\t\treturn;\n");
    rollpage.append("\t\t}\n");
    rollpage.append("\t\tformObj.submit();\n");

    rollpage.append("\t}\n");
    rollpage.append("</script>\n");

    if (this.currentPageno > 1) {
      rollpage.append("<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" onclick=\"trunPage(this.form, '1')\" value=\"[首页]\">\n");
      rollpage.append("&nbsp;<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" onclick=\"trunPage(this.form, '" + (this.currentPageno - 1) + "')\"" + " value=\"[上一页]\">\n");
    }
    else {
      rollpage.append("<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" disabled=\"true\" value=\"[首页]\">\n");
      rollpage.append("&nbsp;<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" disabled=\"true\" value=\"[上一页]\">\n");
    }

    if (this.totalPages - this.currentPageno > 0L) {
      rollpage.append("&nbsp;<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" onclick=\"trunPage(this.form, '" + (this.currentPageno + 1) + "')\"" + " value=\"[下一页]\">\n");
      rollpage.append("&nbsp;<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" onclick=\"trunPage(this.form, '" + this.totalPages + "')\"" + " value=\"[尾页]\">\n");
    }
    else {
      rollpage.append("&nbsp;<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" disabled=\"true\" value=\"[下一页]\">\n");
      rollpage.append("&nbsp;<input type='button' style=\"background-color:transparent;border:0px;cursor:pointer;height:20px!important\" disabled=\"true\" value=\"[尾页]\">\n");
    }

    rollpage.append("&nbsp;<span style='font-family:Verdana, Geneva, Arial, Helvetica, sans-serif;'>第<b>" + this.currentPageno + "/" + this.totalPages + "</b>页 ");
    rollpage.append("&nbsp;每页<input id='eachPageRows' name='pager.eachPageRows' ");
    rollpage.append("value='").append(this.eachPageRows).append("' maxlength='2' style='height:12px!important;width:20px;ime-mode:disabled;'>条记录");
    rollpage.append("&nbsp;合<b>" + this.totalRows + "</b>条记录</span> \n");
    rollpage.append("&nbsp;转到第<input id='" + pageNoParamFlagID + "' name='" + pageNoParamFlagName + "' value='" + this.currentPageno + "' size='2' style='height:12px!important;ime-mode:disabled;'>");
    rollpage.append("&nbsp;<input id='pager_rollPageButton' type='button' style='height:20px!important' onclick=\"trunPage(this.form)\" value='GO'>");
    rollpage.append("<input id='pageTotalRows' name='pageTotalRows' value='" + this.totalRows + "' type='hidden'/>");

    this.toolBar = rollpage.toString();
    return this.toolBar;
  }

  public String getPostToolBar()
  {
    this.currentPageno = getCurrentPageno();
    StringBuffer rollpage = new StringBuffer();
    rollpage.append("<script language=\"javascript\">\n");
    rollpage.append("\tfunction trunPage(){\n");
    if (StringUtils.isBlank(this.formname))
      rollpage.append("\t\tdocument.forms[").append(this.formIndex).append("].action = window.location.href;\n");
    else {
      rollpage.append("\t\tdocument.forms['").append(this.formname).append("'].action = window.location.href;\n");
    }

    rollpage.append("\t\tvar reqPageNo = document.getElementById('" + pageNoParamFlagID + "').value;\n");
    rollpage.append("\t\tif( reqPageNo<1 || reqPageNo>").append(this.totalPages).append(" ){\n");
    rollpage.append("\t\t\t\talert('当前的有效页码是小于或等于").append(this.totalPages).append("的正整数，请重新输入！');\n");
    rollpage.append("\t\t\t\treturn;\n");
    rollpage.append("\t\t}\n");
    if (StringUtils.isBlank(this.formname))
      rollpage.append("\t\t\tdocument.forms[").append(this.formIndex).append("].submit();\n");
    else {
      rollpage.append("\t\t\tdocument.forms['").append(this.formname).append("'].submit();\n");
    }

    rollpage.append("\t}\n");
    rollpage.append("</script>\n");

    rollpage.append("&nbsp;<span style='font-family:Verdana, Geneva, Arial, Helvetica, sans-serif;'>第<b>" + this.currentPageno + "/" + this.totalPages + "</b>页 ");
    rollpage.append("&nbsp;每页<input id='eachPageRows' name='pager.eachPageRows' ");
    rollpage.append("value='").append(this.eachPageRows).append("' maxlength='2' style='height:12px!important;width:20px;ime-mode:disabled;'>条记录");
    rollpage.append("&nbsp;合<b>" + this.totalRows + "</b>条记录</span> \n");
    rollpage.append("&nbsp;转到第<input id='" + pageNoParamFlagID + "' name='" + pageNoParamFlagName + "' value='" + this.currentPageno + "' size='2' style='height:12px!important;ime-mode:disabled;'>");
    rollpage.append("&nbsp;<input id='pager_rollPageButton' type='button' style='height:20px!important' onclick=\"trunPage()\" value='GO'>");
    rollpage.append("<input id='pageTotalRows' name='pageTotalRows' value='" + this.totalRows + "' type='hidden'/>");

    this.toolBar = rollpage.toString();
    return this.toolBar;
  }

  public String getFormname() {
    return this.formname;
  }

  public void setFormname(String formname) {
    this.formname = StringEscapeUtils.escapeHtml(formname);
  }

  public int getFormIndex() {
    return this.formIndex;
  }

  public void setFormIndex(int formIndex) {
    this.formIndex = formIndex;
  }
}