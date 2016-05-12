package com.navinfo.dataservice.engine.fcc.tips;		
 		
 import java.io.IOException;		
 		
 import javax.sql.DataSource;		
 		
 import org.apache.commons.dbutils.DbUtils;		
 import org.apache.hadoop.hbase.HColumnDescriptor;		
 import org.apache.hadoop.hbase.HTableDescriptor;		
 import org.apache.hadoop.hbase.TableName;		
 import org.apache.hadoop.hbase.client.Admin;		
 import org.apache.hadoop.hbase.client.Connection;		
 import org.apache.hadoop.hbase.client.Table;		
 import org.apache.log4j.Logger;		
 		
 import com.navinfo.dataservice.commons.constant.HBaseConstant;		
 import com.navinfo.dataservice.commons.db.HBaseAddress;		
 import com.navinfo.dataservice.commons.log.LoggerRepos;		
 import com.navinfo.dataservice.dao.fcc.HBaseController;		
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
  * Tips同步		
  */		
public class TipsSync {		
	protected static Logger log = LoggerRepos.getLogger(TipsSync.class);		
 			
	public static void sync(DataSource fmgdbDataSource,DataSource manDataSource)throws Exception{		
		java.sql.Connection fmgdbConn = null;		
		java.sql.Connection manConn = null;		
		Connection hbaseConn = null;		
		try{		
			//1. 		
			hbaseConn = HBaseAddress.getHBaseConnection();		
					
			createOrCleanTable(hbaseConn, "temp_tips4sync");		
			
			Table htab = hbaseConn.getTable(TableName.valueOf("temp_tips4sync"));		
					
			extractTipsFromGdb(fmgdbConn,htab);		
					
			Table fccTable = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));		
			updateTips(htab,fccTable);		
			//		
			updateSyncLog(manConn);		
			fmgdbConn.commit();		
			manConn.commit();		
		}catch(Exception e){		
			DbUtils.rollbackAndCloseQuietly(fmgdbConn);		
			DbUtils.rollbackAndCloseQuietly(manConn);		
			if(hbaseConn!=null)		
			hbaseConn.close();		
		}finally{		
					
		}		
	}		
	
	/**		
	 * 初始化		
	 * 		
	 * @param connection		
	 * @param tabName		
	 * @throws IOException		
	 */		
	private static void createOrCleanTable(Connection connection,		
			String tabName) throws IOException {		
		
		Admin admin = connection.getAdmin();		
		
		TableName tableName = TableName.valueOf(tabName);		
		
		if (admin.tableExists(tableName)) {		
			admin.disableTable(tableName);		
			admin.deleteTable(tableName);		
		}		
		HTableDescriptor htd = new HTableDescriptor(tableName);		
		
		HColumnDescriptor hcd = new HColumnDescriptor("data");		
		htd.addFamily(hcd);		
		admin.createTable(htd);		
	}		
	
	/**		
	 * 执行创建Tips		
	 * 		
	 * @param fmgdbOA		
	 *            fmgdb+连接		
	 * @param Table		
	 *            tips表		
	 * @return 		
	 * @throws Exception		
	 */		
 	public static void extractTipsFromGdb(java.sql.Connection fmgdbConn, Table htab)		
 			throws Exception {		
 		
 		BridgeTipsBuilder.importTipsNoIndex(fmgdbConn, htab);		
 				
 		RdLaneConnexityTipsBuilder.importTips(fmgdbConn, htab);		
 				
 		RdCrossTipsBuilder.importTips(fmgdbConn, htab);		
 		
 		DirectTipsBuilder.importTips(fmgdbConn, htab);		
 		
 		HighwayTipsBuilder.importTips(fmgdbConn, htab);		
 		
 		RdRestrictionTipsBuilder.importTips(fmgdbConn, htab);		
 				
 		RdSpeedLimitTipsBuilder.importTips(fmgdbConn, htab);		
 				
 		Mark3DTipsBuilder.importTips(fmgdbConn, htab);		
 				
 		ConstructTipsBuilder.importTips(fmgdbConn, htab);		
 		
 	}		
	public static void updateTips(Table tempTipsTable,Table fccTipsTable){		
		HBaseController cont = new HBaseController();		
//		Scanner scan = tempTipsTable.getScanner("data".getBytes());		
//		for(List<KeyValue> list:cont.scan(tempTipsTable.get, null, null, null, null)){		
//					
//		}		
	}		
	public static void updateSyncLog(java.sql.Connection manConn)throws Exception{		
				
	}		
}