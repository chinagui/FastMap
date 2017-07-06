package com.navinfo.dataservice.engine.sys.msg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
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
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?)  ORDER BY CREATE_TIME DESC";
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
				String insertDeleteLogSql = "INSERT INTO SYS_MESSAGE_READ_LOG(MSG_ID,USER_ID,MSG_STATUS,READ_TYPE) VALUES(?,?,2,1)";
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
			String sql = "SELECT DISTINCT M.MSG_TITLE FROM SYS_MESSAGE T WHERE T.MSG_TYPE=2 AND T.TARGET_USER_ID=?";
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
			sql.append("SELECT M.* FROM(SELECT T.*,R.USER_ID,R.READ_TYPE,NVL(R.MSG_STATUS,0) MSG_STATUS "
					+ "FROM SYS_MESSAGE T,SYS_MESSAGE_READ_LOG R WHERE T.MSG_ID = R.MSG_ID(+)) M WHERE 1=1 AND ");
			//添加筛选条件
			if(jo.get("msgStatus") != null){
				if((Integer)jo.get("msgStatus") == 0){
					//筛选未读消息
					sql.append(" M.MSG_STATUS IN (0) ");
				}else if((Integer)jo.get("msgStatus") == 1){
					//筛选已读消息
					sql.append(" M.MSG_STATUS IN (1)");
				}else if((Integer)jo.get("msgStatus") == 2){
					//筛选删除消息
					sql.append(" M.MSG_STATUS IN (2)");
				}
			}else{
				sql.append(" M.MSG_STATUS IN (0,1)");
			}
			//添加搜索条件
			if(StringUtils.isNotEmpty((String) jo.get("pushUserName"))){
				//模糊搜索处理人
				sql.append(" AND M.PUSH_USER_NAME LIKE '%"+jo.get("pushUserName")+"%'");
			}
			if(StringUtils.isNotEmpty((String)jo.get("msgContent") )){
				//模糊搜索标题内容
				sql.append(" AND M.MSG_CONTENT LIKE '%"+jo.get("msgContent")+"%'");
			}
			if(StringUtils.isNotEmpty((String)jo.get("msgTitle") )){
				//精确搜索事件
				sql.append(" AND M.MSG_TITLE='"+jo.get("msgTitle")+"'");
			}
			sql.append(" AND M.TARGET_USER_ID IN (0,"+userId+") AND M.MSG_TYPE=2 ");
			String querySql = sql.append(" ORDER BY CREATE_TIME DESC").toString();
			Object[] params = {};
			//日志
			log.info("查询man管理消息的sql:"+sql.toString());
			Page page = queryRunner.query(pageNum,pageSize,conn, querySql, new MsgWithPageHandler(pageNum, pageSize), params);
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
			String msgTypeSql = "SELECT MSG_TYPE FROM SYS_MESSAGE WHERE MSG_ID=?";
			Object[] msgTypeParams={msgId};
			long msgType = queryRunner.queryForLong(sysConn,msgTypeSql,msgTypeParams);
			//是否为已读
			if(count > 0){
				//已读消息
				//是否为系统消息
				if(msgType >0){
					//非系统消息
					//永久删除日志
					String deleteLogSql = "DELETE FROM SYS_MESSAGE_READ_LOG WHERE MSG_ID=? AND USER_ID=?";
					Object[] updateDeleteLogParams={msgId,userId};
					queryRunner.update(sysConn, deleteLogSql, updateDeleteLogParams);
					//永久删除消息
					String deleteSql = "DELETE FROM SYS_MESSAGE WHERE MSG_ID=? AND TARGET_USER_ID=?";
					Object[] deleteParams={msgId,userId};
					queryRunner.update(sysConn, deleteSql, deleteParams);
				}else{
					//系统消息
					//修改删除日志的状态为3
					String updateDeleteLogSql = "UPDATE SYS_MESSAGE_READ_LOG SET MSG_STATUS= 3 WHERE MSG_ID=? AND USER_ID=?";
					Object[] updateDeleteLogParams={msgId,userId};
					queryRunner.update(sysConn, updateDeleteLogSql, updateDeleteLogParams);
				}
			}else{
				//是否为系统消息
				if(msgType >0){
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
	
	/**
	 * 查询未读管理消息列表
	 * @author Han Shaoming
	 * @param userId
	 * @return
	 * @throws ServiceException 
	 */
	public List<Map<String, Object>> getUnUnOperateMsg(long userId) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			//查询消息
			conn =  MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE NOT EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L "
					+ " WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=?) AND T.TARGET_USER_ID IN (0,?) "
					+ " AND T.MSG_TYPE IN (2) ORDER BY CREATE_TIME DESC";
			//日志
			log.info("查询未读管理消息列表的sql:"+sql);
			
			Object[] params = {userId,userId};
			ResultSetHandler<List<Map<String,Object>>> rsh = new ResultSetHandler<List<Map<String,Object>>>() {
				
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> msg = new HashMap<String, Object>();
						msg.put("msgId",rs.getLong("MSG_ID"));
						msg.put("msgContent",rs.getString("MSG_CONTENT"));
						msg.put("createTime",DateUtils.dateToString(rs.getTimestamp("CREATE_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
						msg.put("type", "message");
						msgs.add(msg);
					}
					return msgs;
				}
			};
			List<Map<String, Object>> query = queryRunner.query(conn, sql, rsh, params);
			log.info("查询未读管理消息列表:"+query.toString());
			return query;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 全部消息查询列表
	 * @author Han Shaoming
	 * @param userId
	 * @param condition
	 * @return List<Map<String, Object>>:List<Map<"msgId", 123>>
	 * @throws ServiceException 
	 */
	public List<Map<String, Object>> getAllMsg(Long userId, String condition) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			//日志
			log.info("全部消息查询列表的筛选条件"+condition);
			
			JSONObject jo = JSONObject.fromObject(condition);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT M.* FROM(SELECT T.*,R.USER_ID,R.READ_TYPE,NVL(R.MSG_STATUS,0) MSG_STATUS "
					+ "FROM SYS_MESSAGE T,SYS_MESSAGE_READ_LOG R WHERE T.MSG_ID = R.MSG_ID(+)) M "
					+ "WHERE M.MSG_STATUS IN (0,1) ");
			//添加筛选条件
			if(jo.get("msgType") != null){
				if((Integer)jo.get("msgType") == 1){
					//筛选系统消息+job消息
					sql.append(" AND M.MSG_TYPE IN (0,1) ");
				}else if((Integer)jo.get("msgType") == 2){
					//筛选管理消息
					sql.append(" AND M.MSG_TYPE=2 ");
				}
			}
			if(jo.get("isHistory") != null){
				if((Integer)jo.get("isHistory") == 1){
					//筛选非历史消息
					sql.append(" AND M.CREATE_TIME >= (SYSDATE-5) ");
				}else if((Integer)jo.get("isHistory") == 2){
					//筛选历史消息
					sql.append(" AND M.CREATE_TIME < (SYSDATE-5) ");
				}
			}
			sql.append(" AND M.TARGET_USER_ID IN (0,"+userId+") ");
			String querySql = sql.append(" ORDER BY CREATE_TIME DESC").toString();
			Object[] params = {};
			//日志
			log.info("全部消息查询列表的sql:"+sql.toString());
			
			List<Map<String, Object>> msgs = queryRunner.query(conn, querySql, new MsgWithHandler(), params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 分页查询5天以上的消息列表
	 * @author Han Shaoming
	 * @param userId
	 * @param condition
	 * @return List<Map<String, Object>>:List<Map<"msgId", 123>>
	 * @throws ServiceException 
	 */
	public Page getAllMsgHistory(Long userId, String condition,int pageNum, int pageSize) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			//日志
			log.info("全部消息查询列表的筛选条件"+condition);
			
			JSONObject jo = JSONObject.fromObject(condition);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT M.* FROM(SELECT T.*,R.USER_ID,R.READ_TYPE,NVL(R.MSG_STATUS,0) MSG_STATUS "
					+ "FROM SYS_MESSAGE T,SYS_MESSAGE_READ_LOG R WHERE T.MSG_ID = R.MSG_ID(+)) M "
					+ "WHERE M.MSG_STATUS IN (0,1) ");
			//添加筛选条件
			if(jo.get("msgType") != null){
				if((Integer)jo.get("msgType") == 1){
					//筛选系统消息+job消息
					sql.append(" AND M.MSG_TYPE IN (0,1) ");
				}else if((Integer)jo.get("msgType") == 2){
					//筛选管理消息
					sql.append(" AND M.MSG_TYPE=2 ");
				}
			}
			if(jo.get("isHistory") != null){
				if((Integer)jo.get("isHistory") == 1){
					//筛选非历史消息
					sql.append(" AND M.CREATE_TIME >= (SYSDATE-5) ");
				}else if((Integer)jo.get("isHistory") == 2){
					//筛选历史消息
					sql.append(" AND M.CREATE_TIME < (SYSDATE-5) ");
				}
			}
			sql.append(" AND M.TARGET_USER_ID IN (0,"+userId+") ");
			String querySql = sql.append(" ORDER BY CREATE_TIME DESC").toString();
			Object[] params = {};
			//日志
			log.info("全部消息查询列表的sql:"+sql.toString());
			
			Page msgs = queryRunner.query(pageNum, pageSize,conn, querySql, new MsgWithPageHandlerAll(pageNum, pageSize), params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 删除消息查询列表
	 * @author Han Shaoming
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException 
	 */
	public Page getDeleteMsgList(Long userId, int pageNum, int pageSize) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			queryRunner = new QueryRunner();
			
			String sql = "SELECT T.* FROM SYS_MESSAGE T WHERE EXISTS(SELECT 1 FROM SYS_MESSAGE_READ_LOG L "
					+ " WHERE T.MSG_ID=L.MSG_ID AND L.USER_ID=? AND L.MSG_STATUS IN(2)) "
					+ " AND T.TARGET_USER_ID IN (0,?)  ORDER BY CREATE_TIME DESC";
			Object[] params = {userId,userId};
			//日志
			log.info("全部消息查询列表的sql:"+sql.toString());
			
			Page page = queryRunner.query(pageNum, pageSize, conn, sql, new MultiRowWithPageHandler(pageNum, pageSize), params);
			//处理消息类型
			List<SysMsg> msgs =  (List<SysMsg>) page.getResult();
			for (SysMsg sysMsg : msgs) {
				if(sysMsg.getMsgType() == 0){
					sysMsg.setMsgType(1L);
				}
			}
			page.setResult(msgs);
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
	 * 监控系统处理邮件发送数据
	 * @author Han Shaoming
	 * @param tos
	 * @param subject
	 * @param content
	 * @return 
	 * @throws ServiceException
	 */
	public void handleMonitorMessage(String tos,String subject,String content) throws ServiceException{
		try {
			// 获取IP
			String ip = null;
			String regex = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\."
					+ "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\."
					+ "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(subject);
			while (m.find()) {
				if (!"".equals(m.group())) {
					ip = m.group();
					System.out.println("come here:" + m.group());
					break;
				}
			}
			String[] datas = content.split("\r\n");
			System.out.println(datas.toString());
			// 报警状态
			String status = null;
			String statusPre = datas[0];
			if ("OK".equals(statusPre)) {
				status = "监控报警恢复";
			} else if ("PROBLEM".equals(statusPre)) {
				status = "监控报警";
			} else {
				status = statusPre;
			}
			// 报警等级
			String level = null;
			String levelPre = datas[1];
			if ("P0".equals(levelPre)) {
				level = "零级";
			} else if ("P1".equals(levelPre)) {
				level = "一级";
			} else if ("P2".equals(levelPre)) {
				level = "二级";
			} else if ("P3".equals(levelPre)) {
				level = "三级";
			} else if ("P4".equals(levelPre)) {
				level = "四级";
			} else if ("P5".equals(levelPre)) {
				level = "五级";
			} else if ("P6".equals(levelPre)) {
				level = "六级";
			} else {
				level = levelPre;
			}
			// 采集指标名称
			String metric = null;
			String metricPre = datas[3].split(":", 2)[1];
			if ("fos.service.interfaceStatus".equals(metricPre)) {
				metric = "服务接口状态";
			} else if ("fos.service.responseTime".equals(metricPre)) {
				metric = "服务接口响应时间";
			} else if ("fos.service.totalVisitCount".equals(metricPre)) {
				metric = "服务总访问次数";
			} else if ("fos.service.visitCount".equals(metricPre)) {
				metric = "服务接口访问次数";
			} else if ("fos.tomcat.gc".equals(metricPre)) {
				metric = "tomcat垃圾回收时间";
			} else if ("fos.tomcat.jdbc.activeConn".equals(metricPre)) {
				metric = "数据库活跃连接数";
			} else if ("fos.tomcat.jdbc.unclosedConn".equals(metricPre)) {
				metric = "数据库未关闭的连接数";
			} else if ("fos.tomcat.memoUsed".equals(metricPre)) {
				metric = "tomcat内存使用率";
			} else if ("net.port.listen".equals(metricPre)) {
				metric = "服务器启动状态";
			} else if ("cpu.busy".equals(metricPre)) {
				metric = "CPU使用率";
			}else if ("df.bytes.used.percent".equals(metricPre)) {
				metric = "磁盘使用率";
			}else if ("load.1min".equals(metricPre)) {
				metric = "系统负载";
			}else if ("mem.memused.percent".equals(metricPre)) {
				metric = "内存使用率";
			} else {
				metric = metricPre;
			}
			// 采集指标tag
			String tag = null;
			String tagPre = datas[4].split(":", 2)[1];
			if ("port=8081".equals(tagPre)) {
				tag = "edit服务(" + tagPre + ")";
			} else if ("port=8082".equals(tagPre)) {
				tag = "fcc服务(" + tagPre + ")";
			} else if ("port=8083".equals(tagPre)) {
				tag = "metadata服务(" + tagPre + ")";
			} else if ("port=8084".equals(tagPre)) {
				tag = "man服务(" + tagPre + ")";
			} else if ("port=8085".equals(tagPre)) {
				tag = "render服务(" + tagPre + ")";
			} else if ("port=8086".equals(tagPre)) {
				tag = "dropbox服务(" + tagPre + ")";
			} else if ("port=8087".equals(tagPre)) {
				tag = "job服务(" + tagPre + ")";
			} else if ("port=8089".equals(tagPre)) {
				tag = "datahub服务(" + tagPre + ")";
			} else if ("port=8090".equals(tagPre)) {
				tag = "statics服务(" + tagPre + ")";
			} else if ("port=8091".equals(tagPre)) {
				tag = "mapspotter服务(" + tagPre + ")";
			} else if ("port=8092".equals(tagPre)) {
				tag = "column服务(" + tagPre + ")";
			} else if ("port=8093".equals(tagPre)) {
				tag = "row服务(" + tagPre + ")";
			} else if ("port=8094".equals(tagPre)) {
				tag = "sys服务(" + tagPre + ")";
			} else if ("port=8095".equals(tagPre)) {
				tag = "collector服务(" + tagPre + ")";
			} else if ("port=8096".equals(tagPre)) {
				tag = "dealership服务(" + tagPre + ")";
			} else{
				tag = tagPre;
			}
			// 报警值及监控值
			String value = datas[5].split(":", 2)[1];
			String reg = "\\d+(\\.\\d+)?";
			Pattern p1 = Pattern.compile(reg);
			Matcher m1 = p1.matcher(value);
			List<String> list = new ArrayList<String>();
			while (m1.find()) {
				if (!"".equals(m1.group())) {
					list.add(m1.group());
				}
			}
			String alarmValue = list.get(0);
			String monitorValue = list.get(1);
			// 报警描述
			String note = datas[6].split(":", 2)[1];
			// 报警时间
			String time = datas[8].split(":", 2)[1];
			// 报警策略
			String alarmAddr = datas[9];
			// 处理邮件内容
			String mailTitle = null;
			if ("OK".equals(statusPre)) {
				mailTitle = "FM监控异常报警:"+ip+"的"+metric;
			} else if ("PROBLEM".equals(statusPre)) {
				mailTitle = "FM监控恢复通知:"+ip+"的"+metric;
			} else {
				mailTitle = "FM监控("+ip+")";
			}
			StringBuilder mailContext = new StringBuilder();
			mailContext.append("<!DOCTYPE html><html><head>");
			mailContext.append("<meta http-equiv=\"content-type\" content=\"text/html\" charset=\"UTF-8\" />");
			mailContext.append("<title>FastMap Monitor</title></head>");
			mailContext.append("<body style=\"background-color:#F5F5F5\">");
			mailContext.append("<table border=\"1\" width=\"50%\" cellpadding=\"10\" cellspacing=\"2\" align=\"center\">");
			mailContext.append("<caption><h1>FastMap-监控系统</h1></caption>");
			mailContext.append("<tr align=\"center\" bgcolor=\"azure\">");
			mailContext.append("<th>服务器</th>");
			mailContext.append("<th>"+ip+"</th>");
			mailContext.append("<th colspan=\"2\">"+time+"</th>");
			mailContext.append("</tr>");
			mailContext.append("<tr align=\"center\" bgcolor=\"lightgray\">");
			mailContext.append("<th>报警状态</th>");
			mailContext.append("<td>"+status+"</td>");
			mailContext.append("<th>报警等级</th>");
			mailContext.append("<td>"+level+"</td>");
			mailContext.append("</tr>");
			mailContext.append("<tr align=\"center\" bgcolor=\"lightgray\">");
			mailContext.append("<th>监控名称</th>");
			mailContext.append("<td colspan=\"3\">"+metric+"</td>");
			mailContext.append("</tr>");
			mailContext.append("<tr align=\"center\" bgcolor=\"lightgray\">");
			mailContext.append("<th>tag名称</th>");
			mailContext.append("<td colspan=\"3\">"+tag+"</td>");
			mailContext.append("</tr>");
			mailContext.append("<tr align=\"center\" bgcolor=\"lightgray\">");
			mailContext.append("<th>报警值</th>");
			mailContext.append("<td>"+alarmValue+"</td>");
			mailContext.append("<th>监控值</th>");
			mailContext.append("<td>"+monitorValue+"</td>");
			mailContext.append("</tr>");
			mailContext.append("<tr align=\"center\" bgcolor=\"lightgray\">");
			mailContext.append("<th>报警描述</th>");
			mailContext.append("<td colspan=\"3\">"+note+"</td>");
			mailContext.append("</tr>");
			mailContext.append("<tr align=\"center\" bgcolor=\"lightgray\">");
			mailContext.append("<th colspan=\"4\"><a href=\""+alarmAddr+"\" target=\"_blank\">报警策略</a></th>");
			mailContext.append("</tr>");
			mailContext.append("</table></body></html>");
			//调用接口发送邮件
			String url = SystemConfigFactory.getSystemConfig().getValue(PropConstant.smapMailUrl);
			String SEND_EMAil=SystemConfigFactory.getSystemConfig().getValue(PropConstant.sendEmail);
			String SEND_PWD=SystemConfigFactory.getSystemConfig().getValue(PropConstant.sendPwd);
			Map<String,String> parMap = new HashMap<String,String>();
			parMap.put("mailUser", SEND_EMAil);
			parMap.put("mailPwd", SEND_PWD);
			parMap.put("mailList", tos);
			parMap.put("title", mailTitle);
			parMap.put("content", mailContext.toString());
			String result = ServiceInvokeUtil.invokeByGet(url, parMap);
			log.info("发送邮件，调用smap请求返回值："+result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("发送失败，原因为:"+e.getMessage(),e);
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
				msg.setCreateTime(DateUtils.dateToString(rs.getTimestamp("CREATE_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
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
				msg.setCreateTime(DateUtils.dateToString(rs.getTimestamp("CREATE_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
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
	/**
	 * 
	 * @ClassName MsgWithPageHandler
	 * @author Han Shaoming
	 * @date 2016年11月9日 上午10:35:38
	 * @Description TODO
	 */
	class MsgWithPageHandler implements ResultSetHandler<Page>{
		private int pageNum;
		private int pageSize;
		MsgWithPageHandler(int pageNum,int pageSize){
			this.pageNum=pageNum;
			this.pageSize=pageSize;
		}
		public Page handle(ResultSet rs) throws SQLException {
			Page page = new Page(pageNum);
			page.setPageSize(pageSize);
			int total = 0;
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("msgId",rs.getLong("MSG_ID"));
				msg.put("msgType",rs.getInt("MSG_TYPE"));
				msg.put("msgContent",rs.getString("MSG_CONTENT"));
				msg.put("createTime",DateUtils.dateToString(rs.getTimestamp("CREATE_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
				msg.put("targetUserId",rs.getLong("TARGET_USER_ID"));
				msg.put("msgTitle",rs.getString("MSG_TITLE"));
				msg.put("pushUserId",rs.getLong("PUSH_USER_ID"));
				//处理关联要素
				String msgParam = rs.getString("MSG_PARAM");
				JSONObject jsn = JSONObject.fromObject(msgParam);
				String relateObject = (String) jsn.get("relateObject");
				//long relateObjectId = (Integer) jsn.get("relateObjectId");
				msg.put("relateObject",relateObject);
				msg.put("relateObjectId",jsn.get("relateObjectId"));
				msg.put("pushUserName",rs.getString("PUSH_USER_NAME"));
				msg.put("msgStatus",rs.getLong("MSG_STATUS"));
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
	
	/**
	 * 
	 * @ClassName MsgWithPageHandler
	 * @author Han Shaoming
	 * @date 2016年11月9日 上午10:35:38
	 * @Description TODO
	 */
	class MsgWithPageHandlerAll implements ResultSetHandler<Page>{
		private int pageNum;
		private int pageSize;
		MsgWithPageHandlerAll(int pageNum,int pageSize){
			this.pageNum=pageNum;
			this.pageSize=pageSize;
		}
		public Page handle(ResultSet rs) throws SQLException {
			Page page = new Page(pageNum);
			page.setPageSize(pageSize);
			int total = 0;
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("msgId",rs.getLong("MSG_ID"));
				msg.put("msgType",rs.getInt("MSG_TYPE"));
				msg.put("msgContent",rs.getString("MSG_CONTENT"));
				msg.put("createTime",DateUtils.dateToString(rs.getTimestamp("CREATE_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
				msg.put("targetUserId",rs.getLong("TARGET_USER_ID"));
				msg.put("msgTitle",rs.getString("MSG_TITLE"));
				msg.put("pushUserId",rs.getLong("PUSH_USER_ID"));
				msg.put("msgParam",rs.getString("MSG_PARAM"));
				msg.put("pushUserName",rs.getString("PUSH_USER_NAME"));
				msg.put("msgStatus",rs.getLong("MSG_STATUS"));
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
	
	/**
	 * 
	 * @ClassName MsgWithHandler
	 * @author Han Shaoming
	 * @date 2016年11月15日 下午3:35:23
	 * @Description TODO
	 */
	class MsgWithHandler implements ResultSetHandler<List<Map<String,Object>>>{
		
		public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("msgId",rs.getLong("MSG_ID"));
				int msgType=rs.getInt("MSG_TYPE");
				if(msgType==0){msgType=1;}
				msg.put("msgType",msgType);
				msg.put("msgContent",rs.getString("MSG_CONTENT"));
				msg.put("createTime",DateUtils.dateToString(rs.getTimestamp("CREATE_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
				msg.put("targetUserId",rs.getLong("TARGET_USER_ID"));
				msg.put("msgTitle",rs.getString("MSG_TITLE"));
				msg.put("pushUserId",rs.getLong("PUSH_USER_ID"));
				msg.put("msgParam",rs.getString("MSG_PARAM"));
				msg.put("pushUserName",rs.getString("PUSH_USER_NAME"));
				msg.put("msgStatus",rs.getLong("MSG_STATUS"));
				msgs.add(msg);
			}
			return msgs;
		}
		
	}
	
	
	
}
