package com.navinfo.dataservice.web.springmvc;

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


    protected Map<String,?> exception(Exception e) {
        return exception(e.getMessage());
    }
    protected Map<String,?> exception(String msg) {
        return createModelMap(1,msg,null);
    }

    // 验证不通过的
    protected Map<String,?> fail(String msg) {
        return createModelMap(0,msg,null);
    }
    protected Map<String,?> success(String msg) {
        return createModelMap(100,msg,null);
    }
    protected Map<String,?> success(String msg,Map<String,?> result) {
        return createModelMap(100,msg,result);
    }
    protected Map<String,?> success(Map<String,?> result) {
        return createModelMap(100,"success",result);
    }
    protected Map<String,?> success(String msg,Page page){
    	return createModelMap(100,msg,page);
    }
    protected Map<String,?> success(Page page){
    	return createModelMap(100,"success",page);
    }
    private Map<String,?> createModelMap(int code,String msg,Object data){
    	Map<String,Object> result = new HashMap<String,Object>();
    	result.put("code", code);
    	result.put("msg", msg);
    	result.put("data", data);
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
    protected String URLDecode(String parameter) throws Exception {
        if (parameter == null || parameter.trim().length() == 0) {
            return null;
        }
        parameter = URLDecoder.decode(parameter, "UTF-8");
        return parameter;
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