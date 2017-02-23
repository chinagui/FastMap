package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * FM-A04-03-01
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1) Lifecycle=3（新增）；
 *     (2) Lifecycle=2（更新）且name字段修改；
 *     检查原则：
 *     name中存在全角或半角{"$","￥","？"}字符的，全部报出。
 *     提示：非法字符，请重新核实照片
 * @author zhangxiaoyi
 */
public class FMA040301 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			if(!isCheck(poiObj)){return;}
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			String tmpStr=CheckUtil.strQ2B(nameStr);
			if(tmpStr.contains("$")||tmpStr.contains("￥")||tmpStr.contains("?")||tmpStr.contains("？")){
				setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
	/**
	 * 只针对改名称、新增的POI true
	 * @param poiObj
	 * @return
	 */
	private boolean isCheck(IxPoiObj poiObj){
		//存在IX_POI_NAME新增或者修改履历
		IxPoiName br=poiObj.getOfficeOriginCHName();
		if(br!=null){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}
			}
		}
		return false;
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
