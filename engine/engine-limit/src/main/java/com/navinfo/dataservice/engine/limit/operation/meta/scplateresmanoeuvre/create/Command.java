package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;

	private String groupId = "";// GROUP_ID
	private String vehicle = "";// VEHICLE
	private String attribution = "";// ATTRIBUTION
	private String restrict = "";// RESTRICT
	private int tempPlate = 1;// TEMP_PLATE
	private String tempPlateNum = "";// TEMP_PLATE_NUM
	private int charSwitch = 1;// CHAR_SWITCH
	private String charToNum = "";// CHAR_TO_NUM
	private String tailNumber = "";// TAIL_NUMBER
	private String platecolor = "";// PLATECOLOR
	private String energyType = "";// ENERGY_TYPE
	private String gasEmisstand = "";// GAS_EMISSTAND
	private int seatnum = 0;// SEATNUM
	private double vehicleLength = 0.0;// VEHICLE_LENGTH
	private double resWeigh = 0.0;// RES_WEIGH
	private double resAxleLoad = 0.0;// RES_AXLE_LOAD
	private int resAxleCount = 0;// RES_AXLE_COUNT
	private String startDate = "";// START_DATE
	private String endDate = "";// END_DATE
	private String resDatetype = "";// RES_DATETYPE
	private String time = "";// TIME
	private String specFlag = "";// SPEC_FLAG

	public String getGroupId() {
		return groupId;
	}

	public String getVehicle() {
		return vehicle;
	}

	public String getAttribution() {
		return attribution;
	}

	public String getRestrict() {
		return restrict;
	}

	public int getTempPlate() {
		return tempPlate;
	}

	public String getTempPlateNum() {
		return tempPlateNum;
	}

	public int getCharSwitch() {
		return charSwitch;
	}

	public String getCharToNum() {
		return charToNum;
	}

	public String getTailNumber() {
		return tailNumber;
	}

	public String getPlatecolor() {
		return platecolor;
	}

	public String getEnergyType() {
		return energyType;
	}

	public String getGasEmisstand() {
		return gasEmisstand;
	}

	public int getSeatnum() {
		return seatnum;
	}

	public double getVehicleLength() {
		return vehicleLength;
	}

	public double getResWeigh() {
		return resWeigh;
	}

	public double getResAxleLoad() {
		return resAxleLoad;
	}

	public int getResAxleCount() {
		return resAxleCount;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getResDatetype() {
		return resDatetype;
	}

	public String getTime() {
		return time;
	}

	public String getSpecFlag() {
		return specFlag;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		//this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");
		this.groupId = data.getString("groupId");
		this.vehicle = data.getString("vehicle");
		this.attribution = data.getString("attribution");
		this.restrict = data.getString("restrict");
		this.tempPlate = data.getInt("tempPlate");
		this.tempPlateNum = data.getString("tempPlateNum");
		this.charSwitch = data.getInt("charSwitch");
		this.charToNum = data.getString("charToNum");
		this.tailNumber = data.getString("tailNumber");
		this.platecolor = data.getString("plateColor");
		this.energyType = data.getString("energyType");
		this.gasEmisstand = data.getString("gasEmisstand");
		this.seatnum = data.getInt("seatNum");
		this.vehicleLength = data.getDouble("vehicleLength");
		this.resWeigh = data.getDouble("resWeigh");
		this.resAxleCount = data.getInt("resAxleCount");
		this.resAxleLoad = data.getDouble("resAxleLoad");
		this.startDate = data.getString("startDate");
		this.endDate = data.getString("endDate");
		this.resDatetype = data.getString("resDatetype");
		this.time = data.getString("time");
		this.specFlag = data.getString("specFlag");
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public DbType getDbType() {
		return DbType.LIMITDB;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		return LimitObjType.SCPLATERESMANOEUVRE;
	}
}
