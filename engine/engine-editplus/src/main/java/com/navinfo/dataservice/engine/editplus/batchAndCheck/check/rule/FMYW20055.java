package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * FM-YW-20-055
 * 检查条件：
 *   Lifecycle！=1（删除）
 * 检查原则：
 * 驾校报名处分类（分类为160108）的POI名称，不能以“驾校”或“驾驶学校”或“駕駛學校”结尾，否则报log：驾校报名处分类不能以“驾校”或“驾驶学校”或“駕駛學校”结尾。
 * @author zhangxiaoyi
 */
public class FMYW20055 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(!kind.equals("160108")){return;}
			
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			String tmpStr=CheckUtil.strQ2B(nameStr);
			Pattern p = Pattern.compile(".*(驾校|驾驶学校|駕駛學校)+$");
			Matcher m = p.matcher(tmpStr);
			if(m.matches()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {}

}
