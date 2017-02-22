package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName GLM60293
 * @author Han Shaoming
 * @date 2017年2月21日 下午8:58:28
 * @Description TODO
 * 检查条件： 非删除POI对象；
 * 检查原则：
 * 检查数据满足以下两种情况之一：
 * 1）官方原始中文名称和地址(fullname)完成相同；
 * 2）分类分别为加油站（230215）和加气站（230216），名称不同，地址相同；
 * 满足1或2且显示坐标距离在5米之内且两方分类、CHAIN值在SC_POINT_KIND_NEW中TYPE=5的
 * POIKIND、POIKIND_CHAIN和R_KIND、R_KIND_CHAIN列表中，为同一组别，
 * 但未制作同一关系的数据，报出Log：普通POI未制作多分类同属性同一关系！
 */
public class GLM60293 extends BasicCheckRule {

	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pid=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			pid.add(poi.getPid());
		}
		//判断1和2条件是否满足
		if(pid!=null&&pid.size()>0){
			String pids=pid.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
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
			String sqlStr="WITH T AS" 
					+"(SELECT P1.PID PID1, P1.GEOMETRY G1,P1.MESH_ID M1,P1.KIND_CODE K1,P1.CHAIN C1,"
					+"P2.PID PID2,P2.GEOMETRY G2,P2.KIND_CODE K2,P2.CHAIN C2"
					+"	FROM IX_POI P1,"
					+"		IX_POI P2,"
					+"		IX_POI_NAME N1,"
					+"		IX_POI_NAME N2,"
					+"		IX_POI_ADDRESS A1,"
					+"		IX_POI_ADDRESS A2"
					+"	WHERE"
					+"		P1.PID = N1.POI_PID"
					+"	AND N1.NAME_CLASS = 1"
					+"	AND N1.NAME_TYPE = 2"
					+"	AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+"	AND P2.PID = N2.POI_PID"
					+"	AND N2.NAME_CLASS = 1"
					+"	AND N2.NAME_TYPE = 2"
					+"	AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+"	AND P1.PID = A1.POI_PID"
					+"	AND A1.LANG_CODE IN ('CHI', 'CHT')"
					+"	AND P2.PID = A2.POI_PID"
					+"	AND A2.LANG_CODE IN ('CHI', 'CHT')"
					+"	AND A1.FULLNAME = A2.FULLNAME"
					+"	AND N1. NAME = N2. NAME"
					+"	AND P1."+pidString
					+"	AND P1.PID != P2.PID"
					+"	UNION ALL"
					+"		SELECT P1.PID PID1, P1.GEOMETRY G1,P1.MESH_ID M1,P1.KIND_CODE K1,P1.CHAIN C1,"
					+"		P2.PID PID2,P2.GEOMETRY G2,P2.KIND_CODE K2,P2.CHAIN C2"
					+"		FROM IX_POI P1,"
					+"			IX_POI P2,"
					+"			IX_POI_NAME N1,"
					+"			IX_POI_NAME N2,"
					+"			IX_POI_ADDRESS A1,"
					+"			IX_POI_ADDRESS A2"
					+"		WHERE"
					+"			P1.PID = N1.POI_PID"
					+"		AND N1.NAME_CLASS = 1"
					+"		AND N1.NAME_TYPE = 2"
					+"		AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+"		AND P2.PID = N2.POI_PID"
					+"		AND N2.NAME_CLASS = 1"
					+"		AND N2.NAME_TYPE = 2"
					+"		AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+"		AND P1.PID = A1.POI_PID"
					+"		AND A1.LANG_CODE IN ('CHI', 'CHT')"
					+"		AND P2.PID = A2.POI_PID"
					+"		AND A2.LANG_CODE IN ('CHI', 'CHT')"
					+"		AND A1.FULLNAME = A2.FULLNAME"
					+"		AND N1. NAME <> N2. NAME"
					+"		AND ((P1.KIND_CODE = '230215'"
					+"				AND P2.KIND_CODE = '230216'"
					+"			)OR (P1.KIND_CODE = '230216'"
					+"				AND P2.KIND_CODE = '230215'))"
					+"		AND P1."+pidString
					+"		AND P1.PID != P2.PID)" 
					+"SELECT T.PID1 PID,T.G1 GEOMETRY,T.M1 MESH_ID,T.K1,T.C1,PID2,T.K2,T.C2"
					+"FROM T"
					+"WHERE SDO_GEOM.SDO_DISTANCE (G1, G2, 0.00000005) < 5"
					+"AND T .PID1 != T .PID2";
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			//SC_POINT_KIND_NEW中TYPE=5
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<Map<String, String>> scPointKindNew5List = metadataApi.scPointKindNew5List();
			while (rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				String kindCode1 = rs.getString("K1");
				String kindCode2 = rs.getString("K2");
				String chain1 = rs.getString("C1");
				String chain2 = rs.getString("C2");
				//两方分类、CHAIN值在SC_POINT_KIND_NEW中TYPE=5的 POIKIND、POIKIND_CHAIN和R_KIND、R_KIND_CHAIN列表中
				boolean flag = false;
				for (Map<String, String> map : scPointKindNew5List) {
					String poiKind = map.get("poiKind");
					String poiKindChain = map.get("poiKindChain");
					String rKind = map.get("rKind");
					String rKindChain = map.get("rKindChain"); 
					if((kindCode1.equals(poiKind)&&((chain1==null&&poiKindChain ==null)||chain1.equals(poiKindChain))
							&&kindCode2.equals(rKind)&&((chain2==null&&rKindChain ==null)||chain2.equals(rKindChain)))
						||(kindCode1.equals(rKind)&&((chain1==null&&rKindChain ==null)||chain1.equals(rKindChain))
								&&kindCode2.equals(poiKind)&&((chain2==null&&poiKindChain ==null)||chain2.equals(poiKindChain)))){
						flag = true;
						break;
					}
				}
				if(flag){
					//判断是否存在同一关系
					Set<Long> samePoiPidList=new HashSet<Long>();
					samePoiPidList.add(pidTmp1);
					Map<Long, Long> samePoiParentMap = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), samePoiPidList);
					STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
					String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
					if(!samePoiParentMap.containsKey(pidTmp1)
							||!samePoiParentMap.get(pidTmp1).equals(pidTmp2)){
						setCheckResult(geometry, targets, rs.getInt("MESH_ID"),null);
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
