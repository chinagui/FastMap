package com.navinfo.dataservice.dao.fcc.model;

import org.apache.commons.lang.StringUtils;

import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/** 
 * @ClassName: TipsDao
 * @author xiaoxiaowen4127
 * @date 2017年7月8日
 * @Description: TipsDao.java
 */
public class TipsDao {
	protected String id;
	protected int stage;
	protected String t_date;
	protected String t_operateDate;
	protected int t_lifecycle;
	protected int t_command;
	protected int handler;
	protected String s_sourceType;
	protected String g_location;
	protected String g_guide;
	protected String deep;
	protected String feedback;
	protected int s_reliability;
	protected Geometry wkt;
	protected String tipdiff;
	protected int s_qTaskId;
	protected int s_mTaskId;
	protected int s_qSubTaskId;
	protected int s_mSubTaskId;
	protected Geometry wktLocation;
	protected int t_tipStatus;
	protected int t_dEditStatus;
	protected int t_mEditStatus;
	protected String s_project;
	protected int t_dEditMeth;
	protected int t_mEditMeth;
	protected String relate_links;
	protected String relate_nodes;
	
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
	public String getT_date() {
		return t_date;
	}
	public void setT_date(String t_date) {
		this.t_date = t_date;
	}
	public String getT_operateDate() {
		return t_operateDate;
	}
	public void setT_operateDate(String t_operateDate) {
		this.t_operateDate = t_operateDate;
	}
	public int getT_lifecycle() {
		return t_lifecycle;
	}
	public void setT_lifecycle(int t_lifecycle) {
		this.t_lifecycle = t_lifecycle;
	}
	public int getT_command() {
		return t_command;
	}
	public void setT_command(int t_command) {
		this.t_command = t_command;
	}
	public int getHandler() {
		return handler;
	}
	public void setHandler(int handler) {
		this.handler = handler;
	}
	public String getS_sourceType() {
		return s_sourceType;
	}
	public void setS_sourceType(String s_sourceType) {
		this.s_sourceType = s_sourceType;
	}
	public String getG_location() {
		return g_location;
	}
	public void setG_location(String g_location) {
		this.g_location = g_location;
	}
	public String getG_guide() {
		return g_guide;
	}
	public void setG_guide(String g_guide) {
		this.g_guide = g_guide;
	}
	public String getDeep() {
		return deep;
	}
	public void setDeep(String deep) {
		this.deep = deep;
	}
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public int getS_reliability() {
		return s_reliability;
	}
	public void setS_reliability(int s_reliability) {
		this.s_reliability = s_reliability;
	}
	public Geometry getWkt() {
		return wkt;
	}
	public void setWkt(Geometry wkt) {
		this.wkt = wkt;
	}
	public String getTipdiff() {
		return tipdiff;
	}
	public void setTipdiff(String tipdiff) {
		this.tipdiff = tipdiff;
	}
	public int getS_qTaskId() {
		return s_qTaskId;
	}
	public void setS_qTaskId(int s_qTaskId) {
		this.s_qTaskId = s_qTaskId;
	}
	public int getS_mTaskId() {
		return s_mTaskId;
	}
	public void setS_mTaskId(int s_mTaskId) {
		this.s_mTaskId = s_mTaskId;
	}
	public int getS_qSubTaskId() {
		return s_qSubTaskId;
	}
	public void setS_qSubTaskId(int s_qSubTaskId) {
		this.s_qSubTaskId = s_qSubTaskId;
	}
	public int getS_mSubTaskId() {
		return s_mSubTaskId;
	}
	public void setS_mSubTaskId(int s_mSubTaskId) {
		this.s_mSubTaskId = s_mSubTaskId;
	}
	public Geometry getWktLocation() {
		return wktLocation;
	}
	public void setWktLocation(Geometry wktLocation) {
		this.wktLocation = wktLocation;
	}
	public int getT_tipStatus() {
		return t_tipStatus;
	}
	public void setT_tipStatus(int t_tipStatus) {
		this.t_tipStatus = t_tipStatus;
	}
	public int getT_dEditStatus() {
		return t_dEditStatus;
	}
	public void setT_dEditStatus(int t_dEditStatus) {
		this.t_dEditStatus = t_dEditStatus;
	}
	public int getT_mEditStatus() {
		return t_mEditStatus;
	}
	public void setT_mEditStatus(int t_mEditStatus) {
		this.t_mEditStatus = t_mEditStatus;
	}
	public String getS_project() {
		return s_project;
	}
	public void setS_project(String s_project) {
		this.s_project = s_project;
	}
	public int getT_dEditMeth() {
		return t_dEditMeth;
	}
	public void setT_dEditMeth(int t_dEditMeth) {
		this.t_dEditMeth = t_dEditMeth;
	}
	public int getT_mEditMeth() {
		return t_mEditMeth;
	}
	public void setT_mEditMeth(int t_mEditMeth) {
		this.t_mEditMeth = t_mEditMeth;
	}
	public String getRelate_links() {
		return relate_links;
	}
	public void setRelate_links(String relate_links) {
		this.relate_links = relate_links;
	}
	public String getRelate_nodes() {
		return relate_nodes;
	}
	public void setRelate_nodes(String relate_nodes) {
		this.relate_nodes = relate_nodes;
	}
	public Object[] toIndexMainArr(){
		Object[] cols = new Object[18];
		cols[0] = id;
		cols[1] = stage;
		cols[2] = t_date;
		cols[3] = t_operateDate;
		cols[4] = t_lifecycle;
		cols[5] = handler;
		cols[6] = s_sourceType;
		cols[7] = wkt.toText();
		cols[8] = s_qTaskId;
		cols[9] = s_mTaskId;
		cols[10] = s_qSubTaskId;
		cols[11] = s_mSubTaskId;
		cols[12] = wktLocation.toText();
		cols[13] = t_tipStatus;
		cols[14] = t_dEditStatus;
		cols[15] = t_mEditStatus;
		cols[16] = s_project;
		cols[17] = t_dEditMeth;
		cols[18] = t_mEditMeth;
		return cols;
	}
	public String[][] toIndexLinkArr(){
		if(StringUtils.isEmpty(getRelate_links())){
			return null;
		}
		String[] raw = getRelate_links().split(",");
		String[][] all = new String[raw.length][];
		for(int i=0;i<raw.length;i++){
			all[i]=new String[]{getId(),raw[i]};
		}
		return all;
	}

	public String[][] toIndexNodeArr(){
		if(StringUtils.isEmpty(getRelate_nodes())){
			return null;
		}
		String[] raw = getRelate_nodes().split(",");
		String[][] all = new String[raw.length][];
		for(int i=0;i<raw.length;i++){
			all[i]=new String[]{getId(),raw[i]};
		}
		return all;
	}
	public static void main(String[] args) {
		TipsDao ti = new TipsDao();
		ti.setT_mEditStatus(100);
		JSONObject jo = JSONObject.fromObject(ti);
		System.out.println(jo.toString());
	}
}
