package com.navinfo.dataservice.dao.glm.search.batch;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import oracle.sql.STRUCT;

public class PaGridIncreSearch {
	private static final Logger logger = Logger.getLogger(PaGridIncreSearch.class);
	/**
	 * 
	 * @param gridDateList
	 * @return data
	 * @throws Exception
	 */
	public Collection<IxPointaddress> getPaByGrids(Map<String,String> gridDateMap) throws Exception{
		Map<Long,IxPointaddress> results = new HashMap<Long,IxPointaddress>();//key:pid,value:obj
		Connection manConn = null;
		try {
			
			manConn = DBConnector.getInstance().getManConnection();
			//*********zl 将grids 放入clob 查询**
			
			Clob gridClob = ConnectionUtil.createClob(manConn);
			
			gridClob.setString(1, StringUtils.join(gridDateMap.keySet(), ","));
			
			String sql = "SELECT g.grid_id,r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id in (select column_value from table(clob_to_table(?))) ";
			logger.info(sql);
			Map<Integer,Collection<String>> dbGridMap = new QueryRunner().query(manConn, sql, new ResultSetHandler<Map<Integer,Collection<String>>>(){

				@Override
				public Map<Integer, Collection<String>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Collection<String>> map = new HashMap<Integer,Collection<String>>();
					while (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						List<String> gridList = new ArrayList<String>();
						if (map.containsKey(dbId)) {
							gridList = (List<String>) map.get(dbId);
						}
						gridList.add(rs.getString("grid_id"));
						map.put(dbId, gridList);
					}
					return map;
				}
				
			},gridClob);
			for(Integer dbId:dbGridMap.keySet()){
				logger.debug("starting load ixpointaddress from dbId:"+dbId);
				Map<String,String> subMap = new HashMap<String,String>();
				Collection<String> myGrids = dbGridMap.get(dbId);
				logger.debug("my grids is "+StringUtils.join(myGrids,","));
				for(String g:gridDateMap.keySet()){
					if(myGrids.contains(g)){
						subMap.put(g, gridDateMap.get(g));
					}
				}
				results.putAll(loadDateSingleDb(dbId,subMap));
			}
			return results.values();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(manConn);
		}
	}
	private Map<Long,IxPointaddress> loadDateSingleDb(int dbId,Map<String,String> gridDateMap)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			Map<Long,IxPointaddress> pas = null;
			for(String grid:gridDateMap.keySet()){
				logger.debug("starting load grid:"+grid+"from dbId:"+dbId);
				if(pas==null){
					pas = loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid));
				}else{
					pas.putAll(loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid)));
				}
			}
//			loadChildTables(conn,pois);
			return pas;
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(conn);
		}
	}
	/**
	 * 
	 * @param gridDate
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private Map<Long,IxPointaddress> loadDataSingleDbGrid(Connection conn,String grid,String date) throws Exception{
		
		Map<Long,IxPointaddress> pas = null;
		LogReader logReader = new LogReader(conn);
		
		if (!StringUtils.isEmpty(date)) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
				formatter.parse(date);
			} catch (Exception e) {
				logger.error("grid:"+grid+"对应的date错误:"+e.getMessage());
				date = "";
			}
			
		}
		
		if(StringUtils.isEmpty(date)){
			logger.info("load all pa，初始化u_record应为0");
			pas = loadIxPa(grid,conn);
			logger.info("load status");
			Map<Integer,Collection<Long>> paStatus = logReader.getUpdatedObj("IX_POINTADDRESS","IX_POINTADDRESS", grid, null);
			logger.info("begin set pa's u_record with poiStatus mapping");
			//load 变更poi的状态，设置u_record
			if(paStatus!=null && paStatus.size()>0){
				for(Integer status:paStatus.keySet()){
					for(Long pid:paStatus.get(status)){
						if(pas.containsKey(pid)){
							pas.get(pid).setuRecord(status);
						}
					}
				}
			}
		}else{
			logger.info("load status");
			Map<Integer,Collection<Long>> paStatus = logReader.getUpdatedObj("IX_POINTADDRESS","IX_POINTADDRESS", grid, date);
			logger.info("begin set pa's u_record with paStatus mapping");
			//load 
			pas = new HashMap<Long,IxPointaddress>();
			for(Integer status:paStatus.keySet()){
				Map<Long,IxPointaddress> result = loadIxPa(status,paStatus.get(status),conn);
				if(result!=null) pas.putAll(result);
			}
			//修正状态为作业季的新增删除修改状态
			Map<Long,Integer> ps = logReader.getObjectState(pas.keySet(),"IX_POINTADDRESS");
			for(Entry<Long, IxPointaddress> entry:pas.entrySet()){
				entry.getValue().setuRecord(ps.get(entry.getKey()));
			}
		}
		return pas;
	}
	
	/**
	 * @Title: loadIxPoi
	 * @Description: 全量下载 (修改)(第七迭代)(变更: 已删除的poi 也需要下载)
	 * @param grid
	 * @param conn
	 * @return
	 * @throws Exception  Map<Long,IxPoi>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月28日 下午7:23:21 
	 */
	@SuppressWarnings("static-access")
	private Map<Long,IxPointaddress> loadIxPa(String grid,Connection conn)throws Exception{
		Map<Long,IxPointaddress> pas = new HashMap<Long,IxPointaddress>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT IDCODE,DPR_NAME,DP_NAME,PID,guide_link_pid,x_guide,y_guide,MEMOIRE,GEOMETRY  ");
		sb.append(" FROM ix_pointaddress ");
		sb.append(" WHERE sdo_within_distance(geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		//*********2016.11.28 zl***********
		// 不下载已删除的点20161013
		//sb.append(" AND u_record!=2");
		//*********2016.11.28 zl***********
		logger.info("poi query sql:"+sb);
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			GridUtils gu = new GridUtils();
			String wkt = gu.grid2Wkt(grid);
			logger.info("grid wkt:"+wkt);
			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				IxPointaddress ixPa = new IxPointaddress();
				fillMainTable(ixPa,resultSet);
				
				Coordinate coord = ixPa.getGeometry().getCoordinate();
				String[] grids = CompGridUtil.point2Grids(coord.x,coord.y);
				boolean inside=false;
				for(String gridId : grids){
					if(gridId.equals(grid)){
						inside=true;
					}
				}
				if(!inside){
					continue;
				}
				Long pid = (long) ixPa.getPid();
				pas.put(pid, ixPa);
			}
			
			return pas;
		} catch(Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 增量下载
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private Map<Long,IxPointaddress> loadIxPa(int status,Collection<Long> pas,Connection conn)throws Exception{
		
		Clob pidClod = null;
		Map<Long,IxPointaddress> pasMap = new HashMap<Long,IxPointaddress>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT IDCODE,DPR_NAME,DP_NAME,PID,guide_link_pid,x_guide,y_guide,MEMOIRE,GEOMETRY  ");
		sb.append(" FROM ix_pointaddress ");
		
		pidClod = ConnectionUtil.createClob(conn);
		String pids = StringUtils.join(pas, ",");
		logger.info("pids"+pids);
		pidClod.setString(1, pids);
		sb.append(" WHERE pid in (select column_value from table(clob_to_table(?)))");
		logger.info("pa query sql:"+sb);
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setClob(1, pidClod);
			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				IxPointaddress ixPa = new IxPointaddress();
				fillMainTable(ixPa,resultSet);
				ixPa.setuRecord(status);
				Long pid = (long) ixPa.getPid();
				pasMap.put(pid, ixPa);
			}
			
			return pasMap;
		}catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 增量下载时
	 * @param ixPoi
	 * @param resultSet
	 * @throws Exception
	 */
	private void fillMainTable(IxPointaddress ixPa,ResultSet resultSet) throws Exception{
		
		ixPa.setPid(resultSet.getInt("pid"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct);

		ixPa.setGeometry(geometry);

		ixPa.setxGuide(resultSet.getDouble("x_guide"));

		ixPa.setyGuide(resultSet.getDouble("y_guide"));
		
		ixPa.setDprName(resultSet.getString("dpr_name"));
		
		ixPa.setDpName(resultSet.getString("dp_name"));

		ixPa.setGuideLinkPid(resultSet.getInt("guide_link_pid"));
		
		ixPa.setIdcode(resultSet.getString("idcode"));
		
		ixPa.setMemoire(resultSet.getString("memoire"));
		
	}

}