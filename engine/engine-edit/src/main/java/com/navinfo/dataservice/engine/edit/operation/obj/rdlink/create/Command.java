package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {

	protected Logger log = Logger.getLogger(this.getClass());

	private String requester;

	private Geometry geometry;

	private int eNodePid;

	private int sNodePid;

	private int kind = 7;

	private int laneNum = 2;
	
	private int width = 55;
	
	private int laneClass =1;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLaneClass() {
		return laneClass;
	}

	public void setLaneClass(int laneClass) {
		this.laneClass = laneClass;
	}

	private JSONArray catchLinks;

	private List<Map<String, Object>> mapListJson;

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public void seteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public void setsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public JSONArray getCatchLinks() {
		return catchLinks;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");

		this.eNodePid = data.getInt("eNodePid");

		this.sNodePid = data.getInt("sNodePid");

		try {
			this.geometry = GeoTranslator.geojson2Jts(
					data.getJSONObject("geometry"), 1, 5);
		} catch (Exception e) {
			String msg = e.getLocalizedMessage();

			log.error(e.getMessage(), e);

			if (msg.contains("found 1 - must be 0 or >= 2")) {
				throw new Exception("线至少包含两个点");
			} else {
				throw new Exception(msg);
			}
		}

		if (data.containsKey("kind")) {
			this.kind = data.getInt("kind");
		}

		if(data.containsKey("width")){
			this.width = data.getInt("width");
		}
		if(data.containsKey("laneClass")){
			this.laneClass = data.getInt("laneClass");
		}

		if (data.containsKey("laneNum")) {
			this.laneNum = data.getInt("laneNum");

			autoSet( data );
		}


		if (data.containsKey("catchLinks")) {

			JSONArray jsonArray = JSONArray.fromObject(data
					.getJSONArray("catchLinks"));

			mapListJson = (List) jsonArray;

			this.catchLinks = jsonArray;

		} else {
			this.catchLinks = new JSONArray();
		}
	}

	/**
	 * 自动维护道路幅宽、车道等级
	 */
	private void autoSet(JSONObject data) {

		if (!data.containsKey("laneClass")) {
			//创建link时道路方向为双方向，且左右车道数为0
			int flagValue = this.laneNum % 2 == 0 ? this.laneNum / 2 : (this.laneNum + 1) / 2;

			if (flagValue == 0) {
				this.laneClass = 0;
			} else if (flagValue == 1) {
				this.laneClass = 1;
			} else if (flagValue == 2 || flagValue == 3) {
				this.laneClass = 2;
			} else if (flagValue > 4) {
				this.laneClass = 3;
			}
		}

		if (!data.containsKey("width")) {
			//创建link时左右车道均为0 只需根据总车道数计算
			if (this.laneNum == 0) {
				this.width = 0;
			} else if (this.laneNum == 1) {
				this.width = 30;
			} else if (this.laneNum == 2 || this.laneNum == 3) {
				this.width = 55;
			} else if (this.laneNum >= 4) {
				this.width = 130;
			}
			this.width = data.getInt("width");
		}
	}

	public List<Map<String, Object>> getMapListJson() {
		return mapListJson;
	}

	public void setMapListJson(List<Map<String, Object>> mapListJson) {
		this.mapListJson = mapListJson;
	}

	
}
