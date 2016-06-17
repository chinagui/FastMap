package com.navinfo.dataservice.engine.man.layer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Layer;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.Page;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/** 
* @ClassName:  CustomisedLayerService 
* @author code generator
* @date 2016-06-13 05:53:14 
* @Description: TODO
*/
@Service
public class LayerService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(long userId,String wkt)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();			
			String createSql = "insert into customised_layer (LAYER_ID, GEOMETRY, CREATE_USER_ID, CREATE_DATE) "
					+ "values(customised_layer_seq.nextval,sdo_geometry('"+wkt+"',8307),"+userId+",sysdate)";			
			DbOperation.exeUpdateOrInsertBySql(conn, createSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(String layerId,String wkt)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();			
			String updateSql = "update customised_layer set GEOMETRY=sdo_geometry('"+wkt+"',8307) where LAYER_ID="+layerId;			
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
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
			String updateSql = "delete from customised_layer where LAYER_ID="+layerId;			
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<Layer> listByWkt(String wkt)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getManConnection();	
			
			String selectSql ="SELECT LAYER_ID,LAYER_NAME,T.GEOMETRY.GET_WKT() as GEOMETRY,CREATE_USER_ID,CREATE_DATE FROM CUSTOMISED_LAYER t"
					+ " where SDO_ANYINTERACT(geometry,sdo_geometry('"+wkt+"',8307))='TRUE'";
			ResultSetHandler<List<Layer>> rsHandler = new ResultSetHandler<List<Layer>>(){
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
	    	};
	    	QueryRunner run = new QueryRunner();
			return run.query(conn,selectSql,rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
