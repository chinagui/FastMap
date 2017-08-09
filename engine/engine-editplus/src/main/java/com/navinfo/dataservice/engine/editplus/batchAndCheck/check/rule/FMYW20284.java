package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20284
 * @author Han Shaoming
 * @date 2017年2月28日 下午1:30:03
 * @Description TODO
 * 检查条件：  非删除POI对象
 * 检查原则：
 * 火车站(分类为230126)、停车场POI（停车场230210、换乘停车场（P+R停车场）230213、货车专用停车场230214）、火车站的进出站口（客运火车站230103、
 * 火车站出发到达230105、货运火车站230107）、机场的出发到达（机场出发/到达230127、机场出发/到达门230128）、普通道路服务区\停车区（普通道路服务区/停车区230230）、
 * 收费站（收费站230208）、加油站（加油站230215）、加气站（加气站230216）、充电站（电动汽车充电站230218）、充电桩（电动汽车充电桩230227）、
 * 紧急停车带（紧急停车带230229）、超限超载检测站（超限超载检测站230219）、刹车失灵缓冲区（刹车失灵缓冲区230220）、
 * 刹车冷却区（刹车冷却区230221）POI关联link为10级路(rd_link.kind=10),
 * 则报log：重要POI关联到10级道路，请确认！
 */
public class FMYW20284 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		List<String> kindCodeList = Arrays.asList("230126","230210","230213","230214","230103","230105","230107","230127","230128","230230","230208","230215","230216","230218","230227","230229","230219","230220","230221");
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			long linkPid = poi.getLinkPid();
			if(kindCodeList.contains(kindCode)){
				Map<Long, Integer> rdLink = CheckUtil.searchRdLink(linkPid, this.getCheckRuleCommand().getConn());
				if(rdLink != null && rdLink.containsKey(linkPid)){
					if(rdLink.get(linkPid) == 10){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
