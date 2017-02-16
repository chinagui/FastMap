package com.navinfo.dataservice.engine.man.block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.message.MessageOperation;
import com.navinfo.dataservice.engine.man.message.SendEmail;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.net.aso.k;
import oracle.sql.CLOB;
import oracle.sql.STRUCT;

/**
 * @ClassName: BlockService
 * @author code generator
 * @date 2016-06-08 01:32:00
 * @Description: TODO
 */

public class BlockService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private List blockIdList;

	private BlockService() {
	}

	private static class SingletonHolder {
		private static final BlockService INSTANCE = new BlockService();
	}

	public static BlockService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public int batchOpen(long userId, JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("blocks");
			int updateCount = 0;
			List<Integer> blockIdList = new ArrayList<Integer>();
			String createSql = "insert into block_man (BLOCK_MAN_ID, BLOCK_MAN_NAME,CREATE_USER_ID,BLOCK_ID,COLLECT_GROUP_ID, COLLECT_PLAN_START_DATE,"
					+ "COLLECT_PLAN_END_DATE,DAY_EDIT_GROUP_ID,DAY_EDIT_PLAN_START_DATE,DAY_EDIT_PLAN_END_DATE,"
					+ "DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,"
					+ "DESCP,STATUS,TASK_ID,road_Plan_Total,poi_Plan_Total ) "
					+ "values(BLOCK_MAN_SEQ.NEXTVAL,?,?,?,?,to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?,"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "?,?,(SELECT DISTINCT TASK_ID FROM BLOCK T,TASK K WHERE T.CITY_ID=K.CITY_ID AND K.LATEST=1 AND T.BLOCK_ID=?),?,?)";

			Object[][] param = new Object[blockArray.size()][];
			
			//获取block名称
			Map<Integer,String> blockNameMap = BlockOperation.queryBlockNameByBlocks(conn, blockArray);
			List<Integer> updateBlockList = BlockOperation.queryOperationBlocks(conn, blockArray);
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				if (updateBlockList.contains(block.getInt("blockId"))) {
					continue;
				}

				Object[] obj = new Object[] { blockNameMap.get(block.getInt("blockId")),userId, block.getInt("blockId"), block.getInt("collectGroupId"),
						block.getString("collectPlanStartDate"), block.getString("collectPlanEndDate"),
						block.getInt("dayEditGroupId"), block.getString("dayEditPlanStartDate"),
						block.getString("dayEditPlanEndDate"), 
						block.getString("dayProducePlanStartDate"), block.getString("dayProducePlanEndDate"),
						block.getString("descp"), 2, block.getInt("blockId"),block.getInt("roadPlanTotal"),block.getInt("poiPlanTotal") };
				param[i] = obj;
				blockIdList.add(block.getInt("blockId"));
			}
			BlockOperation.openBlockByBlockIdList(conn, blockIdList);

			int[] rows = null;
			if(param[0]!=null){
				rows = run.batch(conn, createSql, param);
			}
			if(rows!=null){
				updateCount = rows.length;
			}
			
			return updateCount;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public int batchUpdate(JSONObject json, long userId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("blocks");
			List blockIdList = new ArrayList();
			int updateCount = 0;
			String createSql = "update block_man set COLLECT_GROUP_ID=?, COLLECT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "COLLECT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_EDIT_GROUP_ID=?,DAY_EDIT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_EDIT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "DAY_PRODUCE_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_PRODUCE_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ " DESCP=?,road_Plan_Total=?,poi_Plan_Total=?   where BLOCK_MAN_ID=?";

			Object[][] param = new Object[blockArray.size()][];
			List<Integer> updateBlockList = BlockOperation.queryOpenOperationBlocks(conn, blockArray);
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				BlockMan bean = (BlockMan) JSONObject.toBean(block, BlockMan.class);
				//if (updateBlockList.contains(bean.getBlockManId())) {
				Object[] obj = new Object[] { bean.getCollectGroupId(), bean.getCollectPlanStartDate(),
						bean.getCollectPlanEndDate(), bean.getDayEditGroupId(), bean.getDayEditPlanStartDate(),
						bean.getDayEditPlanEndDate(),bean.getDayProducePlanStartDate(),
						bean.getDayProducePlanEndDate(), bean.getDescp(),bean.getRoadPlanTotal(),bean.getPoiPlanTotal(),  bean.getBlockManId() };
				param[i] = obj;
				//}
			}
			if (param[0]!=null){
				int[] rows = run.batch(conn, createSql, param);
				updateCount = rows.length;
			}
			//发送消息
			try {
				//查询blockMan数据
				List<Map<String, Object>> blockManList = this.getBlockManByBlockManId(conn, updateBlockList);
				/*block创建/编辑/关闭
				1.分配的采集作业组组长
				2.分配的日编作业组组长
				block变更：XXX(block名称)消息发生变更，请关注*/
				List<Object[]> msgContentList=new ArrayList<Object[]>();
				String msgTitle="block编辑";
				for (Map<String, Object> blockMan : blockManList) {
					String collectGroupLeader=(String) blockMan.get("collectGroupLeader");
					String dayEditGroupLeader=(String) blockMan.get("dayEditGroupLeader");
					if(collectGroupLeader!=null && !collectGroupLeader.isEmpty()){
						Object[] msgTmp=new Object[4];
						msgTmp[0]=collectGroupLeader;
						msgTmp[1]=msgTitle;
						msgTmp[2]="block变更:"+blockMan.get("blockManName")+"消息发生变更,请关注";
						//关联要素
						JSONObject msgParam = new JSONObject();
						msgParam.put("relateObject", "BLOCK_MAN");
						msgParam.put("relateObjectId", blockMan.get("blockManId"));
						msgTmp[3]=msgParam.toString();
						msgContentList.add(msgTmp);
					}
					if(dayEditGroupLeader!=null && !dayEditGroupLeader.isEmpty()){
						Object[] msgTmp=new Object[4];
						msgTmp[0]=dayEditGroupLeader;
						msgTmp[1]=msgTitle;
						msgTmp[2]="block变更:"+blockMan.get("blockManName")+"消息发生变更,请关注";
						//关联要素
						JSONObject msgParam = new JSONObject();
						msgParam.put("relateObject", "BLOCK_MAN");
						msgParam.put("relateObjectId", blockMan.get("blockManId"));
						msgTmp[3]=msgParam.toString();
						msgContentList.add(msgTmp);
					}
					
				}
				if(msgContentList.size()>0){
					blockPushMsgByMsg(conn,msgContentList,userId);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("block编辑消息发送失败,原因:"+e.getMessage(), e);
			}
			
			return updateCount;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<HashMap> listByProduce(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY from BLOCK t where sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry(?, 8307)), 'DISTANCE=0') = 'TRUE'";
			return BlockOperation.queryProduceBlock(conn, selectSql, json);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<HashMap> listByWkt(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();
			String wkt = json.getString("wkt");
			String planningStatus = ((json.getJSONArray("planningStatus").toString()).replace('[', '(')).replace(']',
					')');
			//int type = 1;
			//if(json.containsKey("type")){
			//	type = json.getInt("type");
			//}

			String selectSql = "SELECT T.BLOCK_ID, T.BLOCK_NAME, T.GEOMETRY, T.PLAN_STATUS, T.CITY_ID"
					+ "  FROM BLOCK T"
					+ " WHERE T.PLAN_STATUS IN "+planningStatus
					+ "   AND SDO_ANYINTERACT(T.GEOMETRY, SDO_GEOMETRY('" + wkt + "', 8307)) ="
					+ "       'TRUE'";

			/*if (StringUtils.isNotEmpty(json.getString("snapshot"))) {
				if ("1".equals(json.getString("snapshot"))) {
					selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.PLAN_STATUS,t.CITY_ID,TMP.PERCENT"
							+ " from BLOCK t"
							+ ", (SELECT DISTINCT BM.BLOCK_ID,FSOB.PERCENT FROM BLOCK_MAN BM, FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE BM.BLOCK_MAN_ID = FSOB.BLOCK_MAN_ID(+) AND BM.LATEST = 1) TMP"
							+ " where t.PLAN_STATUS in " + planningStatus
							+ " AND T.BLOCK_ID = TMP.BLOCK_ID";
				}
			};*/
			/*if (!json.containsKey("relation") || ("intersect".equals(json.getString("relation")))) {
				selectSql += " and SDO_ANYINTERACT(t.geometry,sdo_geometry('" + wkt + "',8307))='TRUE'";
			} else {
				if ("within".equals(json.getString("relation"))) {
					selectSql += " and sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry('" + wkt
							+ "', 8307)), 'DISTANCE=0') = 'TRUE'";
				}
			}
			
			if(4==type){
				selectSql += " AND t.CITY_ID = 100002";
			}else if(1==type){
				selectSql += " AND t.CITY_ID < 100000";
			}*/
			log.debug(selectSql);
			return BlockOperation.queryBlockBySql(conn, selectSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public HashMap<?, ?> queryByBlockId(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			// 鎸佷箙鍖�
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONObject obj = JSONObject.fromObject(json);
			BlockMan bean = (BlockMan) JSONObject.toBean(obj, BlockMan.class);

			String selectSql = "select t.CITY_ID, t.BLOCK_NAME, t.GEOMETRY,NVL(TT.NAME,'---') NAME,"
					+ " t.PLAN_STATUS, T.work_property, CASE T.CITY_ID  WHEN 100002 THEN 4 ELSE 1 END TASK_TYPE"
					+ " from BLOCK t,task tt where t.BLOCK_ID = ? and t.city_id=tt.city_id(+)";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>() {
				public HashMap<String, Object> handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("blockManId", 0);
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockManName", rs.getString("BLOCK_NAME"));
						map.put("workProperty", rs.getString("WORK_PROPERTY"));
						map.put("roadPlanTotal", -1);
						map.put("poiPlanTotal", -1);
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						map.put("planStatus", rs.getInt("PLAN_STATUS"));
						map.put("taskName", rs.getString("NAME"));
						map.put("collectGroupId", 0);
						map.put("dayEditGroupId", 0);
						map.put("monthEditGroupId", 0);
						map.put("collectPlanStartDate", "---");
						map.put("collectPlanEndDate", "---");
						map.put("dayEditPlanStartDate", "---");
						map.put("dayEditPlanEndDate", "---");
						map.put("monthEditPlanStartDate", "---");
						map.put("monthEditPlanEndDate", "---");
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("dayProducePlanStartDate", "---");
						map.put("dayProducePlanEndDate", "---");
						map.put("monthProducePlanStartDate", "---");
						map.put("monthProducePlanEndDate", "---");
						map.put("taskType", rs.getInt("task_type"));
						map.put("blockDescp", "");
						map.put("createUserName","---");
						return map;
					}
					return null;
				}

			};
			return run.query(conn, selectSql, rsHandler, bean.getBlockId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}	

	public HashMap<?, ?> query(JSONObject json) throws ServiceException {
		JSONObject objTmp = JSONObject.fromObject(json);
		BlockMan beanTmp = (BlockMan) JSONObject.toBean(objTmp, BlockMan.class);
		if(beanTmp.getBlockManId()==0){return queryByBlockId(json);}
		Connection conn = null;
		try {
			// 鎸佷箙鍖�
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONObject obj = JSONObject.fromObject(json);
			BlockMan bean = (BlockMan) JSONObject.toBean(obj, BlockMan.class);

			String selectSql = "select B.BLOCK_MAN_ID,t.CITY_ID, B.BLOCK_MAN_NAME, t.GEOMETRY,"
					+ " t.PLAN_STATUS, k.name taskName,k.task_type,b.descp,nvl(u.user_real_name, '') USER_REAL_NAME, b.collect_group_id, b.day_edit_group_id,"
					+ " b.month_edit_group_id, to_char(b.collect_plan_start_date, 'yyyymmdd') collect_plan_start_date, to_char(b.collect_plan_end_date, 'yyyymmdd') collect_plan_end_date,"
					+ " to_char(b.day_edit_plan_start_date, 'yyyymmdd') day_edit_plan_start_date, to_char(b.day_edit_plan_end_date, 'yyyymmdd') day_edit_plan_end_date, to_char(b.month_edit_plan_start_date, 'yyyymmdd') month_edit_plan_start_date,"
					+ " to_char(b.month_edit_plan_end_date, 'yyyymmdd') month_edit_plan_end_date,to_char(b.day_produce_plan_start_date, 'yyyymmdd') day_produce_plan_start_date,"
					+ " to_char(b.day_produce_plan_end_date, 'yyyymmdd') day_produce_plan_end_date,"
					+ " to_char(b.month_produce_plan_start_date, 'yyyymmdd') month_produce_plan_start_date,"
					+ " to_char(b.month_produce_plan_end_date, 'yyyymmdd') month_produce_plan_end_date,"
					+ " T.work_property,B.road_plan_total,B.POI_plan_total"
					+ " from BLOCK t, BLOCK_MAN b, TASK k,USER_INFO u where B.BLOCK_MAN_ID = ?"
					+ " and t.block_id = b.block_id and b.task_id = k.task_id and k.latest = 1 and b.latest=1 and b.create_user_id=u.user_id ";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>() {
				public HashMap<String, Object> handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("blockManId", rs.getInt("BLOCK_MAN_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
						map.put("workProperty", rs.getString("WORK_PROPERTY"));
						map.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
						map.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						map.put("planStatus", rs.getInt("PLAN_STATUS"));
						map.put("taskName", rs.getString("taskName"));
						map.put("collectGroupId", rs.getInt("collect_group_id"));
						map.put("dayEditGroupId", rs.getInt("day_edit_group_id"));
						map.put("monthEditGroupId", rs.getInt("month_edit_group_id"));
						map.put("collectPlanStartDate", rs.getString("collect_plan_start_date"));
						map.put("collectPlanEndDate", rs.getString("collect_plan_end_date"));
						map.put("dayEditPlanStartDate", rs.getString("day_edit_plan_start_date"));
						map.put("dayEditPlanEndDate", rs.getString("day_edit_plan_end_date"));
						map.put("monthEditPlanStartDate", rs.getString("month_edit_plan_start_date"));
						map.put("monthEditPlanEndDate", rs.getString("month_edit_plan_end_date"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("dayProducePlanStartDate", rs.getString("day_produce_plan_start_date"));
						map.put("dayProducePlanEndDate", rs.getString("day_produce_plan_end_date"));
						map.put("monthProducePlanStartDate", rs.getString("month_produce_plan_start_date"));
						map.put("monthProducePlanEndDate", rs.getString("month_produce_plan_end_date"));
						map.put("taskType", rs.getInt("task_type"));
						map.put("blockDescp", rs.getString("descp"));
						map.put("createUserName", rs.getString("USER_REAL_NAME"));
						return map;
					}
					return null;
				}

			};
			return run.query(conn, selectSql, rsHandler, bean.getBlockManId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
/*
	public Page listByGroupId(JSONObject json, int currentPageNum, int pageSize) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			String selectSql = null;
			int stage = json.getInt("stage");
			JSONObject conditionJson = json.getJSONObject("condition");
			JSONObject orderJson = json.getJSONObject("order");

			int groupId = json.getInt("groupId");
			if (0 == stage) {
				selectSql = "SELECT P.BLOCK_MAN_ID,P.BLOCK_MAN_NAME,P.STATUS,P.PLAN_START_DATE,P.PLAN_END_DATE,P.FLAG,P.TASK_TYPE FROM "
						+ " (SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.COLLECT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.COLLECT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,1 FLAG,T.COLLECT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,SUBTASK S,TASK TT   WHERE "
						+ "  T.BLOCK_MAN_ID=S.BLOCK_MAN_ID AND T.TASK_ID = TT.TASK_ID  AND T.LATEST = 1 AND S.STAGE =0  UNION ALL "
						+ "  SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.COLLECT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.COLLECT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,0 FLAG,T.COLLECT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,TASK TT WHERE T.TASK_ID = TT.TASK_ID   AND T.LATEST = 1 "
						+ "   AND T.STATUS=1 AND NOT EXISTS (SELECT su.subtask_id FROM subtask su WHERE su.block_man_id=T.BLOCK_MAN_ID)) P"
						+ " WHERE  P.COLLECT_GROUP_ID = " + groupId;

			}
			if (1 == stage) {

				selectSql = "SELECT P.BLOCK_MAN_ID,P.BLOCK_MAN_NAME,P.STATUS,P.PLAN_START_DATE,P.PLAN_END_DATE,P.FLAG,P.TASK_TYPE FROM "
						+ " (SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,1 FLAG,T.DAY_EDIT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,SUBTASK S,TASK TT   WHERE "
						+ "  T.BLOCK_MAN_ID=S.BLOCK_MAN_ID AND T.TASK_ID = TT.TASK_ID  AND T.LATEST = 1 AND S.STAGE =1  UNION ALL "
						+ "  SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,0 FLAG,T.DAY_EDIT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,TASK TT WHERE T.TASK_ID = TT.TASK_ID  AND T.LATEST = 1 "
						+ "   AND T.STATUS=1 AND NOT EXISTS (SELECT su.subtask_id FROM subtask su WHERE su.BLOCK_MAN_ID=T.BLOCK_MAN_ID)) P"
						+ " WHERE  P.DAY_EDIT_GROUP_ID = " + groupId;
			}

			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockManName".equals(key)) {
						selectSql += " and P.BLOCK_MAN_NAME like '%" + conditionJson.getString(key) + "%'";
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockManId".equals(key)) {
						selectSql += (" order by P.BLOCK_MAN_ID " + orderJson.getString(key));
						break;
					}
				}
			} else {
				selectSql += " order by P.BLOCK_MAN_ID";
			}
			return BlockOperation.queryBlockByGroup(conn, selectSql, stage, currentPageNum, pageSize);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
*/
	public List<Integer> close(List<Integer> blockManIdList, long userId) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			// 获取所有blockIdList中可以关闭的block
			List<Integer> blockReadyToClose = BlockOperation.getBlockListReadyToClose(conn, blockManIdList);

			if (!blockReadyToClose.isEmpty()) {
				BlockOperation.closeBlockByBlockIdList(conn, blockReadyToClose);
			}

			List<Integer> unClosedBlocks = new ArrayList<Integer>();
			for (int i = 0; i < blockManIdList.size(); i++) {
				if (!blockReadyToClose.contains(blockManIdList.get(i))) {
					unClosedBlocks.add(blockManIdList.get(i));
				}
			}
			//发送消息
			try {
				//查询blockMan数据
				List<Map<String, Object>> blockManList = this.getBlockManByBlockManId(conn, blockReadyToClose);
				/*block创建/编辑/关闭
				1.分配的采集作业组组长
				2.分配的日编作业组组长
				block关闭：XXX(block名称)已关闭，请关注*/
				List<Object[]> msgContentList=new ArrayList<Object[]>();
				String msgTitle="block关闭";
				for (Map<String, Object> blockMan : blockManList) {
					String collectGroupLeader=(String) blockMan.get("collectGroupLeader");
					String dayEditGroupLeader=(String) blockMan.get("dayEditGroupLeader");
					if(collectGroupLeader!=null && !collectGroupLeader.isEmpty()){
						Object[] msgTmp=new Object[4];
						msgTmp[0]=collectGroupLeader;
						msgTmp[1]=msgTitle;
						msgTmp[2]="block关闭:"+blockMan.get("blockManName")+"已关闭,请关注";
						//关联要素
						JSONObject msgParam = new JSONObject();
						msgParam.put("relateObject", "BLOCK_MAN");
						msgParam.put("relateObjectId", blockMan.get("blockManId"));
						msgTmp[3]=msgParam.toString();
						msgContentList.add(msgTmp);
					}
					if(dayEditGroupLeader!=null && !dayEditGroupLeader.isEmpty()){
						Object[] msgTmp=new Object[4];
						msgTmp[0]=dayEditGroupLeader;
						msgTmp[1]=msgTitle;
						msgTmp[2]="block关闭:"+blockMan.get("blockManName")+"已关闭,请关注";
						//关联要素
						JSONObject msgParam = new JSONObject();
						msgParam.put("relateObject", "BLOCK_MAN");
						msgParam.put("relateObjectId", blockMan.get("blockManId"));
						msgTmp[3]=msgParam.toString();
						msgContentList.add(msgTmp);
					}
					
				}
				if(msgContentList.size()>0){
					blockPushMsgByMsg(conn,msgContentList,userId);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("block关闭消息发送失败,原因:"+e.getMessage(), e);
			}
			
			return unClosedBlocks;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("block关闭失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
/*
	public Page listAllBak(JSONObject enterParam, String listType, JSONObject conditionJson, JSONObject orderJson,
			int currentPageNum, int pageSize) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select distinct m.BLOCK_ID,m.DESCP,m.COLLECT_GROUP_ID,u.GROUP_NAME COLLECT_GROUP,"
					+ "m.DAY_EDIT_GROUP_ID,(select distinct group_name from user_group"
					+ "  where group_id = m.DAY_EDIT_GROUP_ID) DAY_EDIT_GROUP, m.MONTH_EDIT_GROUP_ID,(select distinct group_name"
					+ "  from user_group where group_id = m.MONTH_EDIT_GROUP_ID) MONTH_EDIT_GROUP,"
					+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyymmdd') COLLECT_PLAN_START_DATE,"
					+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyymmdd') COLLECT_PLAN_END_DATE,"
					+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') DAY_EDIT_PLAN_START_DATE,"
					+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') DAY_EDIT_PLAN_END_DATE,"
					+ " to_char(m.MONTH_EDIT_PLAN_START_DATE, 'yyyymmdd') MONTH_EDIT_PLAN_START_DATE,"
					+ " to_char(m.MONTH_EDIT_PLAN_END_DATE, 'yyyymmdd') MONTH_EDIT_PLAN_END_DATE,"
					+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_START_DATE,"
					+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_END_DATE,"
					+ " to_char(m.MONTH_PRODUCE_PLAN_START_DATE, 'yyyymmdd') MONTH_PRODUCE_PLAN_START_DATE,"
					+ " to_char(m.MONTH_PRODUCE_PLAN_END_DATE, 'yyyymmdd') MONTH_PRODUCE_PLAN_END_DATE,"
					+ " t.BLOCK_NAME," + " nvl(u.user_real_name, '') USER_REAL_NAME," + " m.STATUS," + " k.TASK_ID,"
					+ " k.NAME," + " to_char(k.PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,"
					+ " to_char(k.PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,"
					+ " to_char(k.MONTH_EDIT_PLAN_START_DATE, 'yyyymmdd') TASK_START_DATE,"
					+ " to_char(k.MONTH_EDIT_PLAN_END_DATE, 'yyyymmdd') TASK_END_DATE"
					+ " from block_man m, block t, user_info u, task k, user_group u"
					+ " where m.block_id = t.block_id(+) and m.latest = 1 and m.create_user_id = u.user_id(+)"
					+ " and t.city_id = k.city_id(+)" + " and k.latest = 1" + "and m.collect_group_id = u.group_id(+)";
			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockId".equals(key)) {
						selectSql += " and t.block_id=" + conditionJson.getInt(key);
					}
					if ("createUserName".equals(key)) {
						selectSql += " and u.USER_REAL_NAME like '%" + conditionJson.getString(key) + "%'";
					}
					if ("blockName".equals(key)) {
						selectSql += " and t.block_name like '%" + conditionJson.getString(key) + "%'";
					}
					if ("status".equals(key)) {
						String status = ((conditionJson.getJSONArray(key).toString()).replace('[', '(')).replace(']',
								')');
						selectSql += " and m.status in " + status;
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("collectPlanStartDate".equals(key)) {
						selectSql += (" order by t.COLLECT_PLAN_START_DATE "
								+ orderJson.getString("collectPlanStartDate"));
						break;
					}
					if ("collectPlanEndDate".equals(key)) {
						selectSql += (" order by t.COLLECT_PLAN_END_DATE " + orderJson.getString("collectPlanEndDate"));
						break;
					}
					if ("blockId".equals(key)) {
						selectSql += (" order by m.block_id " + orderJson.getString("blockId"));
						break;
					}
				}
			} else {
				selectSql += " order by m.block_id";
			}
			return BlockOperation.selectBlockList(conn, selectSql, null, currentPageNum, pageSize);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	/*
	public List listAll(JSONObject enterParam, String listType, JSONObject conditionJson, JSONObject orderJson)
			throws Exception {
		Connection conn = null;
		String selectSql = "";
		String selectNoPlanSqlByCityId = "";// 未规划城市下的block查询
		try {
			conn = DBConnector.getInstance().getManConnection();

			int cityId = 0;
			String inforId = "";
			int taskType = enterParam.getInt("taskType");
			if (4 == taskType) {
				inforId = enterParam.getString("inforId");
			} else {
				cityId = enterParam.getInt("cityId");
			}
			if (listType == null || "snapshot".equals(listType)) {
				if (!inforId.isEmpty()) {
					selectSql = "WITH T AS (SELECT m.block_id,t.block_name,m.status blockStatus,i.plan_status FROM BLOCK t,Block_Man m,infor i WHERE i.infor_id='"
							+ inforId + "' AND i.task_id=m.task_id AND t.block_id=m.block_id AND m.latest = 1) "
							+ "SELECT block_id,block_name,blockStatus,plan_status from T WHERE 1=1";
				} else {
					selectSql = " WITH T AS (SELECT t.block_id,t.block_name,m.status blockStatus,t.plan_status FROM BLOCK t,Block_Man m"
							+ " WHERE t.block_id=m.block_id  AND m.latest = 1 AND t.city_id=" + cityId
							+ " UNION ALL  SELECT t.block_id,t.block_name,0 blockStatus,t.plan_status "
							+ "FROM BLOCK t WHERE t.plan_status=0 AND t.city_id=" + cityId
							+ " ) SELECT block_id,block_name,blockStatus,plan_status from T  WHERE 1=1";
				}

			} else {
				if (!inforId.isEmpty()) {
					selectSql = "WITH T AS (select distinct t.block_id,t.block_name,m.status blockStatus,t.plan_status,m.DESCP, nvl(u.user_real_name, '') USER_REAL_NAME,"
							+ " m.COLLECT_GROUP_ID,u.GROUP_NAME COLLECT_GROUP, "
							+ " m.DAY_EDIT_GROUP_ID,(select distinct group_name from user_group  "
							+ " where group_id = m.DAY_EDIT_GROUP_ID) DAY_EDIT_GROUP, "
							+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyymmdd') COLLECT_PLAN_START_DATE,  "
							+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyymmdd') COLLECT_PLAN_END_DATE,  "
							+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') DAY_EDIT_PLAN_START_DATE,  "
							+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') DAY_EDIT_PLAN_END_DATE,  "
							+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_START_DATE,  "
							+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_END_DATE,  "
							+ " k.TASK_ID, k.NAME, k.task_type, "
							+ " to_char(k.PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,  "
							+ " to_char(k.PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE from block_man m, block t, user_info u, task k, user_group u,infor i where i.infor_id='"
							+ inforId
							+ "' AND m.block_id = t.block_id(+) and m.latest = 1 and m.create_user_id = u.user_id(+)  "
							+ " and i.task_id = m.task_id AND m.task_id=k.task_id and k.latest = 1 and m.collect_group_id = u.group_id(+) ) "
							+ " SELECT block_id,block_name,blockStatus,plan_status,DESCP,USER_REAL_NAME,COLLECT_GROUP_ID,COLLECT_GROUP,"
							+ " DAY_EDIT_GROUP_ID,DAY_EDIT_GROUP,COLLECT_PLAN_START_DATE,COLLECT_PLAN_END_DATE,DAY_EDIT_PLAN_START_DATE, "
							+ " DAY_EDIT_PLAN_END_DATE,DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,TASK_ID,NAME, task_type,"
							+ " PLAN_START_DATE,PLAN_END_DATE from T  WHERE 1=1 ";
				} else {
					selectSql = "WITH T AS (select distinct t.block_id,t.block_name,m.status blockStatus,t.plan_status,m.DESCP, nvl(u.user_real_name, '') USER_REAL_NAME,"
							+ " m.COLLECT_GROUP_ID,u.GROUP_NAME COLLECT_GROUP, "
							+ " m.DAY_EDIT_GROUP_ID,(select distinct group_name from user_group "
							+ " where group_id = m.DAY_EDIT_GROUP_ID) DAY_EDIT_GROUP,"
							+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyymmdd') COLLECT_PLAN_START_DATE, "
							+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyymmdd') COLLECT_PLAN_END_DATE, "
							+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') DAY_EDIT_PLAN_START_DATE, "
							+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') DAY_EDIT_PLAN_END_DATE, "
							+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_START_DATE, "
							+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_END_DATE, "
							+ " k.TASK_ID, k.NAME, k.task_type,"
							+ " to_char(k.PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE, "
							+ " to_char(k.PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE"
							+ " from block_man m, block t, user_info u, task k, user_group u"
							+ " where m.block_id = t.block_id and m.latest = 1 and m.create_user_id = u.user_id(+)"
							+ " and t.city_id = k.city_id and k.latest = 1 and m.collect_group_id = u.group_id(+)"
							+ " AND t.city_id=" + cityId
							+ " UNION ALL SELECT t.block_id,t.block_name, 0 blockStatus,t.plan_status,'---' DESCP,'---' USER_REAL_NAME,0 COLLECT_GROUP_ID,'---' COLLECT_GROUP,"
							+ " 0 DAY_EDIT_GROUP_ID,'---' DAY_EDIT_GROUP,'---' COLLECT_PLAN_START_DATE,'---' COLLECT_PLAN_END_DATE,'---' DAY_EDIT_PLAN_START_DATE, "
							+ " '---' DAY_EDIT_PLAN_END_DATE,'---' DAY_PRODUCE_PLAN_START_DATE,'---' DAY_PRODUCE_PLAN_END_DATE,0 TASK_ID,'---' NAME, 0 task_type,"
							+ " '---' PLAN_START_DATE,'---' PLAN_END_DATE FROM BLOCK t WHERE t.plan_status=0 AND t.city_id="
							+ cityId
							+ ") SELECT block_id,block_name,blockStatus,plan_status,DESCP,USER_REAL_NAME,COLLECT_GROUP_ID,COLLECT_GROUP,"
							+ " DAY_EDIT_GROUP_ID,DAY_EDIT_GROUP,COLLECT_PLAN_START_DATE,COLLECT_PLAN_END_DATE,DAY_EDIT_PLAN_START_DATE, "
							+ " DAY_EDIT_PLAN_END_DATE,DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,TASK_ID,NAME, task_type,"
							+ " PLAN_START_DATE,PLAN_END_DATE from T  WHERE 1=1 ";
				}

			}

			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockId".equals(key)) {
						selectSql += " and T.block_id=" + conditionJson.getInt(key);
					}
					if ("createUserName".equals(key)) {
						if (listType != null && "integrate".equals(listType)) {
							selectSql += " and T.USER_REAL_NAME like '%" + conditionJson.getString(key) + "%'";
						}
					}
					if ("blockName".equals(key)) {
						selectSql += " and T.block_name like '%" + conditionJson.getString(key) + "%'";
					}
					if ("blockStatus".equals(key)) {
						String blockStatus = ((conditionJson.getJSONArray(key).toString()).replace('[', '('))
								.replace(']', ')');
						selectSql += " and T.blockStatus in " + blockStatus;
					}
					if ("taskName".equals(key)) {
						if (listType != null && "integrate".equals(listType)) {
							selectSql += " and T.NAME like '%" + conditionJson.getString(key) + "%'";
						}
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockName".equals(key)) {
						selectSql += (" order by T.block_name " + orderJson.getString("blockName"));
						break;
					}
					if ("planStatus".equals(key)) {
						selectSql += (" order by T.plan_status " + orderJson.getString("planStatus"));
						break;
					}
					if ("blockId".equals(key)) {
						selectSql += (" order by T.block_id " + orderJson.getString("blockId"));
						break;
					}
				}
			} else {
				selectSql += " order by T.block_id";
			}
			return BlockOperation.selectAllBlock(conn, selectSql, listType);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/

	/*public List<HashMap> listByInfoId(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			String inforId = json.getString("inforId");

			String selectSql = "select distinct i.block_id,t.block_name,t.city_id,b.status,k.name,to_char(k.plan_start_date, 'yyyymmdd') plan_start_date,"
					+ "to_char(k.plan_end_date, 'yyyymmdd') plan_end_date,"
					+ " to_char(k.month_edit_plan_start_date, 'yyyymmdd') month_edit_plan_start_date,"
					+ "to_char(k.month_edit_plan_end_date, 'yyyymmdd') month_edit_plan_end_date "
					+ " from infor_block_mapping i, block t, task k,block_man b where  i.block_id = t.block_id and i.block_id=b.block_id"
					+ " and t.city_id = k.city_id and k.latest = 1 and i.infor_id ='" + inforId + "'";
			String selectSqlNotOpen = "select t.block_id,t.block_name,t.city_id,0 status from infor_block_mapping i,block t where t.plan_status=0 and"
					+ " i.block_id=t.block_id and not exists （select 1 from block_man b where b.block_id=t.block_id）and  i.infor_id ='"
					+ inforId + "'";

			return BlockOperation.QuerylistByInfoId(conn, selectSql, selectSqlNotOpen);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	/**
	 * 查询blockMan数据
	 * @author Han Shaoming
	 * @param conn
	 * @param blockManIds
	 * @return
	 * @throws Exception
	 */
	private List<Map<String,Object>> getBlockManByBlockManId(Connection conn,List blockManIds) throws Exception{
		try {
			if (blockManIds.size()==0){
				return null;
			}
			String BlockIds = "(";
			BlockIds += StringUtils.join(blockManIds.toArray(), ",") + ")";
			String selectSql = "select DISTINCT m.block_man_id,m.block_man_name,(SELECT u.leader_id FROM User_Group u "
					+ "WHERE u.group_id=m.collect_group_id) collectGroupLeader,(SELECT u.leader_id FROM User_Group u "
					+ "WHERE u.group_id=m.day_edit_group_id) dayEditGroupLeader from block_man m WHERE m.block_man_id in "
					+ BlockIds;
			//System.out.println(selectSql);
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(selectSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
			ResultSet rs = stmt.executeQuery();
			List<Map<String,Object>> blockManList = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("blockManId", rs.getLong("BLOCK_MAN_ID"));
				map.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
				map.put("collectGroupLeader", rs.getString("COLLECTGROUPLEADER"));
				map.put("dayEditGroupLeader", rs.getString("DAYEDITGROUPLEADER"));
				blockManList.add(map);
			}
			return blockManList;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/*
	private String blockPushMsgByConn(Connection conn,List blockManIds, long userId) throws Exception {
		try {
			if (blockManIds.size()==0){
				return "";
			}
			String BlockIds = "(";
			BlockIds += StringUtils.join(blockManIds.toArray(), ",") + ")";
			String selectSql = "select DISTINCT m.block_man_id,m.block_man_name,(SELECT u.leader_id FROM User_Group u "
					+ "WHERE u.group_id=m.collect_group_id) collectGroupLeader,(SELECT u.leader_id FROM User_Group u "
					+ "WHERE u.group_id=m.day_edit_group_id) dayEditGroupLeader from block_man m WHERE m.block_man_id in "
					+ BlockIds;
			//System.out.println(selectSql);
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(selectSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
			ResultSet rs = stmt.executeQuery();
			block创建/编辑/关闭
			1.分配的采集作业组组长
			2.分配的日编作业组组长
			block:XXX(block名称)内容发生变更，请关注
			List<Object[]> msgContentList=new ArrayList<Object[]>();
			String msgTitle="新增block";
			while (rs.next()) {
				String collectGroupLeader=rs.getString("COLLECTGROUPLEADER");
				String dayEditGroupLeader=rs.getString("DAYEDITGROUPLEADER");
				if(collectGroupLeader!=null && !collectGroupLeader.isEmpty()){
					Object[] msgTmp=new Object[3];
					msgTmp[0]=collectGroupLeader;
					msgTmp[1]=msgTitle;
					msgTmp[2]="新增block:"+rs.getString("BLOCK_MAN_NAME")+",请关注";
					msgContentList.add(msgTmp);
				}
				if(dayEditGroupLeader!=null && !dayEditGroupLeader.isEmpty()){
					Object[] msgTmp=new Object[3];
					msgTmp[0]=dayEditGroupLeader;
					msgTmp[1]=msgTitle;
					msgTmp[2]="新增block:"+rs.getString("BLOCK_MAN_NAME")+",请关注";
					msgContentList.add(msgTmp);
				}
			}
			if(msgContentList.size()>0){
				blockPushMsgByMsg(conn,msgContentList,userId);
				BlockOperation.updateMainBlock(conn, blockManIds);
			}	

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("发布失败，原因为:" + e.getMessage(), e);
		}
		return "发布成功";

	}
	*/
	
	public String blockPushMsg(List blockManIds, long userId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			//查询blockMan数据
			List<Map<String, Object>> blockManList = this.getBlockManByBlockManId(conn, blockManIds);
			/*block创建/编辑/关闭
			1.分配的采集作业组组长
			2.分配的日编作业组组长
			block:XXX(block名称)，请关注*/
			List<Object[]> msgContentList=new ArrayList<Object[]>();
			String msgTitle="block发布";
			for (Map<String, Object> blockMan : blockManList) {
				String collectGroupLeader=(String) blockMan.get("collectGroupLeader");
				String dayEditGroupLeader=(String) blockMan.get("dayEditGroupLeader");
				if(collectGroupLeader!=null && !collectGroupLeader.isEmpty()){
					Object[] msgTmp=new Object[4];
					msgTmp[0]=collectGroupLeader;
					msgTmp[1]=msgTitle;
					msgTmp[2]="新增block:"+blockMan.get("blockManName")+",请关注";
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "BLOCK_MAN");
					msgParam.put("relateObjectId", blockMan.get("blockManId"));
					msgTmp[3]=msgParam.toString();
					msgContentList.add(msgTmp);
				}
				if(dayEditGroupLeader!=null && !dayEditGroupLeader.isEmpty()){
					Object[] msgTmp=new Object[4];
					msgTmp[0]=dayEditGroupLeader;
					msgTmp[1]=msgTitle;
					msgTmp[2]="新增block:"+blockMan.get("blockManName")+",请关注";
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "BLOCK_MAN");
					msgParam.put("relateObjectId", blockMan.get("blockManId"));
					msgTmp[3]=msgParam.toString();
					msgContentList.add(msgTmp);
				}
				if(msgContentList.size()>0){
					blockPushMsgByMsg(conn,msgContentList,userId);	}			
			}
			BlockOperation.updateMainBlock(conn, blockManIds);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("新增block消息发送失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		return "BLOCK批量发布"+blockManIds.size()+"个成功，0个失败";

	}

	private void blockPushMsgByMsg(Connection conn,	List<Object[]> msgContentList, long userId) throws Exception {
		//String userSql="SELECT DISTINCT M.USER_ID FROM ROLE_USER_MAPPING M WHERE M.ROLE_ID IN (4, 5)";
		//List<Integer> userIdList=UserInfoOperation.getUserListBySql(conn, userSql);
		Object[][] msgList=new Object[msgContentList.size()][3];
		int num=0;
		for(Object[] msgContent:msgContentList){
			msgList[num]=msgContent;
			num+=1;
			//发送邮件
			String toMail = null;
			String mailTitle = null;
			String mailContent = null;
			//查询用户详情
			Map<String, Object> userInfo = UserInfoOperation.getUserInfoByUserId(conn, Long.parseLong((String) msgContent[0]));
			if(userInfo != null && userInfo.get("userEmail") != null){
				//判断邮箱格式
				String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher((CharSequence) userInfo.get("userEmail"));
                if(matcher.matches()){
                	toMail = (String) userInfo.get("userEmail");
                	mailTitle = (String) msgContent[1];
                	mailContent = (String) msgContent[2];
                	//发送邮件到消息队列
                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
                }
			}
			//查询用户名称
			String pushUserName = null;
			if(userInfo != null && userInfo.size() > 0){
				pushUserName = (String) userInfo.get("userRealName");
			}
			//发送消息到消息队列
			SysMsgPublisher.publishMsg((String)msgContent[1], (String)msgContent[2], userId, new long[]{Long.parseLong((String) msgContent[0])}, 2, (String)msgContent[3], pushUserName);
		}
	}

	public Page list(int stage, JSONObject condition, JSONObject order, int currentPageNum,int pageSize, int snapshot) throws Exception {
		// TODO Auto-generated method stub
		
		Connection conn = null;		
		try {
			conn = DBConnector.getInstance().getManConnection();
			//返回部分字段,采集/日编角色返回，仅返回采集/日编的内容
			if(snapshot==1){
				return this.listBySnapshot(conn,stage, condition, order, currentPageNum, pageSize);
				}
			else{//生管角色登陆用：返回采集，日编任务的全部内容
				return this.listByAll(conn,condition, order, currentPageNum, pageSize);
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private Page listByAll(Connection conn, JSONObject conditionJson,
			JSONObject order, int currentPageNum, int pageSize) throws Exception {
		String conditionSql="";
		String statusSql="";
		if(null!=conditionJson && !conditionJson.isEmpty()){
			Iterator keys = conditionJson.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if("blockManName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_MAN_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("blockId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_ID ="+conditionJson.getInt(key);}
				if("name".equals(key)){
					conditionSql=conditionSql+" AND (MAN_LIST.BLOCK_MAN_NAME LIKE '%"+conditionJson.getString(key)+"%'"
							+ " or MAN_LIST.BLOCK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
				if("taskId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.TASK_ID ="+conditionJson.getInt(key);}
				if("cityId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.CITY_ID ="+conditionJson.getInt(key);}
				if("blockManId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_MAN_ID ="+conditionJson.getInt(key);}
				if("createUserName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.CREATE_USER_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("taskName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				/*if("blockPlanStatus".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_PLAN_STATUS ="+conditionJson.getInt(key);}
				*/
				//1-15采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,未规划,草稿,已完成,已关闭,按时完成,提前完成,逾期完成,采集逾期,日编逾期
				if("selectParam1".equals(key)){
					JSONArray selectParam1=conditionJson.getJSONArray(key);
					JSONArray collectProgress=new JSONArray();
					JSONArray dailyProgress=new JSONArray();
					JSONArray planStatus=new JSONArray();
					for(Object i:selectParam1){
						int tmp=(int) i;
						if(tmp==1||tmp==2||tmp==3){collectProgress.add(tmp);}
						if(tmp==4||tmp==5||tmp==6){dailyProgress.add(tmp-3);}
						if(tmp==7){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.BLOCK_PLAN_STATUS =0";}
						if(tmp==8){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.BLOCK_STATUS =2";}
						if(tmp==9||tmp==10){planStatus.add(tmp-6);}
												
						if(tmp==11){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date=0";
						}
						if(tmp==12){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date>0";
						}
						if(tmp==13){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date<0";
						}
						if(tmp==14){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.COLLECT_DIFF_DATE<0";
						}
						if(tmp==15){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.daily_DIFF_DATE<0";
						}
					}
					if(!collectProgress.isEmpty()){
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" MAN_LIST.collect_Progress IN ("+collectProgress.join(",")+")";}
					if(!dailyProgress.isEmpty()){
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" MAN_LIST.daily_Progress IN ("+dailyProgress.join(",")+")";}
					if(!planStatus.isEmpty()){
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" MAN_LIST.PLAN_STATUS IN ("+planStatus.join(",")+")";}
				}
				if("collectProgress".equals(key)){
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.collect_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
				if("dailyProgress".equals(key)){
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.daily_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
				if("blockStatus".equals(key)){
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.BLOCK_STATUS ="+conditionJson.getInt(key);}
				if("blockPlanStatus".equals(key)){
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.BLOCK_PLAN_STATUS ="+conditionJson.getInt(key);}
				if("planStatus".equals(key)){
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.PLAN_STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")";}
			}
		}	
		if(!statusSql.isEmpty()){//有非status
			conditionSql+=" and ("+statusSql+")";}
		
		long pageStartNum = (currentPageNum - 1) * pageSize + 1;
		long pageEndNum = currentPageNum * pageSize;
		String selectSql = "";
		String selectPart="";
		String wherePart="";
		selectSql="WITH BLOCK_PLAN AS"
				//block已完成
				+ " (SELECT BM.BLOCK_MAN_ID, 3 PLAN_STATUS"
				+ "    FROM SUBTASK T, BLOCK_MAN BM"
				+ "   WHERE T.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
				+ "     AND NOT EXISTS (SELECT 1"
				+ "            FROM SUBTASK BMM"
				+ "           WHERE BMM.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
				+ "             AND BMM.STATUS <> 0)"
				+ "  UNION ALL"
				//block作业中
				+ "  SELECT T.BLOCK_MAN_ID, 2 PLAN_STATUS"
				+ "    FROM BLOCK_MAN T"
				+ "   WHERE NOT EXISTS"
				+ "   (SELECT 1 FROM SUBTASK BMM WHERE BMM.BLOCK_MAN_ID = T.BLOCK_MAN_ID)"
				+ "  UNION ALL"
				//block作业中
				+ "  SELECT T.BLOCK_MAN_ID, 2 PLAN_STATUS"
				+ "    FROM BLOCK_MAN T, SUBTASK BM"
				+ "   WHERE T.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
				+ "     AND EXISTS (SELECT 1"
				+ "            FROM SUBTASK BMM"
				+ "           WHERE BMM.BLOCK_MAN_ID = T.BLOCK_MAN_ID"
				+ "             AND BMM.STATUS <> 0)),"
				+ " MAN_LIST AS"
				+ " (SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.DESCP BLOCK_DESCP,"
				+ "                  T.TASK_ID,"
				+ "                  B.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"
				+ "                  nvl(B.work_property,'---') work_property,"
				+ "                  TT.CITY_ID,"
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				/*
				 * 记录默认排序原则：
				 * ①根据状态排序：开启>草稿>未规划>100%(已完成)>已关闭 
				 *    用order_status来表示这个排序的先后顺序。分别是开启0>草稿1>未规划2>100%(已完成)3>已关闭4
				 * ②相同状态中根据剩余工期排序，逾期>0天>剩余/提前
				 * ③开启状态相同剩余工期，根据完成度排序，完成度高>完成度低；其它状态，根据名称
				 */
				+ "                  CASE T.STATUS"
				+ "                      WHEN 1 THEN CASE TP.PLAN_STATUS WHEN 2 THEN 0"
                + "                       when 3 then 3 end "
                + "                         when 2 then 1"
                + "                           when 0 then 4 end order_status,"
                + "                  CASE T.STATUS"
				+ "                      WHEN 1 THEN CASE TP.PLAN_STATUS WHEN 2 THEN 2"
                + "                       when 3 then 3 end "
                + "                         when 2 then 1"
                + "                           when 0 then 4 end plan_status,"
				+ "                  S.PERCENT,"
				+ "                  S.DIFF_DATE,"
				+ "                  TO_CHAR(T.COLLECT_PLAN_START_DATE, 'YYYYMMDD') COLLECT_PLAN_START_DATE,"
				+ "                  TO_CHAR(T.COLLECT_PLAN_END_DATE, 'YYYYMMDD') COLLECT_PLAN_END_DATE,"
				+ "                  T.COLLECT_GROUP_ID,"
				+ "                  S.COLLECT_PERCENT,"
				+ "                  S.COLLECT_DIFF_DATE,"
				+ "                  S.COLLECT_PROGRESS,"
				/*+ "                  CASE NVL(ST.STAGE, 999)"
				+ "                    WHEN 0 THEN 1"
				+ "                    ELSE 0 END COLLECT_ASSIGN_STATUS,"*/
				+ "                  GC.GROUP_NAME COLLECT_GROUP_NAME,"
				+ "                  TO_CHAR(T.DAY_EDIT_PLAN_START_DATE, 'YYYYMMDD') DAY_EDIT_PLAN_START_DATE,"
				+ "                  TO_CHAR(T.DAY_EDIT_PLAN_END_DATE, 'YYYYMMDD') DAY_EDIT_PLAN_END_DATE,"
				+ "                  T.DAY_EDIT_GROUP_ID,"
				+ "                  S.DAILY_PERCENT,"
				+ "                  S.DAILY_DIFF_DATE,"
				+ "                  S.DAILY_PROGRESS,"
				/*+ "                  CASE NVL(ST.STAGE, 999)"
				+ "                    WHEN 1 THEN 1"
				+ "                    ELSE 0 END DAILY_ASSIGN_STATUS,"*/
				+ "                  GE.GROUP_NAME DAY_EDIT_GROUP_NAME,"
				+ "                  TT.TASK_TYPE,"
				+ "                  I.USER_REAL_NAME CREATE_USER_NAME,"
				+ "                  T.CREATE_USER_ID"
				+ "    FROM BLOCK                     B,"
				+ "         BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                GC,"
				+ "         USER_GROUP                GE,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				//+ "         SUBTASK                   ST,"
				+ "         USER_INFO I,"
				+ "         BLOCK_PLAN                 TP"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.CREATE_USER_ID=I.USER_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND B.BLOCK_ID = T.BLOCK_ID"
				+ "     AND T.COLLECT_GROUP_ID = GC.GROUP_ID(+)"
				+ "     AND T.DAY_EDIT_GROUP_ID = GE.GROUP_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = TP.BLOCK_MAN_ID(+)"
				//+ "     AND T.BLOCK_MAN_ID = ST.BLOCK_MAN_ID(+)"
				+ "     AND TT.LATEST = 1"
				+ "  UNION ALL"
				+ "  SELECT DISTINCT 0,"
				+ "                  '---',"
				+ "                  '---',"
				+ "                  0,"
				+ "                  B.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"
				+ "                  nvl(B.work_property,'---') work_property,"
				+ "                  C.CITY_ID,"
				+ "                  0 STATUS,"
				+ "                  B.PLAN_STATUS block_plan_status,"
				+ "                  2 order_status,"
				+ "                  1 plan_status,"
				+ "                  0,"
				+ "                  0,"
				+ "                  '---' COLLECT_PLAN_START_DATE,"
				+ "                  '---' COLLECT_PLAN_END_DATE,"
				+ "                  0 COLLECT_GROUP_ID,"
				+ "                  0 COLLECT_PERCENT,"
				+ "                  0 COLLECT_DIFF_DATE,"
				+ "                  0 COLLECT_PROGRESS,"
				//+ "                  0 COLLECT_ASSIGN_STATUS,"
				+ "                  '---' COLLECT_GROUP_NAME,"
				+ "                  '---' DAY_EDIT_PLAN_START_DATE,"
				+ "                  '---' DAY_EDIT_PLAN_END_DATE,"
				+ "                  0 DAY_EDIT_GROUP_ID,"
				+ "                  0 DAILY_PERCENT,"
				+ "                  0 DAILY_DIFF_DATE,"
				+ "                  0 DAILY_PROGRESS,"
				//+ "                  0 DAILY_ASSIGN_STATUS,"
				+ "                  '---' DAY_EDIT_GROUP_NAME,"
				+ "                  1 TASK_TYPE,"
				+ "                  '---',"
				+ "                  0"
				+ "    FROM BLOCK B, CITY C"
				+ "   WHERE B.CITY_ID = C.CITY_ID"
				+ "     AND B.PLAN_STATUS = 0"
				+ "     AND C.CITY_ID<>100002),"
				+ " FINAL_TABLE AS"
				+ " (SELECT *"
				+ "    FROM MAN_LIST"
				+ "    WHERE 1=1"
				+ conditionSql+""
				+ " order by man_list.order_status asc,man_list.diff_date desc,man_list.percent desc,man_list.block_man_name,man_list.block_name)"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
				+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
		return BlockOperation.getAllQuery(conn, selectSql,currentPageNum,pageSize);
	}

	public Page listBySnapshot(Connection conn,int stage, JSONObject conditionJson, JSONObject order, int currentPageNum,int pageSize) throws Exception {
		
		String conditionSql="";
		String statusSql="";
		if(null!=conditionJson && !conditionJson.isEmpty()){
			Iterator keys = conditionJson.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if("blockManName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_MAN_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("blockId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_ID ="+conditionJson.getInt(key);}
				if("name".equals(key)){
					conditionSql=conditionSql+" AND (MAN_LIST.BLOCK_MAN_NAME LIKE '%"+conditionJson.getString(key)+"%' "
							+ "or MAN_LIST.BLOCK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
				if("groupId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.GROUP_ID ="+conditionJson.getInt(key);}
				if("planStatus".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_STATUS =1 AND MAN_LIST.PLAN_STATUS="+conditionJson.getInt(key);}
				
				//1-6采集/日编正常,采集/日编异常,待分配,正常完成,逾期完成,提前完成
				if("selectParam1".equals(key)){
					JSONArray selectParam1=conditionJson.getJSONArray(key);
					JSONArray progress=new JSONArray();
					for(Object i:selectParam1){
						int tmp=(int) i;
						if(tmp==1||tmp==2){progress.add(tmp);}		
						if(tmp==3){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.ASSIGN_STATUS=0";
						}
						if(tmp==4){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date=0";
						}
						if(tmp==6){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date>0";
						}
						if(tmp==5){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date<0";
						}
					}
					//进展正常/异常 必须是已分配的（ASSIGN_STATUS=1）
					if(!progress.isEmpty()){
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" (MAN_LIST.Progress IN ("+progress.join(",")+") AND MAN_LIST.ASSIGN_STATUS=1)";}
				}
				
				if ("assignStatus".equals(key)) {
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.ASSIGN_STATUS="+conditionJson.getInt(key);}
				if ("progress".equals(key)) {
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
				if ("diffDate".equals(key)) {
					JSONArray diffDateArray=conditionJson.getJSONArray(key);
					for(Object diffDate:diffDateArray){
						if((int) diffDate==1){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date>0";
						}
						if((int) diffDate==0){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date=0";
						}
						if((int) diffDate==-1){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date<0";
						}
						}
					}
			}
		}	
		if(!statusSql.isEmpty()){//有非status
			conditionSql+=" and ("+statusSql+")";}
		
		long pageStartNum = (currentPageNum - 1) * pageSize + 1;
		long pageEndNum = currentPageNum * pageSize;
		String selectSql = "";
		String selectPart="";
		String wherePart="";
		String stagePart="";
		if(stage==0){
			selectPart="                  TO_CHAR(T.COLLECT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
					+ "                  TO_CHAR(T.COLLECT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
					+ "                  T.COLLECT_GROUP_ID GROUP_ID,"
					+ "                  NVL(S.COLLECT_PERCENT,0) PERCENT,"
					+ "                  NVL(S.COLLECT_DIFF_DATE,0) DIFF_DATE,"
					+ "                  NVL(S.COLLECT_PROGRESS,1) progress,"
					/*+ "                  CASE NVL(ST.STAGE, 999)"
					+ "                    WHEN 0 THEN 1"
					+ "                    ELSE 0 END ASSIGN_STATUS,"*/
					+ "                  G.GROUP_NAME,";
			wherePart="     AND T.COLLECT_GROUP_ID = G.GROUP_ID(+)";
			stagePart="STAGE=0";
		}
		if(stage==1){
			selectPart="                  TO_CHAR(T.DAY_EDIT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
					+ "                  TO_CHAR(T.DAY_EDIT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
					+ "                  T.DAY_EDIT_GROUP_ID GROUP_ID,"
					+ "                  NVL(S.DAILY_PERCENT,0) PERCENT,"
					+ "                  NVL(S.DAILY_DIFF_DATE,0) DIFF_DATE,"
					+ "                  NVL(S.DAILY_PROGRESS,1) progress,"
					/*+ "                  CASE NVL(ST.STAGE, 999)"
					+ "                    WHEN 1 THEN 1"
					+ "                    ELSE 0 END ASSIGN_STATUS,"*/
					+ "                  G.GROUP_NAME,";
			wherePart="     AND T.DAY_EDIT_GROUP_ID = G.GROUP_ID(+)";
			stagePart="STAGE=1";
		}
		selectSql="WITH MAN_LIST AS"
				//未分配子任务
				+ " (SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"	
				+ "                  T.TASK_ID,"
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  0 ASSIGN_STATUS,"
				+ "                  2 PLAN_STATUS,"
				+selectPart					
				+ "                  TT.TASK_TYPE"
				+ "    FROM BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                G,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				+ "         BLOCK                   B"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.BLOCK_ID=B.BLOCK_ID"
				+ "     AND T.LATEST = 1"
				//+ "     AND T.STATUS=1"
				+ "     AND (EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT."+stagePart
						+ " GROUP BY STT.BLOCK_MAN_ID HAVING SUM(DISTINCT STT.STATUS)=2)"
						+ " OR NOT EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT."+stagePart+"))"
				+wherePart					
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "  UNION"
				//分配子任务，且子任务都是关闭状态==〉已完成
				+ " SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"
				+ "                  T.TASK_ID,"
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  1 ASSIGN_STATUS,"
				+ "                  3 PLAN_STATUS,"
				+selectPart					
				+ "                  TT.TASK_TYPE"
				+ "    FROM BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                G,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				+ "         SUBTASK                   ST,"
				+ "         BLOCK                   B"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.BLOCK_ID=B.BLOCK_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.STATUS=1"
				+ "     AND ST.STATUS IN (0,1)"
				+ "     AND ST."+stagePart
				+wherePart		
				+ "     AND NOT EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT.STATUS in (1,2) AND STT."+stagePart+")"
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = ST.BLOCK_MAN_ID"
				+ "  UNION"
				//分配子任务，且存在非关子任务==〉作业中
				+ " SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"	
				+ "                  T.TASK_ID,"
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  1 ASSIGN_STATUS,"
				+ "                  2 PLAN_STATUS,"
				+selectPart					
				+ "                  TT.TASK_TYPE"
				+ "    FROM BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                G,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				+ "         SUBTASK                   ST,"
				+ "         BLOCK                   B"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.BLOCK_ID=B.BLOCK_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.STATUS=1"
				+ "     AND ST.STATUS IN (0,1)"
				+ "     AND ST."+stagePart
				+wherePart		
				+ "     AND EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT.STATUS<>0 AND STT."+stagePart+")"
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = ST.BLOCK_MAN_ID(+)"
				+ "  UNION"
				//未规划block
				+ "  SELECT DISTINCT 0,"
				+ "                  '---',"
				+ "                  B.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"		
				+ "                  0,"
				+ "                  0 STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  0 ASSIGN_STATUS,"
				+ "                  1 PLAN_STATUS,"
				+ "                  '---' PLAN_START_DATE,"
				+ "                  '---' PLAN_END_DATE,"
				+ "                  0 GROUP_ID,"
				+ "                  0 PERCENT,"
				+ "                  0 DIFF_DATE,"
				+ "                  0 PROGRESS,"
				+ "                  '---' GROUP_NAME,"
				+ "                  1 TASK_TYPE"
				+ "    FROM BLOCK B, CITY C"
				+ "   WHERE B.CITY_ID = C.CITY_ID"
				+ "     AND B.PLAN_STATUS = 0"
				+ "     AND C.CITY_ID<>100002),"
				+ " FINAL_TABLE AS"
				+ " (SELECT *"
				+ "    FROM MAN_LIST"
				+ "    WHERE 1=1"
				+ conditionSql+")"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
				+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
		return BlockOperation.getSnapshotQuery(conn, selectSql,currentPageNum,pageSize);
	}

	/**
	 * @param blockId
	 * @param blockManId 
	 * @param type
	 * @return
	 * @throws ServiceException 
	 */
	public List queryWktByBlockId(int blockId, int blockManId,int type) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			List result = new ArrayList();
			if(1==type){
				//常规
				result = BlockOperation.queryWktByBlockIdNormal(conn, blockId);
			}else if(4==type){
				//情报
				result = BlockOperation.queryWktByBlockIdInfor(conn, blockManId);
			}
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询wkt失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询block名称列表
	 * @author Han Shaoming
	 * @param userId
	 * @param blockManName
	 * @return
	 * @throws ServiceException 
	 */
	public List<Map<String, Object>> queryBlockManNameList(long userId, String blockManName) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			
			//根据blockManName查询blockMan数据
			String sql = "SELECT * FROM BLOCK_MAN WHERE BLOCK_MAN_NAME LIKE '%"+blockManName+"%'";
			Object[] params = {};
			//处理结果集
			ResultSetHandler<List<Map<String, Object>>> rsh = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Map<String, Object>> blockManNameList = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("blockManId",rs.getLong("BLOCK_MAN_ID"));
						map.put("blockManName",rs.getString("BLOCK_MAN_NAME"));
						blockManNameList.add(map);
					}
					return blockManNameList;
				}
			};
			//获取数据
			List<Map<String, Object>> list = queryRunner.query(conn, sql, rsh, params);
			//日志
			log.info("查询的blockMan数据的sql"+sql);
			log.info("查询的blockMan数据"+list.toString());
			return list;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
