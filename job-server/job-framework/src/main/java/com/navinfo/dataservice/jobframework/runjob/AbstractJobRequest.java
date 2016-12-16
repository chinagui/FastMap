package com.navinfo.dataservice.jobframework.runjob;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
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
	protected String gdbVersion=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
	
	/**
	 * 注意循环调用。。。。。。
	 */
	protected Map<String,AbstractJobRequest> subJobRequests;
	
	public abstract void defineSubJobRequests()throws JobCreateException;
	
	public abstract String getJobType();
	public abstract String getJobTypeName();
	
	public int getStepCount()throws JobException{
		int count = myStepCount()+1;
		if(subJobRequests!=null){
			for(AbstractJobRequest r:subJobRequests.values()){
				count+=r.getStepCount();
			}
		}
		return count;
	}
	
	protected abstract int myStepCount()throws JobException;
	
	public abstract void validate()throws JobException;
	
	
	public String getGdbVersion() {
		return gdbVersion;
	}

	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}

	public AbstractJobRequest getSubJobRequest(String key) {
		return subJobRequests==null?null:subJobRequests.get(key);
	}

	public void setSubJobRequests(Map<String, AbstractJobRequest> subJobRequests) {
		this.subJobRequests = subJobRequests;
	}

	public void parseByJsonConfig(JSONObject json)throws JobCreateException{
		if(json==null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		}else{
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
		//
		defineSubJobRequests();
	}

	public void setAttrValue(String attName,Object attValue)throws JobCreateException{
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
				argtypes= new Class[]{int.class};
			}else if(attValue instanceof Double){
				argtypes = new Class[]{double.class};
			}else if(attValue instanceof Boolean){
				argtypes= new Class[]{boolean.class};
			}else if(attValue instanceof JSONArray){
				JSONArray attArr = (JSONArray)attValue;
				if(attArr.size()>0){
					Object subObj = attArr.get(0);
					if(subObj instanceof String
							||subObj instanceof Integer
							||subObj instanceof Double
							||subObj instanceof Boolean
							){
						argtypes= new Class[]{List.class};
					}else if(subObj instanceof JSONObject){
						argtypes= new Class[]{Map.class};
						Map newAttValue = new HashMap();
						for(Object o:attArr){
							JSONObject jo = (JSONObject)o;
							Object key = jo.get("key");
							Object value = jo.get("value");
							if(key!=null&&value!=null){
								newAttValue.put(key, value);
							}
						}
						attValue=newAttValue;
					}else{
						throw new Exception(attName+"为数组类型，其内部格式为不支持的json结构");
					}
				}else{
					return;
				}
				
			}else if(attValue instanceof JSONObject){
				//sub job
				argtypes= new Class[]{JSONObject.class};
			}
			Method method = this.getClass().getMethod(methodName, argtypes);
			method.invoke(this, attValue);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobCreateException("Request解析过程中可能未找到方法,原因为:"+e.getMessage(),e);
		}
	}
	
	public static void main(String[] args){
		JSONObject json = new JSONObject();
		json.put("para1", "param1");
		json.put("para2", null);
		json.put("para3", 1);
		json.put("para4", 1.1);
		json.put("para5", true);
		json.put("para6", new JSONArray());
		json.put("para7", 1L);
		JSONObject subJson1 = new JSONObject();
		json.put("subObject1", subJson1);
		Set<String> data = new HashSet<String>();
		data.add("AAA");
		json.put("para8", data);
		for(Iterator it = json.keys();it.hasNext();){
			String attName = (String)it.next();
			System.out.println(json.get(attName).getClass().getSimpleName());
		}
	}
}
