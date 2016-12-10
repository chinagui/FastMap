package com.navinfo.dataservice.dao.glm.model.poi.deep;

/**
 * poi检查/批处理/重分类，配置类
 * @author wangdongbin
 *
 */
public class PoiColumnOpConf {
	
	private String id;
	private String firstWorkItem;
	private String secondWorkItem;
	private int saveExebatch = 0;
	private String saveBatchrules;
	private int saveExecheck = 0;
	private String saveCkrules;
	private int saveExeclassify = 0;
	private String saveClassifyrules;
	private int submitExebatch = 0;
	private String submitBatchrules;
	private int submitExecheck = 0;
	private String submitCkrules;
	private int submitExeclassify = 0;
	private String submitClassifyrules;
	private int type;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFirstWorkItem() {
		return firstWorkItem;
	}
	public void setFirstWorkItem(String firstWorkItem) {
		this.firstWorkItem = firstWorkItem;
	}
	public String getSecondWorkItem() {
		return secondWorkItem;
	}
	public void setSecondWorkItem(String secondWorkItem) {
		this.secondWorkItem = secondWorkItem;
	}
	public int getSaveExebatch() {
		return saveExebatch;
	}
	public void setSaveExebatch(int saveExebatch) {
		this.saveExebatch = saveExebatch;
	}
	public String getSaveBatchrules() {
		return saveBatchrules;
	}
	public void setSaveBatchrules(String saveBatchrules) {
		this.saveBatchrules = saveBatchrules;
	}
	public int getSaveExecheck() {
		return saveExecheck;
	}
	public void setSaveExecheck(int saveExecheck) {
		this.saveExecheck = saveExecheck;
	}
	public String getSaveCkrules() {
		return saveCkrules;
	}
	public void setSaveCkrules(String saveCkrules) {
		this.saveCkrules = saveCkrules;
	}
	public int getSaveExeclassify() {
		return saveExeclassify;
	}
	public void setSaveExeclassify(int saveExeclassify) {
		this.saveExeclassify = saveExeclassify;
	}
	public String getSaveClassifyrules() {
		return saveClassifyrules;
	}
	public void setSaveClassifyrules(String saveClassifyrules) {
		this.saveClassifyrules = saveClassifyrules;
	}
	public int getSubmitExebatch() {
		return submitExebatch;
	}
	public void setSubmitExebatch(int submitExebatch) {
		this.submitExebatch = submitExebatch;
	}
	public String getSubmitBatchrules() {
		return submitBatchrules;
	}
	public void setSubmitBatchrules(String submitBatchrules) {
		this.submitBatchrules = submitBatchrules;
	}
	public int getSubmitExecheck() {
		return submitExecheck;
	}
	public void setSubmitExecheck(int submitExecheck) {
		this.submitExecheck = submitExecheck;
	}
	public String getSubmitCkrules() {
		return submitCkrules;
	}
	public void setSubmitCkrules(String submitCkrules) {
		this.submitCkrules = submitCkrules;
	}
	public int getSubmitExeclassify() {
		return submitExeclassify;
	}
	public void setSubmitExeclassify(int submitExeclassify) {
		this.submitExeclassify = submitExeclassify;
	}
	public String getSubmitClassifyrules() {
		return submitClassifyrules;
	}
	public void setSubmitClassifyrules(String submitClassifyrules) {
		this.submitClassifyrules = submitClassifyrules;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
