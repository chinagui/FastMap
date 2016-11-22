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
 * @ClassName: TmcPoint
 * @author Zhang Xiaolong
 * @date 2016年11月15日 下午4:16:00
 * @Description: TODO
 */
public class TmcPoint {
	private int tmcId;

	// 位置表标识
	private String cid;

	// 位置表代码
	private int loctableId;

	// 位置代码
	private int locCode;

	// 类型代码
	private String typeCode;

	// 正向进入道路
	private int inPos;

	// 负向进入道路
	private int inNeg;

	// 正向离开道路
	private int outPos;

	// 负向离开道路
	private int outNeg;

	// 道路正向出现
	private int presentPos;

	// 道路负向出现
	private int presentNeg;

	private String translateName;

	// 道路参考
	private int lineTmcId;

	// 区域参考
	private int areaTmcId;

	// 正向偏移
	private int locoffPos;

	// 负向偏移
	private int locoffNeg;

	// 邻接范围代码
	private String neighbourBound;

	// 邻接表代码
	private int neighbourTable;

	// 是否城市内
	private int urban;

	// 是否打断道路
	private int interuptRoad;

	// 编辑标识
	private int editFlag;
	
	private JSONArray geometry;
	
	private List<TmcPointName> names = new ArrayList<>();

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
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
	
	public String getTranslateName() {
		return translateName;
	}

	public void setTranslateName(String translateName) {
		this.translateName = translateName;
	}

	public int getLineTmcId() {
		return lineTmcId;
	}

	public void setLineTmcId(int lineTmcId) {
		this.lineTmcId = lineTmcId;
	}

	public int getAreaTmcId() {
		return areaTmcId;
	}

	public void setAreaTmcId(int areaTmcId) {
		this.areaTmcId = areaTmcId;
	}

	public JSONArray getGeometry() {
		return geometry;
	}

	public void setGeometry(JSONArray geometry) {
		this.geometry = geometry;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public int getLoctableId() {
		return loctableId;
	}

	public void setLoctableId(int loctableId) {
		this.loctableId = loctableId;
	}

	public int getLocCode() {
		return locCode;
	}

	public void setLocCode(int locCode) {
		this.locCode = locCode;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public int getInPos() {
		return inPos;
	}

	public void setInPos(int inPos) {
		this.inPos = inPos;
	}

	public int getInNeg() {
		return inNeg;
	}

	public void setInNeg(int inNeg) {
		this.inNeg = inNeg;
	}

	public int getOutPos() {
		return outPos;
	}

	public void setOutPos(int outPos) {
		this.outPos = outPos;
	}

	public int getOutNeg() {
		return outNeg;
	}

	public void setOutNeg(int outNeg) {
		this.outNeg = outNeg;
	}

	public int getPresentPos() {
		return presentPos;
	}

	public void setPresentPos(int presentPos) {
		this.presentPos = presentPos;
	}

	public int getPresentNeg() {
		return presentNeg;
	}

	public void setPresentNeg(int presentNeg) {
		this.presentNeg = presentNeg;
	}

	public String getNeighbourBound() {
		return neighbourBound;
	}

	public void setNeighbourBound(String neighbourBound) {
		this.neighbourBound = neighbourBound;
	}

	public int getNeighbourTable() {
		return neighbourTable;
	}

	public void setNeighbourTable(int neighbourTable) {
		this.neighbourTable = neighbourTable;
	}

	public int getUrban() {
		return urban;
	}
	
	public List<TmcPointName> getNames() {
		return names;
	}

	public void setNames(List<TmcPointName> names) {
		this.names = names;
	}

	public void setUrban(int urban) {
		this.urban = urban;
	}

	public int getInteruptRoad() {
		return interuptRoad;
	}

	public void setInteruptRoad(int interuptRoad) {
		this.interuptRoad = interuptRoad;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}
	
	public List<List<TmcPointName>> children() {
		List<List<TmcPointName>> children = new ArrayList<List<TmcPointName>>();
		children.add(this.names);
		return children;
	}

	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		JsonConfig jsonConfig = Geojson.geoJsonConfig(1, 5);

		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {

			JSONObject json = JSONObject.fromObject(this, jsonConfig);

			return json;
		}
		return JSONObject.fromObject(this, jsonConfig);
	}
}
