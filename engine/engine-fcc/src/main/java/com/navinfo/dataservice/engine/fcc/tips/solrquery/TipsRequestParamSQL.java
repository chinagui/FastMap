package com.navinfo.dataservice.engine.fcc.tips.solrquery;

import java.util.*;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SolrQueryUtils;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.TipsWorkStatus;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by zhangjunfang on 2017/5/20.
 */
public class TipsRequestParamSQL {
	private static final Logger logger = Logger
			.getLogger(TipsRequestParamSQL.class);

	public String getByTileWithGap(String parameter) throws Exception {
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		int subtaskId = 0; // web编辑才有

		Subtask subtask = null;
		int subTaskType = 0;// 3 grid粗编 4 区域粗编

		if (jsonReq.containsKey("subtaskId")) {
			ManApi apiService = (ManApi) ApplicationContextUtil
					.getBean("manApi");
			subtaskId = jsonReq.getInt("subtaskId");
			subtask = apiService.queryBySubtaskId(subtaskId);
			subTaskType = subtask.getType();// 3 grid粗编 4 区域粗编
		}

		String mdFlag = null;
		if (jsonReq.containsKey("mdFlag")) {
			mdFlag = jsonReq.getString("mdFlag");
		}
		String pType = null;
		if (jsonReq.containsKey("pType")) {
			pType = jsonReq.getString("pType");
		}

		JSONArray types = new JSONArray();
		if (jsonReq.containsKey("types")) {
			types = jsonReq.getJSONArray("types");
		}

		JSONArray noQFilter = new JSONArray();
		if (jsonReq.containsKey("noQFilter")) {
			noQFilter = jsonReq.getJSONArray("noQFilter");
		}

		JSONArray stages = new JSONArray();
		if (StringUtils.isNotEmpty(pType)) {
			if (pType.equals("sl")) {// 矢量化 赵航
				stages.add(0);
				stages.add(1);
				stages.add(2);
				stages.add(6);
			} else if (pType.equals("ms")) {// 生产管理 万冲
				stages.add(0);
				stages.add(1);
				stages.add(2);
				stages.add(3);
				stages.add(6);
				stages.add(7);
				stages.add(8);
			} else if (pType.equals("fc")) {// FC 预处理 钟小明 20170724和玉秀确认，FC预处理不限制stage
//				stages.add(1);
//				stages.add(2);
//				stages.add(3);
//				stages.add(5);
//				stages.add(6);
			}
		} else {// web 刘哲
			if ("d".equals(mdFlag)) {// 日编
				stages.add(1);
				stages.add(2);
				stages.add(5);
				stages.add(6);
				stages.add(7);
			} else if ("m".equals(mdFlag)) {// 月编
				stages.add(1);
				stages.add(3);
				stages.add(5);
				stages.add(6);
				stages.add(7);
			}
		}

		StringBuilder builder = new StringBuilder();

		boolean remove8001=false;
		if(stages.size()>0 && StringUtils.isEmpty(pType)){
			// WEB
			// 类型过滤
			// 日编Grid粗编子任务作业时不展示FC预处理tips（8001）
			// 3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
			if (subTaskType == 3) {
//				builder.append(" AND s_sourceType!='8001'");// 接边Tips
				remove8001=true;
			} else if (subTaskType == 4) {// 4 区域粗编
//				builder.append(" AND s_sourceType='8001'");// 预处理提交
				types = new JSONArray();
				types.add("8001");
			}
		}

		if (types.size() > 0) {
			Set<String> typeSet = new HashSet<>();
			for(int i=0;i<types.size();i++){
				typeSet.add(types.getString(i));
			}
			for(String type : this.getFilter315()){
				typeSet.remove(type);
			}
			if(remove8001){//过滤8001
				typeSet.remove("8001");
			}
			if(typeSet.size()>0) {
				this.getStringArrayQuery(builder, typeSet, "s_sourceType");
			}
		}else{
			// 过滤315 web不显示的tips 20170118
			if(remove8001){
				//除了要过滤的tips，还要过滤8001
				this.getFilter315With8001(builder);
			}else {
				//不过滤8001
				this.getFilter315(builder);
			}
		}

		if (stages.size() > 0) {
			this.getIntArrayQuery(builder, stages, "stage");
		}
			if (StringUtils.isNotEmpty(pType)) {
				if (pType.equals("sl")) {// 矢量化 赵航
				} else if (pType.equals("ms")) {// 生产管理 万冲
					builder.append(" and t_tipStatus=2");
					// 20170615 过滤内业Tips
					builder.append(" and not REGEXP_LIKE(s_sourceType,'^80')");
					// 20170510 增加中线有无过滤
					addTaskFilterSql(noQFilter, builder);
				} else if (pType.equals("fc")) {// FC 预处理 钟小明
					builder.append(" AND (t_tipStatus=2 OR (s_sourceType='8001' AND t_tipStatus=1))");
				}
			} else {// web 刘哲
				StringBuilder webBuilder = new StringBuilder();
				JSONArray workStatus = null;
				if (jsonReq.containsKey("workStatus")) {
					workStatus = jsonReq.getJSONArray("workStatus");
				}

				// 状态过滤
				if (workStatus == null
						|| workStatus.contains(9)
						|| (workStatus.contains(0) && workStatus.contains(1) && workStatus
								.contains(2))) {
					if (webBuilder.length() > 0) {
						webBuilder.append(" OR ");
					}
					webBuilder.append("(t_tipStatus=2)");
				} else {
					if (workStatus.contains(0)) {

						if (webBuilder.length() > 0) {
							webBuilder.append(" OR ");
						}
						webBuilder
								.append("(t_tipStatus=2 AND t_dEditStatus=0 AND stage in (1,2,5,6,7))");

					}
					if (workStatus.contains(1)) {
						if (webBuilder.length() > 0) {
							webBuilder.append(" OR ");
						}
						webBuilder
								.append("(stage in (2,7) AND t_dEditStatus=1)");
					}
					if (workStatus.contains(2)) {
						if (webBuilder.length() > 0) {
							webBuilder.append(" OR ");
						}
						webBuilder
								.append("(stage in (2,7) AND t_dEditStatus=2)");
					}
				}

				if (webBuilder.length() > 0) {
					if (builder.length() > 0) {
						builder.append(" AND ");
					}
					builder.append("(");
					builder.append(webBuilder);
					builder.append(")");
				}
			}

		if (builder.length() > 0) {
			builder.append(" and");
		}
		builder.append(" sdo_filter(wktLocation,sdo_geometry(:1,8307)) = 'TRUE'");
		String sql = "select /*+ index(tips_index,IDX_SDO_TIPS_INDEX_WKTLOCATION) */ * from tips_index where " + builder.toString();
		logger.info("getByTileWithGap:" + sql);
		return sql;
	}

	private Set<String> getFilter315() {
		return SolrQueryUtils.notDisplayTipTpye;
	}

	private StringBuilder getFilter315With8001(StringBuilder builder) {
		if (StringUtils
				.isNotEmpty(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQLWith8001)) {
			if (builder.length() == 0) {
				builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQLWith8001);
			} else {
				builder.append(" AND "
						+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQLWith8001);
			}
		}
		return builder;
	}

	private StringBuilder getFilter315(StringBuilder builder) {
		if (StringUtils
				.isNotEmpty(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
			if (builder.length() == 0) {
				builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			} else {
				builder.append(" AND "
						+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
			}
		}
		return builder;
	}

	private StringBuilder getStringArrayQuery(StringBuilder builder,
											  Set<String> stringArray, String fieldName) {

		if (stringArray != null) {
			if (builder.length() > 0) {
				builder.append(" AND");
			}
			builder.append(" " + fieldName + " in (");
			int i=0;
			for (String fieldValue : stringArray) {
				if (i > 0) {
					builder.append(",");
				}
				builder.append("'");
				builder.append(fieldValue);
				builder.append("'");
				i++;
			}
			builder.append(")");
		}

		return builder;
	}

    private StringBuilder getIntArrayQueryFromString(StringBuilder builder,
                                              List<Integer> stringArray, String fieldName) {

        if (stringArray != null) {
            if (builder.length() > 0) {
                builder.append(" AND");
            }
            builder.append(" " + fieldName + " in (");
            int i=0;
            for (Integer fieldValue : stringArray) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append("'");
                builder.append(fieldValue);
                builder.append("'");
                i++;
            }
            builder.append(")");
        }

        return builder;
    }

	private StringBuilder getStringArrayQuery(StringBuilder builder,
			JSONArray stringArray, String fieldName) {

		if (stringArray != null) {
			if (builder.length() > 0) {
				builder.append(" AND");
			}
			builder.append(" " + fieldName + " in (");
			for (int i = 0; i < stringArray.size(); i++) {
				String fieldValue = stringArray.getString(i);
				if (i > 0) {
					builder.append(",");
				}
				builder.append("'");
				builder.append(fieldValue);
				builder.append("'");
			}
			builder.append(")");
		}

		return builder;
	}

	private StringBuilder getIntArrayQuery(StringBuilder builder,
			JSONArray intArray, String fieldName) {
		if (builder.length() > 0) {
			builder.append(" AND");
		}
		builder.append(" " + fieldName + " in (");
		for (int i = 0; i < intArray.size(); i++) {
			int fieldValue = intArray.getInt(i);
			if (i > 0) {
				builder.append(",");
			}
			builder.append(fieldValue);
		}
		builder.append(")");
		return builder;
	}

	/**
	 * 中线有无过滤
	 * 
	 * @param noQFilter
	 * @param builder
	 */
	private void addTaskFilterSql(JSONArray noQFilter, StringBuilder builder) {
		if ((noQFilter != null) && (noQFilter.size() > 0)) {
			builder.append(" AND s_qTaskId=0");
			if (noQFilter.size() < 2) {
				int flag = noQFilter.getInt(0);
				if (flag == 1)
					builder.append(" AND s_mTaskId!=0");
				else if (flag == 2)
					builder.append(" AND s_mTaskId=0");
			}
		}
	}

	public String getTipsCheckWhere(String parameter) throws Exception {
		JSONObject jsonReq = JSONObject.fromObject(parameter);
		// solr查询语句
		StringBuilder builder = new StringBuilder();
		int programType = jsonReq.getInt("programType");
		int subtaskId = jsonReq.getInt("subTaskId");
		if (programType == TaskType.PROGRAM_TYPE_Q) {// 快线
			builder.append("s_qSubTaskId=" + subtaskId);
		} else if (programType == TaskType.PROGRAM_TYPE_M) {// 中线
			builder.append("s_mSubTaskId=" + subtaskId);
		}

		if (jsonReq.containsKey("type")) {
			builder.append(" AND s_sourceType='2001'  AND t_lifecycle=3");
		}
		logger.info("getTipsCheckWhere :" + builder.toString());
		return builder.toString();
	}

	private void getWokerStatusFilterQuery(int woker, int cheker,
			int workStatus, StringBuilder builder, JSONArray rowkeyList) {
		// 1.日编待质检tips：取stage=2，且t_dEditStatus=2，且handler=质检子任务对应的日编子任务所分配的作业员ID的tips；

		builder.append(" AND stage=2 AND t_dEditStatus=2 AND handler=" + woker
				+ "");

	}

	public String getTipsMobileWhere(String date,
			int[] notExpSourceType) {
		String param = " sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ";

		if (date != null && !date.equals("")) {
			param += " AND t_date > to_date('" + date + "','yyyyMMddHH24MIss')"
					+ " ";
		}

		// 过滤的类型
		StringBuilder builder =null;
		// 1. 示例：TITLE:(* NOT "上网费用高" NOT "宽带收费不合理" )
		if (notExpSourceType != null && notExpSourceType.length != 0) {
			builder = new StringBuilder(" AND s_sourceType NOT  IN (");
			for (int i = 0; i < notExpSourceType.length; i++) {
				String fieldValue = String.valueOf(notExpSourceType[i]);
				if (i > 0) {
					builder.append(",");
				}
				builder.append("'");
				builder.append(fieldValue);
				builder.append("'");
			}
			builder.append(")");
		}
		
		if(builder!=null){
			param=param+builder.toString();
		}
		
		return param;

	}

	public String getTipsCheckUnCommit(String parameter) throws Exception {
		JSONObject jsonReq = JSONObject.fromObject(parameter);
		int subtaskId = jsonReq.getInt("subTaskId");
		StringBuilder builder = new StringBuilder();

		builder.append(" t_tipStatus<>2 ");

		int programType = jsonReq.getInt("programType");

		if (programType == TaskType.PROGRAM_TYPE_Q) {// 快线
			builder.append(" AND ");
			builder.append("s_qSubTaskId=");
			builder.append(subtaskId);
		} else if (programType == TaskType.PROGRAM_TYPE_M) {// 中线
			builder.append(" AND ");
			builder.append("s_mSubTaskId=");
			builder.append(subtaskId);
		}
		logger.info("getTipsCheckUnCommit:" + builder.toString());
		return builder.toString();
	}

	public String getTaskFilterSQL(int taskId, int taskType) throws Exception {
		StringBuilder builder = new StringBuilder();
		if (taskType == TaskType.Q_TASK_TYPE) {
			builder.append(" AND s_qTaskId = " + taskId);
		} else if (taskType == TaskType.Q_SUB_TASK_TYPE) {
			builder.append(" AND s_qSubTaskId = " + taskId);
		} else if (taskType == TaskType.M_TASK_TYPE) {
			builder.append(" AND s_mTaskId = " + taskId);
		} else if (taskType == TaskType.M_SUB_TASK_TYPE) {
			builder.append(" AND s_mSubTaskId = " + taskId);
		}
		throw new Exception("不支持的任务类型：" + taskType);
	}

	public String getTipsWebSql(String wkt) {
		return "select * from tips_index where "
				+ " sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' "
				+ " AND "
				+ SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL;
	}

	public OracleWhereClause getSnapShot(String parameter, java.sql.Connection tipsConn) throws Exception {
		JSONObject jsonReq = JSONObject.fromObject(parameter);
		int workStatus = jsonReq.getInt("workStatus");
		int subtaskId = jsonReq.getInt("subtaskId");
		String sourceType = jsonReq.getString("type");


        ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
        Subtask subtask = apiService.queryBySubtaskId(subtaskId);

		// solr查询语句
		StringBuilder builder = new StringBuilder();

		builder.append("sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ");
		List<Object> values = new ArrayList<Object>();
        values.add(ConnectionUtil.createClob(tipsConn, subtask.getGeometry()));

        StringBuilder taskBuilder = null;
        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null && taskSet.size() > 0) {
            taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
        }

        // 日编Grid粗编子任务作业时不展示FC预处理tips（8001）
        int subTaskType = subtask.getType();// 3 grid粗编 4 区域粗编

        if (subTaskType != 3 && subTaskType != 4 && taskBuilder != null) {
            builder.append(" AND " + taskBuilder);
        } else if (subTaskType == 3) {// 3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
            builder.append(" AND ");
            builder.append("(");
            builder.append("s_sourceType='8002'");
            if(taskBuilder != null) {
                builder.append(" OR ");
                builder.append(taskBuilder);
            }
            builder.append(")");

        } else if (subTaskType == 4) {// 4 区域粗编
            // 20170712修改。 如果是区域粗编子任务，tips列表中只统计显示FC预处理Tips（s_sourceType=8001）
            //根据日编子任务查找项目ID，再根据项目ID查找项目ID下的日编子任务
            //TODO 晓毅提供接口
            List<Integer> projectSet = apiService.queryRudeSubTaskBySubTask(subtaskId);
            StringBuilder projectBuilder = new StringBuilder();
            this.getIntArrayQueryFromString(projectBuilder, projectSet, "s_project");
            builder.append(" AND ");
            builder.append(" s_sourceType='8001'");
            builder.append(" AND ");
            builder.append(projectBuilder);
        }

        builder.append(" AND t_tipStatus=2");
        builder.append(" AND s_sourceType=" + sourceType);
        // 315过滤
        this.getFilter315(builder);

        if (subtask.getIsQuality() == 1) {//质检子任务
            builder.append(" AND stage=7");
            builder.append(" ANd handler=" + subtask.getExeUserId());
        }else{//作业子任务
            builder.append(" AND stage in(1,2,5,6)");
        }

        if (workStatus == TipsWorkStatus.PREPARED_WORKING
                || workStatus == TipsWorkStatus.PREPARED_CHECKING) {// 待作业，待质检
            builder.append(" AND t_dEditStatus=0");
        } else if (workStatus == TipsWorkStatus.WORK_HAS_PROBLEM
                || workStatus == TipsWorkStatus.CHECK_HAS_PROBLEM) {// 有问题待确认
            builder.append(" AND t_dEditStatus=1");
        } else if (workStatus == TipsWorkStatus.WORK_HAS_FINISHED
                || workStatus == TipsWorkStatus.CHECK_HAS_FINISHED) {// 已作业,已质检
            builder.append(" AND t_dEditStatus=2");
        }

		logger.info("getSnapShot:" + builder.toString());
		return new OracleWhereClause(builder.toString(), values);
	}

    public OracleWhereClause getTaskRender(String parameter, java.sql.Connection tipsConn) throws Exception {
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        JSONArray workStatus = jsonReq.getJSONArray("workStatus");
        int subtaskId = jsonReq.getInt("subtaskId");

        ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
        Subtask subtask = apiService.queryBySubtaskId(subtaskId);

        // solr查询语句
        StringBuilder builder = new StringBuilder();

        builder.append("sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ");
        List<Object> values = new ArrayList<Object>();
        values.add(ConnectionUtil.createClob(tipsConn, subtask.getGeometry()));

        StringBuilder taskBuilder = null;
        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null && taskSet.size() > 0) {
            taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
        }

        // 日编Grid粗编子任务作业时不展示FC预处理tips（8001）
        int subTaskType = subtask.getType();// 3 grid粗编 4 区域粗编

        if (subTaskType != 3 && subTaskType != 4 && taskBuilder != null) {
            builder.append(" AND " + taskBuilder);
        } else if (subTaskType == 3) {// 3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
            builder.append(" AND ");
            builder.append("(");
            builder.append("s_sourceType='8002'");
            if(taskBuilder != null) {
                builder.append(" OR ");
                builder.append(taskBuilder);
            }
            builder.append(")");

        } else if (subTaskType == 4) {// 4 区域粗编
            // 20170712修改。 如果是区域粗编子任务，tips列表中只统计显示FC预处理Tips（s_sourceType=8001）
            //根据日编子任务查找项目ID，再根据项目ID查找项目ID下的日编子任务
            //TODO 晓毅提供接口
            List<Integer> projectSet = apiService.queryRudeSubTaskBySubTask(subtaskId);
            StringBuilder projectBuilder = new StringBuilder();
            this.getIntArrayQueryFromString(projectBuilder, projectSet, "s_project");
            builder.append(" AND ");
            builder.append(" s_sourceType='8001'");
            builder.append(" AND ");
            builder.append(projectBuilder);
        }

        builder.append(" AND t_tipStatus=2");
        // 315过滤
        this.getFilter315(builder);

        if (subtask.getIsQuality() == 1) {//质检子任务
            builder.append(" AND stage=7");
            builder.append(" ANd handler=" + subtask.getExeUserId());
        }else{//作业子任务
            builder.append(" AND stage in(1,2,5,6)");
        }

        StringBuilder statusBuilder = new StringBuilder();
        if (workStatus.contains(TipsWorkStatus.PREPARED_WORKING)
                || workStatus.contains(TipsWorkStatus.PREPARED_CHECKING)) {// 待作业，待质检
            if(statusBuilder.length() > 0) {
                statusBuilder.append(" OR ");
            }
            statusBuilder.append("t_dEditStatus=0");
        }
        if (workStatus.contains(TipsWorkStatus.WORK_HAS_PROBLEM)
                || workStatus.contains(TipsWorkStatus.CHECK_HAS_PROBLEM)) {// 有问题待确认
            if(statusBuilder.length() > 0) {
                statusBuilder.append(" OR ");
            }
            statusBuilder.append("t_dEditStatus=1");
        }
        if (workStatus.contains(TipsWorkStatus.WORK_HAS_FINISHED)
                || workStatus.contains(TipsWorkStatus.CHECK_HAS_FINISHED)) {// 已作业,已质检
            if(statusBuilder.length() > 0) {
                statusBuilder.append(" OR ");
            }
            statusBuilder.append("t_dEditStatus=2");
        }

        if(statusBuilder.length() > 0) {
            builder.append(" AND ");
            builder.append("(");
            builder.append(statusBuilder);
            builder.append(")");
        }

        logger.info("getSnapShot:" + builder.toString());
        return new OracleWhereClause(builder.toString(), values);
    }

	/**
	 * 根据子任务号获取采集任务ID
	 * 
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 */
	private Set<Integer> getCollectIdsBySubTaskId(int subtaskId)
			throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		Set<Integer> taskSet = manApi.getCollectTaskIdByDaySubtask(subtaskId);
		return taskSet;
	}

	private StringBuilder getSolrIntSetQueryNoAnd(Set<Integer> intSet,
			String fieldName) {
		StringBuilder builder = new StringBuilder();
		builder.append(fieldName + " in (");
		int i = 0;
		for (Integer filedValue : intSet) {
			if (i > 0) {
				builder.append(",");
			}
			builder.append(filedValue);
			i++;
		}
		builder.append(")");
		return builder;
	}

	// 获取Tips个数列表 tip/getStats 接口参数
	public OracleWhereClause getTipStat(String parameter, java.sql.Connection tipsConn) throws Exception {
		JSONObject jsonReq = JSONObject.fromObject(parameter);
//		JSONArray grids = jsonReq.getJSONArray("grids");
//		String wkt = GridUtils.grids2Wkt(grids);
		int workStatus = jsonReq.getInt("workStatus");
		int subtaskId = jsonReq.getInt("subtaskId");

        ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
        Subtask subtask = apiService.queryBySubtaskId(subtaskId);

		// solr查询语句
		StringBuilder builder = new StringBuilder();

		builder.append("sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ");
		List<Object> values = new ArrayList<Object>();
		values.add(ConnectionUtil.createClob(tipsConn, subtask.getGeometry()));

		Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
		StringBuilder taskBuilder = null;
		if (taskSet != null && taskSet.size() > 0) {
			taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
		}

		// 日编Grid粗编子任务作业时不展示FC预处理tips（8001）
		int subTaskType = subtask.getType();// 3 grid粗编 4 区域粗编

		if (subTaskType != 3 && subTaskType != 4 && taskBuilder != null) {
			builder.append(" AND " + taskBuilder);
		} else if (subTaskType == 3) {// 3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
            builder.append(" AND ");
            builder.append("(");
            builder.append("s_sourceType='8002'");
            if(taskBuilder != null) {
                builder.append(" OR ");
                builder.append(taskBuilder);
            }
            builder.append(")");

		} else if (subTaskType == 4) {// 4 区域粗编
			// 20170712修改。 如果是区域粗编子任务，tips列表中只统计显示FC预处理Tips（s_sourceType=8001）
            //根据日编子任务查找项目ID，再根据项目ID查找项目ID下的日编子任务
            //TODO 晓毅提供接口
            List<Integer> projectSet = apiService.queryRudeSubTaskBySubTask(subtaskId);
            StringBuilder projectBuilder = new StringBuilder();
            this.getIntArrayQueryFromString(projectBuilder, projectSet, "s_project");
            builder.append(" AND ");
            builder.append(" s_sourceType='8001'");
            builder.append(" AND ");
            builder.append(projectBuilder);
		}

        builder.append(" AND t_tipStatus=2");

		// 315过滤
		this.getFilter315(builder);

        if (subtask.getIsQuality() == 1) {//质检子任务
            builder.append(" AND stage=7");
            builder.append(" ANd handler=" + subtask.getExeUserId());
        }else{//作业子任务
            builder.append(" AND stage in(1,2,5,6)");
        }

        if (workStatus == TipsWorkStatus.PREPARED_WORKING
                || workStatus == TipsWorkStatus.PREPARED_CHECKING) {// 待作业，待质检
			builder.append(" AND t_dEditStatus=0");
		} else if (workStatus == TipsWorkStatus.WORK_HAS_PROBLEM
                || workStatus == TipsWorkStatus.CHECK_HAS_PROBLEM) {// 有问题待确认
            builder.append(" AND t_dEditStatus=1");
		} else if (workStatus == TipsWorkStatus.WORK_HAS_FINISHED
                || workStatus == TipsWorkStatus.CHECK_HAS_FINISHED) {// 已作业,已质检
            builder.append(" AND t_dEditStatus=2");
		}

		logger.info("getTipStat:" + builder.toString());
		return new OracleWhereClause(builder.toString(), values);
	}

	/**
	 * 质检查询条件
	 * 
	 * @param worker
	 * @param checker
	 * @param workStatus
	 * @param rowkeyList
	 * @return
	 * @throws Exception
	 */
	public String assambleSqlForCheckQuery(int worker, int checker,
			int workStatus, JSONArray rowkeyList) throws Exception {

		// 1.日编待质检tips：取stage=2，且t_dEditStatus=2，且handler=质检子任务对应的日编子任务所分配的作业员ID的tips；
		if (workStatus == TipsWorkStatus.PREPARED_CHECKING) {

			return ("id IN  (select (column_value) from table(clob_to_table(?))) and   stage=2 AND t_dEditStatus=2 and handler="
					+ worker + "");

		}
		// 日编已质检tips：取stage=7，且t_dEditStatus=2，且handler=质检子任务对应的质检员ID；
		else if (workStatus == TipsWorkStatus.CHECK_HAS_FINISHED) {

			return ("id IN  (select (column_value) from table(clob_to_table(?))) and   stage=7 AND t_dEditStatus=2 and handler="
					+ checker + "");

		}
		// ③日编质检有问题待确认tips: 取stage=7，且t_dEditStatus=1，且handler=质检子任务对应的质检员ID；
		else if (workStatus == TipsWorkStatus.CHECK_HAS_PROBLEM) {

			return ("id IN  (select (column_value) from table(clob_to_table(?))) and   stage=7 AND t_dEditStatus=1 and handler="
					+ checker + "");

		}
		return "";

	}

	public String getTipsDayTotal(int subtaskId, int subTaskType,int handler, int isQuality, String statType) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(" sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ");

        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        StringBuilder taskBuilder = null;
        if (taskSet != null && taskSet.size() > 0) {
            taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
        }

        // 日编Grid粗编子任务作业时不展示FC预处理tips（8001）
        if (subTaskType != 3 && subTaskType != 4 && taskBuilder != null) {
            builder.append(" AND " + taskBuilder);
        } else if (subTaskType == 3) {// 3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
            builder.append(" AND ");
            builder.append("(");
            builder.append("s_sourceType='8002'");
            if(taskBuilder != null) {
                builder.append(" OR ");
                builder.append(taskBuilder);
            }
            builder.append(")");

        } else if (subTaskType == 4) {// 4 区域粗编
            // 20170712修改。 如果是区域粗编子任务，tips列表中只统计显示FC预处理Tips（s_sourceType=8001）
            //根据日编子任务查找项目ID，再根据项目ID查找项目ID下的日编子任务
            //TODO 晓毅提供接口
            ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
            List<Integer> projectSet = apiService.queryRudeSubTaskBySubTask(subtaskId);
            StringBuilder projectBuilder = new StringBuilder();
            this.getIntArrayQueryFromString(projectBuilder, projectSet, "s_project");
            builder.append(" AND ");
            builder.append(" s_sourceType='8001'");
            builder.append(" AND ");
            builder.append(projectBuilder);
        }

        builder.append(" AND t_tipStatus=2");

        // 315过滤
        this.getFilter315(builder);

        if (isQuality == 1) {//质检子任务
            builder.append(" AND stage=7");
            builder.append(" ANd handler=" + handler);
        }else{//作业子任务
            builder.append(" AND stage in(1,2,5,6)");
        }

        if(statType.equals("prepared")) {
            builder.append(" AND t_dEditStatus <> 2");
        }

		logger.info("getTipsDayTotal:" + builder.toString());
		return builder.toString();
	}

	private StringBuilder getSolrIntArrayQuery(StringBuilder builder,
			JSONArray intArray, String fieldName) {
		if (builder.length() > 0) {
			builder.append(" AND");
		}
		builder.append(" " + fieldName + " in (");
		for (int i = 0; i < intArray.size(); i++) {
			int fieldValue = intArray.getInt(i);
			if (i > 0) {
				builder.append(",");
			}
			builder.append(fieldValue);
		}
		builder.append(")");
		return builder;
	}

	public String getGpsAndDeleteLinkQuery(int subTaskId, String begin, String end, JSONObject obj) {
		StringBuilder query = new StringBuilder();
		
		//String order = obj.getString("order");
		
		int programType = obj.getInt("programType");
		
		query.append("SELECT * FROM TIPS_INDEX WHERE T_TIPSTATUS = 2 AND S_SOURCETYPE IN (2001,2101)");
		
		if(programType == TaskType.PROGRAM_TYPE_Q){
			query.append(" AND S_QSUBTASKID = " + subTaskId);
		}else if(programType == TaskType.PROGRAM_TYPE_M){
			query.append(" AND S_MSUBTASKID = " + subTaskId);
		}
		
		query.append(" AND T_DATE >= to_timestamp('" + begin + " 00:00:0.000000000','yyyy-mm-dd hh24:mi:ss.ff9')");
		query.append(" AND T_DATE <= to_timestamp('" + end + " 23:59:59.000000000','yyyy-mm-dd hh24:mi:ss.ff9')");

		return query.toString();
	}
}
