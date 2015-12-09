package com.navinfo.navicommons.springmvc;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.navicommons.config.SystemGlobals;
import com.navinfo.navicommons.database.Page;

/**
 * @author liuqing
 */
@Controller
public class BaseController {

    public static final String FTP_HOST = SystemGlobals.getValue("dms.ftp.host", "192.168.3.228");
    public static final int FTP_PORT = SystemGlobals.getIntValue("dms.ftp.port", 21);
    public static final String EXP_DEFAULT_FTP_USER = SystemGlobals.getValue("expGDB.ftp.user", "gdb_exp");
    public static final String EXP_DEFAULT_FTP_PASSWORD = SystemGlobals.getValue("expGDB.ftp.password", "gdbExp@@*");

    public static final String CHANGE_DEFAULT_FTP_USER = SystemGlobals.getValue("changeGDB.ftp.user", "gdb_change");
    public static final String CHANGE_DEFAULT_FTP_PASSWORD = SystemGlobals.getValue("changeGDB.ftp.password", "gdb_change");

    public static final String ONLINEEDIT_DEFAULT_FTP_USER = SystemGlobals.getValue("onlineedit.ftp.user", "");
    public static final String ONLINEEDIT_DEFAULT_FTP_PASSWORD = SystemGlobals.getValue("onlineedit.ftp.password", "");


    public static final String AU_DEFAULT_FTP_USER = SystemGlobals.getValue("au.ftp.user", "test");
    public static final String AU_DEFAULT_FTP_PASSWORD = SystemGlobals.getValue("au.ftp.password", "test");

    protected Logger logger = Logger.getLogger(this.getClass());

    protected ModelAndView createQueryJsonModelAndView(Page page) {
        return new ModelAndView("jsonView", "query", page);
    }

    protected ModelAndView createQueryForNoPageJsonModelAndView(Page page) {
        return new ModelAndView("jsonView", "queryForNoPage", page);
    }

    protected ModelAndView createDMLJsonModelAndView(Map map) {
        return new ModelAndView("jsonView", "dml", map);
    }
    
    protected ModelAndView createDataJsonModelAndView(Map map) {
        return new ModelAndView("jsonView", "data", map);
    }

    protected ModelAndView createXMLModelAndView(Object obj) {
        return new ModelAndView("xmlView", BindingResult.MODEL_KEY_PREFIX + "response", obj);
    }

    protected ModelAndView createModelAndView(Object obj) {
        // 可以通过request或http请求头来决定返回view类型
        return createXMLModelAndView(obj);
    }

    @SuppressWarnings("unchecked")
    protected Map exception(Exception e) {
        return exception(e.getMessage());
    }

    protected Map exception(String msg) {
        Map result = new HashMap();
        result.put("success", 0);// 失败
        result.put("message", msg);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected Map success(String message) {
        Map result = new HashMap();
        result.put("success", 1);// 成功
        result.put("message", message);
        return result;
    }

    protected String URLDecode(String parameter) throws Exception {
        if (parameter == null || parameter.trim().length() == 0) {
            return null;
        }
        parameter = URLDecoder.decode(parameter, "UTF-8");
        return parameter;
    }

    // 验证不通过的
    @SuppressWarnings("unchecked")
    protected Map fail(String message) {
        Map result = new HashMap();
        result.put("success", 2);// 验证失败
        result.put("message", message);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected Map success(Map result) {
        if (result != null) {
            result.put("success", 1);// 成功
        }
        return result;
    }

    protected ModelAndView createQueryJsonModelAndViewForXMLFormat(Page page) {
        List list = (List) page.getResult();
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            sb.append(obj.toString());
        }
        return new ModelAndView("jsonView", "query", sb.toString());
    }


    protected Integer toInteger(Object value) {
        if (value == null)
            return null;
        return Integer.valueOf(value.toString());
    }

    protected String toString(Object value) {
    	if (value == null)
    		return null;
    	return value.toString();
    }
    protected Long toLong(Object value) {
    	if (value == null || "".equals(value))
    		return null;
    	return  Long.valueOf(value.toString());
    }


    protected String[] toBounds(Object value) {
        if (value == null)
            return null;
        return value.toString().split(",");
    }


}