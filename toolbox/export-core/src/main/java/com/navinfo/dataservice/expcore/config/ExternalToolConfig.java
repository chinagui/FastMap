package com.navinfo.dataservice.expcore.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Document;

import com.navinfo.dataservice.jobframework.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.JobRuntimeException;

/** 
 * @ClassName: ExternalToolConfig 
 * @author Xiao Xiaowen 
 * @date 2016-1-20 上午10:20:19 
 * @Description: TODO
 */
public class ExternalToolConfig  extends AbstractJobRequest{
    
	protected String type;

    public static final String TYPE_ON_PK = "on-pk";
    public static final String TYPE_OFF_PK = "off-pk";
    public static final String TYPE_REMOVE_DUP = "remove-dup";
    protected List<String> targetTables; 
    protected String target="all";
    public static final String TARGET_ALL = "all";
    public static final String TARGET_CUSTOM = "custom";
    protected boolean bak4removeDup=false;
	
	public ExternalToolConfig() {
		super();
	}
    public ExternalToolConfig(Document xmlConfig){
    	super();
    	this.parseByXmlConfig(xmlConfig);
    }
    public ExternalToolConfig(JSONObject jsonConfig){
    	super();
    	this.parseByJsonConfig(jsonConfig);
    }

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getTargetTables() {
		return targetTables;
	}
	public void setTargetTables(List<String> targetTables) {
		this.targetTables = targetTables;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public boolean isBak4removeDup() {
		return bak4removeDup;
	}
	public void setBak4removeDup(boolean bak4removeDup) {
		this.bak4removeDup = bak4removeDup;
	}
	@Override
	public void validate() throws JobRuntimeException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setAttrValue(String attName,String attValue)throws JobRuntimeException{
		try{
			String methodName = "set"+(char)(attName.charAt(0)-32)+attName.substring(1, attName.length());
			Class[] argtypes= new Class[]{String.class};
			if(attName.equals("bak4removeDup")){
				//boolean
				argtypes= new Class[]{boolean.class};
				Method method = ExternalToolConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, Boolean.parseBoolean(attValue));
			}else if(attName.equals("targetTables")){
				//list
				argtypes= new Class[]{List.class};
				Method method = ExternalToolConfig.class.getMethod(methodName, argtypes);
				String[] s= attValue.split(",");
				List<String> li = Arrays.asList(s);
				method.invoke(this, li);
			}else{
				//默认为String
				Method method = ExternalToolConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, attValue);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobRuntimeException("Request解析过程中未找到方法,原因为:"+e.getMessage(),e);
		}
	}
}
