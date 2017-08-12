package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName FM14Sum170101
 * @author Han Shaoming
 * @date 2017年2月28日 下午5:58:32
 * @Description TODO
 * 检查条件：无；
 * 检查原则：
 * 1、POI的分类、官方原始中文名称或显示坐标修改后，且vip_flag包含2，则报log1：客户厂商**被修改，请确认！
 * 2、POI删除后，且vip_flag包含2，则报log2：客户厂商**被删除，请确认！
 * 3、新增POI的官方原始中文名称，且与其它非删除POI的官方原始中文名称相同(其它非删除POIvip_flag满足包含2)，则报log3：客户厂商**在数据中存在，请确认！
 */
public class FM14Sum170101 extends BasicCheckRule {

	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pid=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			String vipFlag = poi.getVipFlag();
			if(vipFlag != null){
				String str = vipFlag.replace('|', ',');
				List<Integer> vipFlags = StringUtils.getIntegerListByStr(str);
				if(vipFlags.contains(2)){
					boolean check = false;
					if(poi.getHisOpType().equals(OperationType.UPDATE)
							&&(poi.hisOldValueContains(IxPoi.KIND_CODE)||poi.hisOldValueContains(IxPoi.GEOMETRY))){
						check = true;
					}
					IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
					String name = null;
					if(ixPoiName != null){
						name = ixPoiName.getName();
						if(ixPoiName.getHisOpType().equals(OperationType.UPDATE)
								&&ixPoiName.hisOldValueContains(IxPoiName.NAME)){
							check = true;
						}
					}
					if(check){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "客户厂商:"+name+"被修改，请确认");
					}
					if(poi.getOpType().equals(OperationType.PRE_DELETED)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "客户厂商:"+poi.getPid()+"被删除，请确认");
					}
				}
			}
			//新增数据
			if(poi.getHisOpType().equals(OperationType.INSERT)){
				pid.add(poi.getPid());
			}
		}
		//新增POI的官方原始中文名称，且与其它非删除POI的官方原始中文名称相同(其它非删除POIvip_flag满足包含2)
		if(pid!=null&&pid.size()>0){
			String pids=pid.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			PreparedStatement pstmt=null;
			ResultSet rs=null;
		try{
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pid.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="SELECT P1.PID,P1.GEOMETRY,P1.MESH_ID,N1.NAME,P2.PID PID2"
					+ " FROM"
					+ "	IX_POI P1,"
					+ "	IX_POI P2,"
					+ "	IX_POI_NAME N1,"
					+ "	IX_POI_NAME N2"
					+ " WHERE"
					+ "	P1.PID = N1.POI_PID"
					+ " AND N1.NAME_CLASS = 1"
					+ " AND N1.NAME_TYPE = 2"
					+ " AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+ " AND P2.PID = N2.POI_PID"
					+ " AND N2.NAME_CLASS = 1"
					+ " AND N2.NAME_TYPE = 2"
					+ " AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+ " AND N1. NAME = N2. NAME"
					+ " AND P2.VIP_FLAG LIKE '%2%'"
					+ " AND P1.U_RECORD != 2"
					+ " AND P2.U_RECORD != 2"
					+ " AND N1.U_RECORD != 2"
					+ " AND N2.U_RECORD != 2"
					+ " AND P1."+pidString
					+ " AND P1.PID != P2.PID";
			pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			rs = pstmt.executeQuery();
			//过滤相同pid
			Set<String> filterPid = new HashSet<String>();
			while(rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				String name =rs.getString("NAME");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				if(!filterPid.contains(targets)){
					setCheckResult(geometry, targets, rs.getInt("MESH_ID"),"客户厂商:"+name+"在数据中存在，请确认");
				}
				filterPid.add(targets);
				filterPid.add("[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp1+"]");
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
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
