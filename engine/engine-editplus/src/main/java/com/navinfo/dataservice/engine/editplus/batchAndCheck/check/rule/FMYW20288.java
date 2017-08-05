package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName FMYW20288
 * @author Han Shaoming
 * @date 2017年2月28日 下午3:15:02
 * @Description TODO 检查条件： 非删除POI对象 检查原则：
 * (1)POI同点(显示坐标完全相同)，同点有部分有地址，有部分无地址，（不判断地址是否相同)报log：不存在地址，请确认！
 * (删除-20170322)(2)POI同点(显示坐标完全相同)，但是地址中“路”或“道”后面的阿拉伯数字不一致报log：地址中“路”(道)后面的数字不一致
 * ，请确认！ 注："路"与"路"后面的阿拉伯数字比较；"道"与"道"后面的阿拉伯数字比较；
 */
public class FMYW20288 extends BasicCheckRule {

	public void run() throws Exception {
		Map<Long, BasicObj> rows = getRowList();
		List<ArrayList<Long>> pid = new ArrayList<ArrayList<Long>>();
		int n = 0;
		ArrayList<Long> tmpPids = new ArrayList<>();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 已删除的数据不检查
			if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
			
			tmpPids.add(poi.getPid());
			if( n /1000 > 0){
				pid.add(tmpPids);
				n = 0;
				tmpPids = new ArrayList<>();
			}
			n++;
		}
		if (tmpPids.size() > 0){
			pid.add(tmpPids);
		}
		
		// POI同点(显示坐标完全相同)
		if (pid != null && pid.size() > 0) {
			Map<Long, Set<Long>> errorList1=new HashMap<Long, Set<Long>>();
			Map<Long,Geometry> geoMap=new HashMap<Long, Geometry>();
			Map<Long,Integer> meshMap=new HashMap<Long, Integer>();
			Connection conn = this.getCheckRuleCommand().getConn();
			for (List tmppid: pid){
				String pids = tmppid.toString().replace("[", "").replace("]", "");
				String pidString = " PID IN (" + pids + ")";
	
				String sqlStr = "SELECT P1.PID PID,P1.GEOMETRY,P1.MESH_ID,P2.PID PID2 " 
						+ " FROM IX_POI P1,IX_POI P2"
						+ " WHERE SDO_NN(P2.GEOMETRY,P1.GEOMETRY,'sdo_batch_size=0 DISTANCE=0.00001 UNIT=METER') = 'TRUE'"
						+ "	AND P1. " + pidString
						+ " AND P1.U_RECORD <>2 AND P2.U_RECORD <>2	" 
						+ " AND P1.PID <> P2.PID";
				log.info("FM-YW-20-288 sql:"+sqlStr);
				PreparedStatement pstmt = null;
				ResultSet rs = null;
			try{
				 pstmt = conn.prepareStatement(sqlStr);
				 rs = pstmt.executeQuery();
	
				while (rs.next()) {
					Long pidTmp1 = rs.getLong("PID");
					Long pidTmp2 = rs.getLong("PID2");
					int meshId = rs.getInt("MESH_ID");
					STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
					geoMap.put(pidTmp1, geometry);
					meshMap.put(pidTmp1, meshId);
					Set<Long> pidList = new HashSet<Long>();
					pidList.add(pidTmp1);
					pidList.add(pidTmp2);
					Set<String> referSubrow = new HashSet<String>();
					referSubrow.add("IX_POI_ADDRESS");
					Map<Long, BasicObj> referObjs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, referSubrow, false, pidList, false, true);
					BasicObj obj1 = referObjs.get(pidTmp1);
					IxPoiObj poiObj1 = (IxPoiObj) obj1;
					IxPoiAddress address1 = poiObj1.getCHAddress();
					BasicObj obj2 = referObjs.get(pidTmp2);
					IxPoiObj poiObj2 = (IxPoiObj) obj2;
					IxPoiAddress address2 = poiObj2.getCHAddress();
					if ((address1 == null && address2 != null) || (address1 != null && address2 == null)) {
						if(!errorList1.containsKey(pidTmp1)){errorList1.put(pidTmp1, new HashSet<Long>());}
						errorList1.get(pidTmp1).add(pidTmp2);
					}
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(rs);
				DBUtils.closeStatement(pstmt);
			}
			}
			
			//过滤相同pid
			Set<Long> filterPid1 = new HashSet<Long>();
			for(Long pid1:errorList1.keySet()){
				String targets="[IX_POI,"+pid1+"]";
				for(Long pid2:errorList1.get(pid1)){
					targets=targets+";[IX_POI,"+pid2+"]";
				}
				if(!(filterPid1.contains(pid1)&&filterPid1.containsAll(errorList1.get(pid1)))){
					setCheckResult(geoMap.get(pid1), targets, meshMap.get(pid1),"不存在地址，请确认");
				}
				filterPid1.add(pid1);
				filterPid1.addAll(errorList1.get(pid1));
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

//	private String getDigit(String param, String key) throws Exception {
//		try {
//			if (param == null || key == null || !param.contains(key)) {
//				return null;
//			}
//			String sub = null;
//			for(int i=0;i<param.length();){
//				if(!param.contains(key)){break;}
//				int index = param.indexOf(key);
//				if(index<param.length()-1){
//					String str = param.substring(index+1,index+2);					
//					if(CheckUtil.isDigit(str)){
//						sub = param.substring(index+1);
//						break;
//					}else{
//						param = param.substring(index+1);
//					}
//				}else{break;}
//				i=index;
//			}
//			if (sub != null) {
//				String regex = "\\d*";
//				Pattern p = Pattern.compile(regex);
//				Matcher m = p.matcher(sub);
//				while (m.find()) {
//					if (!"".equals(m.group())) {
//						return m.group(0);
//					}
//				}
//			}
//			return null;
//		} catch (Exception e) {
//			throw e;
//		}
//	}

}
