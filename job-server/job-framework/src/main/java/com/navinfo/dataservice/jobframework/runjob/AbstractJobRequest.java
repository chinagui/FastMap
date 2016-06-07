package com.navinfo.dataservice.jobframework.runjob;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;

/** 
 * @ClassName: AbstractJobRequest 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:18:48 
 * @Description: TODO
 */
public abstract class AbstractJobRequest {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected String gdbVersion;
	
	public abstract int getStepCount()throws JobException;
	
	public abstract void validate()throws JobException;
	
	
	public String getGdbVersion() {
		return gdbVersion;
	}

	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}

	public void parseByJsonConfig(JSONObject json)throws JobRuntimeException{
		if(json==null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		}
		for(Iterator it = json.keys();it.hasNext();){
			String attName = (String)it.next();
			Object attValue = json.get(attName);
			if(attValue==null||
			   StringUtils.isEmpty(attName)||
			   (attValue instanceof String && StringUtils.isEmpty((String)attValue))
			   ){
				log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
				continue;
			}
			setAttrValue(attName,attValue);
		}
	}

	public void setAttrValue(String attName,Object attValue)throws JobRuntimeException{
		if(StringUtils.isEmpty(attName)||attValue==null||(attValue instanceof JSONNull)){
			log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
			return;
		}
		try{
			String methodName = "set"+(char)(attName.charAt(0)-32)+attName.substring(1, attName.length());
			Class[] argtypes = null;//默认String
			
			if(attValue instanceof String){
				argtypes = new Class[]{String.class};
			}else if(attValue instanceof Integer){
				argtypes= new Class[]{Integer.class};
			}else if(attValue instanceof Boolean){
				argtypes= new Class[]{Boolean.class};
			}else if(attValue instanceof JSONArray){
				//if(((JSONArray) attValue).get(index))
				argtypes= new Class[]{List.class};
				
			}else if(attValue instanceof JSONObject){
				//sub job
			}
			Method method = this.getClass().getMethod(methodName, argtypes);
			method.invoke(this, attValue);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobRuntimeException("Request解析过程中未找到方法,原因为:"+e.getMessage(),e);
		}
	}
	
	public static void main(String[] args){
		JSONObject json = new JSONObject();
		json.put("para1", "param1");
		JSONObject subJson1 = new JSONObject();
		json.put("subObject1", subJson1);
		for(Iterator it = json.keys();it.hasNext();){
			String attName = (String)it.next();
			System.out.println(json.get(attName).getClass().getSimpleName());
		}
	}
}
