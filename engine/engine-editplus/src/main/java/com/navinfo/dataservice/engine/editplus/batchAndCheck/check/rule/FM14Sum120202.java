package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNominganList;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScSensitiveWordsUtils;
/**
 * FM-14Sum-12-02-02	名称错别字检查	D
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”或 分类为170101\170102\210302不检查；
 * 检查原则：
 * 1、新增POI，名称（names.nameStr)中包含敏感字（SC_SENSITIVE_WORDS表中type=2）；
 * 2、名称更新POI，名称（names.nameStr)中包含敏感字（SC_SENSITIVE_WORDS表中type=2）
 * 且POI的pid+nameStr在sc_point_mingan_list和sc_point_nomingan_list中的PID+NAME查找不到；
 * 备注：
 * 1、配置表中两列关键字、分类和行政区划四个字段，需要同时满足有值列的条件（%表示通配符）
 * 2、配置表中有三个关键字为“三个及三个以上连续数字”需要做特殊处理（如：第一个关键字为“连续三位及三位以上数字”）
 * 3、第二关键字可能会存在“不为XX”的关键字，需要做特殊处理（如：<> %精%）
 * @author zhangxiaoyi
 *
 */
public class FM14Sum120202 extends BasicCheckRule {
	private Map<Long, Long> adminMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind.equals("170101")||kind.equals("170102")||kind.equals("210302")){return;}
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			String pidName=poi.getPid()+"|"+nameStr;
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			if(poi.getHisOpType().equals(OperationType.INSERT)
					||(nameObj.getHisOpType().equals(OperationType.UPDATE)&&nameObj.hisOldValueContains(IxPoiName.NAME)
							&&!api.scPointMinganListPidNameList().contains(pidName))){
				List<ScSensitiveWordsObj> compareList = api.scSensitiveWordsMap().get(2);
				//type:1-POI 地址,2-POI 名称及其他
				int admin = 0;
				if(adminMap.containsKey(poi.getPid())){
					admin = Integer.valueOf(adminMap.get(poi.getPid()).toString());
				}
				List<ScSensitiveWordsObj> wordList = ScSensitiveWordsUtils.matchSensitiveWords(nameStr, poi.getKindCode(), admin, compareList,2);	
				if(wordList==null||wordList.isEmpty()){return;}
				List<String> errorlist = new ArrayList<String>();
				for(ScSensitiveWordsObj errorTmp:wordList){
					if(errorTmp.getSensitiveWord2()==null||errorTmp.getSensitiveWord2().isEmpty()){
						errorlist.add(errorTmp.getSensitiveWord());
					}else{
						errorlist.add(errorTmp.getSensitiveWord()+","+errorTmp.getSensitiveWord2());
					}
				}
				if(nameObj.getHisOpType().equals(OperationType.UPDATE)&&nameObj.hisOldValueContains(IxPoiName.NAME)
						&&ScPointNominganList.scPointNominganListPidNameList(pidName)){return;}
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "名称中包含敏感字："+errorlist.toString().replace("[", "").replace("]", ""));
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		adminMap = IxPoiSelector.getAdminIdByPids(getCheckRuleCommand().getConn(),pidList);
	}

}
