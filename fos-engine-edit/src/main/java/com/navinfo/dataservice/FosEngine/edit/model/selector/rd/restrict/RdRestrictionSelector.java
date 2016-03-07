package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdRestrictionSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdRestrictionSelector.class);

	private Connection conn;

	public RdRestrictionSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdRestriction restrict = new RdRestriction();

//		String sql = "select * from " + restrict.tableName()
//				+ " where pid=:1 and u_record!=2";

		String sql = "select a.*,b.mesh_id from " + restrict.tableName()
				+ " a,rd_link b where a.pid=:1 and a.u_record!=2 and a.in_link_pid = b.link_pid ";

		
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

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));
				
				int meshId = resultSet.getInt("mesh_id");
				
				restrict.setMesh(meshId);

				RdRestrictionDetailSelector detailSelector = new RdRestrictionDetailSelector(
						conn);

				restrict.setDetails(detailSelector.loadRowsByParentId(id, isLock));
				
				for(IRow row : restrict.getDetails()){
					
					row.setMesh(meshId);
					
					RdRestrictionDetail detail = (RdRestrictionDetail)row;
					
					restrict.detailMap.put(detail.getPid(), detail);
					
					for(IRow row2 : detail.getConditions()){
						
						row2.setMesh(meshId);
						
						RdRestrictionCondition condition = (RdRestrictionCondition)row2;
						
						restrict.conditionMap.put(condition.getRowId(), condition);
					}
				}
			} else {
				
				throw new DataNotFoundException("数据不存在");
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

		return restrict;
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

	public List<RdRestriction> loadRdRestrictionByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdRestriction> reses = new ArrayList<RdRestriction>();

//		String sql = "select * from rd_restriction where in_link_pid = :1 and u_record!=:2";
		
		String sql = "select a.*,b.mesh_id from rd_restriction a,rd_link b where a.in_link_pid = :1 and a.u_record!=:2 and b.u_record!=2 and a.in_link_pid = b.link_pid";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));
				
				int meshId = resultSet.getInt("mesh_id");
				
				restrict.setMesh(meshId);

				RdRestrictionDetailSelector detail = new RdRestrictionDetailSelector(
						conn);

				restrict.setDetails(detail.loadRowsByParentId(
						restrict.getPid(), isLock));
				
				for(IRow obj : restrict.getDetails()){
					obj.setMesh(meshId);
				}

				reses.add(restrict);
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}

		return reses;
	}
	
	public List<RdRestriction> loadRdRestrictionByNodePid(int nodePid,
			boolean isLock) throws Exception {
		List<RdRestriction> reses = new ArrayList<RdRestriction>();

//		String sql = "select * from rd_restriction where node_pid = :1 and u_record!=:2";
		
		String sql = "select a.*,b.mesh_id from rd_restriction a,rd_link b where a.node_pid = :1 and a.u_record!=:2 and a.in_link_pid = b.link_pid ";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));
				
				int meshId = resultSet.getInt("mesh_id");
				
				restrict.setMesh(meshId);

				RdRestrictionDetailSelector detail = new RdRestrictionDetailSelector(
						conn);

				restrict.setDetails(detail.loadRowsByParentId(
						restrict.getPid(), isLock));
				
				for(IRow obj : restrict.getDetails()){
					obj.setMesh(meshId);
				}

				reses.add(restrict);
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}

		return reses;
	}

	public IRow loadRdRestrictionByLinkNode(int linkPid, int nodePid,
			boolean isLock) throws Exception {

		RdRestriction restrict = new RdRestriction();

//		String sql = "select * from rd_restriction where in_link_pid = :1 and node_pid=:2 and u_record!=:3";
		
		String sql = "select a.*,b.mesh_id from rd_restriction a,rd_link b where a.in_link_pid = :1 and a.node_pid=:2 and a.u_record!=:3 and a.in_link_pid = b.link_pid ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, 2);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));
				
				int meshId = resultSet.getInt("mesh_id");
				
				restrict.setMesh(meshId);

				RdRestrictionDetailSelector detail = new RdRestrictionDetailSelector(
						conn);

				restrict.setDetails(detail.loadRowsByParentId(
						restrict.getPid(), isLock));
				
				for(IRow obj : restrict.getDetails()){
					obj.setMesh(meshId);
				}

			} else {
				logger.info("未找到RdRestriction: linkPid " + linkPid
						+ ", nodePid " + nodePid);
				throw new DataNotFoundException("数据不存在");
			}

			if (resultSet.next()) {
				
				throw new Exception("存在多条交限");
			}
		} catch (Exception e) {
			

			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}

		return restrict;

	}

}
