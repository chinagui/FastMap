package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @Title: GLM60490
 * @Package: com.navinfo.dataservice.engine.edit.check
 * @Description:
检查条件：
	非删除POI对象
检查原则：
	(1)如果一个POI有chain,但是他的等级比B1低，则报LOG1,做了品牌的数据，等级未提高；
	(2)如果一个POI对应分类在SC_POINT_CODE2LEVEL的OLD_POI_LEVEL中不存在B1，且其无chain，但是数据的等级为B1，则报LOG2，未做品牌的数据，等级被提高；
logMSG: LOG1:做了品牌的数据，等级未提高；
		LOG2:未做品牌的数据，等级被提高；
 * @Author: LittleDog
 * @Date: 2017年10月19日
 * @Version: V1.0
 */
public class GLM60490 extends BasicCheckRule {

	public static MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void run() throws Exception {
		List<String> levelList = Arrays.asList(new String[] { "B2", "B3", "B4", "C" });
		Map<String, String> map = metadataApi.scPointCode2LevelOld();

		for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
			BasicObj basicObj = entryRow.getValue();

			// 已删除的数据不检查
			if (basicObj.opType().equals(OperationType.PRE_DELETED)) {
				continue;
			}

			if (basicObj.objName().equals(ObjectName.IX_POI)) {
				IxPoiObj poiObj = (IxPoiObj) basicObj;
				IxPoi poi = (IxPoi) poiObj.getMainrow();

				String level = poi.getLevel();
				if (StringUtils.isNotEmpty(poi.getChain())) {
					if (StringUtils.isNotEmpty(level) && levelList.contains(level)) {
						setCheckResult(basicObj, "做了品牌的数据，等级未提高!");
					}
				} else {
					String kindCode = poi.getKindCode();
					if (StringUtils.isNotEmpty(kindCode) && map.containsKey(kindCode)) {
						String oldPoiLevel = map.get(kindCode);
						if (oldPoiLevel.indexOf("B1") == -1 && "B1".equals(level)) {
							setCheckResult(basicObj, "未做品牌的数据，等级被提高!");
						}
					}
				}
			}
		}
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
