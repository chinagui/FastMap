package com.navinfo.dataservice.engine.man.role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 
 * @ClassName RoleService
 * @author Han Shaoming
 * @date 2016年11月12日 上午11:30:21
 * @Description TODO
 */
public class RoleService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private static class SingletonHolder{
		private static final RoleService INSTANCE =new RoleService();
	}
	public static RoleService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 根据角色查询用户列表
	 * @author Han Shaoming
	 * @param userId
	 * @param roleId
	 * @param conditionJson 
	 * @return
	 * @throws ServiceException 
	 */
	public List<Map<String, Object>> queryUserNameByRoleId(long userId, long roleId, JSONObject conditionJson) throws ServiceException {
		Connection conn = null;
		QueryRunner queryRunner = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			
			//根据roleId查询用户数据
			String sql = "SELECT U.* FROM USER_INFO U,ROLE_USER_MAPPING R WHERE U.USER_ID=R.USER_ID AND R.ROLE_ID=?";
			if(!conditionJson.isEmpty()){
				if(conditionJson.containsKey("userName")){
					sql+=" and u.USER_REAL_NAME like '%"+conditionJson.getString("userName")+"%'";
				}
			}
			Object[] params = {roleId};
			//处理结果集
			ResultSetHandler<List<Map<String, Object>>> rsh = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Map<String, Object>> userNameList = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("userId",rs.getLong("USER_ID"));
						map.put("userName",rs.getString("USER_REAL_NAME"));
						userNameList.add(map);
					}
					return userNameList;
				}
			};
			//获取数据
			List<Map<String, Object>> list = queryRunner.query(conn, sql, rsh, params);
			//日志
			log.info("查询的userInfo数据"+list.toString());
			return list;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
