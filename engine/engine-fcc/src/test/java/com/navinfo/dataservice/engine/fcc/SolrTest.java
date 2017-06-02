package com.navinfo.dataservice.engine.fcc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Test;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;
import com.navinfo.dataservice.engine.fcc.tips.TipsLineRelateQuery;
import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;

/**
 * @ClassName: SolrTest.java
 * @author y
 * @date 2017-1-6 上午11:32:49
 * @Description: TODO
 * 
 */
public class SolrTest {

	SolrController solr = new SolrController();

	@Test
	public void testEdit() {

		JSONArray stages = new JSONArray();
		stages.add(1);
		stages.add(2);
		// stages.add(5);
		try {
			List<JSONObject> datas = null;// solr.queryTipsWeb("POLYGON ((-142.94007897377014 83.34101832624152,-142.93962836265564 83.34101832624152,-142.93962836265564 83.3410705787633,-142.94007897377014 83.3410705787633,-142.94007897377014 83.34101832624152))",
											// stages);
			for (JSONObject jsonObject : datas) {
				if (jsonObject.containsKey("t_inStatus")) {
					jsonObject.remove("t_inStatus");
				}
				if (!jsonObject.containsKey("t_pStatus")) {
					jsonObject.put("t_pStatus", 0);
					jsonObject.put("t_mInProc", 0);
					jsonObject.put("t_dInProc", 0);
				}
				if (!jsonObject.containsKey("t_inMeth")) {
					jsonObject.put("t_inMeth", 0);
				}
				if (jsonObject.containsKey("feedback")) {
					try {
						JSONArray arr = JSONArray.fromObject(jsonObject
								.get("feedback"));
						JSONObject newFeedBObject = new JSONObject();
						newFeedBObject.put("f_array", arr);

						jsonObject.put("feedback", newFeedBObject);
						System.out.println(jsonObject.get("id"));
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				System.out.println(jsonObject.get("id"));
				solr.addTips(jsonObject);
			}

			System.out.println("处理完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @Description:修改数据状态（测试造数据）
	 * @author: y
	 * @time:2017-6-1 下午2:28:52
	 */
	@Test
	public void testEditStatus() {
	/*	JSONArray grids = JSONArray
				.fromObject("[59566311,59566322,59566321,59566332,59566333,59566331,59566312]");*/
		JSONArray grids = JSONArray
				.fromObject("[59567601]");
		
		try {
			String wkt = GridUtils.grids2Wkt(grids);
			List<JSONObject> tips = solr.queryTipsWeb(wkt);
			int stage = 2;
			int t_dEditStatus = 2;
			int t_dEditMeth = 2;
			int handler = 1672;
			int count=0;
			for (JSONObject jsonObject : tips) {

				System.out.println("type:"+jsonObject.getString("s_sourceType")+"rowkey:"+jsonObject.getString("id"));
				jsonObject.put("t_dEditStatus", t_dEditStatus);
				jsonObject.put("t_dEditMeth", t_dEditMeth);
				jsonObject.put("handler", handler);
				jsonObject.put("stage", stage);
				
				jsonObject.put("s_qTaskId", 244);
				
				String rowkey = jsonObject.getString("id");

				solr.addTips(jsonObject);

				updateHbaseAddTrack(rowkey, stage, handler, t_dEditStatus,
						t_dEditMeth);
				count++;
				
				if(count>100){
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-6-1 下午2:34:55
	 */
	private void updateHbaseAddTrack(String rowkey, int stage, int handler,
			int t_dEditStatus, int t_dEditMeth) {
		// TODO Auto-generated method stub
		Connection hbaseConn = null;
		Table htab = null;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
			String[] queryColNames = { "track" };

			JSONObject oldTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab,
					rowkey, queryColNames);

			JSONObject track = oldTip.getJSONObject("track");

			JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
			String date=StringUtils.getCurrentTime();
			// 更新hbase
			JSONObject trackInfo = TipsUtils.newTrackInfo(stage, date, handler);
			trackInfoArr.add(trackInfo);// 增加修改后的

			track.put("t_dEditStatus", t_dEditStatus);
			track.put("t_dEditMeth", t_dEditMeth);
			track.put("t_trackInfo", trackInfoArr);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			htab.put(put);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 批量增加solr字段relate_links

	@Test
	public void testUpdate() {

		SolrController solr = new SolrController();
		String rowkey = "";
		String s_sourceType = "";
		try {
			String queryBuilder = new String("stage: (1 2 5 6)  ");
			String filterQueryBuilder = "";
			List<JSONObject> datas = solr.queryTips(queryBuilder,
					filterQueryBuilder);
			for (JSONObject jsonObject : datas) {

				if (jsonObject.containsKey("relate_nodes")) {

					continue;
				}
				rowkey = jsonObject.getString("id");

				s_sourceType = jsonObject.getString("s_sourceType");

				JSONObject deep = JSONObject.fromObject(jsonObject.get("deep"));

				try {

					Map<String, String> relateMap = TipsLineRelateQuery
							.getRelateLine(s_sourceType, deep);

					jsonObject.put("relate_links",
							relateMap.get("relate_links"));

					jsonObject.put("relate_nodes",
							relateMap.get("relate_nodes"));

				} catch (Exception e) {
					System.out.println("转换出错：" + s_sourceType + ":" + rowkey);
					e.printStackTrace();
				}

				if (!jsonObject.containsKey("wktLocation")) {
					// 这个主要是g_location:目前只用于tips的下载和渲染
					jsonObject.put("wktLocation", jsonObject.getString("wkt"));
				}

				if (!jsonObject.containsKey("s_qSubTaskId")) {
					// 这个主要是g_location:目前只用于tips的下载和渲染
					jsonObject.put("s_qSubTaskId", 0);
					jsonObject.put("s_mSubTaskId", 0);
				}

				try {

					solr.addTips(jsonObject);
				} catch (Exception e) {
					System.out.println(jsonObject + ":" + jsonObject);
					System.out.println("转换出错：" + s_sourceType + ":" + rowkey);
					e.printStackTrace();
				}

				System.out.println(rowkey + "处理完毕");
			}
			System.out.println("所有tips处理完毕");
		} catch (Exception e) {
			System.out.println(s_sourceType + ":" + rowkey);
			e.printStackTrace();
		}

	}

	@Test
	public void testQuery() {

		String taskId = "65_2";

		taskId = "9_2";

		// taskId="1";

		String fileName = "e:/" + taskId + ".sql";

		SolrController solr = new SolrController();
		String rowkey = "";
		String s_sourceType = "";
		try {
			int count = 0;

			PrintWriter pw = new PrintWriter(fileName);
			// 65 490
			// String queryBuilder = new
			// String("wkt:\"intersects(MULTIPOLYGON (((116.0625 40.0625, 116.03125 40.0625, 116.03125 40.083333333333336, 116.03125 40.104166666666664, 116.03125 40.10416666666667, 116.03125 40.125, 116.0625 40.125, 116.0625 40.145833333333336, 116.09375 40.145833333333336, 116.125 40.145833333333336, 116.15625 40.145833333333336, 116.1875 40.145833333333336, 116.21875 40.145833333333336, 116.25 40.145833333333336, 116.28125 40.145833333333336, 116.3125 40.145833333333336, 116.34375 40.145833333333336, 116.34375 40.125, 116.34375 40.10416666666667, 116.34375 40.104166666666664, 116.34375 40.083333333333336, 116.3125 40.083333333333336, 116.28125 40.083333333333336, 116.28125 40.0625, 116.25 40.0625, 116.21875 40.0625, 116.1875 40.0625, 116.15625 40.0625, 116.125 40.0625, 116.09375 40.0625, 116.0625 40.0625)), ((116.28125 39.833333333333336, 116.28125 39.85416666666667, 116.3125 39.85416666666667, 116.3125 39.833333333333336, 116.28125 39.833333333333336)), ((116.40625 39.833333333333336, 116.40625 39.85416666666667, 116.4375 39.85416666666667, 116.4375 39.833333333333336, 116.40625 39.833333333333336)), ((116.40625 39.916666666666664, 116.40625 39.9375, 116.4375 39.9375, 116.4375 39.916666666666664, 116.40625 39.916666666666664)), ((116.28125 39.916666666666664, 116.28125 39.9375, 116.3125 39.9375, 116.3125 39.916666666666664, 116.28125 39.916666666666664))))\" AND stage:(1 2)  AND t_date:[20170429000000 TO *] AND  t_lifecycle:3");
			// 9 343
			String queryBuilder = new String(
					"wkt:\"intersects(POLYGON ((116.28125 40.0625, 116.28125 40.083333333333336, 116.3125 40.083333333333336, 116.3125 40.0625, 116.34375 40.0625, 116.34375 40.04166666666667, 116.34375 40.041666666666664, 116.34375 40.020833333333336, 116.3125 40.020833333333336, 116.3125 40, 116.28125 40, 116.25 40, 116.21875 40, 116.1875 40, 116.15625 40, 116.15625 40.020833333333336, 116.125 40.020833333333336, 116.09375 40.020833333333336, 116.0625 40.020833333333336, 116.0625 40.041666666666664, 116.0625 40.04166666666667, 116.0625 40.0625, 116.09375 40.0625, 116.125 40.0625, 116.15625 40.0625, 116.1875 40.0625, 116.21875 40.0625, 116.25 40.0625, 116.28125 40.0625)))\" AND stage:(1 2)  AND t_date:[20170429000000 TO *] AND  t_lifecycle:3");
			// 1 count:7099
			// String queryBuilder=new
			// String("wkt:\"intersects(POLYGON ((116.15625 39.958333333333336, 116.15625 39.979166666666664, 116.15625 39.97916666666667, 116.15625 40, 116.1875 40, 116.21875 40, 116.25 40, 116.28125 40, 116.3125 40, 116.3125 40.020833333333336, 116.34375 40.020833333333336, 116.34375 40.041666666666664, 116.34375 40.04166666666667, 116.34375 40.0625, 116.3125 40.0625, 116.3125 40.083333333333336, 116.34375 40.083333333333336, 116.34375 40.104166666666664, 116.34375 40.10416666666667, 116.34375 40.125, 116.34375 40.145833333333336, 116.3125 40.145833333333336, 116.28125 40.145833333333336, 116.25 40.145833333333336, 116.21875 40.145833333333336, 116.1875 40.145833333333336, 116.15625 40.145833333333336, 116.125 40.145833333333336, 116.09375 40.145833333333336, 116.0625 40.145833333333336, 116.0625 40.125, 116.03125 40.125, 116.03125 40.10416666666667, 116.03125 40.104166666666664, 116.03125 40.083333333333336, 116 40.083333333333336, 115.96875 40.083333333333336, 115.96875 40.104166666666664, 115.9375 40.104166666666664, 115.9375 40.125, 115.9375 40.145833333333336, 115.90625 40.145833333333336, 115.875 40.145833333333336, 115.84375 40.145833333333336, 115.84375 40.166666666666664, 115.84375 40.16666666666667, 115.84375 40.1875, 115.875 40.1875, 115.875 40.208333333333336, 115.875 40.22916666666667, 115.90625 40.22916666666667, 115.90625 40.25, 115.9375 40.25, 115.96875 40.25, 115.96875 40.270833333333336, 115.96875 40.29166666666667, 116 40.29166666666667, 116 40.3125, 116 40.333333333333336, 116.03125 40.333333333333336, 116.03125 40.3125, 116.0625 40.3125, 116.0625 40.333333333333336, 116.09375 40.333333333333336, 116.125 40.333333333333336, 116.125 40.3125, 116.15625 40.3125, 116.1875 40.3125, 116.1875 40.29166666666667, 116.21875 40.29166666666667, 116.21875 40.3125, 116.25 40.3125, 116.25 40.333333333333336, 116.25 40.35416666666667, 116.28125 40.35416666666667, 116.28125 40.375, 116.3125 40.375, 116.34375 40.375, 116.375 40.375, 116.375 40.35416666666667, 116.40625 40.35416666666667, 116.40625 40.333333333333336, 116.4375 40.333333333333336, 116.4375 40.3125, 116.4375 40.29166666666667, 116.46875 40.29166666666667, 116.46875 40.270833333333336, 116.5 40.270833333333336, 116.5 40.25, 116.5 40.229166666666664, 116.46875 40.229166666666664, 116.46875 40.208333333333336, 116.46875 40.1875, 116.5 40.1875, 116.5 40.16666666666667, 116.5 40.166666666666664, 116.5 40.145833333333336, 116.5 40.125, 116.5 40.10416666666667, 116.5 40.104166666666664, 116.5 40.083333333333336, 116.5 40.0625, 116.53125 40.0625, 116.5625 40.0625, 116.5625 40.083333333333336, 116.5625 40.10416666666667, 116.59375 40.10416666666667, 116.625 40.10416666666667, 116.625 40.083333333333336, 116.625 40.0625, 116.65625 40.0625, 116.65625 40.04166666666667, 116.65625 40.041666666666664, 116.65625 40.020833333333336, 116.6875 40.020833333333336, 116.71875 40.020833333333336, 116.75 40.020833333333336, 116.78125 40.020833333333336, 116.78125 40, 116.78125 39.979166666666664, 116.75 39.979166666666664, 116.75 39.958333333333336, 116.78125 39.958333333333336, 116.78125 39.9375, 116.78125 39.91666666666667, 116.78125 39.916666666666664, 116.78125 39.895833333333336, 116.78125 39.875, 116.8125 39.875, 116.84375 39.875, 116.84375 39.85416666666667, 116.875 39.85416666666667, 116.875 39.833333333333336, 116.90625 39.833333333333336, 116.9375 39.833333333333336, 116.9375 39.8125, 116.9375 39.79166666666667, 116.9375 39.791666666666664, 116.9375 39.770833333333336, 116.9375 39.75, 116.9375 39.72916666666667, 116.9375 39.729166666666664, 116.9375 39.708333333333336, 116.96875 39.708333333333336, 116.96875 39.6875, 116.9375 39.6875, 116.90625 39.6875, 116.90625 39.666666666666664, 116.875 39.666666666666664, 116.84375 39.666666666666664, 116.84375 39.645833333333336, 116.8125 39.645833333333336, 116.8125 39.625, 116.8125 39.604166666666664, 116.78125 39.604166666666664, 116.75 39.604166666666664, 116.75 39.625, 116.71875 39.625, 116.71875 39.645833333333336, 116.71875 39.666666666666664, 116.6875 39.666666666666664, 116.65625 39.666666666666664, 116.65625 39.6875, 116.65625 39.708333333333336, 116.65625 39.72916666666667, 116.6875 39.72916666666667, 116.6875 39.75, 116.65625 39.75, 116.65625 39.770833333333336, 116.625 39.770833333333336, 116.625 39.79166666666667, 116.65625 39.79166666666667, 116.65625 39.8125, 116.625 39.8125, 116.59375 39.8125, 116.59375 39.833333333333336, 116.5625 39.833333333333336, 116.53125 39.833333333333336, 116.5 39.833333333333336, 116.5 39.8125, 116.46875 39.8125, 116.46875 39.791666666666664, 116.4375 39.791666666666664, 116.4375 39.770833333333336, 116.40625 39.770833333333336, 116.40625 39.75, 116.375 39.75, 116.375 39.770833333333336, 116.34375 39.770833333333336, 116.3125 39.770833333333336, 116.28125 39.770833333333336, 116.25 39.770833333333336, 116.25 39.791666666666664, 116.25 39.79166666666667, 116.25 39.8125, 116.25 39.833333333333336, 116.21875 39.833333333333336, 116.21875 39.854166666666664, 116.21875 39.85416666666667, 116.21875 39.875, 116.1875 39.875, 116.1875 39.895833333333336, 116.15625 39.895833333333336, 116.15625 39.916666666666664, 116.125 39.916666666666664, 116.125 39.9375, 116.15625 39.9375, 116.15625 39.958333333333336)))\" AND stage:(1 2)  AND t_date:[20170429000000 TO *] AND  t_lifecycle:3");

			String filterQueryBuilder = "";
			List<JSONObject> datas = solr.queryTips(queryBuilder,
					filterQueryBuilder);
			for (JSONObject jsonObject : datas) {

				rowkey = jsonObject.getString("id");

				s_sourceType = jsonObject.getString("s_sourceType");

				pw.println("insert into  test_rowkey2 values ('" + rowkey
						+ "','" + s_sourceType + "');");

				count += 1;
			}

			pw.close();
			System.out.println("count:" + count);
		} catch (Exception e) {
			System.out.println(s_sourceType + ":" + rowkey);
			e.printStackTrace();
		}

	}

}
