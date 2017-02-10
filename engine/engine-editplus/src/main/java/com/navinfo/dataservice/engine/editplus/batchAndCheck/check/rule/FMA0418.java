package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;

/**
 * 检查条件： 以下条件其中之一满足时，需要进行检查： 
 * (1)标准化中文名称新增；
 * (2)标准化中文名称修改； 
 * 检查原则：
 * 检查SC_POINT_NAMECK表中TYPE=5且HM_FLAG<>’HM’名称包含关键字且拼音与配置表中拼音不相同的报出
 * 检查SC_POINT_NAMECK表中TYPE=7且HM_FLAG<>’HM’只要名称中包含关键字的报出。
 * 提示：POI标准化名称常见多音字检查：POI标准化名称中含有多音字“xx”
 * 检查名称：标准化中文名称（type=1，class={1,3,5,6}，langCode=CHI）
 * 
 * @author gaopengrong
 */
public class FMA0418 extends BasicCheckRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 存在IX_POI_NAME新增或者修改履历
			List<IxPoiName> names = poiObj.getStandardCHIName();
			for (IxPoiName name : names) {
				if (name.getHisOpType().equals(OperationType.INSERT)
						|| (name.getHisOpType().equals(OperationType.UPDATE)
								&& name.hisOldValueContains(IxPoiName.NAME))) {
					String newNameStr = name.getName();
					String newNamePhonetic = name.getNamePhonetic();
					// 检查SC_POINT_NAMECK表中TYPE=5且HM_FLAG<>’HM’名称包含关键字且拼音与配置表中拼音不相同的报出
					MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					Map<String, String> typeD5 = metadataApi.scPointNameckTypeD5();
					Map<String, String> keyResult5 = ScPointNameckUtil.matchType(newNameStr, typeD5);
					for (String preKey : keyResult5.keySet()) {
						if (!newNamePhonetic.contains(keyResult5.get(preKey))) {
							String log = "POI标准化名称中含有多音字“" + preKey + "”,且拼音与配置表中拼音不相同";
							setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), log);
						}
					}
					// 检查SC_POINT_NAMECK表中TYPE=7且HM_FLAG<>’HM’只要名称中包含关键字的报出
					Map<String, String> typeD7 = metadataApi.scPointNameckTypeD7();
					Map<String, String> keyResult7 = ScPointNameckUtil.matchType(newNameStr, typeD7);
					if (keyResult7.size() != 0) {
						String log = "POI标准化名称中包含关键字“" + (keyResult7.keySet()).toString() + "”";
						setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), log);
					}
				}
			}
		}
	}

}
