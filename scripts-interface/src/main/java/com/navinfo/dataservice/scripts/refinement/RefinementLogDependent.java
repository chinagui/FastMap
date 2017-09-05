package com.navinfo.dataservice.scripts.refinement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/** 
 * @ClassName: RefinementLogDependent
 * @author songdongyan
 * @date 2017年5月8日
 * @Description: 精细化作业与日志相关的部分
 * 根据大区库ID，获取连接
 * 获取新增、修改、删除的PID(POI)
 * selector加载新增、修改的POI对象
 * 加载hislog
 * 逐一判断isHislogChanged,isHisLog.contains()
 */
public class RefinementLogDependent {

	String deletedSql = "INSERT INTO REFINED_LOG_DEPENDENT (PID,DELFLAG) VALUES (?,?)";
	String updatedSql = "INSERT INTO REFINED_LOG_DEPENDENT (PID,MOVEFLAG,LOG) VALUES (?,?,?)";
	String insertedSql = "INSERT INTO REFINED_LOG_DEPENDENT (PID,ADDFLAG) VALUES (?,?)";

	PreparedStatement perstmtDeleted = null;
	PreparedStatement perstmtUpdated = null;
	PreparedStatement perstmtInserted = null;


	
	public void refinementLogDependentMain(int dbId) throws Exception{
		Connection conn = null;
		try{
			DbInfo dbInfo = DbService.getInstance().getDbById(dbId);

			OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
			conn = schema.getPoolDataSource().getConnection();
			createTable(conn);
			run(conn);
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

	
	public void run(Connection conn) throws Exception{
		try{
			//读履历获取增删改的pid
			LogReader logReader = new LogReader(conn);
			List<String> a = new ArrayList<String>();
			a.add("1");
			a.add("2");
			Map<Integer,Collection<Long>> pidMap = logReader.getUpdatedObjSpecial("IX_POI", "IX_POI", null, null, null);
//			Map<Integer,Collection<Long>> pidMap = logReader.getUpdatedObj("IX_POI", "IX_POI", a, "201704050000", "201704120000");
//			Map<Integer,Collection<Long>> pidMap = logReader.getUpdatedObj("IX_POI", "IX_POI", a, "201704050000");

			//新增
			Collection<Long> inserted = pidMap.get(1);
			if(inserted!=null&&!inserted.isEmpty()){
				for(Long pid:inserted){
					if(perstmtInserted==null){
						perstmtInserted = conn.prepareStatement(insertedSql);
					}
					perstmtInserted.setLong(1,pid);
					perstmtInserted.setInt(2,1);
					perstmtInserted.addBatch();
				}
			}

			//修改
			Collection<Long> updated = pidMap.get(3);
			if(updated!=null&&!updated.isEmpty()){
				perstmtUpdated = generate(conn,OperationType.UPDATE,updated,updatedSql,perstmtUpdated);
			}
			
			//删除
			Collection<Long> deleted = pidMap.get(2);
			if(deleted!=null&&!deleted.isEmpty()){
				for(Long pid:deleted){
					if(perstmtDeleted==null){
						perstmtDeleted = conn.prepareStatement(deletedSql);
					}
					perstmtDeleted.setLong(1,pid);
					perstmtDeleted.setInt(2,1);
					perstmtDeleted.addBatch();
				}
			}
			
			if(perstmtDeleted!=null){
				perstmtDeleted.executeBatch();
			}
			
			if(perstmtInserted!=null){
				perstmtInserted.executeBatch();
			}
			if(perstmtUpdated!=null){
				perstmtUpdated.executeBatch();
			}
		} catch (Exception e) {
			throw e;
		} 
		finally{
			DbUtils.closeQuietly(perstmtDeleted);
			DbUtils.closeQuietly(perstmtInserted);
			DbUtils.closeQuietly(perstmtUpdated);
		}
	}

	/**
	 * @param conn 
	 * @param opType 
	 * @param pidCollection
	 * @param sql
	 * @param perstmt
	 * @throws Exception 
	 */
	private PreparedStatement generate(Connection conn, OperationType opType, Collection<Long> pidCollection, String sql, PreparedStatement perstmt) throws Exception {
		//加载对象
		Map<Long,BasicObj> updatedObjs = ObjBatchSelector.selectByPids(conn, "IX_POI", null, false, pidCollection, false, true);
		Map<Long, List<LogDetail>> logDetailMap = PoiLogDetailStat.loadAllLog(conn, updatedObjs.keySet());
		for(Map.Entry<Long, BasicObj> entry:updatedObjs.entrySet()){
			BasicObj ixPoi= entry.getValue();
			ObjHisLogParser.parse(ixPoi, logDetailMap.get(entry.getKey()));
			Set<String> logSet = new HashSet<String>();
			if(ixPoi.hisOldValueContains("IX_POI_RESTAURANT")){
				logSet.add("改风味类型");
			}
			if(ixPoi.getMainrow().hisOldValueContains("CHAIN")){
				logSet.add("改品牌");
			}
			if(ixPoi.hisOldValueContains("IX_POI_ADDRESS")){
				logSet.add("改地址");
			}
			if(ixPoi.hisOldValueContains("IX_POI_CONTACT")){
				logSet.add("改电话");
			}
			if(ixPoi.hisOldValueContains("IX_POI_NAME")){
				logSet.add("改名称");
			}
			if(ixPoi.getMainrow().hisOldValueContains("X_GUIDE")){
				logSet.add("改GUIDEX");
			}
			if(ixPoi.getMainrow().hisOldValueContains("Y_GUIDE")){
				logSet.add("改GUIDEY");
			}
			if(ixPoi.getMainrow().hisOldValueContains("KIND_CODE")){
				logSet.add("改分类");
			}
			if((ixPoi.getMainrow().hisOldValueContains("INDOOR"))
					||(ixPoi.getMainrow().hisOldValueContains("OPEN_24H"))
					||(ixPoi.hisOldValueContains("IX_POI_PARKING","PARKING_TYPE"))
					||(ixPoi.hisOldValueContains("IX_POI_HOTEL","RATING"))){
				logSet.add("改标注");
			}
			if(ixPoi.getMainrow().hisOldValueContains("LEVEL")){
				logSet.add("改等级");
			}

			
			if(perstmt==null){
				perstmt = conn.prepareStatement(sql);
			}
			perstmt.setLong(1,entry.getKey());
			if(ixPoi.getMainrow().hisOldValueContains("GEOMETRY")){
				perstmt.setInt(2,1);
			}else{
				perstmt.setInt(2,0);
			}

			if(logSet.size() > 0){
				perstmt.setString(3,StringUtils.join(logSet.toArray(),"|"));
			}else{
				perstmt.setString(3,null);
			}
			perstmt.addBatch();
		}
		return perstmt;
	}

	
	public static void main(String[] args) throws Exception{
		RefinementLogDependent RefinementLogDependent = new RefinementLogDependent();
//		RefinementLogDependent.refinementLogDependentMain(13);
		RefinementLogDependent.refinementLogDependentMain(Integer.parseInt(args[0]));
	}
	

}
