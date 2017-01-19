package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScSensitiveWordsUtils;
import com.navinfo.navicommons.database.QueryRunner;
/**
 * FM-14Sum-12-01-01	名称错别字检查	D
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 1、新增POI，名称（name）中包含敏感字（SC_SENSITIVE_WORDS表中type=1）；
 * 2、名称更新POI，名称（name）中包含敏感字（SC_SENSITIVE_WORDS表中type=1）
 * 且POI的pid+name在sc_point_mingan_list和sc_point_nomingan_list中的PID+NAME查找不到；
 * 备注：
 * 1、配置表中两列关键字、分类和行政区划四个字段，需要同时满足有值列的条件（%表示通配符）
 * 2、配置表中有三个关键字为“三个及三个以上连续数字”需要做特殊处理（如：第一个关键字为“连续三位及三位以上数字”）
 * 3、第二关键字可能会存在“不为XX”的关键字，需要做特殊处理（如：<> %精%）
 * @author zhangxiaoyi
 *
 */
public class FM14Sum120101 extends BasicCheckRule {
	private Map<Long, Integer> adminMap=new HashMap<Long, Integer>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			String pidName=poi.getPid()+"|"+nameStr;
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			if(poi.getHisOpType().equals(OperationType.INSERT)
					||(nameObj.getHisOpType().equals(OperationType.UPDATE)&&nameObj.hisOldValueContains(IxPoiName.NAME)
							&&!api.scPointMinganListPidNameList().contains(pidName)
							&&!api.scPointNominganListPidNameList().contains(pidName))){
				List<ScSensitiveWordsObj> compareList = api.scSensitiveWordsMap().get(1);
				List<ScSensitiveWordsObj> wordList = ScSensitiveWordsUtils.matchSensitiveWords(nameStr, poi.getKindCode(), adminMap.get(poi.getRegionId()), compareList);	
				if(wordList==null||wordList.isEmpty()){return;}
				List<String> errorlist = new ArrayList<String>();
				for(ScSensitiveWordsObj errorTmp:wordList){
					if(errorTmp.getSensitiveWord2()==null||errorTmp.getSensitiveWord2().isEmpty()){
						errorlist.add(errorTmp.getSensitiveWord());
					}else{
						errorlist.add(errorTmp.getSensitiveWord()+","+errorTmp.getSensitiveWord2());
					}
				}
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "名称中包含敏感字："+errorlist.toString().replace("[", "").replace("]", ""));
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		List<Long> regionIdList=new ArrayList<Long>();
		for(BasicObj obj:batchDataList){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			regionIdList.add(poi.getRegionId());
		}
		String sql = "SELECT REGION_ID, ADMIN_ID FROM AD_ADMIN"
				+ " WHERE REGION_ID IN (" + StringUtils.join(regionIdList.toArray(),",") + ")";
		
		ResultSetHandler<Map<Long,Integer>> rsHandler = new ResultSetHandler<Map<Long,Integer>>() {
			public Map<Long,Integer> handle(ResultSet rs) throws SQLException {
				Map<Long,Integer> result = new HashMap<Long,Integer>();
				while (rs.next()) {
					int admin = rs.getInt("ADMIN_ID");
					long region = rs.getLong("REGION_ID");
					result.put(region, admin);
				}
				return result;
			}
		};
		adminMap = new QueryRunner().query(getCheckRuleCommand().getConn(),sql, rsHandler);
	}

}
