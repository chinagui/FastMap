package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
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

		if (data.containsKey("laneNum")) {
			this.laneNum = data.getInt("laneNum");
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

	public List<Map<String, Object>> getMapListJson() {
		return mapListJson;
	}

	public void setMapListJson(List<Map<String, Object>> mapListJson) {
		this.mapListJson = mapListJson;
	}

	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		JSONArray array2 = new JSONArray();
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("A", 1);
		jsonObject2.put("B", 2);
		array2.add(jsonObject2);
		jsonObject.put("linkPid", array2);
		array.add(jsonObject);
		json.put("catchLinks", array);
		System.out.println(json);

		JSONArray catchLinks = new JSONArray();

		JSONArray array1 = json.getJSONArray("catchLinks");

		for (int i = 0; i < array1.size(); i++) {
			JSONObject jo = array1.getJSONObject(i);
			if (jo.containsKey("linkPid")) {
				JSONArray linkArray = jo.getJSONArray("linkPid");
				for (int j = 0; j < linkArray.size(); j++) {
					JSONObject geoPoint = linkArray.getJSONObject(i);
					geoPoint.put("A", 3);
					geoPoint.put("B", 4);
				}
			}
			catchLinks.add(jo);
		}
		System.out.println(catchLinks);
	}

}
