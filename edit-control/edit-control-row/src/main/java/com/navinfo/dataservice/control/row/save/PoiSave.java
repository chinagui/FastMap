package com.navinfo.dataservice.control.row.save;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiPart;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiSelector;
import com.navinfo.dataservice.engine.batch.BatchProcess;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiSave {
	private static final Logger logger = Logger.getLogger(PoiSave.class);

	/**
	 * @param parameter
	 * @param userId
	 * @return
	 * @throws Exception
	 * @zhaokk POI行編保存
	 */
	public JSONObject save(String parameter, long userId) throws Exception {

		Connection conn = null;
		JSONObject result = new JSONObject();
		try {

			JSONObject json = JSONObject.fromObject(parameter);

			OperType operType = Enum.valueOf(OperType.class,
					json.getString("command"));

			ObjType objType = Enum.valueOf(ObjType.class,
					json.getString("type"));

			int dbId = json.getInt("dbId");
			// 加载用户subtaskId
			int subtaskId = 0;
			if (json.containsKey("subtaskId")) {
				subtaskId = json.getInt("subtaskId");
			}
			
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Map<String, Integer> taskInfo = apiService.getTaskBySubtaskId(subtaskId);
			
			Map<String, Integer> newTaskInfo= changeTaskInfo(subtaskId,taskInfo);

			conn = DBConnector.getInstance().getConnectionById(dbId);
			EditApiImpl editApiImpl = new EditApiImpl(conn);
 
			String tmpdata = json.get("data") == null ? "" : json.get("data").toString();
			int poiLength = 0;
			if(tmpdata.indexOf("{") == 0){
				poiLength = json.getJSONObject("data").size();
			}else if(tmpdata.indexOf("[") == 0){
				poiLength = json.getJSONArray("data").size();
			}

			if (poiLength == 0 && operType == OperType.UPDATE
					&& objType != ObjType.IXSAMEPOI
					&& objType != ObjType.IXPOIPARENT) {
				editApiImpl.updatePoifreshVerified(json.getInt("objId"));
				JSONArray ret = new JSONArray();
				result.put("log", ret);
				result.put("check", ret);
				return result;
			}
			
			editApiImpl.setToken(userId);
			editApiImpl.setSubtaskId(subtaskId);
			StringBuffer sb = new StringBuffer();
			int pid = 0;
			// POI同一关系
			if (ObjType.IXSAMEPOI == objType) {
				if (OperType.CREATE == operType) {
					String poiPids = JsonUtils.getStringValueFromJSONArray(json
							.getJSONArray("poiPids"));
					sb.append(poiPids);
				} else if (OperType.UPDATE == operType) {
					JSONObject data = json.getJSONObject("data");
					Integer samePid = data.getInt("pid");
					this.generatePoiPid(sb, samePid, conn);
				} else if (OperType.DELETE == operType) {
					Integer samePid = json.getInt("objId");
					this.generatePoiPid(sb, samePid, conn);
				}
				result = editApiImpl.runPoi(json);
				// POI父子关系
			} else if (ObjType.IXPOIPARENT == objType) {
				Integer childPoiPid = json.getInt("objId");
				Integer parentPoiPid = 0;
				if (OperType.CREATE == operType || OperType.UPDATE == operType) {
					parentPoiPid = json.getInt("parentPid");
					// 去掉父子关系超3级限制 zpp 2017.03.01
					// // 一个父子关系家族中，最多允许3级父子关系存在，大于3级以上，不可制作父子关系,
					// // 判断制作父子关系是否超过三级
					// boolean errorFlag = ParentChildReletion3level(conn,
					// childPoiPid, parentPoiPid);
					// if (!errorFlag){
					// throw new Exception("父子关系大于3级以上，不可制作父子关系！");
					// }
				} else if (OperType.DELETE == operType) {
					IxPoiParentSelector selector = new IxPoiParentSelector(conn);
					List<IRow> parents = selector.loadParentRowsByChildrenId(
							childPoiPid, true);
					for (IRow row : parents) {
						IxPoiParent parent = (IxPoiParent) row;
						parentPoiPid = parent.getParentPoiPid();
						break;
					}
				}

				sb.append(childPoiPid).append(",").append(parentPoiPid);
				result = editApiImpl.runPoi(json);
				if (OperType.CREATE != operType) {
					pid = json.getInt("objId");
				} else {
					pid = result.getInt("pid");
				}
				// 其他
			} else {
				if(OperType.DELETE == operType){
					Integer poiPid = json.getInt("objId");
					IxPoiParentSelector selector = new IxPoiParentSelector(conn);
					int parentPid = selector.getParentPid(poiPid);
					if(parentPid!=0){sb.append(",").append(parentPid);}
					List<Integer> ChildrenPid = selector.getChildrenPids(poiPid);
					for(int Child:ChildrenPid){
						sb.append(",").append(Child);
					}	
				}
				result = editApiImpl.runPoi(json);
				if (OperType.CREATE == operType) {
					pid = result.getInt("pid");
					sb.append(",").append(String.valueOf(pid));
				} else if(OperType.BATCHMOVE == operType){
					JSONArray logs = result.getJSONArray("log");
					for(int i = 0; i<logs.size();i++){
						JSONObject single = logs.getJSONObject(i);
						pid = single.getInt("pid");
						sb.append(",").append(String.valueOf(pid));
					}
				}
				else {
					pid = json.getInt("objId");
					sb.append(",").append(String.valueOf(pid));
				}
				sb.deleteCharAt(0);
			}

			// if (ObjType.IXSAMEPOI != objType) {
			// json.put("objId", pid);
			// BatchProcess batchProcess = new BatchProcess("row","save");
			// List<String> batchList = batchProcess.getRowRules();
			// batchProcess.execute(json, conn, editApiImpl, batchList);
			// }
			
			upatePoiStatus(sb.toString(), conn, newTaskInfo,true);

			if (operType == OperType.UPDATE) {
				editApiImpl.updatePoifreshVerified(pid);
			}

			return result;
		} catch (DataNotChangeException e) {
			DbUtils.rollback(conn);
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private Map<String, Integer> changeTaskInfo(int subtaskId,Map<String, Integer> taskInfo) throws Exception {
		Map<String, Integer> newTaskInfo =new HashMap<String, Integer>();
		if(taskInfo.get("programType")==1){
			newTaskInfo.put("MEDIUM_SUBTASK_ID",subtaskId);
			newTaskInfo.put("MEDIUM_TASK_ID",taskInfo.get("taskId"));
			newTaskInfo.put("QUICK_SUBTASK_ID",0);
			newTaskInfo.put("QUICK_TASK_ID",0);
		}else{
			newTaskInfo.put("MEDIUM_SUBTASK_ID",0);
			newTaskInfo.put("MEDIUM_TASK_ID",0);
			newTaskInfo.put("QUICK_SUBTASK_ID",subtaskId);
			newTaskInfo.put("QUICK_TASK_ID",taskInfo.get("taskId"));
		}
		
		return newTaskInfo;
	}
	private void generatePoiPid(StringBuffer sb, Integer samePid,
			Connection conn) throws Exception {
		IxSamepoiSelector selector = new IxSamepoiSelector(conn);
		IxSamepoi samepoi = (IxSamepoi) selector.loadById(samePid, true);
		int length = samepoi.getParts().size();
		for (int i = 0; i < length; i++) {
			IxSamepoiPart part = (IxSamepoiPart) samepoi.getParts().get(i);
			if (i < length - 1) {
				sb.append(part.getPoiPid()).append(",");
			} else {
				sb.append(part.getPoiPid());
			}
		}
	}

	/**
	 * @Title: upatePoiStatus
	 * @Description:poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 *                                      (修)(第七迭代) 变更:当新增 poi_edit_status 时,为
	 *                                      commit_his_status 字段赋默认值 0
	 * @param pids
	 * @param conn
	 * @param flag
	 * @throws Exception
	 *             void
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月8日 下午1:50:56
	 * @author gaopengrong
	 * @date 2017年4月18日 下午4:50:56
	 * @Description:(第八迭代)任务号：4403
	 */
	public void upatePoiStatus(String pids, Connection conn,Map<String, Integer> newTaskInfo, boolean flag)
			throws Exception {
		int qst=newTaskInfo.get("QUICK_SUBTASK_ID");
		int qt=newTaskInfo.get("QUICK_TASK_ID");
		int mst=newTaskInfo.get("MEDIUM_SUBTASK_ID");
		int mt=newTaskInfo.get("MEDIUM_TASK_ID");
		
		String str = qst+","+qt+","+mst+","+mt;
		StringBuilder sb = new StringBuilder();
		if (flag) {
			//对应需求任务4403，其中涉及任务号的变更，详细见《一体化日编任务作业.vsd》
			sb.append(" MERGE INTO poi_edit_status T1 ");
			sb.append(" USING (SELECT ");
			sb.append(" 	(CASE WHEN "+mst+" = 0 THEN T.MEDIUM_SUBTASK_ID WHEN T.STATUS IN (1, 2) AND T.MEDIUM_SUBTASK_ID NOT IN (0,"+mst+") THEN T.MEDIUM_SUBTASK_ID WHEN T.STATUS=1 AND T.MEDIUM_SUBTASK_ID =0 THEN T.MEDIUM_SUBTASK_ID ELSE "+mst+" END) MST,");
			sb.append(" 	(CASE WHEN "+mt+" = 0 THEN T.MEDIUM_TASK_ID WHEN T.STATUS IN (1, 2) AND T.MEDIUM_TASK_ID NOT IN (0,"+mt+") THEN T.MEDIUM_TASK_ID WHEN T.STATUS=1 AND T.MEDIUM_TASK_ID =0 THEN T.MEDIUM_TASK_ID ELSE "+mt+" END) MT,");
			sb.append(" 	(CASE WHEN "+qst+" = 0 THEN T.QUICK_SUBTASK_ID WHEN T.STATUS IN (1, 2) AND T.QUICK_SUBTASK_ID NOT IN (0,"+qst+") THEN T.QUICK_SUBTASK_ID WHEN T.STATUS=1 AND T.QUICK_SUBTASK_ID =0 THEN T.QUICK_SUBTASK_ID ELSE "+qst+" END) QST,");
			sb.append(" 	(CASE WHEN "+qt+" = 0 THEN T.QUICK_TASK_ID WHEN T.STATUS IN (1, 2) AND T.QUICK_TASK_ID NOT IN (0,"+qt+") THEN T.QUICK_TASK_ID WHEN T.STATUS=1 AND T.QUICK_TASK_ID =0 THEN T.QUICK_TASK_ID ELSE "+qt+" END) QT,");
			sb.append(" 	(CASE WHEN "+mst+" <> 0 AND T.STATUS=1 AND T.MEDIUM_SUBTASK_ID= 0 THEN 1");
			//sb.append(" 		  WHEN "+mst+" <> 0 AND T.STATUS=1 AND T.MEDIUM_SUBTASK_ID="+mst+" THEN 1");
			sb.append(" 		  WHEN "+mst+" <> 0 AND T.STATUS IN (1, 2) AND T.MEDIUM_SUBTASK_ID <> 0 AND "+mst+" <> T.MEDIUM_SUBTASK_ID THEN T.STATUS");
			sb.append(" 		  WHEN "+qst+" <> 0 AND T.STATUS=1 AND T.QUICK_SUBTASK_ID= 0 THEN 1");
			//sb.append(" 		  WHEN "+qst+" <> 0 AND T.STATUS=1 AND T.QUICK_SUBTASK_ID="+qst+" THEN 1");
			sb.append(" 		  WHEN "+qst+" <> 0 AND T.STATUS IN (1, 2) AND T.QUICK_SUBTASK_ID <> 0 AND "+qst+" <> T.QUICK_SUBTASK_ID THEN T.STATUS");
			sb.append(" 		  ELSE 2 END) B,");
			sb.append(" 	0 AS C,");
			sb.append(" 	IX.PID AS D");
			sb.append(" 	FROM IX_POI IX, POI_EDIT_STATUS T WHERE IX.PID = T.PID(+) AND IX.PID IN ("+ pids + ")) T2 ");
			sb.append(" ON ( T1.pid=T2.d) ");
			sb.append(" WHEN MATCHED THEN ");
			sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.QUICK_SUBTASK_ID=T2.QST,T1.QUICK_TASK_ID=T2.QT,T1.MEDIUM_SUBTASK_ID=T2.MST,T1.MEDIUM_TASK_ID=T2.MT ");
			sb.append(" WHEN NOT MATCHED THEN ");
			// zl 2016.12.08 新增时为 commit_his_status 字段赋默认值 0
			sb.append(" INSERT (T1.status,T1.fresh_verified,T1.pid,T1.commit_his_status,T1.QUICK_SUBTASK_ID,T1.QUICK_TASK_ID,T1.MEDIUM_SUBTASK_ID,T1.MEDIUM_TASK_ID) VALUES(T2.b,T2.c,T2.d,0,"+ str +")");
		} else {
			//鲜度验证保存时调用
			sb.append(" UPDATE poi_edit_status T1 SET T1.status = 2 where T1.pid in ("
					+ pids + ")");
		}

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}

	}

	/**
	 * 判断poiPid的父子关系是否超过三级
	 * 
	 * @param conn
	 *            大区库conn
	 * @param poiPid
	 *            当前poi的pid
	 * @param parentPid
	 *            当前poi的父pid，如果没有父，传0
	 * @return
	 * @throws Exception
	 */
	public boolean ParentChildReletion3level(Connection conn, int poiPid,
			int parentPid) throws Exception {
		IxPoiParentSelector poiParentSelector = new IxPoiParentSelector(conn);
		List<Integer> childPids = poiParentSelector.getChildrenPids(poiPid);
		List<String> error = new ArrayList<String>();
		if (childPids.size() != 0) {
			// 遍历每个1级子有没有2级子
			for (int oneChildPid : childPids) {
				List<Integer> twoChildPids = poiParentSelector
						.getChildrenPids(oneChildPid);
				// 有二级子
				if (twoChildPids.size() != 0) {
					if (parentPid != 0) {
						// 有二级子，并且有父，即父子关系为4级，则报错
						error.add("F");
					} else {
						// 遍历每个2级子有没有3级子
						for (int twoChildPid : twoChildPids) {
							List<Integer> threeChildPids = poiParentSelector
									.getChildrenPids(twoChildPid);
							// 有三级子，即父子关系为4级，则报错
							if (threeChildPids.size() != 0) {
								error.add("F");
							}
						}
					}
				} else {
					if (parentPid != 0) {
						int twoParentPid = poiParentSelector
								.getParentPid(parentPid);
						if (twoParentPid != 0) {
							// 有一级子，有二级父，父子关系为4级，报错
							error.add("F");
						}
					}

				}
			}
		} else {
			// 当前poi没有子，但是有3级父，报错
			if (parentPid != 0) {
				int twoParentPid = poiParentSelector.getParentPid(parentPid);
				if (twoParentPid != 0) {
					int threeParentPid = poiParentSelector
							.getParentPid(twoParentPid);
					if (threeParentPid != 0) {
						error.add("F");
					}
				}
			}
		}

		if (error.contains("F")) {
			return false;
		}
		return true;
	}
}
