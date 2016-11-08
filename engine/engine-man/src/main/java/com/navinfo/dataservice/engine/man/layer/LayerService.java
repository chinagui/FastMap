package com.navinfo.dataservice.engine.man.layer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.navinfo.navicommons.database.DataBaseUtils;
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
		try{
			conn = DBConnector.getInstance().getManConnection();			
			String createSql = "insert into customised_layer (LAYER_ID, LAYER_NAME,GEOMETRY, CREATE_USER_ID, CREATE_DATE,STATUS) "
					+ "values(customised_layer_seq.nextval,'"+layerName+"',sdo_geometry('"+wkt+"',8307),"+userId+",sysdate,1)";			
			DbOperation.exeUpdateOrInsertBySql(conn, createSql);
			
			String msgTitle="重点区块创建";
			List<String> msgContentList=new ArrayList<String>();
			msgContentList.add("重点区块:"+layerName+"内容发生变更，请关注");
			layerPushMsg(conn,msgTitle,msgContentList);
			
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
	
	public void update(String layerId,String wkt,String layerName)throws Exception{
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
			if (wkt!=null && StringUtils.isNotEmpty(wkt)){
				String msgTitle="重点区块修改";
				if (layerName==null|| StringUtils.isEmpty(layerName)){
					String selectSql ="SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY,CREATE_USER_ID,CREATE_DATE FROM CUSTOMISED_LAYER t"
							+ " where LAYER_ID="+layerId;
					List<HashMap> layerMap = query(selectSql, conn);
					layerName=(String) layerMap.get(0).get("layerName");
				};
				List<String> msgContentList=new ArrayList<String>();
				msgContentList.add("重点区块:"+layerName+"内容发生变更，请关注");
				layerPushMsg(conn,msgTitle,msgContentList);
			};
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void delete(String layerId)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();			
			String updateSql = "UPDATE customised_layer SET STATUS=0 where LAYER_ID="+layerId;			
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
			String msgTitle="重点区块删除";
			String selectSql ="SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY,CREATE_USER_ID,CREATE_DATE FROM CUSTOMISED_LAYER t"
						+ " where LAYER_ID="+layerId;
			List<HashMap> layerMap = query(selectSql, conn);
			String layerName=(String) layerMap.get(0).get("layerName");
			List<String> msgContentList=new ArrayList<String>();
			msgContentList.add("重点区块:"+layerName+"内容发生变更，请关注");
			layerPushMsg(conn,msgTitle,msgContentList);
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
	
	public List<HashMap> listAll(JSONObject conditionJson,JSONObject orderJson)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY,CREATE_USER_ID,CREATE_DATE FROM CUSTOMISED_LAYER t where T.STATUS=1";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("layerName".equals(key)) {selectSql+=" and T.LAYER_NAME like '%"+conditionJson.getString(key)+"%'";}
					}
				}
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("layerId".equals(key)) {selectSql+=" order by T.LAYER_ID "+orderJson.getString(key);break;}
					if ("createDate".equals(key)) {selectSql+=" order by T.CREATE_DATE "+orderJson.getString(key);break;}
					}
			}else{
				selectSql+=" order by T.LAYER_ID";
			}
			return this.query(selectSql, conn);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
