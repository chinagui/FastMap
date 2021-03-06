package com.navinfo.dataservice.engine.fcc.tips;

import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.nirobot.core.tipsprocess.BaseTipsProcessor;
import com.navinfo.nirobot.core.tipsprocess.TipsProcessorFactory;

public class TipsDiffer {

	private static final Logger logger = Logger.getLogger(TipsDiffer.class);

	/**
	 * @throws Exception
	 * @Description:tips差分，当前上传结果和old差分，生成tipsDiff
	 * @time:2017-2-13上午9:20:52
	 */
	public static void tipsDiff(Map<String, String> allNeedDiffRowkeysCodeMap)
			throws Exception {
		tipsDiff(allNeedDiffRowkeysCodeMap, null);
	}

	/**
	 * @throws Exception
	 * @Description:tips差分，当前上传结果和old差分，生成tipsDiff
	 * @time:2017-2-13上午9:20:52
	 */
	public static void tipsDiff(Map<String, String> allNeedDiffRowkeysCodeMap,
			java.sql.Connection connOuter) throws Exception {
		String errRowkey = null; // 报错时用
		Connection hbaseConn = null;
		Table htab = null;
		java.sql.Connection oraConnection = null;
		try {
			// 如果调用者传入连接，则直接使用。使用后不要关闭。
			if (connOuter == null) {
				oraConnection = DBConnector.getInstance()
						.getTipsIdxConnection();
			}
			// 如果调用者没有传入，在，新建连接，使用后提交并关闭
			else {
				oraConnection = connOuter;
			}
			hbaseConn = HBaseConnector.getInstance().getConnection();
			Set<String> rowkeySet = allNeedDiffRowkeysCodeMap.keySet();
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
			if (rowkeySet.size() > 0) {
				for (String rowkey : rowkeySet) {
					Get get = new Get(Bytes.toBytes(rowkey));
					boolean isExists = htab.exists(get);
					if (!isExists) {
						continue;
					}
					errRowkey = rowkey;
					String s_sourceType = allNeedDiffRowkeysCodeMap.get(rowkey);

					BaseTipsProcessor processor = TipsProcessorFactory
							.getInstance().createProcessor(s_sourceType);
					// 20170331新增，有新增的robot中没有支持的tips processor返回空
					if (processor != null) {

						processor.setTipsOracleConn(oraConnection);

						processor.diff(rowkey, hbaseConn);

					}

				}
			}

		} catch (Exception e) {

			DbUtils.rollbackAndCloseQuietly(oraConnection);
			logger.error(
					"tips差分报错：rowkey:" + errRowkey + ";出错原因：" + e.getMessage(),
					e);

			throw new Exception("tips差分报错：rowkey:" + errRowkey + ";出错原因："
					+ e.getMessage(), e);
		} finally {
			System.out.println("-----------");
			// 连接不能关
			if (htab != null) {
				htab.close();
			}
			// 若外部传入连接，不能关闭。由调用者关闭(不是外部传来的连接，则需要自行关闭)
			if (connOuter == null) {
				DbUtils.commitAndCloseQuietly(oraConnection);
			}
		}

	}

}
