package com.navinfo.dataservice.dao.fcc;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SolrController {

	private static final Logger logger = Logger.getLogger(SolrController.class);

	public SolrController() {
	}

	public List<JSONObject> queryTipsWeb(String wkt, int type, JSONArray stages, boolean isPre, Set<Integer> taskList)
			throws Exception {
		// 没有任务号过滤的 默认为null
		return queryWebTips(wkt, type, stages, isPre, taskList);
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

			builder.append(" AND stage in (");

			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);

				if (i > 0) {
					builder.append(",");
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
	public List<TipsDao> queryTipsByTask(Connection tipsConn,int taskId, int taskType) throws Exception {
		
		StringBuilder builder = new StringBuilder("select * from tips_index i where ("); // 默认条件全查，避免后面增加条件，都需要有AND
        StringBuilder whereBuilder = new StringBuilder();
        addTaskFilterSql(taskId, taskType, whereBuilder); // 任务号过滤
        builder.append(whereBuilder);
		builder.append(")");

		TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
		List<TipsDao> tipsDao = operator.query(builder.toString());

		return tipsDao;
	}
	
	/**
	 * 按照任务和状态筛选Tips
	 * 
	 * @param taskId
	 * @param taskType
	 * @param tipStatus
	 * @return
	 * @throws Exception
	 */
	public List<TipsDao> queryTipsByTask(Connection tipsConn,int taskId, int taskType, int tipStatus) throws Exception {
		StringBuilder builder = new StringBuilder("select * from tips_index i where ("); // 默认条件全查，避免后面增加条件，都需要有AND
		StringBuilder whereBuilder = new StringBuilder();
        addTaskFilterSql(taskId, taskType, whereBuilder); // 任务号过滤
        builder.append(whereBuilder);
		builder.append(")");
		builder.append(" and i.t_tipStatus=" + tipStatus);
		TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
		List<TipsDao> tipsDao = operator.query(builder.toString());
		//SolrDocumentList sdList = this.queryTipsSolrDocFilter(builder.toString(), fqBuilder.toString());
		return tipsDao;
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
	private void addTaskFilterSql(int taskId, int taskType, StringBuilder builder) throws Exception {

		if (taskType == TaskType.Q_TASK_TYPE) {
			if (builder.length() > 0) {
				builder.append(" AND ");
			}
			builder.append("s_qTaskId =" + taskId);

		} else if (taskType == TaskType.Q_SUB_TASK_TYPE) {
			if (builder.length() > 0) {
				builder.append(" AND ");
			}
			builder.append("s_qSubTaskId =" + taskId);

		} else if (taskType == TaskType.M_TASK_TYPE) {
			if (builder.length() > 0) {
				builder.append(" AND ");
			}
			builder.append("s_mTaskId =" + taskId);

		} else if (taskType == TaskType.M_SUB_TASK_TYPE) {
			if (builder.length() > 0) {
				builder.append(" AND ");
			}
			builder.append("s_mSubTaskId =" + taskId);

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
	 * @param taskList
	 * @return
	 * @author: y
	 * @throws IOException
	 * @throws Exception
	 * @time:2017-4-19 下午1:15:51
	 */
	public List<JSONObject> queryWebTips(String wkt, int type, JSONArray stages, boolean isPre, Set<Integer> taskList)
			throws Exception {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);

			StringBuilder builder = new StringBuilder("select * from tips_index where ");

			builder.append(" s_sourceType='");
			builder.append(type);
			builder.append("'");

			addStageFilterSql(stages, builder);


			// 过滤315 web不显示的tips 20170118
			if (!"".equals(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
				if ("".equals(builder.toString())) {
					builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
				} else {
					builder.append(" AND " + SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
				}
			}

			if (taskList != null) {

				addTaskIdFilterSql(builder, taskList);

			}


			builder.append(" and sdo_relate(wkt,sdo_geometry(:1,8307),'mask=anyinteract') = 'TRUE'");

			builder.append(" order by t_operateDate desc");

			List<TipsDao> sdList = operator.query(builder.toString(), ConnectionUtil.createClob(conn, wkt));

			for (TipsDao tipsDao:sdList) {
                JSONObject snapshot = this.tipsFromJSONObject(tipsDao);
				snapshots.add(snapshot);
			}
		}
		catch (Exception ex){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw ex;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
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

			builder.append(" AND s_qTaskId in (");

			int i = 0;
			for (Integer taskId : taskList) {
				if (i > 0) {
					builder.append(",");
				}
				builder.append(taskId);
				i++;
			}

			builder.append(")");
		}

	}

	/**
	 * 根据快线采集任务ID查询Tips
	 * 
	 * @param collectTaskIds
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @throws SQLException 
	 */
	public List<JSONObject> queryCollectTaskTips(Set<Integer> collectTaskIds, int taskType)
			throws Exception {
		StringBuilder builder = new StringBuilder();
		String solrIndexFiled = null;
		if (taskType == TaskType.PROGRAM_TYPE_Q) {
			solrIndexFiled = "s_qTaskId";
		} else if (taskType == TaskType.PROGRAM_TYPE_M) {
			solrIndexFiled = "s_mTaskId";
		}
		if (collectTaskIds.size() > 0) {
			builder.append(solrIndexFiled);
			builder.append("in (");
			int index = 0;
			for (int collectTaskId : collectTaskIds) {
				if (index != 0)
					builder.append(",");
				builder.append(collectTaskId);
				index++;
			}
			builder.append(")");
		}
		logger.info("queryCollectTaskTips:" + builder.toString());
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);
			List<TipsDao> sdList = operator.query("select * from tips_index where "+builder);
			List<JSONObject> snapshots = new ArrayList<JSONObject>();
			for (TipsDao tipsDao:sdList) {
                JSONObject snapshot = this.tipsFromJSONObject(tipsDao);
				snapshots.add(snapshot);
			}
			return snapshots;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

    public static JSONObject tipsFromJSONObject(TipsDao tipsDao) {
        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
        JSONObject json = JSONObject.fromObject(tipsDao, jsonConfig);
        return json;
    }
}
