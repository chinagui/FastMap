package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignasreal;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboard;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdBranchSelector implements ISelector {


	private Connection conn;

	public RdBranchSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdBranch branch = new RdBranch();

		String sql = "select * from " + branch.tableName()
				+ " where branch_pid=:1 and u_record!=2";

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

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);

				branch.setDetails(detailSelector.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getDetails()) {
					RdBranchDetail obj = (RdBranchDetail) row;

					branch.detailMap.put(obj.getPid(), obj);
				}

				RdSignboardSelector signboardSelector = new RdSignboardSelector(
						conn);

				branch.setSignboards(signboardSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSignboards()) {
					RdSignboard obj = (RdSignboard) row;

					branch.signboardMap.put(obj.getPid(), obj);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSignasreals()) {
					RdSignasreal obj = (RdSignasreal) row;

					branch.signasrealMap.put(obj.getPid(), obj);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getSeriesbranches()) {
					RdSeriesbranch obj = (RdSeriesbranch) row;

					branch.seriesbranchMap.put(obj.rowId(), obj);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getRealimages()) {
					RdBranchRealimage obj = (RdBranchRealimage) row;

					branch.realimageMap.put(obj.rowId(), obj);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSchematics()) {
					RdBranchSchematic obj = (RdBranchSchematic) row;

					branch.schematicMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getVias()) {
					RdBranchVia obj = (RdBranchVia) row;

					branch.viaMap.put(obj.rowId(), obj);
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

		return branch;
	}

	public IRow loadByPidDetailId(int pid, int detailId, boolean isLock) throws Exception {

		RdBranch branch = new RdBranch();

		String sql = "select * from " + branch.tableName()
				+ " where branch_pid=:1 and u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);
				
				IRow detail = detailSelector.loadById(detailId, isLock);
				
				List<IRow> details = new ArrayList<IRow>();
				
				details.add(detail);
				
				branch.setDetails(details);

				for (IRow row : branch.getDetails()) {
					RdBranchDetail obj = (RdBranchDetail) row;

					branch.detailMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(pid, isLock));

				for (IRow row : branch.getVias()) {
					RdBranchVia obj = (RdBranchVia) row;

					branch.viaMap.put(obj.rowId(), obj);
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

		return branch;
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
	
	public List<RdBranch> loadRdBranchByInLinkPid(int linkPid,boolean isLock)throws Exception
	{
		List<RdBranch> branchs = new ArrayList<RdBranch>();
		
		String sql = "select * from rd_branch where in_link_pid = :1 and u_record!=2 ";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				RdBranch branch = new RdBranch();
				
				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branchs.add(branch);
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
		
		return branchs;
	}
	
	public List<RdBranch> loadRdBranchByOutLinkPid(int linkPid,boolean isLock)throws Exception
	{
		List<RdBranch> branchs = new ArrayList<RdBranch>();
		
		String sql = "select a.*, c.node_pid   from rd_branch a, rd_link b, rd_cross_node c " +
				" where a.relationship_type = 1    and a.out_link_pid = b.link_pid    and c.node_pid in (b.s_node_pid, b.e_node_pid)    and a.out_link_pid = :1    and a.u_record!=2 " +
				"union all" +
				" select a.*,       " +
				" case          when b.s_node_pid in (d.s_node_pid, d.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid  " +
				" from rd_branch a, rd_link b, rd_branch_via c, rd_link d  " +
				"where a.relationship_type = 2    and a.branch_pid = c.branch_pid    and a.out_link_pid = b.link_pid    and c.link_pid = d.link_pid " +
				"   and (b.s_node_pid in (d.s_node_pid, d.e_node_pid) or        b.e_node_pid in (d.s_node_pid, d.e_node_pid))    and b.link_pid = :2    and a.u_record!=2 ";
		
//		if (isLock){
//			sql += " for update nowait";
//		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);
			
			pstmt.setInt(2, linkPid);

			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				RdBranch branch = new RdBranch();
				
				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.isetOutNodePid(resultSet.getInt("out_node_pid"));
				
				branchs.add(branch);
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
		
		return branchs;
	}

	public RdBranch loadByLinkNodeLink(int inLinkPid, int nodePid, int outLinkPid, boolean isLock)throws Exception
	{
		
		RdBranch branch = new RdBranch();
		
		String sql = "select * from rd_branch where in_link_pid=:1 and node_pid=:2 and out_link_pid=:3 and u_record!=2";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);
			
			pstmt.setInt(2, nodePid);
			
			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){

				int id = resultSet.getInt("branch_pid");
				
				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);

				branch.setDetails(detailSelector.loadRowsByParentId(id, isLock));

				RdSignboardSelector signboardSelector = new RdSignboardSelector(
						conn);

				branch.setSignboards(signboardSelector.loadRowsByParentId(id,
						isLock));

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(id,
						isLock));

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(id, isLock));

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(id,
						isLock));

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(id,
						isLock));

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(id, isLock));

			}
			else{
				return null;
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
		
		return branch;
		
	}
	public List<RdBranch> loadRdBranchByNodePid(int nodePid,boolean isLock)throws Exception
	{
		List<RdBranch> branchs = new ArrayList<RdBranch>();
		
		String sql = "select * from rd_branch where node_pid = :1 and u_record!=2 ";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				
				RdBranch branch = new RdBranch();

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);

				branch.setDetails(detailSelector.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getDetails()) {
					RdBranchDetail obj = (RdBranchDetail) row;

					branch.detailMap.put(obj.getPid(), obj);
				}

				RdSignboardSelector signboardSelector = new RdSignboardSelector(
						conn);

				branch.setSignboards(signboardSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSignboards()) {
					RdSignboard obj = (RdSignboard) row;

					branch.signboardMap.put(obj.getPid(), obj);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSignasreals()) {
					RdSignasreal obj = (RdSignasreal) row;

					branch.signasrealMap.put(obj.getPid(), obj);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getSeriesbranches()) {
					RdSeriesbranch obj = (RdSeriesbranch) row;

					branch.seriesbranchMap.put(obj.rowId(), obj);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getRealimages()) {
					RdBranchRealimage obj = (RdBranchRealimage) row;

					branch.realimageMap.put(obj.rowId(), obj);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSchematics()) {
					RdBranchSchematic obj = (RdBranchSchematic) row;

					branch.schematicMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getVias()) {
					RdBranchVia obj = (RdBranchVia) row;

					branch.viaMap.put(obj.rowId(), obj);
				}
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
		
		return branchs;
	}
}
