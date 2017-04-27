package com.navinfo.dataservice.engine.fcc.tips;

import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.fcc.HBaseConnector;
/*import com.navinfo.nirobot.common.storage.SolrBulkUpdater;
import com.navinfo.nirobot.core.tipsinitialize.utils.TipsBuilderUtils;
import com.navinfo.nirobot.core.tipsprocess.BaseTipsProcessor;
import com.navinfo.nirobot.core.tipsprocess.TipsProcessorFactory;*/

public class TipsDiffer {
	
	
	private static final Logger logger = Logger.getLogger(TipsDiffer.class);
	/**
	 * @throws Exception
	 * @Description:tips差分，当前上传结果和old差分，生成tipsDiff
	 * @time:2017-2-13上午9:20:52
	 */
	public static void tipsDiff( Map<String, String> allNeedDiffRowkeysCodeMap) throws Exception {
		/*String errRowkey = null; // 报错时用
		Connection hbaseConn = null;
		SolrBulkUpdater solrConn = null;

		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			solrConn = new SolrBulkUpdater(TipsBuilderUtils.QueueSize,
					TipsBuilderUtils.ThreadCount);
			Set<String> rowkeySet = allNeedDiffRowkeysCodeMap.keySet();
			if (rowkeySet.size() > 0) {
				for (String rowkey : rowkeySet) {
					errRowkey = rowkey;
					String s_sourceType = allNeedDiffRowkeysCodeMap.get(rowkey);

					BaseTipsProcessor processor = TipsProcessorFactory
							.getInstance().createProcessor(s_sourceType);
					// 20170331新增，有新增的robot中没有支持的tips processor返回空
					if (processor != null) {

						processor.setSolrConn(solrConn);

						processor.diff(rowkey, hbaseConn);

						solrConn.commit();
					}

				}
			}

		} catch (Exception e) {
			logger.error(
					"tips差分报错：rowkey:" + errRowkey + ";出错原因：" + e.getMessage(),
					e);
			throw new Exception("tips差分报错：rowkey:" + errRowkey + ";出错原因："
					+ e.getMessage(), e);
		} finally {
			System.out.println("-----------");
			// 连接不能关

			
			 *  * if(hbaseConn!=null){ HbaseOperator.close(hbaseConn); }
			 * 
			 * if(solrConn!=null){ solrConn.close(); }
			 

		}*/

	}

}
