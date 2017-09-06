package com.navinfo.dataservice.dao.fcc.model;

import com.navinfo.dataservice.commons.util.DateUtils;
import oracle.sql.STRUCT;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	protected String t_dataDate;
	
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
	public void setWkt(String wkt) throws Exception {
		this.wkt = GeoTranslator.wkt2Geometry(wkt);;
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
	public void setWktLocation(String wktLocation) throws Exception {
		this.wktLocation = GeoTranslator.wkt2Geometry(wktLocation);
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
	
	
	/**
	 * @return the t_dataDate
	 */
	public String getT_dataDate() {
		return t_dataDate;
	}
	/**
	 * @param t_dataDate the t_dataDate to set
	 */
	public void setT_dataDate(String t_dataDate) {
		this.t_dataDate = t_dataDate;
	}
	public Object[] toIndexMainArr(){
		Object[] cols = new Object[20];
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
		cols[19] = t_dataDate;
		return cols;
	}
	public String[][] toIndexLinkArr(){
		if(StringUtils.isEmpty(getRelate_links())){
			return null;
		}
		String repStr = getRelate_links().replace("|", ",");
		String[] raw = repStr.split(",");
		Set<String> cols = new HashSet<String>();
		for(String r:raw){
			if(StringUtils.isNotEmpty(r)){
				cols.add(r);
			}
		}
		String[][] all = new String[cols.size()][];
		int i = 0;
		for(String col:cols){
			all[i]=new String[]{getId(),col};
			i++;
		}
		return all;
	}

	public String[][] toIndexNodeArr(){
		if(StringUtils.isEmpty(getRelate_nodes())){
			return null;
		}
		String repStr = getRelate_nodes().replace("|", ",");
		String[] raw = repStr.split(",");
		Set<String> cols = new HashSet<String>();
		for(String r:raw){
			if(StringUtils.isNotEmpty(r)){
				cols.add(r);
			}
		}
		String[][] all = new String[cols.size()][];
		int i = 0;
		for(String col:cols){
			all[i]=new String[]{getId(),col};
			i++;
		}
		return all;
	}
	public void loadResultSet(ResultSet rs) throws SQLException{
		this.setId(rs.getString("id"));
		this.setStage(rs.getInt("stage"));
		this.setT_date(DateUtils.dateToString(rs.getTimestamp("t_date"),DateUtils.DATE_COMPACTED_FORMAT));
		this.setT_operateDate(DateUtils.dateToString(rs.getTimestamp("t_operateDate"),DateUtils.DATE_COMPACTED_FORMAT));
		this.setT_lifecycle(rs.getInt("t_lifecycle"));
		this.setHandler(rs.getInt("handler"));
		this.setS_mTaskId(rs.getInt("s_mTaskId"));
		this.setS_qTaskId(rs.getInt("s_qTaskId"));
		this.setS_mSubTaskId(rs.getInt("s_mSubTaskId"));
		this.setS_qSubTaskId(rs.getInt("s_qSubTaskId"));
		this.setS_sourceType(rs.getString("s_sourceType"));
		this.setT_dEditStatus(rs.getInt("t_dEditStatus"));
		this.setT_mEditStatus(rs.getInt("t_mEditStatus"));
		this.setT_tipStatus(rs.getInt("t_tipStatus"));
		this.setS_project(rs.getString("s_project"));
		this.setT_mEditMeth(rs.getInt("t_mEditMeth"));
		this.setT_dataDate(rs.getString("t_dataDate"));
		try {
			STRUCT wkt = (STRUCT) rs.getObject("wkt");
			this.setWkt(GeoTranslator.struct2Jts(wkt));
			STRUCT wktLocation = (STRUCT) rs.getObject("wktLocation");
			this.setWktLocation(GeoTranslator.struct2Jts(wktLocation));
		}catch (Exception ex){
			throw new SQLException(ex.getMessage());
		}
	}
	public void loadHbase(JSONObject hbaseTips){
		if(hbaseTips.containsKey("deep")) {
			JSONObject deep = hbaseTips.getJSONObject("deep");
			this.setDeep(deep.toString());
		}
		if(hbaseTips.containsKey("feedback")) {
			JSONObject feedback = hbaseTips.getJSONObject("feedback");
			this.setFeedback(feedback.toString());
		}
		if(hbaseTips.containsKey("geometry")) {
			JSONObject geometry = hbaseTips.getJSONObject("geometry");
			this.setG_guide(geometry.getJSONObject("g_guide").toString());
			this.setG_location(geometry.getJSONObject("g_location").toString());
		}
		if(hbaseTips.containsKey("tipdiff")) {
			JSONObject tipdiff = hbaseTips.getJSONObject("tipdiff");
			this.setTipdiff(tipdiff.toString());
		}
	}
	public TipsDao copy(){
		TipsDao tipsDao = new TipsDao();
		tipsDao.setId(this.getId());
		tipsDao.setStage(this.getStage());
		tipsDao.setT_operateDate(this.getT_operateDate());
		tipsDao.setT_lifecycle(this.getT_lifecycle());
		tipsDao.setT_command(this.getT_command());
		tipsDao.setHandler(this.getHandler());
		tipsDao.setS_sourceType(this.getS_sourceType());
		tipsDao.setWkt(this.getWkt());
		tipsDao.setTipdiff(this.getTipdiff());
		tipsDao.setS_qTaskId(this.getS_qTaskId());
		tipsDao.setS_mTaskId(this.getS_mTaskId());
		tipsDao.setS_qSubTaskId(this.getS_qSubTaskId());
		tipsDao.setS_mSubTaskId(this.getS_mSubTaskId());
		tipsDao.setWktLocation(this.getWktLocation());
		tipsDao.setT_tipStatus(this.getT_tipStatus());
		tipsDao.setT_dEditMeth(this.getT_dEditMeth());
		tipsDao.setT_mEditMeth(this.getT_mEditMeth());
		tipsDao.setT_mEditStatus(this.getT_mEditStatus());
		tipsDao.setT_dEditStatus(this.getT_dEditStatus());
		tipsDao.setS_project(this.getS_project());
		tipsDao.setDeep(this.getDeep());
		tipsDao.setG_location(this.getG_location());
		tipsDao.setG_guide(this.getG_guide());
		tipsDao.setFeedback(this.getFeedback());
		tipsDao.setRelate_links(this.getRelate_links());
		tipsDao.setRelate_nodes(this.getRelate_nodes());
		tipsDao.setT_dataDate(this.getT_dataDate());
		return tipsDao;
	}
}
