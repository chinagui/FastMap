package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-YW-20-214
 * 检查条件：
 *   Lifecycle！=1（删除）
 * 检查原则：
 * 1.名称为“公厕”或“公廁”，分类不为公共厕所；
 * 2.名称为“充电桩”或“充電樁”，分类不为充电桩；
 * 3.分类为公共厕所，名称不为“公厕”或“公廁”；
 * 4.分类为充电桩，名称不为“充电桩”或“充電樁”；
 * 5.名称为“报刊亭”，分类不为“报刊零售（130403）”；
 * 6.名称含“麦当劳甜品店”，分类不为“冷饮店（110302）”
 * 7.名称含“自行车租赁点”，分类不为“租赁服务（200200）”
 * 以上原则满足其一，即报log
 * KINDCODE_WC=210215
 * KINDCODE_CHARGE=230227
 * @author zhangxiaoyi
 */
public class FMYW20214 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			if((!kind.equals("210215")&&(nameStr.equals("公厕")||nameStr.equals("公廁")))
					||(!kind.equals("230227")&&(nameStr.equals("充电桩")||nameStr.equals("充電樁")))
					||(kind.equals("210215")&&!(nameStr.equals("公厕")||nameStr.equals("公廁")))
					||(kind.equals("230227")&&!(nameStr.equals("充电桩")||nameStr.equals("充電樁")))
					||(!kind.equals("130403")&&nameStr.equals("报刊亭"))
					||(!kind.equals("110302")&&nameStr.contains("麦当劳甜品店"))
					||(!kind.equals("200200")&&nameStr.contains("自行车租赁点"))){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {}

}
