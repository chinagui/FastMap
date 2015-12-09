package com.navinfo.dataservice.datahub.springmvc;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

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
    
    

    protected Map<String,?> exception(Exception e) {
        return exception(e.getMessage());
    }

    protected Map<String,?> exception(String msg) {
        return createModelMap(0,msg,null);
    }

    protected Map<String,?> success(String msg) {
        return createModelMap(1,msg,null);
    }
    protected Map<String,?> success(Map<String,Object> data) {
        return createModelMap(1,null,data);
    }
    protected Map<String,?> success(String msg,Map<String,Object> data) {
        return createModelMap(1,msg,data);
    }
    protected Map<String,Object> success(String msg,Page page) {
        return createModelMapByPage(1,msg,page);
    }
    protected Map<String,Object> success(Page page) {
        return createModelMapByPage(1,null,page);
    }
    @SuppressWarnings("unchecked")
    private Map<String,?> createModelMap(int code,String msg,Object data){
    	Map result = new HashMap();
        result.put("code", Integer.valueOf(code));//0,失败，1，成功
        result.put("msg", "123");
        result.put("data", data);
        return result;
    }
    private Map<String,Object> createModelMapByPage(int code,String msg,Page page){
    	Map<String,Object> result = new HashMap<String,Object>();
        result.put("code", code);//0,失败，1，成功
        result.put("msg", msg);
        result.put("data", page);
        return result;
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

    public static void main(String[] args){
    	Map<String,Object> result = new HashMap<String,Object>();
    	result.put("msg", null);
    	System.out.println("success");
    	System.out.println(result.getClass().getName());
    }
}