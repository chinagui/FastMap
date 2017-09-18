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
 * @author gaopengrong
 * ==========================
 * @author z
 * @desc 修复bug8921 履历中取出的值为null,转String会报异常
 * java.lang.ClassCastException: net.sf.json.JSONNull cannot be cast to java.lang.String
 */
public class FMBAT20142 extends BasicBatchRule {
	private Map<Long,Long> pidAdminId;
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		pidAdminId = IxPoiSelector.getAdminIdByPids(getBatchRuleCommand().getConn(), pidList);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		String adminCode=null;
		if(pidAdminId!=null&&pidAdminId.containsKey(poiObj.getMainrow().getObjPid())){
			adminCode=pidAdminId.get(poiObj.getMainrow().getObjPid()).toString();
		}
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		if(chiAddress==null){return;} 
		if(!chiAddress.getHisOpType().equals(OperationType.UPDATE) && !chiAddress.getHisOpType().equals(OperationType.INSERT)){return;}
		
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

		//PROVINCE
		if(chiAddress.hisOldValueContains(IxPoiAddress.PROVINCE)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.PROVINCE) instanceof JSONNull?"":(String)chiAddress.getHisOldValue(IxPoiAddress.PROVINCE);
			String newValue=chiAddress.getProvince();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setProvPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//CITY
		if(chiAddress.hisOldValueContains(IxPoiAddress.CITY)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.CITY) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.CITY);
			String newValue=chiAddress.getCity();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setCityPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//COUNTY
		if(chiAddress.hisOldValueContains(IxPoiAddress.COUNTY)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.COUNTY) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.COUNTY);
			String newValue=chiAddress.getCounty();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setCountyPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//TOWN
		if(chiAddress.hisOldValueContains(IxPoiAddress.TOWN)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.TOWN) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.TOWN);
			String newValue=chiAddress.getTown();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setTownPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//PLACE
		if(chiAddress.hisOldValueContains(IxPoiAddress.PLACE)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.PLACE) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.PLACE);
			String newValue=chiAddress.getPlace();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setPlacePhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//STREET
		if(chiAddress.hisOldValueContains(IxPoiAddress.STREET)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.STREET) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.STREET);
			String newValue=chiAddress.getStreet();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setStreetPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//LANDMARK
		if(chiAddress.hisOldValueContains(IxPoiAddress.LANDMARK)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.LANDMARK) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.LANDMARK);
			String newValue=chiAddress.getLandmark();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setLandmarkPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//PREFIX
		if(chiAddress.hisOldValueContains(IxPoiAddress.PREFIX)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.PREFIX) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.PREFIX);
			String newValue=chiAddress.getPrefix();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setPrefixPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//HOUSENUM
		if(chiAddress.hisOldValueContains(IxPoiAddress.HOUSENUM)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.HOUSENUM) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.HOUSENUM);
			String newValue=chiAddress.getHousenum();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setHousenumPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//TYPE
		if(chiAddress.hisOldValueContains(IxPoiAddress.TYPE)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.TYPE) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.TYPE);
			String newValue=chiAddress.getType();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setTypePhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//SUBNUM
		if(chiAddress.hisOldValueContains(IxPoiAddress.SUBNUM)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.SUBNUM) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.SUBNUM);
			String newValue=chiAddress.getSubnum();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setSubnumPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//SURFIX
		if(chiAddress.hisOldValueContains(IxPoiAddress.SURFIX)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.SURFIX) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.SURFIX);
			String newValue=chiAddress.getSurfix();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setSurfixPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//ESTAB
		if(chiAddress.hisOldValueContains(IxPoiAddress.ESTAB)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.ESTAB) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.ESTAB);
			String newValue=chiAddress.getEstab();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setEstabPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//BUILDING
		if(chiAddress.hisOldValueContains(IxPoiAddress.BUILDING)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.BUILDING) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.BUILDING);
			String newValue=chiAddress.getBuilding();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setBuildingPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//UNIT
		if(chiAddress.hisOldValueContains(IxPoiAddress.UNIT)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.UNIT) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.UNIT);
			String newValue=chiAddress.getUnit();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setUnitPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//FLOOR
		if(chiAddress.hisOldValueContains(IxPoiAddress.FLOOR)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.FLOOR) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.FLOOR);
			String newValue=chiAddress.getFloor();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setFloorPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//ROOM
		if(chiAddress.hisOldValueContains(IxPoiAddress.ROOM)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.ROOM) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.ROOM);
			String newValue=chiAddress.getRoom();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setRoomPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
		//ADDONS
		if(chiAddress.hisOldValueContains(IxPoiAddress.ADDONS)){
			String oldValue = chiAddress.getHisOldValue(IxPoiAddress.ADDONS) instanceof JSONNull?"":(String) chiAddress.getHisOldValue(IxPoiAddress.ADDONS);
			String newValue=chiAddress.getAddons();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setAddonsPhonetic(metadataApi.pyConvert(newValue,adminCode,null));
			}
		}
	}
	
	public static void main(String argv[]){
		String test = "{\"name\":null}";
		JSONObject testObj =  JSONObject.fromObject(test);
		Object oldName = testObj.get("name");
		if (oldName instanceof JSONNull){
			System.out.println("he he");
		}
	}

}
