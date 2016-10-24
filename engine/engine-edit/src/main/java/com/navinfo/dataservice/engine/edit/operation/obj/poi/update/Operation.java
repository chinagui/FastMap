package com.navinfo.dataservice.engine.edit.operation.obj.poi.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameFlag;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameTone;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiVideo;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private IxPoi ixPoi;

	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	public Operation(Command command, IxPoi ixPoi, Connection conn) {
		this.command = command;

		this.ixPoi = ixPoi;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		boolean isChanged = ixPoi.fillChangeFields(content);

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(ixPoi, ObjStatus.DELETE, ixPoi.pid());

				return null;
			} else {

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

		return null;
	}

	private void updataIxPoiAddress(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("addresses")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("addresses");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiAddress row = ixPoi.addressMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiAddress不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiAddress row = new IxPoiAddress();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiAddressId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiAudio(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("audioes")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("audioes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiAudio row = ixPoi.audioMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiAudio不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiContact(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("contacts")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("contacts");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiContact row = ixPoi.contactMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiContact不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiEntryimage(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("entryImages")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("entryImages");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiEntryimage row = ixPoi.entryImageMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiEntryimage不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiFlag(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("flags")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("flags");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiFlag row = ixPoi.flagMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiFlag不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiIcon(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("icons")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("icons");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiIcon row = ixPoi.iconMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiIcon不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiIcon row = new IxPoiIcon();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiIconId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiName(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("names")) {
			return;
		}

		JSONArray subObj = content.getJSONArray("names");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			String objStatus = json.getString("objStatus");

			IxPoiName row = null;
			// 新增
			if (ObjStatus.INSERT.toString().equals(objStatus)) {

				row = new IxPoiName();

				row.Unserialize(json);

				row.setPid(PidUtil.getInstance().applyPoiNameId());

				row.setPoiPid(ixPoi.getPid());

				row.setMesh(ixPoi.mesh());

				result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());

			} else {

				row = ixPoi.nameMap.get(json.getString("rowId"));

				if (row == null) {
					throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiName不存在");
				}
				// 删除
				if (ObjStatus.DELETE.toString().equals(objStatus)) {

					result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

					continue;
				}
				// 更新
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {

					boolean isChanged = row.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
					}
				}
			}

			// 维护IxPoiNameTone、IxPoiNameFlag
			if (row != null) {

				updataIxPoiNameTones(result, row, content);

				updataIxPoiNameFlags(result, row, content);
			}
		}
	}

	private void updataIxPoiNameTones(Result result, IxPoiName poiName, JSONObject content) throws Exception {
		if (!content.containsKey("nameTones")) {
			return;
		}

		JSONArray subObj = content.getJSONArray("nameTones");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			String objStatus = json.getString("objStatus");
			// 新增
			if (ObjStatus.INSERT.toString().equals(objStatus)) {

				IxPoiNameTone row = new IxPoiNameTone();

				row.Unserialize(json);

				row.setNameId(poiName.getPid());

				row.setMesh(ixPoi.mesh());

				result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());

				continue;
			}

			if (!json.containsKey("rowId")) {
				continue;
			}

			IxPoiNameTone row = poiName.nameToneMap.get(json.getString("rowId"));

			if (row == null) {
				throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiNameTone不存在");
			}

			// 删除
			if (ObjStatus.DELETE.toString().equals(objStatus)) {

				result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

				continue;
			}
			// 修改
			if (ObjStatus.UPDATE.toString().equals(objStatus)) {

				boolean isChanged = row.fillChangeFields(json);

				if (isChanged) {
					result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
				}
			}
		}
	}

	private void updataIxPoiNameFlags(Result result, IxPoiName poiName, JSONObject content) throws Exception {
		if (!content.containsKey("nameFlags")) {
			return;
		}

		JSONArray subObj = content.getJSONArray("nameFlags");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			String objStatus = json.getString("objStatus");
			// 新增
			if (ObjStatus.INSERT.toString().equals(objStatus)) {

				IxPoiNameFlag row = new IxPoiNameFlag();

				row.Unserialize(json);

				row.setNameId(poiName.getPid());

				row.setMesh(ixPoi.mesh());

				result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());

				continue;
			}

			if (!json.containsKey("rowId")) {
				continue;
			}

			IxPoiNameFlag row = poiName.nameFlagMap.get(json.getString("rowId"));

			if (row == null) {
				throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiNameFlag不存在");
			}

			// 删除
			if (ObjStatus.DELETE.toString().equals(objStatus)) {

				result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

				continue;
			}
			// 修改
			if (ObjStatus.UPDATE.toString().equals(objStatus)) {

				boolean isChanged = row.fillChangeFields(json);

				if (isChanged) {
					result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
				}
			}
		}
	}

	private void updataIxPoiPhoto(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("photos")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("photos");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiPhoto row = ixPoi.photoMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiPhoto不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiVideo(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("videoes")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("videoes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiVideo row = ixPoi.videoMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiVideo不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiParking(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("parkings")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("parkings");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiParking row = ixPoi.parkingMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiParking不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiParking row = new IxPoiParking();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiParkingsId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiDetail(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("details")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("details");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiDetail row = ixPoi.detailMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiDetail不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiBusinessTime(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("businesstimes")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("businesstimes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiBusinessTime row = ixPoi.businesstimeMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiBusinessTime不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiChargingStation(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("chargingstations")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("chargingstations");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiChargingStation row = ixPoi.chargingstationMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiChargingStation不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiChargingStation row = new IxPoiChargingStation();

					row.Unserialize(json);

					// row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiChargingPlot(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("chargingplots")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("chargingplots");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiChargingPlot row = ixPoi.chargingplotMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiChargingPlot不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiChargingPlotPh(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("chargingplotPhs")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("chargingplotPhs");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiChargingPlotPh row = ixPoi.chargingplotPhMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiChargingPlotPh不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiBuilding(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("buildings")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("buildings");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiBuilding row = ixPoi.buildingMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiBuilding不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	private void updataIxPoiAdvertisement(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("advertisements")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("advertisements");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiAdvertisement row = ixPoi.advertisementMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiAdvertisement不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiAdvertisement row = new IxPoiAdvertisement();

					row.Unserialize(json);

					// row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiGasstation(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("gasstations")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("gasstations");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiGasstation row = ixPoi.gasstationMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiGasstation不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiGasstation row = new IxPoiGasstation();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiGasstationId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiIntroduction(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("introductions")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("introductions");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiIntroduction row = ixPoi.introductionMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiIntroduction不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiIntroduction row = new IxPoiIntroduction();

					row.Unserialize(json);

					// row.setPid(0);

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiAttraction(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("attractions")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("attractions");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiAttraction row = ixPoi.attractionMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiAttraction不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiAttraction row = new IxPoiAttraction();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiAttractionId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiHotel(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("hotels")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("hotels");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiHotel row = ixPoi.hotelMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiHotel不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiHotel row = new IxPoiHotel();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiHotelId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiRestaurant(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("restaurants")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("restaurants");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiRestaurant row = ixPoi.restaurantMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiRestaurant不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
						}
					}
				} else {
					IxPoiRestaurant row = new IxPoiRestaurant();

					row.Unserialize(json);

					row.setPid(PidUtil.getInstance().applyPoiRestaurantId());

					row.setPoiPid(ixPoi.getPid());

					row.setMesh(ixPoi.mesh());

					result.insertObject(row, ObjStatus.INSERT, ixPoi.pid());
				}
			}
		}

	}

	private void updataIxPoiCarrental(Result result, JSONObject content) throws Exception {
		if (!content.containsKey("carrentals")) {
			return;
		}
		JSONArray subObj = content.getJSONArray("carrentals");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					IxPoiCarrental row = ixPoi.carrentalMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的IxPoiCarrental不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, ixPoi.pid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, ixPoi.pid());
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

	/**
	 * 打断link维护poi关系
	 * 
	 * @param oldLink
	 * @param newLinks
	 * @param result
	 * @throws Exception
	 */
	public void breakLinkForPoi(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

		IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);

		List<IxPoi> poiList = ixPoiSelector.loadIxPoiByLinkPid(oldLink.getPid(), true);

		for (IxPoi ixPoi : poiList) {
			RdLink resultLink = breakPoiGuideLink(ixPoi, oldLink, newLinks);

			if (resultLink != null) {
				ixPoi.changedFields().put("linkPid", resultLink.getPid());

				result.insertObject(ixPoi, ObjStatus.UPDATE, ixPoi.getPid());
			}
		}
	}

	/**
	 * 针对修行移动的打断
	 * 
	 * @throws Exception
	 */
	public void updateLinkSideForPoi(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

		IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);

		List<IxPoi> poiList = ixPoiSelector.loadIxPoiByLinkPid(oldLink.getPid(), true);

		for (IxPoi ixPoi : poiList) {
			if (ixPoi == null) {
				return;
			}

			RdLink resultLink = breakPoiGuideLink(ixPoi, oldLink, newLinks);

			if (resultLink != null) {
				ixPoi.changedFields().put("linkPid", resultLink.getPid());

				updatePoiGuideLinkSide(ixPoi, resultLink);
			} else {
				updatePoiGuideLinkSide(ixPoi, newLinks.get(0));
			}

			result.insertObject(ixPoi, ObjStatus.UPDATE, ixPoi.getPid());
		}

	}

	/**
	 * 更新poi在引导link的的方位
	 * 
	 * @param ixPoi
	 *            poi对象
	 * @param rdLink
	 *            引导link对象
	 * @throws Exception
	 */
	private void updatePoiGuideLinkSide(IxPoi ixPoi, RdLink rdLink) throws Exception {
		Geometry poiGeo = ixPoi.getGeometry();

		Geometry linkGeo = rdLink.getGeometry();

		Coordinate nearestPoint = GeometryUtils.GetNearestPointOnLine(poiGeo.getCoordinate(), linkGeo);

		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { nearestPoint.x, nearestPoint.y });

		Geometry nearestPointGeo = GeoTranslator.geojson2Jts(geojson, 1, 0);

		int side = GeometryUtils.calulatPointSideOflink(poiGeo, linkGeo, nearestPointGeo);

		if (side != 0) {
			Geometry guidePoint = GeoTranslator.transform(nearestPointGeo, 0.00001, 5);

			ixPoi.changedFields().put("xGuide", guidePoint.getCoordinate().x);

			ixPoi.changedFields().put("yGuide", guidePoint.getCoordinate().y);

			ixPoi.changedFields().put("side", side);
		}
	}

	/**
	 * 打断link维护poi关系的公共方法,不加入result
	 * 
	 * @param oldLink
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private RdLink breakPoiGuideLink(IxPoi ixPoi, RdLink oldLink, List<RdLink> newLinks) throws Exception {
		RdLink resultLink = null;

		if (ixPoi != null && newLinks.size() > 1) {
			double xGuide = ixPoi.getxGuide();

			double yGuide = ixPoi.getyGuide();

			JSONObject geojson = new JSONObject();

			geojson.put("type", "Point");

			geojson.put("coordinates", new double[] { xGuide, yGuide });

			Geometry point = GeoTranslator.geojson2Jts(geojson, 100000, 0);

			for (RdLink newLink : newLinks) {
				if (newLink.getGeometry().isWithinDistance(point, 1)) {
					resultLink = newLink;
					break;
				}
			}
		}

		return resultLink;
	}
}
