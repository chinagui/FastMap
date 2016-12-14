/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.model;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @ClassName: TmcLine
 * @author Zhang Xiaolong
 * @date 2016年11月15日 下午3:26:44
 * @Description: TODO
 */
public class TmcLine {
	private int tmcId;
	
	//位置代码
	private int locCode;
	
	//位置表代码
	private String loctableId;
	
	private String translateName;

	//位置表代码
	private String cid;
	
	//类型代码
	private String typeCode;
	
	//道路序号
	private int seqNum;

	//正向偏移量
	private int locoffPos;
	
	//负向偏移量
	private int locoffNeg;
	
	//上级道路参考
	private int uplineTmcId;
	
	private int areaTmcId;

	private JSONArray geometry;

	private List<TmcLineName> names = new ArrayList<>();
	
	private List<TmcPoint> points = new ArrayList<>();

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public String getTranslateName() {
		return translateName;
	}
	
	public String getLoctableId() {
		return loctableId;
	}

	public void setLoctableId(String loctableId) {
		this.loctableId = loctableId;
	}

	public void setTranslateName(String translateName) {
		this.translateName = translateName;
	}

	public JSONArray getGeometry() {
		return geometry;
	}

	public void setGeometry(JSONArray geometry) {
		this.geometry = geometry;
	}

	public int getAreaTmcId() {
		return areaTmcId;
	}

	public void setAreaTmcId(int areaTmcId) {
		this.areaTmcId = areaTmcId;
	}

	public List<TmcLineName> getNames() {
		return names;
	}

	public void setNames(List<TmcLineName> names) {
		this.names = names;
	}

	public List<List<TmcLineName>> children() {
		List<List<TmcLineName>> children = new ArrayList<List<TmcLineName>>();
		children.add(this.names);
		return children;
	}

	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		JsonConfig jsonConfig = Geojson.geoJsonConfig(1, 5);

		return JSONObject.fromObject(this, jsonConfig);
	}

	public int getLocCode() {
		return locCode;
	}

	public void setLocCode(int locCode) {
		this.locCode = locCode;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public int getLocoffPos() {
		return locoffPos;
	}

	public void setLocoffPos(int locoffPos) {
		this.locoffPos = locoffPos;
	}

	public int getLocoffNeg() {
		return locoffNeg;
	}

	public void setLocoffNeg(int locoffNeg) {
		this.locoffNeg = locoffNeg;
	}

	public int getUplineTmcId() {
		return uplineTmcId;
	}
	
	public List<TmcPoint> getPoints() {
		return points;
	}

	public void setPoints(List<TmcPoint> points) {
		this.points = points;
	}

	public void setUplineTmcId(int uplineTmcId) {
		this.uplineTmcId = uplineTmcId;
	}
}
