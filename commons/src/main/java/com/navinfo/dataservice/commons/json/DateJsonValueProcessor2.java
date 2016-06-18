package com.navinfo.dataservice.commons.json;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class DateJsonValueProcessor2 implements JsonValueProcessor{

	public static final String Default_DATE_PATTERN ="yyyyMMdd";  
    private DateFormat dateFormat ;  
    public DateJsonValueProcessor2(String datePattern){  
        try{  
            dateFormat  = new SimpleDateFormat(datePattern);}  
        catch(Exception e ){  
            dateFormat = new SimpleDateFormat(Default_DATE_PATTERN);  
        } 
    }  
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {  
        return process(value);  
    }  
    public Object processObjectValue(String key, Object value,JsonConfig jsonConfig) {  
        return process(value);  
    }  
    private Object process(Object value){
    	if(null==value){return "";}
        return dateFormat.format((Date)value);
    }  

}
