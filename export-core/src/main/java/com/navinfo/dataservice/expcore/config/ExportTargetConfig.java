package com.navinfo.dataservice.expcore.config;

import com.navinfo.dataservice.expcore.exception.ExportConfigValidateException;
import com.navinfo.dataservice.expcore.model.OracleSchema;
import com.navinfo.dataservice.expcore.target.TargetType;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dms.tools.vm.log.VMTaskLogger;

public class ExportTargetConfig {
	
	protected String gdbVersion;//230,240,...
	//1为原来的正常导出，3为原来的removeData==true，2需要增加先做1再做3
	private Integer exportMode;//1-从源复制，2-从源剪切，3-从源复制导出并删除源
	private String exportScope;//
	public final static int SCOPE_FULL=1;
	public final static int SCOPE_MESH=2;
	public final static int SCOPE_AREA=3;
	protected OracleSchema sourceSchema;
	protected int tempResourceIndex;//暂不支持内部自己分配临时表资源,支持后放到OracleSource中生成
	
	protected boolean truncateData=false;//在写入目标的子版本之前是否清理数据
	protected boolean destroyTarget=false;//导出失败时，是否销毁
	
	protected TargetType targetType;
	protected boolean newTarget=true;//没有指定导出目标时为true，指定导出目标时为false
	protected OracleSchema targetSchema;
	
	private Set<String> meshes;//若根据图幅导出，设置的导出图幅set

	private boolean dataIntegrity = true;// 是否带毛边提取

	public static final String DATA_INTEGRITY = "dataIntegrity";
	public static final String DATA_NOT_INTEGRITY = "dataNotIntegrity";

	private boolean disableAsynchronousExecute = false;// true
														// :关闭在数据导出后执行根据面批edit_flag

	private boolean enableCreateScript = false;//子版本在插入数据之前是否创建上脚本，新建和已存在的子版本都会创建
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	//关于源的，关于导出过程的，关于目标的，全写到这里来

    public ExportTargetConfig() {
		super();
		log = VMTaskLogger.getLogger(log);
	}
    public ExportTargetConfig(String jsonConfig){
    	super();
		log = VMTaskLogger.getLogger(log);
    	this.parseByJsonConfig(jsonConfig);
    }

	/**
	 * @return the exportMode
	 */
	public Integer getExportMode() {
		return exportMode;
	}

	/**
	 * @param exportMode the exportMode to set
	 */
	public void setExportMode(Integer exportMode) {
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
	 * @return the tType
	 */
	public TargetType getTargetType() {
		return targetType;
	}

	/**
	 * @param tType the tType to set
	 */
	public void setTargetType(TargetType targetType) {
		this.targetType = targetType;
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
	public Set<String> getMeshes() {
		return meshes;
	}

	/**
	 * @param meshes the meshes to set
	 */
	public void setMeshes(Set<String> meshes) {
		this.meshes = meshes;
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
	public boolean isDisableAsynchronousExecute() {
		return disableAsynchronousExecute;
	}

	/**
	 * @param disableAsynchronousExecute the disableAsynchronousExecute to set
	 */
	public void setDisableAsynchronousExecute(boolean disableAsynchronousExecute) {
		this.disableAsynchronousExecute = disableAsynchronousExecute;
	}

	/**
	 * @return the enableCreateScript
	 */
	public boolean isEnableCreateScript() {
		return enableCreateScript;
	}

	/**
	 * @param enableCreateScript the enableCreateScript to set
	 */
	public void setEnableCreateScript(boolean enableCreateScript) {
		this.enableCreateScript = enableCreateScript;
	}

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
	 * @return the sourceSchema
	 */
	public OracleSchema getSourceSchema() {
		return sourceSchema;
	}
	/**
	 * @param sourceSchema the sourceSchema to set
	 */
	public void setSourceSchema(OracleSchema sourceSchema) {
		this.sourceSchema = sourceSchema;
	}
	/**
	 * @return the targetSchema
	 */
	public OracleSchema getTargetSchema() {
		return targetSchema;
	}
	/**
	 * @param targetSchema the targetSchema to set
	 */
	public void setTargetSchema(OracleSchema targetSchema) {
		this.targetSchema = targetSchema;
	}
	@Override
	public String toString() {
		return "";
	}
	public void parseByXmlConfig(String xmlConfig){
		
	}
	public void parseByJsonConfig(String jsonConfig){
		
	}
	public void validate()throws ExportConfigValidateException,Exception{
		//todo
	}
}
