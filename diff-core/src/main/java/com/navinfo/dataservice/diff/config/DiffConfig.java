package com.navinfo.dataservice.diff.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.dom4j.Document;

import com.navinfo.dataservice.commons.job.AbstractJobRequest;
import com.navinfo.dataservice.commons.job.JobRuntimeException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-7 下午5:55
 */
public class DiffConfig extends AbstractJobRequest
{
	//左边为变化库，右边为基准库
    private int leftDbId;//左边数据库
    private int rightDbId;//右边数据库
	//两者只能一个有值，当两者都有值时只取specificTables
	private List<String> specificTables;
	private List<String> excludedTables;
    //线程数
    private int threadCount = 10;
    private String level = LEVEL_COLUMN; //差分粒度：表级，
    public static final String LEVEL_TABLE = "table";
    public static final String LEVEL_COLUMN = "column";
	public String getGdbVersion() {
		return gdbVersion;
	}
	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}
	public int getLeftDbId() {
		return leftDbId;
	}
	public void setLeftDbId(int leftDbId) {
		this.leftDbId = leftDbId;
	}
	public int getRightDbId() {
		return rightDbId;
	}
	public void setRightDbId(int rightDbId) {
		this.rightDbId = rightDbId;
	}
	public List<String> getSpecificTables() {
		return specificTables;
	}
	public void setSpecificTables(List<String> specificTables) {
		this.specificTables = specificTables;
	}
	public List<String> getExcludedTables() {
		return excludedTables;
	}
	public void setExcludedTables(List<String> excludedTables) {
		this.excludedTables = excludedTables;
	}
	public int getThreadCount() {
		return threadCount;
	}
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
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
			if(attName.equals("leftDbId")||attName.equals("rightDbId")){
				argtypes= new Class[]{int.class};
				Method method = DiffConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, Integer.parseInt(attValue));
			}else if(attName.equals("fastCopy")||attName.equals("truncateData")||attName.equals("destroyTarget")||attName.equals("newTarget")
					||attName.equals("multiThread4Input")||attName.equals("multiThread4Output")||attName.equals("dataIntegrity")){
				argtypes= new Class[]{boolean.class};
				Method method = DiffConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, Boolean.parseBoolean(attValue));
			}else if(attName.equals("specificTables")
					||attName.equals("excludedTables")){
				argtypes= new Class[]{List.class};
				Method method = DiffConfig.class.getMethod(methodName, argtypes);
				String[] s= attValue.split(",");
				List<String> li = Arrays.asList(s);
				method.invoke(this, li);
			}else{
				//默认为String
				Method method = DiffConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, attValue);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobRuntimeException("Request解析过程中未找到方法,原因为:"+e.getMessage(),e);
		}
	}
}
