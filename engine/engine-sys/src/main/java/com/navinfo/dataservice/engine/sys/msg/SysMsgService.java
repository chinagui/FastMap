package com.navinfo.dataservice.engine.sys.msg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SysMsgService
 * @author Han Shaoming
 * @date 2016年9月22日 下午4:44:23
 * @Description TODO
 */
public class SysMsgService {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private volatile static SysMsgService instance;
	public static SysMsgService getInstance(){
		if(instance==null){
			synchronized(SysMsgService.class){
				if(instance==null){
					instance=new SysMsgService();
				}
			}
		}
		return instance;
	}
	private SysMsgService(){}
	
	/**
	 * 查询未读消息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public List<SysMsg> getUnread(long userId)throws ServiceException{
		List<SysMsg> msgs = null;
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?) ORDER BY CREATE_TIME DESC";
			msgs = queryRunner.query(sysConn, sql, new MultiRowHandler(), userId,userId);
			//设置为已读
			//sql = "INSERT INTO SYS_MESSAGE_READ_LOG (MSG_ID,USER_ID) SELECT T.MSG_ID,? USER_ID FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?)";
			//queryRunner.update(sysConn, sql, userId,userId,userId);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	
	/**
	 * 更改系统消息状态
	 * @param userId
	 * @throws ServiceException
	 */
	public void updateMsgStatusToRead(long msgId,long userId)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//判断消息是否已读
			String sql = "SELECT COUNT(1) FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
			Object[] params={msgId,userId};
			long count = queryRunner.queryForLong(sysConn,sql,params);
			//是否为已读
			if(count > 0){
				//已读消息
			}else{
				//未读消息
				//设置为已读
				String updateSql = "INSERT INTO SYS_MESSAGE_READ_LOG (MSG_ID,USER_ID,MSG_STATUS,READ_TYPE) VALUES(?,?,1,1)";
				Object[] updateParams={msgId,userId};
				queryRunner.update(sysConn, updateSql, updateParams);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更改失败，原因为:"+e.getMessage());
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 查询已读系统消息
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public Page getReadMsg(long userId,int pageNum,int pageSize)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=? AND L.MSG_STATUS=1) ORDER BY CREATE_TIME DESC";
			Object[] params = {userId};
			Page page = queryRunner.query(pageNum,pageSize,sysConn, sql, new MultiRowWithPageHandler(pageNum,pageSize), params);
			return page;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 删除消息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public void deleteMsg(long msgId,long userId)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//判断消息是否已读
			String sql = "SELECT COUNT(1) FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
			Object[] params={msgId,userId};
			long count = queryRunner.queryForLong(sysConn,sql,params);
			//是否为已读
			if(count > 0){
				//已读消息
				//修改删除日志的状态为2
				String updateDeleteLogSql = "UPDATE SYS_MESSAGE_READ_LOG SET MSG_STATUS= 2 WHERE MSG_ID=? AND USER_ID=?";
				Object[] updateDeleteLogParams={msgId,userId};
				queryRunner.update(sysConn, updateDeleteLogSql, updateDeleteLogParams);
			}else{
				//未读消息
				//添加删除日志并且状态为2
				String insertDeleteLogSql = "INSERT INTO SYS_MESSAGE_READ_LOG(MSG_ID,USER_ID,MSG_STATUS,,READ_TYPE) VALUES(?,?,2,1)";
				Object[] insertDeleteLogParams={msgId,userId};
				queryRunner.update(sysConn, insertDeleteLogSql, insertDeleteLogParams);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 查询消息详情
	 * @param msgId
	 * @return
	 * @throws ServiceException 
	 */
	public List<SysMsg> selectSysMsgDetail(long msgId) throws ServiceException{
		Connection conn = null;
		QueryRunner queryRunner = null;
		try {
			//查询消息
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			String userSql = "SELECT * FROM SYS_MESSAGE WHERE MSG_ID=?";
			Object[] userParams = {msgId};
			List<SysMsg> sysMsg = queryRunner.query(conn, userSql, userParams, new MultiRowHandler());
			return sysMsg;
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询消息详情失败，原因为:"+e.getMessage(),e);
		} finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询man管理消息的title
	 * @author Han Shaoming
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public List<String> getManMsgTitleList(long userId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		QueryRunner queryRunner = null;
		try {
			//查询消息
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			String sql = "SELECT DISTINCT M.MSG_TITLE FROM SYS_MESSAGE M WHERE M.TARGET_USER_ID=?";
			Object[] params = {userId};
			ResultSetHandler<List<String>> rsh = new ResultSetHandler<List<String>>() {

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<String> titleList = new ArrayList<String>();
					while(rs.next()){
						titleList.add(rs.getString("MSG_TITLE"));
					}
					return null;
				}
			};
			List<String> list = queryRunner.query(conn, sql, rsh, params);
			return list;
		} catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取man管理消息列表
	 * @author Han Shaoming
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 * @param condition
	 * @return
	 * @throws ServiceException 
	 */
	public Page getManMsgList(long userId, int pageNum, int pageSize, String condition) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			//查询消息
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			//拼接sql语句
			JSONObject jo = JSONObject.fromObject(condition);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT T.* FROM SYS_MESSAGE T ");
			//添加筛选条件
			if(jo.get("msgStatus") != null){
				if((long)jo.get("msgStatus") == 0){
					//筛选未读消息
					sql.append(" WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE 1=1 ");
				}else if((long)jo.get("msgStatus") == 1){
					//筛选已读消息
					sql.append(" WHERE EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE L.MSG_STATUS IN (1)");
				}else if((long)jo.get("msgStatus") == 2){
					//筛选删除消息
					sql.append(" WHERE EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE L.MSG_STATUS IN (2)");
				}
			}else{
				sql.append(" WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE L.MSG_STATUS IN (2)");
			}
			sql.append(" AND T.MSG_ID=L.MSG_ID AND L.USER_ID="+userId+") ");
			//添加搜索条件
			if(jo.get("pushUserName") != null){
				//模糊搜索处理人
				sql.append(" AND T.PUSH_USER_NAME LIKE '%"+jo.get("pushUserName")+"%'");
			}
			if(jo.get("msgContent") != null){
				//模糊搜索标题内容
				sql.append(" AND T.MSG_CONTENT LIKE '%"+jo.get("msgContent")+"%'");
			}
			if(jo.get("msgTitle") != null){
				//精确搜索事件
				sql.append(" AND T.MSG_TITLE='"+jo.get("msgTitle")+"'");
			}
			sql.append(" AND T.TARGET_USER_ID IN (0,"+userId+") AND T.MSG_TYPE=2 ");
			String querySql = sql.append(" ORDER BY CREATE_TIME DESC").toString();
			Object[] params = {};
			//日志
			log.info("查询man管理消息的sql:"+sql.toString());
			Page page = queryRunner.query(pageNum,pageSize,conn, querySql, new MultiRowWithPageHandler(pageNum,pageSize), params);
			return page;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 批量修改管理信息状态
	 * @author Han Shaoming
	 * @param userId
	 * @param msgStatus 
	 * @param msgIds
	 * @return
	 * @throws ServiceException 
	 */
	public String updateManMsg(long userId, long msgStatus, JSONArray msgIds) throws ServiceException {
		// TODO Auto-generated method stub
		int total = 0;
		try{
			//编辑消息状态
			for(int i=0;i<msgIds.size();i++){
				long msgId = msgIds.getLong(i);
				if(msgStatus == 1){
					//已查看
					this.updateMsgStatusToRead(msgId, userId);
				}else if(msgStatus == 2){
					//删除
					this.deleteMsg(msgId, userId);
				}else if(msgStatus == 3){
					//永久删除
					this.deleteForeverMsg(msgId, userId);
				}else if(msgStatus == 4){
					//恢复到已读
					this.updateDeleteMsgToRead(msgId, userId);
				}
				total+=1;
			}
		return "批量修改消息状态："+total+"个成功,"+(msgIds.size()-total)+"个失败!";
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("编辑失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 永久删除消息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public void deleteForeverMsg(long msgId,long userId)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//判断消息是否已读
			String sql = "SELECT COUNT(1) FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
			Object[] params={msgId,userId};
			long count = queryRunner.queryForLong(sysConn,sql,params);
			//判断是否为系统消息
			String msgTypeSql = "SELECT TARGET_USER_ID FROM SYS_MESSAGE WHERE MSG_ID=?";
			Object[] msgTypeParams={msgId};
			long targetUserId = queryRunner.queryForLong(sysConn,msgTypeSql,msgTypeParams);
			//是否为已读
			if(count > 0){
				//已读消息
				//是否为系统消息
				if(targetUserId >0){
					//非系统消息
					//永久删除日志
					String deleteLogSql = "DELETE FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
					Object[] updateDeleteLogParams={msgId,userId};
					queryRunner.update(sysConn, deleteLogSql, updateDeleteLogParams);
				}else{
					//系统消息
					//修改删除日志的状态为3
					String updateDeleteLogSql = "UPDATE SYS_MESSAGE_READ_LOG SET MSG_STATUS= 3 WHERE MSG_ID=? AND USER_ID=?";
					Object[] updateDeleteLogParams={msgId,userId};
					queryRunner.update(sysConn, updateDeleteLogSql, updateDeleteLogParams);
				}
			}else{
				//是否为系统消息
				if(targetUserId >0){
					//非系统消息
					//永久删除消息
					String deleteSql = "DELETE FROM SYS_MESSAGE WHERE MSG_ID=? AND TARGET_USER_ID=?";
					Object[] updateDeleteLogParams={msgId,userId};
					queryRunner.update(sysConn, deleteSql, updateDeleteLogParams);
				}else{
					//系统消息
					//修改删除日志的状态为3
					String updateDeleteLogSql = "UPDATE SYS_MESSAGE_READ_LOG SET MSG_STATUS= 3 WHERE MSG_ID=? AND USER_ID=?";
					Object[] updateDeleteLogParams={msgId,userId};
					queryRunner.update(sysConn, updateDeleteLogSql, updateDeleteLogParams);
				}
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("永久删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	/**
	 * 恢复删除消息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public void updateDeleteMsgToRead(long msgId,long userId)throws ServiceException{
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//判断消息是否已读
			String sql = "SELECT COUNT(1) FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
			Object[] params={msgId,userId};
			long count = queryRunner.queryForLong(sysConn,sql,params);
			//是否为已读
			if(count > 0){
				//已读消息
				//修改日志的状态为1
				String updateDeleteLogSql = "UPDATE SYS_MESSAGE_READ_LOG SET MSG_STATUS= 1 WHERE MSG_ID=? AND USER_ID=?";
				Object[] updateDeleteLogParams={msgId,userId};
				queryRunner.update(sysConn, updateDeleteLogSql, updateDeleteLogParams);
			}else{
				//未读消息
				//添加日志并且状态为1
				String insertDeleteLogSql = "INSERT INTO SYS_MESSAGE_READ_LOG(MSG_ID,USER_ID,MSG_STATUS,READ_TYPE) VALUES(?,?,1,1)";
				Object[] insertDeleteLogParams={msgId,userId};
				queryRunner.update(sysConn, insertDeleteLogSql, insertDeleteLogParams);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
	}
	
	
	/* resultset handler */
	class MultiRowHandler implements ResultSetHandler<List<SysMsg>>{
		
		public List<SysMsg> handle(ResultSet rs) throws SQLException {
			List<SysMsg> msgs = new ArrayList<SysMsg>();
			while(rs.next()){
				SysMsg msg = new SysMsg();
				msg.setMsgId(rs.getLong("MSG_ID"));
				msg.setMsgType(rs.getInt("MSG_TYPE"));
				msg.setMsgContent(rs.getString("MSG_CONTENT"));
				msg.setCreateTime(rs.getTimestamp("CREATE_TIME"));
				msg.setTargetUserId(rs.getLong("TARGET_USER_ID"));
				msg.setMsgTitle(rs.getString("MSG_TITLE"));
				msg.setPushUserId(rs.getLong("PUSH_USER_ID"));
				msg.setMsgParam(rs.getString("MSG_PARAM"));
				msg.setPushUserName(rs.getString("PUSH_USER_NAME"));
				msgs.add(msg);
			}
			return msgs;
		}
		
	}
	/* resultset handler */
	class MultiRowWithPageHandler implements ResultSetHandler<Page>{
		private int pageNum;
		private int pageSize;
		MultiRowWithPageHandler(int pageNum,int pageSize){
			this.pageNum=pageNum;
			this.pageSize=pageSize;
		}
		public Page handle(ResultSet rs) throws SQLException {
			Page page = new Page(pageNum);
			page.setPageSize(pageSize);
			int total = 0;
			List<SysMsg> msgs = new ArrayList<SysMsg>();
			while(rs.next()){
				SysMsg msg = new SysMsg();
				msg.setMsgId(rs.getLong("MSG_ID"));
				msg.setMsgType(rs.getInt("MSG_TYPE"));
				msg.setMsgContent(rs.getString("MSG_CONTENT"));
				msg.setCreateTime(rs.getTimestamp("CREATE_TIME"));
				msg.setTargetUserId(rs.getLong("TARGET_USER_ID"));
				msg.setMsgTitle(rs.getString("MSG_TITLE"));
				msg.setPushUserId(rs.getLong("PUSH_USER_ID"));
				msg.setMsgParam(rs.getString("MSG_PARAM"));
				msg.setPushUserName(rs.getString("PUSH_USER_NAME"));
				msgs.add(msg);
				if(total==0){
					total=rs.getInt("TOTAL_RECORD_NUM_");
				}
			}
			page.setResult(msgs);
			page.setTotalCount(total);
			return page;
		}
		
	}
	
	
	
	
}
