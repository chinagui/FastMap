package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.cross;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdCrossSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdCrossSelector.class);

	private Connection conn;

	public RdCrossSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdCross cross = new RdCross();

		String sql = "select a.*,c.mesh_id from rd_cross a, rd_cross_node b,rd_node_mesh c where a.pid=b.pid and b.node_pid=c.node_pid and a.pid=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				cross.setPid(resultSet.getInt("pid"));

				cross.setType(resultSet.getInt("type"));

				cross.setSignal(resultSet.getInt("signal"));

				cross.setElectroeye(resultSet.getInt("electroeye"));

				cross.setKgFlag(resultSet.getInt("kg_flag"));
				
				cross.setMesh(resultSet.getInt("mesh_id"));

				List<IRow> links = new RdCrossLinkSelector(conn)
						.loadRowsByParentId(id, isLock);
				
				for(IRow row : links){
					row.setMesh(cross.mesh());
				}

				cross.setLinks(links);
				
				for (IRow row : cross.getLinks()) {
					RdCrossLink obj = (RdCrossLink) row;

					cross.linkMap.put(obj.rowId(), obj);
				}

				List<IRow> nodes = new RdCrossNodeSelector(conn)
						.loadRowsByParentId(id, isLock);
				
				for(IRow row : nodes){
					row.setMesh(cross.mesh());
				}

				cross.setNodes(nodes);
				
				for (IRow row : cross.getNodes()) {
					RdCrossNode obj = (RdCrossNode) row;

					cross.nodeMap.put(obj.rowId(), obj);
				}

				List<IRow> names = new RdCrossNameSelector(conn)
						.loadRowsByParentId(id, isLock);
				
				for(IRow row : names){
					row.setMesh(cross.mesh());
				}

				cross.setNames(names);
				
				for (IRow row : cross.getNames()) {
					RdCrossName obj = (RdCrossName) row;

					cross.nameMap.put(obj.rowId(), obj);
				}

				cross.setRowId(resultSet.getString("row_id"));

			} else {
				
				throw new DataNotFoundException(null);
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

		return cross;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		return null;
	}
	
	/**
	 * 
	 * 获取node或者link所在的路口
	 * 
	 * @param nodePid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdCross> loadRdCrossByNodeOrLink(List<Integer> nodePids, List<Integer> linkPids, boolean isLock) throws Exception{
		
		List<RdCross> result = new ArrayList<RdCross>();
		
		String nodeStr = nodePids.toString();
		
		nodeStr.replace("[", "(");
		
		nodeStr.replace("]", ")");
		
		String linkStr = linkPids.toString();
		
		linkStr.replace("[", "(");
		
		linkStr.replace("]", ")");
		
		if (nodePids.size() == 0 && linkPids.size() == 0){
			return result;
		}
		
		String sql = "";
		
		if(nodePids.size() == 0){
			sql = "select a.*, c.mesh_id   from rd_cross a, RD_LINK c  where   exists (select null           from rd_cross_link d          where a.pid = d.pid            and d.link_pid in ("+linkStr+")            AND D.LINK_PID = C.LINK_PID            and d.u_record != 2)    and a.u_record != 2";
		}
		else if (linkPids.size() == 0){
			sql = "select a.*, c.mesh_id   from rd_cross a, rd_node_mesh c  where  exists (select null           from rd_cross_node d          where a.pid = d.pid            and d.node_pid in ("+nodeStr+")            and d.u_record != 2            and c.node_pid = d.node_pid            )    and a.u_record != 2";
		}
		else{
			sql = "select a.*, c.mesh_id   from rd_cross a, rd_node_mesh c  where  exists (select null           from rd_cross_node d          where a.pid = d.pid            and d.node_pid in ("+nodeStr+")            and d.u_record != 2            and c.node_pid = d.node_pid            )    and a.u_record != 2    union    select a.*, c.mesh_id   from rd_cross a, RD_LINK c  where   exists (select null           from rd_cross_link d          where a.pid = d.pid            and d.link_pid in ("+linkStr+")            AND D.LINK_PID = C.LINK_PID            and d.u_record != 2)    and a.u_record != 2";
		}
		
		Statement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.createStatement();

			resultSet = pstmt.executeQuery(sql);
			
			while (resultSet.next()) {
				
				RdCross cross = new RdCross();

				cross.setPid(resultSet.getInt("pid"));

				cross.setType(resultSet.getInt("type"));

				cross.setSignal(resultSet.getInt("signal"));

				cross.setElectroeye(resultSet.getInt("electroeye"));

				cross.setKgFlag(resultSet.getInt("kg_flag"));
				
				cross.setMesh(resultSet.getInt("mesh_id"));

				List<IRow> links = new RdCrossLinkSelector(conn)
						.loadRowsByParentId(cross.getPid(), isLock);
				
				for(IRow row : links){
					row.setMesh(cross.mesh());
				}

				cross.setLinks(links);
				
				List<IRow> nodes = new RdCrossNodeSelector(conn)
						.loadRowsByParentId(cross.getPid(), isLock);
				
				for(IRow row : nodes){
					row.setMesh(cross.mesh());
				}

				cross.setNodes(nodes);
				
				List<IRow> names = new RdCrossNameSelector(conn)
						.loadRowsByParentId(cross.getPid(), isLock);
				
				for(IRow row : names){
					row.setMesh(cross.mesh());
				}

				cross.setNames(names);
				
				cross.setRowId(resultSet.getString("row_id"));

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

		return result;
	}

}
