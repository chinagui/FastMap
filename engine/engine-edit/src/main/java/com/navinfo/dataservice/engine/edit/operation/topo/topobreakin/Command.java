package com.navinfo.dataservice.engine.edit.operation.topo.topobreakin;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class Command extends AbstractCommand {

	private String requester;
	private GeometryFactory geometryFactory = new GeometryFactory();

	@Override
	public OperType getOperType() {
		return OperType.TOPOBREAK;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	/**
	 * 需打断的links
	 */
	private List<Integer> linkPids = new ArrayList<>();

	public List<Integer> getLinkPids() {
		return this.linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	/**
	 * 
	 */
	private List<IRow> noNeedBreakLinks = new ArrayList<>();
	
	public List<IRow> getNoNeedBreakLinks(){
		return noNeedBreakLinks;
	}
	
	public void setNoNeedBreakLinks(List<IRow> links){
		this.noNeedBreakLinks = links;
	}
	
	/**
	 * 打断点
	 */
	private Point breakPoint;

	public Point getBreakPoint() {
		return this.breakPoint;
	}

	public void setBreakPoint(Point point) {
		this.breakPoint = point;
	}

	/**
	 * 自动打断区域node的Pid
	 */
	private int breakNodePid;

	public int getBreakNodePid() {
		return this.breakNodePid;
	}

	public void setBreakNodePid(int nodePid) {
		this.breakNodePid = nodePid;
	}

	/**
	 * 打断link是否需要修形
	 */
	private boolean isModifyGeo = false;

	public boolean getIsModifyGeo() {
		return isModifyGeo;
	}

	public void setIsModifyGeo(boolean isModifyGeo) {
		this.isModifyGeo = isModifyGeo;
	}

	/**
	 * Constructor
	 * 
	 * @param json
	 * @param requester
	 * @throws JSONException
	 */
	public Command(JSONObject json, String requester) throws JSONException {
		this.requester = requester;

		JSONArray objIds = json.getJSONArray("objId");
		for (int i = 0; i < objIds.size(); i++) {
			this.linkPids.add(objIds.getInt(i));
		}

		JSONObject data = json.getJSONObject("data");

		if (data.containsKey("nodePid")) {
			this.setBreakNodePid(data.getInt("nodePid"));
		}

		this.setDbId(json.getInt("dbId"));

		Coordinate coord = new Coordinate(data.getDouble("longitude"), data.getDouble("latitude"));
		this.breakPoint = geometryFactory.createPoint(coord);
	}
}
