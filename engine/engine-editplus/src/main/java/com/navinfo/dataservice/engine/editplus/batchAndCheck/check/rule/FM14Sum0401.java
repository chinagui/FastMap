package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM14Sum0401
 * @author Han Shaoming
 * @date 2017年2月27日 上午11:29:25
 * @Description TODO
 * 检查条件：非删除POI对象；
 * 检查原则：
 * 根据分类业务逻辑控制表，查询出C级的设施没有标记C级。
 * 1、在分类业务逻辑控制表中查询POI的分类对应的level(sc_point_code2level.new_poi_level)，
 * 如果查询出的level为C级POI，而数据中的level不是C级，报log：C级分类没有标记C级
 * 备注：当kindcode="120101、230210、230213、230214"时不检查
 */
public class FM14Sum0401 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode==null||"120101".equals(kindCode)||"230210".equals(kindCode)
					||"230213".equals(kindCode)||"230214".equals(kindCode)){return;}
			String level = poi.getLevel();
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> scPointCode2LevelMap = metadataApi.scPointCode2Level(kindCode);
			String scPointCode2Level = scPointCode2LevelMap.get(kindCode);
			if("C".equals(scPointCode2Level)&&!"C".equals(level)){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
