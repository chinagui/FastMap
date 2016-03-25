package com.navinfo.dataservice.expcore.config;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.expcore.exception.ExportConfigValidateException;
import com.navinfo.dataservice.expcore.exception.ExportException;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.navinfo.dataservice.commons.log.JobLogger;

import net.sf.json.JSONObject;

public class ExportConfig {
	
	protected String gdbVersion;//230,240,240+,252+,...
	
	private String exportMode;//
	public final static String MODE_FLEXIBLE="flexible";//定制导出，走db_link,只支持oracle到oracle
	public final static String MODE_FULL_COPY="full_copy";//整库复制，走db_link,只支持oracle到oracle
	public final static String MODE_COPY="copy";
	public final static String MODE_CUT="cut";
	public final static String MODE_DELETE="delete";
	
	protected int sourceDbId=0;//
	
	protected String condition;

	public final static String CONDITION_BY_MESH="mesh";
	public final static String CONDITION_BY_AREA="area";
	public final static String CONDITION_BY_POLYGON="polygon";
	private Set<String> conditionParams;//若根据图幅导出，设置的导出图幅set,或polygonset等等
	
	private String feature;
	public final static String FEATURE_ALL="all";
	public final static String FEATURE_POI="poi";
	public final static String FEATURE_LINK="link";
	public final static String FEATURE_FACE="face";
	public final static String FEATURE_CK="ck";
	
	protected boolean truncateData=false;//在写入目标的子版本之前是否清理数据
	protected boolean destroyTarget=false;//导出失败时，是否销毁

	protected int targetDbId=0;//
	
	protected boolean multiThread4Input=true;
	protected boolean multiThread4Output=true;
	

	private boolean dataIntegrity = false;// 是否带毛边提取

	public static final String DATA_INTEGRITY = "dataIntegrity";
	public static final String DATA_NOT_INTEGRITY = "dataNotIntegrity";
	
	//
	protected Map<String,String> tableReNames;
	protected List<String> checkExistTables;
	protected String whenExist;
	public static final String WHEN_EXIST_IGNORE = "ignore";
	public static final String WHEN_EXIST_OVERWRITE = "overwrite";
	
	//全库导出属性
	private List<String> specificTables;
	private List<String> excludedTables;
	
	//自由导出属性
	protected  List<String> flexTables;
	protected Map<String,String> flexConditions;
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	//关于源的，关于导出过程的，关于目标的，全写到这里来

    public ExportConfig() {
		super();
		log = JobLogger.getLogger(log);
	}
    public ExportConfig(Document xmlConfig) throws ExportException{
    	super();
		log = JobLogger.getLogger(log);
    	this.parseByXmlConfig(xmlConfig);
    }
    public ExportConfig(JSONObject jsonConfig) throws ExportException{
    	super();
		log = JobLogger.getLogger(log);
    	this.parseByJsonConfig(jsonConfig);
    }

	/**
	 * @return the exportMode
	 */
	public String getExportMode() {
		return exportMode;
	}

	/**
	 * @param exportMode the exportMode to set
	 */
	public void setExportMode(String exportMode) {
		this.exportMode = exportMode;
	}

	/**
	 * @return the truncateData
	 */
	public boolean isTruncateData() {
		return truncateData;
	}

	/**
	 * @param truncateData the truncateData to set
	 */
	public void setTruncateData(boolean truncateData) {
		this.truncateData = truncateData;
	}

	/**
	 * @return the destroyTarget
	 */
	public boolean isDestroyTarget() {
		return destroyTarget;
	}

	/**
	 * @param destroyTarget the destroyTarget to set
	 */
	public void setDestroyTarget(boolean destroyTarget) {
		this.destroyTarget = destroyTarget;
	}

	/**
	 * @return the meshes
	 */
	public Set<String> getConditionParams() {
		return conditionParams;
	}

	/**
	 * @param meshes the meshes to set
	 */
	public void setConditionParams(Set<String> conditionParams) {
		this.conditionParams = conditionParams;
	}

	/**
	 * @return the feature
	 */
	public String getFeature() {
		return feature;
	}
	/**
	 * @param feature the feature to set
	 */
	public void setFeature(String feature) {
		this.feature = feature;
	}
	/**
	 * @return the dataIntegrity
	 */
	public boolean isDataIntegrity() {
		return dataIntegrity;
	}

	/**
	 * @param dataIntegrity the dataIntegrity to set
	 */
	public void setDataIntegrity(boolean dataIntegrity) {
		this.dataIntegrity = dataIntegrity;
	}

	/**
	 * @return the disableAsynchronousExecute
	 */
//	public boolean isDisableAsynchronousExecute() {
//		return disableAsynchronousExecute;
//	}
//
//	/**
//	 * @param disableAsynchronousExecute the disableAsynchronousExecute to set
//	 */
//	public void setDisableAsynchronousExecute(boolean disableAsynchronousExecute) {
//		this.disableAsynchronousExecute = disableAsynchronousExecute;
//	}
//
//	/**
//	 * @return the enableCreateScript
//	 */
//	public boolean isEnableCreateScript() {
//		return enableCreateScript;
//	}
//
//	/**
//	 * @param enableCreateScript the enableCreateScript to set
//	 */
//	public void setEnableCreateScript(boolean enableCreateScript) {
//		this.enableCreateScript = enableCreateScript;
//	}

	/**
	 * @return the log
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(Logger log) {
		this.log = log;
	}

	/**
	 * @return the dataIntegrity
	 */
	public static String getDataIntegrity() {
		return DATA_INTEGRITY;
	}

	/**
	 * @return the dataNotIntegrity
	 */
	public static String getDataNotIntegrity() {
		return DATA_NOT_INTEGRITY;
	}

	/**
	 * @return the gdbVersion
	 */
	public String getGdbVersion() {
		return gdbVersion;
	}
	/**
	 * @param gdbVersion the gdbVersion to set
	 */
	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}
	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}
	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}
	/**
	 * @return the multiThread4Input
	 */
	public boolean isMultiThread4Input() {
		return multiThread4Input;
	}
	/**
	 * @param multiThread4Input the multiThread4Input to set
	 */
	public void setMultiThread4Input(boolean multiThread4Input) {
		this.multiThread4Input = multiThread4Input;
	}
	/**
	 * @return the multiThread4Output
	 */
	public boolean isMultiThread4Output() {
		return multiThread4Output;
	}
	/**
	 * @param multiThread4Output the multiThread4Output to set
	 */
	public void setMultiThread4Output(boolean multiThread4Output) {
		this.multiThread4Output = multiThread4Output;
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
	

	public Map<String, String> getTableReNames() {
		return tableReNames;
	}
	public void setTableReNames(Map<String, String> tableReNames) {
		this.tableReNames = tableReNames;
	}
	public List<String> getCheckExistTables() {
		return checkExistTables;
	}
	public void setCheckExistTables(List<String> checkExistTables) {
		this.checkExistTables = checkExistTables;
	}
	public String getWhenExist() {
		return whenExist;
	}
	public void setWhenExist(String whenExist) {
		this.whenExist = whenExist;
	}
	public List<String> getFlexTables() {
		return flexTables;
	}
	public void setFlexTables(List<String> flexTables) {
		this.flexTables = flexTables;
	}
	public Map<String, String> getFlexConditions() {
		return flexConditions;
	}
	public void setFlexConditions(Map<String, String> flexConditions) {
		this.flexConditions = flexConditions;
	}
	public int getSourceDbId() {
		return sourceDbId;
	}
	public void setSourceDbId(int sourceDbId) {
		this.sourceDbId = sourceDbId;
	}

	public int getTargetDbId() {
		return targetDbId;
	}
	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}
	@Override
	public String toString() {
		return "";
	}
	public void parseByXmlConfig(Document rootDoc) throws ExportException{
		if(rootDoc==null) {
			log.warn("注意：未传入的解析xml对象，导出的config未被初始化");
		}
		List<Element> attrsList = rootDoc.getRootElement().elements();
		for(Element attrs: attrsList){
			List<Element> attrList = attrs.elements();
			for(Element att:attrList){
				String attName = att.attributeValue("name");
				String attValue = att.attributeValue("value");
				if(StringUtils.isEmpty(attName)||StringUtils.isEmpty(attValue)){
					log.warn("注意：导出配置的xml中存在name或者value为空的attr node，已经被忽略。");
					continue;
				}
				setAttrValue(attName,attValue);
			}
		}
	}
	public void parseByJsonConfig(JSONObject json)throws ExportException{
		if(json==null) {
			log.warn("注意：未传入的解析json对象，导出的config未被初始化");
		}
		for(Iterator it = json.keys();it.hasNext();){
			String attName = (String)it.next();
			String attValue = (String)json.get(attName);
			if(StringUtils.isEmpty(attName)||StringUtils.isEmpty(attValue)){
				log.warn("注意：导出配置的json中存在name或者value为空的属性，已经被忽略。");
				continue;
			}
			setAttrValue(attName,attValue);
		}
	}
	private void setAttrValue(String attName,String attValue)throws ExportException{
		try{
			String methodName = "set"+(char)(attName.charAt(0)-32)+attName.substring(1, attName.length());
			Class[] argtypes= new Class[]{String.class};
			if(attName.equals("sourceDbId")||attName.equals("targetDbId")){
				argtypes= new Class[]{int.class};
				Method method = ExportConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, Integer.parseInt(attValue));
			}else if(attName.equals("fastCopy")||attName.equals("truncateData")||attName.equals("destroyTarget")||attName.equals("newTarget")
					||attName.equals("multiThread4Input")||attName.equals("multiThread4Output")||attName.equals("dataIntegrity")){
				argtypes= new Class[]{boolean.class};
				Method method = ExportConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, Boolean.parseBoolean(attValue));
			}else if(attName.equals("conditionParams")){
				String[] s= attValue.split(",");
				Set<String> se = new HashSet<String>(Arrays.asList(s));
				this.setConditionParams(se);
			}else if(attName.equals("checkExistTables")){
				String[] s= attValue.split(",");
				List<String> li = Arrays.asList(s);
				this.setCheckExistTables(li);
			}else if(attName.equals("tableReNames")){
				Map<String,String> m = new HashMap<String,String>();
				String[] sArr= attValue.split(",");
				for(String s:sArr){
					String[] rArr = s.split(":");
					if(rArr!=null&&rArr.length==2){
						m.put(rArr[0], rArr[1]);
					}else{
						log.error("导出参数配置的tableReNames属性存在错误。");
						throw new ExportException("导出参数配置的tableReNames属性存在错误。");
					}
				}
				this.setTableReNames(m);
			}else if(attName.equals("flexTables")){
				String[] s= attValue.split(",");
				List<String> li = Arrays.asList(s);
				this.setFlexTables(li);
			}else if(attName.equals("flexConditions")){
				Map<String,String> m = new HashMap<String,String>();
				String[] sArr= attValue.split(",");
				for(String s:sArr){
					String[] rArr = s.split(":");
					if(rArr!=null&&rArr.length==2){
						m.put(rArr[0], rArr[1]);
					}else{
						log.error("导出参数配置的flexConditions属性存在错误。");
						throw new ExportException("导出参数配置的flexConditions属性存在错误。");
					}
				}
				this.setFlexConditions(m);
			}else if(attName.equals("specificTables")
					||attName.equals("excludedTables")){
				argtypes= new Class[]{List.class};
				Method method = ExportConfig.class.getMethod(methodName, argtypes);
				String[] s= attValue.split(",");
				List<String> li = Arrays.asList(s);
				method.invoke(this, li);
			}
			else{
				argtypes= new Class[]{String.class};
				Method method = ExportConfig.class.getMethod(methodName, argtypes);
				method.invoke(this, attValue);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new ExportException("ExportConfig解析过程中未找到方法,原因为:"+e.getMessage(),e);
		}
	}
	public void validate()throws ExportConfigValidateException,Exception{
		//todo
	}
	
}
