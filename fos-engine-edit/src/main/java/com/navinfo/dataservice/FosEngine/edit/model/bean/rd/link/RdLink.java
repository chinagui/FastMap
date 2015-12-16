package com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.FosEngine.comm.geom.GeoTranslator;
import com.navinfo.dataservice.FosEngine.comm.geom.Geojson;
import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.vividsolutions.jts.geom.Geometry;

public class RdLink implements IObj {

	private int pid;

	private int sNodePid;

	private int eNodePid;

	private int direct = 1;

	private int kind = 7;

	private int laneNum = 2;

	private int laneLeft;

	private int laneRight;

	private int multiDigitized;

	private int functionClass = 5;

	private int appInfo = 1;

	private int tollInfo = 2;

	private int routeAdopt = 2;

	private int developState;

	private int imiCode;

	private int specialTraffic;

	private int urban;

	private int paveStatus;

	private int laneWidthLeft = 1;

	private int laneWidthRight = 1;

	private int laneClass = 2;

	private int width;

	private int isViaduct;

	private int leftRegionId;

	private int rightRegionId;

	private double length;

	private int meshId;

	private int onewayMark;

	private int streetLight;

	private int parkingLot;

	private int adasFlag;

	private int sidewalkFlag;

	private int walkstairFlag;

	private int diciType;

	private int walkFlag;

	private String difGroupid;

	private int srcFlag = 6;

	private int digitalLevel;

	private int editFlag = 1;

	private int truckFlag;

	private int originLinkPid;

	private int centerDivider;

	private int parkingFlag;

	private String memo;

	private Geometry geometry;

	private String rowId;
	
	private String name;

	private List<IRow> forms = new ArrayList<IRow>();

	private List<IRow> limits = new ArrayList<IRow>();

	private List<IRow> names = new ArrayList<IRow>();
	
	private List<IRow> intRtics = new ArrayList<IRow>();

	private List<IRow> limitTrucks = new ArrayList<IRow>();

	private List<IRow> rtics = new ArrayList<IRow>();
	
	private List<IRow> sidewalks = new ArrayList<IRow>();

	private List<IRow> speedlimits = new ArrayList<IRow>();

	private List<IRow> walkstairs = new ArrayList<IRow>();
	
	private List<IRow> zones = new ArrayList<IRow>();

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public Map<String, RdLinkForm> formMap = new HashMap<String, RdLinkForm>();

	public Map<String, RdLinkLimit> limitMap = new HashMap<String, RdLinkLimit>();

	public Map<String, RdLinkName> nameMap = new HashMap<String, RdLinkName>();
	
	public Map<String, RdLinkIntRtic> intRticMap = new HashMap<String, RdLinkIntRtic>();
	
	public Map<String, RdLinkRtic> rticMap = new HashMap<String, RdLinkRtic>();
	
	public Map<String, RdLinkLimitTruck> limitTruckMap = new HashMap<String, RdLinkLimitTruck>();
	
	public Map<String, RdLinkSidewalk> sidewalkMap = new HashMap<String, RdLinkSidewalk>();
	
	public Map<String, RdLinkSpeedlimit> speedlimitMap = new HashMap<String, RdLinkSpeedlimit>();
	
	public Map<String, RdLinkWalkstair> walkstairMap = new HashMap<String, RdLinkWalkstair>();
	
	public Map<String, RdLinkZone> zoneMap = new HashMap<String, RdLinkZone>();

	public RdLink() {

	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public int getLaneLeft() {
		return laneLeft;
	}

	public void setLaneLeft(int laneLeft) {
		this.laneLeft = laneLeft;
	}

	public int getLaneRight() {
		return laneRight;
	}

	public void setLaneRight(int laneRight) {
		this.laneRight = laneRight;
	}

	public int getMultiDigitized() {
		return multiDigitized;
	}

	public void setMultiDigitized(int multiDigitized) {
		this.multiDigitized = multiDigitized;
	}

	public int getFunctionClass() {
		return functionClass;
	}

	public void setFunctionClass(int functionClass) {
		this.functionClass = functionClass;
	}

	public List<IRow> getForms() {
		return forms;
	}

	public void setForms(List<IRow> forms) {
		this.forms = forms;
	}

	public List<IRow> getLimits() {
		return limits;
	}

	public void setLimits(List<IRow> limits) {
		this.limits = limits;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	private List<Relate> relates;

	public List<Relate> getRelates() {
		return relates;
	}

	public void setRelates(List<Relate> relates) {
		this.relates = relates;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int linkPid) {
		this.pid = linkPid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public void setsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public void seteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		if (objLevel == ObjLevel.FULL) {
			JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

			JSONObject json = JSONObject.fromObject(this, jsonConfig);

			return json;
		} else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("pid", pid);

			json.put("name", name);

			json.put("sNodePid", sNodePid);

			json.put("eNodePid", eNodePid);

			json.put("direct", direct);

			json.put("kind", kind);

			json.put("geometry",
					GeoTranslator.jts2Geojson(geometry, 0.00001, 5));

			return json;
		}
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "forms":
					forms.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkForm row = new RdLinkForm();

						row.Unserialize(jo);

						forms.add(row);
					}

					break;
				case "limits":

					limits.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkLimit row = new RdLinkLimit();

						row.Unserialize(jo);

						limits.add(row);
					}

					break;

				case "names":

					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;

				default:
					break;
				}

			} else if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(
						json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else {

				if (!"objStatus".equals(key)) {
					Field f = this.getClass().getDeclaredField(key);

					f.setAccessible(true);

					f.set(this, json.get(key));

				}
			}
		}

		return true;
	}

	public static void main(String[] args) {
		RdLink a = new RdLink();

		System.out.println(JSONObject.fromObject(a));
	}

	@Override
	public String tableName() {

		return "rd_link";
	}

	@Override
	public ObjStatus status() {

		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {

	}

	@Override
	public ObjType objType() {

		return ObjType.RDLINK;
	}

	@Override
	public List<IRow> relatedRows() {

		return null;
	}

	@Override
	public void copy(IRow row) {

		RdLink sourceLink = (RdLink) row;

		this.setDirect(sourceLink.getDirect());

		this.seteNodePid(sourceLink.geteNodePid());

		this.setFunctionClass(sourceLink.getFunctionClass());

		this.setGeometry(sourceLink.getGeometry());

		this.setKind(sourceLink.getKind());

		this.setLaneLeft(sourceLink.getLaneLeft());

		this.setLaneNum(sourceLink.getLaneNum());

		this.setLaneRight(sourceLink.getLaneRight());

		this.setMultiDigitized(sourceLink.getMultiDigitized());

		this.setName(sourceLink.getName());

		this.setsNodePid(sourceLink.getsNodePid());

		List<IRow> formsSource = sourceLink.getForms();

		List<IRow> forms = new ArrayList<IRow>();

		for (IRow fs : formsSource) {

			RdLinkForm f = new RdLinkForm();

			f.setLinkPid(this.getPid());

			f.copy(fs);

			forms.add(f);
		}

		this.setForms(forms);

		List<IRow> limitsSource = sourceLink.getLimits();

		List<IRow> limits = new ArrayList<IRow>();

		for (IRow fs : limitsSource) {

			RdLinkLimit f = new RdLinkLimit();

			f.setLinkPid(this.getPid());

			f.copy(fs);

			limits.add(f);
		}

		this.setLimits(limits);

		List<IRow> namesSource = sourceLink.getNames();

		List<IRow> names = new ArrayList<IRow>();

		for (IRow fs : namesSource) {

			RdLinkName f = new RdLinkName();

			f.setLinkPid(this.getPid());

			f.copy(fs);

			names.add(f);
		}

		this.setNames(names);
	}

	public boolean isNodeOnLink(int nodePid) {

		if (nodePid == this.eNodePid || nodePid == this.sNodePid) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public int pid() {

		return this.getPid();
	}

	@Override
	public String primaryKey() {

		return "link_pid";
	}

	@Override
	public int primaryValue() {

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getForms());

		children.add(this.getNames());

		children.add(this.getLimits());

		return children;
	}

	@Override
	public String primaryTableName() {

		return "rd_link";
	}

	@Override
	public String rowId() {

		return rowId;
	}

	@Override
	public void setRowId(String rowId) {

		this.rowId = rowId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else if ("geometry".equals(key)) {
				changedFields.put(key, json.getJSONObject(key));
			} else {
				if (!"objStatus".equals(key)) {

					Field field = this.getClass().getDeclaredField(key);

					field.setAccessible(true);

					Object objValue = field.get(this);

					String oldValue = null;

					if (objValue == null) {
						oldValue = "null";
					} else {
						oldValue = String.valueOf(objValue);
					}

					String newValue = json.getString(key);

					if (!newValue.equals(oldValue)) {
						changedFields.put(key, json.get(key));

					}

				}
			}
		}

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	public int getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(int appInfo) {
		this.appInfo = appInfo;
	}

	public int getTollInfo() {
		return tollInfo;
	}

	public void setTollInfo(int tollInfo) {
		this.tollInfo = tollInfo;
	}

	public int getRouteAdopt() {
		return routeAdopt;
	}

	public void setRouteAdopt(int routeAdopt) {
		this.routeAdopt = routeAdopt;
	}

	public int getDevelopState() {
		return developState;
	}

	public void setDevelopState(int developState) {
		this.developState = developState;
	}

	public int getImiCode() {
		return imiCode;
	}

	public void setImiCode(int imiCode) {
		this.imiCode = imiCode;
	}

	public int getSpecialTraffic() {
		return specialTraffic;
	}

	public void setSpecialTraffic(int specialTraffic) {
		this.specialTraffic = specialTraffic;
	}

	public int getUrban() {
		return urban;
	}

	public void setUrban(int urban) {
		this.urban = urban;
	}

	public int getPaveStatus() {
		return paveStatus;
	}

	public void setPaveStatus(int paveStatus) {
		this.paveStatus = paveStatus;
	}

	public int getLaneWidthLeft() {
		return laneWidthLeft;
	}

	public void setLaneWidthLeft(int laneWidthLeft) {
		this.laneWidthLeft = laneWidthLeft;
	}

	public int getLaneWidthRight() {
		return laneWidthRight;
	}

	public void setLaneWidthRight(int laneWidthRight) {
		this.laneWidthRight = laneWidthRight;
	}

	public int getLaneClass() {
		return laneClass;
	}

	public void setLaneClass(int laneClass) {
		this.laneClass = laneClass;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getIsViaduct() {
		return isViaduct;
	}

	public void setIsViaduct(int isViaduct) {
		this.isViaduct = isViaduct;
	}

	public int getLeftRegionId() {
		return leftRegionId;
	}

	public void setLeftRegionId(int leftRegionId) {
		this.leftRegionId = leftRegionId;
	}

	public int getRightRegionId() {
		return rightRegionId;
	}

	public void setRightRegionId(int rightRegionId) {
		this.rightRegionId = rightRegionId;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getOnewayMark() {
		return onewayMark;
	}

	public void setOnewayMark(int onewayMark) {
		this.onewayMark = onewayMark;
	}

	public int getStreetLight() {
		return streetLight;
	}

	public void setStreetLight(int streetLight) {
		this.streetLight = streetLight;
	}

	public int getParkingLot() {
		return parkingLot;
	}

	public void setParkingLot(int parkingLot) {
		this.parkingLot = parkingLot;
	}

	public int getAdasFlag() {
		return adasFlag;
	}

	public void setAdasFlag(int adasFlag) {
		this.adasFlag = adasFlag;
	}

	public int getSidewalkFlag() {
		return sidewalkFlag;
	}

	public void setSidewalkFlag(int sidewalkFlag) {
		this.sidewalkFlag = sidewalkFlag;
	}

	public int getWalkstairFlag() {
		return walkstairFlag;
	}

	public void setWalkstairFlag(int walkstairFlag) {
		this.walkstairFlag = walkstairFlag;
	}

	public int getDiciType() {
		return diciType;
	}

	public void setDiciType(int diciType) {
		this.diciType = diciType;
	}

	public int getWalkFlag() {
		return walkFlag;
	}

	public void setWalkFlag(int walkFlag) {
		this.walkFlag = walkFlag;
	}

	public String getDifGroupid() {
		return difGroupid;
	}

	public void setDifGroupid(String difGroupid) {
		this.difGroupid = difGroupid;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public int getDigitalLevel() {
		return digitalLevel;
	}

	public void setDigitalLevel(int digitalLevel) {
		this.digitalLevel = digitalLevel;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public int getTruckFlag() {
		return truckFlag;
	}

	public void setTruckFlag(int truckFlag) {
		this.truckFlag = truckFlag;
	}

	public int getOriginLinkPid() {
		return originLinkPid;
	}

	public void setOriginLinkPid(int originLinkPid) {
		this.originLinkPid = originLinkPid;
	}

	public int getCenterDivider() {
		return centerDivider;
	}

	public void setCenterDivider(int centerDivider) {
		this.centerDivider = centerDivider;
	}

	public int getParkingFlag() {
		return parkingFlag;
	}

	public void setParkingFlag(int parkingFlag) {
		this.parkingFlag = parkingFlag;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public List<IRow> getIntRtics() {
		return intRtics;
	}

	public void setIntRtics(List<IRow> intRtics) {
		this.intRtics = intRtics;
	}

	public List<IRow> getLimitTrucks() {
		return limitTrucks;
	}

	public void setLimitTrucks(List<IRow> limitTrucks) {
		this.limitTrucks = limitTrucks;
	}

	public List<IRow> getRtics() {
		return rtics;
	}

	public void setRtics(List<IRow> rtics) {
		this.rtics = rtics;
	}

	public List<IRow> getSidewalks() {
		return sidewalks;
	}

	public void setSidewalks(List<IRow> sidewalks) {
		this.sidewalks = sidewalks;
	}

	public List<IRow> getSpeedlimits() {
		return speedlimits;
	}

	public void setSpeedlimits(List<IRow> speedlimits) {
		this.speedlimits = speedlimits;
	}

	public List<IRow> getWalkstairs() {
		return walkstairs;
	}

	public void setWalkstairs(List<IRow> walkstairs) {
		this.walkstairs = walkstairs;
	}

	public List<IRow> getZones() {
		return zones;
	}

	public void setZones(List<IRow> zones) {
		this.zones = zones;
	}

}
