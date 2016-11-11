package com.navinfo.dataservice.dao.glm.model.rd.link;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class RdLink implements IObj {

	private int pid;

	private int sNodePid;

	private int eNodePid;

	private int direct = 1;

	private int kind = 7;

	private int laneNum = 2;

	private int laneLeft;

	private int laneRight;

	private int functionClass = 5;

	private int appInfo = 1;

	private int tollInfo = 2;

	private int routeAdopt = 2;
	
	private int multiDigitized =0;

	private int developState;

	private int imiCode;

	private int specialTraffic;

	private int urban;

	private int paveStatus;

	private int laneWidthLeft = 1;

	private int laneWidthRight = 1;

	private int laneClass = 1;

	private int width = 50;

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
	private int adasMemo;

	public int getAdasMemo() {
		return adasMemo;
	}

	public void setAdasMemo(int adasMemo) {
		this.adasMemo = adasMemo;
	}

	public int getFeeStd() {
		return feeStd;
	}

	public void setFeeStd(int feeStd) {
		this.feeStd = feeStd;
	}

	public int getFeeFlag() {
		return feeFlag;
	}

	public void setFeeFlag(int feeFlag) {
		this.feeFlag = feeFlag;
	}

	public int getSystemId() {
		return systemId;
	}

	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}

	private String difGroupid;

	private int srcFlag = 6;

	private int digitalLevel;

	private int editFlag = 1;

	private int truckFlag;
	
	private int feeStd;
	private int feeFlag;
	private int systemId;
	private int originLinkPid;

	private int centerDivider;

	private int parkingFlag;

	private String memo;

	private Geometry geometry;

	private String rowId;

	// private String name;

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

	// public String getName() {
	// return name;
	// }
	//
	// public void setName(String name) {
	// this.name = name;
	// }

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {
			JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

			JSONObject json = JSONObject.fromObject(this, jsonConfig);

			JSONArray array = new JSONArray();

			for (IRow speedlimit : this.getSpeedlimits()) {
				array.add(speedlimit.Serialize(objLevel));
			}

			json.put("speedlimits", array);

			return json;
		} else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("pid", pid);

			// json.put("name", name);

			json.put("sNodePid", sNodePid);

			json.put("eNodePid", eNodePid);

			json.put("direct", direct);

			json.put("kind", kind);

			json.put("geometry", GeoTranslator.jts2Geojson(geometry, 0.00001, 5));

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

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

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
		
		this.setsNodePid(sourceLink.getsNodePid());

		this.seteNodePid(sourceLink.geteNodePid());

		this.setKind(sourceLink.getKind());

		this.setDirect(sourceLink.getDirect());

		this.setAppInfo(sourceLink.getAppInfo());

		this.setTollInfo(sourceLink.getTollInfo());

		this.setRouteAdopt(sourceLink.getRouteAdopt());

		this.setMultiDigitized(sourceLink.getMultiDigitized());

		this.setDevelopState(sourceLink.getDevelopState());

		this.setImiCode(sourceLink.getImiCode());

		this.setSpecialTraffic(sourceLink.getSpecialTraffic());

		this.setFunctionClass(sourceLink.getFunctionClass());

		this.setUrban(sourceLink.getUrban());

		this.setPaveStatus(sourceLink.getPaveStatus());

		this.setLaneLeft(sourceLink.getLaneLeft());

		this.setLaneNum(sourceLink.getLaneNum());

		this.setLaneRight(sourceLink.getLaneRight());

		this.setLaneWidthLeft(sourceLink.getLaneWidthLeft());

		this.setLaneWidthRight(sourceLink.getLaneWidthRight());

		this.setLaneClass(sourceLink.getLaneClass());

		this.setWidth(sourceLink.getWidth());

		this.setIsViaduct(sourceLink.getIsViaduct());

		this.setLeftRegionId(sourceLink.getLeftRegionId());

		this.setRightRegionId(sourceLink.getRightRegionId());

		this.setGeometry(sourceLink.getGeometry());

		this.setLength(sourceLink.getLength());

		this.setMeshId(sourceLink.getMeshId());

		this.setOnewayMark(sourceLink.getOnewayMark());

		this.setStreetLight(sourceLink.getStreetLight());

		this.setParkingLot(sourceLink.getParkingLot());

		this.setAdasFlag(sourceLink.getAdasFlag());

		this.setSidewalkFlag(sourceLink.getSidewalkFlag());

		this.setWalkstairFlag(sourceLink.getWalkstairFlag());

		this.setDiciType(sourceLink.getDiciType());

		this.setWalkFlag(sourceLink.getWalkFlag());

		this.setDifGroupid(sourceLink.getDifGroupid());

		this.setSrcFlag(sourceLink.getSrcFlag());

		this.setDigitalLevel(sourceLink.getDigitalLevel());

		this.setEditFlag(sourceLink.getEditFlag());

		this.setTruckFlag(sourceLink.getTruckFlag());

		this.setOriginLinkPid(sourceLink.getOriginLinkPid());

		this.setCenterDivider(sourceLink.getCenterDivider());

		this.setParkingFlag(sourceLink.getParkingFlag());

		this.setMemo(sourceLink.getMemo());

		// this.setName(sourceLink.getName());

		List<IRow> formsSource = sourceLink.getForms();

		List<IRow> forms = new ArrayList<IRow>();

		for (IRow fs : formsSource) {

			RdLinkForm f = new RdLinkForm();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			forms.add(f);
		}

		this.setForms(forms);

		List<IRow> limitsSource = sourceLink.getLimits();

		List<IRow> limits = new ArrayList<IRow>();

		for (IRow fs : limitsSource) {

			RdLinkLimit f = new RdLinkLimit();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			limits.add(f);
		}

		this.setLimits(limits);

		List<IRow> namesSource = sourceLink.getNames();

		List<IRow> names = new ArrayList<IRow>();

		for (IRow fs : namesSource) {

			RdLinkName f = new RdLinkName();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			names.add(f);
		}

		this.setNames(names);

		List<IRow> intRticsSources = sourceLink.getIntRtics();

		List<IRow> intRtics = new ArrayList<IRow>();

		for (IRow fs : intRticsSources) {

			RdLinkIntRtic f = new RdLinkIntRtic();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			intRtics.add(f);
		}

		this.setIntRtics(intRtics);
		
		
		List<IRow> rticsSources = sourceLink.getRtics();

		List<IRow> rtics = new ArrayList<IRow>();

		for (IRow fs : rticsSources) {

			RdLinkRtic f = new RdLinkRtic();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			rtics.add(f);
		}

		this.setRtics(rtics);		

		List<IRow> limitTrucksSources = sourceLink.getLimitTrucks();

		List<IRow> limitTrucks = new ArrayList<IRow>();

		for (IRow fs : limitTrucksSources) {

			RdLinkLimitTruck f = new RdLinkLimitTruck();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			limitTrucks.add(f);
		}

		this.setLimitTrucks(limitTrucks);

		List<IRow> speedlimitSources = sourceLink.getSpeedlimits();

		List<IRow> speedlimits = new ArrayList<IRow>();

		for (IRow fs : speedlimitSources) {

			RdLinkSpeedlimit f = new RdLinkSpeedlimit();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			speedlimits.add(f);
		}

		this.setSpeedlimits(speedlimits);

		List<IRow> zoneSources = sourceLink.getZones();

		List<IRow> zones = new ArrayList<IRow>();

		for (IRow fs : zoneSources) {

			RdLinkZone f = new RdLinkZone();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			zones.add(f);
		}

		this.setZones(zones);
		
		
		
		List<IRow> sidewalkSources = sourceLink.getSidewalks();

		List<IRow> sidewalks = new ArrayList<IRow>();

		for (IRow fs : sidewalkSources) {

			RdLinkSidewalk f = new RdLinkSidewalk();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			sidewalks.add(f);
		}

		this.setSidewalks(sidewalks);
		
		
		List<IRow> walkstairSources = sourceLink.getWalkstairs();

		List<IRow> walkstairs = new ArrayList<IRow>();

		for (IRow fs : walkstairSources) {

			RdLinkWalkstair f = new RdLinkWalkstair();

			f.copy(fs);

			f.setLinkPid(this.getPid());

			walkstairs.add(f);
		}

		this.setWalkstairs(walkstairs);		
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
	public String parentPKName() {

		return "link_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getForms());

		children.add(this.getNames());

		children.add(this.getLimits());

		children.add(this.getIntRtics());

		children.add(this.getLimitTrucks());

		children.add(this.getSpeedlimits());

		children.add(this.getZones());

		children.add(this.getRtics());

		children.add(this.getSidewalks());

		children.add(this.getWalkstairs());

		return children;
	}

	@Override
	public String parentTableName() {

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

				JSONObject geojson = json.getJSONObject(key);

				String wkt = Geojson.geojson2Wkt(geojson.toString());

				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);

				if (!wkt.equals(oldwkt)) {
					double length = GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(geojson));

					changedFields.put("length", length);

					changedFields.put(key, json.getJSONObject(key));
				}
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
						Object value = json.get(key);

						if (value instanceof String) {
							changedFields.put(key, newValue.replace("'", "''"));
						} else {
							changedFields.put(key, value);
						}

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

	@Override
	public int mesh() {
		return this.meshId;
	}

	@Override
	public void setMesh(int mesh) {
	}

	@Override
	public String primaryKey() {
		return "link_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdLinkName.class, names);
		childList.put(RdLinkForm.class, forms);
		childList.put(RdLinkLimit.class, limits);
		childList.put(RdLinkIntRtic.class, intRtics);
		childList.put(RdLinkRtic.class, rtics);
		childList.put(RdLinkLimitTruck.class, limitTrucks);
		childList.put(RdLinkSidewalk.class, sidewalks);
		childList.put(RdLinkSpeedlimit.class, speedlimits);
		childList.put(RdLinkWalkstair.class, walkstairs);
		childList.put(RdLinkZone.class, zones);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
		childMap.put(RdLinkName.class, nameMap);
		childMap.put(RdLinkForm.class, formMap);
		childMap.put(RdLinkLimit.class, limitMap);
		childMap.put(RdLinkIntRtic.class, intRticMap);
		childMap.put(RdLinkRtic.class, rticMap);
		childMap.put(RdLinkLimitTruck.class, limitTruckMap);
		childMap.put(RdLinkSidewalk.class, sidewalkMap);
		childMap.put(RdLinkSpeedlimit.class, speedlimitMap);
		childMap.put(RdLinkWalkstair.class, walkstairMap);
		childMap.put(RdLinkZone.class, zoneMap);
		return childMap;
	}

}
