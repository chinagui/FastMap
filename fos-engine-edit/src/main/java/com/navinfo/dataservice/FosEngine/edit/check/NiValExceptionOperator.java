package com.navinfo.dataservice.FosEngine.edit.check;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.service.PidService;
import com.vividsolutions.jts.io.ParseException;

public class NiValExceptionOperator {

	private Connection conn;

	public NiValExceptionOperator() {

	}

	public NiValExceptionOperator(Connection conn) {
		this.conn = conn;
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String worker) throws Exception {

		String sql = "merge into ni_val_exception a using ( select * from ( select :8 as RESERVED from dual) where RESERVED not in ( select RESERVED          from ni_val_exception          where RESERVED is not null        union all        select RESERVED          from ck_exception          where RESERVED is not null          )) b on (a.RESERVED = b.reserved) when not matched then   insert     (RESERVED, ruleid, information, location, targets, mesh_id, worker)   values     (:1, :2, :3, sdo_geometry(:4, 8307), :5, :6, :7)";
		PreparedStatement pstmt = conn.prepareStatement(sql);

		try {
			String md5 = this.generateMd5(ruleId,
					CheckItems.getInforByRuleId(ruleId), targets, null);

			pstmt.setString(1, md5);
			
			pstmt.setString(2, md5);

			pstmt.setString(3, ruleId);

			pstmt.setString(4, CheckItems.getInforByRuleId(ruleId));

			pstmt.setString(5, loc);

			pstmt.setString(6, targets);

			pstmt.setInt(7, meshId);

			pstmt.setString(8, worker);
			
			

			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

	public void deleteCheckLog(String reserved, int projectId) throws Exception {

		String sql = "update ni_val_exception set del_flag = 1 where RESERVED =:1";

		conn = DBOraclePoolManager.getConnection(projectId);

		PreparedStatement pstmt = conn.prepareStatement(sql);

		try {
			pstmt.setString(1, reserved);

			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

			try {
				conn.close();
			} catch (Exception e) {

			}
		}

	}

	/**
	 * 修改检查结果状态为例外、确认已修改、确认不修改
	 * 确认已修改进ni_val_exception_history表
	 * 例外、确认不修改就ck_exception表
	 * @param reserved
	 * @param projectId
	 * @param type 1例外，2确认不修改，3确认已修改
	 * @throws Exception
	 */
	public void updateCheckLogStatus(String reserved, int projectId, int type)
			throws Exception {

		conn = DBOraclePoolManager.getConnection(projectId);

		conn.setAutoCommit(false);
		
		PreparedStatement pstmt = null;

		try {
			
			String sql="";
			
			if(type==3)
			{
				sql="insert into ni_val_exception_history from ni_val_exception a where a.reserved=:1";
			}
			else{
				
				int pid = PidService.getInstance().applyCkExceptionId();
				
				sql="insert into ck_exception(exception_id, rule_id, task_name, status, group_id, rank, situation, information, suggestion, geometry, targets, addition_info, memo, create_date, update_date, mesh_id, scope_flag, province_name, map_scale, reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, u_date, row_id) select "+pid+",ruleid, task_name,";
				
				sql += type + ",groupid, \"LEVEL\" level_, situation, information, suggestion,sdo_util.to_wktgeometry(location), targets, addition_info, '',created, updated, mesh_id, scope_flag, province_name, map_scale, reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, u_date, row_id from ni_val_exception a where a.reserved=:1";
			}
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, reserved);
			
			pstmt.executeUpdate();
			
			pstmt.close();
			
			sql="delete from ni_val_exception where reserved=:1";
			
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, reserved);
			
			pstmt.executeUpdate();
			
			conn.commit();
			
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

			try {
				conn.close();
			} catch (Exception e) {

			}
		}
	}

	private String generateMd5(String ruleId, String infor, String targets,
			String addInfo) {

		StringBuilder sb = new StringBuilder(ruleId);

		sb.append(infor);

		sb.append(targets);

		if (addInfo != null) {
			sb.append(addInfo);
		}

		return getMd5(sb.toString());

	}

	private static MessageDigest md5 = null;
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 用于获取一个String的md5值
	 * 
	 * @param string
	 * @return
	 */
	private static String getMd5(String str) {
		byte[] bs = md5.digest(str.getBytes());
		StringBuilder sb = new StringBuilder(40);
		for (byte x : bs) {
			if ((x & 0xff) >> 4 == 0) {
				sb.append("0").append(Integer.toHexString(x & 0xff));
			} else {
				sb.append(Integer.toHexString(x & 0xff));
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {

		String username1 = "fm_prjgdb_bj";

		String password1 = "fm_prjgdb_bj";

		int port1 = 1521;

		String ip1 = "192.168.4.61";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		NiValExceptionOperator op = new NiValExceptionOperator(
				oa1.getConn());

		//op.insertCheckLog("12321321", "POINT(116.1313 37.131)", "[link:31]", 13, "13");
		
		op.updateCheckLogStatus("f9ae31ed7d51317e05a01beb81ca9f2f", 11, 1);
		
	}
}
