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

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
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
		//List<Long> pid=new ArrayList<Long>();
		List<ArrayList<Long>> pidList = new ArrayList<ArrayList<Long>>();
		int n = 0;
		ArrayList<Long> tmpPids = new ArrayList<>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			//pid.add(poi.getPid());
			tmpPids.add(poi.getPid());
			if( n /900 > 0){
				pidList.add(tmpPids);
				n = 0;
				tmpPids = new ArrayList<>();
			}
			n++;
		}
		if(tmpPids.size() > 0){
			pidList.add(tmpPids);
		}
//		//判断1和2条件是否满足
		//过滤相同pid
		Set<String> filterPid = new HashSet<String>();
		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		//POIKIND、POIKIND_CHAIN和R_KIND、R_KIND_CHAIN==>key:POIKIND_POIKIND_CHAIN,value:Set<R_KIND_R_KIND_CHAIN>
		Map<String, Set<String>> metaMap=new HashMap<String, Set<String>>();
		List<Map<String, String>> scPointKindNew5List = metadataApi.scPointKindNew5List();
		for (Map<String, String> map : scPointKindNew5List) {
			String poiKind = map.get("poiKind");
			String poiKindChain = map.get("poiKindChain");
			String rKind = map.get("rKind");
			String rKindChain = map.get("rKindChain"); 
			String key=poiKind+"_"+poiKindChain;
			String value=rKind+"_"+rKindChain;
			if(!metaMap.containsKey(key)){
				metaMap.put(key, new HashSet<String>());
			}
			metaMap.get(key).add(value);
		}
		for (List tmpPid: pidList){
		//if(pid!=null&&pid.size()>0){
			String pids = tmpPid.toString().replace("[", "").replace("]", "");
			//String pids = pid.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			pidString=" PID IN ("+pids+")";
			String sqlStr="WITH T AS"
					+ " (SELECT P1.PID       PID1,"
					+ "         P1.GEOMETRY,"
					+ "         P2.PID       PID2,"
					+ "         P1.MESH_ID,"
					+ "         P1.KIND_CODE K1,"
					+ "         P1.CHAIN     C1,"
					+ "         P2.KIND_CODE K2,"
					+ "         P2.CHAIN     C2"
					+ "    FROM IX_POI P1, IX_POI P2"
					+ "   WHERE P1."+pidString
					+ "     AND P1.U_RECORD <> 2"
					+ "     AND P2.U_RECORD <> 2"
					+ "     AND P1.PID <> P2.PID"
					+ "     AND SDO_NN(P2.GEOMETRY,"
					+ "                P1.GEOMETRY,"
					+ "                'sdo_batch_size=0 DISTANCE=5 UNIT=METER') = 'TRUE')"
					+ " SELECT /*+NO_MERGE(T)*/"
					+ " T.PID1 pid,"
					+ " T.PID2,"
					+ " T.GEOMETRY,"
					+ " T.MESH_ID,"
					+ " T.K1,"
					+ " T.C1,"
					+ " T.K2,"
					+ " T.C2,"
					+ " N1.        NAME name1,"
					+ " N2.        NAME name2"
					+ "  FROM T,"
					+ "       IX_POI_NAME    N1,"
					+ "       IX_POI_NAME    N2,"
					+ "       IX_POI_ADDRESS A1,"
					+ "       IX_POI_ADDRESS A2"
					+ " WHERE T.PID1 = N1.POI_PID"
					+ "   AND N1.NAME_CLASS = 1"
					+ "   AND N1.NAME_TYPE = 2"
					+ "   AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+ "   AND T.PID2 = N2.POI_PID"
					+ "   AND N2.NAME_CLASS = 1"
					+ "   AND N2.NAME_TYPE = 2"
					+ "   AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+ "   AND T.PID1 = A1.POI_PID"
					+ "   AND A1.LANG_CODE IN ('CHI', 'CHT')"
					+ "   AND T.PID2 = A2.POI_PID"
					+ "   AND A2.LANG_CODE IN ('CHI', 'CHT')"
					+ "   AND A1.FULLNAME = A2.FULLNAME"
					+ "   AND N1.U_RECORD <> 2"
					+ "   AND N2.U_RECORD <> 2"
					+ "   AND A1.U_RECORD <> 2"
					+ "   AND A2.U_RECORD <> 2";
			log.info(sqlStr);
			PreparedStatement pstmt=null;
			ResultSet rs = null;
			try{
				pstmt=conn.prepareStatement(sqlStr);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Long pidTmp1=rs.getLong("PID");
					Long pidTmp2=rs.getLong("PID2");
					String kindCode1 = rs.getString("K1");
					String kindCode2 = rs.getString("K2");
					String chain1 = rs.getString("C1");
					String chain2 = rs.getString("C2");
					
					// * 1）官方原始中文名称和地址(fullname)完成相同；
					// * 2）分类分别为加油站（230215）和加气站（230216），名称不同，地址相同；				 
					String name1=rs.getString("NAME1");
					String name2=rs.getString("NAME2");
					if((name1!=null&&!name1.equals(name2))||(name1==null&&name2!=null)){
						if(!((kindCode1.equals("230215")&&kindCode2.equals("230216"))
								||(kindCode2.equals("230215")&&kindCode1.equals("230216")))){
							continue;
						}
					}
					
					//两方分类、CHAIN值在SC_POINT_KIND_NEW中TYPE=5的 POIKIND、POIKIND_CHAIN和R_KIND、R_KIND_CHAIN列表中
					boolean flag = false;
					String key1=kindCode1+"_"+chain1;
					String key2=kindCode2+"_"+chain2;
					if((metaMap.containsKey(key1)&&metaMap.get(key1).contains(key2))
							||(metaMap.containsKey(key2)&&metaMap.get(key2).contains(key1))){
						flag = true;
					}
					if(flag){
						//判断是否存在同一关系
						Set<Long> samePoiPidList=new HashSet<Long>();
						samePoiPidList.add(pidTmp1);
						Map<Long, Long> samePoiParentMap = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), samePoiPidList);
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
						String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
						String targets2="[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp1+"]";
						if(!samePoiParentMap.containsKey(pidTmp1)
								||!samePoiParentMap.get(pidTmp1).equals(pidTmp2)){
							if(!filterPid.contains(targets)&&!filterPid.contains(targets2)){
								setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
							}
							filterPid.add(targets);
							filterPid.add("[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp1+"]");
						}
					}
				}
			}catch (SQLException e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(rs);
				DBUtils.closeStatement(pstmt);
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
