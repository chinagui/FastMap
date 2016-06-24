package com.navinfo.dataservice.dao.glm.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;

public class RdBranchSelector implements ISelector {


	private Connection conn;

	public RdBranchSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdBranch branch = new RdBranch();

//		String sql = "select * from " + branch.tableName()
//				+ " where branch_pid=:1 and u_record!=2";
		
		String sql = "select a.*,b.mesh_id from " + branch.tableName()
				+ " a,rd_link b where a.branch_pid=:1 and a.u_record!=2 and a.in_link_pid = b.link_pid ";

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
				
				int meshId = resultSet.getInt("mesh_id");
				
				branch.setMesh(meshId);

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);

				branch.setDetails(detailSelector.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getDetails()) {
					row.setMesh(meshId);
					
					RdBranchDetail obj = (RdBranchDetail) row;
					
					for(IRow name : obj.getNames()){
						name.setMesh(meshId);
					}

					branch.detailMap.put(obj.getPid(), obj);
				}

				RdSignboardSelector signboardSelector = new RdSignboardSelector(
						conn);

				branch.setSignboards(signboardSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSignboards()) {
					
					row.setMesh(meshId);
					
					RdSignboard obj = (RdSignboard) row;
					
					branch.signboardMap.put(obj.getPid(), obj);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSignasreals()) {
					row.setMesh(meshId);
					
					RdSignasreal obj = (RdSignasreal) row;
					
					branch.signasrealMap.put(obj.getPid(), obj);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getSeriesbranches()) {
					row.setMesh(meshId);
					
					RdSeriesbranch obj = (RdSeriesbranch) row;
					
					branch.seriesbranchMap.put(obj.rowId(), obj);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getRealimages()) {
					row.setMesh(meshId);
					
					RdBranchRealimage obj = (RdBranchRealimage) row;

					branch.realimageMap.put(obj.rowId(), obj);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSchematics()) {
					row.setMesh(meshId);
					
					RdBranchSchematic obj = (RdBranchSchematic) row;
					
					branch.schematicMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getVias()) {
					row.setMesh(meshId);
					
					RdBranchVia obj = (RdBranchVia) row;
					
					branch.viaMap.put(obj.rowId(), obj);
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

		return branch;
	}

	public IRow loadByDetailId(int detailId,int branchType,String rowId, boolean isLock) throws Exception {
		
		String tableName="";
		String condition = "";
		if(branchType >=0 && branchType <= 4){
			tableName = " rd_branch_detail "; 
			condition = " detail_id=:1 ";
		}if(branchType == 5){
			tableName = " rd_branch_realimage "; 
			condition = " row_id=hextoraw(:1)";
			
		}if(branchType == 6){
			tableName = " rd_signasreal "; 
			condition = " signasreal_id =:1 ";
		}
		if(branchType == 7){
			tableName = " rd_seriesbranch "; 
			condition = " row_id=hextoraw(:1)";
		}if(branchType == 8){
			tableName = " rd_branch_schematic "; 
			condition = " schematic_id =:1 ";
			
		}if(branchType == 9){
			tableName = " rd_signboard "; 
			condition = " signboard_id =:1 ";
			
		}
		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b,"+tableName+" c where a.u_record!=2 and a.in_link_pid = b.link_pid  and a.branch_pid=c.branch_pid and c."+condition+"";

		if (isLock) {
			sql += " for update nowait";
		}
		RdBranch branch = new RdBranch();
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);
			
			if(branchType == 5||branchType == 7){
				pstmt.setString(1, rowId);
			}else{
				pstmt.setInt(1, detailId);
			}
			

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int meshId = resultSet.getInt("mesh_id");

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.setMesh(meshId);
				if(branchType >=0 && branchType <= 4){
					RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
							conn);
					
					IRow detail = detailSelector.loadById(detailId, isLock);
					
					List<IRow> details = new ArrayList<IRow>();
					
					details.add(detail);

					branch.setDetails(details);
				}
				if(branchType == 5){
					RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(conn);
					IRow image = realimageSelector.loadByRowId(rowId, isLock);
					List<IRow> images = new ArrayList<IRow>();
					images.add(image);
					branch.setRealimages(images);
				}
				if(branchType == 6){
					RdSignasrealSelector signasrealSelector =  new RdSignasrealSelector(conn);
					IRow signasreal = signasrealSelector.loadById(detailId, isLock);
					List<IRow> signasreals = new ArrayList<IRow>();
					signasreals.add(signasreal);
					branch.setSignasreals(signasreals);
				}
				if(branchType == 7){
					RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(conn);
					IRow seriesbranch = seriesbranchSelector.loadByRowId(rowId, isLock);
					List<IRow> seriesbranches = new ArrayList<IRow>();
					seriesbranches.add(seriesbranch);
					branch.setSeriesbranches(seriesbranches);
				}
				if(branchType == 8){
					RdBranchSchematicSelector schematicSelector=  new RdBranchSchematicSelector(conn);
					IRow schematic = schematicSelector.loadById(detailId, isLock);
					List<IRow> schematics = new ArrayList<IRow>();
					schematics.add(schematic);
					branch.setSchematics(schematics);
				}
				if(branchType == 9){
					RdSignboardSelector signboardSelector =  new RdSignboardSelector(conn);
					IRow signboard = signboardSelector.loadById(detailId, isLock);
					List<IRow> signboards = new ArrayList<IRow>();
					signboards.add(signboard);
					branch.setSignboards(signboards);
				}

				

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(), isLock));

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
		
		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b where a.in_link_pid = :1 and a.u_record!=2 and a.in_link_pid = b.link_pid ";
		
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
				
				int meshId = resultSet.getInt("mesh_id");
				
				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.setMesh(meshId);
				
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
		
//		String sql = "select a.*, c.node_pid   from rd_branch a, rd_link b, rd_cross_node c " +
//				" where a.relationship_type = 1    and a.out_link_pid = b.link_pid    and c.node_pid in (b.s_node_pid, b.e_node_pid)    and a.out_link_pid = :1    and a.u_record!=2 " +
//				"union all" +
//				" select a.*,       " +
//				" case          when b.s_node_pid in (d.s_node_pid, d.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid  " +
//				" from rd_branch a, rd_link b, rd_branch_via c, rd_link d  " +
//				"where a.relationship_type = 2    and a.branch_pid = c.branch_pid    and a.out_link_pid = b.link_pid    and c.link_pid = d.link_pid " +
//				"   and (b.s_node_pid in (d.s_node_pid, d.e_node_pid) or        b.e_node_pid in (d.s_node_pid, d.e_node_pid))    and b.link_pid = :2    and a.u_record!=2 ";
		
		String sql = "select a.*, c.node_pid out_node_pid,d.mesh_id   from rd_branch a, rd_link b, rd_cross_node c,rd_link d  where a.relationship_type = 1    and a.out_link_pid = b.link_pid    and c.node_pid in (b.s_node_pid, b.e_node_pid)    and a.out_link_pid = :1    and a.u_record != 2    and a.in_link_pid = d.link_pid union all select a.*,        case          when b.s_node_pid in (d.s_node_pid, d.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid,e.mesh_id   from rd_branch a, rd_link b, rd_branch_via c, rd_link d,rd_link e  where a.relationship_type = 2    and a.branch_pid = c.branch_pid    and a.out_link_pid = b.link_pid    and c.link_pid = d.link_pid    and (b.s_node_pid in (d.s_node_pid, d.e_node_pid) or        b.e_node_pid in (d.s_node_pid, d.e_node_pid))    and b.link_pid = :2    and a.u_record != 2    and a.in_link_pid = e.link_pid ";
		
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
				int meshId = resultSet.getInt("mesh_id");
				
				RdBranch branch = new RdBranch();
				
				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.isetOutNodePid(resultSet.getInt("out_node_pid"));
				
				branch.setMesh(meshId);
				
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
		
		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b where a.in_link_pid=:1 and a.node_pid=:2 and a.out_link_pid=:3 and a.u_record!=2 and a.in_link_pid = b.link_pid ";
		
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
				int meshId = resultSet.getInt("mesh_id");
				
				int id = resultSet.getInt("branch_pid");
				
				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.setMesh(meshId);

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);

				branch.setDetails(detailSelector.loadRowsByParentId(id, isLock));
				
				for(IRow obj : branch.getDetails()){
					obj.setMesh(meshId);
				}

				RdSignboardSelector signboardSelector = new RdSignboardSelector(
						conn);

				branch.setSignboards(signboardSelector.loadRowsByParentId(id,
						isLock));
				
				for(IRow obj : branch.getSignboards()){
					obj.setMesh(meshId);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(id,
						isLock));
				
				for(IRow obj : branch.getSignasreals()){
					obj.setMesh(meshId);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(id, isLock));
				
				for(IRow obj : branch.getSeriesbranches()){
					obj.setMesh(meshId);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(id,
						isLock));
				
				for(IRow obj : branch.getRealimages()){
					obj.setMesh(meshId);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(id,
						isLock));
				
				for(IRow obj : branch.getSchematics()){
					obj.setMesh(meshId);
				}

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
		
//		String sql = "select * from rd_branch where node_pid = :1 and u_record!=2 ";
		
		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b where a.node_pid = :1 and a.u_record!=2 and a.in_link_pid=b.link_pid ";
		
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
				
				int meshId = resultSet.getInt("mesh_id");
				
				RdBranch branch = new RdBranch();

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.setMesh(meshId);

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
					
					obj.setMesh(meshId);

					branch.signboardMap.put(obj.getPid(), obj);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSignasreals()) {
					RdSignasreal obj = (RdSignasreal) row;
					
					obj.setMesh(meshId);

					branch.signasrealMap.put(obj.getPid(), obj);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getSeriesbranches()) {
					RdSeriesbranch obj = (RdSeriesbranch) row;
					
					obj.setMesh(meshId);

					branch.seriesbranchMap.put(obj.rowId(), obj);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getRealimages()) {
					RdBranchRealimage obj = (RdBranchRealimage) row;
					
					obj.setMesh(meshId);

					branch.realimageMap.put(obj.rowId(), obj);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSchematics()) {
					RdBranchSchematic obj = (RdBranchSchematic) row;
					
					obj.setMesh(meshId);

					branch.schematicMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getVias()) {
					RdBranchVia obj = (RdBranchVia) row;
					
					obj.setMesh(meshId);

					branch.viaMap.put(obj.rowId(), obj);
				}
				
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
	
	public List<RdBranch> loadRdBranchByLinkNode(int linkPid, int nodePid1, int nodePid2,
			boolean isLock) throws Exception {

		List<RdBranch> branchs = new ArrayList<RdBranch>();
		
		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b where a.node_pid in (:1,:2) and a.in_link_pid=:3 and a.u_record!=2 and a.in_link_pid=b.link_pid ";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid1);
			
			pstmt.setInt(2, nodePid2);

			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				
				int meshId = resultSet.getInt("mesh_id");
				
				RdBranch branch = new RdBranch();

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));
				
				branch.setMesh(meshId);

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
					
					obj.setMesh(meshId);

					branch.signboardMap.put(obj.getPid(), obj);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSignasreals()) {
					RdSignasreal obj = (RdSignasreal) row;
					
					obj.setMesh(meshId);

					branch.signasrealMap.put(obj.getPid(), obj);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getSeriesbranches()) {
					RdSeriesbranch obj = (RdSeriesbranch) row;
					
					obj.setMesh(meshId);

					branch.seriesbranchMap.put(obj.rowId(), obj);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getRealimages()) {
					RdBranchRealimage obj = (RdBranchRealimage) row;
					
					obj.setMesh(meshId);

					branch.realimageMap.put(obj.rowId(), obj);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(branch.getPid(),
						isLock));

				for (IRow row : branch.getSchematics()) {
					RdBranchSchematic obj = (RdBranchSchematic) row;
					
					obj.setMesh(meshId);

					branch.schematicMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(), isLock));

				for (IRow row : branch.getVias()) {
					RdBranchVia obj = (RdBranchVia) row;
					
					obj.setMesh(meshId);

					branch.viaMap.put(obj.rowId(), obj);
				}
				
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
}
