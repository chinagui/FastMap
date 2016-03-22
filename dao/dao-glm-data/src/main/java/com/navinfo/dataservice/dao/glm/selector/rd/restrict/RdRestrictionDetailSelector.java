package com.navinfo.dataservice.dao.glm.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;

public class RdRestrictionDetailSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdRestrictionDetailSelector.class);

	private Connection conn;

	public RdRestrictionDetailSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdRestrictionDetail detail = new RdRestrictionDetail();

//		String sql = "select * from " + detail.tableName()
//				+ " where detail_id=:1 and u_record!=2";
		
		String sql = "select a.*,c.mesh_id from " + detail.tableName()
				+ " a,rd_restriction b,rd_link c where a.detail_id=:1 and a.u_record!=2 and a.restric_pid = b.pid and b.in_link_pid = c.link_pid ";

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

				detail.setPid(resultSet.getInt("detail_id"));

				detail.setRestricPid(resultSet.getInt("restric_pid"));

				detail.setOutLinkPid(resultSet.getInt("out_link_pid"));

				detail.setFlag(resultSet.getInt("flag"));

				detail.setRestricInfo(resultSet.getInt("restric_info"));

				detail.setType(resultSet.getInt("type"));

				detail.setRowId(resultSet.getString("row_id"));

				detail.setRelationshipType(resultSet
						.getInt("relationship_type"));

				RdRestrictionViaSelector via = new RdRestrictionViaSelector(
						conn);

				detail.setVias(via.loadRowsByParentId(id, isLock));

				RdRestrictionConditionSelector cond = new RdRestrictionConditionSelector(
						conn);

				detail.setConditions(cond.loadRowsByParentId(id, isLock));
				
				int meshId = resultSet.getInt("mesh_id");
				
				detail.setMesh(meshId);
				
				for(IRow row : detail.getConditions()){
					RdRestrictionCondition condition = (RdRestrictionCondition)row;
					
					condition.setMesh(meshId);
					
					detail.conditionMap.put(condition.getRowId(), condition);
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

		return detail;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_restriction_detail where restric_pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdRestrictionDetail detail = new RdRestrictionDetail();

				detail.setPid(resultSet.getInt("detail_id"));

				detail.setRestricPid(resultSet.getInt("restric_pid"));

				detail.setOutLinkPid(resultSet.getInt("out_link_pid"));

				detail.setFlag(resultSet.getInt("flag"));

				detail.setRestricInfo(resultSet.getInt("restric_info"));

				detail.setType(resultSet.getInt("type"));

				detail.setRowId(resultSet.getString("row_id"));

				detail.setRelationshipType(resultSet
						.getInt("relationship_type"));

				RdRestrictionViaSelector via = new RdRestrictionViaSelector(
						conn);

				detail.setVias(via.loadRowsByParentId(detail.getPid(), isLock));

				RdRestrictionConditionSelector cond = new RdRestrictionConditionSelector(
						conn);

				detail.setConditions(cond.loadRowsByParentId(detail.getPid(), isLock));
				
				for(IRow row : detail.getConditions()){
					RdRestrictionCondition condition = (RdRestrictionCondition)row;
					
					detail.conditionMap.put(condition.getRowId(), condition);
				}

				rows.add(detail);
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

		return rows;
	}

	public List<RdRestrictionDetail> loadDetailsByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdRestrictionDetail> rows = new ArrayList<RdRestrictionDetail>();

//		String sql = "select a.*, b.node_pid out_node_pid from rd_restriction_detail a, rd_cross_node b  where a.out_link_pid = :1 and a.u_record!=2  and a.relationship_type = 1  and exists (select null  from rd_restriction c, rd_cross_node d where a.restric_pid = c.pid   and c.node_pid = d.node_pid   and d.pid = b.pid)  and exists  (select null  from rd_link e where a.out_link_pid = e.link_pid   and b.node_pid in (e.s_node_pid, e.e_node_pid)) "
//				+ "union all select a.*, d.node_pid out_node_pid from rd_restriction_detail a, rd_restriction d  where a.relationship_type = 2 and a.u_record!=2  and a.out_link_pid = :2  and not exists (select null  from rd_restriction_via c where a.detail_id = c.detail_id)  and a.restric_pid = d.pid  "
//				+ "union all select a.*, case when b.s_node_pid in (e.s_node_pid, e.e_node_pid) then  b.s_node_pid else  b.e_node_pid end out_node_pid from rd_restriction_detail a, rd_restriction_via c, rd_link b, rd_link e  where a.relationship_type = 2 and a.u_record!=2  and a.out_link_pid = :3  and a.detail_id = c.detail_id  and a.out_link_pid = b.link_pid  and c.link_pid = e.link_pid  and (b.s_node_pid in (e.s_node_pid, e.e_node_pid) or b.e_node_pid in (e.s_node_pid, e.e_node_pid))";

		String sql = "select a.*, b.node_pid out_node_pid,f.mesh_id   from rd_restriction_detail a, rd_cross_node b,rd_link f  where a.out_link_pid = :1    and a.u_record != 2    and a.relationship_type = 1    and exists (select null           from rd_restriction c, rd_cross_node d          where a.restric_pid = c.pid            and c.node_pid = d.node_pid            and d.pid = b.pid            and c.in_link_pid = f.link_pid            )    and exists  (select null           from rd_link e          where a.out_link_pid = e.link_pid            and b.node_pid in (e.s_node_pid, e.e_node_pid)) union all select a.*, d.node_pid out_node_pid,f.mesh_id   from rd_restriction_detail a, rd_restriction d,rd_link f  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :2    and not exists (select null           from rd_restriction_via c          where a.detail_id = c.detail_id)    and a.restric_pid = d.pid    and d.in_link_pid = f.link_pid union all select a.*,        case          when b.s_node_pid in (e.s_node_pid, e.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid,g.mesh_id   from rd_restriction_detail a, rd_restriction_via c, rd_link b, rd_link e,rd_restriction f,rd_link g  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :3    and a.detail_id = c.detail_id    and a.out_link_pid = b.link_pid    and c.link_pid = e.link_pid    and (b.s_node_pid in (e.s_node_pid, e.e_node_pid) or        b.e_node_pid in (e.s_node_pid, e.e_node_pid))        and a.restric_pid = f.pid        and f.in_link_pid = g.link_pid";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);

			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdRestrictionDetail detail = new RdRestrictionDetail();

				detail.setPid(resultSet.getInt("detail_id"));

				detail.setRestricPid(resultSet.getInt("restric_pid"));

				detail.setOutLinkPid(resultSet.getInt("out_link_pid"));

				detail.setFlag(resultSet.getInt("flag"));

				detail.setRestricInfo(resultSet.getInt("restric_info"));

				detail.setType(resultSet.getInt("type"));

				detail.setRowId(resultSet.getString("row_id"));

				detail.setRelationshipType(resultSet
						.getInt("relationship_type"));
				
				int meshId = resultSet.getInt("mesh_id");
				
				detail.setMesh(meshId);

				RdRestrictionViaSelector via = new RdRestrictionViaSelector(
						conn);

				detail.setVias(via.loadRowsByParentId(detail.getPid(),
						isLock));
				
				for(IRow obj: detail.getVias()){
					obj.setMesh(meshId);
				}

				RdRestrictionConditionSelector cond = new RdRestrictionConditionSelector(
						conn);

				detail.setConditions(cond.loadRowsByParentId(
						detail.getPid(), isLock));
				
				for(IRow obj: detail.getConditions()){
					obj.setMesh(meshId);
				}

				rows.add(detail);
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

		return rows;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}
}
