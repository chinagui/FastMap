package com.navinfo.dataservice.FosEngine.edit.check;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.navinfo.dataservice.FosEngine.edit.log.LogWriter;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class NiValExceptionOperator {

	private Connection conn;

	public NiValExceptionOperator() {

	}

	public NiValExceptionOperator(Connection conn) {
		this.conn = conn;
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String worker) throws Exception {

		String sql = "merge into ni_val_exception a using ( select * from ( select :1 as RESERVED from dual) where RESERVED not in ( select RESERVED          from ni_val_exception          where RESERVED is not null        union all        select RESERVED          from ck_exception          where RESERVED is not null          )) b on (a.RESERVED = b.reserved) when not matched then   insert     (RESERVED, ruleid, information, location, targets, mesh_id, worker, row_id, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, :10, sysdate, sysdate)";
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
			
			pstmt.setString(9, UuidUtils.genUuid());
			
			pstmt.setInt(10, 1);
			
			int res =pstmt.executeUpdate();
			
			if(res>0){
				
				CkResultObjectOperator op = new CkResultObjectOperator(conn);
				
				op.insertCkResultObject(md5, targets);
			}
			
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}
	
	public void deleteNiValException(String tableName, int pid)
			throws Exception {
		
		String sql = "delete from ni_val_exception a where exists (select null from ck_result_object b where a.reserved=b.ck_result_id and b.table_name=? and b.pid=?)";
		
		PreparedStatement pstmt=null;
		
		try {
			
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();
			
			pstmt.close();

			sql = "delete from ck_result_object a where a.ck_result_id in (select b.ck_result_id from ck_result_object b where b.table_name=? and b.pid=?)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

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

	/**
	 * 修改检查结果状态为例外、确认已修改、确认不修改
	 * 确认已修改进ni_val_exception_history表
	 * 例外、确认不修改就ck_exception表
	 * @param reserved
	 * @param projectId
	 * @param type 1例外，2确认不修改，3确认已修改
	 * @throws Exception
	 */
	public void updateCheckLogStatus(String reserved, int type)
			throws Exception {

		conn.setAutoCommit(false);
		
		PreparedStatement pstmt = null;

		try {
			
			String sql="";
			
			if(type==3)
			{
				sql="insert into ni_val_exception_history select * from ni_val_exception a where a.reserved=:1";
			}
			else{
				
				NiValExceptionSelector selector = new NiValExceptionSelector(conn);
				
				NiValException exception = selector.loadById(reserved, false);
				
				CkException ckexception = new CkException();
				
				ckexception.copy(exception);
				
				int pid = PidService.getInstance().applyCkExceptionId();
				
				ckexception.setExceptionId(pid);
				
				ckexception.setStatus(type);
				
				ckexception.setRowId(UuidUtils.genUuid());
				
				Result result = new Result();
				
				result.insertObject(ckexception, ObjStatus.INSERT);
				
				LogWriter writer = new LogWriter(conn);
				
				writer.recordLog(new Command(), result);
				
				sql="insert into ck_exception(exception_id, rule_id, task_name, status, group_id, rank, situation, information, suggestion, geometry, targets, addition_info, memo, create_date, update_date, mesh_id, scope_flag, province_name, map_scale, reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, u_date, row_id, u_record) select "+pid+",ruleid, task_name,";
				
				sql += type + ",groupid, \"LEVEL\" level_, situation, information, suggestion,sdo_util.to_wktgeometry(location), targets, addition_info, '',created, updated, mesh_id, scope_flag, province_name, map_scale, reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, u_date, '"+ckexception.rowId()+"',1 from ni_val_exception a where a.reserved=:1";
			}
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, reserved);
			
			pstmt.executeUpdate();
			
			pstmt.close();
			
			sql="delete from ni_val_exception where reserved=:1";
			
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, reserved);
			
			pstmt.executeUpdate();
			
			pstmt.close();
			
			sql="delete from ck_result_object where ck_result_id=:1";
			
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

		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/Desktop/config.properties");
		
		String username1 = "fm_prjgdb_bj02";

		String password1 = "fm_prjgdb_bj02";

		int port1 = 1521;

		String ip1 = "192.168.4.61";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		NiValExceptionOperator op = new NiValExceptionOperator(
				oa1.getConn());

		//op.insertCheckLog("3213131", "POINT(116.1313 37.131)", "[RD_LINK,32131]", 13, "13");
		
		op.updateCheckLogStatus("5490db11c96209409ce126ac3058c292", 3);
		
		//op.deleteNiValException("RD_LINK", 32131);
		
		System.out.println("done");
		
	}
}
