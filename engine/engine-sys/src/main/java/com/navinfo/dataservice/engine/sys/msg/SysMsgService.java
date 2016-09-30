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
 * @ClassName: MsgService
 * @author xiaoxiaowen4127
 * @date 2016年9月8日
 * @Description: MsgService.java
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
	
	public List<SysMsg> getUnread(long userId)throws ServiceException{
		List<SysMsg> msgs = null;
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?)";
			msgs = run.query(conn, sql, new MultiRowHandler(), userId,userId);
			//设置为已读
			sql = "INSERT INTO SYS_MESSAGE_READ_LOG (MSG_ID,USER_ID) SELECT T.MSG_ID,? USER_ID FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?)";
			run.update(conn, sql, userId,userId,userId);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Page getRead(long userId,int pageNum,int pageSize)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?)";
			return run.query(pageNum,pageSize,conn, sql, new MultiRowWithPageHandler(pageNum,pageSize), userId);
		}catch(Exception e){
			DbUtils.closeQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
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
