package com.navinfo.dataservice.dao.fcc.check.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.fcc.check.model.CheckTask;

/** 
 * @ClassName: CheckTaskOperator.java
 * @author y
 * @date 2017-5-31 上午10:30:06
 * @Description: 质检任务信息表
 */
public class CheckTaskOperator {
	
	Logger log = Logger.getLogger(CheckTaskOperator.class);
	
	private static String INSERT_SQL = 
	"INSERT INTO  CHECK_TASK(		\n"+
			"  TASK_ID  ,                   \n"+
			"  TASK_NAME  ,                 \n"+
			"  SUB_TASK_NAME,               \n"+
			"  WORKER ,                     \n"+
			"  WORK_GROUP,                  \n"+
			"  CHECKER ,                    \n"+
			"  WORK_TOTAL_COUNT,            \n"+
			"  CHECK_TOTAL_COUNT ,          \n"+
			"  TIPS_TYPE_COUNT,             \n"+
			"  CHECK_STATUS ,               \n"+
			"  EXTRACT_TIME                 \n"+
			")VALUES                        \n"+
			"(                              \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"?,                             \n"+
			"SYSDATE                        \n"+
			")                              \n";
	
	
	/**
	 * @Description:保存质检问题记录
	 * @param task
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-5-25 下午4:03:49
	 */
	public CheckTask save(CheckTask task) throws Exception {

		PreparedStatement pst = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getCheckConnection();

			pst = conn.prepareStatement(INSERT_SQL);
			pst.setInt(1, task.getTaskId());
			pst.setString(2, task.getTaskName());
			pst.setString(3, task.getSubTaskName());
			pst.setString(4, task.getWokerInfo());
			pst.setString(5, task.getWorkGroup());
			pst.setString(6, task.getCheckInfo());
			pst.setInt(7, task.getWorkTotalCount());
			pst.setInt(8, task.getCheckTotalCount());
			pst.setInt(9, task.getTipTypeCount());
			pst.setInt(10, task.getCheckStatus());
			pst.execute();

			return task;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error( e.getMessage(), e);
			throw new Exception( e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(pst);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}


	/**
	 * @Description:关闭质检任务，修改质检任务状态为：已完成
	 * @param checkTaskId
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-31 下午3:59:08
	 */
	public void closeTask(int checkTaskId) throws Exception {
		
		String updateSql="UPDATE CHECK_TASK SET CHECK_STATUS=1,CHECK_END_DATE=SYSDATE WHERE TASK_ID=?";
		PreparedStatement pst = null;
		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getCheckConnection();
			pst = conn.prepareStatement(updateSql);
			pst.setInt(1, checkTaskId);
			pst.execute();
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error( e.getMessage(), e);
			throw new Exception( e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(pst);
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}


}
