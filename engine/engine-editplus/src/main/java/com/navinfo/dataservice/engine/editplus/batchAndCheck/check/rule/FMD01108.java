package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 *  FM-D01-108 	子号、楼栋号、楼门号、楼层、房间号”字段存在小写字母检查	DHM
 *  检查条件：
		非删除POI对象
	检查原则：
	 中文地址 IX_POI_ADDRESS表中lang_code为CHI或CHT，子号（subnum）、楼栋号（building）、
	 楼门（unit）、楼层（floor）、房间号（room）字段中存在小写全角字母时，报LOG：**（字段）存在小写字母！
 *	sunjiawei
 */
public class FMD01108 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		String subnumStr = address.getSubnum();
//		String buildingStr = address.getBuilding();
		String unitStr = address.getUnit();
		String floorStr = address.getFloor();
		String roomStr = address.getRoom();
		String errorStr = "";
		if(StringUtils.isNotBlank(subnumStr)){
			if(judgeMacher(subnumStr)){
				errorStr+="子号、";
			}
		}
//		if(StringUtils.isNotBlank(buildingStr)){
//			if(judgeMacher(buildingStr)){
//				errorStr+="楼栋号、";
//			}
//		}
		if(StringUtils.isNotBlank(unitStr)){
			if(judgeMacher(unitStr)){
				errorStr+="楼门、";
			}
		}
		if(StringUtils.isNotBlank(floorStr)){
			if(judgeMacher(floorStr)){
				errorStr+="楼层、";
			}
		}
		if(StringUtils.isNotBlank(roomStr)){
			if(judgeMacher(roomStr)){
				errorStr+="房间号、";
			}
		}
		
		if(StringUtils.isNotBlank(errorStr)){
			errorStr = errorStr.substring(0,errorStr.lastIndexOf("、"));
			setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),errorStr+"(字段)存在小写字母！");
			return;
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}
	
	
	public boolean judgeMacher(String str){
		Pattern p = Pattern.compile(".*[ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ]+.*");
		Matcher m = p.matcher(str);
		return m.matches();
	}
	
}
