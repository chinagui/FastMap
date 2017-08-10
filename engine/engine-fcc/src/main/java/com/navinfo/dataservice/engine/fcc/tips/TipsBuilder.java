package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.pool.OracleAddress;
import com.navinfo.dataservice.engine.fcc.tips.bridge.BridgeTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.connexity.RdLaneConnexityTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.construct.ConstructTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.cross.RdCrossTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.direct.DirectTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.highway.HighwayTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.mark3d.Mark3DTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.restriction.RdRestrictionTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.speedLimit.RdSpeedLimitTipsBuilder;

/**
 * 创建Tips
 */
public class TipsBuilder {

	/**
	 * 初始化
	 * 
	 * @param connection
	 * @param tabName
	 * @throws IOException
	 */
	private static void createTabIfNotExists(Connection connection,
			String tabName) throws IOException {

		Admin admin = connection.getAdmin();

		TableName tableName = TableName.valueOf(tabName);

		if (!admin.tableExists(tableName)) {
			HTableDescriptor htd = new HTableDescriptor(tableName);

			HColumnDescriptor hcd = new HColumnDescriptor("data");

			htd.addFamily(hcd);

			admin.createTable(htd);
		}
	}

	/**
	 * 在tips导入前，对需要组合的数据进行合并组织
	 * 
	 * @param conn
	 */
	private static void prepareAuxData(java.sql.Connection conn) throws Exception {

		String sql = "create table tmp_tips_order as  select in_link_pid,      " +
				"  node_pid,        row_number() over(partition by in_link_pid, node_pid order by seq_order) seq_order,      " +
				"  count(*) over(partition by in_link_pid, node_pid) cnt   from (select in_link_pid, node_pid, 2 seq_order    " +
				"       from rd_restriction         union all         select in_link_pid, node_pid, 8         " +
				"  from rd_branch         union all         select in_link_pid, node_pid, 1         " +
				"  from rd_lane_connexity)";

		Statement stmt =null;
		try{
			stmt=conn.createStatement();
			stmt.execute(sql);
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(stmt);
		}
	}

	/**
	 * 销毁临时使用的辅助数据
	 * 
	 * @param conn
	 */
	private static void destroyAuxData(java.sql.Connection conn)  throws Exception {

		String sql = "drop table tmp_tips_order purge";
		
		Statement stmt = null;
		try{
			stmt = conn.createStatement();		
			stmt.execute(sql);
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(stmt);
		}
	}

	/**
	 * 执行创建Tips
	 * 
	 * @param fmgdbOA
	 *            fmgdb+连接
	 * @param pmOA
	 *            项目库连接
	 * @param uuid
	 *            唯一标识
	 * @return True成功
	 * @throws Exception
	 */
	public boolean run(OracleAddress fmgdbOA, String uuid)
			throws Exception {

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		
		createTabIfNotExists(hbaseConn, HBaseConstant.tipTab);
		Table htab = null;
		try{
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
			
	//		prepareAuxData(pmOA.getConn());
	
			BridgeTipsBuilder.importTips(fmgdbOA.getConn(), htab);
			
			RdLaneConnexityTipsBuilder.importTips(fmgdbOA.getConn(), htab);
			
			RdCrossTipsBuilder.importTips(fmgdbOA.getConn(), htab);
	
			DirectTipsBuilder.importTips(fmgdbOA.getConn(), htab);
	
			HighwayTipsBuilder.importTips(fmgdbOA.getConn(), htab);
	
			RdRestrictionTipsBuilder.importTips(fmgdbOA.getConn(), htab);
			
			RdSpeedLimitTipsBuilder.importTips(fmgdbOA.getConn(), htab);
			
			Mark3DTipsBuilder.importTips(fmgdbOA.getConn(), htab);
			
			ConstructTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		}catch (Exception e) {
			throw e;
		}finally {
			if(htab!=null){
				htab.close();
			}
		}
		return false;
	}
	public static void main(String[] args) throws Exception {

		String uuid = UuidUtils.genUuid();
		
		String username1 = "gdb240_15win_ml_6p_1216";
		
		String password1 ="gdb240_15win_ml_6p_1216";
		
		int port1 =1521;
		
		String ip1 = "192.168.4.131";
		
		String serviceName1 = "orcl";
		
		OracleAddress oa1 = new OracleAddress(username1,password1,port1,ip1,serviceName1);
		
		TipsBuilder b = new TipsBuilder();
		
		b.run(oa1, uuid);
		
		System.out.println("done");
		
	}
}
