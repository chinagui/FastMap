package com.navinfo.dataservice.engine.man.mqmsg;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 同步消费消息
 * 
 * @ClassName: InfoChangeMsgHandler
 * @author Xiao Xiaowen
 * @date 2016年6月25日 上午10:42:43
 * @Description: TODO
 * 
 */
public class InfoChangeMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	String sql = "INSERT INTO INFOR(INFOR_ID,INFOR_NAME,GEOMETRY,INFOR_LEVEL,PLAN_STATUS,INFOR_CONTENT) "
			+ "VALUES (?,?,?,?,0,?)";

	@Override
	public void handle(String message) {
		try {
			// 解析保存到man库infor表中
			save(message);
		} catch (Exception e) {
			log.warn("接收到info_change消息,但保存失败，该消息已消费。message：" + message);
			log.error(e.getMessage(), e);

		}
	}
	
	private List<String> gridsFilter(Connection conn,String gridId,String[] gridIds) throws Exception{
		try {
			QueryRunner run = new QueryRunner();
			
			String gridIdsStr = StringUtils.join(gridIds, ",");
			
			String selectSql = "SELECT BGM2.GRID_ID"
					+ " FROM BLOCK_GRID_MAPPING BGM2"
					+ " WHERE BGM2.GRID_ID IN (" + gridIdsStr + ")"
					+ " AND BGM2.BLOCK_ID = (SELECT BGM.BLOCK_ID FROM BLOCK_GRID_MAPPING BGM,BLOCK B WHERE BGM.BLOCK_ID = B.BLOCK_ID AND B.CITY_ID = 100002 AND BGM.GRID_ID = " + gridId +")";
			
			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> gridIdlist = new ArrayList<String>();
					while (rs.next()) {
						gridIdlist.add(rs.getString("GRID_ID"));
					}
					return gridIdlist;
				}

			};

			return run.query(conn, selectSql, rsHandler);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	public void save(String message) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			Clob c = ConnectionUtil.createClob(conn);
			JSONObject dataJson = JSONObject.fromObject(message);
			String inforGeo = dataJson.getString("geometry");
			String inforId = dataJson.getString("rowkey");
			c.setString(1, inforGeo);
			List<Object> values = new ArrayList<Object>();
			values.add(inforId);
			values.add(dataJson.getString("INFO_NAME"));
			values.add(c);
			values.add(dataJson.getString("i_level"));
			values.add(dataJson.getString("INFO_CONTENT"));
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, values.toArray());
			
			//初始化infor_grid_mapping关系表
			String insertSql = "INSERT INTO infor_grid_mapping(infor_id,grid_id) VALUES(?,?)";
			String[] inforGeoList = inforGeo.split(";");
			Set<String> gridsAfter = new HashSet<String>(); 
			for (String geoTmp : inforGeoList) {
				Geometry inforTmp = GeoTranslator.wkt2Geometry(geoTmp);
				Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(inforTmp);
				//grid扩圈
				for(Iterator<String> gridsItr = (Iterator<String>)grids.iterator();gridsItr.hasNext();)  
		        {              
					String gridId = gridsItr.next();
					String[] gridAfter = GridUtils.get9NeighborGrids(gridId);
					List<String> gridIdlist = gridsFilter(conn,gridId,gridAfter);					
					for(int i=0;i<gridIdlist.size();i++){
						gridsAfter.add(gridIdlist.get(i));
					}           
		        } 		
			}
			
			Iterator<String> it = (Iterator<String>) gridsAfter.iterator();
			int num=0;
			while (it.hasNext()) {
				List<Object> tmpObjects = new ArrayList<Object>();
				tmpObjects.add(inforId);
				tmpObjects.add(Integer.parseInt(it.next()));
				run.update(conn, insertSql, tmpObjects.toArray());
				//inforGridValues[num]=tmpObjects;
				num=num+1;
			}
			conn.commit();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public static void main(String[] args) {
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "dubbo-consumer.xml"});
			context.start();
			new ApplicationContextUtil().setApplicationContext(context);
			final InfoChangeMsgHandler sub = new InfoChangeMsgHandler();
			String message = "{\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);LINESTRING (116.1 40.43,116.64 40.30);POLYGON((116.625 40.25,116.625 40.333333,116.75 40.333333,116.625 40.25));\",\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"i_level\":2,\"b_sourceCode\":1,\"b_sourceId\":\"sfoiuojkw89234jkjsfjksf\",\"b_reliability\":3,\"INFO_NAME\":\"道路通车\",\"INFO_CONTENT\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\"}";
//			String message = "{\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"i_level\":2,\"b_sourceCode\":1,\"b_sourceId\":\"sfoiuojkw89234jkjsfjksf\",\"b_reliability\":3,\"INFO_NAME\":\"道路通车\",\"INFO_CONTENT\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\"}";
			sub.save(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
