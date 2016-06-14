package com.navinfo.dataservice.engine.man.userInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/** 
* @ClassName:  UserInfoService 
* @author code generator
* @date 2016-06-14 03:24:34 
* @Description: TODO
*/
@Service
public class UserInfoService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(UserInfo  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			String createSql = "insert into user_info (USER_ID, USER_REAL_NAME, USER_NICK_NAME, USER_PASSWORD, USER_EMAIL, USER_PHONE, USER_LEVEL, USER_SCORE, USER_ICON, USER_GPSID) values(?,?,?,?,?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getUserId() , bean.getUserRealName(), bean.getUserNickName(), bean.getUserPassword(), bean.getUserEmail(), bean.getUserPhone(), bean.getUserLevel(), bean.getUserScore(), bean.getUserIcon(), bean.getUserGpsid()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(UserInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String updateSql = "update user_info set USER_ID=?, USER_REAL_NAME=?, USER_NICK_NAME=?, USER_PASSWORD=?, USER_EMAIL=?, USER_PHONE=?, USER_LEVEL=?, USER_SCORE=?, USER_ICON=?, USER_GPSID=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				updateSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getUserRealName()!=null && StringUtils.isNotEmpty(bean.getUserRealName().toString())){
				updateSql+=" and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			};
			if (bean!=null&&bean.getUserNickName()!=null && StringUtils.isNotEmpty(bean.getUserNickName().toString())){
				updateSql+=" and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			};
			if (bean!=null&&bean.getUserPassword()!=null && StringUtils.isNotEmpty(bean.getUserPassword().toString())){
				updateSql+=" and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			};
			if (bean!=null&&bean.getUserEmail()!=null && StringUtils.isNotEmpty(bean.getUserEmail().toString())){
				updateSql+=" and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			};
			if (bean!=null&&bean.getUserPhone()!=null && StringUtils.isNotEmpty(bean.getUserPhone().toString())){
				updateSql+=" and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			};
			if (bean!=null&&bean.getUserLevel()!=null && StringUtils.isNotEmpty(bean.getUserLevel().toString())){
				updateSql+=" and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			};
			if (bean!=null&&bean.getUserScore()!=null && StringUtils.isNotEmpty(bean.getUserScore().toString())){
				updateSql+=" and USER_SCORE=? ";
				values.add(bean.getUserScore());
			};
			if (bean!=null&&bean.getUserIcon()!=null && StringUtils.isNotEmpty(bean.getUserIcon().toString())){
				updateSql+=" and USER_ICON=? ";
				values.add(bean.getUserIcon());
			};
			if (bean!=null&&bean.getUserGpsid()!=null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())){
				updateSql+=" and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			};
			run.update(conn, 
					   updateSql, 
					   values.toArray(),
					   values.toArray()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void delete(UserInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String deleteSql = "delete from  user_info where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				deleteSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getUserRealName()!=null && StringUtils.isNotEmpty(bean.getUserRealName().toString())){
				deleteSql+=" and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			};
			if (bean!=null&&bean.getUserNickName()!=null && StringUtils.isNotEmpty(bean.getUserNickName().toString())){
				deleteSql+=" and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			};
			if (bean!=null&&bean.getUserPassword()!=null && StringUtils.isNotEmpty(bean.getUserPassword().toString())){
				deleteSql+=" and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			};
			if (bean!=null&&bean.getUserEmail()!=null && StringUtils.isNotEmpty(bean.getUserEmail().toString())){
				deleteSql+=" and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			};
			if (bean!=null&&bean.getUserPhone()!=null && StringUtils.isNotEmpty(bean.getUserPhone().toString())){
				deleteSql+=" and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			};
			if (bean!=null&&bean.getUserLevel()!=null && StringUtils.isNotEmpty(bean.getUserLevel().toString())){
				deleteSql+=" and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			};
			if (bean!=null&&bean.getUserScore()!=null && StringUtils.isNotEmpty(bean.getUserScore().toString())){
				deleteSql+=" and USER_SCORE=? ";
				values.add(bean.getUserScore());
			};
			if (bean!=null&&bean.getUserIcon()!=null && StringUtils.isNotEmpty(bean.getUserIcon().toString())){
				deleteSql+=" and USER_ICON=? ";
				values.add(bean.getUserIcon());
			};
			if (bean!=null&&bean.getUserGpsid()!=null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())){
				deleteSql+=" and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			};
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public Page list(UserInfo bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select * from user_info where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				selectSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getUserRealName()!=null && StringUtils.isNotEmpty(bean.getUserRealName().toString())){
				selectSql+=" and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			};
			if (bean!=null&&bean.getUserNickName()!=null && StringUtils.isNotEmpty(bean.getUserNickName().toString())){
				selectSql+=" and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			};
			if (bean!=null&&bean.getUserPassword()!=null && StringUtils.isNotEmpty(bean.getUserPassword().toString())){
				selectSql+=" and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			};
			if (bean!=null&&bean.getUserEmail()!=null && StringUtils.isNotEmpty(bean.getUserEmail().toString())){
				selectSql+=" and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			};
			if (bean!=null&&bean.getUserPhone()!=null && StringUtils.isNotEmpty(bean.getUserPhone().toString())){
				selectSql+=" and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			};
			if (bean!=null&&bean.getUserLevel()!=null && StringUtils.isNotEmpty(bean.getUserLevel().toString())){
				selectSql+=" and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			};
			if (bean!=null&&bean.getUserScore()!=null && StringUtils.isNotEmpty(bean.getUserScore().toString())){
				selectSql+=" and USER_SCORE=? ";
				values.add(bean.getUserScore());
			};
			if (bean!=null&&bean.getUserIcon()!=null && StringUtils.isNotEmpty(bean.getUserIcon().toString())){
				selectSql+=" and USER_ICON=? ";
				values.add(bean.getUserIcon());
			};
			if (bean!=null&&bean.getUserGpsid()!=null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())){
				selectSql+=" and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<UserInfo> list = new ArrayList<UserInfo>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						UserInfo model = new UserInfo();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setUserId(rs.getInt("USER_ID"));
						model.setUserRealName(rs.getString("USER_REAL_NAME"));
						model.setUserNickName(rs.getString("USER_NICK_NAME"));
						model.setUserPassword(rs.getString("USER_PASSWORD"));
						model.setUserEmail(rs.getString("USER_EMAIL"));
						model.setUserPhone(rs.getString("USER_PHONE"));
						model.setUserLevel(rs.getInt("USER_LEVEL"));
						model.setUserScore(rs.getInt("USER_SCORE"));
						model.setUserIcon(rs.getObject("USER_ICON"));
						model.setUserGpsid(rs.getString("USER_GPSID"));
						list.add(model);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
			if (values.size()==0){
	    		return run.query(currentPageNum, 20, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, 20, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<UserInfo> list(UserInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
					
			String selectSql = "select * from user_info where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				selectSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getUserRealName()!=null && StringUtils.isNotEmpty(bean.getUserRealName().toString())){
				selectSql+=" and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			};
			if (bean!=null&&bean.getUserNickName()!=null && StringUtils.isNotEmpty(bean.getUserNickName().toString())){
				selectSql+=" and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			};
			if (bean!=null&&bean.getUserPassword()!=null && StringUtils.isNotEmpty(bean.getUserPassword().toString())){
				selectSql+=" and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			};
			if (bean!=null&&bean.getUserEmail()!=null && StringUtils.isNotEmpty(bean.getUserEmail().toString())){
				selectSql+=" and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			};
			if (bean!=null&&bean.getUserPhone()!=null && StringUtils.isNotEmpty(bean.getUserPhone().toString())){
				selectSql+=" and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			};
			if (bean!=null&&bean.getUserLevel()!=null && StringUtils.isNotEmpty(bean.getUserLevel().toString())){
				selectSql+=" and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			};
			if (bean!=null&&bean.getUserScore()!=null && StringUtils.isNotEmpty(bean.getUserScore().toString())){
				selectSql+=" and USER_SCORE=? ";
				values.add(bean.getUserScore());
			};
			if (bean!=null&&bean.getUserIcon()!=null && StringUtils.isNotEmpty(bean.getUserIcon().toString())){
				selectSql+=" and USER_ICON=? ";
				values.add(bean.getUserIcon());
			};
			if (bean!=null&&bean.getUserGpsid()!=null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())){
				selectSql+=" and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			};
			ResultSetHandler<List<UserInfo>> rsHandler = new ResultSetHandler<List<UserInfo>>(){
				public List<UserInfo> handle(ResultSet rs) throws SQLException {
					List<UserInfo> list = new ArrayList<UserInfo>();
					while(rs.next()){
						UserInfo model = new UserInfo();
						model.setUserId(rs.getInt("USER_ID"));
						model.setUserRealName(rs.getString("USER_REAL_NAME"));
						model.setUserNickName(rs.getString("USER_NICK_NAME"));
						model.setUserPassword(rs.getString("USER_PASSWORD"));
						model.setUserEmail(rs.getString("USER_EMAIL"));
						model.setUserPhone(rs.getString("USER_PHONE"));
						model.setUserLevel(rs.getInt("USER_LEVEL"));
						model.setUserScore(rs.getInt("USER_SCORE"));
						model.setUserIcon(rs.getObject("USER_ICON"));
						model.setUserGpsid(rs.getString("USER_GPSID"));
						list.add(model);
					}
					return list;
				}
	    		
	    	}		;
	    	if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public UserInfo query(UserInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			String selectSql = "select * from user_info where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				selectSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getUserRealName()!=null && StringUtils.isNotEmpty(bean.getUserRealName().toString())){
				selectSql+=" and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			};
			if (bean!=null&&bean.getUserNickName()!=null && StringUtils.isNotEmpty(bean.getUserNickName().toString())){
				selectSql+=" and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			};
			if (bean!=null&&bean.getUserPassword()!=null && StringUtils.isNotEmpty(bean.getUserPassword().toString())){
				selectSql+=" and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			};
			if (bean!=null&&bean.getUserEmail()!=null && StringUtils.isNotEmpty(bean.getUserEmail().toString())){
				selectSql+=" and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			};
			if (bean!=null&&bean.getUserPhone()!=null && StringUtils.isNotEmpty(bean.getUserPhone().toString())){
				selectSql+=" and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			};
			if (bean!=null&&bean.getUserLevel()!=null && StringUtils.isNotEmpty(bean.getUserLevel().toString())){
				selectSql+=" and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			};
			if (bean!=null&&bean.getUserScore()!=null && StringUtils.isNotEmpty(bean.getUserScore().toString())){
				selectSql+=" and USER_SCORE=? ";
				values.add(bean.getUserScore());
			};
			if (bean!=null&&bean.getUserIcon()!=null && StringUtils.isNotEmpty(bean.getUserIcon().toString())){
				selectSql+=" and USER_ICON=? ";
				values.add(bean.getUserIcon());
			};
			if (bean!=null&&bean.getUserGpsid()!=null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())){
				selectSql+=" and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<UserInfo> rsHandler = new ResultSetHandler<UserInfo>(){
				public UserInfo handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						UserInfo model = new UserInfo();
						model.setUserId(rs.getInt("USER_ID"));
						model.setUserRealName(rs.getString("USER_REAL_NAME"));
						model.setUserNickName(rs.getString("USER_NICK_NAME"));
						model.setUserPassword(rs.getString("USER_PASSWORD"));
						model.setUserEmail(rs.getString("USER_EMAIL"));
						model.setUserPhone(rs.getString("USER_PHONE"));
						model.setUserLevel(rs.getInt("USER_LEVEL"));
						model.setUserScore(rs.getInt("USER_SCORE"));
						model.setUserIcon(rs.getObject("USER_ICON"));
						model.setUserGpsid(rs.getString("USER_GPSID"));
						return model;
					}
					return null;
				}
	    		
	    	}		;				
			if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
