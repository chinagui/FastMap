package com.navinfo.dataservice.engine.man.layer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;
import oracle.sql.CLOB;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.message.MessageOperation;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName:  CustomisedLayerService 
* @author code generator
* @date 2016-06-13 05:53:14 
* @Description: TODO
*/
@Service
public class LayerService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private static class SingletonHolder {
		private static final LayerService INSTANCE = new LayerService();
	}

	public static LayerService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void create(long userId, String layerName,String wkt)throws Exception{
		Connection conn = null;
		QueryRunner queryRunner = null;
		Long id = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			queryRunner = new QueryRunner();
			//获取插入数据的id
			String idSql = "SELECT CUSTOMISED_LAYER_SEQ.NEXTVAL FROM DUAL";
			Object[] idParams = {};
			id = queryRunner.queryForLong(conn, idSql, idParams);
			
			String createSql = "insert into customised_layer (LAYER_ID, LAYER_NAME,GEOMETRY, CREATE_USER_ID, CREATE_DATE,STATUS,CITY_ID) "
					+ "values("+id+",'"+layerName+"',sdo_geometry('"+wkt+"',8307),"+userId+",sysdate,1, "
							+ "(SELECT C.CITY_ID FROM CITY C WHERE SDO_CONTAINS(C.GEOMETRY,sdo_geometry('"+wkt+"',8307))='TRUE' AND ROWNUM=1))";
			//log日志
			log.info("创建重点区块的sql:"+createSql);
			DbOperation.exeUpdateOrInsertBySql(conn, createSql);
			
			//发送消息
			/*重点区块新增
			 *1.所有生管角色
			 *2.重点区块所在城市的作业组组长(采集、日编、月编)
			 *新增重点区块:XXX(任务名称),请关注*/
			try {
				List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
				List<Long> groupIdList = new ArrayList<Long>();
				Map<String,Object> map = new HashMap<String, Object>();
				String msgTitle="重点区块新增";
				String msgContent = "新增重点区块:"+layerName+",请关注";
				map.put("msgContent", msgContent);
				//关联要素
				JSONObject msgParam = new JSONObject();
				msgParam.put("relateObject", "LAYER");
				msgParam.put("relateObjectId", id);
				map.put("msgParam", msgParam.toString());
				msgContentList.add(map);
				//根据cityId查询task数据
				List<Map<String, Object>> taskList = TaskOperation.getTaskByCityId(conn, id, 1);
				for (Map<String, Object> task : taskList) {
					groupIdList.add((Long) task.get("monthEditGroupId"));
					//查询block分配的采集和日编作业组组长id
					if(task.get("taskId") != null){
						Map<String, Object> blockMan = TaskOperation.getBlockManByTaskId(conn, (long) task.get("taskId"), 1);
						if(blockMan != null){
							groupIdList.add((Long) blockMan.get("collectGroupId"));
							groupIdList.add((Long) blockMan.get("dayEditGroupId"));
						}
					}
					
				}
				if(msgContentList.size()>0){
					layerPushMsg(conn,msgTitle,msgContentList, groupIdList, userId);
				}
			} catch (Exception e) {
				// TODO: handle exception
				log.error(e.getMessage(), e);
				throw new Exception("新增失败，原因为:"+e.getMessage(),e);
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/*重点区块几何变更
	 * 1.所有生管角色
	 * 2.所有采集角色
	 * 3.所有日编角色
	 * 4.所有月编角色
	 * 重点区块:XXX(重点区块名称)内容发生变更，请关注*/
	/*
	public void layerPushMsg(Connection conn,String msgTile,List<String> msgContentList) throws Exception{
		String userSql="SELECT DISTINCT M.USER_ID FROM ROLE_USER_MAPPING M WHERE M.ROLE_ID IN (3, 4,5,6)";
		List<Integer> userIdList=UserInfoOperation.getUserListBySql(conn, userSql);
		Object[][] msgList=new Object[userIdList.size()*msgContentList.size()][3];
		int num=0;
		for(int userId:userIdList){
			for(String msgContent:msgContentList){
				msgList[num][0]=userId;
				msgList[num][1]=msgTile;
				msgList[num][2]=msgContent;
				num+=1;
			}
		}
		MessageOperation.batchInsert(conn,msgList, 0,"MAN");
	}
	*/
	public void update(long userId, String layerId,String wkt,String layerName)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();			
			//String updateSql = "update customised_layer set GEOMETRY=sdo_geometry('"+wkt+"',8307) where LAYER_ID="+layerId;			
			
			String baseSql = "update customised_layer set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			List<Object> values=new ArrayList();
			if (wkt!=null && StringUtils.isNotEmpty(wkt)){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" GEOMETRY=sdo_geometry('"+wkt+"',8307) ";
			};
			if (layerName!=null&& StringUtils.isNotEmpty(layerName)){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" LAYER_NAME='"+layerName+"'";
			};
			updateSql+=" where LAYER_ID="+layerId;
			run.update(conn, baseSql+updateSql);
			//发送消息
			/*重点区块变更
			 *1.所有生管角色
			 *2.重点区块所在城市的作业组组长(采集、日编、月编)
			 *重点区块变更:XXX(任务名称)信息发生变更,请关注*/
			try {
				if (wkt!=null && StringUtils.isNotEmpty(wkt)){
					Long cityId = null;
					if (layerName==null|| StringUtils.isEmpty(layerName)){
						String selectSql ="SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY,CREATE_USER_ID,CREATE_DATE,CITY_ID FROM CUSTOMISED_LAYER t"
								+ " where LAYER_ID="+layerId;
						List<HashMap> layerMap = query(selectSql, conn);
						if(layerMap !=null && layerMap.size()>0){
							layerName=(String) layerMap.get(0).get("layerName");
							cityId = (Long) layerMap.get(0).get("city");
						}
						
					}
					List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
					List<Long> groupIdList = new ArrayList<Long>();
					Map<String,Object> map = new HashMap<String, Object>();
					String msgTitle="重点区块变更";
					String msgContent = "重点区块变更:"+layerName+"信息发生变更,请关注";
					map.put("msgContent", msgContent);
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "LAYER");
					msgParam.put("relateObjectId", Long.parseLong(layerId));
					map.put("msgParam", msgParam.toString());
					msgContentList.add(map);
					//根据cityId查询task数据
					List<Map<String, Object>> taskList = TaskOperation.getTaskByCityId(conn, cityId, 1);
					for (Map<String, Object> task : taskList) {
						groupIdList.add((Long) task.get("monthEditGroupId"));
						//查询block分配的采集和日编作业组组长id
						if(task.get("taskId") != null){
							Map<String, Object> blockMan = TaskOperation.getBlockManByTaskId(conn, (long) task.get("taskId"), 1);
							if(blockMan != null){
								groupIdList.add((Long) blockMan.get("collectGroupId"));
								groupIdList.add((Long) blockMan.get("dayEditGroupId"));
							}
						}
						
					}
					if(msgContentList.size()>0){
						layerPushMsg(conn,msgTitle,msgContentList, groupIdList, userId);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				log.error(e.getMessage(), e);
				throw new Exception("发送失败，原因为:"+e.getMessage(),e);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void delete(long userId, String layerId)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();			
			String updateSql = "UPDATE customised_layer SET STATUS=0 where LAYER_ID="+layerId;			
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
			//发送消息
			/*重点区块删除
			 *1.所有生管角色
			 *2.重点区块所在城市的作业组组长(采集、日编、月编)
			 *重点区块删除:XXX(任务名称)已被删除,请关注*/
			try {
				Long cityId = null;
				String layerName = null;
				//查询layer数据
				String selectSql ="SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY,CREATE_USER_ID,CREATE_DATE,CITY_ID FROM CUSTOMISED_LAYER t"
						+ " where LAYER_ID="+layerId;
				List<HashMap> layerMap = query(selectSql, conn);
				if(layerMap !=null && layerMap.size()>0){
					layerName=(String) layerMap.get(0).get("layerName");
					cityId = (Long) layerMap.get(0).get("city");
				}
					
				List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
				List<Long> groupIdList = new ArrayList<Long>();
				Map<String,Object> map = new HashMap<String, Object>();
				String msgTitle="重点区块删除";
				String msgContent = "重点区块删除:"+layerName+"已被删除,请关注";
				map.put("msgContent", msgContent);
				//关联要素
				JSONObject msgParam = new JSONObject();
				msgParam.put("relateObject", "LAYER");
				msgParam.put("relateObjectId", Long.parseLong(layerId));
				map.put("msgParam", msgParam.toString());
				msgContentList.add(map);
				//根据cityId查询task数据
				List<Map<String, Object>> taskList = TaskOperation.getTaskByCityId(conn, cityId, 1);
				for (Map<String, Object> task : taskList) {
					groupIdList.add((Long) task.get("monthEditGroupId"));
					//查询block分配的采集和日编作业组组长id
					if(task.get("taskId") != null){
						Map<String, Object> blockMan = TaskOperation.getBlockManByTaskId(conn, (long) task.get("taskId"), 1);
						if(blockMan != null){
							groupIdList.add((Long) blockMan.get("collectGroupId"));
							groupIdList.add((Long) blockMan.get("dayEditGroupId"));
						}
					}
					
				}
				if(msgContentList.size()>0){
					layerPushMsg(conn,msgTitle,msgContentList, groupIdList, userId);
				}
			} catch (Exception e) {
				// TODO: handle exception
				log.error(e.getMessage(), e);
				throw new Exception("发送失败，原因为:"+e.getMessage(),e);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<HashMap> listByWkt(String wkt)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getManConnection();	
			
			String selectSql ="SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY,CREATE_USER_ID,CREATE_DATE FROM CUSTOMISED_LAYER t"
					+ " where SDO_ANYINTERACT(geometry,sdo_geometry('"+wkt+"',8307))='TRUE' AND T.STATUS=1";
			return this.query(selectSql, conn);
			/*ResultSetHandler<List<Layer>> rsHandler = new ResultSetHandler<List<Layer>>(){
				public List<Layer> handle(ResultSet rs) throws SQLException{
					List<Layer> result=new ArrayList<Layer>();
					while(rs.next()){
						Layer map = new Layer();
						map.setLayerId(rs.getInt("LAYER_ID"));
						map.setLayerName(rs.getString("LAYER_NAME"));
						map.setGeometry(rs.getString("GEOMETRY"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						result.add(map);
					}
					return result;
				}	    		
	    	};*/
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<HashMap> query(String selectSql,Connection conn) throws Exception{
		ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
			public List<HashMap> handle(ResultSet rs) throws SQLException {
				List<HashMap> list = new ArrayList<HashMap>();
				while(rs.next()){
					try {
						HashMap<String,Object> map = new HashMap<String,Object>();
						map.put("layerId", rs.getInt("LAYER_ID"));
						map.put("layerName", rs.getString("LAYER_NAME"));
						STRUCT struct=(STRUCT)rs.getObject("GEOMETRY");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", DateUtils.dateToString(rs.getTimestamp("CREATE_DATE")));
						map.put("cityId", rs.getLong("CITY_ID"));
						list.add(map);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return list;
			}
    	};
    	QueryRunner run = new QueryRunner();
		return run.query(conn,selectSql,rsHandler);
	}
	
	/**
	 * 查询layer列表
	 * 规划管理页面--重点区块图层--搜索(修改)
	 * @author Han Shaoming
	 * @param conditionJson
	 * @param orderJson
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public Page listAll(JSONObject conditionJson,JSONObject orderJson, int pageNum, int pageSize)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			String selectSql = "SELECT T.LAYER_ID,T.LAYER_NAME,T.GEOMETRY,T.CREATE_USER_ID,T.CREATE_DATE,U.USER_REAL_NAME "
					+ "FROM CUSTOMISED_LAYER T,USER_INFO U where T.CREATE_USER_ID=U.USER_ID AND T.STATUS=1";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("layerName".equals(key)) {selectSql+=" AND T.LAYER_NAME like '%"+conditionJson.getString(key)+"%'";}
					}
				}
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("layerId".equals(key)) {selectSql+=" ORDER BY T.LAYER_ID "+orderJson.getString(key);break;}
					if ("createDate".equals(key)) {selectSql+=" ORDER BY T.CREATE_DATE "+orderJson.getString(key);break;}
					}
			}else{
				selectSql+=" ORDER BY T.LAYER_ID";
			}
			//日志
			log.info("查询layer列表的sql:"+selectSql);
			Object[] params = {};
			Page page = queryRunner.query(pageNum, pageSize, conn, selectSql, new LayerWithPageHandler(pageNum, pageSize),params);
			return page;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 
	 * @ClassName LayerWithPageHandler
	 * @author Han Shaoming
	 * @date 2016年11月12日 下午3:32:26
	 * @Description TODO
	 */
	class LayerWithPageHandler implements ResultSetHandler<Page>{
		private int pageNum;
		private int pageSize;
		LayerWithPageHandler(int pageNum,int pageSize){
			this.pageNum=pageNum;
			this.pageSize=pageSize;
		}
		public Page handle(ResultSet rs) throws SQLException {
			Page page = new Page(pageNum);
			page.setPageSize(pageSize);
			int total = 0;
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				try {
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("layerId", rs.getInt("LAYER_ID"));
					map.put("layerName", rs.getString("LAYER_NAME"));
					STRUCT struct=(STRUCT)rs.getObject("GEOMETRY");
					try {
						String clobStr = GeoTranslator.struct2Wkt(struct);
						map.put("geometry", Geojson.wkt2Geojson(clobStr));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					map.put("createUserId", rs.getInt("CREATE_USER_ID"));
					map.put("createDate", DateUtils.dateToString(rs.getTimestamp("CREATE_DATE")));
					map.put("createUserName", rs.getString("USER_REAL_NAME"));
					list.add(map);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(total==0){
					total=rs.getInt("TOTAL_RECORD_NUM_");
				}
			}
			page.setResult(list);
			page.setTotalCount(total);
			return page;
		}
	}
	
	/*重点区块新增/变更/删除
	 * 1.所有生管角色
	 * 2.分配的月编作业组组长
	 * 重点区块:XXX(任务名称)内容发生变更，请关注*/
	public void layerPushMsg(Connection conn,String msgTitle,List<Map<String, Object>> msgContentList, List<Long> groupIdList, long pushUser) throws Exception {
		//查询所有生管角色
		String userSql="SELECT DISTINCT M.USER_ID FROM ROLE_USER_MAPPING M WHERE M.ROLE_ID =3";
		List<Integer> userIdList=UserInfoOperation.getUserListBySql(conn, userSql);
		//查询分配的作业组组长
		List<Long> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
		for (Long leaderId : leaderIdByGroupId) {
			userIdList.add(leaderId.intValue());
		}
		for(int userId:userIdList){
			//查询用户名称
			Map<String, Object> userInfo = UserInfoOperation.getUserInfoByUserId(conn, userId);
			String pushUserName = null;
			if(userInfo != null && userInfo.size() > 0){
				pushUserName = (String) userInfo.get("userRealName");
			}
			for(Map<String, Object> map:msgContentList){
				//发送消息到消息队列
				String msgContent = (String) map.get("msgContent");
				String msgParam = (String) map.get("msgParam");
				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser, new long[]{userId}, 2, msgParam, pushUserName);
			}
		}
		//发送邮件
		String toMail = null;
		String mailTitle = null;
		String mailContent = null;
		//查询用户详情
		for (int userId : userIdList) {
			Map<String, Object> userInfo = UserInfoOperation.getUserInfoByUserId(conn, userId);
			if(userInfo != null && userInfo.get("userEmail") != null){
				for (Map<String, Object> map : msgContentList) {
					//判断邮箱格式
					String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
	                Pattern regex = Pattern.compile(check);
	                Matcher matcher = regex.matcher((CharSequence) userInfo.get("userEmail"));
	                if(matcher.matches()){
	                	toMail = (String) userInfo.get("userEmail");
	                	mailTitle = msgTitle;
	                	mailContent = (String) map.get("msgContent");
	                	//发送邮件到消息队列
	                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
	                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
	                }
				}
			}
		}
	}
}
