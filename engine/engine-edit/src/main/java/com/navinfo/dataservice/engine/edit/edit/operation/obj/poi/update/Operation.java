package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlotPh;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAudio;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEntryimage;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiFlag;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiIcon;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiOperateRef;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiVideo;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {

	private Command command;

	private IxPoi ixPoi;

	public Operation(Command command, IxPoi ixPoi) {
		this.command = command;

		this.ixPoi = ixPoi;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(ixPoi, ObjStatus.DELETE, ixPoi.pid());

				return null;
			} else {

				boolean isChanged = ixPoi.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(ixPoi, ObjStatus.UPDATE, ixPoi.pid());
				}
			}
		}

		updataIxPoiAddress(result, content);

		updataIxPoiAudio(result, content);

		updataIxPoiContact(result, content);

		updataIxPoiEntryimage(result, content);

		updataIxPoiFlag(result, content);

		updataIxPoiIcon(result, content);

		updataIxPoiName(result, content);

		updataIxPoiPhoto(result, content);

		updataIxPoiVideo(result, content);

		updataIxPoiParking(result, content);

		updataIxPoiDetail(result, content);

		updataIxPoiBusinessTime(result, content);

		updataIxPoiChargingStation(result, content);

		updataIxPoiChargingPlot(result, content);

		updataIxPoiChargingPlotPh(result, content);

		updataIxPoiBuilding(result, content);

		updataIxPoiAdvertisement(result, content);

		updataIxPoiGasstation(result, content);

		updataIxPoiIntroduction(result, content);

		updataIxPoiAttraction(result, content);

		updataIxPoiHotel(result, content);

		updataIxPoiRestaurant(result, content);

		updataIxPoiCarrental(result, content);
		
		updataIxPoiOperateRef( result,  content);

		return null;
	}

	private void updataIxPoiAddress(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("addresses")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("addresses");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiAddress row = ixPoi.addressMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiAddress不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiAddress row = new IxPoiAddress();

					row.Unserialize(json);

					row.setPid(PidService.getInstance().applyPoiAddressId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiAudio(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("audioes")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("audioes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiAudio row = ixPoi.audioMap
							.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiAudio不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiAudio row = new IxPoiAudio();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiContact(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("contacts")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("contacts");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiContact row = ixPoi.contactMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiContact不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiContact row = new IxPoiContact();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiEntryimage(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("entryImages")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("entryImages");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiEntryimage row = ixPoi.entryImageMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiEntryimage不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiEntryimage row = new IxPoiEntryimage();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiFlag(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("flags")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("flags");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiFlag row = ixPoi.flagMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiFlag不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiFlag row = new IxPoiFlag();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiIcon(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("icons")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("icons");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiIcon row = ixPoi.iconMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiIcon不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiIcon row = new IxPoiIcon();

					row.Unserialize(json);

					row.setPid(PidService.getInstance().applyPoiIconId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiName(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("names")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("names");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiName row = ixPoi.nameMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiName不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiName row = new IxPoiName();

					row.Unserialize(json);

					row.setPid(PidService.getInstance().applyPoiNameId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiPhoto(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("photos")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("photos");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiPhoto row = ixPoi.photoMap
							.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiPhoto不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiPhoto row = new IxPoiPhoto();

					row.Unserialize(json);				

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiVideo(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("videoes")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("videoes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiVideo row = ixPoi.videoMap
							.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiVideo不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiVideo row = new IxPoiVideo();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiParking(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("parkings")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("parkings");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiParking row = ixPoi.parkingMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiParking不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiParking row = new IxPoiParking();

					row.Unserialize(json);

					//row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiDetail(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("details")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("details");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiDetail row = ixPoi.detailMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiDetail不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiDetail row = new IxPoiDetail();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiBusinessTime(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("businesstimes")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("businesstimes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiBusinessTime row = ixPoi.businesstimeMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiBusinessTime不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiBusinessTime row = new IxPoiBusinessTime();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiChargingStation(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("chargingstations")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("chargingstations");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiChargingStation row = ixPoi.chargingstationMap
							.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiChargingStation不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiChargingStation row = new IxPoiChargingStation();

					row.Unserialize(json);

					//row.setPid(0);
					
					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiChargingPlot(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("chargingplots")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("chargingplots");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiChargingPlot row = ixPoi.chargingplotMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiChargingPlot不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiChargingPlot row = new IxPoiChargingPlot();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiChargingPlotPh(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("chargingplotPhs")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("chargingplotPhs");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiChargingPlotPh row = ixPoi.chargingplotPhMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiChargingPlotPh不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiChargingPlotPh row = new IxPoiChargingPlotPh();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiBuilding(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("buildings")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("buildings");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiBuilding row = ixPoi.buildingMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiBuilding不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiBuilding row = new IxPoiBuilding();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiAdvertisement(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("advertisements")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("advertisements");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiAdvertisement row = ixPoi.advertisementMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiAdvertisement不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiAdvertisement row = new IxPoiAdvertisement();

					row.Unserialize(json);

					//row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiGasstation(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("gasstations")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("gasstations");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiGasstation row = ixPoi.gasstationMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiGasstation不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiGasstation row = new IxPoiGasstation();

					row.Unserialize(json);

					//row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiIntroduction(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("introductions")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("introductions");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiIntroduction row = ixPoi.introductionMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiIntroduction不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiIntroduction row = new IxPoiIntroduction();

					row.Unserialize(json);

					//row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiAttraction(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("attractions")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("attractions");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiAttraction row = ixPoi.attractionMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiAttraction不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiAttraction row = new IxPoiAttraction();

					row.Unserialize(json);

					row.setPid(PidService.getInstance().applyPoiAttractionId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiHotel(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("hotels")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("hotels");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiHotel row = ixPoi.hotelMap
							.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiHotel不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiHotel row = new IxPoiHotel();

					row.Unserialize(json);

					row.setPid(PidService.getInstance().applyPoiHotelId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiRestaurant(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("restaurants")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("restaurants");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiRestaurant row = ixPoi.restaurantMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiRestaurant不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiRestaurant row = new IxPoiRestaurant();

					row.Unserialize(json);

					row.setPid(PidService.getInstance().applyPoiRestaurantId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiCarrental(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("carrentals")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("carrentals");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiCarrental row = ixPoi.carrentalMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiCarrental不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiCarrental row = new IxPoiCarrental();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiOperateRef(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("operateRefs")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("operateRefs");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					IxPoiOperateRef row = ixPoi.operateRefMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的IxPoiOperateRef不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									ixPoi.pid());
						}
					}
				} else {
					IxPoiOperateRef row = new IxPoiOperateRef();

					row.Unserialize(json);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

}
