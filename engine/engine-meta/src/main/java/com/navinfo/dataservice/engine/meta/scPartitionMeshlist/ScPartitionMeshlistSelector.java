package com.navinfo.dataservice.engine.meta.scPartitionMeshlist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.model.Mesh4Partition;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ScPartitionMeshlistSelector 
 * @author Gao Pengrong
 * @date 2017-2-16 上午9:41:24 
 * @Description: 加载Sc_Partition_Meshlist表中数据
 */

public class ScPartitionMeshlistSelector {
	
	
	public ScPartitionMeshlistSelector() {
		
	}
	/**
	 * cp_meshlist,sc_partition_meshlist,通过行政区划号码，查询图幅相关信息
	 * @return List<Mesh4Partition>
	 * @throws Exception
	 */
	public List<Mesh4Partition> queryMeshes4PartitionByAdmincodes(Set<Integer> admincodes) throws Exception{
		
		Connection conn = null;
		
		String sql = "SELECT SPM.MESH mesh,cm.admincode,spm.province,spm.province_code,spm.action,spm.open_flag FROM CP_MESHLIST CM, SC_PARTITION_MESHLIST SPM WHERE CM.MESH = SPM.MESH AND CM.ADMINCODE in ("+StringUtils.join(admincodes.toArray(), ",")+")";
		
		ResultSet rs = null;
		
		PreparedStatement pstmt = null;
		
		List<Mesh4Partition> meshList= new ArrayList<Mesh4Partition>();
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Mesh4Partition meshs =new Mesh4Partition();
				meshs.setMesh(rs.getInt("mesh"));
				meshs.setAdminCode(rs.getInt("admincode"));
				meshs.setProvince(rs.getString("province"));
				meshs.setProvinceCode(rs.getString("province_code"));
				meshs.setAction(rs.getInt("action"));
				meshs.setDay2monSwitch(rs.getInt("open_flag"));
				
				meshList.add(meshs);					
			} 
			return meshList;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * cp_meshlist,sc_partition_meshlist查询所有图幅相关信息
	 * @return List<Mesh4Partition>
	 * @throws Exception
	 */
	public List<Mesh4Partition> listMeshes4Partition() throws Exception{
		
		Connection conn = null;
		
		String sql = "SELECT SPM.MESH mesh,cm.admincode,spm.province,spm.province_code,spm.action,spm.open_flag FROM CP_MESHLIST CM, SC_PARTITION_MESHLIST SPM WHERE CM.MESH = SPM.MESH";
		
		ResultSet rs = null;
		
		PreparedStatement pstmt = null;
		
		List<Mesh4Partition> meshList= new ArrayList<Mesh4Partition>();
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Mesh4Partition meshs =new Mesh4Partition();
				meshs.setMesh(rs.getInt("mesh"));
				meshs.setAdminCode(rs.getInt("admincode"));
				meshs.setProvince(rs.getString("province"));
				meshs.setProvinceCode(rs.getString("province_code"));
				meshs.setAction(rs.getInt("action"));
				meshs.setDay2monSwitch(rs.getInt("open_flag"));
				
				meshList.add(meshs);					
			} 
			return meshList;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}

}
