package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import oracle.sql.STRUCT;


/**
 * ZONE:NODE  查询接口
 * @author luyao
 *
 */
public class ZoneNodeSelector extends AbstractSelector  {
	
	private static Logger logger = Logger.getLogger(ZoneNodeSelector.class);

	private Connection conn;

	public ZoneNodeSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(ZoneNode.class);
	}
	
	// 加载盲端节点
	public List<ZoneNode> loadEndZoneNodeByLinkPid(int linkPid, boolean isLock)
			throws Exception {

		List<ZoneNode> nodes = new ArrayList<ZoneNode>();
		String sql ="with tmp1 as  (select s_node_pid, e_node_pid from zone_link where link_pid = :1), tmp2 as  (select b.link_pid, s_node_pid     from zone_link b    where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from zone_link b    where exists (select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record!=2) , tmp3 as  (select b.link_pid, s_node_pid as e_node_pid     from zone_link b    where exists (select null from tmp1 a where a.e_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from zone_link b    where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record!=2), tmp4 as  (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as  (select e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as  (select pid from tmp4 union select pid from tmp5) select *   from zone_node a  where exists (select null from tmp6 b where a.node_pid = b.pid) and a.u_record!=2";
		
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				ZoneNode node = new ZoneNode();

				node.setPid(resultSet.getInt("node_pid"));
				
				node.setRowId(resultSet.getString("row_id"));

				node.setKind(resultSet.getInt("kind"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				node.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

				node.setRowId(resultSet.getString("row_id"));

				ZoneNodeMeshSelector mesh = new ZoneNodeMeshSelector(conn);

				node.setMeshes(mesh.loadRowsByParentId(node.getPid(), isLock));

				nodes.add(node);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return nodes;
	}
	
	
	public int loadZoneLinkCountOnNode(int nodePid)
			throws Exception {

		String sql = "select count(1) count from zone_link a where a.s_node_pid=:1 or a.e_node_pid=:2";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);
			
			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
		
		return 0;
	}


}
