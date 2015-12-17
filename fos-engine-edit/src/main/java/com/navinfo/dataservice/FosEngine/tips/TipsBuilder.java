package com.navinfo.dataservice.FosEngine.tips;

import java.io.IOException;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.db.OracleAddress;
import com.navinfo.dataservice.FosEngine.comm.service.ProgressService;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.tips.Rotary.RotaryTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.bridge.BridgeTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.connexity.RdLaneConnexityTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.construct.ConstructTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.cross.RdCrossTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.direct.DirectTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.elect.RdElectTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.forbicross.ForbiCrossTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.highway.HighwayTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.linkname.LinkNameTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.restriction.RdRestrictionTipsBuilder;
import com.navinfo.dataservice.FosEngine.tips.speedLimit.RdSpeedLimitTipsBuilder;

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
	public static void createTabIfNotExists(Connection connection,
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
	public boolean run(OracleAddress fmgdbOA, OracleAddress pmOA, String uuid)
			throws Exception {
		
		Connection hbaseConn = HBaseAddress.getHBaseConnection();
		
		createTabIfNotExists(hbaseConn, "tips");
		
		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));

		ProgressService progressManager = new ProgressService(pmOA.getConn(), uuid);
		
		progressManager.updateProgress("完成度:" + 10 + "%");
		
		BridgeTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 15 + "%");

		RdLaneConnexityTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 25 + "%");
		
		ConstructTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 30 + "%");
		
		RdCrossTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 35 + "%");
		
		DirectTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 40 + "%");
		
		RdElectTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 45 + "%");
		
		ForbiCrossTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 50 + "%");
		
		HighwayTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 55 + "%");
		
		LinkNameTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 60 + "%");
		
		RdRestrictionTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 70 + "%");
		
		RotaryTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 80 + "%");
		
		RdSpeedLimitTipsBuilder.importTips(fmgdbOA.getConn(), htab);
		
		progressManager.updateProgress("完成度:" + 100 + "%");
		
		return false;
	}

}
