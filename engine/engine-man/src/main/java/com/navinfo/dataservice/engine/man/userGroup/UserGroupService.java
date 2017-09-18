package com.navinfo.dataservice.engine.man.userGroup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

/** 
* @ClassName:  UserGroupService 
* @author code generator
* @date 2016-06-17 05:31:11 
* @Description: TODO
*/
@Service
public class UserGroupService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private UserGroupService() {
	}

	private static class SingletonHolder {
		private static final UserGroupService INSTANCE = new UserGroupService();
	}

	public static UserGroupService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	public void create(UserGroup  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			String createSql = "insert into user_group (GROUP_ID, GROUP_NAME, GROUP_TYPE, LEADER_ID) values(?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getGroupId() , bean.getGroupName(), bean.getGroupType(), bean.getLeaderId()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Page list(UserGroup bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select * from user_group where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getGroupId()!=0){
				selectSql+=" and GROUP_ID=? ";
				values.add(bean.getGroupId());
			};
			if (bean!=null&&bean.getGroupName()!=null && StringUtils.isNotEmpty(bean.getGroupName().toString())){
				selectSql+=" and GROUP_NAME=? ";
				values.add(bean.getGroupName());
			};
			if (bean!=null&&bean.getGroupType()!=null && StringUtils.isNotEmpty(bean.getGroupType().toString())){
				selectSql+=" and GROUP_TYPE=? ";
				values.add(bean.getGroupType());
			};
			if (bean!=null&&bean.getLeaderId()!=null && StringUtils.isNotEmpty(bean.getLeaderId().toString())){
				selectSql+=" and LEADER_ID=? ";
				values.add(bean.getLeaderId());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<UserGroup> list = new ArrayList<UserGroup>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						UserGroup model = new UserGroup();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setGroupId(rs.getInt("GROUP_ID"));
						model.setGroupName(rs.getString("GROUP_NAME"));
						model.setGroupType(rs.getInt("GROUP_TYPE"));
						model.setLeaderId(rs.getInt("LEADER_ID"));
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
	public List<UserGroup> list(UserGroup bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
					
			String selectSql = "select * from user_group where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getGroupId()!=0){
				selectSql+=" and GROUP_ID=? ";
				values.add(bean.getGroupId());
			};
			if (bean!=null&&bean.getGroupName()!=null && StringUtils.isNotEmpty(bean.getGroupName().toString())){
				selectSql+=" and GROUP_NAME=? ";
				values.add(bean.getGroupName());
			};
			if (bean!=null&&bean.getGroupType()!=null && StringUtils.isNotEmpty(bean.getGroupType().toString())){
				selectSql+=" and GROUP_TYPE=? ";
				values.add(bean.getGroupType());
			};
			if (bean!=null&&bean.getLeaderId()!=null && StringUtils.isNotEmpty(bean.getLeaderId().toString())){
				selectSql+=" and LEADER_ID=? ";
				values.add(bean.getLeaderId());
			};
			ResultSetHandler<List<UserGroup>> rsHandler = new ResultSetHandler<List<UserGroup>>(){
				public List<UserGroup> handle(ResultSet rs) throws SQLException {
					List<UserGroup> list = new ArrayList<UserGroup>();
					while(rs.next()){
						UserGroup model = new UserGroup();
						model.setGroupId(rs.getInt("GROUP_ID"));
						model.setGroupName(rs.getString("GROUP_NAME"));
						model.setGroupType(rs.getInt("GROUP_TYPE"));
						model.setLeaderId(rs.getInt("LEADER_ID"));
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
	public UserGroup query(UserGroup bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			String selectSql = "select * from user_group where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getGroupId()!=0){
				selectSql+=" and GROUP_ID=? ";
				values.add(bean.getGroupId());
			};
			if (bean!=null&&bean.getGroupName()!=null && StringUtils.isNotEmpty(bean.getGroupName().toString())){
				selectSql+=" and GROUP_NAME=? ";
				values.add(bean.getGroupName());
			};
			if (bean!=null&&bean.getGroupType()!=null && StringUtils.isNotEmpty(bean.getGroupType().toString())){
				selectSql+=" and GROUP_TYPE=? ";
				values.add(bean.getGroupType());
			};
			if (bean!=null&&bean.getLeaderId()!=null && StringUtils.isNotEmpty(bean.getLeaderId().toString())){
				selectSql+=" and LEADER_ID=? ";
				values.add(bean.getLeaderId());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<UserGroup> rsHandler = new ResultSetHandler<UserGroup>(){
				public UserGroup handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						UserGroup model = new UserGroup();
						model.setGroupId(rs.getInt("GROUP_ID"));
						model.setGroupName(rs.getString("GROUP_NAME"));
						model.setGroupType(rs.getInt("GROUP_TYPE"));
						model.setLeaderId(rs.getInt("LEADER_ID"));
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
	
	public List<UserGroup> listByUser(UserInfo userInfo)throws ServiceException{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	    	
	    	return listByUser(conn,userInfo.getUserId());	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<UserGroup> listByUser(Connection conn,int userId)throws ServiceException{
		try{
			QueryRunner run = new QueryRunner();
					
			String selectSql = "select ug.GROUP_ID"
					+ ",ug.GROUP_NAME"
					+ " from user_group ug,group_user_mapping gum"
					+ " where ug.group_id = gum.group_id"
					+ " and gum.user_id = " + userId;

			ResultSetHandler<List<UserGroup>> rsHandler = new ResultSetHandler<List<UserGroup>>(){
				public List<UserGroup> handle(ResultSet rs) throws SQLException {
					List<UserGroup> list = new ArrayList<UserGroup>();
					while(rs.next()){
						UserGroup model = new UserGroup();
						model.setGroupId(rs.getInt("GROUP_ID"));
						model.setGroupName(rs.getString("GROUP_NAME"));
						list.add(model);
					}
					return list;
				}
	    		
	    	}		;
	    	
	    	
	    	return run.query(conn, selectSql, rsHandler);
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	//根据用户组类型获取用户组列表
	public List<UserGroup> listByType(int groupType,JSONObject conditionJson)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select ug.GROUP_ID"
					+ ",ug.GROUP_NAME,ug.GROUP_TYPE"
					+ " from user_group ug"
					+ " WHERE UG.PARENT_GROUP_ID IS NULL";
					
			if(groupType != -1){	
				selectSql += " AND ug.group_type =" + groupType;
			}
			if(conditionJson!=null){
				if(conditionJson.containsKey("groupSubtype")){
					selectSql += " and ug.group_subtype =" + conditionJson.get("groupSubtype");
				}
			}

			ResultSetHandler<List<UserGroup>> rsHandler = new ResultSetHandler<List<UserGroup>>(){
				public List<UserGroup> handle(ResultSet rs) throws SQLException {
					List<UserGroup> list = new ArrayList<UserGroup>();
					while(rs.next()){
						UserGroup model = new UserGroup();
						model.setGroupId(rs.getInt("GROUP_ID"));
						model.setGroupName(rs.getString("GROUP_NAME"));
						model.setGroupType(rs.getInt("GROUP_TYPE"));
						list.add(model);
					}
					return list;
				}
	    		
	    	}		;
	    	
	    	
	    	return run.query(conn, selectSql, rsHandler);
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	//根据用户组类型获取用户组列表及用户组下用户信息
	public ArrayList<HashMap<?, ?>> listByTypeWithUserInfo(int groupType,JSONObject conditionJson)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select ug.GROUP_ID, ug.GROUP_NAME, ug.GROUP_TYPE,u.user_id,u.user_real_name,u.risk "
					+ " from user_group ug, group_user_mapping gum, user_info u "
					+ " where ug.group_id = gum.group_id "
					+ " and gum.user_id = u.user_id ";
		
			if(groupType != -1){	
				selectSql += " and ug.group_type =" + groupType;
			}
			if(conditionJson!=null){
				if(conditionJson.containsKey("groupSubtype")){
					selectSql += " and ug.group_subtype =" + conditionJson.get("groupSubtype");
				}
			}
			selectSql += " order by ug.group_id";
			
			ResultSetHandler<ArrayList<HashMap<?,?>>> rsHandler = new ResultSetHandler<ArrayList<HashMap<?,?>>>(){
				public ArrayList<HashMap<?,?>> handle(ResultSet rs) throws SQLException {
					ArrayList<HashMap<?,?>> list = new ArrayList<HashMap<?,?>>();
					HashMap group = new HashMap();
					List userList = new ArrayList();
					while(rs.next()){
						if(group.containsKey("groupId")&&((int)group.get("groupId")==rs.getInt("GROUP_ID"))){
							HashMap user = new HashMap();
							user.put("userId", rs.getInt("user_id"));
							user.put("userRealName", rs.getString("user_real_name"));
							user.put("risk", rs.getInt("risk"));
							userList.add(user);
						}else if(group.isEmpty()){
							group.put("groupId", rs.getInt("GROUP_ID"));
							group.put("groupName", rs.getString("GROUP_NAME"));
							group.put("groupType", rs.getInt("GROUP_TYPE"));
							userList = new ArrayList();
							HashMap user = new HashMap();
							user.put("userId", rs.getInt("user_id"));
							user.put("userRealName", rs.getString("user_real_name"));
							user.put("risk", rs.getInt("risk"));
							userList.add(user);
						}else if(group.containsKey("groupId")&&((int)group.get("groupId")!=rs.getInt("GROUP_ID"))){
							group.put("userList", userList);
							list.add(group);
							group = new HashMap();
							group.put("groupId", rs.getInt("GROUP_ID"));
							group.put("groupName", rs.getString("GROUP_NAME"));
							group.put("groupType", rs.getInt("GROUP_TYPE"));
							userList = new ArrayList();
							HashMap user = new HashMap();
							user.put("userId", rs.getInt("user_id"));
							user.put("userRealName", rs.getString("user_real_name"));
							user.put("risk", rs.getInt("risk"));
							userList.add(user);
						}
					}
					group.put("userList", userList);
					list.add(group);
					return list;
				}
	    		
	    	};
	    	
	    	
	    	return run.query(conn, selectSql, rsHandler);
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 *根据父用户组ID获取子用户组列表
	 * @param groupId
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> listByGroupId(int groupId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "SELECT UG.GROUP_ID,UG.GROUP_NAME"
					+ " FROM USER_GROUP UG"
					+ " WHERE UG.PARENT_GROUP_ID = " + groupId;

			ResultSetHandler<List<HashMap<String, Object>>> rsHandler = new ResultSetHandler<List<HashMap<String, Object>>>(){
				public List<HashMap<String, Object>> handle(ResultSet rs) throws SQLException {
					List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
					while(rs.next()){
						HashMap<String, Object> group = new HashMap<String, Object>();
						group.put("groupId", rs.getInt("GROUP_ID"));
						group.put("groupName", rs.getString("GROUP_NAME"));
						list.add(group);
					}
					return list;
				}
	    		
	    	};

	    	List<HashMap<String, Object>> userGroupList = run.query(conn, selectSql, rsHandler);
	    	
	    	Map<String,Object> result = new HashMap<String,Object>();
			result.put("total", userGroupList.size());
			result.put("data", userGroupList);
			
	    	return result;
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public String getGroupNameByGroupId(long groupId) throws ServiceException{
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			// 查询组名
			String querySql = "select g.group_name from user_group g where g.group_id = " + groupId;

			ResultSetHandler<String> rsh = new ResultSetHandler<String>() {
				@Override
				public String handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					String name = null;
					if(rs.next()){
						name = rs.getString("group_name");
					}
					return name;
				}
			};
			String name = run.query(conn, querySql, rsh);
			return name;
		} catch (Exception e) {
			// TODO: handle exception
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询组名失败，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public UserGroup getGroupNameByGroupId(Connection conn,long groupId) throws ServiceException{
		try {
			Set<Long> userSet=new HashSet<Long>();
			userSet.add(groupId);
			Map<Long, UserGroup> returnMap = getGroupByGroupIds(conn, userSet);
			if(returnMap.containsKey(groupId)){return returnMap.get(groupId);}
			return null;
		} catch (Exception e) {
			// TODO: handle exception
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询组名失败，原因为:" + e.getMessage(), e);
		}
	}
	
	public Map<Long, UserGroup> getGroupByGroupIds(Connection conn,Set<Long> groupIdSet) throws ServiceException{
		try {
			QueryRunner run = new QueryRunner();
			// 查询组名
			String querySql = "select g.group_id,g.group_name from user_group g where g.group_id in " + groupIdSet.toString().replace("[", "(").replace("]", ")");

			ResultSetHandler<Map<Long, UserGroup>> rsh = new ResultSetHandler<Map<Long, UserGroup>>() {
				@Override
				public Map<Long, UserGroup> handle(ResultSet rs) throws SQLException {
					Map<Long, UserGroup> groups=new HashMap<Long, UserGroup>();
					while(rs.next()){
						UserGroup group=new UserGroup();
						group.setGroupName(rs.getString("group_name"));
						group.setGroupId(rs.getInt("group_id"));
						groups.put(rs.getLong("group_id"), group);
					}
					return groups;
				}
			};
			return run.query(conn, querySql, rsh);
		} catch (Exception e) {
			// TODO: handle exception
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询组名失败，原因为:" + e.getMessage(), e);
		}
	}
	

	public Map<Integer,UserGroup> getGroupByAdmin(Connection conn, String admin) throws ServiceException{
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT G.GROUP_ID, G.GROUP_NAME, G.GROUP_TYPE           ");
			sb.append("   FROM USER_GROUP G                                     ");
			sb.append("  WHERE G.GROUP_TYPE IN (0, 1)                           ");
			sb.append("    AND G.GROUP_NAME IN (SELECT M.COLLECT_GROUP_NAME     ");
			sb.append("                           FROM ADMIN_GROUP_MAPPING M    ");
			sb.append("                          WHERE M.PROVINCE_NAME = '" + admin + "'  ");
			sb.append("                         UNION                           ");
			sb.append("                         SELECT M.EDIT_GROUP_NAME        ");
			sb.append("                           FROM ADMIN_GROUP_MAPPING M    ");
			sb.append("                          WHERE M.PROVINCE_NAME = '" + admin + "')  ");
			
			log.info("getGroupByAdmin sql :" + sb.toString());

			ResultSetHandler<Map<Integer,UserGroup>> rsh = new ResultSetHandler<Map<Integer,UserGroup>>() {
				@Override
				public Map<Integer,UserGroup> handle(ResultSet rs) throws SQLException {
					Map<Integer,UserGroup> result = new HashMap<Integer,UserGroup>();
					while(rs.next()){
						int groupId = rs.getInt("GROUP_ID");
						String groupName = rs.getString("GROUP_NAME");
						int type = rs.getInt("GROUP_TYPE");
						UserGroup group = new UserGroup();
						group.setGroupId(groupId);
						group.setGroupName(groupName);
						group.setGroupType(type);
						result.put(type, group);
					}
					return result;
				}
			};
			Map<Integer,UserGroup> result = run.query(conn, sb.toString(), rsh);
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("getGroupByAdmin失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
	 * 组赋值方法 1采集2编辑3众包4情报5多源6月编
	 * @param adminCode
	 * @param type
	 * @throws Exception 
	 * @author songhe
	 */
	public UserGroup getGroupByAminCode(String adminCode, int type){
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			return getGroupByAminCode(conn, adminCode, type);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}
	
	/**
	 * 组赋值方法 1采集2编辑3众包4情报5多源6月编
	 * @param adminCode
	 * @param type
	 * @throws Exception 
	 * @author songhe
	 */
	public UserGroup getGroupByAminCode(Connection conn, String adminCode, int type) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			
			String name = "";
			switch(type){
				case 1:
					name = "COLLECT_GROUP_NAME";
					break;
				case 2:
					name = "EDIT_GROUP_NAME";
					break;
				case 3:
					name = "CROWD_GROUP_NAME";
					break;
				case 4:
					name = "INFOR_GROUP_NAME";
					break;
				case 5:
					name = "MULTISOURCE_GROUP_NAME";
					break;
				case 6:
					name = "MONTH_GROUP_NAME";
					break;
			}
			
			String selectSql = "select u.group_id, u.group_name, u.group_type, u.leader_id, u.parent_group_id"
					+ " from USER_GROUP u , ADMIN_GROUP_MAPPING t where t.ADMIN_CODE = '"+ adminCode +"'" 
					+ "and u.group_name = t." + name;
			
			return run.query(conn, selectSql, new ResultSetHandler<UserGroup>(){
				
				@Override
				public UserGroup handle(ResultSet result) throws SQLException {
					UserGroup  userGroup = new UserGroup();
					if(result.next()){
						userGroup.setGroupId(result.getInt("GROUP_ID"));
						userGroup.setGroupName(result.getString("GROUP_NAME"));
						userGroup.setGroupType(result.getInt("GROUP_TYPE"));
						userGroup.setLeaderId(result.getInt("LEADER_ID"));
						userGroup.setParentGroupId(result.getInt("PARENT_GROUP_ID"));
					}
					return userGroup;
				}});
		}catch(Exception e){
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}
	
}
