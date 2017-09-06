package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.engine.fcc.tips.model.TipsIndexModel;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsSource;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsTrack;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;

public class EdgeMatchTipsOperator extends BaseTipsOperate{

	static String S_SOURCETYPE = "8002";//接边标识tips类型

	private static final Logger logger = Logger
			.getLogger(EdgeMatchTipsOperator.class);

	public EdgeMatchTipsOperator() {
		

	}

	/**
	 * @Description:创建一个tips
	 * @param g_location
	 * @param content
	 *            :feedback.content
	 * @author: y
	 * @param user
	 * @param memo
	 * @throws Exception
	 * @time:2016-11-15 上午11:03:20
	 */
	public String create( JSONObject g_location, String content, int user, String memo, int qSubTaskId) throws Exception {

		Connection hbaseConn;
		java.sql.Connection tipsConn=null;
		Table htab = null;
		String rowkey = null;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			// 1.rowkey
			rowkey = TipsUtils.getNewRowkey(S_SOURCETYPE);

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
			//20170509 状态流转变更
			int t_tipStatus = 2;
			int t_lifecycle = 3;
			//track
			TipsTrack track = new TipsTrack();
			track.setT_lifecycle(t_lifecycle);
			track.setT_date(currentDate);
			track.setT_tipStatus(t_tipStatus);
			track.addTrackInfo(stage, currentDate, user);
			track.setT_dataDate(currentDate);
			JSONObject trackJson = JSONObject.fromObject(track);


			// 4.geometry
			JSONObject jsonGeom = new JSONObject();
			JSONObject g_guide=g_location; //g_guide和g_location值一样
			jsonGeom.put("g_location", g_location);
			jsonGeom.put("g_guide",g_guide ); 

			// source
			// source
			int s_sourceCode = 15;
			int s_qSubTaskId = qSubTaskId;//TODO 快线子任务
			TipsSource source = new TipsSource();
			source.setS_sourceCode(s_sourceCode);
			source.setS_qSubTaskId(s_qSubTaskId);//快线子任务ID
			source.setS_sourceType(S_SOURCETYPE);
			JSONObject sourceJson = JSONObject.fromObject(source);

			// deep;
			/*Object deepObj = null;
			if (!StringUtils.isEmpty(deep)) {
				deepObj = JSONObject.fromObject(deep);
			}*/

			// put
			Put put = new Put(rowkey.getBytes());
			put.addColumn("data".getBytes(), "track".getBytes(), trackJson
					.toString().getBytes());
			com.alibaba.fastjson.JSONObject fastGeom = TipsUtils.netJson2fastJson(jsonGeom);
			put.addColumn("data".getBytes(), "geometry".getBytes(), fastGeom
					.toString().getBytes());
			put.addColumn("data".getBytes(), "feedback".getBytes(), feedbackObj
					.toString().getBytes());
			put.addColumn("data".getBytes(), "source".getBytes(), sourceJson
					.toString().getBytes());

			// solr index json
			TipsDao tipsIndexModel = TipsUtils.generateSolrIndex(rowkey, stage, currentDate, user,
					trackJson, sourceJson, jsonGeom, new JSONObject(), feedbackObj);
			tipsConn=DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
			operator.save(tipsIndexModel);
			//solr.addTips(JSONObject.fromObject(tipsIndexModel));

			List<Put> puts = new ArrayList<Put>();
			puts.add(put);
			htab.put(puts);


		} catch (IOException e) {
			DbUtils.rollbackAndCloseQuietly(tipsConn);
			logger.error("新增tips出错：原因：" + e.getMessage());
			throw new Exception("新增tips出错：原因：" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(tipsConn);
			if(htab != null) {
				htab.close();
			}
		}
		return rowkey;
	}

	



}
