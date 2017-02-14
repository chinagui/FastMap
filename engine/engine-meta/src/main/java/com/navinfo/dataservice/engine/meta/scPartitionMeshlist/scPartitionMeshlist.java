package com.navinfo.dataservice.engine.meta.scPartitionMeshlist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.metadata.model.Mesh4Partition;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;

public class scPartitionMeshlist {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private List<Mesh4Partition> meshList= new ArrayList<Mesh4Partition>();

	private static class SingletonHolder {
		private static final scPartitionMeshlist INSTANCE = new scPartitionMeshlist();
	}

	public static final scPartitionMeshlist getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * cp_meshlist,sc_partition_meshlist,通过行政区划号码，查询图幅相关信息
	 * @return List<Mesh4Partition>
	 * @throws Exception
	 */
	public List<Mesh4Partition> queryMeshes4PartitionByAdmincodes(Set<Integer> admincodes) throws Exception{
		if (meshList==null||meshList.isEmpty()) {
				synchronized (this) {
					if (meshList==null||meshList.isEmpty()) {
						try {
							String sql = "SELECT SPM.MESH mesh,cm.admincode,spm.province,spm.province_code,spm.action,spm.open_flag FROM CP_MESHLIST CM, SC_PARTITION_MESHLIST SPM WHERE CM.MESH = SPM.MESH AND CM.ADMINCODE in ("+StringUtils.join(admincodes.toArray(), ",")+")";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								Mesh4Partition meshs =new Mesh4Partition();
								while (rs.next()) {
									meshs.setMesh(rs.getInt("mesh"));
									meshs.setAdminCode(rs.getInt("admincode"));
									meshs.setProvince(rs.getString("province"));
									meshs.setProvinceCode(rs.getString("province_code"));
									meshs.setAction(rs.getInt("action"));
									meshs.setDay2monSwitch(rs.getInt("open_flag"));
									
									meshList.add(meshs);					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_partition_meshlist失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return meshList;
	}
	
	/**
	 * cp_meshlist,sc_partition_meshlist查询所有图幅相关信息
	 * @return List<Mesh4Partition>
	 * @throws Exception
	 */
	public List<Mesh4Partition> listMeshes4Partition() throws Exception{
		if (meshList==null||meshList.isEmpty()) {
				synchronized (this) {
					if (meshList==null||meshList.isEmpty()) {
						try {
							String sql = "SELECT SPM.MESH mesh,cm.admincode,spm.province,spm.province_code,spm.action,spm.open_flag FROM CP_MESHLIST CM, SC_PARTITION_MESHLIST SPM WHERE CM.MESH = SPM.MESH";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								Mesh4Partition meshs =new Mesh4Partition();
								while (rs.next()) {
									meshs.setMesh(rs.getInt("mesh"));
									meshs.setAdminCode(rs.getInt("admincode"));
									meshs.setProvince(rs.getString("province"));
									meshs.setProvinceCode(rs.getString("province_code"));
									meshs.setAction(rs.getInt("action"));
									meshs.setDay2monSwitch(rs.getInt("open_flag"));
									
									meshList.add(meshs);					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_partition_meshlist失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return meshList;
	}
	
}
