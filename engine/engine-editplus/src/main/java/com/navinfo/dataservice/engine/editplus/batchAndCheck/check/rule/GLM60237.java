package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * @ClassName GLM60237
 * @author Han Shaoming
 * @date 2017年2月21日 上午9:06:34
 * @Description TODO
 * 检查条件：制作了多类别同属性的同一关系的POI(非删除POI对象且存在同一关系且IX_SAMEPOI.RELATION_TYPE=1)
 * 检查原则：
 * 在同一组关系中，如果原始官方中文名称相同的POI，这些POI的显示坐标点位距离不应超过5米，
 * 否则报log:确认是否应该制作同一关系!
 */
public class GLM60237 extends BasicCheckRule {

	private Map<Long, Long> samePoiMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		samePoiMap = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(samePoiMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
