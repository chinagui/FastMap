/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.crf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			StringBuilder sb = new StringBuilder(
					"select b.name from( select c.NAME_GROUPID from rd_object_link a,rd_link_form b,rd_link_name c where a.pid = :1 and a.LINK_PID = b.LINK_PID and b.LINK_PID=c.LINK_PID and ((b.FORM_OF_WAY = 15 and c.NAME_TYPE = 1) or c.NAME_TYPE = 2) and a.U_RECORD !=2 group by c.NAME_GROUPID union all select /*+ leading(A,B) use_hash(A,B)*/ d.NAME_GROUPID from rd_object_inter a,rd_inter_link b,rd_link_form c,rd_link_name d where a.pid = 7514 and a.INTER_PID = b.PID and b.LINK_PID=c.LINK_PID and c.LINK_PID = d.LINK_PID and ((c.FORM_OF_WAY = 15 and d.NAME_TYPE = 1) or d.NAME_TYPE = 2) and a.U_RECORD !=2 group by d.NAME_GROUPID union all select /*+ leading(A,B) use_hash(A,B)*/ d.NAME_GROUPID from rd_object_road a,rd_road_link b,rd_link_form c,rd_link_name d where a.pid = 7514 and a.road_PID = b.PID and b.LINK_PID=c.LINK_PID and c.LINK_PID = d.LINK_PID and ((c.FORM_OF_WAY = 15 and d.NAME_TYPE = 1) or d.NAME_TYPE = 2) and a.U_RECORD !=2 group by d.NAME_GROUPID)tmp,rd_name b where tmp.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI' group by tmp.NAME_GROUPID,b.name");
			pstmt = getConn().prepareStatement(sb.toString());

			pstmt.setInt(1, pid);

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
