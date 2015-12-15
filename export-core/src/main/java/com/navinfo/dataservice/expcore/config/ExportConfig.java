package com.navinfo.dataservice.expcore.config;

import com.navinfo.navicommons.utils.StringUtils;
import com.navinfo.dataservice.expcore.exception.ExportConfigValidateException;
import com.navinfo.dataservice.expcore.exception.ExportException;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.commons.log.DSJobLogger;

public class ExportConfig {
	
	protected String gdbVersion;//230,240,...
	
	protected boolean fastCopy;
	
	private String exportMode;//
	public final static String MODE_COPY="copy";
	public final static String MODE_CUT="cut";
	public final static String MODE_DELETE="delete";
	
	protected String sourceUserName;
	protected String sourcePassword;
	protected String sourceIp;
	protected int sourcePort;
	protected String sourceServiceName;
	protected String sourceTablespaceName;
	
	protected String condition;
//	public final static String CONDITION_FULL="full";
	public final static String CONDITION_BY_MESH="mesh";
	public final static String CONDITION_BY_AREA="area";
	public final static String CONDITION_BY_POLYGON="polygon";
	private Set<String> conditionParams;//若根据图幅导出，设置的导出图幅set,或polygonset等等
	
	private String feature;
	public final static String FEATURE_ALL="all";
	public final static String FEATURE_POI="poi";
	public final static String FEATURE_LINK="link";
	public final static String FEATURE_FACE="face";
	
	protected boolean truncateData=false;//在写入目标的子版本之前是否清理数据
	protected boolean destroyTarget=false;//导出失败时，是否销毁
	
//	protected TargetType targetType;
	protected boolean newTarget=true;//没有指定导出目标时为true，指定导出目标时为false
	protected String targetSysName;//如果newTarget==true，则需要传入dba（具备创建用户的权限）用户
	protected String targetSysPassword;
	protected String targetUserName;
	protected String targetPassword;
	protected String targetIp;
	protected int targetPort;
	protected String targetServiceName;
	protected String targetTablespaceName;
	
	protected boolean multiThread4Input=true;
	protected boolean multiThread4Output=true;
	

	private boolean dataIntegrity = true;// 是否带毛边提取

	public static final String DATA_INTEGRITY = "dataIntegrity";
	public static final String DATA_NOT_INTEGRITY = "dataNotIntegrity";

//	private boolean disableAsynchronousExecute = false;// true
														// :关闭在数据导出后执行根据面批edit_flag

//	private boolean enableCreateScript = false;//子版本在插入数据之前是否创建上脚本，新建和已存在的子版本都会创建
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	//关于源的，关于导出过程的，关于目标的，全写到这里来

    public ExportConfig() {
		super();
		log = DSJobLogger.getLogger(log);
	}
    public ExportConfig(String xmlConfig) throws ExportException{
    	super();
		log = DSJobLogger.getLogger(log);
    	this.parseByXmlConfig(xmlConfig);
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
	 * @return the fastCopy
	 */
	public boolean isFastCopy() {
		return fastCopy;
	}
	/**
	 * @param fastCopy the fastCopy to set
	 */
	public void setFastCopy(boolean fastCopy) {
		this.fastCopy = fastCopy;
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
	 * @return the newTarget
	 */
	public boolean isNewTarget() {
		return newTarget;
	}
	/**
	 * @param newTarget the newTarget to set
	 */
	public void setNewTarget(boolean newTarget) {
		this.newTarget = newTarget;
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
	/**
	 * @return the sourceUserName
	 */
	public String getSourceUserName() {
		return sourceUserName;
	}
	/**
	 * @param sourceUserName the sourceUserName to set
	 */
	public void setSourceUserName(String sourceUserName) {
		this.sourceUserName = sourceUserName;
	}
	/**
	 * @return the sourcePassword
	 */
	public String getSourcePassword() {
		return sourcePassword;
	}
	/**
	 * @param sourcePassword the sourcePassword to set
	 */
	public void setSourcePassword(String sourcePassword) {
		this.sourcePassword = sourcePassword;
	}
	/**
	 * @return the sourceIp
	 */
	public String getSourceIp() {
		return sourceIp;
	}
	/**
	 * @param sourceIp the sourceIp to set
	 */
	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}
	/**
	 * @return the sourcePort
	 */
	public int getSourcePort() {
		return sourcePort;
	}
	/**
	 * @param sourcePort the sourcePort to set
	 */
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
	/**
	 * @return the sourceServiceName
	 */
	public String getSourceServiceName() {
		return sourceServiceName;
	}
	/**
	 * @param sourceServiceName the sourceServiceName to set
	 */
	public void setSourceServiceName(String sourceServiceName) {
		this.sourceServiceName = sourceServiceName;
	}
	/**
	 * @return the sourceTablespaceName
	 */
	public String getSourceTablespaceName() {
		return sourceTablespaceName;
	}
	/**
	 * @param sourceTablespaceName the sourceTablespaceName to set
	 */
	public void setSourceTablespaceName(String sourceTablespaceName) {
		this.sourceTablespaceName = sourceTablespaceName;
	}
	/**
	 * @return the targetUserName
	 */
	public String getTargetUserName() {
		return targetUserName;
	}
	/**
	 * @param targetUserName the targetUserName to set
	 */
	public void setTargetUserName(String targetUserName) {
		this.targetUserName = targetUserName;
	}
	/**
	 * @return the targetPassword
	 */
	public String getTargetPassword() {
		return targetPassword;
	}
	/**
	 * @param targetPassword the targetPassword to set
	 */
	public void setTargetPassword(String targetPassword) {
		this.targetPassword = targetPassword;
	}
	/**
	 * @return the targetSysName
	 */
	public String getTargetSysName() {
		return targetSysName;
	}
	/**
	 * @param targetSysName the targetSysName to set
	 */
	public void setTargetSysName(String targetSysName) {
		this.targetSysName = targetSysName;
	}
	/**
	 * @return the targetSysPassword
	 */
	public String getTargetSysPassword() {
		return targetSysPassword;
	}
	/**
	 * @param targetSysPassword the targetSysPassword to set
	 */
	public void setTargetSysPassword(String targetSysPassword) {
		this.targetSysPassword = targetSysPassword;
	}
	/**
	 * @return the targetIp
	 */
	public String getTargetIp() {
		return targetIp;
	}
	/**
	 * @param targetIp the targetIp to set
	 */
	public void setTargetIp(String targetIp) {
		this.targetIp = targetIp;
	}
	/**
	 * @return the targetPort
	 */
	public int getTargetPort() {
		return targetPort;
	}
	/**
	 * @param targetPort the targetPort to set
	 */
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}
	/**
	 * @return the targetServiceName
	 */
	public String getTargetServiceName() {
		return targetServiceName;
	}
	/**
	 * @param targetServiceName the targetServiceName to set
	 */
	public void setTargetServiceName(String targetServiceName) {
		this.targetServiceName = targetServiceName;
	}
	/**
	 * @return the targetTablespaceName
	 */
	public String getTargetTablespaceName() {
		return targetTablespaceName;
	}
	/**
	 * @param targetTablespaceName the targetTablespaceName to set
	 */
	public void setTargetTablespaceName(String targetTablespaceName) {
		this.targetTablespaceName = targetTablespaceName;
	}
	@Override
	public String toString() {
		return "";
	}
	public void parseByXmlConfig(String xmlConfig) throws ExportException{
		try{
			Document rootDoc=readStringXml(xmlConfig);
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
					String methodName = "set"+(char)(attName.charAt(0)-32)+attName.substring(1, attName.length());
					Class[] argtypes= new Class[]{String.class};
					if(attName.equals("sourcePort")||attName.equals("targetPort")){
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
					}else{
						argtypes= new Class[]{String.class};
						Method method = ExportConfig.class.getMethod(methodName, argtypes);
						method.invoke(this, attValue);
					}
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			String step="导出配置xml解析过程中出错，";
			if(e instanceof DocumentException){
				step="xml文档解析出错,";
			}else if(e instanceof NoSuchMethodException){
				step="ExportConfig解析过程中未找到方法，";
			}
			throw new ExportException(step+"原因为:"+e.getMessage(),e);
		}
	}
	public void parseByJsonConfig(String jsonConfig){
		
	}
	public void validate()throws ExportConfigValidateException,Exception{
		//todo
	}
	
	private Document readStringXml(String xmlParam) throws DocumentException {
		SAXReader reader = new SAXReader();
		StringReader sReader = new StringReader(xmlParam);
		Document document = reader.read(sReader);
		return document;
	}
}
