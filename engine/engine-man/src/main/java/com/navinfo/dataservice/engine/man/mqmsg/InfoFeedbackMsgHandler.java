package com.navinfo.dataservice.engine.man.mqmsg;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONObject;

/**
 * 同步消费消息
 * 
 * @ClassName: InfoChangeMsgHandler
 * @author Xiao Xiaowen
 * @date 2016年6月25日 上午10:42:43
 * @Description: TODO
 * 
 */
public class InfoFeedbackMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	@Override
	public void handle(String message) {
		try {
			// 解析保存到man库infor表中
			save(message);
		} catch (Exception e) {
			log.warn("接收到info_change消息,但保存失败，该消息已消费。message：" + message);
			log.error(e.getMessage(), e);

		}
	}

	/**
	 *  feedMsg.put("taskId", info.get("taskId"));
                                            feedMsg.put("isAdopted", info.get("c_isAdopted"));
                                            feedMsg.put("denyReason", info.get("c_denyReason"));
                                            feedMsg.put("feedbackDate", info.get("c_date"));

	 * @param message
	 * @throws Exception
	 */
	public void save(String message) throws Exception {
		log.info("FEEDBACK:"+message);
		Connection conn = null;
		try {
			JSONObject msgJSON = JSONObject.fromObject(message);
			String sql="UPDATE INFOR"
					+ "   SET IS_ADOPTED = ?, DENY_REASON = ?, FEEDBACK_DATE = ?"
					+ " WHERE INFOR_ID = (SELECT P.INFOR_ID"
					+ "                     FROM PROGRAM P, TASK T, SUBTASK S"
					+ "                    WHERE P.PROGRAM_ID = T.PROGRAM_ID"
					+ "                      AND T.TASK_ID = S.TASK_ID"
					+ "                      AND S.SUBTASK_ID = ?)";
			conn = DBConnector.getInstance().getManConnection();			
			List<Object> values = new ArrayList<Object>();
			values.add(msgJSON.getInt("isAdopted"));
			values.add(msgJSON.getString("denyReason"));
			values.add(DateUtils.stringToTimestamp(msgJSON.getString("feedbackDate"), DateUtils.DATE_COMPACTED_FORMAT));
			values.add(msgJSON.getInt("taskId"));
			QueryRunner run = new QueryRunner();
			log.info("FEEDBACK:"+sql);
			run.update(conn, sql, values.toArray());			
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
