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
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;

/**
 * @ClassName: BaseTipsOperate.java
 * @author y
 * @date 2016-12-27 下午8:00:31
 * @Description: tips操作基础类（放一些公共方法）
 *
 */
public class BaseTipsOperate {

	protected SolrController solr = new SolrController();

	private static final Logger logger = Logger
			.getLogger(BaseTipsOperate.class);


	/**
	 * @Description:更新备注信息（feebacks type=3(文字)）
	 * @param rowkey
	 * @param user
	 * @param memo
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 上午11:29:03
	 */
	public void updateFeedbackMemo(String rowkey, int user, String memo) throws Exception {

		try {

			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));
			//获取solr数据
			JSONObject solrIndex = solr.getById(rowkey);

			int stage=2;
			//如果是预处理的tips则stage=5

			if(solrIndex.getString("s_sourceType").equals(PretreatmentTipsOperator.FC_SOURCE_TYPE)){
				stage=5;
			}


			// 获取到改钱的 feddback和track
			JSONObject oldTip = getOldTips(rowkey, htab);

			// 1.更新feddback和track
			JSONObject track = oldTip.getJSONObject("track");

			JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

			String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

			// 新增一个trackInfo
			JSONObject jsonTrackInfo = new JSONObject();

			jsonTrackInfo.put("stage", stage);

			jsonTrackInfo.put("date", date);

			jsonTrackInfo.put("handler", user);

			trackInfoArr.add(jsonTrackInfo);

			track.put("t_trackInfo", trackInfoArr);

			track.put("t_lifecycle", 2);

			// 2.更新feedback

			// 新增一个f_array type=3的是文字
			JSONObject feedBack = oldTip.getJSONObject("feedback");

			JSONArray f_array = feedBack.getJSONArray("f_array");


			for (Object object : f_array) {

				JSONObject obj = JSONObject.fromObject(object);

				//先删掉

				if (obj.getInt("type") == 3) {

					f_array.remove(obj);

					break;
				}
			}

			// 如果count=0,则说明原来没有备注，则，增加一条

			int type = 3; // 文字

			JSONObject newFeedback = TipsUtils.newFeedback(user, memo, type,
					date);

			f_array.add(newFeedback);

			// 更新feedback
			feedBack.put("f_array", f_array);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedBack
					.toString().getBytes());

			// 同步更新solr

			solrIndex.put("stage", stage);

			solrIndex.put("t_date", date);

			//
			solrIndex.put("t_lifecycle", 2);

			solrIndex.put("handler", user);

			solrIndex.put("feedback", feedBack);

			solr.addTips(solrIndex);

			htab.put(put);

			htab.close();

		} catch (IOException e) {

			logger.error(e.getMessage(), e);

			throw new Exception("改备注信息出错：rowkey:"+rowkey+"原因：" + e.getMessage(), e);
		}

	}





	/**
	 * @Description:获取到tips改前的信息
	 * @param rowkey
	 * @param htab
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 下午2:16:44
	 */
	protected JSONObject getOldTips(String rowkey, Table htab) throws Exception {
		JSONObject oldTip = null;
		List<Get> gets = new ArrayList<Get>();

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		get.addColumn("data".getBytes(), "feedback".getBytes());

		get.addColumn("data".getBytes(), "deep".getBytes());

		gets.add(get);

		Result[] results = htab.get(gets);

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			// String rowkey = new String(result.getRow());

			try {
				JSONObject jo = new JSONObject();

				String track = new String(result.getValue("data".getBytes(),
						"track".getBytes()));
				jo.put("track",track);


				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}


				byte[] deepByte=result.getValue("data".getBytes(),
						"deep".getBytes());

				String deep=null;

				if(deepByte!=null){
					deep = new String(deepByte);
				}

				jo.put("deep",deep);

				oldTip = jo;
			} catch (Exception e) {
				logger.error("根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e.getCause());

				throw new Exception(
						"根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e);
			}
		}
		return oldTip;
	}


	/**
	 * @Description:删除tips
	 * @param rowkey
	 * @author: y
	 * @param user ：删除用户
	 * @param delType :0 逻辑删除，1：物理删除
	 * @throws Exception
	 * @time:2016-11-16 下午5:21:09
	 */
	public void deleteByRowkey(String rowkey, int delType, int user) throws Exception {
		Connection hbaseConn;
		try {
			//物理删除
			if(delType==1){
				physicalDel(rowkey);
			}
			//逻辑删除
			else{
				logicDel(rowkey,user);
			}

		} catch (SolrServerException e) {

			logger.error("删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);

			throw new Exception(
					"删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
		}

	}





	/**
	 * @Description:逻辑删除tips(将t_lifecycle改为1：删除)
	 * @param rowkey：被删除的tips的rowkey
	 * @param user：删除操作的作业员id
	 * @author: y
	 * @throws Exception
	 * @time:2017-4-8 下午4:14:57
	 */
	private void logicDel(String rowkey, int user) throws Exception {

		String date = StringUtils.getCurrentTime();

		//修改hbase
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		Result result = htab.get(get);

		if (result.isEmpty()) {
			throw new Exception("根据rowkey,没有找到需要删除的tips信息，rowkey："+rowkey);
		}

		Put put = new Put(rowkey.getBytes());

		JSONObject track = JSONObject.fromObject(new String(result.getValue(
				"data".getBytes(), "track".getBytes())));

		JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

		JSONObject lastTrackInfo = trackInfoArr.getJSONObject(trackInfoArr.size() - 1);

		int lastStage = lastTrackInfo.getInt("stage");

		JSONObject jo = new JSONObject();

		jo.put("stage", lastStage);

		jo.put("date", date);

		jo.put("handler", user);

		trackInfoArr.add(jo);

		track.put("t_trackInfo", trackInfoArr);

		track.put("t_date", date);

		track.put("t_lifecycle", 1);//将t_lifecycle改为1：删除

		put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
				.getBytes());

		htab.put(put);

		htab.close();


		//同步更新solr
		JSONObject solrIndex=solr.getById(rowkey);

		solrIndex.put("t_lifecycle", 1);

		solrIndex.put("t_date", date);

		solrIndex.put("handler", user);

		solr.addTips(solrIndex);




	}





	/**
	 * @Description:TOOD
	 * @param rowkey
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2017-4-8 下午4:14:15
	 */
	private void physicalDel(String rowkey) throws SolrServerException,
			IOException {
		Connection hbaseConn;
		// delete hbase
		hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName
				.valueOf(HBaseConstant.tipTab));

		List list = new ArrayList();
		Delete d1 = new Delete(rowkey.getBytes());
		list.add(d1);

		htab.delete(list);

		htab.close();

		// delete solr
		solr.deleteByRowkey(rowkey);
	}


}
