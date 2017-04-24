package com.navinfo.dataservice.dao.fcc;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolrController {

	private int fetchNum = Integer.MAX_VALUE;

	private HttpSolrClient client;

	public SolrController() {
		client = SolrConnector.getInstance().getClient();
	}

	public void addTips(JSONObject json) throws JSONException,
			SolrServerException, IOException {

		SolrInputDocument doc = new SolrInputDocument();

		doc.addField("id", json.getString("id"));

		doc.addField("wkt", json.getString("wkt"));
		
		//这个主要是g_location:目前只用于tips的下载和渲染
		doc.addField("wktLocation", json.getString("wktLocation"));

		doc.addField("stage", json.getInt("stage"));

		doc.addField("t_operateDate", json.getString("t_operateDate"));

		doc.addField("t_date", json.getString("t_date"));

		doc.addField("t_lifecycle", json.getInt("t_lifecycle"));

		doc.addField("t_command", json.getInt("t_command"));

		doc.addField("t_cStatus", json.getInt("t_cStatus"));

		doc.addField("t_dStatus", json.getInt("t_dStatus"));

		// doc.addField("t_inStatus", json.getInt("t_inStatus"));

		doc.addField("t_inMeth", json.getInt("t_inMeth"));

		doc.addField("t_pStatus", json.getInt("t_pStatus"));

		doc.addField("t_dInProc", json.getInt("t_dInProc"));

		doc.addField("t_mInProc", json.getInt("t_mInProc"));

		doc.addField("handler", json.getInt("handler"));

		doc.addField("t_mStatus", json.getInt("t_mStatus"));

		doc.addField("s_sourceCode", json.getInt("s_sourceCode"));

		doc.addField("s_sourceType", json.getString("s_sourceType"));

		doc.addField("g_location", json.getString("g_location"));

		doc.addField("g_guide", json.getString("g_guide"));

		doc.addField("deep", json.getString("deep"));

		doc.addField("feedback", json.getString("feedback"));

		doc.addField("s_reliability", json.getInt("s_reliability"));

		if (json.containsKey("tipdiff")) {

			doc.addField("tipdiff", json.getString("tipdiff"));
		}

		doc.addField("s_qTaskId", json.getInt("s_qTaskId"));

		doc.addField("s_mTaskId", json.getInt("s_mTaskId"));

		doc.addField("s_qSubTaskId", json.getInt("s_qSubTaskId"));

		doc.addField("s_mSubTaskId", json.getInt("s_mSubTaskId"));
		


		// doc.addField("t_fStatus", json.getInt("t_fStatus"));

		// 不是必须的字段

		if (json.containsKey("t_fStatus")) {

			doc.addField("t_fStatus", json.getString("t_fStatus"));
		}

		// doc.addField("tipdiff", json.getString("tipdiff"));

		client.add(doc);

		client.commit();

	}

	public boolean checkTipsMobile(String wkt, String date,
			int[] notExpSourceType) throws SolrServerException, IOException {

		String param = "wkt:\"intersects(" + wkt + ")\"";

		if (date != null && !date.equals("")) {
			param += " AND t_date:[" + date + " TO *]";
		}

		// 过滤的类型
		// 1. 示例：TITLE:(* NOT "上网费用高" NOT "宽带收费不合理" )
		if (notExpSourceType != null && notExpSourceType.length != 0) {
			String typeStr = "( *";
			for (int type : notExpSourceType) {
				typeStr += " NOT \"" + type + "\"";
			}
			typeStr += ")";

			param += " AND s_sourceType:" + typeStr;

			// System.out.println(param);

		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.addField("id");

		query.set("start", 0);

		query.set("rows", 1);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum > 0) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> queryTipsMobile(String wkt, String date,
			int[] notExpSourceType) throws SolrServerException, IOException {
		List<String> rowkeys = new ArrayList<String>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		if (date != null && !date.equals("")) {
			param += " AND t_date:[" + date + " TO *]";
		}

		// 过滤的类型
		// 1. 示例：TITLE:(* NOT "上网费用高" NOT "宽带收费不合理" )
		if (notExpSourceType != null && notExpSourceType.length != 0) {
			String typeStr = "( *";
			for (int type : notExpSourceType) {
				typeStr += " NOT \"" + type + "\"";
			}
			typeStr += ")";

			param += " AND s_sourceType:" + typeStr;

			System.out.println(param);

		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.addField("id");

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		for (int i = 0; i < totalNum; i++) {
			SolrDocument doc = sdList.get(i);

			rowkeys.add(doc.get("id").toString());
		}

		return rowkeys;
	}

	public List<JSONObject> queryTipsWeb(String wkt, int z, double px, double py)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				String location = snapshot.getString("g");

				snapshot.put("g", this.covertLonLat2Piexls(location, z, px, py));

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	/**
	 * @Description:按照wkt ,tip类型 \stage查询tips
	 * @param wkt
	 * @param type
	 * @param stages
	 * @param isPre
	 *            是否是预处理平台，默认不是
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2017-1-5 下午3:25:50
	 */
	public List<JSONObject> queryTipsWeb(String wkt, int type,
			JSONArray stages, boolean isPre) throws SolrServerException,
			IOException {
		// 没有任务号过滤的 默认为null
		return queryWebTips(wkt, type, stages, isPre, null);
	}

	/**
	 * @Description:查询满足条件的tips(预处理用)
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2017-01-09 上午09:17:22
	 */
	public List<JSONObject> queryHasNotSubmitPreTipsByWktAndUser(int user)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder("handler:" + user
				+ " AND t_pStatus:0 AND s_sourceType:8001 ");

		// builder.append("AND wkt:\"intersects(");

		// builder.append(wkt);

		// builder.append(")\" ");

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	/**
	 * @Description:查询满足条件的tips
	 * @param wkt
	 * @param stage
	 * @param t_dStatus
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2016-10-25 下午3:17:22
	 */
	public List<JSONObject> queryTips(String wkt, int stage, int t_dStatus)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder();

		builder.append("wkt:\"intersects(");

		builder.append(wkt);

		builder.append(")\" ");

		builder.append(" AND stage:" + stage);

		builder.append(" AND t_dStatus:" + t_dStatus);

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}
	

	public List<JSONObject> queryTipsWeb(String wkt, JSONArray stages) throws SolrServerException, IOException{
		
		return queryTipsWeb(wkt, stages,null);
	}

	public List<JSONObject> queryTipsWeb(String wkt, JSONArray stages,Set<Integer> taskSet)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		StringBuilder builder = new StringBuilder();

		if (stages.size() > 0) {

			builder.append("stage:(");

			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);

				if (i > 0) {
					builder.append(" ");
				}
				builder.append(stage);
			}

			builder.append(")");
		}

		// 过滤315 web不显示的tips 20170118
		if (!"".equals(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
			if ("".equals(builder.toString())) {
				builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			} else {
				builder.append(" AND "
						+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			}
		}

		// 取掉fc预处理没有提交的tips
		if ("".equals(builder.toString())) {
			builder.append(" -(t_pStatus:0 AND s_sourceType:8001)");
		} else {
			builder.append(" AND -(t_pStatus:0 AND s_sourceType:8001)");
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		if (!"".equals(builder.toString())) {
			query.set("fq", builder.toString());
		}
		
		if (taskSet != null) {

			addTaskIdFilterSql(builder, taskSet);

		}

		query.set("start", 0);

		query.set("rows", fetchNum);

		// query.addField("s_sourceType");

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	public List<JSONObject> queryTipsWeb(String wkt)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		StringBuilder builder = new StringBuilder();

		// 过滤315 web不显示的tips 20170118
		if (!"".equals(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
			if ("".equals(builder.toString())) {
				builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			} else {
				builder.append(" AND "
						+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			}
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		if (!"".equals(builder.toString())) {
			query.set("fq", builder.toString());
		}

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	public List<JSONObject> queryTipsWebType(String wkt, JSONArray types,
			JSONArray stages, boolean filterDelete) throws SolrServerException,
			IOException {
		// 默认不是预处理的tips
		return queryTipsWebType(wkt, types, stages, filterDelete, false);
	}

	public JSONObject getById(String id) throws Exception {

		String param = "id:" + id;

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum == 0) {
			return null;
		}

		SolrDocument doc = sdList.get(0);

		JSONObject snapshot = JSONObject.fromObject(doc);

		return snapshot;
	}

	private String covertLonLat2Piexls(String location, int z, double px,
			double py) {
		StringBuilder sb = new StringBuilder();

		String regex = "[0-9.]+,[0-9.]+";

		String[] splits = location.split(regex);

		sb.append(splits[0]);

		int pos = 1;

		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(location);

		while (matcher.find()) {
			String lonlatStr = matcher.group();

			String[] lonlat = lonlatStr.split(",");

			double lon = Double.parseDouble(lonlat[0]);

			double lat = Double.parseDouble(lonlat[1]);

			JSONArray ja = Geojson.lonlat2Pixel(lon, lat, z, px, py);

			sb.append(ja.getInt(0));

			sb.append(",");

			sb.append(ja.getInt(1));

			sb.append(splits[pos++]);
		}

		return sb.toString();
	}

	/**
	 * @Description:TOOD
	 * @param rowkey
	 * @author: y
	 * @throws IOException
	 * @throws SolrServerException
	 * @time:2016-11-16 下午5:26:52
	 */
	public void deleteByRowkey(String rowkey) throws SolrServerException,
			IOException {

		client.deleteById(rowkey);

		client.commit();

	}

	/**
	 * @Description:渲染接口
	 * @param wkt
	 * @param types
	 * @param stages
	 * @param b
	 * @param isPre
	 * @return
	 * @author: y
	 * @throws IOException
	 * @throws SolrServerException
	 * @time:2017-1-5 下午2:03:57
	 */
	public List<JSONObject> queryTipsWebType(String wkt, JSONArray types,
			JSONArray stages, boolean filterDelete, boolean isPre)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder();

		// builder.append("wkt:\"intersects(" + wkt + ")\"  AND stage:(1 2 3)");

		builder.append("wkt:\"intersects(" + wkt + ")\" ");

		if (filterDelete) {
			// 过滤删除的数据
			builder.append(" AND -t_lifecycle:1 ");
		}

		addStageFilterSql(stages, builder);

		addTypesFileterSql(types, builder);

		// 不是预处理，则需要过滤预处理没提交的tips,t_pStatus=0是没有提交的

		if (!isPre) {

			if ("".equals(builder.toString())) {
				builder.append(" -(t_pStatus:0 AND s_sourceType:8001)");
			} else {
				builder.append(" AND -(t_pStatus:0 AND s_sourceType:8001)");
			}
		}

		// 过滤315 web不显示的tips 20170118
		if (!"".equals(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
			if ("".equals(builder.toString())) {
				builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			} else {
				builder.append(" AND "
						+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			}
		}

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	/**
	 * @Description:增加stage过滤sql
	 * @param stages
	 * @param builder
	 * @author: y
	 * @time:2017-4-17 下午3:34:02
	 */
	private void addStageFilterSql(JSONArray stages, StringBuilder builder) {
		if (stages.size() > 0) {

			builder.append(" AND stage:(");

			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);

				if (i > 0) {
					builder.append(" ");
				}
				builder.append(stage);
			}

			builder.append(")");
		}
	}

	/**
	 * @Description:增加type过滤sql
	 * @param types
	 * @param builder
	 * @author: y
	 * @time:2017-4-17 下午3:33:37
	 */
	private void addTypesFileterSql(JSONArray types, StringBuilder builder) {
		if (types.size() > 0) {

			builder.append(" AND s_sourceType:(");

			for (int i = 0; i < types.size(); i++) {
				String type = types.getString(i);

				if (i > 0) {
					builder.append(" ");
				}
				builder.append(type);
			}

			builder.append(")");
		}
	}

	/**
	 * @Description:根据任务号，任务类型查找tips
	 * @param taskId
	 * @param taskType
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-4-14 下午5:28:14
	 */
	public List<JSONObject> getTipsByTask(int taskId, int taskType)
			throws Exception {

		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "";

		if (taskType == 1) {

			param = "s_qTaskId :" + taskId;

		} else if (taskType == 2) {

			param = "s_qSubTaskId :" + taskId;

		} else if (taskType == 3) {

			param = "s_mTaskId :" + taskId;

		} else if (taskType == 4) {

			param = "s_mSubTaskId :" + taskId;

		} else {
			throw new Exception("不支持的任务类型：" + taskType);
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	/**
	 * @Description:按照任务号+类型查询tips
	 * @return
	 * @author: y
	 * @param taskId
	 *            ：任务id
	 * @param taskType
	 *            :任务类型
	 * @throws Exception
	 * @time:2017-4-17 下午3:23:03
	 */
	public List<JSONObject> queryTipsByTask(int taskId, int taskType)
			throws Exception {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder("*:*"); // 默认条件全查，避免后面增加条件，都需要有AND

		addTaskFilterSql(taskId, taskType, builder); // 任务号过滤

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	/**
	 * @Description:根据任务号+tips类型返回任务号范围内的tips
	 * @param souceTypes
	 *            :tips类型
	 * @param taskId
	 *            :任务号
	 * @param taskType
	 *            ：任务类型
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-4-13 上午9:07:15
	 */
	public List<JSONObject> queryTipsByTaskTaskSourceTypes(
			JSONArray souceTypes, int taskId, int taskType) throws Exception {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder("*:*"); // 默认条件全查，避免后面增加条件，都需要有AND

		addTaskFilterSql(taskId, taskType, builder); // 任务号过滤

		addTypesFileterSql(souceTypes, builder); // 添加类型过滤

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}
		return snapshots;
	}

	/**
	 * @Description:增加任务号过滤sql
	 * @param taskId
	 * @param taskType
	 * @param builder
	 * @throws Exception
	 * @author: y
	 * @time:2017-4-17 下午3:37:36
	 */
	private void addTaskFilterSql(int taskId, int taskType,
			StringBuilder builder) throws Exception {

		if (taskType == TaskType.Q_TASK_TYPE) {

			builder.append(" AND s_qTaskId :" + taskId);

		} else if (taskType == TaskType.Q_SUB_TASK_TYPE) {

			builder.append(" AND s_qSubTaskId :" + taskId);

		} else if (taskType == TaskType.M_TASK_TYPE) {

			builder.append(" AND s_mTaskId :" + taskId);

		} else if (taskType == TaskType.M_SUB_TASK_TYPE) {

			builder.append(" AND s_mSubTaskId :" + taskId);

		} else {
			throw new Exception("不支持的任务类型：" + taskType);
		}
	}

	/**
	 * @Description:TOOD
	 * @param wkt
	 * @param type
	 * @param stages
	 * @param isPre
	 * @param subtaskid
	 * @return
	 * @author: y
	 * @throws IOException
	 * @throws SolrServerException
	 * @time:2017-4-19 下午1:15:51
	 */
	public List<JSONObject> queryWebTips(String wkt, int type,
			JSONArray stages, boolean isPre, Set<Integer> taskList)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder("*:* ");

		builder.append("AND wkt:\"intersects(");

		builder.append(wkt);

		builder.append(")\" AND s_sourceType:");

		builder.append(type);

		addStageFilterSql(stages, builder);

		// 不是预处理，则需要过滤预处理没提交的tips,t_pStatus=0是没有提交的
		if (!isPre) {

			if ("".equals(builder.toString())) {
				builder.append(" -(t_pStatus:0 AND s_sourceType:8001)");
			} else {
				builder.append(" AND -(t_pStatus:0 AND s_sourceType:8001)");
			}
		}

		// 过滤315 web不显示的tips 20170118
		if (!"".equals(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
			if ("".equals(builder.toString())) {
				builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			} else {
				builder.append(" AND "
						+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			}
		}

		if (taskList != null) {

			addTaskIdFilterSql(builder, taskList);

		}

		SolrQuery query = new SolrQuery();
		
		System.out.println(builder.toString());

		query.set("q", builder.toString());

		query.set("sort", "t_operateDate desc");

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	/**
	 * @Description:TOOD
	 * @param builder
	 * @param taskList
	 * @author: y
	 * @time:2017-4-19 下午1:23:30
	 */
	private void addTaskIdFilterSql(StringBuilder builder, Set<Integer> taskList) {

		if (taskList.size() > 0) {

			builder.append(" AND s_qTaskId:(");

			int i = 0;
			for (Integer taskId : taskList) {
				if (i > 0) {
					builder.append(" ");
				}
				builder.append(taskId);
				i++;
			}

			builder.append(")");
		}

	}

    /**
     * 根据快线采集任务ID查询Tips
     * @param collectTaskIds
     * @return
     * @throws SolrServerException
     * @throws IOException
     */
    public List<JSONObject> queryCollectTaskTips(Set<Integer> collectTaskIds) throws SolrServerException, IOException{
        StringBuilder builder = new StringBuilder();
        if (collectTaskIds.size() > 0) {
            builder.append("s_qTaskId:(");
            int index = 0;
            for (int collectTaskId : collectTaskIds) {
                if(index != 0)
                    builder.append(" ");
                builder.append(collectTaskId);
                index ++;
            }
            builder.append(")");
        }
        List<JSONObject> snapshots = this.queryTips(builder.toString(), null);
        return snapshots;
    }

    /**
     * 根据查询条件查询符合条件的所有Tips
     * @param queryBuilder
     * @param filterQueryBuilder
     * @return
     * @throws SolrServerException
     * @throws IOException
     */
    public List<JSONObject> queryTips(String queryBuilder, String filterQueryBuilder) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.set("q", queryBuilder);
        if(StringUtils.isNotEmpty(filterQueryBuilder)){
            query.set("fq", filterQueryBuilder);
        }
        query.set("start", 0);
        query.set("rows", fetchNum);

        List<JSONObject> snapshots = new ArrayList<JSONObject>();
        QueryResponse response = client.query(query);
        SolrDocumentList sdList = response.getResults();
        long totalNum = sdList.getNumFound();
        if (totalNum <= fetchNum) {
            for (int i = 0; i < totalNum; i++) {
                SolrDocument doc = sdList.get(i);
                JSONObject snapshot = JSONObject.fromObject(doc);
                snapshots.add(snapshot);
            }
        } else {
            // 暂先不处理
        }
        return snapshots;
    }


}
