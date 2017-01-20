package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * GLM60447
 * 检查对象：IX_POI_CONTACT表中的一条记录，contact_type = '1'，且对应的IX_POI表记录的STATE≠1；
 * 检查原则：
 * 1.如果电话的区号（固话-前的部分）与元数据库SC_POINT_ADMINAREA表中的电话区号(areacode)相同，
 * 则检查该记录的行政区划号(admin_id)与元数据表该记录的行政区划号（adminareacode）是否相同,如果不同且两个行政区划不相邻
 * （即两个行政区划的所有构成面没有公共边），则报错；
 * @author zhangxiaoyi
 */
public class GLM60447 extends BasicCheckRule {
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		loadReferDatas(rows.values());
		if(rows==null||rows.isEmpty()){return;}
		Set<Long> pidList=new HashSet<Long>();
		Map<Long, List<String>> pidContactMap=new HashMap<Long, List<String>>();
		MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, List<String>> contactMap = api.scPointAdminareaContactMap();
		//如果电话的区号（固话-前的部分）与元数据库SC_POINT_ADMINAREA表中的电话区号(areacode)相同
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
				if(obj.objName().equals(ObjectName.IX_POI)){
					IxPoiObj poiObj=(IxPoiObj) obj;
					IxPoi poi=(IxPoi) poiObj.getMainrow();
					
					List<IxPoiContact> contacts = poiObj.getIxPoiContacts();
					for(IxPoiContact contactTmp:contacts){
						if(contactTmp.getContactType()!=1){continue;}
						String contactStr=contactTmp.getContact();
						if(contactStr==null||contactStr.isEmpty()||!contactStr.contains("-")){continue;}
						String preContact=contactStr.split("-")[0];
						if(contactMap.containsKey(preContact)){
							pidList.add(obj.objPid());
							pidContactMap.put(poi.getPid(), contactMap.get(preContact));
							break;
						}						
					}
				}
			}
		}
		//检查该记录的行政区划号(admin_id)与元数据表该记录的行政区划号（adminareacode）是否相同
		Map<Long, Long> adminMap = IxPoiSelector.getAdminIdByPids(getCheckRuleCommand().getConn(),pidList);
		List<Long> pids=new ArrayList<Long>();
		Set<String> adminList=new HashSet<String>();
		for(Long pid:pidList){
			if(pidContactMap.get(pid).contains(adminMap.get(pid).toString())){return;}
			pids.add(pid);
			adminList.addAll(pidContactMap.get(pid));
			adminList.add(adminMap.get(pid).toString());
		}
		if(adminList==null||adminList.size()==0){
			return;
		}
		//如果不同且两个行政区划不相邻,（即两个行政区划的所有构成面没有公共边），则报错
		String sqlStr="SELECT A.ADMIN_ID, T.LINK_PID"
				+ "  FROM AD_ADMIN A, AD_FACE F, AD_FACE_TOPO T"
				+ " WHERE A.REGION_ID = F.REGION_ID"
				+ "   AND F.FACE_PID = T.FACE_PID"
				+ "   AND A.ADMIN_ID IN ("+adminList.toString().replace("[", "").replace("]", "")+")";
		Connection conn = this.getCheckRuleCommand().getConn();
		PreparedStatement pstmt=conn.prepareStatement(sqlStr);
		ResultSet rs = pstmt.executeQuery();
		Map<String, Set<Long>> adminLinkMap=new HashMap<String, Set<Long>>();
		while (rs.next()) {
			String admin=rs.getString("ADMIN_ID");
			Long linkPid=rs.getLong("LINK_PID");
			if(!adminLinkMap.containsKey(admin)){
				adminLinkMap.put(admin, new HashSet<Long>());
			}
			adminLinkMap.get(admin).add(linkPid);
		}
		for(Long pid:pids){
			Set<Long> selfLink=new HashSet<Long>();
			if(!adminLinkMap.containsKey(adminMap.get(pid).toString())){continue;}
			selfLink.addAll(adminLinkMap.get(adminMap.get(pid).toString()));
			
			Set<Long> rightLink=new HashSet<Long>();
			for(String admin:pidContactMap.get(pid)){
				if(!adminLinkMap.containsKey(admin)){continue;}
				rightLink.addAll(adminLinkMap.get(admin));
			}
			
			if(rightLink==null||rightLink.isEmpty()){continue;}
			int beforeSum=selfLink.size();
			selfLink.removeAll(rightLink);
			int after=selfLink.size();
			if(selfLink==null||selfLink.isEmpty()||after<beforeSum){continue;}
			IxPoiObj poiObj=(IxPoiObj) rows.get(pid);
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),null);
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
