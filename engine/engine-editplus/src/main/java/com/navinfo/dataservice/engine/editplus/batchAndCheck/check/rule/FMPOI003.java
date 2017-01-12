package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM_POI_003
 * 检查原则：
 * 1、若POI官方原始中文名中包含机场(或機場)、包含出发（或出發）且以门(或門)结尾，
 * 例如：XX机场XX出发XX门，但分类不是230128(机场出发/到达门)；
 * 2、若POI官方原始中文名中包含机场(或機場)、包含到达（或到達）且以门(或門)结尾，
 * 例如：XX机场XX出发XX门，但分类不是230128(机场出发/到达门)；
 *  满足原则1或者2的数据，报出LOG。
 * @author zhangxiaoyi
 */
public class FMPOI003 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			if(kind.equals("230128")){return;}
			IxPoiName name = poiObj.getOfficeOriginCHName();
			if(name==null){return;}
			String nameStr = name.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			//若POI官方原始中文名中包含机场(或機場)、包含出发（或出發）且以门(或門)结尾，例如：XX机场XX出发XX门
			Pattern p2 = Pattern.compile(".+(机场|機場).+(出发|出發).+(门|門)$");
			Matcher m2 = p2.matcher(nameStr);
			if(m2.matches()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
			//若POI官方原始中文名中包含机场(或機場)、包含到达（或到達）且以门(或門)结尾，例如：XX机场XX出发XX门
			p2 = Pattern.compile(".+(机场|機場).+(到达|到達).+(门|門)$");
			m2 = p2.matcher(nameStr);
			if(m2.matches()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
