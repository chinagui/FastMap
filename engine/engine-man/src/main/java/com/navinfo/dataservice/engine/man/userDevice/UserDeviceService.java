package com.navinfo.dataservice.engine.man.userDevice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
/** 
* @ClassName:  UserDeviceService 
* @author code generator
* @date 2016-06-14 07:32:56 
* @Description: TODO
*/
@Service
public class UserDeviceService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	public void create(UserDevice  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			String createSql = "insert into user_device (DEVICE_ID, USER_ID, DEVICE_TOKEN, DEVICE_PLATFORM, DEVICE_VERSION, DEVICE_MODEL, DEVICE_SYSTEM_VERSION, DEVICE_DESCENDANT_VERSION) values(?,?,?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getDeviceId() , bean.getUserId(), bean.getDeviceToken(), bean.getDevicePlatform(), bean.getDeviceVersion(), bean.getDeviceModel(), bean.getDeviceSystemVersion(), bean.getDeviceDescendantVersion()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 信鸽消息推送接口
	 * 例如 pushMessage(1,"title_test","msg_content",XingeUtil.PUSH_MSG_TYPE_PROJECT,null)
	 * @param userId 需要推送的用户id 
	 * @param title 消息的标题，必须有值
	 * @param content 消息的详细内容，可为空
	 * @param msgType 消息类型，参考com.navinfo.dataservice.commons.xinge.XingeUtil.java常量PUSH_MSG_TYPE_*
	 * @param otherInfo 其他消息内容，可为空
	 * @return boolean类型，成功返回true，失败false
	 */
	public boolean pushMessage(long userId,String title,String content,int msgType,String otherInfo){
		try{
			UserDevice dObj=new UserDevice();
			dObj.setUserId((int) userId);
			List<UserDevice> deviceList = this.list(dObj);
			if (deviceList.isEmpty() || deviceList.size()==0){
				log.warn("用户没有登录设备");
				return false;}
			if(title.isEmpty()){
				log.warn("消息为空");
				return false;}
			if(content.isEmpty()){content="";}
			JSONObject msgReturn=new JSONObject();
			for(int i=0;i<deviceList.size();i++){
				UserDevice dtmp=deviceList.get(i);
				XingeUtil xingeUtil=new XingeUtil(dtmp.getDevicePlatform(), dtmp.getDeviceToken(),
						title, content, msgType, otherInfo);
				msgReturn=xingeUtil.pushSingleDevice();
				if(msgReturn.getInt("ret_code")==-1){
					log.error(msgReturn);
					return false;}
			}
			return true;
		}catch(Exception e){
			log.error("消息推送失败，原因为:"+e.getMessage(), e);
			return false;
			//throw new ServiceException("消息推送失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public void update(UserDevice bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String updateSql = "update user_device set DEVICE_ID=?, USER_ID=?, DEVICE_TOKEN=?, DEVICE_PLATFORM=?, DEVICE_VERSION=?, DEVICE_MODEL=?, DEVICE_SYSTEM_VERSION=?, DEVICE_DESCENDANT_VERSION=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getDeviceId()!=null && StringUtils.isNotEmpty(bean.getDeviceId().toString())){
				updateSql+=" and DEVICE_ID=? ";
				values.add(bean.getDeviceId());
			};
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				updateSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getDeviceToken()!=null && StringUtils.isNotEmpty(bean.getDeviceToken().toString())){
				updateSql+=" and DEVICE_TOKEN=? ";
				values.add(bean.getDeviceToken());
			};
			if (bean!=null&&bean.getDevicePlatform()!=null && StringUtils.isNotEmpty(bean.getDevicePlatform().toString())){
				updateSql+=" and DEVICE_PLATFORM=? ";
				values.add(bean.getDevicePlatform());
			};
			if (bean!=null&&bean.getDeviceVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceVersion().toString())){
				updateSql+=" and DEVICE_VERSION=? ";
				values.add(bean.getDeviceVersion());
			};
			if (bean!=null&&bean.getDeviceModel()!=null && StringUtils.isNotEmpty(bean.getDeviceModel().toString())){
				updateSql+=" and DEVICE_MODEL=? ";
				values.add(bean.getDeviceModel());
			};
			if (bean!=null&&bean.getDeviceSystemVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceSystemVersion().toString())){
				updateSql+=" and DEVICE_SYSTEM_VERSION=? ";
				values.add(bean.getDeviceSystemVersion());
			};
			if (bean!=null&&bean.getDeviceDescendantVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceDescendantVersion().toString())){
				updateSql+=" and DEVICE_DESCENDANT_VERSION=? ";
				values.add(bean.getDeviceDescendantVersion());
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
	public void delete(UserDevice bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String deleteSql = "delete from  user_device where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getDeviceId()!=null && StringUtils.isNotEmpty(bean.getDeviceId().toString())){
				deleteSql+=" and DEVICE_ID=? ";
				values.add(bean.getDeviceId());
			};
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				deleteSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getDeviceToken()!=null && StringUtils.isNotEmpty(bean.getDeviceToken().toString())){
				deleteSql+=" and DEVICE_TOKEN=? ";
				values.add(bean.getDeviceToken());
			};
			if (bean!=null&&bean.getDevicePlatform()!=null && StringUtils.isNotEmpty(bean.getDevicePlatform().toString())){
				deleteSql+=" and DEVICE_PLATFORM=? ";
				values.add(bean.getDevicePlatform());
			};
			if (bean!=null&&bean.getDeviceVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceVersion().toString())){
				deleteSql+=" and DEVICE_VERSION=? ";
				values.add(bean.getDeviceVersion());
			};
			if (bean!=null&&bean.getDeviceModel()!=null && StringUtils.isNotEmpty(bean.getDeviceModel().toString())){
				deleteSql+=" and DEVICE_MODEL=? ";
				values.add(bean.getDeviceModel());
			};
			if (bean!=null&&bean.getDeviceSystemVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceSystemVersion().toString())){
				deleteSql+=" and DEVICE_SYSTEM_VERSION=? ";
				values.add(bean.getDeviceSystemVersion());
			};
			if (bean!=null&&bean.getDeviceDescendantVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceDescendantVersion().toString())){
				deleteSql+=" and DEVICE_DESCENDANT_VERSION=? ";
				values.add(bean.getDeviceDescendantVersion());
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
	public Page list(UserDevice bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select * from user_device where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getDeviceId()!=null && StringUtils.isNotEmpty(bean.getDeviceId().toString())){
				selectSql+=" and DEVICE_ID=? ";
				values.add(bean.getDeviceId());
			};
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				selectSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getDeviceToken()!=null && StringUtils.isNotEmpty(bean.getDeviceToken().toString())){
				selectSql+=" and DEVICE_TOKEN=? ";
				values.add(bean.getDeviceToken());
			};
			if (bean!=null&&bean.getDevicePlatform()!=null && StringUtils.isNotEmpty(bean.getDevicePlatform().toString())){
				selectSql+=" and DEVICE_PLATFORM=? ";
				values.add(bean.getDevicePlatform());
			};
			if (bean!=null&&bean.getDeviceVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceVersion().toString())){
				selectSql+=" and DEVICE_VERSION=? ";
				values.add(bean.getDeviceVersion());
			};
			if (bean!=null&&bean.getDeviceModel()!=null && StringUtils.isNotEmpty(bean.getDeviceModel().toString())){
				selectSql+=" and DEVICE_MODEL=? ";
				values.add(bean.getDeviceModel());
			};
			if (bean!=null&&bean.getDeviceSystemVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceSystemVersion().toString())){
				selectSql+=" and DEVICE_SYSTEM_VERSION=? ";
				values.add(bean.getDeviceSystemVersion());
			};
			if (bean!=null&&bean.getDeviceDescendantVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceDescendantVersion().toString())){
				selectSql+=" and DEVICE_DESCENDANT_VERSION=? ";
				values.add(bean.getDeviceDescendantVersion());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<UserDevice> list = new ArrayList<UserDevice>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						UserDevice model = new UserDevice();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setDeviceId(rs.getInt("DEVICE_ID"));
						model.setUserId(rs.getInt("USER_ID"));
						model.setDeviceToken(rs.getString("DEVICE_TOKEN"));
						model.setDevicePlatform(rs.getString("DEVICE_PLATFORM"));
						model.setDeviceVersion(rs.getString("DEVICE_VERSION"));
						model.setDeviceModel(rs.getString("DEVICE_MODEL"));
						model.setDeviceSystemVersion(rs.getString("DEVICE_SYSTEM_VERSION"));
						model.setDeviceDescendantVersion(rs.getString("DEVICE_DESCENDANT_VERSION"));
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
	public List<UserDevice> list(UserDevice bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
					
			String selectSql = "select * from user_device where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getDeviceId()!=null && StringUtils.isNotEmpty(bean.getDeviceId().toString())){
				selectSql+=" and DEVICE_ID=? ";
				values.add(bean.getDeviceId());
			};
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				selectSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getDeviceToken()!=null && StringUtils.isNotEmpty(bean.getDeviceToken().toString())){
				selectSql+=" and DEVICE_TOKEN=? ";
				values.add(bean.getDeviceToken());
			};
			if (bean!=null&&bean.getDevicePlatform()!=null && StringUtils.isNotEmpty(bean.getDevicePlatform().toString())){
				selectSql+=" and DEVICE_PLATFORM=? ";
				values.add(bean.getDevicePlatform());
			};
			if (bean!=null&&bean.getDeviceVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceVersion().toString())){
				selectSql+=" and DEVICE_VERSION=? ";
				values.add(bean.getDeviceVersion());
			};
			if (bean!=null&&bean.getDeviceModel()!=null && StringUtils.isNotEmpty(bean.getDeviceModel().toString())){
				selectSql+=" and DEVICE_MODEL=? ";
				values.add(bean.getDeviceModel());
			};
			if (bean!=null&&bean.getDeviceSystemVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceSystemVersion().toString())){
				selectSql+=" and DEVICE_SYSTEM_VERSION=? ";
				values.add(bean.getDeviceSystemVersion());
			};
			if (bean!=null&&bean.getDeviceDescendantVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceDescendantVersion().toString())){
				selectSql+=" and DEVICE_DESCENDANT_VERSION=? ";
				values.add(bean.getDeviceDescendantVersion());
			};
			ResultSetHandler<List<UserDevice>> rsHandler = new ResultSetHandler<List<UserDevice>>(){
				public List<UserDevice> handle(ResultSet rs) throws SQLException {
					List<UserDevice> list = new ArrayList<UserDevice>();
					while(rs.next()){
						UserDevice model = new UserDevice();
						model.setDeviceId(rs.getInt("DEVICE_ID"));
						model.setUserId(rs.getInt("USER_ID"));
						model.setDeviceToken(rs.getString("DEVICE_TOKEN"));
						model.setDevicePlatform(rs.getString("DEVICE_PLATFORM"));
						model.setDeviceVersion(rs.getString("DEVICE_VERSION"));
						model.setDeviceModel(rs.getString("DEVICE_MODEL"));
						model.setDeviceSystemVersion(rs.getString("DEVICE_SYSTEM_VERSION"));
						model.setDeviceDescendantVersion(rs.getString("DEVICE_DESCENDANT_VERSION"));
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
	public UserDevice query(UserDevice bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			String selectSql = "select * from user_device where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getDeviceId()!=null && StringUtils.isNotEmpty(bean.getDeviceId().toString())){
				selectSql+=" and DEVICE_ID=? ";
				values.add(bean.getDeviceId());
			};
			if (bean!=null&&bean.getUserId()!=null && StringUtils.isNotEmpty(bean.getUserId().toString())){
				selectSql+=" and USER_ID=? ";
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getDeviceToken()!=null && StringUtils.isNotEmpty(bean.getDeviceToken().toString())){
				selectSql+=" and DEVICE_TOKEN=? ";
				values.add(bean.getDeviceToken());
			};
			if (bean!=null&&bean.getDevicePlatform()!=null && StringUtils.isNotEmpty(bean.getDevicePlatform().toString())){
				selectSql+=" and DEVICE_PLATFORM=? ";
				values.add(bean.getDevicePlatform());
			};
			if (bean!=null&&bean.getDeviceVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceVersion().toString())){
				selectSql+=" and DEVICE_VERSION=? ";
				values.add(bean.getDeviceVersion());
			};
			if (bean!=null&&bean.getDeviceModel()!=null && StringUtils.isNotEmpty(bean.getDeviceModel().toString())){
				selectSql+=" and DEVICE_MODEL=? ";
				values.add(bean.getDeviceModel());
			};
			if (bean!=null&&bean.getDeviceSystemVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceSystemVersion().toString())){
				selectSql+=" and DEVICE_SYSTEM_VERSION=? ";
				values.add(bean.getDeviceSystemVersion());
			};
			if (bean!=null&&bean.getDeviceDescendantVersion()!=null && StringUtils.isNotEmpty(bean.getDeviceDescendantVersion().toString())){
				selectSql+=" and DEVICE_DESCENDANT_VERSION=? ";
				values.add(bean.getDeviceDescendantVersion());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<UserDevice> rsHandler = new ResultSetHandler<UserDevice>(){
				public UserDevice handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						UserDevice model = new UserDevice();
						model.setDeviceId(rs.getInt("DEVICE_ID"));
						model.setUserId(rs.getInt("USER_ID"));
						model.setDeviceToken(rs.getString("DEVICE_TOKEN"));
						model.setDevicePlatform(rs.getString("DEVICE_PLATFORM"));
						model.setDeviceVersion(rs.getString("DEVICE_VERSION"));
						model.setDeviceModel(rs.getString("DEVICE_MODEL"));
						model.setDeviceSystemVersion(rs.getString("DEVICE_SYSTEM_VERSION"));
						model.setDeviceDescendantVersion(rs.getString("DEVICE_DESCENDANT_VERSION"));
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
