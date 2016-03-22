package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.sql.Statement;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.service.ProgressService;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.fcc.tips.bridge.BridgeTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.connexity.RdLaneConnexityTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.cross.RdCrossTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.direct.DirectTipsBuilder;
import com.navinfo.dataservice.engine.fcc.tips.highway.HighwayTipsBuilder;
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

		Statement stmt = conn.createStatement();
		
		stmt.execute(sql);
		
		stmt.close();
	}

	/**
	 * 销毁临时使用的辅助数据
	 * 
	 * @param conn
	 */
	private static void destroyAuxData(java.sql.Connection conn)  throws Exception {

		String sql = "drop table tmp_tips_order purge";
		
		Statement stmt = conn.createStatement();
		
		stmt.execute(sql);
		
		stmt.close();
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
	public boolean run(OracleAddress fmgdbOA, OracleAddress pmOA, String uuid,String solrUrl)
			throws Exception {

		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		createTabIfNotExists(hbaseConn, "tips");

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));
		
//		prepareAuxData(pmOA.getConn());

		ProgressService progressManager = new ProgressService(pmOA.getConn(),
				uuid);

		progressManager.updateProgress("完成度:" + 10 + "%");
//
		BridgeTipsBuilder.importTips(fmgdbOA.getConn(), htab, solrUrl);
//
		progressManager.updateProgress("完成度:" + 15 + "%");
//
		RdLaneConnexityTipsBuilder.importTips(fmgdbOA.getConn(), htab,solrUrl);
//
//		progressManager.updateProgress("完成度:" + 25 + "%");
//
//		ConstructTipsBuilder.importTips(fmgdbOA.getConn(), htab);
//
		progressManager.updateProgress("完成度:" + 30 + "%");

		RdCrossTipsBuilder.importTips(fmgdbOA.getConn(), htab, solrUrl);

//		progressManager.updateProgress("完成度:" + 35 + "%");
//
		DirectTipsBuilder.importTips(fmgdbOA.getConn(), htab,solrUrl);
//
//		progressManager.updateProgress("完成度:" + 40 + "%");
//
//		RdElectTipsBuilder.importTips(fmgdbOA.getConn(), htab);
//
//		progressManager.updateProgress("完成度:" + 45 + "%");
//
//		ForbiCrossTipsBuilder.importTips(fmgdbOA.getConn(), htab);
//
		progressManager.updateProgress("完成度:" + 50 + "%");
//
		HighwayTipsBuilder.importTips(fmgdbOA.getConn(), htab, solrUrl);
//
//		progressManager.updateProgress("完成度:" + 55 + "%");
//
//		LinkNameTipsBuilder.importTips(fmgdbOA.getConn(), htab);
//
		progressManager.updateProgress("完成度:" + 60 + "%");
//
		RdRestrictionTipsBuilder.importTips(fmgdbOA.getConn(), htab,solrUrl);
//
//		progressManager.updateProgress("完成度:" + 70 + "%");
//
//		RotaryTipsBuilder.importTips(fmgdbOA.getConn(), htab);
//
//		progressManager.updateProgress("完成度:" + 80 + "%");

		RdSpeedLimitTipsBuilder.importTips(fmgdbOA.getConn(), htab,solrUrl);

		progressManager.updateProgress("完成度:" + 100 + "%");
		
//		destroyAuxData(pmOA.getConn());

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
		
		String username2 = "fmgdb5";
		
		String password2 ="fmgdb5";
		
		int port2 =1521;
		
		String ip2 = "192.168.4.131";
		
		String serviceName2 = "orcl";
		
		OracleAddress oa2 = new OracleAddress(username2,password2,port2,ip2,serviceName2);
		
		TipsBuilder b = new TipsBuilder();
		
		HBaseAddress.initHBaseAddress("192.168.3.156");
		
		b.run(oa1, oa2, uuid,"http://192.168.4.130:8081/solr/tips");
		
		System.out.println("done");
		
	}
}
