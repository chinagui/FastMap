package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;

public class RdLaneConnexitySelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdLaneConnexitySelector.class);

	private Connection conn;

	public RdLaneConnexitySelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdLaneConnexity connexity = new RdLaneConnexity();

		String sql = "select * from " + connexity.tableName()
				+ " where pid=:1 and u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				connexity.setPid(resultSet.getInt("pid"));

				connexity.setInLinkPid(resultSet.getInt("in_link_pid"));

				connexity.setNodePid(resultSet.getInt("node_pid"));
				
				connexity.setLaneInfo(resultSet.getString("lane_info"));
				
				connexity.setConflictFlag(resultSet.getInt("conflict_flag"));
				
				connexity.setKgFlag(resultSet.getInt("kg_flag"));
				
				connexity.setLaneNum(resultSet.getInt("lane_num"));
				
				connexity.setLeftExtend(resultSet.getInt("left_extend"));
				
				connexity.setRightExtend(resultSet.getInt("right_extend"));
				
				connexity.setSrcFlag(resultSet.getInt("src_flag"));

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
						conn);

				connexity.setTopos(topoSelector.loadRowsByParentId(id, isLock));
				
				for(IRow row : connexity.getTopos()){
					RdLaneTopology topo = (RdLaneTopology)row;
					
					connexity.topologyMap.put(topo.getPid(), topo);
					
					for(IRow row2 : topo.getVias()){
						RdLaneVia via = (RdLaneVia)row2;
						
						connexity.viaMap.put(via.getRowId(), via);
					}
				}
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

		return connexity;
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

}
