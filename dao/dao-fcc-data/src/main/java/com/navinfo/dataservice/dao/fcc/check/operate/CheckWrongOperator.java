package com.navinfo.dataservice.dao.fcc.check.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import com.drew.lang.DateUtil;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckPercentConfig;

/**
 * @ClassName: CheckWrongOperator.java
 * @author y
 * @date 2017-5-25 下午3:48:52
 * @Description: 质检-质检问题记录操作表
 * 
 */
public class CheckWrongOperator {

	Logger log = Logger.getLogger(CheckPercentConfig.class);

	private static String INSERT_SQL = "INSERT INTO CHECK_WRONG						\n"
			+ "  (LOG_ID,                                     \n"
			+ "   CHECK_TASK_ID,                              \n"
			+ "   TIPS_CODE,                                  \n"
			+ "   TIPS_ROWKEY,                                \n"
			+ "   QU_DESC,                                    \n"
			+ "   REASON,                                     \n"
			+ "   ER_CONTENT,                                 \n"
			+ "   QU_RANK,                                    \n"
			+ "   WORKER,                                     \n"
			+ "   CHECKER,                                 \n"
			+ "   WORK_TIME,                                  \n"
			+ "   CHECK_TIME,                                 \n"
			+ "   IS_PREFER)                                  \n"
			+ "VALUES                                         \n"
			+ "  (?,                                 			\n" + // logId
			"   ?,                                          \n" + // checkTaskId
			"   ?,                                          \n" + // tipsCode
			"   ?,                                          \n" + // tipsRowkey
			"   ?,                                       	\n" + // quDesc
			"   ?,                                          \n" + // reason
			"   ?,                                      	 \n" + // content
			"   ?,                                          \n" + // level
			"   ?,                                          \n" + // WOKER
			"   ?,                                          \n" + // CHECKER
			"   ?,									         \n" + // workerDate
			"   ?,       								    \n" + // checkDate
			"   ?)                                         \n"; // isPrefer

	public CheckWrongOperator() {
		super();
	}

	/**
	 * @Description:保存质检问题记录
	 * @param wrong
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-5-25 下午4:03:49
	 */
	public CheckWrong save(CheckWrong wrong) throws Exception {

		PreparedStatement pst = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getCheckConnection();

			String uuid = UuidUtils.genUuid();

			wrong.setLogId(uuid);

			wrong.setCheckTime(DateUtils.format(new Date(),"yyyyMMddHHmmss"));
			
			

			pst = conn.prepareStatement(INSERT_SQL);

			pst.setString(1, uuid);
			pst.setInt(2, wrong.getCheckTaskId());
			pst.setString(3, wrong.getTipsCode());
			pst.setString(4, wrong.getTipsRowkey());
			pst.setString(5, wrong.getQuDesc());
			pst.setString(6, wrong.getReason());
			pst.setString(7, wrong.getErContent());
			pst.setString(8, wrong.getQuRank());
			pst.setString(9, wrong.getWorker());
			pst.setString(10, wrong.getChecker());
			pst.setTimestamp(11, new java.sql.Timestamp(DateUtils.stringToLong(wrong.getWorkTime(), "yyyyMMddHHmmss") ));
			pst.setTimestamp(12, new java.sql.Timestamp(DateUtils.stringToLong(wrong.getCheckTime(), "yyyyMMddHHmmss")));
			pst.setInt(13, wrong.getIsPrefer());

			pst.execute();

			return wrong;
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
	 * @Description:质检问题记录（更新）
	 * @param logId
	 * @param wrong
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-27 上午9:31:19
	 */
	public void update(String logId, CheckWrong wrong) throws Exception {

		String sql = "UPDATE CHECK_WRONG SET ";

		String whereSql = " WHERE LOG_ID= '" + logId + "'";

		StringBuffer setSql = new StringBuffer("");
		
		Connection conn = null;
		
		try {

			conn = DBConnector.getInstance().getCheckConnection();

			if (StringUtils.isNotEmpty(wrong.getQuDesc())) {
		
				setSql.append(" QU_DESC = '" + wrong.getQuDesc() + "',");
			}
		
			if (StringUtils.isNotEmpty(wrong.getReason())) {
		
				setSql.append(" REASON = '" + wrong.getReason() + "',");
			}
		
			if (StringUtils.isNotEmpty(wrong.getErContent())) {
		
				setSql.append(" ER_CONTENT = '" + wrong.getErContent() + "',");
			}
		
			if (StringUtils.isNotEmpty(wrong.getQuRank())) {
		
				setSql.append(" QU_RANK = '" + wrong.getQuRank()+ "',");
			}
		
			if (wrong.getIsPrefer()==0||wrong.getIsPrefer()==1) {
		
				setSql.append(" IS_PREFER = '" + wrong.getIsPrefer() + "'");
			}
			
			QueryRunner runner=new  QueryRunner();
			
			runner.update(conn, sql+setSql.toString()+whereSql);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error( e.getMessage(), e);
			throw new Exception( e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @Description:TOOD
	 * @param logId
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-27 上午10:19:35
	 */
	public void deleteByLogId(String logId) throws Exception {

		String delsSql = "DELETE FROM CHECK_WRONG  WHERE LOG_ID= ?";

		Connection conn = null;
		
		try {

			conn = DBConnector.getInstance().getCheckConnection();
			
			QueryRunner runner=new  QueryRunner();
			
			runner.update(conn, delsSql,logId);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error( e.getMessage(), e);
			throw new Exception(e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}

}
