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
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?)";
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
			//设置为已读
			String sql = "INSERT INTO SYS_MESSAGE_READ_LOG (MSG_ID,USER_ID) VALUES(?,?)";
			Object[] params={msgId,userId};
			queryRunner.update(sysConn, sql, params);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更改失败，原因为:该消息已读,不能重复更改消息状态!");
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
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=? AND L.MSG_STATUS=1)";
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
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public void deleteMsg(long msgId,long userId)throws ServiceException{
		System.out.println("msgId==================="+msgId);
		Connection sysConn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//判断消息是否已读
			String sql = "SELECT COUNT(1) FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
			Object[] params={msgId,userId};
			long count = queryRunner.queryForLong(sysConn,sql,params);
			System.out.println("count=========="+count);
			//是否为已读
			if(count > 0){
				//已读消息
				System.out.println("================已读消息");
				//修改删除日志的状态为2
				String updateDeleteLogSql = "UPDATE SYS_MESSAGE_READ_LOG SET MSG_STATUS= 2 WHERE MSG_ID=? AND USER_ID=?";
				Object[] updateDeleteLogParams={msgId,userId};
				queryRunner.update(sysConn, updateDeleteLogSql, updateDeleteLogParams);
			}else{
				//未读消息
				//添加删除日志并且状态为2
				System.out.println("==================添加删除日志并且状态为2");
				String insertDeleteLogSql = "INSERT INTO SYS_MESSAGE_READ_LOG(MSG_ID,USER_ID,MSG_STATUS) VALUES(?,?,2)";
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
