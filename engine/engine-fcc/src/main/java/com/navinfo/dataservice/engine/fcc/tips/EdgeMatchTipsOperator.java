package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

public class EdgeMatchTipsOperator extends BaseTipsOperate{

	static String S_SOURCETYPE="8002";//接边标识tips类型

	private static final Logger logger = Logger
			.getLogger(EdgeMatchTipsOperator.class);

	public EdgeMatchTipsOperator() {

	}

	/**
	 * @Description:创建一个tips
	 * @param sourceType
	 * @param g_location
	 * @param g_guide
	 * @param content
	 *            :feedback.content
	 * @param deep
	 * @author: y
	 * @param user
	 * @param memo 
	 * @param type
	 * @throws Exception
	 * @time:2016-11-15 上午11:03:20
	 */
	public String create( JSONObject g_location, String content, int user, String memo) throws Exception {

		Connection hbaseConn;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			// 1.rowkey
			String rowkey = TipsUtils.getNewRowkey(S_SOURCETYPE);

			// 2.feedback
			String currentDate = DateUtils.dateToString(new Date(),
					DateUtils.DATE_COMPACTED_FORMAT);

			JSONObject feedbackObj = new JSONObject();

			JSONArray f_array = new JSONArray();
			
			//geo
			int type=6;
			JSONObject newFeedback = TipsUtils.newFeedback(user, content, type,
						currentDate);
			f_array.add(newFeedback);
			
			//memo,如果有，则增加一个备注
			if(StringUtils.isNotEmpty(memo)){
				type=3;
				JSONObject newFeedback2 = TipsUtils.newFeedback(user, memo, type,
							currentDate);
				f_array.add(newFeedback2);
			}
			
			feedbackObj.put("f_array", f_array);
			
			
			// 3.track
			int stage = 2;

			int t_lifecycle = 3;
			int t_command = 0;
			int t_cStatus = 0;
			int t_dStatus = 0;
			int t_mStatus = 0;
			//int t_inStatus = 0;
			int t_inMeth = 0;
			int t_pStatus = 0;
			int t_dInProc = 0;
			int t_mInProc = 0;  

			JSONObject jsonTrack = TipsUtils.generateTrackJson(t_lifecycle,stage,
					user, t_command, null, currentDate,currentDate,t_cStatus, t_dStatus,
					t_mStatus, t_inMeth,t_pStatus,t_dInProc,t_mInProc);

			// 4.geometry
			JSONObject jsonGeom = new JSONObject();
			JSONObject g_guide=g_location; //g_guide和g_location值一样
			jsonGeom.put("g_location", g_location);
			jsonGeom.put("g_guide",g_guide ); 

			// source
			int s_sourceCode = 15;
			int s_reliability = 100;
			JSONObject source = new JSONObject();
			source.put("s_featureKind", 2);
			source.put("s_project", TipsUtils.STRING_NULL_DEFAULT_VALUE);
			source.put("s_sourceCode", s_sourceCode);
			source.put("s_sourceId", TipsUtils.STRING_NULL_DEFAULT_VALUE);
			source.put("s_sourceType", S_SOURCETYPE);
			source.put("s_reliability", 100);
			source.put("s_sourceProvider", 0);

			// deep;
			/*Object deepObj = null;
			if (!StringUtils.isEmpty(deep)) {
				deepObj = JSONObject.fromObject(deep);
			}*/

			// put
			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
					.toString().getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedbackObj
					.toString().getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(), source
					.toString().getBytes());

			/*if (!StringUtils.isEmpty(deep)) {
				put.addColumn("data".getBytes(), "deep".getBytes(), deepObj
						.toString().getBytes());
			}*/

			// solr index json

			JSONObject solrIndex = TipsUtils.generateSolrIndex(rowkey, stage,
					currentDate, currentDate, t_lifecycle, t_command, user,
					t_cStatus, t_dStatus, t_mStatus, S_SOURCETYPE, s_sourceCode,
					g_guide, g_location, null, feedbackObj , s_reliability,t_inMeth,t_pStatus,t_dInProc,t_mInProc);

			solr.addTips(solrIndex);

			List<Put> puts = new ArrayList<Put>();

			puts.add(put);

			htab.put(puts);

			htab.close();
			
			htab.close();
			
			return rowkey;

		} catch (IOException e) {
			logger.error("新增tips出错：原因：" + e.getMessage());
			throw new Exception("新增tips出错：原因：" + e.getMessage(), e);
		}

	}

	

	/**
	 * @Description:删除tips
	 * @param rowkey
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 下午5:21:09
	 */
	public void deleteByRowkey(String rowkey) throws Exception {
		Connection hbaseConn;
		try {
			// delete solr
			solr.deleteByRowkey(rowkey);

			// delete hbase
			hbaseConn = HBaseConnector.getInstance().getConnection();
			
			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));
			
			List list = new ArrayList();
			Delete d1 = new Delete(rowkey.getBytes());
			list.add(d1);
			
			htab.delete(list);
			
			htab.close();
		} catch (SolrServerException e) {

			logger.error("删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
			
			throw new Exception(
					"删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
		}

	}

}
