package com.navinfo.dataservice.engine.man.inforMan;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: InforManService
 * @author code generator
 * @date 2016-06-15 02:27:02
 * @Description: TODO
 */
public class InforManService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private InforManService() {
	}

	private static class SingletonHolder {
		private static final InforManService INSTANCE = new InforManService();
	}

	public static InforManService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void create(JSONObject json, long userId) throws Exception {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockIdArray = json.getJSONArray("blockIds");
			json.remove("blockIds");
			InforMan bean = (InforMan) JsonOperation.jsonToBean(json, InforMan.class);
			String createSql = "insert into infor_man (INFOR_ID, INFOR_STATUS, DESCP, CREATE_USER_ID, "
					+ "COLLECT_PLAN_START_DATE, COLLECT_PLAN_END_DATE, DAY_EDIT_PLAN_START_DATE, DAY_EDIT_PLAN_END_DATE, MONTH_EDIT_PLAN_START_DATE, MONTH_EDIT_PLAN_END_DATE, DAY_PRODUCE_PLAN_START_DATE, DAY_PRODUCE_PLAN_END_DATE, MONTH_PRODUCE_PLAN_START_DATE, MONTH_PRODUCE_PLAN_END_DATE) values(?,?,?,?,"
					+ "to_timestamp('" + bean.getCollectPlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"
					+ bean.getCollectPlanEndDate() + "','yyyy-mm-dd hh24:mi:ss.ff')," + "to_timestamp('"
					+ bean.getDayEditPlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"
					+ bean.getDayEditPlanEndDate() + "','yyyy-mm-dd hh24:mi:ss.ff')," + "to_timestamp('"
					+ bean.getMonthEditPlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"
					+ bean.getMonthEditPlanEndDate() + "','yyyy-mm-dd hh24:mi:ss.ff')," + "to_timestamp('"
					+ bean.getDayProducePlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff')," + "to_timestamp('"
					+ bean.getDayProducePlanEndDate() + "','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"
					+ bean.getMonthProducePlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"
					+ bean.getMonthProducePlanEndDate() + "','yyyy-mm-dd hh24:mi:ss.ff'))";
			run.update(conn, createSql, bean.getInforId(), 2, bean.getDescp(), userId);
			
			InforManOperation.insertInforBlockMapping(conn,blockIdArray,bean.getInforId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public void update(JSONObject json) throws Exception {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("infors");
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				InforMan bean = (InforMan) JsonOperation.jsonToBean(block, InforMan.class);

				String updateSql = "update infor_man set ";
				List<Object> values = new ArrayList<Object>();
				String valueSql = "";
				if (bean != null && bean.getInforStatus() != null&& StringUtils.isNotEmpty(bean.getInforStatus().toString())) {
				 if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				   valueSql += " INFOR_STATUS=?";
				   values.add(bean.getInforStatus());
				}
				if (bean != null && bean.getDescp() != null && StringUtils.isNotEmpty(bean.getDescp().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " DESCP=?";
					   values.add(bean.getDescp());
					
				}
				if (bean != null && bean.getCollectPlanStartDate() != null&& StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " COLLECT_PLAN_START_DATE=?";
					   values.add(bean.getCollectPlanStartDate());
				}
				if (bean != null && bean.getCollectPlanEndDate() != null&& StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " COLLECT_PLAN_END_DATE=?";
					   values.add(bean.getCollectPlanEndDate());
				}
				if (bean != null && bean.getDayEditPlanStartDate() != null&& StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " DAY_EDIT_PLAN_START_DATE=?";
					   values.add(bean.getDayEditPlanStartDate());
				}
				
				if (bean != null && bean.getDayEditPlanEndDate() != null&& StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " DAY_EDIT_PLAN_END_DATE=?";
					values.add(bean.getDayEditPlanEndDate());
				}
				if (bean != null && bean.getMonthEditPlanStartDate() != null&& StringUtils.isNotEmpty(bean.getMonthEditPlanStartDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " MONTH_EDIT_PLAN_START_DATE=?";
					values.add(bean.getMonthEditPlanStartDate());
				}
				
				if (bean != null && bean.getMonthEditPlanEndDate() != null&& StringUtils.isNotEmpty(bean.getMonthEditPlanEndDate().toString())) {
					
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " MONTH_EDIT_PLAN_END_DATE=?";
					values.add(bean.getMonthEditPlanEndDate());
				}
				
				if (bean != null && bean.getDayProducePlanStartDate() != null&& StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " DAY_PRODUCE_PLAN_START_DATE=?";
					values.add(bean.getDayProducePlanStartDate());
				}
				
				if (bean != null && bean.getDayProducePlanEndDate() != null&& StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " DAY_PRODUCE_PLAN_END_DATE=?";
					values.add(bean.getDayProducePlanEndDate());
				}
				
				if (bean != null && bean.getMonthProducePlanStartDate() != null&& StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " MONTH_PRODUCE_PLAN_START_DATE=?";
					values.add(bean.getMonthProducePlanStartDate());
				}
				
				if (bean != null && bean.getMonthProducePlanEndDate() != null&& StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())) {
					if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
					   valueSql += " MONTH_PRODUCE_PLAN_END_DATE=?";
					values.add(bean.getMonthProducePlanEndDate());
				}
				
				if (bean != null && bean.getInforId() != null && StringUtils.isNotEmpty(bean.getInforId().toString())) {
					valueSql += " where INFOR_ID=?";
					values.add(bean.getInforId());
				}
				run.update(conn, updateSql+valueSql, values.toArray());
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public HashMap<String,Object> query(String inforId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select * from infor where INFOR_ID='" + inforId + "'";
			List<HashMap<String,Object>> list = InforManOperation.selectTaskBySql2(conn, selectSql, null);
			if (list.size() > 0) {
				return list.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public void close(List<String> inforIdslist) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String inforIdStr = inforIdslist.toString().replace("[", "'").replace("]", "'").replace(" ", "")
					.replace(",", "','");

			String updateSql = "UPDATE INFOR SET PLAN_STATUS=2 WHERE INFOR_ID IN (" + inforIdStr + ")";
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Page listAll(JSONObject conditionJson, JSONObject orderJson, int currentPageNum, int pageSize)
			throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select i.infor_id," + " i.descp,"
					+ " nvl(u.user_real_name, '') user_name,"
					+ " to_char(i.collect_plan_start_date, 'yyyymmdd') collect_plan_start_date,"
					+ " to_char(i.collect_plan_end_date, 'yyyymmdd') collect_plan_end_date,"
					+ " to_char(i.day_edit_plan_start_date, 'yyyymmdd') day_edit_plan_start_date,"
					+ " to_char(i.day_edit_plan_end_date, 'yyyymmdd') day_edit_plan_end_date,"
					+ " to_char(i.day_produce_plan_start_date, 'yyyymmdd') day_produce_plan_start_date,"
					+ " to_char(i.day_produce_plan_end_date, 'yyyymmdd') day_produce_plan_end_date,"
					+ " to_char(i.month_edit_plan_start_date, 'yyyymmdd') month_edit_plan_start_date,"
					+ " to_char(i.month_edit_plan_end_date, 'yyyymmdd') month_edit_plan_end_date,"
					+ " to_char(i.month_produce_plan_start_date, 'yyyymmdd') month_produce_plan_start_date,"
					+ " to_char(i.month_produce_plan_end_date, 'yyyymmdd') month_produce_plan_end_date,"
					+ " i.infor_status," + " b.block_id," + " k.block_name"
					+ " from infor_man i, infor_block_mapping b, block k, user_info u"
					+ " where i.infor_id = b.infor_id(+)" + "  and b.block_id = k.block_id(+)"
					+ "   and i.create_user_id = u.user_id(+)";
			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("inforId".equals(key)) {
						selectSql += " and i.infor_id=" + conditionJson.getString(key);
					}
					if ("createUserName".equals(key)) {
						selectSql += " and u.USER_REAL_NAME like '%" + conditionJson.getString(key) + "%'";
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("inforStatus".equals(key)) {
						selectSql += (" order by i.infor_status " + orderJson.getString("inforStatus"));
						break;
					}
					if ("inforId".equals(key)) {
						selectSql += (" order by i.infor_id " + orderJson.getString("inforId"));
						break;
					}
				}
			} else {
				selectSql += " order by i.infor_id";
			}
			return InforManOperation.selectInforList(conn, selectSql, null, currentPageNum, pageSize);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public HashMap<String, Object> queryByTaskId(int taskId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select * from infor where task_id='" + taskId + "'";
			List<HashMap<String,Object>> list = InforManOperation.selectTaskBySql2(conn, selectSql, null);
			if (list.size() > 0) {
				return list.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
