package com.navinfo.dataservice.dao.fcc.model;

import org.apache.commons.lang.StringUtils;

import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: TipsIndexDao
 * @author xiaoxiaowen4127
 * @date 2017年7月8日
 * @Description: TipsSolrDao.java
 */
public class TipsIndexDao {
	protected String id;
	protected int stage;
	protected String tDate;
	protected String tOperateDate;
	protected int tLifecycle;
	protected int tCommand;
	protected int handler;
	protected String sSourceType;
	protected Geometry wkt;
	protected String tipDiff;
	protected int sqTaskId;
	protected int smTaskId;
	protected int sqSubtaskId;
	protected int smSubtaskId;
	protected Geometry wktLocation;
	protected int tTipStatus;
	protected int tdEditStatus;
	protected int tmEditStatus;
	protected int sProject;
	protected int tdEditMethod;
	protected int tmEditMethod;
	protected String relateLinks;
	protected String relateNodes;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getStage() {
		return stage;
	}
	public void setStage(int stage) {
		this.stage = stage;
	}
	public String gettDate() {
		return tDate;
	}
	public void settDate(String tDate) {
		this.tDate = tDate;
	}
	public String gettOperateDate() {
		return tOperateDate;
	}
	public void settOperateDate(String tOperateDate) {
		this.tOperateDate = tOperateDate;
	}
	public int gettLifecycle() {
		return tLifecycle;
	}
	public void settLifecycle(int tLifecycle) {
		this.tLifecycle = tLifecycle;
	}
	public int gettCommand() {
		return tCommand;
	}
	public void settCommand(int tCommand) {
		this.tCommand = tCommand;
	}
	public int getHandler() {
		return handler;
	}
	public void setHandler(int handler) {
		this.handler = handler;
	}
	public String getsSourceType() {
		return sSourceType;
	}
	public void setsSourceType(String sSourceType) {
		this.sSourceType = sSourceType;
	}
	public Geometry getWkt() {
		return wkt;
	}
	public void setWkt(Geometry wkt) {
		this.wkt = wkt;
	}
	public String getTipDiff() {
		return tipDiff;
	}
	public void setTipDiff(String tipDiff) {
		this.tipDiff = tipDiff;
	}
	public int getSqTaskId() {
		return sqTaskId;
	}
	public void setSqTaskId(int sqTaskId) {
		this.sqTaskId = sqTaskId;
	}
	public int getSmTaskId() {
		return smTaskId;
	}
	public void setSmTaskId(int smTaskId) {
		this.smTaskId = smTaskId;
	}
	public int getSqSubtaskId() {
		return sqSubtaskId;
	}
	public void setSqSubtaskId(int sqSubtaskId) {
		this.sqSubtaskId = sqSubtaskId;
	}
	public int getSmSubtaskId() {
		return smSubtaskId;
	}
	public void setSmSubtaskId(int smSubtaskId) {
		this.smSubtaskId = smSubtaskId;
	}
	public Geometry getWktLocation() {
		return wktLocation;
	}
	public void setWktLocation(Geometry wktLocation) {
		this.wktLocation = wktLocation;
	}
	public int gettTipStatus() {
		return tTipStatus;
	}
	public void settTipStatus(int tTipStatus) {
		this.tTipStatus = tTipStatus;
	}
	public int getTdEditStatus() {
		return tdEditStatus;
	}
	public void setTdEditStatus(int tdEditStatus) {
		this.tdEditStatus = tdEditStatus;
	}
	public int getTmEditStatus() {
		return tmEditStatus;
	}
	public void setTmEditStatus(int tmEditStatus) {
		this.tmEditStatus = tmEditStatus;
	}
	public int getsProject() {
		return sProject;
	}
	public void setsProject(int sProject) {
		this.sProject = sProject;
	}
	public int getTdEditMethod() {
		return tdEditMethod;
	}
	public void setTdEditMethod(int tdEditMethod) {
		this.tdEditMethod = tdEditMethod;
	}
	public int getTmEditMethod() {
		return tmEditMethod;
	}
	public void setTmEditMethod(int tmEditMethod) {
		this.tmEditMethod = tmEditMethod;
	}
	public String getRelateLinks() {
		return relateLinks;
	}
	public void setRelateLinks(String relateLinks) {
		this.relateLinks = relateLinks;
	}
	public String getRelateNodes() {
		return relateNodes;
	}
	public void setRelateNodes(String relateNodes) {
		this.relateNodes = relateNodes;
	}
	
	public Object[] toColsObjectArr(){
		Object[] cols = new Object[23];
		cols[0]=id;
		cols[1]=stage;
		cols[2] = tDate;
		cols[3] = tOperateDate;
		cols[4] = tLifecycle;
		cols[5] = tCommand;
		cols[6] = handler;
		cols[7] = sSourceType;
		cols[8] = wkt.toText();
		cols[9] = tipDiff;
		cols[10] = sqTaskId;
		cols[11] = smTaskId;
		cols[12] = sqSubtaskId;
		cols[13] = smSubtaskId;
		cols[14] = wktLocation.toText();
		cols[15] = tTipStatus;
		cols[16] = tdEditStatus;
		cols[17] = tmEditStatus;
		cols[18] = sProject;
		cols[19] = tdEditMethod;
		cols[20] = tmEditMethod;
		cols[21] = relateLinks;
		cols[22] = relateNodes;
		return cols;
	}
}
