package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * 
 * 当中文地址18个中文字段更新时，将18个字段分别转拼音
 * 
 * @author gaopengrong ==========================
 * @author z
 * @desc 修复bug8921 履历中取出的值为null,转String会报异常 java.lang.ClassCastException:
 *       net.sf.json.JSONNull cannot be cast to java.lang.String
 */
public class FMBAT20142 extends BasicBatchRule {
	private Map<Long, Long> pidAdminId;

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList = new HashSet<Long>();
		for (BasicObj obj : batchDataList) {
			pidList.add(obj.objPid());
		}
		pidAdminId = IxPoiSelector.getAdminIdByPids(getBatchRuleCommand().getConn(), pidList);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		String adminCode = null;
		if (pidAdminId != null && pidAdminId.containsKey(poiObj.getMainrow().getObjPid())) {
			adminCode = pidAdminId.get(poiObj.getMainrow().getObjPid()).toString();
		}
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		if (chiAddress == null) {
			return;
		}
		if (!chiAddress.getHisOpType().equals(OperationType.UPDATE)
				&& !chiAddress.getHisOpType().equals(OperationType.INSERT)) {
			return;
		}

		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

		// PROVINCE
		if (chiAddress.hisOldValueContains(IxPoiAddress.PROVINCE)) {
			Object province = chiAddress.getHisOldValue(IxPoiAddress.PROVINCE);
			String oldValue = (province instanceof JSONNull || province == null) ? "" : province.toString();
			String newValue = chiAddress.getProvince() == null ? "" : chiAddress.getProvince();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setProvPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// CITY
		if (chiAddress.hisOldValueContains(IxPoiAddress.CITY)) {
			Object city = chiAddress.getHisOldValue(IxPoiAddress.CITY);
			String oldValue = (city instanceof JSONNull || city == null) ? "" : city.toString();
			String newValue = chiAddress.getCity() == null ? "" : chiAddress.getCity();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setCityPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// COUNTY
		if (chiAddress.hisOldValueContains(IxPoiAddress.COUNTY)) {
			Object county = chiAddress.getHisOldValue(IxPoiAddress.COUNTY);
			String oldValue = (county instanceof JSONNull || county == null) ? "" : county.toString();
			String newValue = chiAddress.getCounty() == null ? "" : chiAddress.getCounty();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setCountyPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// TOWN
		if (chiAddress.hisOldValueContains(IxPoiAddress.TOWN)) {
			Object town = chiAddress.getHisOldValue(IxPoiAddress.TOWN);
			String oldValue = (town instanceof JSONNull || town == null) ? "" : town.toString();
			String newValue = chiAddress.getTown() == null ? "" : chiAddress.getTown();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setTownPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// PLACE
		if (chiAddress.hisOldValueContains(IxPoiAddress.PLACE)) {
			Object place = chiAddress.getHisOldValue(IxPoiAddress.PLACE);
			String oldValue = (place instanceof JSONNull || place == null) ? "" : place.toString();
			String newValue = chiAddress.getPlace() == null ? "" : chiAddress.getPlace();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setPlacePhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// STREET
		if (chiAddress.hisOldValueContains(IxPoiAddress.STREET)) {
			Object street = chiAddress.getHisOldValue(IxPoiAddress.STREET);
			String oldValue = (street instanceof JSONNull || street == null) ? "" : street.toString();
			String newValue = chiAddress.getStreet() == null ? "" : chiAddress.getStreet();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setStreetPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// LANDMARK
		if (chiAddress.hisOldValueContains(IxPoiAddress.LANDMARK)) {
			Object landmark = chiAddress.getHisOldValue(IxPoiAddress.LANDMARK);
			String oldValue = (landmark instanceof JSONNull || landmark == null) ? "" : landmark.toString();
			String newValue = chiAddress.getLandmark() == null ? "" : chiAddress.getLandmark();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setLandmarkPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// PREFIX
		if (chiAddress.hisOldValueContains(IxPoiAddress.PREFIX)) {
			Object prefix = chiAddress.getHisOldValue(IxPoiAddress.PREFIX);
			String oldValue = (prefix instanceof JSONNull || prefix == null) ? "" : prefix.toString();
			String newValue = chiAddress.getPrefix() == null ? "" : chiAddress.getPrefix();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setPrefixPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// HOUSENUM
		if (chiAddress.hisOldValueContains(IxPoiAddress.HOUSENUM)) {
			Object housenum = chiAddress.getHisOldValue(IxPoiAddress.HOUSENUM);
			String oldValue = (housenum instanceof JSONNull || housenum == null) ? "" : housenum.toString();
			String newValue = chiAddress.getHousenum() == null ? "" : chiAddress.getHousenum();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setHousenumPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// TYPE
		if (chiAddress.hisOldValueContains(IxPoiAddress.TYPE)) {
			Object type = chiAddress.getHisOldValue(IxPoiAddress.TYPE);
			String oldValue = (type instanceof JSONNull || type == null) ? "" : type.toString();
			String newValue = chiAddress.getType() == null ? "" : chiAddress.getType();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setTypePhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// SUBNUM
		if (chiAddress.hisOldValueContains(IxPoiAddress.SUBNUM)) {
			Object subnum = chiAddress.getHisOldValue(IxPoiAddress.SUBNUM);
			String oldValue = (subnum instanceof JSONNull || subnum == null) ? "" : subnum.toString();
			String newValue = chiAddress.getSubnum() == null ? "" : chiAddress.getSubnum();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setSubnumPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// SURFIX
		if (chiAddress.hisOldValueContains(IxPoiAddress.SURFIX)) {
			Object surfix = chiAddress.getHisOldValue(IxPoiAddress.SURFIX);
			String oldValue = (surfix instanceof JSONNull || surfix == null) ? "" : surfix.toString();
			String newValue = chiAddress.getSurfix() == null ? "" : chiAddress.getSurfix();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setSurfixPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// ESTAB
		if (chiAddress.hisOldValueContains(IxPoiAddress.ESTAB)) {
			Object estab = chiAddress.getHisOldValue(IxPoiAddress.ESTAB);
			String oldValue = (estab instanceof JSONNull || estab == null) ? "" : estab.toString();
			String newValue = chiAddress.getEstab() == null ? "" : chiAddress.getEstab();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setEstabPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// BUILDING
		if (chiAddress.hisOldValueContains(IxPoiAddress.BUILDING)) {
			Object building = chiAddress.getHisOldValue(IxPoiAddress.BUILDING);
			String oldValue = (building instanceof JSONNull || building == null) ? "" : building.toString();
			String newValue = chiAddress.getBuilding() == null ? "" : chiAddress.getBuilding();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setBuildingPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// UNIT
		if (chiAddress.hisOldValueContains(IxPoiAddress.UNIT)) {
			Object unit = chiAddress.getHisOldValue(IxPoiAddress.UNIT);
			String oldValue = (unit instanceof JSONNull || unit == null) ? "" : unit.toString();
			String newValue = chiAddress.getUnit() == null ? "" : chiAddress.getUnit();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setUnitPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// FLOOR
		if (chiAddress.hisOldValueContains(IxPoiAddress.FLOOR)) {
			Object floor = chiAddress.getHisOldValue(IxPoiAddress.FLOOR);
			String oldValue = (floor instanceof JSONNull || floor == null) ? "" : floor.toString();
			String newValue = chiAddress.getFloor() == null ? "" : chiAddress.getFloor();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setFloorPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// ROOM
		if (chiAddress.hisOldValueContains(IxPoiAddress.ROOM)) {
			Object room = chiAddress.getHisOldValue(IxPoiAddress.ROOM);
			String oldValue = (room instanceof JSONNull || room == null) ? "" : room.toString();
			String newValue = chiAddress.getRoom() == null ? "" : chiAddress.getRoom();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setRoomPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
		// ADDONS
		if (chiAddress.hisOldValueContains(IxPoiAddress.ADDONS)) {
			Object addons = chiAddress.getHisOldValue(IxPoiAddress.ADDONS);
			String oldValue = (addons instanceof JSONNull || addons == null) ? "" : addons.toString();
			String newValue = chiAddress.getAddons() == null ? "" : chiAddress.getAddons();
			if (!newValue.equals(oldValue)) {
				// 批拼音
				chiAddress.setAddonsPhonetic(metadataApi.pyConvert(newValue, adminCode, null));
			}
		}
	}

	public static void main(String argv[]) {
		// String test = "{\"name\":null}";
		// JSONObject testObj = JSONObject.fromObject(test);
		// Object oldName = testObj.get("name");
		// System.out.println(oldName);
		// if (oldName instanceof JSONNull){
		// System.out.println("he he");
		// }
		// if(oldName != null){
		// System.out.println("ha ha");
		// }
		// System.out.println("=================");
		// Map<String, Object> test1 = new HashMap<>();
		// test1.put("name", null);
		// if(test1.containsKey("name")){
		// Object oldName1 = test1.get("name");
		// System.out.println(oldName1);
		// if(oldName1 == null){
		// System.out.println("hehe");
		// }
		// if(oldName1 instanceof JSONNull){
		// System.out.println("haha");
		// }
		// }

		Map<String, Object> oldValues = null;
		JSONObject jo = JSONObject.fromObject("{\"NAME\":null}");
		oldValues = (Map) JSONObject.toBean(jo, Map.class);
		System.out.println(oldValues.get("NAME").getClass());
		String name = String.valueOf(oldValues.get("NAME"));
		System.out.println(name);
	}

}
