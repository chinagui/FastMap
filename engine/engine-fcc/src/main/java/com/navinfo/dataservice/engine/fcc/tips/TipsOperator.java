package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class TipsOperator {

	private SolrController solr = new SolrController();
    private int fetchNum = Integer.MAX_VALUE;
	private static final Logger logger = Logger.getLogger(TipsOperator.class);

	public TipsOperator() {

	}

	/**
	 * 修改tips
	 * 
	 * @param rowkey
	 * @param handler
	 * @param pid
	 * @param mdFlag
	 * @return
	 * @throws Exception
	 */
	public boolean update(String rowkey, int handler, String pid, String mdFlag, int editStatus, int editMeth)
			throws Exception {

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		if (StringUtils.isNotEmpty(pid)) {
			get.addColumn("data".getBytes(), "deep".getBytes());
		}

		Result result = htab.get(get);

		if (result.isEmpty()) {
			return false;
		}

		Put put = new Put(rowkey.getBytes());

		JSONObject track = JSONObject.fromObject(new String(result.getValue(
				"data".getBytes(), "track".getBytes())));

		JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
//        int editStatus = json.getInt("editStatus");
//        int editMeth = json.getInt("editMeth");

        JSONObject jsonTrackInfo = new JSONObject();
        if(mdFlag.equals("d")) {//日编
            int oldEStatus = track.getInt("t_dEditStatus");
            if (oldEStatus == 0 && editStatus != 0) {
                jsonTrackInfo.put("stage", 2);
            }
            if(oldEStatus != 0 && editStatus == 0) {
                jsonTrackInfo.put("stage", -1);
            }
            track.put("t_dEditStatus", editStatus);
            track.put("t_dEditMeth", editMeth);
        }else if(mdFlag.equals("m")) {//月编
            int oldEStatus = track.getInt("t_mEditStatus");
            if (oldEStatus == 0 && editStatus != 0) {
                jsonTrackInfo.put("stage", 3);
            }
            if(oldEStatus != 0 && editStatus == 0) {
                jsonTrackInfo.put("stage", -1);
            }
            track.put("t_mEditStatus", editStatus);
            track.put("t_mEditMeth", editMeth);
        }

        String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
        JSONObject lastTrack = trackInfoArr.getJSONObject(trackInfoArr.size()-1);
        if(jsonTrackInfo.containsKey("stage")) {
            int curStage = jsonTrackInfo.getInt("stage");
            if(trackInfoArr.size() == 0) {
                // 更新hbase 增一个trackInfo
                if(curStage != -1) {
                    trackInfoArr.add(jsonTrackInfo);
                }
            }else {
                int lastStage = lastTrack.getInt("stage");
                if(lastStage == curStage) {//更新
                    lastTrack.put("date", date);
                    lastTrack.put("handler", handler);
                    trackInfoArr.remove(trackInfoArr.size()-1);
                    trackInfoArr.add(lastTrack);
                }else{//新增
                    if(curStage == -1 && trackInfoArr.size() >= 2) {
                        JSONObject lastSecondTrack = trackInfoArr.getJSONObject(trackInfoArr.size() - 2);
                        int lastSecondStage = lastSecondTrack.getInt("stage");
                        jsonTrackInfo.put("stage", lastSecondStage);
                    }
                    trackInfoArr.add(jsonTrackInfo);
                }
            }
        } else {
            lastTrack.put("date", date);
            lastTrack.put("handler", handler);
            trackInfoArr.remove(trackInfoArr.size()-1);
            trackInfoArr.add(lastTrack);
        }

		track.put("t_trackInfo", trackInfoArr);

		track.put("t_date", date);

		put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
				.getBytes());

		String newDeep = null;

		if (StringUtils.isNotEmpty(pid)) {

			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));
			if (deep.containsKey("id")) {
				deep.put("id", String.valueOf(pid));

				newDeep = deep.toString();

				put.addColumn("data".getBytes(), "deep".getBytes(), deep
						.toString().getBytes());
			}
		}

		JSONObject solrIndex = solr.getById(rowkey);

        if(jsonTrackInfo.containsKey("stage")) {
            solrIndex.put("stage", jsonTrackInfo.getInt("stage"));
        }

		solrIndex.put("t_date", date);

        if(mdFlag.equals("d")) {//日编
            solrIndex.put("t_dEditStatus", editStatus);
            solrIndex.put("t_dEditMeth", editMeth);
        }else if(mdFlag.equals("m")) {//月编
            solrIndex.put("t_mEditStatus", editStatus);
            solrIndex.put("t_mEditMeth", editMeth);
        }



	/*	if (0 == lifecycle) {
			solrIndex.put("t_lifecycle", 2);
		}*/

		solrIndex.put("handler", handler);

		if (newDeep != null) {
			solrIndex.put("deep", newDeep);
		}

		solr.addTips(solrIndex);

		htab.put(put);

		return true;
	}

	/**
	 * 删除tips
	 * 
	 * @param rowkey
	 * @return
	 */
	public boolean delete(String rowkey) {
		return false;
	}

	/**
	 * @Description:批量更新tips状态
	 * @param data
	 *            ：数据 JSON格式：{rowkey:'',status:''}
	 * @param handler
	 *            ：作业员id
	 * @param mdFlag
	 *            ：m 月编；d：日编
	 * @author: y
	 * @throws Exception
	 * @time:2017-2-8 下午3:17:20
	 */
	public void batchUpdateStatus(JSONArray data, int handler, String mdFlag)
			throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        try {
            hbaseConn = HBaseConnector.getInstance().getConnection();

            htab = hbaseConn.getTable(TableName
                    .valueOf(HBaseConstant.tipTab));

            for (Object object : data) {
                JSONObject json = JSONObject.fromObject(object);

                String rowkey = json.getString("rowkey");

                // 1.获取到改前的 feddback和track （还有deep）
                JSONObject oldTip = getOldTips(rowkey, htab);

                JSONObject track = oldTip.getJSONObject("track");

                JSONObject updateKeyValues = new JSONObject(); // 被修改的tips字段的值

                // new 一个trackInfo

                String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

                JSONObject jsonTrackInfo = new JSONObject();

                JSONObject value = new JSONObject();

                int editStatus = json.getInt("editStatus");
                int editMeth = json.getInt("editMeth");
                if (mdFlag.equals("d")) {//日编
                    value.put("t_dEditStatus", editStatus);
                    value.put("t_dEditMeth", editMeth);
                    int oldEStatus = track.getInt("t_dEditStatus");
                    if (oldEStatus == 0 && editStatus != 0) {
                        jsonTrackInfo.put("stage", 2);
                    }
                    if(oldEStatus !=0 && editStatus == 0) {
                        jsonTrackInfo.put("stage", -1);
                    }
                } else if (mdFlag.equals("m")) {//月编
                    value.put("t_mEditStatus", editStatus);
                    value.put("t_mEditMeth", editMeth);
                    int oldEStatus = track.getInt("t_mEditStatus");
                    if (oldEStatus == 0 && editStatus != 0) {
                        jsonTrackInfo.put("stage", 3);
                    }
                    if(oldEStatus !=0 && editStatus == 0) {
                        jsonTrackInfo.put("stage", -1);
                    }
                }
                jsonTrackInfo.put("date", date);
                jsonTrackInfo.put("handler", handler);

                value.put("t_date", date);

                value.put("t_trackInfo", jsonTrackInfo);

                updateKeyValues.put("track", value);

                Put put = updateTips(rowkey, oldTip, updateKeyValues);

                htab.put(put);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(htab != null) {
                htab.close();
            }
//            if(hbaseConn != null) {
//                hbaseConn.close();
//            }
        }

	}

	/**
	 * @Description:修改tips的属性
	 * @param rowkey
	 *            ：tips的rowkey
	 * @param updateKeyValues
	 *            JSONJoject:被修改的属性的键值对
	 * @author: y
	 * @throws Exception
	 * @time:2017-2-8 下午3:44:46
	 */
	private Put updateTips(String rowkey, JSONObject oldTip, JSONObject updateKeyValues)
			throws Exception {

		Set<String> updateAttKeys = updateKeyValues.keySet(); // 被修改的属性

//		Connection hbaseConn;
		try {
//			hbaseConn = HBaseConnector.getInstance().getConnection();
//
//			Table htab = hbaseConn.getTable(TableName
//					.valueOf(HBaseConstant.tipTab));

			// 1.获取到改前的 feddback和track （还有deep）
//			JSONObject oldTip = getOldTips(rowkey, htab);

			JSONObject track = oldTip.getJSONObject("track");

			Put put = new Put(rowkey.getBytes());

			// 更新字段
			for (String key : updateAttKeys) {

				// 1.如果是track，则需要特殊处理
				if ("track".equals(key)) {

					JSONObject trackValues = updateKeyValues
							.getJSONObject("track");

					Set<String> updateTrackFiledsName = trackValues.keySet(); // 被修改的属性

					// 1.1trackInfo特殊处理
                    if(trackValues.containsKey("t_trackInfo")) {
                        JSONObject jsonTrackInfo = trackValues
                                .getJSONObject("t_trackInfo");
                        JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");//tips中track
                        JSONObject lastTrack = trackInfoArr.getJSONObject(trackInfoArr.size()-1);
                        if(jsonTrackInfo.containsKey("stage")) {
                            int curStage = jsonTrackInfo.getInt("stage");
                            if(trackInfoArr.size() == 0) {
                                // 更新hbase 增一个trackInfo
                                if(curStage != -1) {
                                    trackInfoArr.add(jsonTrackInfo);
                                }
                            }else {
                                int lastStage = lastTrack.getInt("stage");
                                if(lastStage == curStage) {//更新
                                    lastTrack.put("date", jsonTrackInfo.getString("date"));
                                    lastTrack.put("handler", jsonTrackInfo.getInt("handler"));
                                    trackInfoArr.remove(trackInfoArr.size()-1);
                                    trackInfoArr.add(lastTrack);
                                }else{//新增
                                    if(curStage == -1 && trackInfoArr.size() >= 2) {
                                        JSONObject lastSecondTrack = trackInfoArr.getJSONObject(trackInfoArr.size() - 2);
                                        int lastSecondStage = lastSecondTrack.getInt("stage");
                                        jsonTrackInfo.put("stage", lastSecondStage);
                                    }
                                    if(curStage != -1) {
                                        trackInfoArr.add(jsonTrackInfo);
                                    }
                                }
                            }
                        } else {
                            lastTrack.put("date", jsonTrackInfo.getString("date"));
                            lastTrack.put("handler", jsonTrackInfo.getInt("handler"));
                            trackInfoArr.remove(trackInfoArr.size()-1);
                            trackInfoArr.add(lastTrack);
                        }

                        track.put("t_trackInfo", trackInfoArr);
                    }

					// 1.2更新track的其他字段
					for (String filedName : updateTrackFiledsName) {
						if ("t_trackInfo".equals(filedName)) {
							continue;
						}
						track.put(filedName, trackValues.get(filedName));
					}

					// 1.3hbase 更新track
					put.addColumn("data".getBytes(), "track".getBytes(), track
							.toString().getBytes());
				}

				// track和feedback外的其他字段直接更新（这个地方需要补充呢，如果是feebback？？）
				// 且source_type：solr怎么更新
				else {
					put.addColumn("data".getBytes(), key.getBytes(),
							updateKeyValues.get(key).toString().getBytes());
				}

				// （这个地方需要补充呢，如果是feebback？？）
				// 且source_type：solr怎么更新,如果更新的是坐标。。。那么wkt要维护

				// 根据修改的字段，更新solr
				updateSorlIndex(rowkey, updateKeyValues);

                return put;

			}
		} catch (Exception e) {

			logger.error(
					"根据rowkey修改tips信息出错：" + rowkey + "\n" + e.getMessage(),
					e.getCause());

			throw new Exception("根据rowkey修改tips信息出错:" + rowkey + "\n"
					+ e.getMessage(), e);
		}
        return null;
	}

	/**
	 * @Description:根据修改后的字段，更新solr信息
	 * @param rowkey
	 * @param updateKeyValues
	 *            ，修改了的字段。json对象
	 * @author: y
	 * @throws Exception 
	 * @time:2017-2-9 上午10:02:10
	 */
	private void updateSorlIndex(String rowkey, JSONObject updateKeyValues) throws Exception {

		// 获取solr数据
		JSONObject solrIndex = solr.getById(rowkey);

		// 更新字段
		Set<String> updateAttKeys = updateKeyValues.keySet(); // 被修改的属性
		for (String key : updateAttKeys) {

			// 1.track相关字段的更新
			if ("track".equals(key)) {

				JSONObject trackValues = updateKeyValues.getJSONObject("track");

				JSONObject jsonTrackInfo = trackValues
						.getJSONObject("t_trackInfo");

                if(jsonTrackInfo.containsKey("stage")) {
                    solrIndex.put("stage", jsonTrackInfo.getInt("stage"));
                }

				solrIndex.put("handler", jsonTrackInfo.getInt("handler"));

				Set<String> updateTrackFiledsName = trackValues.keySet(); // 被修改的track属性

				// 1.2更新track的其他字段
				for (String filedName : updateTrackFiledsName) {

					if ("t_trackInfo".equals(filedName)) {
						continue;
					}

					solrIndex.put(filedName, trackValues.get(filedName));

				}
			}

			// 暂时不修改，待补充 ??????????????????
			if ("source".equals(key)) {

			}

			// 暂时不修改，待完善，编辑端给的是不是编辑后合并好的，如果不是需要在这里用旧的tips信息进行合并 ?????????
			if ("feedback".equals(key)) {

				solrIndex.put("feedback", updateKeyValues.get(key));
			}

			if ("deep".equals(key)) {

				solrIndex.put("deep", updateKeyValues.get(key));
			}
			
			//?????????有没有其他要改的？
			if ("geometry".equals(key)) {

				JSONObject geoJson = updateKeyValues.getJSONObject("geometry");

				if (geoJson.containsKey("g_location")) {

					solrIndex.put("g_location", geoJson.get("g_location"));
				}

				if (geoJson.containsKey("g_guide")) {

					solrIndex.put("g_guide", geoJson.get("g_guide"));
				}

				solrIndex.put("wkt", TipsImportUtils.generateSolrWkt(solrIndex
						.getString("s_sourceType"),
						JSONObject.fromObject(solrIndex.get("deep")),
						JSONObject.fromObject(solrIndex.get("g_location")),
						JSONObject.fromObject(solrIndex.get("feedback"))));
			}
			
		}

		solr.addTips(solrIndex);

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
				jo.put("track", track);

				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}

				byte[] deepByte = result.getValue("data".getBytes(),
						"deep".getBytes());

				String deep = null;

				if (deepByte != null) {
					deep = new String(deepByte);
				}

				jo.put("deep", deep);

				oldTip = jo;
			} catch (Exception e) {
				logger.error(
						"根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(),
						e.getCause());

				throw new Exception("根据rowkey查询tips信息出错：" + rowkey + "\n"
						+ e.getMessage(), e);
			}
		}
		return oldTip;
	}

	/**
	 * @Description:根据快线任务号，更新中线任务号
	 * @param sQTaskId
	 * @param gridMTaskMap
	 * @author: y
	 * @throws Exception 
	 * @time:2017-4-19 下午9:53:48
	 */
	public void batchUpdateMTaskId(int sQTaskId,
			Map<Integer, Integer> gridMTaskMap) throws Exception {
		TipsSelector selector=new TipsSelector();
		String  rowkey="";
		List<Put> puts=new ArrayList<>();
		Connection hbaseConn = null;

		Table htab = null;
		try {
			
			hbaseConn=HBaseConnector.getInstance().getConnection();
			
			htab=hbaseConn
					.getTable(TableName.valueOf(HBaseConstant.tipTab));
			
			List<JSONObject> tipsList=selector.getTipsByTaskId(sQTaskId, TaskType.Q_TASK_TYPE);
			for (JSONObject json : tipsList) {
				
				rowkey=json.getString("id");
				
				String wkt=json.getString("wkt");
				
				Geometry geo =  GeoTranslator.wkt2Geometry(wkt);
				
				Set<String> gridSet=TipsGridCalculate.calculate(geo);
				
				int grid=0;
				
				//多个，取一个grid即可？？？ 待确认
				for (String gridStr : gridSet) {
					
					grid=Integer.parseInt(gridStr);
					
					break;
				}
				
				int mTaskId=gridMTaskMap.get(grid);
				
				//1.update solr
				
				json.put("s_mTaskId", mTaskId);
				
				solr.addTips(json);
				
				//2.update hbase
				Get get = new Get(rowkey.getBytes());

				get.addColumn("data".getBytes(), "source".getBytes());

				Result result = htab.get(get);
				
				JSONObject source = JSONObject.fromObject(new String(result.getValue(
						"data".getBytes(), "source".getBytes())));
				
				
				Put put = new Put(rowkey.getBytes());
				
				source.put("s_mTaskId", mTaskId);
				
				put.addColumn("data".getBytes(), "source".getBytes(), source.toString()
						.getBytes());

				puts.add(put);
				
			}
			
			htab.put(puts);
			
			htab.close();
			
		} catch (Exception e) {
			logger.error("快转中：更新中线出错："+e.getMessage(), e);
			throw new Exception("快转中：更新中线出错："+e.getMessage(), e);
		}
		
		
	}

    /**
     * 根据rowkey列表批快线的任务，子任务号
     * @param taskId
     * @param subtaskId
     * @param tips
     * @throws Exception
     */
    public void batchQuickTask(int taskId, int subtaskId, List<String> tips)
            throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        List<Put> puts = new ArrayList<>();
        try {
            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
            for (String rowkey : tips) {
                //更新hbase
                Get get = new Get(rowkey.getBytes());
                Result result = htab.get(get);
                JSONObject source = JSONObject.fromObject(new String(result.getValue(
                        "data".getBytes(), "source".getBytes())));
                Put put = new Put(rowkey.getBytes());
                source.put("s_qTaskId", taskId);
                source.put("s_qSubTaskId", subtaskId);
                put.addColumn("data".getBytes(), "source".getBytes(), source.toString()
                        .getBytes());
                puts.add(put);

                //更新solr
                JSONObject solrIndex = solr.getById(rowkey);
                solrIndex.put("s_qTaskId", taskId);
                solrIndex.put("s_qSubTaskId", subtaskId);
                solr.addTips(solrIndex);
            }
            htab.put(puts);
            htab.close();
        }catch (Exception e) {
            logger.error("根据rowkey列表批快线的任务，子任务号出错："+e.getMessage(), e);
            throw new Exception("根据rowkey列表批快线的任务，子任务号出错："+e.getMessage(), e);
        }
    }

    /**
     * tips无任务批中线任务号api
     * @param wkt
     * @param midTaskId
     * @throws Exception
     */
    public long batchNoTaskDataByMidTask(String wkt, int midTaskId)
            throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("wkt:\"intersects(");
        builder.append(wkt);
        builder.append(")\"");
        builder.append(" AND s_qTaskId:0");
        builder.append(" AND s_mTaskId:0");
        Connection hbaseConn = null;
        Table htab = null;
        List<Put> puts = new ArrayList<>();
        try {
            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
            SolrDocumentList sdList = solr.queryTipsSolrDocFilter(builder.toString(), null);
            long totalNum = sdList.getNumFound();
            if (totalNum <= fetchNum) {
                for (int i = 0; i < totalNum; i++) {
                    SolrDocument doc = sdList.get(i);
                    JSONObject snapshot = JSONObject.fromObject(doc);
                    String rowkey = snapshot.getString("id");
                    //更新hbase
                    Get get = new Get(rowkey.getBytes());
                    Result result = htab.get(get);
                    JSONObject source = JSONObject.fromObject(new String(result.getValue(
                            "data".getBytes(), "source".getBytes())));
                    Put put = new Put(rowkey.getBytes());
                    source.put("s_mTaskId", midTaskId);
                    put.addColumn("data".getBytes(), "source".getBytes(), source.toString()
                            .getBytes());
                    puts.add(put);

                    //更新solr
                    JSONObject solrIndex = solr.getById(rowkey);
                    solrIndex.put("s_mTaskId", midTaskId);
                    solr.addTips(solrIndex);
                }
            } else {
                // 暂先不处理
            }
            htab.put(puts);
            htab.close();
            return totalNum;
        }catch (Exception e) {
            logger.error("根据rowkey列表批中线任务号出错："+e.getMessage(), e);
            throw new Exception("根据rowkey列表批中线任务号出错："+e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        TipsOperator tipsOperator = new TipsOperator();
        List<String> tips = new ArrayList<>();
        tips.add("111601213315");
//        tipsOperator.batchQuickTask(1, 12, tips);
//        tipsOperator.batchMidTask(888, tips);

    }

}
