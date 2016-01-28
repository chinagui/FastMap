package com.navinfo.dataservice.FosEngine.edit.check;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class NiValCheckOperator {

	private Connection conn;
	
	public NiValCheckOperator(){
		
	}

	public NiValCheckOperator(Connection conn) {
		this.conn = conn;
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId,String worker) throws Exception{

		String sql = "merge into ni_val_exception a using ( select RESERVED from ni_val_exception union all select RESERVED from ck_exception ) b on (a.RESERVED = b.reserved) when not matched then   insert     (RESERVED, ruleid, information, location, targets, mesh_id,worker)   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7,:8)";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		
		try {
			String md5 = this.generateMd5(ruleId, CheckItems.getInforByRuleId(ruleId), targets, null);
			
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
		}finally{
		
			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
			
		}
		
	}
	
	public void deleteCheckLog(String reserved,int projectId) throws Exception{

		String sql = "update ni_val_exception set del_flag = 1 where RESERVED =:1";
		
		conn = DBOraclePoolManager.getConnection(projectId);
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		
		try {
			pstmt.setString(1, reserved);
			
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		}finally{
		
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
	
	public static void main(String[] args) throws ParseException {
		
		Geometry geom = null;
		
		WKTReader reader = new WKTReader();
		
		geom = reader.read("LINESTRING (1 1, 2 2, 2 1, 1 0)");
		
		System.out.println(geom.isSimple());
	}
}
