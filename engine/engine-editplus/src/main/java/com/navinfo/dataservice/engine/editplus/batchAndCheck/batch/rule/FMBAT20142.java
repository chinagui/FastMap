package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 
 * 当中文地址18个中文字段更新时，将18个字段分别转拼音
 * @author gaopengrong
 */
public class FMBAT20142 extends BasicBatchRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		if(chiAddress==null){return;} 
		if(!chiAddress.getHisOpType().equals(OperationType.UPDATE)){return;}
		
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

		//PROVINCE
		if(chiAddress.hisOldValueContains(IxPoiAddress.PROVINCE)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.PROVINCE);
			String newValue=chiAddress.getProvince();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setProvPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//CITY
		if(chiAddress.hisOldValueContains(IxPoiAddress.CITY)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.CITY);
			String newValue=chiAddress.getCity();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setCityPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//COUNTY
		if(chiAddress.hisOldValueContains(IxPoiAddress.COUNTY)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.COUNTY);
			String newValue=chiAddress.getCounty();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setCountyPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//TOWN
		if(chiAddress.hisOldValueContains(IxPoiAddress.TOWN)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.TOWN);
			String newValue=chiAddress.getTown();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setTownPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//PLACE
		if(chiAddress.hisOldValueContains(IxPoiAddress.PLACE)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.PLACE);
			String newValue=chiAddress.getPlace();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setPlacePhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//STREET
		if(chiAddress.hisOldValueContains(IxPoiAddress.STREET)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.STREET);
			String newValue=chiAddress.getStreet();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setStreetPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//LANDMARK
		if(chiAddress.hisOldValueContains(IxPoiAddress.LANDMARK)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.LANDMARK);
			String newValue=chiAddress.getLandmark();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setLandmarkPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//PREFIX
		if(chiAddress.hisOldValueContains(IxPoiAddress.PREFIX)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.PREFIX);
			String newValue=chiAddress.getPrefix();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setPrefixPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//HOUSENUM
		if(chiAddress.hisOldValueContains(IxPoiAddress.HOUSENUM)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.HOUSENUM);
			String newValue=chiAddress.getHousenum();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setHousenumPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//TYPE
		if(chiAddress.hisOldValueContains(IxPoiAddress.TYPE)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.TYPE);
			String newValue=chiAddress.getType();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setTypePhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//SUBNUM
		if(chiAddress.hisOldValueContains(IxPoiAddress.SUBNUM)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.SUBNUM);
			String newValue=chiAddress.getSubnum();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setSubnumPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//SURFIX
		if(chiAddress.hisOldValueContains(IxPoiAddress.SURFIX)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.SURFIX);
			String newValue=chiAddress.getSurfix();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setSurfixPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//ESTAB
		if(chiAddress.hisOldValueContains(IxPoiAddress.ESTAB)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.ESTAB);
			String newValue=chiAddress.getEstab();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setEstabPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//BUILDING
		if(chiAddress.hisOldValueContains(IxPoiAddress.BUILDING)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.BUILDING);
			String newValue=chiAddress.getBuilding();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setBuildingPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//UNIT
		if(chiAddress.hisOldValueContains(IxPoiAddress.UNIT)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.UNIT);
			String newValue=chiAddress.getUnit();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setUnitPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//FLOOR
		if(chiAddress.hisOldValueContains(IxPoiAddress.FLOOR)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.FLOOR);
			String newValue=chiAddress.getFloor();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setFloorPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//ROOM
		if(chiAddress.hisOldValueContains(IxPoiAddress.ROOM)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.ROOM);
			String newValue=chiAddress.getRoom();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setRoomPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
		//ADDONS
		if(chiAddress.hisOldValueContains(IxPoiAddress.ADDONS)){
			String oldValue=(String) chiAddress.getHisOldValue(IxPoiAddress.ADDONS);
			String newValue=chiAddress.getAddons();
			if(!newValue.equals(oldValue)){
				//批拼音
				chiAddress.setAddonsPhonetic(metadataApi.pyConvert(newValue)[1]);
			}
		}
	}

}
