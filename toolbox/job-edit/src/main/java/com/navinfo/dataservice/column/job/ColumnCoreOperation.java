package com.navinfo.dataservice.column.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;

public class ColumnCoreOperation {
	
	protected static Logger log = LoggerRepos.getLogger(ColumnCoreOperation.class);
	
	/**
	 * 实现逻辑
	 *  1)	取数据检查结果
	 *	2)	检查结果和poi_deep_status里已记录的work_item_id做差分比较
	 *	3)	若同一一级项或者二级项下，①已记录了该检查项，则不做处理；②没记录则新增一条记录；③若检查结果没有该规则号，classifyRules中有，poi_deep_status表中记录了该规则号，则从poi_deep_status表删除该记录
	 * @param mapParams
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void runClassify(HashMap mapParams) throws Exception {
		Connection conn = null;
		try{
			int userId=(int) mapParams.get("userId");
			List ckRules=(List) mapParams.get("ckRules");
			List classifyRules=(List) mapParams.get("classifyRules");
			List dataList=(List) mapParams.get("data"); //每条数据需包含子任务号，rowId
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			int oldDbId = 0;
			
			for(int i=0;i<dataList.size();i++){
				HashMap mapData=(HashMap) dataList.get(i);
				String rowId=(String) mapData.get("rowId");
				int taskId=(int) mapData.get("taskId");
				Subtask subtask = apiService.queryBySubtaskId(taskId);
				int dbId = subtask.getDbId();
				if (dbId != oldDbId) {
					DbUtils.closeQuietly(conn);
					oldDbId = dbId;
					conn = DBConnector.getInstance().getConnectionById(oldDbId);
				}
				//根据数据取检查结果
				NiValExceptionSelector checkSelector=new NiValExceptionSelector(conn);
				List checkResultList=checkSelector.loadByPid((Integer)mapData.get("pid"), ckRules);
				IxPoiColumnStatusSelector deepStatusSelector = new IxPoiColumnStatusSelector(conn);
				
				//取poi_deep_status中打标记结果
				List existClassifyList=deepStatusSelector.queryClassifyByRowid(rowId,taskId);
				
				//检查结果与poi_deep_status中结果，进行差分处理
				List currentCheckResult=checkResultList;
				
				//poi_deep_status不存在的作业项，要插入
				checkResultList.retainAll(ckRules);
				checkResultList.removeAll(existClassifyList);
				insertWorkItem(checkResultList,conn,rowId,taskId);
				
				//重分类回退，本次要重分类classifyRules,检查结果中没有，若poi_deep_status存在,需从poi_deep_status中删掉
				List currentClassifyRules=classifyRules;
				classifyRules.removeAll(currentCheckResult);
				classifyRules.retainAll(existClassifyList);
				deleteWorkItem(classifyRules,conn,rowId,taskId);
			}
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * 往poi_deep_status表插入作业标记信息
	 * @param checkResultList
	 * @param conn
	 * @param rowId
	 * @param taskId
	 * @throws Exception
	 */
	public void insertWorkItem(List<String> checkResultList,Connection conn,String rowId,int taskId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into poi_deep_status(row_id,work_item_id,first_work_status,second_work_status,handler,task_id) values(?,?,?,?,?) ");
		
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			for(String workItem:checkResultList){
				pstmt.setString(1, rowId);
				pstmt.setString(2, workItem);
				pstmt.setInt(3, 1);
				pstmt.setInt(4, 1);
				pstmt.setInt(5, 0);
				pstmt.setInt(6, taskId);
				pstmt.addBatch();	
			}

			pstmt.executeBatch();
			pstmt.clearBatch();
		}catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("往poi_deep_status表插入作业标记信息出错，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}	
	/**
	 * 从poi_deep_status表删除作业标记信息
	 * @param classifyRules
	 * @param conn
	 * @param rowId
	 * @param taskId
	 * @throws Exception
	 */
	public void deleteWorkItem(List<String> classifyRules,Connection conn,String rowId,int taskId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from poi_deep_status where row_id=? and work_item_id=? and task_id=?");
		
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			for(String workItem:classifyRules){
				pstmt.setString(1, rowId);
				pstmt.setString(2, workItem);
				pstmt.setInt(3, taskId);
				pstmt.addBatch();	
			}

			pstmt.executeBatch();
			pstmt.clearBatch();
	
		}catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("从poi_deep_status表删除作业标记信息出错，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}	

}
