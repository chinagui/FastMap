package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName GLM60210
 * @author Han Shaoming
 * @date 2017年2月20日 下午8:51:24
 * @Description TODO
 * 检查条件： 非删除POI对象且存在同一关系
 * 检查原则：
 * 同一个POI不应制作多组同一关系，否则报log：同一个POI不应制作多组同一关系！
 */
public class GLM60210 extends BasicCheckRule {
	
	private Set<Long> filterPid = new HashSet<Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			long pid = poi.getPid();
			List<Long> pids = new ArrayList<Long>();
			pids.add(pid);
			List<Long> samePoiGroupIdsByPids = IxPoiSelector.getIxSamePoiGroupIdsByPids(this.getCheckRuleCommand().getConn(), pids);
			if(samePoiGroupIdsByPids.size() >1){
				Set<Long> pidList = new HashSet<Long>(); 
				for (Long groupId : samePoiGroupIdsByPids) {
					List<Long> samePoiCounts = CheckUtil.getSamePoiCounts(groupId, this.getCheckRuleCommand().getConn());
					pidList.addAll(samePoiCounts);
				}
				pidList.remove(pid);
				String target="[IX_POI,"+pid+"]";
				for(Long tmp:pidList){
					target=target+";[IX_POI,"+tmp+"]";}
				if(!(filterPid.contains(pid)&&filterPid.containsAll(pidList))){
					setCheckResult(poi.getGeometry(), target,poi.getMeshId(), null);
				}
				filterPid.add(pid);
				filterPid.addAll(pidList);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
