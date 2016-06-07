package com.navinfo.dataservice.engine.man.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
* @ClassName:  UserInfoService 
* @author code generator
* @date 2016-06-06 11:31:13 
* @Description: TODO
*/
@Service
public class UserInfoService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	
	
	public void create(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			UserInfo  bean = (UserInfo)JSONObject.toBean(json, UserInfo.class);	
			
			String createSql = "insert into USER_INFO (USER_ID, USER_REAL_NAME, USER_NICK_NAME, USER_PASSWORD, USER_EMAIL, USER_PHONE, USER_LEVEL, USER_SCORE, USER_ICON) values(?,?,?,?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getUserId() , bean.getUserRealName(), bean.getUserNickName(), bean.getUserPassword(), bean.getUserEmail(), bean.getUserPhone(), bean.getUserLevel(), bean.getUserScore(), bean.getUserIcon()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			UserInfo  bean = (UserInfo)JSONObject.toBean(obj, UserInfo.class);	
			
			String updateSql = "update USER_INFO set USER_ID=?, USER_REAL_NAME=?, USER_NICK_NAME=?, USER_PASSWORD=?, USER_EMAIL=?, USER_PHONE=?, USER_LEVEL=?, USER_SCORE=?, USER_ICON=? where 1=1 USER_ID=? and USER_REAL_NAME=? and USER_NICK_NAME=? and USER_PASSWORD=? and USER_EMAIL=? and USER_PHONE=? and USER_LEVEL=? and USER_SCORE=? and USER_ICON=?";
			List<Object> values=new ArrayList();
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
			run.update(conn, 
					   updateSql, 
					   bean.getUserId() ,bean.getUserRealName(),bean.getUserNickName(),bean.getUserPassword(),bean.getUserEmail(),bean.getUserPhone(),bean.getUserLevel(),bean.getUserScore(),bean.getUserIcon(),
					   values.toArray()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void delete(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			UserInfo  bean = (UserInfo)JSONObject.toBean(obj, UserInfo.class);	
			
			String deleteSql = "delete from  USER_INFO where 1=1 ";
			List<Object> values=new ArrayList();
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
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public Page list(JSONObject json ,final int currentPageNum)throws Exception{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			UserInfo  bean = (UserInfo)JSONObject.toBean(obj, UserInfo.class);
			
			String selectSql = "select * from USER_INFO where 1=1 ";
			List<Object> values=new ArrayList();
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
			ResultSetHandler rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						HashMap map = new HashMap();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("userId", rs.getInt("USER_ID"));
						map.put("userRealName", rs.getString("USER_REAL_NAME"));
						map.put("userNickName", rs.getString("USER_NICK_NAME"));
						map.put("userPassword", rs.getString("USER_PASSWORD"));
						map.put("userEmail", rs.getString("USER_EMAIL"));
						map.put("userPhone", rs.getString("USER_PHONE"));
						map.put("userLevel", rs.getInt("USER_LEVEL"));
						map.put("userScore", rs.getInt("USER_SCORE"));
						map.put("userIcon", rs.getObject("USER_ICON"));
						list.add(map);
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
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<HashMap> list(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
					
			JSONObject obj = JSONObject.fromObject(json);	
			UserInfo  bean = (UserInfo)JSONObject.toBean(obj, UserInfo.class);	
			String selectSql = "select * from USER_INFO where 1=1 ";
			List<Object> values=new ArrayList();
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
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("userId", rs.getInt("USER_ID"));
						map.put("userRealName", rs.getString("USER_REAL_NAME"));
						map.put("userNickName", rs.getString("USER_NICK_NAME"));
						map.put("userPassword", rs.getString("USER_PASSWORD"));
						map.put("userEmail", rs.getString("USER_EMAIL"));
						map.put("userPhone", rs.getString("USER_PHONE"));
						map.put("userLevel", rs.getInt("USER_LEVEL"));
						map.put("userScore", rs.getInt("USER_SCORE"));
						map.put("userIcon", rs.getObject("USER_ICON"));
						list.add(map);
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
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public HashMap query(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			UserInfo  bean = (UserInfo)JSONObject.toBean(obj, UserInfo.class);	
			
			String selectSql = "select * from USER_INFO where USER_ID=? and USER_REAL_NAME=? and USER_NICK_NAME=? and USER_PASSWORD=? and USER_EMAIL=? and USER_PHONE=? and USER_LEVEL=? and USER_SCORE=? and USER_ICON=?";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("userId", rs.getInt("USER_ID"));
						map.put("userRealName", rs.getString("USER_REAL_NAME"));
						map.put("userNickName", rs.getString("USER_NICK_NAME"));
						map.put("userPassword", rs.getString("USER_PASSWORD"));
						map.put("userEmail", rs.getString("USER_EMAIL"));
						map.put("userPhone", rs.getString("USER_PHONE"));
						map.put("userLevel", rs.getInt("USER_LEVEL"));
						map.put("userScore", rs.getInt("USER_SCORE"));
						map.put("userIcon", rs.getObject("USER_ICON"));
						return map;
					}
					return null;
				}
	    		
	    	}		;				
			return run.query(conn, 
					   selectSql,
					   rsHandler, 
					   bean.getUserId(), bean.getUserRealName(), bean.getUserNickName(), bean.getUserPassword(), bean.getUserEmail(), bean.getUserPhone(), bean.getUserLevel(), bean.getUserScore(), bean.getUserIcon());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
