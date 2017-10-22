package com.navinfo.dataservice.scripts.refinement;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlagMethod;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;


public class PoiTaskTabLogDependent {

	static String deletedSql = "UPDATE  POI_TASK_TAB SET DELFLAG = 'T' where PID in (select column_value from table(clob_to_table(?)))";
	static String updatedSql = "UPDATE  POI_TASK_TAB SET MOVEFLAG= ? ,LOG = ? where PID = ? ";
	static String insertedSql = "UPDATE  POI_TASK_TAB SET ADDFLAG = 'T' where PID in (select column_value from table(clob_to_table(?))) ";

	static PreparedStatement perstmtDeleted = null;
	static PreparedStatement perstmtUpdated = null;
	static PreparedStatement perstmtInserted = null;


	public void refinementLogDependentMain(int dbId) throws Exception{
		Connection conn = null;
		try{
			DbInfo dbInfo = DbService.getInstance().getDbById(dbId);

			OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
			conn = schema.getPoolDataSource().getConnection();
			createTable(conn);
			run(conn,null,null,null,0);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException(e.getMessage());
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
	
	public void createTable(Connection conn) throws SQLException{
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('REFINED_LOG_DEPENDENT') ;    ");
			sb.append("    if num > 0 then                                                                                  ");
			sb.append("        execute immediate 'drop table REFINED_LOG_DEPENDENT' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			
			r.execute(conn, sb.toString());
			sb.delete( 0, sb.length() );
			sb.append("create table REFINED_LOG_DEPENDENT     ");
			sb.append("(                                      ");
			sb.append("  pid     NUMBER(10) not null,         ");
			sb.append("  delflag NUMBER(1) default 0 not null,");
			sb.append("  moveflag NUMBER(1) default 0 not null,");
			sb.append("  addflag  NUMBER(1) default 0 not null,");
			sb.append("  log     VARCHAR2(1000)               ");
			sb.append(")                                      ");
			r.execute(conn, sb.toString());
	}

	
	public static void run(Connection conn,Collection<Long> objPids, String start_date,String end_date, Integer dDbID) throws Exception{
		Connection dDbConn = null;
		try{
			//获取大区库连接
			dDbConn = DBConnector.getInstance().getConnectionById(dDbID);
			//在大区库中读履历获取增删改的pid
			LogReader logReader = new LogReader(dDbConn);
			
			Map<Integer,Collection<Long>> pidMap = logReader.getUpdatedObj("IX_POI", "IX_POI", null, start_date, end_date,objPids);

			//新增
			Collection<Long> inserted = pidMap.get(1);
			if(inserted!=null && inserted.size() > 0){
				if(perstmtInserted==null){
					perstmtInserted = conn.prepareStatement(insertedSql);
				}
				if(perstmtInserted!=null){
					Clob instPidsClob = ConnectionUtil.createClob(conn);
					instPidsClob.setString(1, StringUtils.join(inserted, ","));
					perstmtInserted.setClob(1, instPidsClob);
					perstmtInserted.execute();
					conn.commit();
				}
			}

			//删除
			Collection<Long> deleted = pidMap.get(2);
			if(deleted!=null&&!deleted.isEmpty()){
				if(perstmtDeleted==null){
					perstmtDeleted = conn.prepareStatement(deletedSql);
				}
				if(perstmtDeleted!=null){
					Clob deletePidsClob = ConnectionUtil.createClob(conn);
					deletePidsClob.setString(1, StringUtils.join(deleted, ","));
					perstmtDeleted.setClob(1, deletePidsClob);
					perstmtDeleted.execute();
					conn.commit();
				}
			}
			
			//修改
			Collection<Long> updated = pidMap.get(3);
			if(updated!=null&&!updated.isEmpty()){
				//去大区库中查询履历
				perstmtUpdated = generate(conn,dDbConn,OperationType.UPDATE,updated,updatedSql,perstmtUpdated);
			}
			
			if(perstmtUpdated!=null){
				perstmtUpdated.executeBatch();
				conn.commit();
			}
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} 
		finally{
			DbUtils.closeQuietly(dDbConn);
			DbUtils.closeQuietly(perstmtDeleted);
			DbUtils.closeQuietly(perstmtInserted);
			DbUtils.closeQuietly(perstmtUpdated);
		}
	}

	/**
	 * @param conn 
	 * @param dDbConn 
	 * @param opType 
	 * @param pidCollection
	 * @param sql
	 * @param perstmt
	 * @throws Exception 
	 */
	private static PreparedStatement generate(Connection conn, Connection dDbConn, OperationType opType, Collection<Long> pidCollection, String sql, PreparedStatement perstmt) throws Exception {
		Set<Long> ixSamePoiLogSet = null;
		//根据pid 集合获取 ix_samePoi_part 中的 groupId 集合
		Set<Long> groupIds = queryGroupIdsBypids(dDbConn,pidCollection);
		if(groupIds != null && groupIds.size() > 0){
			//加载同一关系对象
			Set<String> subTbNames = new HashSet<String>();
			subTbNames.add("IX_SAMEPOI_PART");
			Map<Long,BasicObj> updatedSamePoiObjs = ObjBatchSelector.selectByPids(dDbConn, "IX_SAMEPOI", subTbNames, false, groupIds, false, true);
			Map<Long, List<LogDetail>> ixSamePoiLogDetailMap = PoiLogDetailStat.loadAllLog(dDbConn, updatedSamePoiObjs.keySet());
			
			if(updatedSamePoiObjs != null && updatedSamePoiObjs.size() > 0){
				ixSamePoiLogSet = new HashSet<Long>();
				for(Map.Entry<Long, BasicObj> entry:updatedSamePoiObjs.entrySet()){
					IxSamePoiObj ixSamePoi = (IxSamePoiObj) entry.getValue();
					ObjHisLogParser.parse(ixSamePoi, ixSamePoiLogDetailMap.get(entry.getKey()));
					//改同一关系:
					//删除
					if(ixSamePoi.isDelSamepoiPart() > 0){
						ixSamePoiLogSet.add(ixSamePoi.isDelSamepoiPart());
					}
					//新增修改
					ixSamePoi.getSubRowByName("samepoiParts");
					if(ixSamePoi.getSubRowByName("samepoiParts") != null && ixSamePoi.getSubRowByName("samepoiParts").size() > 0 ){
						for(BasicRow samePoiBr : ixSamePoi.getSubRowByName("samepoiParts")){
							IxSamepoiPart ixSamePoiBr = (IxSamepoiPart) samePoiBr;
							if(ixSamePoiBr.isHisChanged()){
								ixSamePoiLogSet.add(ixSamePoiBr.getPoiPid());
							}
						}
					}
				}
			}
		}
		//加载IXPOI对象
		Set<String> subPoiTbNames = new HashSet<String>();
			subPoiTbNames.add("IX_POI_CONTACT");
			subPoiTbNames.add("IX_POI_NAME");
			subPoiTbNames.add("IX_POI_ADDRESS");
			subPoiTbNames.add("IX_POI_RESTAURANT");
			subPoiTbNames.add("IX_POI_HOTEL");
			subPoiTbNames.add("IX_POI_CHILDREN");
			subPoiTbNames.add("IX_POI_FLAG_METHOD");
			
			
		Map<Long,BasicObj> updatedObjs = ObjBatchSelector.selectByPids(dDbConn, "IX_POI", subPoiTbNames, false, pidCollection, false, true);
		Map<Long, List<LogDetail>> logDetailMap = PoiLogDetailStat.loadAllLog(dDbConn, updatedObjs.keySet());
		for(Map.Entry<Long, BasicObj> entry:updatedObjs.entrySet()){
			IxPoiObj ixPoi= (IxPoiObj) entry.getValue();
			ObjHisLogParser.parse(ixPoi, logDetailMap.get(entry.getKey()));
			Set<String> logSet = new HashSet<String>();
			
			//*******************************
			//该名称:
			if(ixPoi.isDelOfficeOriginCHName()){
				logSet.add("改名称");
			}else{
				IxPoiName ixpoiNameooc = ixPoi.getOfficeOriginCHName();
				if(ixpoiNameooc != null ){
					OperationType ixPoiNameOpType = ixpoiNameooc.getOpType();
						if(ixPoiNameOpType.equals(OperationType.INSERT)){//ixPoi 主表是修改,IxPoiName为新增
							logSet.add("改名称");
						}
						if(ixPoiNameOpType.equals(OperationType.UPDATE)){//ixPoi 主表是修改,IxPoiName为修改
							//判断履历是否更改过 name字段 
							if(ixpoiNameooc.hisOldValueContains("NAME")){
								logSet.add("改名称");
							}
						}
				}
			}
			
			//改分类:
			if(ixPoi.getMainrow().hisOldValueContains("KIND_CODE")){
				logSet.add("改分类");
			}
			//改电话:
			if(ixPoi.getSubRowByName("contacts") != null && ixPoi.getSubRowByName("contacts").size() > 0 ){
				for(BasicRow contactBr : ixPoi.getSubRowByName("contacts")){
					IxPoiContact ixpoicontBr = (IxPoiContact) contactBr;
					if(ixpoicontBr.getContactType() == 1 || ixpoicontBr.getContactType() == 2){
						if(ixpoicontBr.isHisChanged()){
							logSet.add("改电话");
							break;
						}
					}
				}
			}
			//改地址:
			if(ixPoi.getSubRowByName("addresses") != null && ixPoi.getSubRowByName("addresses").size() > 0 ){
				for(BasicRow addressBr : ixPoi.getSubRowByName("addresses")){
					IxPoiAddress ixpoiaddrBr = (IxPoiAddress) addressBr;
						if((ixpoiaddrBr.getLangCode() == "CHI" || ixpoiaddrBr.getLangCode() == "CHT") && ixpoiaddrBr.hisOldValueContains("FULLNAME")){
							logSet.add("改地址");
							break;
						}
				}

			}
			//改邮编
			if(ixPoi.getMainrow().hisOldValueContains("POST_CODE")){
				logSet.add("改邮编");
			}
			//改FATHERSON
			//先判断是否存在已删除的数据 
			if(ixPoi.isDelIxPoiChildren()){
				logSet.add("改FATHERSON");
			}else{
				if(ixPoi.getSubRowByName("children") != null && ixPoi.getSubRowByName("children").size() > 0 ){
					for(BasicRow childrenBr : ixPoi.getSubRowByName("children")){
						IxPoiChildren ixpoichildrenBr = (IxPoiChildren) childrenBr;
							if(ixpoichildrenBr.isHisChanged()){
								logSet.add("改FATHERSON");
								break;
							}
					}
				}
			}
			
			//改GUIDEX
			if(ixPoi.getMainrow().hisOldValueContains("X_GUIDE")){
				logSet.add("改GUIDEX");
			}
			//改GUIDEY
			if(ixPoi.getMainrow().hisOldValueContains("Y_GUIDE")){
				logSet.add("改GUIDEY");
			}
			//改POI_LEVEL
			if(ixPoi.getMainrow().hisOldValueContains("LEVEL")){
				logSet.add("改POI_LEVEL");
			}
			//改RELATIONL
			if(ixPoi.getMainrow().hisOldValueContains("GEOMETRY")){
				logSet.add("改RELATIONL");
			}
			//改多义性 			
			if(ixPoi.getSubRowByName("restaurants") != null && ixPoi.getSubRowByName("restaurants").size() > 0 ){
				for(BasicRow restaurantBr : ixPoi.getSubRowByName("restaurants")){
					IxPoiRestaurant ixpoiRestaurantBr = (IxPoiRestaurant) restaurantBr;
						if(ixpoiRestaurantBr.hisOldValueContains("FOOD_TYPE")){
							logSet.add("改多义性");
							break;
						}
				}
			}
			//改CHAIN
			if(ixPoi.getMainrow().hisOldValueContains("CHAIN")){
				logSet.add("改CHAIN");
			}
			//改RATING			
			if(ixPoi.getSubRowByName("hotels") != null && ixPoi.getSubRowByName("hotels").size() > 0 ){
				for(BasicRow hotelBr : ixPoi.getSubRowByName("hotels")){
					IxPoiHotel ixpoiHotelBr = (IxPoiHotel) hotelBr;
						if(ixpoiHotelBr.hisOldValueContains("RATING")){
							logSet.add("改RATING");
							break;
						}
				}
			}
			//改FIELD_VERIFIED  IX_POI_FLAG_METHOD
			if(ixPoi.getSubRowByName("nameTones") != null && ixPoi.getSubRowByName("nameTones").size() > 0 ){
				for(BasicRow flagMethodBr : ixPoi.getSubRowByName("nameTones")){
					IxPoiFlagMethod ixpoiflagMethodBr = (IxPoiFlagMethod) flagMethodBr;
						if(ixpoiflagMethodBr.hisOldValueContains("FIELD_VERIFIED")){
							logSet.add("改FIELD_VERIFIED");
							break;
						}
				}
			}
			//改24小时
			if(ixPoi.getMainrow().hisOldValueContains("OPEN_24H")){
				logSet.add("改24小时");
			}
			//改内部POI  
			if(ixPoi.getMainrow().hisOldValueContains("INDOOR")){
				logSet.add("改内部POI");
			}
			
			//改同一关系			
			if(ixSamePoiLogSet != null && ixSamePoiLogSet.size() > 0 && ixSamePoiLogSet.contains(entry.getKey())){
				logSet.add("改同一关系");
			}
			
			String movflag = "F";
			//位移标记:MOVFLAG
			if(ixPoi.getMainrow().hisOldValueContains("GEOMETRY")){
				movflag = "T";
			}
			if(perstmt==null){
				perstmt = conn.prepareStatement(sql);
			}
			
			perstmt.setString(1,movflag);
			if(logSet.size() > 0){
				System.out.println("log: "+StringUtils.join(logSet.toArray(),"|"));
				perstmt.setString(2,StringUtils.join(logSet.toArray(),"|"));
			}else{
				perstmt.setString(2,null);
			}
			perstmt.setLong(3, entry.getKey());
			perstmt.addBatch();
		}
		return perstmt;
	}

	
	private static Set<Long> queryGroupIdsBypids(Connection conn, Collection<Long> pidCollection) throws Exception {
		try{
			Clob pidClob = ConnectionUtil.createClob(conn);
			pidClob.setString(1, StringUtils.join(pidCollection, ","));
			
			String selectSql = " SELECT distinct p.GROUP_ID  FROM IX_SAMEPOI_PART p WHERE p.poi_pid in  (select column_value from table(clob_to_table(?))) ";
			ResultSetHandler<Set<Long>> rs = new ResultSetHandler<Set<Long>>() {
				
				@Override
				public Set<Long> handle(ResultSet rs) throws SQLException {
					Set<Long> result = new HashSet<Long>();
					while(rs.next()){
						long groupId = rs.getLong("GROUP_ID");
						result.add(groupId);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Set<Long> result = run.query(conn,selectSql,pidClob, rs);
			return result;
		}catch(Exception e){
//			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询groupId 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static void main(String[] args) throws Exception{
		//PoiTaskTabLogDependent RefinementLogDependent = new PoiTaskTabLogDependent();
//		RefinementLogDependent.refinementLogDependentMain(13);
		//RefinementLogDependent.refinementLogDependentMain(Integer.parseInt(args[0]));
	}
	

}
