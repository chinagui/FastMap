package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName GLM60289
 * @author Han Shaoming
 * @date 2017年2月21日 下午3:15:37
 * @Description TODO
 * 检查条件： 非删除POI对象；
 * 检查原则：
 * 对于同一关系中多分类同属性(多义性)(IX_SAMEPOI.Relation_type=1)，且是同一组数据，只能包含两条数据，
 * 其他情况报出Log：制作同一关系的同一组数据内，包含数据条数不能是除2以外的其他数量
 */
public class GLM60289 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_SAMEPOI)){
			IxSamePoiObj poiObj=(IxSamePoiObj) obj;
			IxSamepoi poi=(IxSamepoi) poiObj.getMainrow();
			if(poi.getRelationType()!=1){return;}
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			if(parts==null||parts.size()!=2){
				String targets="";
				for(IxSamepoiPart p:parts){
					if(!targets.isEmpty()){
						targets=targets+";";
					}
					targets=targets+"[IX_POI,"+p.getPoiPid()+"]";
				}
				setCheckResult("", targets,0);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

}
