package com.navinfo.dataservice.dao.glm.selector.rd.crf;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RdObjectSelector
 * @author Zhang Xiaolong
 * @date 2016年8月20日 下午5:36:33
 * @Description: TODO
 */
public class RdObjectSelector extends AbstractSelector {

	public RdObjectSelector(Connection conn) {
		super(RdObject.class, conn);
	}
	
	@Override
	public IRow loadById(int id, boolean isLock, boolean... noChild) throws Exception {
		IRow row = super.loadById(id, isLock, noChild);
		
		if(row instanceof RdObject)
		{
			RdObject rdObject = (RdObject) row;
			
			List<IRow> rdObjRoads = rdObject.getRoads();
			
			for(IRow rdRoadRow : rdObjRoads)
			{
				RdObjectRoad road = (RdObjectRoad) rdRoadRow;
				
				int roadPid = road.getRoadPid();
				
				List<IRow> links = new AbstractSelector(getConn()).loadRowsByClassParentId(RdRoadLink.class, roadPid, isLock,null,null);
				
				road.setLinks(links);
			}
			
			List<IRow> rdObjInters = rdObject.getInters();
			
			for(IRow rdObjInter : rdObjInters)
			{
				RdObjectInter objInter = (RdObjectInter) rdObjInter;
				
				int interPid = objInter.getInterPid();
				
				List<IRow> links = new AbstractSelector(getConn()).loadRowsByClassParentId(RdInterLink.class, interPid, isLock,null,null);
				
				List<IRow> nodes = new AbstractSelector(getConn()).loadRowsByClassParentId(RdInterNode.class, interPid, isLock,null,null);
				
				objInter.setLinks(links);
				
				objInter.setNodes(nodes);
			}
		}
		
		return row;
	}
	
	/**
	 * 通过crf道路pid查询可以选择的道路名
	 * 如果选择的CRFO中，存在名称类型为立交桥名（连接路）并且属性为“匝道”或者名称类型为立交桥名（主路）的link，
	 * 则此字段将CRFO中名称类型为立交桥名（连接路）并且属性为“匝道”或者名称类型为“立交桥名（主路）”的link上的道路名称显示出来，
	 * 供用户下拉选择，并且可以手工编辑
	 * 
	 * @param pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<String> getRdObjectName(int pid, boolean isLock) throws Exception {
		List<String> nameList = new ArrayList<>();

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {

			String strSql="SELECT B.NAME FROM (SELECT C.NAME_GROUPID FROM RD_OBJECT_LINK A, RD_LINK_FORM B, RD_LINK_NAME C WHERE A.PID = :1 AND A.LINK_PID = B.LINK_PID AND B.LINK_PID = C.LINK_PID AND ((B.FORM_OF_WAY = 15 AND C.NAME_TYPE = 1) OR C.NAME_TYPE = 2) AND A.U_RECORD != 2 AND B.U_RECORD != 2 AND C.U_RECORD != 2 GROUP BY C.NAME_GROUPID UNION ALL SELECT /*+ leading(A,B) use_hash(A,B)*/ D.NAME_GROUPID FROM RD_OBJECT_INTER A, RD_INTER_LINK   B, RD_LINK_FORM    C, RD_LINK_NAME    D WHERE A.PID = :2 AND A.INTER_PID = B.PID AND B.LINK_PID = C.LINK_PID AND C.LINK_PID = D.LINK_PID AND ((C.FORM_OF_WAY = 15 AND D.NAME_TYPE = 1) OR D.NAME_TYPE = 2) AND A.U_RECORD != 2 AND B.U_RECORD != 2 AND C.U_RECORD != 2 AND D.U_RECORD != 2 GROUP BY D.NAME_GROUPID UNION ALL SELECT /*+ leading(A,B) use_hash(A,B)*/ D.NAME_GROUPID FROM RD_OBJECT_ROAD A, RD_ROAD_LINK   B, RD_LINK_FORM   C, RD_LINK_NAME   D WHERE A.PID = :3 AND A.ROAD_PID = B.PID AND B.LINK_PID = C.LINK_PID AND C.LINK_PID = D.LINK_PID AND ((C.FORM_OF_WAY = 15 AND D.NAME_TYPE = 1) OR D.NAME_TYPE = 2) AND A.U_RECORD != 2 AND B.U_RECORD != 2 AND C.U_RECORD != 2 AND D.U_RECORD != 2 GROUP BY D.NAME_GROUPID) TMP, RD_NAME B WHERE TMP.NAME_GROUPID = B.NAME_GROUPID(+) AND B.LANG_CODE(+) = 'CHI' GROUP BY TMP.NAME_GROUPID, B.NAME ";

			pstmt = getConn().prepareStatement(strSql);

			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.setInt(3, pid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				nameList.add(resultSet.getString("name"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return nameList;
	}

	/**
	 * 根据crf对象的组成要素类型和要素类型的pid查询crf对象的map
	 * 
	 * @param pids
	 *            要素pids的string字符串
	 * @param type
	 *            要素类型type
	 * @param isLock
	 *            是否加锁
	 * @return crf对象的map key:同一个crf对象组成要素的pid合并的字符串，value对应的pid
	 * @throws Exception
	 */
	public Map<String, RdObject> loadRdObjectByPidAndType(String pids, ObjType type, boolean isLock) throws Exception {
		Map<String, RdObject> objectMap = new HashMap<>();
		if (StringUtils.isEmpty(pids)) {
			return objectMap;
		}
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		String sql = "";
		switch (type) {
		case RDROAD:
			sql = "select a.pid,a.row_id,listagg(b.road_pid,',')WITHIN GROUP(order by b.pid)as mapkey from rd_object a,rd_object_road b where a.PID = b.PID and b.ROAD_PID in("
					+ pids + ") and a.U_RECORD !=2 and b.U_RECORD !=2 group by a.pid,a.row_id";
			break;
		case RDINTER:
			sql = "select a.pid,a.row_id,listagg(b.inter_pid,',')WITHIN GROUP(order by b.pid) as mapkey from rd_object a,rd_object_inter b where a.PID = b.PID and b.inter_PID in("
					+ pids + ") and a.U_RECORD !=2 and b.U_RECORD !=2 group by a.pid,a.row_id";
			break;
		case RDLINK:
			sql = "select a.pid,a.row_id,listagg(b.link_pid,',')WITHIN GROUP(order by b.pid) as mapkey from rd_object a,rd_object_link b where a.PID = b.PID and b.link_pid in("
					+ pids + ") and a.U_RECORD !=2 and b.U_RECORD !=2 group by a.pid,a.row_id";
			break;
		default:
			break;
		}
		try {
			StringBuilder sb = new StringBuilder(sql);
			pstmt = getConn().prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				String mapKey = resultSet.getString("mapkey");
				RdObject obj = new RdObject();
				obj.setPid(resultSet.getInt("pid"));
				obj.setRowId(resultSet.getString("row_id"));
				List<IRow> roads = loadRowsByClassParentId(RdObjectRoad.class, obj.getPid(), isLock, null);
				obj.setRoads(roads);
				List<IRow> inters = loadRowsByClassParentId(RdObjectInter.class, obj.getPid(), isLock, null);
				obj.setInters(inters);
				List<IRow> links = loadRowsByClassParentId(RdObjectLink.class, obj.getPid(), isLock, null);
				obj.setLinks(links);
				objectMap.put(mapKey, obj);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return objectMap;
	}
}
