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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxChargingplotHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxChargingstationHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxGasstationHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxHotelHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxParkingHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiAddressHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiChildrenHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiContactHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiNameHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiParentHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxRestaurantHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxSamepoiHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.PoiEditStatusHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.PoiEvaluPlanHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.PoiFlagHandler;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class PoiGridIncreSearch {
	private static final Logger logger = Logger.getLogger(PoiGridIncreSearch.class);
	/**
	 * 
	 * @param gridDateList
	 * @return data
	 * @throws Exception
	 */
	public Collection<IxPoi> getPoiByGrids(Map<String,String> gridDateMap) throws Exception{
		Map<Long,IxPoi> results = new HashMap<Long,IxPoi>();//key:pid,value:obj
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
				logger.debug("starting load ixpoi from dbId:"+dbId);
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
			DBUtils.closeConnection(manConn);
		}
	}
	private Map<Long,IxPoi> loadDateSingleDb(int dbId,Map<String,String> gridDateMap)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			Map<Long,IxPoi> pois = null;
			for(String grid:gridDateMap.keySet()){
				logger.debug("starting load grid:"+grid+"from dbId:"+dbId);
				if(pois==null){
					pois = loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid));
				}else{
					pois.putAll(loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid)));
				}
			}
			loadChildTables(conn,pois);
			return pois;
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
	private Map<Long,IxPoi> loadDataSingleDbGrid(Connection conn,String grid,String date) throws Exception{
		
		Map<Long,IxPoi> pois = null;
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
			logger.info("load all poi，初始化u_record应为0");
			pois = loadIxPoi(grid,conn);
			logger.info("load status");
			Map<Integer,Collection<Long>> poiStatus = logReader.getUpdatedObj("IX_POI","IX_POI", grid, null);
			logger.info("begin set poi's u_record with poiStatus mapping");
			//load 变更poi的状态，设置u_record
			if(poiStatus!=null&&poiStatus.size()>0){
				for(Integer status:poiStatus.keySet()){
					for(Long pid:poiStatus.get(status)){
						if(pois.containsKey(pid)){
							pois.get(pid).setuRecord(status);
						}
					}
				}
			}
		}else{
			logger.info("load status");
			Map<Integer,Collection<Long>> poiStatus = logReader.getUpdatedObj("IX_POI","IX_POI", grid, date);
			logger.info("begin set poi's u_record with poiStatus mapping");
			//load 
			pois = new HashMap<Long,IxPoi>();
			for(Integer status:poiStatus.keySet()){
				Map<Long,IxPoi> result = loadIxPoi(status,poiStatus.get(status),conn);
				if(result!=null) pois.putAll(result);
			}
			//修正状态为作业季的新增删除修改状态
			Map<Long,Integer> ps = logReader.getObjectState(pois.keySet(),"IX_POI");
			for(Entry<Long, IxPoi> entry:pois.entrySet()){
				entry.getValue().setuRecord(ps.get(entry.getKey()));
			}
		}
		return pois;
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
	private Map<Long,IxPoi> loadIxPoi(String grid,Connection conn)throws Exception{
		Map<Long,IxPoi> pois = new HashMap<Long,IxPoi>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT poi_num,pid,mesh_id,kind_code,link_pid,x_guide,y_guide,post_code,open_24h,chain,u_record,geometry,\"LEVEL\",sports_venue,indoor,vip_flag,truck_flag  ");
		sb.append(" FROM ix_poi");
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
				IxPoi ixPoi = new IxPoi();
				fillMainTable(ixPoi,resultSet);
				
				Coordinate coord = ixPoi.getGeometry().getCoordinate();
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
				Long pid = (long) ixPoi.getPid();
				pois.put(pid, ixPoi);
			}
			
			return pois;
		} catch(Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 增量下载
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private Map<Long,IxPoi> loadIxPoi(int status,Collection<Long> pois,Connection conn)throws Exception{
		
//		if (status == 2) {
//			return new HashMap<Long,IxPoi>();
//		}
		
		Clob pidClod = null;
		Map<Long,IxPoi> poisMap = new HashMap<Long,IxPoi>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT poi_num,pid,mesh_id,kind_code,link_pid,x_guide,y_guide,post_code,open_24h,chain,u_record,geometry,\"LEVEL\",sports_venue,indoor,vip_flag,truck_flag  ");
		sb.append(" FROM ix_poi");
		pidClod = ConnectionUtil.createClob(conn);
		String pids = StringUtils.join(pois, ",");
		logger.info("pids"+pids);
		pidClod.setString(1, pids);
		sb.append(" WHERE pid in (select column_value from table(clob_to_table(?)))");
		logger.info("poi query sql:"+sb);
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setClob(1, pidClod);
			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				IxPoi ixPoi = new IxPoi();
				fillMainTable(ixPoi,resultSet);
				ixPoi.setuRecord(status);
				Long pid = (long) ixPoi.getPid();
				poisMap.put(pid, ixPoi);
			}
			
			
			return poisMap;
		}catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 增量下载时
	 * @param ixPoi
	 * @param resultSet
	 * @throws Exception
	 */
	private void fillMainTable(IxPoi ixPoi,ResultSet resultSet) throws Exception{
		
		ixPoi.setPid(resultSet.getInt("pid"));

		ixPoi.setKindCode(resultSet.getString("kind_code"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct);

		ixPoi.setGeometry(geometry);

		ixPoi.setxGuide(resultSet.getDouble("x_guide"));

		ixPoi.setyGuide(resultSet.getDouble("y_guide"));

		ixPoi.setLinkPid(resultSet.getInt("link_pid"));
		
		ixPoi.setPoiNum(resultSet.getString("poi_num"));
		
		ixPoi.setMeshId(resultSet.getInt("mesh_id"));
		
		ixPoi.setPostCode(resultSet.getString("post_code"));
		
		ixPoi.setOpen24h(resultSet.getInt("open_24h"));
		
		ixPoi.setChain(resultSet.getString("chain"));
		
		ixPoi.setuRecord(resultSet.getInt("u_record"));
		
		ixPoi.setLevel(resultSet.getString("level"));
		
		ixPoi.setSportsVenue(resultSet.getString("sports_venue"));
		
		ixPoi.setIndoor(resultSet.getInt("indoor"));
		
		ixPoi.setVipFlag(resultSet.getString("vip_flag"));
		
		ixPoi.setTruckFlag(resultSet.getInt("truck_flag"));
		
	}
	
	/**
	 * @Title: loadChildTables
	 * @Description: 下载子表数据 (修)(第七迭代) (变更: 增加查询 samepoi  子表数据)
	 * @param conn
	 * @param pois
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月29日 下午5:57:33 
	 */
	private void loadChildTables(Connection conn,Map<Long,IxPoi> pois)throws Exception{
		
		Collection<Long> pids = pois.keySet();
		
		QueryRunner run = new QueryRunner();
		
		Clob pidsClob = ConnectionUtil.createClob(conn);
		
		pidsClob.setString(1, StringUtils.join(pids, ","));
		
		//***********zl 2016.12.27**************
		Clob pidsClob1 = ConnectionUtil.createClob(conn);//所有未删除的poi的pid 的clob
		Clob pidsClob_del = ConnectionUtil.createClob(conn);//所有删除的poi的pid 的 clob
		logger.info("查询主表IX_POI中,u_record != 2 的 pid");
		String poi_sql = " select pid from ix_poi where  pid in (select to_number(column_value) from table(clob_to_table(?))) and u_record != 2";
		//List<Integer> poiList1 = new ArrayList<Integer>();
		ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>(){
			public List<Integer> handle(ResultSet rs) throws SQLException {
				List<Integer> list1 = new ArrayList<Integer>();
				while(rs.next()){
					Integer pid = rs.getInt("pid");
					list1.add(pid);
				}
				return list1;
			}
    	};
    	
    	List<Integer> poiList1 = run.query(conn, poi_sql,rsHandler,pidsClob);
    	pidsClob1.setString(1, StringUtils.join(poiList1, ","));
		logger.info("查询主表IX_POI中,u_record = 2 的 pid");
		String poi_delete_sql = " select pid from ix_poi where  pid in (select to_number(column_value) from table(clob_to_table(?))) and u_record = 2";
    	
		List<Integer> poiList_del = run.query(conn, poi_delete_sql,rsHandler,pidsClob);
		pidsClob_del.setString(1, StringUtils.join(poiList_del, ","));
		
		
		logger.info("设置子表IX_POI_NAME");
		Map<Long,List<IRow>> names = null;
		Map<Long,List<IRow>> names_delete = null;
		//当主表不是逻辑删除时,子表IX_POI_NAME也不下载逻辑删除的
		String sql="select * from ix_poi_name where "
				+ "u_record !=2 and "
				+ "name_class=1 and name_type=2 and lang_code='CHI' and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		names = run.query(conn, sql, new IxPoiNameHandler(),pidsClob1);

		for(Long pid:names.keySet()){
			pois.get(pid).setNames(names.get(pid));
		}
		//当主表是逻辑删除时,子表IX_POI_NAME下载逻辑删除的最新一条记录
		String sql_del="select * from ("
				+ "select row_number() over(partition by poi_pid order by u_date desc) rn, a.*  "
				+ "from ix_poi_name a  "
				+ "where  name_class=1 and name_type=2 and lang_code='CHI' "
				+ "and poi_pid in (select to_number(column_value) from table(clob_to_table(?))) ) where rn = 1";
		names_delete = run.query(conn, sql_del, new IxPoiNameHandler(),pidsClob_del);
		for(Long pid:names_delete.keySet()){
			pois.get(pid).setNames(names_delete.get(pid));
		}
		//*************************************

		logger.info("设置子表IX_POI_ADDRESS");
		
		 sql="select * from ix_poi_address where "
				 + "u_record !=2 and "
				+ "name_groupid=1 and lang_code='CHI' and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> addresses = run.query(conn, sql, new IxPoiAddressHandler(),pidsClob);

		for(Long pid:addresses.keySet()){
			pois.get(pid).setAddresses(addresses.get(pid));
		}
		//当主表是逻辑删除时,子表IX_POI_ADDRESS下载逻辑删除的最新一条记录
		String sql_del_address="select * from ("
				+ "select row_number() over(partition by poi_pid order by u_date desc) rn, a.*  "
				+ "from ix_poi_address a  "
				+ "where  name_groupid=1 and lang_code='CHI' "
				+ "and poi_pid in (select to_number(column_value) from table(clob_to_table(?))) ) where rn = 1";
		logger.info(sql_del_address);
		Map<Long,List<IRow>> address_delete = run.query(conn, sql_del_address, new IxPoiAddressHandler(),pidsClob_del);
		for(Long pid:address_delete.keySet()){
			pois.get(pid).setAddresses(address_delete.get(pid));
		}
		//*************************************
		
		logger.info("设置子表IX_POI_PARENT");
		
		StringBuilder sbParent = new StringBuilder();
		sbParent.append("WITH A AS(");
		sbParent.append(" SELECT CH.GROUP_ID,CH.CHILD_POI_PID,CH.RELATION_TYPE,P.PARENT_POI_PID FROM IX_POI_CHILDREN CH,IX_POI_PARENT P WHERE CH.GROUP_ID=P.GROUP_ID AND CH.CHILD_POI_PID IN (select to_number(column_value) PID from table(clob_to_table(?))) "
				+ "AND CH.U_RECORD!=2 "
				+ "");
		sbParent.append(" ),");
		sbParent.append(" B AS(");
		sbParent.append(" SELECT CHILD_POI_PID,MIN(PARENT_POI_PID) P_PID FROM A WHERE RELATION_TYPE=2 GROUP BY CHILD_POI_PID");
		sbParent.append(" ),");
		sbParent.append(" C AS(");
		sbParent.append(" SELECT CHILD_POI_PID,MIN(PARENT_POI_PID) P_PID FROM A WHERE NOT EXISTS(SELECT 1 FROM B WHERE A.CHILD_POI_PID=B.CHILD_POI_PID) GROUP BY CHILD_POI_PID");
		sbParent.append(" )");
		sbParent.append(" SELECT B.CHILD_POI_PID,P.POI_NUM FROM B,IX_POI P WHERE B.P_PID=P.PID");
		sbParent.append(" UNION ALL");
		sbParent.append(" SELECT C.CHILD_POI_PID,P.POI_NUM FROM C,IX_POI P WHERE C.P_PID=P.PID");
		
		Map<Long,List<IRow>> parent = run.query(conn, sbParent.toString(), new IxPoiParentHandler(),pidsClob);

		for(Long pid:parent.keySet()){
			pois.get(pid).setParents(parent.get(pid));
		}
		
		logger.info("设置子表IX_POI_CHILDREN");
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT p.parent_poi_pid,c.child_poi_pid,c.relation_type,c.row_id,");
		sb.append("(select poi_num from ix_poi where pid=c.child_poi_pid) poi_num");
		sb.append(" FROM ix_poi_parent p");
		sb.append(" ,ix_poi_children c");
		sb.append(" WHERE p.group_id=c.group_id");
		sb.append(" AND p.parent_poi_pid in (select to_number(column_value) from table(clob_to_table(?)))");
		sb.append(" AND c.u_record !=2");
		
		Map<Long,List<IRow>> children = run.query(conn, sb.toString(), new IxPoiChildrenHandler(),pidsClob);

		for(Long pid:children.keySet()){
			pois.get(pid).setChildren(children.get(pid));
		}
		
		logger.info("设置子表IX_POI_CONTACT");
		
		sql="select * from ix_poi_contact where "
				+ "u_record!=2 and "
				+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> contact = run.query(conn, sql, new IxPoiContactHandler(),pidsClob);

		for(Long pid:contact.keySet()){
			pois.get(pid).setContacts(contact.get(pid));
		}
		
		logger.info("设置子表IX_POI_RESTAURANT");
		
		sql="select * from ix_poi_restaurant WHERE "
				+ "u_record !=2 and "
				+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> restaurant = run.query(conn, sql, new IxRestaurantHandler(),pidsClob);

		for(Long pid:restaurant.keySet()){
			pois.get(pid).setRestaurants(restaurant.get(pid));
		}
		
		logger.info("设置子表IX_POI_PARKING");
		
		sql="select * from ix_poi_parking WHERE "
				+ "u_record !=2 and "
				+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> parking = run.query(conn, sql, new IxParkingHandler(),pidsClob);

		for(Long pid:parking.keySet()){
			pois.get(pid).setParkings(parking.get(pid));
		}
		
		logger.info("设置子表IX_POI_HOTEL");
		
		sql="select * from ix_poi_hotel WHERE "
				+ "u_record !=2 and "
				+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> hotel = run.query(conn, sql, new IxHotelHandler(),pidsClob);

		for(Long pid:hotel.keySet()){
			pois.get(pid).setHotels(hotel.get(pid));
		}
		
		logger.info("设置子表IX_POI_GASSTATION");
		
		sql="select * from ix_poi_gasstation WHERE "
				+ "u_record !=2 and "
				+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> gasstation = run.query(conn, sql, new IxGasstationHandler(),pidsClob);

		for(Long pid:gasstation.keySet()){
			pois.get(pid).setGasstations(gasstation.get(pid));
		}
		
		logger.info("设置子表IX_POI_CHARGINGSTATION");
		
		 sql = "select * from ix_poi_chargingstation WHERE "
		 		+ "u_record !=2 and "
		 		+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		 Map<Long,List<IRow>> chargingstation = run.query(conn, sql, new IxChargingstationHandler(),pidsClob);

		for(Long pid:chargingstation.keySet()){
			pois.get(pid).setChargingstations(chargingstation.get(pid));
		}
		
		logger.info("设置子表IX_POI_CHARGINGPLOT");
		
		 sql = "select * from ix_poi_chargingplot WHERE "
		 		+ "u_record !=2 and "
		 		+ "poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		 Map<Long,List<IRow>> chargingplot = run.query(conn, sql, new IxChargingplotHandler(),pidsClob);

		for(Long pid:chargingplot.keySet()){
			pois.get(pid).setChargingplots(chargingplot.get(pid));
		}
		
		logger.info("设置  samefid");
		
		StringBuilder sbSamepoi = new StringBuilder();
		sbSamepoi.append("WITH q1 as(");
		sbSamepoi.append(" select s.group_id,p.group_id,p.poi_pid pid, ");
		sbSamepoi.append(" (select pp.poi_pid  from ix_samepoi_part pp where pp.group_id = p.group_id and pp.poi_pid != p.poi_pid)  otherpid  ");
		sbSamepoi.append(" from ix_samepoi s , ix_samepoi_part p  ");
		sbSamepoi.append(" where s.group_id = p.group_id ");
		sbSamepoi.append(" and p.poi_pid in (select to_number(column_value) from table(clob_to_table(?))) ");
		sbSamepoi.append(" and s.u_record != 2 and p.u_record != 2 ");//新增条件 ,不下载已删除的same poi 
		sbSamepoi.append(" ) ");
		sbSamepoi.append(" select q.pid,nvl(i.poi_num,'') poi_num from ix_poi i, q1 q where i.pid=q.otherpid ");
		logger.debug(pidsClob.getSubString((long)1,(int)pidsClob.length()));
		System.out.println("samefid sql :" + sbSamepoi.toString());
		Map<Long,String> sameFidMap = run.query(conn, sbSamepoi.toString(), new IxSamepoiHandler(),pidsClob);
		for(Long pid:sameFidMap.keySet()){
			pois.get(pid).setSameFid(sameFidMap.get(pid));
		}
		
		logger.info("设置 字段 editstatus");
		StringBuilder sbEditStatus = new StringBuilder();
		sbEditStatus.append("select distinct t.pid,t.commit_his_status from POI_EDIT_STATUS t ");
		sbEditStatus.append(" where t.pid in (select to_number(column_value) from table(clob_to_table(?))) "
							+ " and t.pid is not null");
		logger.debug("editstatus sql :" + sbEditStatus.toString());
		
		Map<Long,Integer> editStatus = run.query(conn, sbEditStatus.toString(), new PoiEditStatusHandler(),pidsClob);
		for(Long pid:editStatus.keySet()){
			pois.get(pid).setPoiEditStatus(editStatus.get(pid));
		}
		
		//*************
		logger.info("设置 字段 evaluPlan");
		StringBuilder sbEvaluPlan = new StringBuilder();
		sbEvaluPlan.append("SELECT * FROM (");
		sbEvaluPlan.append(" SELECT ROW_NUMBER() OVER(PARTITION BY p.pid ORDER BY p.is_plan_selected DESC) rn, "
				+ " p.pid,p.is_plan_selected,p.is_important   FROM data_plan p where p.data_type = 1 and p.pid in (select to_number(column_value) from table(clob_to_table(?)))  ");
		sbEvaluPlan.append("  )  WHERE rn = 1 ");
		logger.debug("sbEvaluPlan sql :" + sbEvaluPlan.toString());
		
		Map<Long,Integer> evaluPlan = run.query(conn, sbEvaluPlan.toString(), new PoiEvaluPlanHandler(),pidsClob);
		for(Long pid:evaluPlan.keySet()){
			pois.get(pid).setEvaluPlan(evaluPlan.get(pid));
		}
		
		logger.info("设置子表POI_flag");
		
		 sql = "select p.pid ,p.src_record,p.field_verified from poi_flag p where  "
		 		+ " pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		 Map<Long,List<IRow>> poiFlags = run.query(conn, sql, new PoiFlagHandler(),pidsClob);

		for(Long pid:poiFlags.keySet()){
			pois.get(pid).setPoiFlag(poiFlags.get(pid));
		}
			
	}
	public Map<String,Integer> getRegionIdTaskIdBySubtaskId(int subtaskId) throws Exception {
		Connection manConn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			//String sql = " select distinct s.subtask_id,s.task_id,t.region_id,r.daily_db_id from subtask s,task t,region r where s.task_id = t.task_id and t.region_id = r.region_id  and s.subtask_id = ? ";
			String sql =" select distinct s.subtask_id,s.task_id,t.region_id,r.daily_db_id ,c.admin_id from subtask s,task t,program p,city c,region r  where s.task_id =t.task_id and  t.program_id  = p.program_id and p.city_id = c.city_id and t.region_id = r.region_id  and s.subtask_id= ? ";
			logger.info(sql);
			Map<String,Integer> map = new QueryRunner().query(manConn, sql, new ResultSetHandler<Map<String,Integer>>(){

				@Override
				public Map<String,Integer> handle(ResultSet rs) throws SQLException {
					Map<String,Integer> map = new HashMap<String,Integer>();
					while (rs.next()) {
						int taskId = rs.getInt("task_id");
						int regionId = 0;//rs.getInt("region_id");
						int dayDbId = rs.getInt("daily_db_id");
						String adminId = rs.getString("admin_id");
						if(adminId.length() > 2){
							adminId = adminId.substring(0,2);
						}
						regionId = Integer.parseInt(adminId);
						map.put("taskId", taskId);
						map.put("regionId", regionId);
						map.put("dayDbId", dayDbId);
					}
					return map;
				}
				
			},subtaskId);
			
			return map;
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DBUtils.closeConnection(manConn);
		}
	}
	
	public Set<Integer> getPidsByTaskId(int taskId, int dbId) throws Exception {
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			String sql = " select distinct p.pid  from data_plan p where p.data_type = 2  and p.is_plan_selected = 0 and p.task_id = ? ";
			logger.info(sql);
			Set<Integer> PidSet = new QueryRunner().query(conn, sql, new ResultSetHandler<Set<Integer>>(){
				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> set = new HashSet<Integer>();
					while (rs.next()) {
						set.add(rs.getInt("pid"));
					}
					return set;
				}
				
			},taskId);
			return PidSet;
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(conn);
		}
	}
}