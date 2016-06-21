package com.navinfo.dataservice.engine.edit.edit.operation;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.mq.MsgPublisher;

public class PoiMsgPublisher {

	public static void publish(Result result) throws Exception {

		publishInsertMsg(result);

		publishUpdateMsg(result);

		publishDeleteMsg(result);
	}

	private static void publishInsertMsg(Result result) throws Exception {

		for (IRow row : result.getAddObjects()) {
			if (row.objType() == ObjType.IXPOI) {
				if (row.objType() == ObjType.IXPOI) {

					IxPoi poi = (IxPoi) row;

					JSONObject msg = new JSONObject();

					msg.put("pid", poi.getPid());
					msg.put("status", ObjStatus.INSERT.toString());
					msg.put("type", result.getOperStage().toString());
					msg.put("name", poi.getOldName());
					msg.put("address", poi.getOldAddress());
					msg.put("kindCode", poi.getKindCode());
					msg.put("geometry",
							GeoTranslator.jts2Wkt(poi.getGeometry()));

					MsgPublisher
							.publish2WorkQueue("notify_poi", msg.toString());
				} else if (row.objType() == ObjType.IXPOINAME) {

					IxPoiName name = (IxPoiName) row;

					if (name.getNameType() == 2 && name.getNameClass() == 1)
						if (name.getLangCode().equals("CHI")
								|| name.getLangCode().equals("CHT")) {

							JSONObject msg = new JSONObject();

							msg.put("pid", name.getPoiPid());
							msg.put("status", ObjStatus.UPDATE.toString());
							msg.put("type", result.getOperStage().toString());
							msg.put("name", name.getName());

							MsgPublisher.publish2WorkQueue("notify_poi",
									msg.toString());
						}
				} else if (row.objType() == ObjType.IXPOIADDRESS) {

					IxPoiAddress address = (IxPoiAddress) row;

					if (address.getLangCode().equals("CHI")
							|| address.getLangCode().equals("CHT")) {

						JSONObject msg = new JSONObject();

						msg.put("pid", address.getPoiPid());
						msg.put("status", ObjStatus.UPDATE.toString());
						msg.put("type", result.getOperStage().toString());
						msg.put("address", address.getFullname());

						MsgPublisher.publish2WorkQueue("notify_poi",
								msg.toString());
					}
				}
			}
		}
	}

	private static void publishUpdateMsg(Result result) throws Exception {

		for (IRow row : result.getUpdateObjects()) {
			if (row.objType() == ObjType.IXPOI) {

				IxPoi poi = (IxPoi) row;

				JSONObject msg = new JSONObject();

				msg.put("pid", poi.getPid());
				msg.put("status", ObjStatus.UPDATE.toString());
				msg.put("type", result.getOperStage().toString());

				if (poi.changedFields().containsKey("oldName")) {
					msg.put("name", poi.changedFields().get("oldName"));
				}
				if (poi.changedFields().containsKey("oldAddress")) {
					msg.put("oldAddress", poi.changedFields().get("oldAddress"));
				}
				if (poi.changedFields().containsKey("kindCode")) {
					msg.put("kindCode", poi.changedFields().get("kindCode"));
				}
				if (poi.changedFields().containsKey("geometry")) {
					msg.put("geometry", poi.changedFields().get("geometry"));
				}

				MsgPublisher.publish2WorkQueue("notify_poi", msg.toString());
			}
			else if (row.objType() == ObjType.IXPOINAME) {

				IxPoiName name = (IxPoiName) row;

				if (name.getNameType() == 2 && name.getNameClass() == 1)
					if (name.getLangCode().equals("CHI")
							|| name.getLangCode().equals("CHT")) {

						JSONObject msg = new JSONObject();

						msg.put("pid", name.getPoiPid());
						msg.put("status", ObjStatus.UPDATE.toString());
						msg.put("type", result.getOperStage().toString());
						msg.put("name", name.getName());

						MsgPublisher.publish2WorkQueue("notify_poi",
								msg.toString());
					}
			} else if (row.objType() == ObjType.IXPOIADDRESS) {

				IxPoiAddress address = (IxPoiAddress) row;

				if (address.getLangCode().equals("CHI")
						|| address.getLangCode().equals("CHT")) {

					JSONObject msg = new JSONObject();

					msg.put("pid", address.getPoiPid());
					msg.put("status", ObjStatus.UPDATE.toString());
					msg.put("type", result.getOperStage().toString());
					msg.put("address", address.getFullname());

					MsgPublisher.publish2WorkQueue("notify_poi",
							msg.toString());
				}
			}
		}
	}

	private static void publishDeleteMsg(Result result) throws Exception {

		for (IRow row : result.getDelObjects()) {
			if (row.objType() == ObjType.IXPOI) {

				IxPoi poi = (IxPoi) row;

				JSONObject msg = new JSONObject();

				msg.put("pid", poi.getPid());
				msg.put("status", ObjStatus.DELETE.toString());
				msg.put("type", result.getOperStage().toString());

				MsgPublisher.publish2WorkQueue("notify_poi", msg.toString());
			}
		}
	}
}
